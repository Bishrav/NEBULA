#!/usr/bin/env python3
"""Generate human-reviewable engineering information-need candidates from a corpus manifest."""

import argparse
import csv
import hashlib
import json
import re
from collections import Counter
from pathlib import Path


CATEGORIES = ["debugging", "deployment", "configuration", "networking", "security", "observability",
               "database administration", "performance", "migration", "compatibility", "architecture",
               "failure recovery", "API usage"]
KEYWORDS = {
    "debugging": ("debug", "troubleshoot", "diagnos", "error", "failure", "problem"),
    "deployment": ("deploy", "install", "helm", "release", "upgrade", "start"),
    "configuration": ("config", "setting", "parameter", "option", "environment"),
    "networking": ("network", "proxy", "tls", "ssl", "socket", "connection", "dns"),
    "security": ("security", "auth", "permission", "certificate", "encryption", "role", "access"),
    "observability": ("log", "metric", "monitor", "trace", "event", "audit", "observability"),
    "database administration": ("database", "table", "index", "replication", "backup", "vacuum", "role"),
    "performance": ("performance", "cache", "latency", "throughput", "resource", "parallel", "memory"),
    "migration": ("migrat", "convert", "compatibility", "upgrade", "version"),
    "compatibility": ("compatib", "version", "deprecated", "support", "release"),
    "architecture": ("architecture", "component", "controller", "cluster", "control plane", "design"),
    "failure recovery": ("failover", "recover", "restore", "resilien", "replica", "recovery"),
    "API usage": ("api", "endpoint", "request", "client", "resource", "object", "command"),
}


def clean_topic(value):
    value = re.sub(r"\s*\{#[^}]+\}", "", value)
    value = re.sub(r"[*_`#]+", "", value)
    value = re.sub(r"\[[^]]+\]\([^)]*\)", lambda m: m.group(0).split("]")[0].lstrip("["), value)
    value = re.sub(r"\{\{<.*?>\}\}", "", value)
    return re.sub(r"\s+", " ", value).strip(" .:;-")


def headings(text):
    return [clean_topic(m.group(2)) for m in re.finditer(r"^(#{1,6})\s+(.+?)\s*$", text, re.M)
            if len(clean_topic(m.group(2))) >= 4]


def category_scores(text):
    lowered = text.lower()
    return {name: sum(lowered.count(word) for word in words) for name, words in KEYWORDS.items()}


def category(text):
    scores = category_scores(text)
    return max(CATEGORIES, key=lambda name: (scores[name], -CATEGORIES.index(name)))


def make_query(topic, cat, project):
    templates = {
        "debugging": f"How should an engineer troubleshoot {topic} when it does not behave as expected?",
        "deployment": f"What deployment steps and checks are recommended for {topic} in {project}?",
        "configuration": f"How should an engineer configure {topic} safely in {project}?",
        "networking": f"How does an engineer configure or diagnose networking for {topic} in {project}?",
        "security": f"What security controls should be applied when using {topic} in {project}?",
        "observability": f"How can an engineer monitor and verify {topic} in {project}?",
        "database administration": f"How should an administrator operate {topic} safely in {project}?",
        "performance": f"How can an engineer investigate performance issues involving {topic} in {project}?",
        "migration": f"What should an engineer check when migrating or upgrading {topic} in {project}?",
        "compatibility": f"How can an engineer verify version compatibility for {topic} in {project}?",
        "architecture": f"How does {topic} fit into the architecture of {project}?",
        "failure recovery": f"How should an engineer recover from a failure involving {topic} in {project}?",
        "API usage": f"How should an engineer use the API or interface for {topic} in {project}?",
    }
    return templates[cat]


def flags(query, text, project):
    lowered = query.lower() + " " + text.lower()
    return {
        "freshness_sensitive": "version" in lowered or "upgrade" in lowered or "release" in lowered or "deprecated" in lowered,
        "authority_sensitive": "security" in lowered or "recommended" in lowered or "official" in lowered or "safe" in lowered,
        "exact_identifier_dependency": bool(re.search(r"\b(?:kube|kubectl|postgres|sql|api|tls|ssl|cgroup|yaml)\w*\b", lowered)),
        "evidence_required": True,
    }


def generate(manifest_path, output, target):
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    candidates = []
    for doc in manifest["documents"]:
        path = manifest_path.parent.parent / doc["text_path"]
        text = path.read_text(encoding="utf-8")
        topic_list = headings(text)
        if not topic_list:
            sentences = [s.strip() for s in re.split(r"[.!?]\s+", text) if len(s.strip()) > 30]
            topic_list = [clean_topic(sentences[0])[:100]] if sentences else []
        for topic in topic_list[:3]:
            if len(topic.split()) < 2 or topic.lower() in {"contents", "references", "see also"}:
                continue
            scores = category_scores(topic + " " + text[:6000])
            ranked_categories = [name for name in sorted(CATEGORIES, key=lambda name: (-scores[name], CATEGORIES.index(name))) if scores[name] > 0]
            if not ranked_categories:
                ranked_categories = [category(topic)]
            for cat in ranked_categories[:4]:
                query = make_query(topic, cat, doc["project"])
                flags_data = flags(query, text, doc["project"])
                fingerprint = hashlib.sha256((query + doc["document_id"]).lower().encode("utf-8")).hexdigest()[:12]
                candidates.append({"query_id": f"candidate-{fingerprint}", "query_text": query, "category": cat,
                                   "project": doc["project"], "difficulty": "advanced" if len(topic.split()) > 7 else "intermediate",
                                   **flags_data, "source_generation_method": "deterministic corpus-heading paraphrase; human review required",
                                   "grounding_document_id": doc["document_id"], "review_status": "NEEDS_HUMAN_REVIEW",
                                   "grounding_excerpt_checksum": doc["normalized_checksum"]})
    unique = {}
    for row in candidates:
        text_key = re.sub(r"\s+", " ", row["query_text"].lower()).strip()
        unique.setdefault(text_key, row)
    grouped = {name: sorted((row for row in unique.values() if row["category"] == name), key=lambda row: row["query_id"]) for name in CATEGORIES}
    ordered = []
    quota, remainder = divmod(target, len(CATEGORIES))
    for index, name in enumerate(CATEGORIES):
        ordered.extend(grouped[name][:quota + (1 if index < remainder else 0)])
    if len(ordered) < target:
        selected = {row["query_id"] for row in ordered}
        ordered.extend(row for row in sorted(unique.values(), key=lambda row: row["query_id"]) if row["query_id"] not in selected)
        ordered = ordered[:target]
    ordered.sort(key=lambda row: (row["category"], row["project"], row["query_id"]))
    output.parent.mkdir(parents=True, exist_ok=True)
    fields = ["query_id", "query_text", "category", "project", "difficulty", "freshness_sensitive", "authority_sensitive",
              "exact_identifier_dependency", "evidence_required", "source_generation_method", "grounding_document_id",
              "grounding_excerpt_checksum", "review_status"]
    with output.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields, delimiter="|", lineterminator="\n")
        writer.writeheader(); writer.writerows(ordered)
    return ordered


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--target", type=int, default=400)
    args = parser.parse_args()
    rows = generate(args.manifest, args.output, args.target)
    print(json.dumps({"status": "created", "candidates": len(rows), "review_status": "NEEDS_HUMAN_REVIEW"}, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

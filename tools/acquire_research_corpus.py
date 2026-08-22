#!/usr/bin/env python3
"""Acquire a deterministic, provenance-tracked research corpus from approved checkouts.

The tool intentionally requires local upstream checkouts. Clone/authentication policy is
kept outside the corpus writer, while this tool verifies the remote, commit, file scope,
license metadata, normalization, deduplication, and checksums before admission.
"""

import argparse
import hashlib
import json
import re
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path


REGISTRY_FIELDS = ["source_id", "project", "upstream_repository", "scope", "source_url", "license",
                   "attribution", "license_evidence_url", "status", "notes"]
ALLOWED = {"ACCEPTED"}
DOC_EXTENSIONS = {".md", ".mdx", ".sgml", ".xml"}


def digest(data):
    return hashlib.sha256(data).hexdigest()


def run_git(repo, *args):
    return subprocess.check_output(["git", "-C", str(repo), *args], text=True, stderr=subprocess.STDOUT).strip()


def parse_registry(path):
    rows = []
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        fields = [item.strip() for item in line.split("|")]
        if fields == REGISTRY_FIELDS:
            continue
        if len(fields) != len(REGISTRY_FIELDS):
            raise ValueError(f"registry line {line_number}: expected {len(REGISTRY_FIELDS)} fields")
        row = dict(zip(REGISTRY_FIELDS, fields))
        if row["status"] not in {"ACCEPTED", "REJECTED", "NEEDS_REVIEW"}:
            raise ValueError(f"registry line {line_number}: invalid status {row['status']}")
        rows.append(row)
    return rows


def strip_front_matter(text):
    if text.startswith("---\n"):
        end = text.find("\n---", 4)
        if end >= 0:
            return text[end + 4:].lstrip("\r\n"), "removed YAML front matter"
    return text, "no front matter"


def normalize_markdown(text):
    text, note = strip_front_matter(text.replace("\r\n", "\n").replace("\r", "\n"))
    lines = []
    for line in text.splitlines():
        if "<!--" in line and "-->" in line:
            line = re.sub(r"<!--.*?-->", "", line).strip()
        if line.strip().startswith("{{<") or line.strip().startswith("{{%"):
            continue
        lines.append(line.rstrip())
    normalized = re.sub(r"\n{3,}", "\n\n", "\n".join(lines)).strip() + "\n"
    return normalized, note + "; removed deterministic Hugo shortcode lines and comments"


def normalize_sgml(text):
    text = text.replace("\r\n", "\n").replace("\r", "\n")
    text = re.sub(r"<programlisting[^>]*>(.*?)</programlisting>", lambda m: "\n```\n" + m.group(1).strip() + "\n```\n", text, flags=re.S | re.I)
    text = re.sub(r"<screen[^>]*>(.*?)</screen>", lambda m: "\n```\n" + m.group(1).strip() + "\n```\n", text, flags=re.S | re.I)
    text = re.sub(r"<title[^>]*>(.*?)</title>", lambda m: "\n# " + re.sub(r"<[^>]+>", "", m.group(1)).strip() + "\n", text, flags=re.S | re.I)
    text = re.sub(r"</?(?:para|simpara|section|sect1|sect2|sect3|chapter|appendix|itemizedlist|listitem|variablelist|varlistentry|term|literallayout|blockquote)[^>]*>", "\n", text, flags=re.I)
    text = re.sub(r"<[^>]+>", "", text)
    text = re.sub(r"&(?:lt|gt|amp|quot|apos);", lambda m: {"&lt;": "<", "&gt;": ">", "&amp;": "&", "&quot;": '"', "&apos;": "'"}[m.group(0)], text)
    normalized = re.sub(r"\n{3,}", "\n\n", text).strip() + "\n"
    return normalized, "converted SGML/XML headings, paragraphs, and program listings to stable Markdown-like text"


def normalize(path):
    text = path.read_text(encoding="utf-8", errors="strict")
    if path.suffix.lower() in {".md", ".mdx"}:
        return normalize_markdown(text)
    return normalize_sgml(text)


def source_files(source_id, repo, limit):
    if source_id == "source-kubernetes-docs":
        root = repo / "content" / "en" / "docs"
        allowed = {".md", ".mdx"}
    elif source_id == "source-postgresql-docs":
        root = repo / "doc" / "src" / "sgml"
        allowed = {".sgml", ".xml"}
    else:
        raise ValueError(f"no safe file scope configured for {source_id}")
    if not root.is_dir():
        raise ValueError(f"expected source scope is missing: {root}")
    files = sorted(path for path in root.rglob("*") if path.is_file() and path.suffix.lower() in allowed)
    return files[:limit]


def git_date(repo):
    return run_git(repo, "show", "-s", "--format=%cI", "HEAD")


def acquire(args):
    registry = {row["source_id"]: row for row in parse_registry(args.registry)}
    selected = {"source-kubernetes-docs": (Path(args.kubernetes_repo), args.kubernetes_limit),
                "source-postgresql-docs": (Path(args.postgresql_repo), args.postgresql_limit)}
    retrieved_at = args.retrieved_at or datetime.now(timezone.utc).replace(microsecond=0).isoformat()
    root = args.output_root
    raw_root, normalized_root, manifest_root = root / "raw", root / "normalized", root / "manifests"
    for path in (raw_root, normalized_root, manifest_root, root / "licenses", root / "metadata"):
        path.mkdir(parents=True, exist_ok=True)
    documents, rejected = [], []
    normalized_hashes = {}
    for source_id, (repo, limit) in selected.items():
        row = registry.get(source_id)
        if not row:
            raise ValueError(f"missing registry row: {source_id}")
        if row["status"] not in ALLOWED:
            rejected.append({"source_id": source_id, "reason": f"registry status {row['status']}"})
            continue
        remote = run_git(repo, "config", "--get", "remote.origin.url")
        expected = row["upstream_repository"]
        if expected not in remote.replace(".git", ""):
            raise ValueError(f"{source_id}: unexpected upstream remote {remote}")
        commit = run_git(repo, "rev-parse", "HEAD")
        version = run_git(repo, "describe", "--tags", "--always", "HEAD")
        last_updated = git_date(repo)
        for path in source_files(source_id, repo, limit):
            relative = path.relative_to(repo).as_posix()
            raw = path.read_bytes()
            try:
                normalized, transform = normalize(path)
            except UnicodeError as exc:
                rejected.append({"source_id": source_id, "path": relative, "reason": f"non-UTF8: {exc}"})
                continue
            if not normalized.strip():
                rejected.append({"source_id": source_id, "path": relative, "reason": "empty after normalization"})
                continue
            raw_hash, normalized_hash = digest(raw), digest(normalized.encode("utf-8"))
            if normalized_hash in normalized_hashes:
                rejected.append({"source_id": source_id, "path": relative, "reason": "duplicate normalized checksum", "duplicate_of": normalized_hashes[normalized_hash]})
                continue
            slug = re.sub(r"[^a-z0-9]+", "-", relative.lower()).strip("-")
            document_id = f"{source_id.removeprefix('source-')}-{slug}"
            raw_path = raw_root / source_id / relative
            normalized_path = normalized_root / f"{document_id}.md"
            raw_path.parent.mkdir(parents=True, exist_ok=True)
            normalized_path.parent.mkdir(parents=True, exist_ok=True)
            raw_path.write_bytes(raw)
            normalized_path.write_text(normalized, encoding="utf-8", newline="\n")
            normalized_hashes[normalized_hash] = document_id
            documents.append({
                "document_id": document_id, "project": row["project"], "title": path.stem,
                "source_url": f"https://github.com/{expected}/blob/{commit}/{relative}",
                "upstream_repository": f"https://github.com/{expected}", "upstream_commit": commit,
                "source_type": "official-documentation", "retrieved_at": retrieved_at,
                "license": row["license"], "attribution": row["attribution"],
                "content_checksum": raw_hash, "normalized_checksum": normalized_hash,
                "last_updated": last_updated, "transformation_notes": transform,
                "version": version, "authority_category": "official-project-documentation",
                "graph_links": [], "raw_path": raw_path.relative_to(root).as_posix(),
                "text_path": normalized_path.relative_to(root).as_posix(),
                "word_count": len(re.findall(r"\b\w+\b", normalized)),
            })
    manifest = {"schema_version": "research-corpus-manifest-v1", "corpus_version": "research-corpus-v1",
                "retrieved_at": retrieved_at, "provisional": True,
                "licensing_status": "NEEDS_HUMAN_LICENSING_REVIEW",
                "documents": documents}
    (manifest_root / "research-corpus-v1.json").write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    (manifest_root / "rejected-sources.json").write_text(json.dumps(rejected, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return manifest, rejected


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--registry", type=Path, required=True)
    parser.add_argument("--kubernetes-repo", type=Path, required=True)
    parser.add_argument("--postgresql-repo", type=Path, required=True)
    parser.add_argument("--output-root", type=Path, required=True)
    parser.add_argument("--kubernetes-limit", type=int, default=600)
    parser.add_argument("--postgresql-limit", type=int, default=250)
    parser.add_argument("--retrieved-at")
    args = parser.parse_args()
    try:
        manifest, rejected = acquire(args)
        print(json.dumps({"status": "created", "documents": len(manifest["documents"]), "rejected": len(rejected), "provisional": True}, sort_keys=True))
        return 0
    except (OSError, ValueError, subprocess.CalledProcessError, UnicodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())

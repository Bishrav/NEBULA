(function () {
  'use strict';
  var form = document.getElementById('search-form');
  var query = document.getElementById('query');
  var mode = document.getElementById('mode');
  var limit = document.getElementById('limit');
  var apiBase = document.getElementById('api-base');
  var results = document.getElementById('results');
  var statusText = document.getElementById('status-text');
  var statusDot = document.getElementById('status-dot');
  var activeQuery = '';
  var activeMode = 'bm25';
  var evidenceDialog = document.getElementById('evidence-dialog');

  form.addEventListener('submit', function (event) { event.preventDefault(); search(); });
  document.querySelectorAll('[data-query]').forEach(function (button) {
    button.addEventListener('click', function () { query.value = button.getAttribute('data-query'); search(); });
  });
  results.addEventListener('click', function (event) {
    var evidenceButton = event.target.closest('[data-evidence-path]');
    if (evidenceButton) { openEvidence(evidenceButton.dataset.evidencePath); return; }
    var button = event.target.closest('[data-feedback-useful]');
    if (!button) return;
    button.disabled = true;
    fetch(apiBase.value.replace(/\/$/, '') + '/v1/feedback', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query: activeQuery, mode: activeMode, documentId: button.dataset.feedbackId,
        sourcePath: button.dataset.feedbackPath, useful: button.dataset.feedbackUseful === 'true' })
    }).then(function (response) { if (!response.ok) throw new Error('feedback failed'); button.textContent = 'Recorded'; })
      .catch(function () { button.disabled = false; button.textContent = 'Try again'; });
  });
  document.getElementById('close-evidence').addEventListener('click', function () { evidenceDialog.close(); });

  function search() {
    var started = performance.now();
    activeQuery = query.value.trim();
    activeMode = mode.value || 'bm25';
    var params = new URLSearchParams({ q: query.value.trim(), limit: limit.value });
    if (mode.value) params.set('mode', mode.value);
    if (!query.value.trim()) return;
    setStatus('Searching', true);
    fetch(apiBase.value.replace(/\/$/, '') + '/v1/search?' + params.toString())
      .then(function (response) { if (!response.ok) throw new Error('API returned ' + response.status); return response.json(); })
      .then(function (payload) {
        document.getElementById('result-count').textContent = payload.results.length;
        document.getElementById('elapsed').textContent = Math.round(performance.now() - started) + ' ms';
        render(payload.results);
        setStatus('Ready', false);
      })
      .catch(function (error) { results.innerHTML = '<div class="empty-state error"><h2>Search unavailable</h2><p>' + escapeHtml(error.message) + '. Is the NEBULA API running?</p></div>'; setStatus('Offline', false); });
  }

  function render(items) {
    if (!items.length) { results.innerHTML = '<div class="empty-state"><h2>No matching evidence</h2><p>Try a broader question or another ranking mode.</p></div>'; return; }
    results.innerHTML = items.map(function (item) {
        var signals = Object.keys(item.termContributions || {}).map(function (key) { return '<span class="signal">' + escapeHtml(key) + ' <b>' + Number(item.termContributions[key]).toFixed(4) + '</b></span>'; }).join('');
      return '<article class="result-card"><div class="result-head"><div><h2 class="result-title">' + escapeHtml(item.title) + '</h2><div class="result-path">' + escapeHtml(item.sourcePath) + '</div></div><div class="score">score ' + Number(item.score).toFixed(4) + '</div></div><div class="signals">' + signals + '</div><div class="feedback-actions"><button data-evidence-path="' + escapeHtml(item.sourcePath) + '">Open evidence</button><span>Was this useful?</span><button data-feedback-useful="true" data-feedback-id="' + escapeHtml(item.documentId) + '" data-feedback-path="' + escapeHtml(item.sourcePath) + '">Yes</button><button data-feedback-useful="false" data-feedback-id="' + escapeHtml(item.documentId) + '" data-feedback-path="' + escapeHtml(item.sourcePath) + '">Not yet</button></div></article>';
    }).join('');
  }

  function setStatus(text, active) { statusText.textContent = text; statusDot.style.background = active ? '#f8c76b' : (text === 'Offline' ? '#ff788b' : 'var(--green)'); }
  function openEvidence(sourcePath) {
    fetch(apiBase.value.replace(/\/$/, '') + '/v1/documents?path=' + encodeURIComponent(sourcePath))
      .then(function (response) { if (!response.ok) throw new Error('document unavailable'); return response.json(); })
      .then(function (payload) { document.getElementById('evidence-title').textContent = payload.title; document.getElementById('evidence-path').textContent = payload.sourcePath; document.getElementById('evidence-text').textContent = payload.text; evidenceDialog.showModal(); })
      .catch(function () { setStatus('Evidence unavailable', false); });
  }
  function escapeHtml(value) { return String(value).replace(/[&<>"']/g, function (character) { return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[character]; }); }
}());

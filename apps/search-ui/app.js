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
  var metricsRefresh = document.getElementById('refresh-metrics');
  var suggestions = document.getElementById('suggestions');
  var suggestionTimer;
  var suggestionIndex = -1;

  form.addEventListener('submit', function (event) { event.preventDefault(); search(); });
  query.addEventListener('input', function () {
    clearTimeout(suggestionTimer);
    suggestionTimer = setTimeout(loadSuggestions, 120);
  });
  query.addEventListener('keydown', function (event) {
    var options = suggestions.querySelectorAll('[data-suggestion]');
    if (event.key === 'ArrowDown' && options.length) { event.preventDefault(); suggestionIndex = Math.min(suggestionIndex + 1, options.length - 1); highlightSuggestion(options); }
    if (event.key === 'ArrowUp' && options.length) { event.preventDefault(); suggestionIndex = Math.max(suggestionIndex - 1, 0); highlightSuggestion(options); }
    if (event.key === 'Enter' && suggestionIndex >= 0 && options[suggestionIndex]) { event.preventDefault(); options[suggestionIndex].click(); }
    if (event.key === 'Escape') clearSuggestions();
  });
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
    }).then(function (response) { if (!response.ok) throw new Error('feedback failed'); button.textContent = 'Recorded'; loadMetrics(); })
      .catch(function () { button.disabled = false; button.textContent = 'Try again'; });
  });
  document.getElementById('close-evidence').addEventListener('click', function () { evidenceDialog.close(); });
  metricsRefresh.addEventListener('click', loadMetrics);
  loadMetrics();

  function loadSuggestions() {
    var value = query.value.trim();
    if (!value || value.length < 2) { clearSuggestions(); return; }
    var prefix = value.split(/\s+/).pop();
    fetch(apiBase.value.replace(/\/$/, '') + '/v1/suggest?q=' + encodeURIComponent(prefix) + '&limit=6')
      .then(function (response) { return response.ok ? response.json() : { suggestions: [] }; })
      .then(function (payload) { renderSuggestions(payload.suggestions || [], value.slice(0, value.length - prefix.length)); })
      .catch(clearSuggestions);
  }

  function renderSuggestions(items, leadingText) {
    suggestionIndex = -1;
    suggestions.innerHTML = items.map(function (item) {
      return '<button type="button" role="option" data-suggestion="' + escapeHtml(leadingText + item) + '">' + escapeHtml(leadingText + item) + '</button>';
    }).join('');
    suggestions.querySelectorAll('[data-suggestion]').forEach(function (button) {
      button.addEventListener('click', function () { query.value = button.dataset.suggestion; clearSuggestions(); search(); });
    });
  }

  function highlightSuggestion(options) {
    options.forEach(function (option, index) { option.classList.toggle('selected', index === suggestionIndex); });
  }

  function clearSuggestions() { suggestionIndex = -1; suggestions.innerHTML = ''; }

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
        loadMetrics();
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
  function loadMetrics() {
    var base = apiBase.value.replace(/\/$/, '');
    Promise.all([fetch(base + '/v1/metrics/search'), fetch(base + '/v1/metrics/feedback')])
      .then(function (responses) { return Promise.all(responses.map(function (response) { if (!response.ok) throw new Error('metrics unavailable'); return response.json(); })); })
      .then(function (payloads) {
        var searchMetrics = payloads[0];
        var feedbackMetrics = payloads[1];
        document.getElementById('metric-searches').textContent = searchMetrics.total;
        document.getElementById('metric-zero').textContent = searchMetrics.total ? ((searchMetrics.zeroResults / searchMetrics.total) * 100).toFixed(0) + '%' : '0%';
        document.getElementById('metric-latency').textContent = Number(searchMetrics.averageLatencyMs).toFixed(1) + ' ms';
        document.getElementById('metric-useful').textContent = feedbackMetrics.total ? ((feedbackMetrics.useful / feedbackMetrics.total) * 100).toFixed(0) + '%' : '—';
        var modes = Object.keys(searchMetrics.byMode || {}).map(function (key) { return key + ': ' + searchMetrics.byMode[key]; });
        document.getElementById('metric-modes').textContent = modes.length ? 'Mode usage: ' + modes.join(' · ') : 'Mode usage will appear after searches.';
      })
      .catch(function () { document.getElementById('metric-modes').textContent = 'Pilot analytics unavailable'; });
  }
  function openEvidence(sourcePath) {
    fetch(apiBase.value.replace(/\/$/, '') + '/v1/documents?path=' + encodeURIComponent(sourcePath))
      .then(function (response) { if (!response.ok) throw new Error('document unavailable'); return response.json(); })
      .then(function (payload) { document.getElementById('evidence-title').textContent = payload.title; document.getElementById('evidence-path').textContent = payload.sourcePath; document.getElementById('evidence-text').textContent = payload.text; evidenceDialog.showModal(); })
      .catch(function () { setStatus('Evidence unavailable', false); });
  }
  function escapeHtml(value) { return String(value).replace(/[&<>"']/g, function (character) { return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[character]; }); }
}());

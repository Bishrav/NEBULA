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
  var studyButton = document.getElementById('study-session');
  var consentDialog = document.getElementById('consent-dialog');
  var consentCheckbox = document.getElementById('consent-checkbox');
  var reflectionDialog = document.getElementById('reflection-dialog');
  var pendingReflectionTask = null;
  var consented = window.localStorage.getItem('nebula-pilot-consent-v1') === 'true';
  var sessionId = consented ? getSessionId() : null;
  var taskTimers = {};
  var activeTaskId = null;
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
  document.querySelector('.task-panel').addEventListener('click', function (event) {
    var button = event.target.closest('[data-task-action]');
    if (!button) return;
    var card = button.closest('[data-task-id]');
    var taskId = card.dataset.taskId;
    var action = button.dataset.taskAction;
    if (!consented) { consentDialog.showModal(); return; }
    if (action === 'start') {
      activeTaskId = taskId;
      taskTimers[taskId] = performance.now();
      postTask(taskId, 'start', false, 0).then(function () {
        card.querySelector('[data-task-action="start"]').disabled = true;
        card.querySelectorAll('[data-task-action="complete"]').forEach(function (completeButton) { completeButton.disabled = false; });
        document.getElementById('task-status').textContent = 'Task in progress';
      });
    } else {
      var duration = Math.round(performance.now() - (taskTimers[taskId] || performance.now()));
      postTask(taskId, 'complete', button.dataset.taskSuccess === 'true', duration).then(function () {
        card.querySelectorAll('button').forEach(function (taskButton) { taskButton.disabled = true; });
        card.classList.add('task-complete');
        activeTaskId = null;
        pendingReflectionTask = taskId;
        document.getElementById('reflection-confidence').value = '3';
        document.getElementById('reflection-note').value = '';
        reflectionDialog.showModal();
        document.getElementById('task-status').textContent = 'Task recorded';
      });
    }
  });
  results.addEventListener('click', function (event) {
    var evidenceButton = event.target.closest('[data-evidence-path]');
    if (evidenceButton) { openEvidence(evidenceButton.dataset.evidencePath); return; }
    var button = event.target.closest('[data-feedback-useful]');
    if (!button) return;
    button.disabled = true;
    fetch(apiBase.value.replace(/\/$/, '') + '/v1/feedback', {
      method: 'POST', headers: researchHeaders(),
      body: JSON.stringify({ query: activeQuery, mode: activeMode, documentId: button.dataset.feedbackId,
        sourcePath: button.dataset.feedbackPath, useful: button.dataset.feedbackUseful === 'true' })
    }).then(function (response) { if (!response.ok) throw new Error('feedback failed'); button.textContent = 'Recorded'; loadMetrics(); })
      .catch(function () { button.disabled = false; button.textContent = 'Try again'; });
  });
  document.getElementById('close-evidence').addEventListener('click', function () { evidenceDialog.close(); });
  studyButton.addEventListener('click', function () {
    if (consented) { setStatus('Study active', false); return; }
    consentDialog.showModal();
  });
  consentCheckbox.addEventListener('change', function () { document.getElementById('accept-consent').disabled = !consentCheckbox.checked; });
  document.getElementById('cancel-consent').addEventListener('click', function () { consentDialog.close(); });
  document.getElementById('accept-consent').addEventListener('click', function () {
    consented = true;
    window.localStorage.setItem('nebula-pilot-consent-v1', 'true');
    sessionId = getSessionId();
    studyButton.textContent = 'Study active';
    consentDialog.close();
    setStatus('Study active', false);
    document.getElementById('task-status').textContent = 'Ready';
  });
  document.getElementById('skip-reflection').addEventListener('click', function () { pendingReflectionTask = null; reflectionDialog.close(); });
  document.getElementById('save-reflection').addEventListener('click', function () {
    if (!pendingReflectionTask) { reflectionDialog.close(); return; }
    postObservation(pendingReflectionTask, Number(document.getElementById('reflection-confidence').value), document.getElementById('reflection-note').value)
      .then(function () { pendingReflectionTask = null; reflectionDialog.close(); setStatus('Reflection recorded', false); });
  });
  if (consented) { studyButton.textContent = 'Study active'; document.getElementById('task-status').textContent = 'Ready'; }
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
    fetch(apiBase.value.replace(/\/$/, '') + '/v1/search?' + params.toString(), { headers: researchHeaders() })
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
  function getSessionId() {
    var key = 'nebula-pilot-session-id';
    var existing = window.localStorage.getItem(key);
    if (existing) return existing;
    var created = window.crypto && window.crypto.randomUUID ? window.crypto.randomUUID() : 'session-' + Date.now() + '-' + Math.random().toString(16).slice(2);
    window.localStorage.setItem(key, created);
    return created;
  }
  function researchHeaders() {
    var headers = { 'Content-Type': 'application/json' };
    if (consented && sessionId) {
      headers['X-Session-Id'] = sessionId;
      headers['X-Research-Consent'] = 'true';
      if (activeTaskId) headers['X-Task-Id'] = activeTaskId;
    }
    return headers;
  }
  function postTask(taskId, action, success, durationMs) {
    return fetch(apiBase.value.replace(/\/$/, '') + '/v1/research/tasks', {
      method: 'POST', headers: researchHeaders(),
      body: JSON.stringify({ taskId: taskId, action: action, success: success, durationMs: durationMs })
    }).then(function (response) { if (!response.ok) throw new Error('task tracking failed'); return response.json(); })
      .catch(function (error) { setStatus('Task unavailable', false); throw error; });
  }
  function postObservation(taskId, confidence, note) {
    return fetch(apiBase.value.replace(/\/$/, '') + '/v1/research/observations', {
      method: 'POST', headers: researchHeaders(),
      body: JSON.stringify({ taskId: taskId, confidence: confidence, note: note })
    }).then(function (response) { if (!response.ok) throw new Error('reflection failed'); return response.json(); })
      .catch(function (error) { setStatus('Reflection unavailable', false); throw error; });
  }
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

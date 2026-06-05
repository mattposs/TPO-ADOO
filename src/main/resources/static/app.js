const API_BASE = '';
let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
  const saved = sessionStorage.getItem('escrims_user');
  if (saved) { currentUser = JSON.parse(saved); enterApp(); }

  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById(`${btn.dataset.tab}-form`).classList.add('active');
    });
  });

  document.querySelectorAll('.nav-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById(`section-${btn.dataset.section}`).classList.add('active');
      onSectionChange(btn.dataset.section);
    });
  });

  document.getElementById('login-form').addEventListener('submit', handleLogin);
  document.getElementById('register-form').addEventListener('submit', handleRegister);
  document.getElementById('logout-btn').addEventListener('click', logout);

  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', e => { if (e.target === overlay) overlay.classList.add('hidden'); });
  });
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') document.querySelectorAll('.modal-overlay').forEach(m => m.classList.add('hidden'));
  });
});

function onSectionChange(section) {
  if (section === 'stats') loadStats();
  if (section === 'profile') loadProfile();
  if (section === 'my-lobbies') loadMyLobbies();
}

async function api(path, options = {}) {
  const url = `${API_BASE}${path}`;
  const opts = { headers: { 'Content-Type': 'application/json', ...options.headers }, ...options };
  if (opts.body && typeof opts.body === 'object') opts.body = JSON.stringify(opts.body);
  try {
    const res = await fetch(url, opts);
    const text = await res.text();
    let data; try { data = JSON.parse(text); } catch { data = text; }
    if (!res.ok) { const msg = (typeof data === 'object' && (data.message || data.error)) || text || `HTTP ${res.status}`; throw new Error(msg); }
    return data;
  } catch (err) {
    if (err.name === 'TypeError' && err.message.includes('fetch')) throw new Error('No se puede conectar al servidor. ¿Está corriendo el backend?');
    throw err;
  }
}

async function handleLogin(e) {
  e.preventDefault();
  const errEl = document.getElementById('login-error');
  errEl.classList.add('hidden');
  const username = document.getElementById('login-username').value.trim();
  const password = document.getElementById('login-password').value;
  try {
    const data = await api('/auth/login', { method: 'POST', body: { username, password } });
    currentUser = { username, ...(typeof data === 'object' ? data : {}) };
    sessionStorage.setItem('escrims_user', JSON.stringify(currentUser));
    enterApp();
    toast('success', `¡Bienvenido de vuelta, ${username}!`);
  } catch (err) { errEl.textContent = err.message; errEl.classList.remove('hidden'); }
}

async function handleRegister(e) {
  e.preventDefault();
  const errEl = document.getElementById('register-error');
  const okEl = document.getElementById('register-success');
  errEl.classList.add('hidden'); okEl.classList.add('hidden');
  const body = {
    username: document.getElementById('reg-username').value.trim(),
    password: document.getElementById('reg-password').value,
    region: document.getElementById('reg-region').value,
    platform: document.getElementById('reg-platform').value,
    visibleRank: document.getElementById('reg-visible-rank').value,
    rank: parseInt(document.getElementById('reg-rank').value) || 1000,
    preferredRole: document.getElementById('reg-role').value,
    availability: document.getElementById('reg-availability').value,
  };
  try {
    await api('/auth/register', { method: 'POST', body });
    okEl.textContent = '¡Cuenta creada! Ya podés iniciar sesión.';
    okEl.classList.remove('hidden');
    document.getElementById('register-form').reset();
    setTimeout(() => document.querySelector('[data-tab="login"]').click(), 1200);
  } catch (err) { errEl.textContent = err.message; errEl.classList.remove('hidden'); }
}

function enterApp() {
  document.getElementById('auth-screen').classList.remove('active');
  document.getElementById('app-screen').classList.add('active');
  document.getElementById('nav-username').textContent = currentUser.username || 'Jugador';
  loadProfile();
}

function logout() {
  currentUser = null;
  sessionStorage.removeItem('escrims_user');
  document.getElementById('app-screen').classList.remove('active');
  document.getElementById('auth-screen').classList.add('active');
  toast('info', 'Sesión cerrada');
}

async function searchLobbies() {
  const region = document.getElementById('filter-region').value || undefined;
  const minRank = document.getElementById('filter-min-rank').value || undefined;
  const maxRank = document.getElementById('filter-max-rank').value || undefined;
  const maxPing = document.getElementById('filter-max-ping').value || undefined;
  const params = new URLSearchParams();
  if (region) params.append('region', region);
  if (minRank) params.append('minRank', minRank);
  if (maxRank) params.append('maxRank', maxRank);
  if (maxPing) params.append('maxPing', maxPing);
  const list = document.getElementById('lobbies-list');
  list.innerHTML = '<div class="loading-state"><div class="loading-spinner"></div></div>';
  try {
    const data = await api(`/findLobbies?${params.toString()}`);
    renderLobbies(list, Array.isArray(data) ? data : (data.lobbies || []), false);
  } catch (err) {
    list.innerHTML = `<div class="empty-state"><div class="empty-icon"></div><p>${err.message}</p></div>`;
    toast('error', err.message);
  }
}

async function loadMyLobbies() {
  const list = document.getElementById('my-lobbies-list');
  list.innerHTML = '<div class="loading-state"><div class="loading-spinner"></div></div>';
  try {
    const params = new URLSearchParams();
    if (currentUser?.username) params.append('host', currentUser.username);
    const data = await api(`/findLobbies?${params.toString()}`);
    renderLobbies(list, Array.isArray(data) ? data : (data.lobbies || []), true);
  } catch (err) {
    list.innerHTML = `<div class="empty-state"><div class="empty-icon"></div><p>${err.message}</p></div>`;
    toast('error', err.message);
  }
}

function renderLobbies(container, lobbies, isOwner) {
  if (!lobbies.length) { container.innerHTML = '<div class="empty-state"><div class="empty-icon"></div><p>No se encontraron lobbies</p></div>'; return; }
  container.innerHTML = lobbies.map(l => {
    const filled = l.players ? l.players.length : 0;
    const max = l.maxPlayers || 10;
    const pct = Math.round((filled / max) * 100);
    const status = l.status?.toLowerCase() || 'open';
    return `<div class="lobby-card" onclick="openLobbyDetail(${JSON.stringify(l).replace(/"/g, '&quot;')})">
      <div class="lobby-card-header"><span class="lobby-game-mode">${l.gameMode || '?v?'}</span><span class="lobby-status-badge status-${status}">${statusLabel(status)}</span></div>
      <div class="lobby-map"> ${l.map || 'Sin mapa'}  ${l.region || ''}</div>
      <div class="lobby-meta">
        <div class="lobby-meta-item"><span class="lobby-meta-label">MMR:</span><span class="lobby-meta-value">${l.minRank ?? ''}${l.maxRank ?? ''}</span></div>
        <div class="lobby-meta-item"><span class="lobby-meta-label">Ping máx:</span><span class="lobby-meta-value">${l.maxPing ?? ''} ms</span></div>
        <div class="lobby-meta-item"><span class="lobby-meta-label">Host:</span><span class="lobby-meta-value">${l.host?.username || l.host || ''}</span></div>
        <div class="lobby-meta-item"><span class="lobby-meta-label">Hora:</span><span class="lobby-meta-value">${formatTime(l.scheduledTime)}</span></div>
      </div>
      <div class="lobby-card-footer">
        <div><div class="players-count">${filled} / ${max} jugadores</div><div class="players-bar"><div class="players-fill" style="width:${pct}%"></div></div></div>
        ${isOwner && status === 'open' ? `<button class="btn btn-danger btn-sm" onclick="event.stopPropagation();cancelLobby(${l.id})">Cancelar</button>` : ''}
        ${isOwner && status === 'open' ? `<button class="btn btn-success btn-sm" onclick="event.stopPropagation();openStartScrim(${l.id},'${l.gameMode}','${l.map}')">Iniciar</button>` : ''}
      </div></div>`;
  }).join('');
}

function statusLabel(s) {
  const map = { open:'Abierto', started:'En juego', finished:'Finalizado', cancelled:'Cancelado', lobbyarmado:'Lobby armado', confirmado:'Confirmado', enjuego:'En juego', buscando:'Buscando' };
  return map[s] || s;
}
function formatTime(dt) {
  if (!dt) return '';
  try { return new Date(dt).toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' }); } catch { return dt; }
}

function openLobbyDetail(lobby) {
  const content = document.getElementById('lobby-detail-content');
  const actions = document.getElementById('lobby-detail-actions');
  document.getElementById('detail-modal-title').textContent = `Lobby #${lobby.id}  ${lobby.gameMode || ''}`;
  const players = lobby.players || [];
  const isHost = lobby.host?.username === currentUser?.username || lobby.host === currentUser?.username;
  const status = lobby.status?.toLowerCase() || 'open';
  content.innerHTML = `
    <div class="detail-section"><div class="detail-section-title">Información del lobby</div>
      <div class="detail-grid">
        <div class="detail-item"><div class="detail-label">Modo</div><div class="detail-value">${lobby.gameMode || ''}</div></div>
        <div class="detail-item"><div class="detail-label">Mapa</div><div class="detail-value">${lobby.map || ''}</div></div>
        <div class="detail-item"><div class="detail-label">Región</div><div class="detail-value">${lobby.region || ''}</div></div>
        <div class="detail-item"><div class="detail-label">Estado</div><div class="detail-value"><span class="lobby-status-badge status-${status}">${statusLabel(status)}</span></div></div>
        <div class="detail-item"><div class="detail-label">MMR requerido</div><div class="detail-value">${lobby.minRank ?? ''}  ${lobby.maxRank ?? ''}</div></div>
        <div class="detail-item"><div class="detail-label">Ping máximo</div><div class="detail-value">${lobby.maxPing ?? ''} ms</div></div>
        <div class="detail-item"><div class="detail-label">Jugadores</div><div class="detail-value">${players.length} / ${lobby.maxPlayers || ''}</div></div>
        <div class="detail-item"><div class="detail-label">Host</div><div class="detail-value">${lobby.host?.username || lobby.host || ''}</div></div>
        <div class="detail-item"><div class="detail-label">Hora programada</div><div class="detail-value">${formatTime(lobby.scheduledTime)}</div></div>
      </div></div>
    <div class="detail-section"><div class="detail-section-title">Jugadores (${players.length})</div>
      <div class="players-list">${players.length ? players.map(p => `<div class="player-item"><span class="player-name">${p.username || p}</span><span class="player-rank">${p.visibleRank || ''} ${p.rank ? ' ' + p.rank + ' MMR' : ''}</span></div>`).join('') : '<p style="color:var(--text-2);font-size:.875rem">Sin jugadores aún</p>'}</div>
    </div>`;
  actions.innerHTML = `
    <button class="btn btn-ghost" onclick="closeModal('lobby-detail-modal')">Cerrar</button>
    ${!isHost && status === 'open' ? `<button class="btn btn-primary" onclick="postularse(${lobby.id})">Postularse</button>` : ''}
    ${isHost && status === 'open' ? `<button class="btn btn-danger" onclick="cancelLobby(${lobby.id});closeModal('lobby-detail-modal')">Cancelar Lobby</button>` : ''}
    ${isHost && status === 'open' ? `<button class="btn btn-success" onclick="openStartScrim(${lobby.id},'${lobby.gameMode}','${lobby.map}');closeModal('lobby-detail-modal')">Iniciar Scrim</button>` : ''}
    ${status === 'started' || status === 'enjuego' ? `<button class="btn btn-warning" onclick="finishScrim(${lobby.id})">Finalizar Scrim</button>` : ''}`;
  openModal('lobby-detail-modal');
}

async function createLobby() {
  const errEl = document.getElementById('create-lobby-error');
  errEl.classList.add('hidden');
  const body = {
    region: document.getElementById('lobby-region').value,
    gameMode: document.getElementById('lobby-game-mode').value,
    map: document.getElementById('lobby-map').value || 'TBD',
    minPlayers: parseInt(document.getElementById('lobby-min-players').value) || 2,
    maxPlayers: parseInt(document.getElementById('lobby-max-players').value) || 10,
    minRank: parseInt(document.getElementById('lobby-min-rank').value) || 0,
    maxRank: parseInt(document.getElementById('lobby-max-rank').value) || 9999,
    maxPing: parseInt(document.getElementById('lobby-max-ping').value) || 150,
    scheduledTime: document.getElementById('lobby-scheduled-time').value || new Date().toISOString(),
    hostUsername: currentUser?.username,
  };
  try {
    await api('/createLobby', { method: 'POST', body });
    closeModal('create-lobby-modal');
    document.getElementById('create-lobby-form').reset();
    toast('success', '¡Lobby creado exitosamente!');
    document.querySelector('[data-section="my-lobbies"]').click();
  } catch (err) { errEl.textContent = err.message; errEl.classList.remove('hidden'); }
}

async function cancelLobby(id) {
  if (!confirm('¿Seguro que querés cancelar este lobby?')) return;
  try { await api(`/${id}/cancelLobby`, { method: 'POST' }); toast('info', 'Lobby cancelado'); loadMyLobbies(); }
  catch (err) { toast('error', err.message); }
}

function openStartScrim(lobbyId, gameMode, map) {
  document.getElementById('scrim-lobby-id').value = lobbyId;
  document.getElementById('scrim-game-mode').value = gameMode || '5v5';
  document.getElementById('scrim-map').value = map || '';
  openModal('start-scrim-modal');
}

async function startScrim() {
  const errEl = document.getElementById('start-scrim-error');
  errEl.classList.add('hidden');
  const body = { lobbyId: parseInt(document.getElementById('scrim-lobby-id').value), gameMode: document.getElementById('scrim-game-mode').value, map: document.getElementById('scrim-map').value || 'TBD' };
  try { await api('/startScrim', { method: 'POST', body }); closeModal('start-scrim-modal'); toast('success', '¡Scrim iniciado! ¡Buena suerte!'); loadMyLobbies(); }
  catch (err) { errEl.textContent = err.message; errEl.classList.remove('hidden'); }
}

async function finishScrim(id) {
  if (!confirm('¿Finalizar este scrim?')) return;
  try { await api(`/${id}/finishScrim`, { method: 'POST' }); toast('success', 'Scrim finalizado'); closeModal('lobby-detail-modal'); loadMyLobbies(); }
  catch (err) { toast('error', err.message); }
}

async function postularse(lobbyId) {
  try {
    await api(`/${lobbyId}/postulaciones`, { method: 'POST', body: { playerUsername: currentUser?.username, rolDeseado: currentUser?.preferredRole || 'FLEX' } });
    closeModal('lobby-detail-modal');
    toast('success', '¡Te postulaste al lobby!');
  } catch (err) { toast('error', err.message); }
}

async function loadStats() {
  if (!currentUser) return;
  const u = currentUser;
  document.getElementById('stat-games').textContent = u.gamesPlayed ?? '0';
  document.getElementById('stat-wins').textContent = u.wins ?? '0';
  document.getElementById('stat-losses').textContent = u.losses ?? '0';
  document.getElementById('stat-kda').textContent = u.kda ?? '';
  document.getElementById('stat-mmr').textContent = u.rank ?? '';
  const games = parseInt(u.gamesPlayed) || 0;
  const wins = parseInt(u.wins) || 0;
  document.getElementById('stat-wr').textContent = games > 0 ? Math.round((wins / games) * 100) + '%' : '';
}

async function loadProfile() {
  if (!currentUser) return;
  const u = currentUser;
  document.getElementById('profile-username').textContent = u.username || '';
  document.getElementById('profile-avatar-letter').textContent = (u.username || '?')[0].toUpperCase();
  document.getElementById('profile-rank-badge').textContent = u.visibleRank || '';
  document.getElementById('profile-region-badge').textContent = u.region || '';
  document.getElementById('profile-platform').textContent = u.platform || '';
  document.getElementById('profile-role').textContent = u.preferredRole || '';
  document.getElementById('profile-availability').textContent = u.availability || '';
  document.getElementById('profile-mmr').textContent = u.rank ? `${u.rank} MMR` : '';
}

function openModal(id) { document.getElementById(id).classList.remove('hidden'); }
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }

function toast(type, message) {
  const icons = { success: '', error: '', info: '?', warning: '' };
  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.innerHTML = `<span>${icons[type] || '?'}</span><span>${message}</span>`;
  document.getElementById('toast-container').appendChild(el);
  setTimeout(() => { el.style.transition = 'opacity .3s,transform .3s'; el.style.opacity = '0'; el.style.transform = 'translateX(100%)'; setTimeout(() => el.remove(), 300); }, 3500);
}



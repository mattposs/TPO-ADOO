/**
 * app.js - eScrims Frontend Application
 *
 * SPA sin frameworks. Maneja autenticacion, creacion y busqueda de scrims,
 * y todas las acciones sobre scrims (iniciar, cancelar, finalizar, postularse).
 *
 * La API_BASE esta vacia porque nginx sirve el frontend en el mismo origen
 * (puerto 8080) y hace proxy de /api/ al backend en puerto 8081.
 */

// =============================================================================
// CONFIGURACION
// =============================================================================

/** Base URL de la API. Vacia = mismo origen (nginx hace proxy a :8081). */
const API_BASE = '';

// =============================================================================
// ESTADO GLOBAL
// =============================================================================

/** Token JWT del usuario autenticado. Null si no hay sesion. */
let authToken = null;

/** Nombre de usuario logueado, para mostrar en la UI. */
let currentUser = null;

// =============================================================================
// UTILIDADES HTTP
// =============================================================================

/**
 * Realiza una peticion HTTP a la API.
 *
 * @param {string} path   - Ruta relativa, ej: '/api/scrims'
 * @param {string} method - Metodo HTTP: 'GET', 'POST', etc.
 * @param {object} [body] - Cuerpo JSON opcional (para POST/PUT)
 * @returns {Promise<any>} Datos deserializados de la respuesta JSON
 * @throws {Error} Si el servidor devuelve un status >= 400
 */
async function apiCall(path, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };

    // Adjuntar JWT si el usuario esta autenticado
    if (authToken) {
        headers['Authorization'] = `Bearer ${authToken}`;
    }

    const options = { method, headers };
    if (body) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(`${API_BASE}${path}`, options);

    // Leer el cuerpo una sola vez para poder mostrarlo en errores
    const text = await response.text();

    if (!response.ok) {
        // Intentar parsear el mensaje de error del backend
        let msg = `Error ${response.status}`;
        try {
            const err = JSON.parse(text);
            msg = err.message || err.error || msg;
        } catch (_) {
            if (text) msg = text;
        }
        throw new Error(msg);
    }

    // Devolver null si la respuesta esta vacia (ej: 204 No Content)
    if (!text) return null;

    return JSON.parse(text);
}

// =============================================================================
// UTILIDADES DE UI
// =============================================================================

/**
 * Muestra u oculta una seccion por su ID.
 * Solo una seccion es visible a la vez (estilo SPA).
 *
 * @param {string} sectionId - ID del elemento a mostrar
 */
function showSection(sectionId) {
    // Ocultar todas las secciones principales
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));

    // Mostrar la seccion solicitada
    const target = document.getElementById(sectionId);
    if (target) target.classList.add('active');
}

/**
 * Muestra un mensaje de feedback (exito o error) dentro de un contenedor.
 *
 * @param {string} containerId - ID del div donde mostrar el mensaje
 * @param {string} message     - Texto a mostrar
 * @param {'success'|'error'} type - Tipo de mensaje (afecta el color)
 */
function showMessage(containerId, message, type = 'success') {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.textContent = message;
    container.className = `message ${type}`;
    container.style.display = 'block';

    // Auto-ocultar despues de 4 segundos
    setTimeout(() => {
        container.style.display = 'none';
    }, 4000);
}

/**
 * Muestra u oculta el overlay de carga global.
 *
 * @param {boolean} show - true para mostrar, false para ocultar
 */
function setLoading(show) {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.style.display = show ? 'flex' : 'none';
    }
}

// =============================================================================
// AUTENTICACION
// =============================================================================

/**
 * Registra un nuevo usuario.
 * Lee los campos del formulario #register-form y llama a POST /api/auth/register.
 */
async function register() {
    const username = document.getElementById('reg-username').value.trim();
    const email    = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;

    // Validacion basica en frontend
    if (!username || !email || !password) {
        showMessage('register-msg', 'Completa todos los campos.', 'error');
        return;
    }

    setLoading(true);
    try {
        await apiCall('/api/auth/register', 'POST', { username, email, password });
        showMessage('register-msg', 'Registro exitoso! Ya podes iniciar sesion.', 'success');

        // Pre-llenar el campo de login con el usuario recien registrado
        document.getElementById('login-identifier').value = username;
        setTimeout(() => showSection('login-section'), 1500);
    } catch (err) {
        showMessage('register-msg', err.message, 'error');
    } finally {
        setLoading(false);
    }
}

/**
 * Inicia sesion con usuario y password.
 * Guarda el JWT en authToken y actualiza la UI al estado autenticado.
 */
async function login() {
    const identifier = document.getElementById('login-identifier').value.trim();
    const password   = document.getElementById('login-password').value;

    if (!identifier || !password) {
        showMessage('login-msg', 'Ingresa usuario y password.', 'error');
        return;
    }

    setLoading(true);
    try {
        // El backend acepta 'identifier' (puede ser username o email)
        const data = await apiCall('/api/auth/login', 'POST', { identifier, password });

        // Guardar token y usuario en estado global
        authToken   = data.token;
        currentUser = identifier;

        // Actualizar header con nombre de usuario
        document.getElementById('user-display').textContent = identifier;

        // Mostrar navbar autenticada y pasar al dashboard
        document.getElementById('main-nav').style.display = 'flex';
        document.getElementById('auth-nav').style.display  = 'none';
        showSection('dashboard-section');
        loadDashboard();
    } catch (err) {
        showMessage('login-msg', err.message, 'error');
    } finally {
        setLoading(false);
    }
}

/**
 * Cierra la sesion del usuario.
 * Limpia el estado global y vuelve a la pantalla de login.
 */
function logout() {
    authToken   = null;
    currentUser = null;

    // Volver a mostrar navegacion de invitado
    document.getElementById('main-nav').style.display = 'none';
    document.getElementById('auth-nav').style.display  = 'flex';
    showSection('login-section');
}

// =============================================================================
// DASHBOARD
// =============================================================================

/**
 * Carga el dashboard: muestra estadisticas y los ultimos scrims disponibles.
 * Se llama automaticamente despues del login.
 */
async function loadDashboard() {
    setLoading(true);
    try {
        // Traer todos los scrims para calcular estadisticas
        const scrims = await apiCall('/api/scrims');

        // Calcular metricas para las tarjetas del dashboard
        const total  = scrims.length;
        const open   = scrims.filter(s => s.estado === 'ABIERTO'  || s.estado === 'OPEN').length;
        const active = scrims.filter(s => s.estado === 'EN_CURSO' || s.estado === 'ACTIVE').length;

        document.getElementById('stat-total').textContent  = total;
        document.getElementById('stat-open').textContent   = open;
        document.getElementById('stat-active').textContent = active;

        // Mostrar los 5 scrims mas recientes en el feed del dashboard
        const feed = document.getElementById('recent-scrims');
        if (scrims.length === 0) {
            feed.innerHTML = '<p class="empty-state">No hay scrims disponibles aun.</p>';
        } else {
            feed.innerHTML = scrims
                .slice(-5)           // Ultimos 5
                .reverse()           // Mas nuevo primero
                .map(renderScrimCard)
                .join('');
        }
    } catch (err) {
        console.error('Error cargando dashboard:', err);
    } finally {
        setLoading(false);
    }
}

// =============================================================================
// CREAR SCRIM
// =============================================================================

/**
 * Crea un nuevo scrim con los datos del formulario #create-scrim-form.
 * Llama a POST /api/scrims.
 */
async function createScrim() {
    // Leer todos los campos del formulario
    const juego       = document.getElementById('scrim-juego').value.trim();
    const region      = document.getElementById('scrim-region').value.trim();
    const rangoMin    = parseInt(document.getElementById('scrim-rango-min').value) || 0;
    const rangoMax    = parseInt(document.getElementById('scrim-rango-max').value) || 100;
    const latenciaMax = parseInt(document.getElementById('scrim-latencia').value)  || 150;
    const descripcion = document.getElementById('scrim-descripcion').value.trim();

    if (!juego || !region) {
        showMessage('create-msg', 'Juego y region son obligatorios.', 'error');
        return;
    }

    setLoading(true);
    try {
        await apiCall('/api/scrims', 'POST', {
            juego,
            region,
            rangoMin,
            rangoMax,
            latenciaMax,
            descripcion
        });

        showMessage('create-msg', 'Scrim creado con exito!', 'success');

        // Limpiar formulario
        ['scrim-juego','scrim-region','scrim-rango-min','scrim-rango-max',
         'scrim-latencia','scrim-descripcion'].forEach(id => {
            document.getElementById(id).value = '';
        });

        // Recargar dashboard para reflejar el nuevo scrim
        loadDashboard();
    } catch (err) {
        showMessage('create-msg', err.message, 'error');
    } finally {
        setLoading(false);
    }
}

// =============================================================================
// BUSCAR SCRIMS
// =============================================================================

/**
 * Busca scrims aplicando los filtros del formulario.
 * Llama a GET /api/scrims con query params opcionales.
 */
async function findScrims() {
    // Construir query string solo con los filtros que el usuario completo
    const params = new URLSearchParams();

    const region      = document.getElementById('find-region').value.trim();
    const rangoMin    = document.getElementById('find-rango-min').value.trim();
    const rangoMax    = document.getElementById('find-rango-max').value.trim();
    const latenciaMax = document.getElementById('find-latencia').value.trim();

    if (region)      params.append('region',      region);
    if (rangoMin)    params.append('rangoMin',     rangoMin);
    if (rangoMax)    params.append('rangoMax',     rangoMax);
    if (latenciaMax) params.append('latenciaMax',  latenciaMax);

    const query = params.toString() ? `?${params.toString()}` : '';

    setLoading(true);
    try {
        const scrims = await apiCall(`/api/scrims${query}`);
        renderScrimList(scrims, 'find-results');
    } catch (err) {
        showMessage('find-msg', err.message, 'error');
    } finally {
        setLoading(false);
    }
}

/**
 * Renderiza una lista de scrims dentro de un contenedor.
 *
 * @param {Array}  scrims      - Array de objetos scrim del backend
 * @param {string} containerId - ID del div donde renderizar
 */
function renderScrimList(scrims, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!scrims || scrims.length === 0) {
        container.innerHTML = '<p class="empty-state">No se encontraron scrims con esos filtros.</p>';
        return;
    }

    container.innerHTML = scrims.map(renderScrimCard).join('');
}

/**
 * Genera el HTML de una tarjeta de scrim.
 *
 * @param {object} scrim - Objeto scrim del backend
 * @returns {string} HTML de la tarjeta
 */
function renderScrimCard(scrim) {
    // Mapear estados del backend a clases CSS y etiquetas legibles
    const estadoMap = {
        'ABIERTO':   { label: 'Abierto',   css: 'open'     },
        'OPEN':      { label: 'Abierto',   css: 'open'     },
        'EN_CURSO':  { label: 'En Curso',  css: 'active'   },
        'ACTIVE':    { label: 'En Curso',  css: 'active'   },
        'CANCELADO': { label: 'Cancelado', css: 'cancelled'},
        'FINALIZADO':{ label: 'Finalizado',css: 'finished' },
    };

    const estado = estadoMap[scrim.estado] || { label: scrim.estado || 'Desconocido', css: 'open' };

    // Botones de accion segun el estado actual del scrim
    let actionButtons = '';
    const id = scrim.id;

    if (estado.css === 'open') {
        actionButtons = `
            <button class="btn btn-sm btn-success" onclick="postularse(${id})">Postularse</button>
            <button class="btn btn-sm btn-primary" onclick="iniciarScrim(${id})">Iniciar</button>
            <button class="btn btn-sm btn-danger"  onclick="cancelarScrim(${id})">Cancelar</button>
        `;
    } else if (estado.css === 'active') {
        actionButtons = `
            <button class="btn btn-sm btn-accent"  onclick="finalizarScrim(${id})">Finalizar</button>
            <button class="btn btn-sm btn-danger"  onclick="cancelarScrim(${id})">Cancelar</button>
        `;
    }

    return `
        <div class="scrim-card">
            <div class="scrim-header">
                <span class="scrim-game">${escapeHtml(scrim.juego || 'Sin juego')}</span>
                <span class="scrim-status ${estado.css}">${estado.label}</span>
            </div>
            <div class="scrim-details">
                <span>Region: ${escapeHtml(scrim.region || '-')}</span>
                <span>Rango: ${scrim.rangoMin ?? '-'} - ${scrim.rangoMax ?? '-'}</span>
                <span>Latencia max: ${scrim.latenciaMax ?? '-'} ms</span>
            </div>
            ${scrim.descripcion ? `<p class="scrim-desc">${escapeHtml(scrim.descripcion)}</p>` : ''}
            <div class="scrim-actions">${actionButtons}</div>
        </div>
    `;
}

/**
 * Escapa caracteres HTML para prevenir XSS.
 *
 * @param {string} str - Texto a escapar
 * @returns {string} Texto seguro para insertar en HTML
 */
function escapeHtml(str) {
    if (typeof str !== 'string') return String(str ?? '');
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// =============================================================================
// ACCIONES SOBRE SCRIMS
// =============================================================================

/**
 * Postula al usuario actual a un scrim.
 * Llama a POST /api/scrims/{id}/postulaciones.
 *
 * @param {number} id - ID del scrim
 */
async function postularse(id) {
    setLoading(true);
    try {
        await apiCall(`/api/scrims/${id}/postulaciones`, 'POST');
        showMessage('find-msg', 'Postulacion enviada!', 'success');
        findScrims(); // Refrescar lista
    } catch (err) {
        showMessage('find-msg', err.message, 'error');
    } finally {
        setLoading(false);
    }
}

/**
 * Inicia un scrim (cambia estado a EN_CURSO).
 * Llama a POST /api/scrims/{id}/iniciar.
 *
 * @param {number} id - ID del scrim
 */
async function iniciarScrim(id) {
    setLoading(true);
    try {
        await apiCall(`/api/scrims/${id}/iniciar`, 'POST');
        showMessage('find-msg', 'Scrim iniciado!', 'success');
        findScrims();
        loadDashboard();
    } catch (err) {
        showMessage('find-msg', err.message, 'error');
    } finally {
        setLoading(false);
    }
}

/**
 * Cancela un scrim.
 * Llama a POST /api/scrims/{id}/cancelar.
 *
 * @param {number} id - ID del scrim
 */
async function cancelarScrim(id) {
    if (!confirm('Cancelar este scrim?')) return;
    setLoading(true);
    try {
        await apiCall(`/api/scrims/${id}/cancelar`, 'POST');
        showMessage('find-msg', 'Scrim cancelado.', 'success');
        findScrims();
        loadDashboard();
    } catch (err) {
        showMessage('find-msg', err.message, 'error');
    } finally {
        setLoading(false);
    }
}

/**
 * Finaliza un scrim (cambia estado a FINALIZADO).
 * Llama a POST /api/scrims/{id}/finalizar.
 *
 * @param {number} id - ID del scrim
 */
async function finalizarScrim(id) {
    setLoading(true);
    try {
        await apiCall(`/api/scrims/${id}/finalizar`, 'POST');
        showMessage('find-msg', 'Scrim finalizado!', 'success');
        findScrims();
        loadDashboard();
    } catch (err) {
        showMessage('find-msg', err.message, 'error');
    } finally {
        setLoading(false);
    }
}

// =============================================================================
// INICIALIZACION
// =============================================================================

/**
 * Se ejecuta cuando el DOM esta listo.
 * Muestra la pantalla de login por defecto y conecta los handlers del formulario.
 */
document.addEventListener('DOMContentLoaded', () => {
    // Pantalla inicial: login
    showSection('login-section');

    // Permitir submit con Enter en los formularios de auth
    ['login-identifier', 'login-password'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener('keydown', e => { if (e.key === 'Enter') login(); });
    });

    ['reg-username', 'reg-email', 'reg-password'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener('keydown', e => { if (e.key === 'Enter') register(); });
    });
});

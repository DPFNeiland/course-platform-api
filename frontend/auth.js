document.addEventListener('DOMContentLoaded', function() {
    const API_BASE_URL = 'http://localhost:8080';

    const loginSection = document.getElementById('login-section');
    const registerSection = document.getElementById('register-section');
    const showRegisterBtn = document.getElementById('show-register');
    const showLoginBtn = document.getElementById('show-login');
    const errorDiv = document.getElementById('error');
    const perfilSelect = document.getElementById('reg-perfil');
    let profileFields = document.getElementById('profile-fields');

    function showError(msg) {
        errorDiv.textContent = msg;
        errorDiv.style.display = 'block';
        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 5000);
    }

    function extrairMensagemErro(erroData, status) {
        if (erroData) {
            if (erroData.erros && typeof erroData.erros === 'object') {
                const mensagens = Object.values(erroData.erros);
                if (mensagens.length > 0) return mensagens.join('; ');
            }
            if (erroData.erro) return erroData.erro;
            if (erroData.message) return erroData.message;
            if (erroData.error) return erroData.error;
        }
        return status ? `Erro no servidor (HTTP ${status})` : 'Erro ao cadastrar. Verifique os dados e tente novamente.';
    }

    function formatDateForApi(dateValue) {
        const [year, month, day] = dateValue.split('-');
        return `${Number(day)}/${Number(month)}/${year}`;
    }

    function redirectByPerfil(perfil) {
        const perfilNormalizado = normalizarPerfil(perfil);
        const routes = {
            aluno: 'aluno/dashboard.html',
            professor: 'professor/dashboard.html',
            curador: 'curador/dashboard.html',
            admin: 'curador/dashboard.html'
        };
        window.location.href = routes[perfilNormalizado] || 'index.html';
    }

    function normalizarPerfil(perfil) {
        return String(perfil || '')
            .trim()
            .toLowerCase();
    }

    function montarSessao(usuario, perfilFallback) {
        const perfil = normalizarPerfil(usuario?.perfil || usuario?.cargo || perfilFallback);
        const sessao = {
            id: usuario?.id,
            nome: usuario?.nome,
            email: usuario?.email,
            cargo: usuario?.cargo,
            perfil,
            expiraEm: usuario?.expiraEm
        };
        if (!window.jwtSession.isValid(sessao)) {
            throw new Error('O servidor retornou uma sessao invalida. Faca login novamente.');
        }
        return sessao;
    }

    const authReason = new URLSearchParams(window.location.search).get('auth');
    if (authReason === 'expired') showError('Sua sessao expirou. Faca login novamente.');
    if (authReason === 'invalid') showError('Sua sessao e invalida. Faca login novamente.');
    if (authReason === 'logout_failed') showError('Nao foi possivel encerrar a sessao no servidor. Tente novamente.');
    if (authReason) window.history.replaceState({}, document.title, window.location.pathname);

    async function autenticar(email, senha, perfilFallback) {
        const resposta = await window.jwtSession.csrfFetch(API_BASE_URL, '/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, senha })
        });
        if (!resposta.ok) {
            const erroData = await resposta.json().catch(() => null);
            throw new Error(extrairMensagemErro(erroData, resposta.status));
        }
        const usuario = await resposta.json();
        const sessao = montarSessao(usuario, perfilFallback);
        sessionStorage.removeItem('user');
        sessionStorage.setItem('session', JSON.stringify(sessao));
        return sessao;
    }

    function getSelectedPerfil() {
        return normalizarPerfil(perfilSelect?.value || 'aluno');
    }

    function ensureProfileFieldsContainer() {
        if (profileFields) return profileFields;

        const firstLegacyField = document.querySelector('.profile-field');
        profileFields = document.createElement('div');
        profileFields.id = 'profile-fields';
        profileFields.setAttribute('aria-live', 'polite');

        if (firstLegacyField) {
            firstLegacyField.parentNode.insertBefore(profileFields, firstLegacyField);
        } else {
            document.getElementById('reg-senha')?.closest('.input-group')?.before(profileFields);
        }

        document.querySelectorAll('.profile-field').forEach(field => field.remove());
        return profileFields;
    }

    function renderProfileFields() {
        const container = ensureProfileFieldsContainer();
        const perfil = getSelectedPerfil();
        const templates = {
            aluno: `
                <div class="input-group">
                    <label for="reg-ra">RA (Registro Academico)</label>
                    <input type="text" id="reg-ra" name="ra" placeholder="Ex: AB1234" pattern="[A-Za-z]{2}\\d{4}" title="Formato exigido: 2 letras e 4 numeros" required>
                </div>
            `,
            professor: `
                <div class="input-group">
                    <label for="reg-areaFormacao">Area de formacao</label>
                    <input type="text" id="reg-areaFormacao" name="areaFormacao" placeholder="Ex: Tecnologia" required>
                </div>
                <div class="input-group">
                    <label for="reg-horaAula">Valor hora/aula</label>
                    <input type="number" id="reg-horaAula" name="horaAula" min="0" step="0.01" placeholder="Ex: 80" required>
                </div>
                <div class="input-group">
                    <label for="reg-tipoEnsino">Tipo de ensino</label>
                    <select id="reg-tipoEnsino" name="tipoEnsino" required>
                        <option value="ASSINCRONO">Assincrono</option>
                        <option value="SINCRONO">Sincrono</option>
                        <option value="AMBOS">Ambos</option>
                    </select>
                </div>
            `,
            curador: `
                <p class="profile-helper">Curador nao precisa de campos extras. O cadastro sera salvo como conta de curadoria.</p>
            `
        };

        container.innerHTML = templates[perfil] || templates.aluno;
    }

    function updateRegisterFields() {
        renderProfileFields();
    }

    showRegisterBtn.addEventListener('click', (e) => {
        e.preventDefault();
        loginSection.classList.add('hidden');
        registerSection.classList.remove('hidden');
    });

    showLoginBtn.addEventListener('click', (e) => {
        e.preventDefault();
        registerSection.classList.add('hidden');
        loginSection.classList.remove('hidden');
    });

    const registerForm = document.getElementById('registerForm');
    perfilSelect?.addEventListener('change', updateRegisterFields);
    perfilSelect?.addEventListener('input', updateRegisterFields);
    updateRegisterFields();

    registerForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const perfil = getSelectedPerfil();
        const usuarioDTO = {
            nome: document.getElementById('reg-nome').value,
            email: document.getElementById('reg-email').value,
            cpf: document.getElementById('reg-cpf').value,
            genero: document.getElementById('reg-genero').value,
            dataNascimento: formatDateForApi(document.getElementById('reg-dataNascimento').value),
            senha: document.getElementById('reg-senha').value
        };

        let endpoint = '/usuario';
        let payload = usuarioDTO;

        if (perfil === 'aluno') {
            endpoint = '/aluno';
            payload = {
                ...usuarioDTO,
                ra: document.getElementById('reg-ra')?.value,
                status: 'A_CURSAR'
            };
        }

        if (perfil === 'professor') {
            endpoint = '/professor';
            payload = {
                ...usuarioDTO,
                areaFormacao: document.getElementById('reg-areaFormacao')?.value,
                horaAula: Number(document.getElementById('reg-horaAula')?.value),
                tipoEnsino: document.getElementById('reg-tipoEnsino')?.value
            };
        }

        if (perfil === 'curador') {
            endpoint = '/curador';
        }

        window.jwtSession.csrfFetch(API_BASE_URL, endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        })
        .then(async (resposta) => {
            if (resposta.status === 201) {
                const sessao = await autenticar(usuarioDTO.email, usuarioDTO.senha, perfil);
                redirectByPerfil(sessao.perfil);
            } else {
                const erroData = await resposta.json().catch(() => null);
                showError(extrairMensagemErro(erroData, resposta.status));
            }
        })
        .catch(erro => {
            console.error('Erro no cadastro:', erro);
            showError(erro?.message || 'Servidor indisponivel. O Backend esta rodando?');
        });
    });

    const loginForm = document.getElementById('loginForm');

    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const email = document.getElementById('email').value;
        const senha = document.getElementById('senha').value;

        autenticar(email, senha)
        .then((sessao) => {
            redirectByPerfil(sessao.perfil);
        })
        .catch(erro => {
            console.error('Erro no login:', erro);
            showError(erro?.message || 'Servidor indisponivel. O Backend esta rodando?');
        });
    });
});

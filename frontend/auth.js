document.addEventListener('DOMContentLoaded', function() {
    const API_BASE_URL = 'http://localhost:8080';

    const loginSection = document.getElementById('login-section');
    const registerSection = document.getElementById('register-section');
    const showRegisterBtn = document.getElementById('show-register');
    const showLoginBtn = document.getElementById('show-login');
    const errorDiv = document.getElementById('error');

    function showError(msg) {
        errorDiv.textContent = msg;
        errorDiv.style.display = 'block';
        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 5000);
    }

    function formatDateForApi(dateValue) {
        const [year, month, day] = dateValue.split('-');
        return `${Number(day)}/${Number(month)}/${year}`;
    }

    function redirectByPerfil(perfil) {
        const routes = {
            aluno: '/aluno/dashboard.html',
            professor: '/professor/dashboard.html',
            curador: '/curador/dashboard.html',
            admin: '/curador/dashboard.html'
        };
        window.location.href = routes[perfil] || '/index.html';
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

    registerForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const alunoDTO = {
            nome: document.getElementById('reg-nome').value,
            email: document.getElementById('reg-email').value,
            cpf: document.getElementById('reg-cpf').value,
            genero: document.getElementById('reg-genero').value,
            dataNascimento: formatDateForApi(document.getElementById('reg-dataNascimento').value),
            ra: document.getElementById('reg-ra').value,
            senha: document.getElementById('reg-senha').value,
            status: 'A_CURSAR'
        };

        fetch(`${API_BASE_URL}/aluno`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(alunoDTO)
        })
        .then(async (resposta) => {
            if (resposta.status === 201) {
                alert('Conta criada com sucesso! Voce ja pode fazer login.');
                registerForm.reset();
                registerSection.classList.add('hidden');
                loginSection.classList.remove('hidden');
            } else {
                const erroData = await resposta.json().catch(() => null);
                const mensagem = erroData?.message || erroData?.error || 'Erro ao cadastrar. Verifique os dados e tente novamente.';
                showError(mensagem);
            }
        })
        .catch(erro => {
            console.error('Erro no cadastro:', erro);
            showError('Servidor indisponivel. O Backend esta rodando?');
        });
    });

    const loginForm = document.getElementById('loginForm');

    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const email = document.getElementById('email').value;
        const senha = document.getElementById('senha').value;

        fetch(`${API_BASE_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, senha })
        })
        .then(async (resposta) => {
            if (!resposta.ok) {
                const erroData = await resposta.json().catch(() => null);
                const mensagem = erroData?.message || erroData?.error || 'E-mail ou senha invalidos.';
                showError(mensagem);
                return;
            }

            const usuario = await resposta.json();
            sessionStorage.setItem('user', JSON.stringify(usuario));
            sessionStorage.setItem('session', JSON.stringify(usuario));
            redirectByPerfil(usuario.perfil);
        })
        .catch(erro => {
            console.error('Erro no login:', erro);
            showError('Servidor indisponivel. O Backend esta rodando?');
        });
    });
});

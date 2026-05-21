document.addEventListener('DOMContentLoaded', function() {

    // Route protection
    const userStr = sessionStorage.getItem('user') || sessionStorage.getItem('session');
    let user;

    try {
        user = userStr ? JSON.parse(userStr) : null;
    } catch (err) {
        user = null;
    }

    if (!user || !['curador', 'admin'].includes(user.perfil)) {
        window.location.href = '/index.html';
        return;
    }

    // Logout function
    function logout() {
        sessionStorage.clear();
        window.location.href = '/index.html';
    }

    // Event listeners
    const logoutBtn = document.querySelector('#logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }

    // Modal handling
    document.addEventListener('click', function(e) {
        if (e.target.matches('.open-modal')) {
            e.preventDefault();
            const modal = document.querySelector(e.target.dataset.target || '.modal');
            if (modal) modal.classList.add('open');
        }
        if (e.target.matches('.close-modal') || e.target.closest('.close-modal')) {
            e.preventDefault();
            const modal = e.target.closest('.modal') || document.querySelector('.modal.open');
            if (modal) modal.classList.remove('open');
        }
        // Close modal on overlay click
        if (e.target.classList.contains('modal')) {
            e.target.classList.remove('open');
        }
    });

    // User management filters (placeholder)
    const searchUser = document.querySelector('#searchUser');
    const filterProfile = document.querySelector('#filterProfile');
    if (searchUser) {
        searchUser.addEventListener('input', function() {
            console.log('Placeholder: Filtering users by search:', this.value);
        });
    }
    if (filterProfile) {
        filterProfile.addEventListener('change', function() {
            console.log('Placeholder: Filtering users by profile:', this.value);
        });
    }

    // Administrative actions (placeholders)
    document.addEventListener('click', function(e) {
        if (e.target.matches('.edit-access')) {
            e.preventDefault();
            console.log('Placeholder: Edit user accesses');
        }
        if (e.target.matches('.view-logs')) {
            e.preventDefault();
            console.log('Placeholder: View user logs');
        }
        if (e.target.matches('.suspend-user')) {
            e.preventDefault();
            console.log('Placeholder: Suspend user');
        }
        if (e.target.matches('.reactivate-user')) {
            e.preventDefault();
            console.log('Placeholder: Reactivate user');
        }
    });

    // Course Approval interactions (placeholders)
    document.addEventListener('click', function(e) {
        if (e.target.matches('.review-course')) {
            e.preventDefault();
            console.log('Placeholder: Review course');
        }
        if (e.target.matches('.approve-course')) {
            e.preventDefault();
            console.log('Placeholder: Approve course');
        }
        if (e.target.matches('.adjust-course')) {
            e.preventDefault();
            console.log('Placeholder: Request adjustments');
        }
    });

    // Gamification interactions (placeholders)
    document.querySelector('#saveRules')?.addEventListener('click', function(e) {
        e.preventDefault();
        console.log('Placeholder: Save gamification rules');
    });
    document.addEventListener('click', function(e) {
        if (e.target.matches('.add-prize')) {
            e.preventDefault();
            console.log('Placeholder: Add prize');
        }
        if (e.target.matches('.edit-prize')) {
            e.preventDefault();
            console.log('Placeholder: Edit prize');
        }
        if (e.target.matches('.delete-prize')) {
            e.preventDefault();
            console.log('Placeholder: Delete prize');
        }
    });

    // Monitoring interactions (placeholder)
    document.addEventListener('click', function(e) {
        if (e.target.matches('.analyze-case')) {
            e.preventDefault();
            console.log('Placeholder: Analyze case');
        }
    });

});

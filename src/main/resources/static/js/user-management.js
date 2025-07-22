/**
 * User Management JavaScript
 * Handles search, filtering, and form interactions for user management page
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeUserManagement();
});

function initializeUserManagement() {
    initializeSearch();
    initializeDeleteConfirmation();
    initializeRoleChangeConfirmation();
    initializeTableAnimations();
}

/**
 * Initialize search and filter functionality
 */
function initializeSearch() {
    const searchInput = document.getElementById('searchInput');
    const roleFilter = document.getElementById('roleFilter');
    const clearSearch = document.getElementById('clearSearch');
    const visibleCount = document.getElementById('visibleCount');
    const userRows = document.querySelectorAll('.user-row');
    
    if (!searchInput || !roleFilter) return;
    
    function filterUsers() {
        const searchTerm = searchInput.value.toLowerCase().trim();
        const selectedRole = roleFilter.value;
        let visibleUsers = 0;
        
        userRows.forEach(row => {
            const username = row.querySelector('.username')?.textContent.toLowerCase() || '';
            const email = row.querySelector('td:nth-child(3)')?.textContent.toLowerCase() || '';
            const role = row.dataset.role || '';
            
            const matchesSearch = !searchTerm || username.includes(searchTerm) || email.includes(searchTerm);
            const matchesRole = !selectedRole || role === selectedRole;
            
            const isVisible = matchesSearch && matchesRole;
            row.style.display = isVisible ? '' : 'none';
            
            if (isVisible) {
                visibleUsers++;
                // Add fade-in animation
                row.style.animation = 'fadeIn 0.3s ease-in';
            }
        });
        
        // Update visible count
        if (visibleCount) {
            visibleCount.textContent = visibleUsers;
        }
        
        // Show/hide clear button
        if (clearSearch) {
            clearSearch.style.display = searchTerm ? 'flex' : 'none';
        }
        
        // Update empty state
        updateEmptyState(visibleUsers);
    }
    
    // Debounced search for better performance
    let searchTimeout;
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(filterUsers, 200);
    });
    
    roleFilter.addEventListener('change', filterUsers);
    
    // Clear search functionality
    if (clearSearch) {
        clearSearch.addEventListener('click', function() {
            searchInput.value = '';
            searchInput.focus();
            filterUsers();
        });
    }
    
    // Add search shortcuts
    document.addEventListener('keydown', function(e) {
        if (e.ctrlKey && e.key === 'f') {
            e.preventDefault();
            searchInput.focus();
            searchInput.select();
        }
        
        if (e.key === 'Escape' && document.activeElement === searchInput) {
            searchInput.blur();
            if (searchInput.value) {
                searchInput.value = '';
                filterUsers();
            }
        }
    });
    
    // Initialize count
    filterUsers();
}

/**
 * Update empty state when no users match filters
 */
function updateEmptyState(visibleCount) {
    const tableBody = document.querySelector('.users-table tbody');
    if (!tableBody) return;
    
    let emptyRow = document.getElementById('empty-state-row');
    
    if (visibleCount === 0) {
        if (!emptyRow) {
            emptyRow = document.createElement('tr');
            emptyRow.id = 'empty-state-row';
            emptyRow.innerHTML = `
                <td colspan="5" style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                    <i class="fas fa-search" style="font-size: 2rem; margin-bottom: 1rem; opacity: 0.5;"></i>
                    <p style="margin: 0; font-size: 1.1rem;">Nessun utente trovato</p>
                    <p style="margin: 0.5rem 0 0 0; font-size: 0.9rem;">Prova a modificare i criteri di ricerca</p>
                </td>
            `;
            tableBody.appendChild(emptyRow);
        }
    } else if (emptyRow) {
        emptyRow.remove();
    }
}

/**
 * Initialize delete confirmation functionality
 */
function initializeDeleteConfirmation() {
    document.querySelectorAll('.delete-form').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const username = this.dataset.username;
            
            showConfirmationModal({
                title: 'Conferma Eliminazione',
                message: `Sei sicuro di voler eliminare l'utente "<strong>${username}</strong>"?`,
                warning: 'Questa azione non può essere annullata e tutti i dati associati verranno rimossi permanentemente.',
                confirmText: 'Elimina Utente',
                cancelText: 'Annulla',
                confirmClass: 'btn-danger',
                onConfirm: () => {
                    // Add loading state
                    const submitBtn = this.querySelector('button[type="submit"]');
                    const originalContent = submitBtn.innerHTML;
                    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
                    submitBtn.disabled = true;
                    
                    this.submit();
                }
            });
        });
    });
}

/**
 * Initialize role change confirmation functionality
 */
function initializeRoleChangeConfirmation() {
    document.querySelectorAll('.role-form').forEach(form => {
        const select = form.querySelector('.role-select');
        const originalRole = select.dataset.current;
        
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const newRole = select.value;
            
            if (newRole === originalRole) {
                showNotification('Il ruolo selezionato è già quello attuale dell\'utente.', 'warning');
                return;
            }
            
            const username = this.closest('tr').querySelector('.username').textContent;
            const roleNames = {
                'USER': 'Utente',
                'ADMIN': 'Amministratore',
                'SUPER_ADMIN': 'Super-Admin'
            };
            
            showConfirmationModal({
                title: 'Conferma Cambio Ruolo',
                message: `Stai per cambiare il ruolo di "<strong>${username}</strong>"`,
                info: `Da: <span class="role-badge ${originalRole.toLowerCase()}">${roleNames[originalRole] || originalRole}</span><br>
                       A: <span class="role-badge ${newRole.toLowerCase()}">${roleNames[newRole] || newRole}</span>`,
                confirmText: 'Conferma Cambio',
                cancelText: 'Annulla',
                confirmClass: 'btn-primary',
                onConfirm: () => {
                    // Add loading state
                    const submitBtn = this.querySelector('button[type="submit"]');
                    const originalContent = submitBtn.innerHTML;
                    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
                    submitBtn.disabled = true;
                    
                    this.submit();
                }
            });
        });
    });
}

/**
 * Initialize table animations
 */
function initializeTableAnimations() {
    // Animate rows on page load
    const rows = document.querySelectorAll('.user-row');
    rows.forEach((row, index) => {
        row.style.opacity = '0';
        row.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            row.style.transition = 'all 0.3s ease';
            row.style.opacity = '1';
            row.style.transform = 'translateY(0)';
        }, index * 50);
    });
    
    // Add hover effects
    rows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.01)';
            this.style.zIndex = '1';
        });
        
        row.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
            this.style.zIndex = 'auto';
        });
    });
}

/**
 * Show confirmation modal
 */
function showConfirmationModal({ title, message, warning, info, confirmText, cancelText, confirmClass, onConfirm }) {
    // Create modal HTML
    const modalHtml = `
        <div class="confirmation-modal-overlay" id="confirmationModal">
            <div class="confirmation-modal">
                <div class="modal-header">
                    <h3>${title}</h3>
                    <button class="modal-close" type="button">&times;</button>
                </div>
                <div class="modal-body">
                    <p>${message}</p>
                    ${warning ? `<div class="modal-warning"><i class="fas fa-exclamation-triangle"></i> ${warning}</div>` : ''}
                    ${info ? `<div class="modal-info">${info}</div>` : ''}
                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary modal-cancel" type="button">${cancelText}</button>
                    <button class="btn ${confirmClass} modal-confirm" type="button">${confirmText}</button>
                </div>
            </div>
        </div>
    `;
    
    // Add modal to page
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    
    const modal = document.getElementById('confirmationModal');
    const confirmBtn = modal.querySelector('.modal-confirm');
    const cancelBtn = modal.querySelector('.modal-cancel');
    const closeBtn = modal.querySelector('.modal-close');
    
    // Show modal with animation
    setTimeout(() => modal.classList.add('show'), 10);
    
    function closeModal() {
        modal.classList.remove('show');
        setTimeout(() => modal.remove(), 300);
    }
    
    // Event listeners
    confirmBtn.addEventListener('click', () => {
        closeModal();
        onConfirm();
    });
    
    [cancelBtn, closeBtn].forEach(btn => {
        btn.addEventListener('click', closeModal);
    });
    
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });
    
    // ESC key to close
    const escHandler = (e) => {
        if (e.key === 'Escape') {
            closeModal();
            document.removeEventListener('keydown', escHandler);
        }
    };
    document.addEventListener('keydown', escHandler);
    
    // Focus confirm button
    confirmBtn.focus();
}

/**
 * Show notification
 */
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check' : type === 'warning' ? 'exclamation-triangle' : 'info-circle'}"></i>
        <span>${message}</span>
        <button class="notification-close">&times;</button>
    `;
    
    document.body.appendChild(notification);
    
    // Show notification
    setTimeout(() => notification.classList.add('show'), 10);
    
    // Auto hide after 5 seconds
    const autoHide = setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 5000);
    
    // Manual close
    notification.querySelector('.notification-close').addEventListener('click', () => {
        clearTimeout(autoHide);
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    });
}

// Add CSS for modal and notifications
const additionalStyles = `
<style>
.confirmation-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10000;
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s ease;
}

.confirmation-modal-overlay.show {
    opacity: 1;
    visibility: visible;
}

.confirmation-modal {
    background: var(--card-bg);
    border-radius: 16px;
    box-shadow: var(--shadow-large);
    max-width: 500px;
    width: 90%;
    max-height: 90vh;
    overflow-y: auto;
    transform: scale(0.9) translateY(-20px);
    transition: all 0.3s ease;
}

.confirmation-modal-overlay.show .confirmation-modal {
    transform: scale(1) translateY(0);
}

.modal-header {
    padding: 1.5rem;
    border-bottom: 1px solid var(--border-color);
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.modal-header h3 {
    margin: 0;
    color: var(--text-primary);
}

.modal-close {
    background: none;
    border: none;
    font-size: 1.5rem;
    color: var(--text-secondary);
    cursor: pointer;
    padding: 0;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: all 0.2s ease;
}

.modal-close:hover {
    background: var(--hover-bg);
    color: var(--text-primary);
}

.modal-body {
    padding: 1.5rem;
    color: var(--text-primary);
}

.modal-warning {
    background: rgba(255, 193, 7, 0.1);
    border: 1px solid rgba(255, 193, 7, 0.3);
    border-radius: 8px;
    padding: 1rem;
    margin: 1rem 0;
    color: #856404;
    display: flex;
    align-items: flex-start;
    gap: 0.5rem;
}

.modal-info {
    background: var(--background-secondary);
    border-radius: 8px;
    padding: 1rem;
    margin: 1rem 0;
}

.modal-footer {
    padding: 1.5rem;
    border-top: 1px solid var(--border-color);
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
}

.notification {
    position: fixed;
    top: 20px;
    right: 20px;
    background: var(--card-bg);
    border-radius: 12px;
    box-shadow: var(--shadow-large);
    border: 1px solid var(--border-color);
    padding: 1rem 1.5rem;
    display: flex;
    align-items: center;
    gap: 0.75rem;
    z-index: 10001;
    transform: translateX(100%);
    opacity: 0;
    transition: all 0.3s ease;
    min-width: 300px;
    max-width: 500px;
}

.notification.show {
    transform: translateX(0);
    opacity: 1;
}

.notification-success {
    border-left: 4px solid #28a745;
}

.notification-warning {
    border-left: 4px solid #ffc107;
}

.notification-info {
    border-left: 4px solid #007bff;
}

.notification-close {
    background: none;
    border: none;
    color: var(--text-secondary);
    cursor: pointer;
    font-size: 1.2rem;
    margin-left: auto;
    padding: 0;
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.notification-close:hover {
    color: var(--text-primary);
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 576px) {
    .confirmation-modal {
        width: 95%;
        margin: 1rem;
    }
    
    .modal-footer {
        flex-direction: column-reverse;
    }
    
    .notification {
        top: 10px;
        right: 10px;
        left: 10px;
        min-width: unset;
        transform: translateY(-100%);
    }
    
    .notification.show {
        transform: translateY(0);
    }
}
</style>
`;

// Add styles to document
document.head.insertAdjacentHTML('beforeend', additionalStyles);

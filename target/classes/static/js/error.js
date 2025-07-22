/**
 * Error Page JavaScript
 * Gestisce le funzionalità interattive della pagina di errore
 */

class ErrorPageManager {
    constructor() {
        this.init();
    }

    init() {
        this.setupErrorAnimation();
        this.setupTechnicalDetails();
        this.setupCopyToClipboard();
        this.setupKeyboardShortcuts();
    }

    /**
     * Animazione di ingresso per il container errore
     */
    setupErrorAnimation() {
        const errorContainer = document.querySelector('.error-container');
        if (errorContainer) {
            // Aggiungi un leggero ritardo per l'animazione
            setTimeout(() => {
                errorContainer.style.opacity = '1';
                errorContainer.style.transform = 'translateY(0)';
            }, 100);
        }
    }

    /**
     * Gestione dei dettagli tecnici
     */
    setupTechnicalDetails() {
        const detailsElement = document.querySelector('.technical-details details');
        if (detailsElement) {
            const summary = detailsElement.querySelector('summary');
            
            // Aggiungi icona per indicare lo stato
            this.updateDetailsIcon(detailsElement, detailsElement.open);
            
            // Listener per il toggle
            detailsElement.addEventListener('toggle', (e) => {
                this.updateDetailsIcon(e.target, e.target.open);
                
                // Scroll smooth verso i dettagli quando vengono aperti
                if (e.target.open) {
                    setTimeout(() => {
                        e.target.scrollIntoView({ 
                            behavior: 'smooth', 
                            block: 'nearest' 
                        });
                    }, 100);
                }
            });
        }
    }

    /**
     * Aggiorna l'icona dei dettagli tecnici
     */
    updateDetailsIcon(detailsElement, isOpen) {
        const summary = detailsElement.querySelector('summary');
        if (summary) {
            const icon = isOpen ? '🔽' : '▶️';
            const text = summary.textContent.replace(/^[🔽▶️]\s*/, '');
            summary.textContent = `${icon} ${text}`;
        }
    }

    /**
     * Funzionalità copia negli appunti per i dettagli tecnici
     */
    setupCopyToClipboard() {
        const traceElement = document.querySelector('.technical-trace');
        if (traceElement) {
            // Crea button per copiare
            const copyButton = document.createElement('button');
            copyButton.textContent = '📋 Copia Stack Trace';
            copyButton.className = 'copy-trace-btn';
            copyButton.style.cssText = `
                margin-top: 10px;
                padding: 8px 16px;
                background: #007bff;
                color: white;
                border: none;
                border-radius: 4px;
                cursor: pointer;
                font-size: 12px;
                transition: background 0.2s;
            `;
            
            copyButton.addEventListener('click', () => {
                this.copyToClipboard(traceElement.textContent, copyButton);
            });
            
            copyButton.addEventListener('mouseenter', () => {
                copyButton.style.background = '#0056b3';
            });
            
            copyButton.addEventListener('mouseleave', () => {
                copyButton.style.background = '#007bff';
            });
            
            traceElement.parentNode.appendChild(copyButton);
        }
    }

    /**
     * Copia testo negli appunti
     */
    async copyToClipboard(text, button) {
        try {
            await navigator.clipboard.writeText(text);
            const originalText = button.textContent;
            button.textContent = '✅ Copiato!';
            button.style.background = '#28a745';
            
            setTimeout(() => {
                button.textContent = originalText;
                button.style.background = '#007bff';
            }, 2000);
        } catch (err) {
            console.error('Errore nel copiare il testo: ', err);
            button.textContent = '❌ Errore';
            button.style.background = '#dc3545';
            
            setTimeout(() => {
                button.textContent = '📋 Copia Stack Trace';
                button.style.background = '#007bff';
            }, 2000);
        }
    }

    /**
     * Scorciatoie da tastiera
     */
    setupKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            // Ctrl/Cmd + D per toggle dettagli tecnici
            if ((e.ctrlKey || e.metaKey) && e.key === 'd') {
                e.preventDefault();
                const detailsElement = document.querySelector('.technical-details details');
                if (detailsElement) {
                    detailsElement.open = !detailsElement.open;
                    detailsElement.dispatchEvent(new Event('toggle'));
                }
            }
            
            // Escape per tornare alla home
            if (e.key === 'Escape') {
                const homeButton = document.querySelector('a[href="/"]');
                if (homeButton) {
                    homeButton.click();
                }
            }
        });
    }

    /**
     * Mostra un toast di notifica
     */
    showToast(message, type = 'info') {
        // Rimuovi toast esistenti
        const existingToast = document.querySelector('.error-toast');
        if (existingToast) {
            existingToast.remove();
        }

        const toast = document.createElement('div');
        toast.className = 'error-toast';
        toast.textContent = message;
        
        const colors = {
            info: '#007bff',
            success: '#28a745',
            error: '#dc3545',
            warning: '#ffc107'
        };
        
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${colors[type] || colors.info};
            color: white;
            padding: 12px 20px;
            border-radius: 6px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            z-index: 1000;
            font-weight: 500;
            animation: slideInRight 0.3s ease-out;
        `;
        
        document.body.appendChild(toast);
        
        // Rimuovi dopo 3 secondi
        setTimeout(() => {
            toast.style.animation = 'slideOutRight 0.3s ease-in forwards';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }
}

// Aggiungi animazioni CSS per i toast
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    .copy-trace-btn:focus {
        outline: 2px solid #ffffff;
        outline-offset: 2px;
    }
`;
document.head.appendChild(style);

// Inizializza quando il DOM è pronto
document.addEventListener('DOMContentLoaded', () => {
    new ErrorPageManager();
});

// Esporta per utilizzo globale se necessario
window.ErrorPageManager = ErrorPageManager;

/* =======================================================================
   BIBLIOTECA SITARELLO - BOOK EDIT IMAGES JavaScript
   Gestione upload e eliminazione immagini con design elegante
   ======================================================================= */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize all functionality
    initializeImageUpload();
    initializeImageDelete();
    initializeBackToTop();
    initializeAlerts();
});

/* =======================================================================
   IMAGE UPLOAD FUNCTIONALITY
   ======================================================================= */

function initializeImageUpload() {
    const fileInput = document.getElementById('imageFiles');
    const uploadBtn = document.getElementById('uploadBtn');
    const previewArea = document.getElementById('previewArea');
    const previewContainer = document.getElementById('previewContainer');
    const uploadForm = document.getElementById('uploadForm');
    
    if (!fileInput || !uploadBtn) {
        console.log('Upload elements not found');
        return;
    }
    
    // Handle file input change
    fileInput.addEventListener('change', function(e) {
        const files = e.target.files;
        
        if (files.length > 0) {
            uploadBtn.disabled = false;
            showPreview(files);
        } else {
            uploadBtn.disabled = true;
            hidePreview();
        }
    });
    
    // Handle form submission
    uploadForm.addEventListener('submit', function(e) {
        const files = fileInput.files;
        
        if (!files || files.length === 0) {
            e.preventDefault();
            showAlert('Seleziona almeno un file da caricare.', 'danger');
            return;
        }
        
        // Validate files
        const maxSize = 5 * 1024 * 1024; // 5MB
        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
        
        for (let file of files) {
            if (file.size > maxSize) {
                e.preventDefault();
                showAlert(`File "${file.name}" troppo grande. Massimo 5MB consentiti.`, 'danger');
                return;
            }
            
            if (!allowedTypes.includes(file.type)) {
                e.preventDefault();
                showAlert(`Tipo file "${file.name}" non supportato. Usa JPG, PNG o GIF.`, 'danger');
                return;
            }
        }
        
        // Show loading state
        uploadBtn.disabled = true;
        uploadBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Caricamento...';
    });
}

function showPreview(files) {
    const previewArea = document.getElementById('previewArea');
    const previewContainer = document.getElementById('previewContainer');
    
    if (!previewArea || !previewContainer) return;
    
    previewContainer.innerHTML = '';
    
    Array.from(files).forEach((file, index) => {
        if (file.type.startsWith('image/')) {
            const reader = new FileReader();
            
            reader.onload = function(e) {
                const previewItem = document.createElement('div');
                previewItem.className = 'preview-item';
                previewItem.innerHTML = `
                    <img src="${e.target.result}" alt="Preview ${index + 1}" class="preview-image">
                    <div class="preview-info">${file.name}</div>
                `;
                
                previewContainer.appendChild(previewItem);
            };
            
            reader.readAsDataURL(file);
        }
    });
    
    previewArea.style.display = 'block';
}

function hidePreview() {
    const previewArea = document.getElementById('previewArea');
    if (previewArea) {
        previewArea.style.display = 'none';
    }
}

/* =======================================================================
   IMAGE DELETE FUNCTIONALITY
   ======================================================================= */

let currentImageToDelete = null;

function initializeImageDelete() {
    // Handle delete image buttons
    document.addEventListener('click', function(e) {
        if (e.target.closest('.btn-delete-image')) {
            const button = e.target.closest('.btn-delete-image');
            const imageId = button.dataset.imageId;
            const bookId = button.dataset.bookId;
            
            if (imageId && bookId) {
                currentImageToDelete = { imageId, bookId };
                showDeleteModal();
            }
        }
    });
    
    // Handle confirm delete button
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', function() {
            if (currentImageToDelete) {
                deleteImage(currentImageToDelete.imageId, currentImageToDelete.bookId);
            }
        });
    }
}

function showDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
        
        // Add fade in animation
        modal.style.opacity = '0';
        setTimeout(() => {
            modal.style.opacity = '1';
        }, 10);
    }
}

function hideDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (modal) {
        modal.style.opacity = '0';
        setTimeout(() => {
            modal.style.display = 'none';
            document.body.style.overflow = 'auto';
        }, 300);
    }
    currentImageToDelete = null;
}

function deleteImage(imageId, bookId) {
    const confirmBtn = document.getElementById('confirmDeleteBtn');
    if (confirmBtn) {
        // Show loading state
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Eliminazione...';
    }
    
    // Get CSRF token
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    
    // Create form data for POST request
    const formData = new FormData();
    formData.append(header.replace('X-', '').toLowerCase(), token);
    
    fetch(`/book/edit/${bookId}/images/delete/${imageId}`, {
        method: 'POST',
        headers: {
            [header]: token
        },
        body: formData
    })
    .then(response => {
        if (response.ok) {
            // Redirect to refresh the page with success message
            window.location.href = `/book/edit/${bookId}/images?success=delete`;
        } else {
            throw new Error('Errore durante l\'eliminazione');
        }
    })
    .catch(error => {
        console.error('Error deleting image:', error);
        showAlert('Errore durante l\'eliminazione dell\'immagine.', 'danger');
        hideDeleteModal();
        
        // Reset button state
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = '<i class="fas fa-trash"></i> Elimina';
        }
    });
}

/* =======================================================================
   BACK TO TOP FUNCTIONALITY
   ======================================================================= */

function initializeBackToTop() {
    const backToTopBtn = document.getElementById('backToTop');
    
    if (!backToTopBtn) return;
    
    // Show/hide button based on scroll position
    window.addEventListener('scroll', function() {
        if (window.pageYOffset > 300) {
            backToTopBtn.classList.add('visible');
        } else {
            backToTopBtn.classList.remove('visible');
        }
    });
    
    // Handle click
    backToTopBtn.addEventListener('click', function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
}

/* =======================================================================
   ALERT FUNCTIONALITY
   ======================================================================= */

function initializeAlerts() {
    // Auto-hide alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            if (alert.parentElement) {
                alert.style.opacity = '0';
                alert.style.transform = 'translateY(-20px)';
                setTimeout(() => {
                    if (alert.parentElement) {
                        alert.remove();
                    }
                }, 300);
            }
        }, 5000);
    });
}

function showAlert(message, type = 'info') {
    const alertContainer = document.querySelector('.alert-container');
    if (!alertContainer) return;
    
    const alertElement = document.createElement('div');
    alertElement.className = `alert alert-${type}`;
    alertElement.innerHTML = `
        <i class="fas fa-${type === 'danger' ? 'exclamation-triangle' : type === 'success' ? 'check-circle' : 'info-circle'}"></i>
        <span>${message}</span>
        <button type="button" class="alert-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    alertContainer.appendChild(alertElement);
    
    // Animate in
    alertElement.style.opacity = '0';
    alertElement.style.transform = 'translateY(-20px)';
    setTimeout(() => {
        alertElement.style.opacity = '1';
        alertElement.style.transform = 'translateY(0)';
    }, 10);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (alertElement.parentElement) {
            alertElement.style.opacity = '0';
            alertElement.style.transform = 'translateY(-20px)';
            setTimeout(() => {
                if (alertElement.parentElement) {
                    alertElement.remove();
                }
            }, 300);
        }
    }, 5000);
}

/* =======================================================================
   DRAG & DROP FUNCTIONALITY (Enhanced UX)
   ======================================================================= */

function initializeDragAndDrop() {
    const fileInput = document.getElementById('imageFiles');
    const uploadCard = fileInput?.closest('.form-card');
    
    if (!uploadCard || !fileInput) return;
    
    // Prevent default drag behaviors
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        uploadCard.addEventListener(eventName, preventDefaults, false);
        document.body.addEventListener(eventName, preventDefaults, false);
    });
    
    // Highlight drop area
    ['dragenter', 'dragover'].forEach(eventName => {
        uploadCard.addEventListener(eventName, highlight, false);
    });
    
    ['dragleave', 'drop'].forEach(eventName => {
        uploadCard.addEventListener(eventName, unhighlight, false);
    });
    
    // Handle dropped files
    uploadCard.addEventListener('drop', handleDrop, false);
    
    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }
    
    function highlight(e) {
        uploadCard.classList.add('drag-over');
    }
    
    function unhighlight(e) {
        uploadCard.classList.remove('drag-over');
    }
    
    function handleDrop(e) {
        const dt = e.dataTransfer;
        const files = dt.files;
        
        fileInput.files = files;
        
        // Trigger change event
        const event = new Event('change', { bubbles: true });
        fileInput.dispatchEvent(event);
    }
}

// Initialize drag & drop when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeDragAndDrop();
});

/* =======================================================================
   UTILITY FUNCTIONS
   ======================================================================= */

// Format file size for display
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Validate image file type
function isValidImageType(file) {
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
    return allowedTypes.includes(file.type);
}

// Global function to hide modal (called from HTML onclick)
window.hideDeleteModal = hideDeleteModal;

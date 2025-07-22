/* =======================================================================
   BIBLIOTECA SITARELLO - BOOK CREATE PAGE JAVASCRIPT
   JavaScript functionality for the book creation page with step-based form
   ======================================================================= */

document.addEventListener('DOMContentLoaded', function() {
    console.log('Book Create JS initialized');
    
    // Prevent double initialization
    if (window.bookCreatorInitialized) {
        console.log('Book Creator already initialized, skipping...');
        return;
    }
    
    window.bookCreatorInitialized = true;
    
    // Initialize the book creation functionality
    initializeBookCreator();
});

// Main book creator object
const bookCreator = {
    currentStep: 1,
    maxSteps: 3,
    uploadedFiles: [],
    maxFiles: 10,
    maxFileSize: 5 * 1024 * 1024, // 5MB
    
    init: function() {
        this.setupImageUpload();
        this.setupNavigation();
        this.setupFormValidation();
        this.setupPreviewUpdates();
        this.updateProgress();
        this.updateNavigationButtons();
    },
    
    // Setup image upload functionality
    setupImageUpload: function() {
        const imageInput = document.getElementById('imageInput');
        const uploadArea = document.getElementById('imageUploadArea');
        const uploadButton = document.getElementById('uploadButton');
        
        if (!imageInput || !uploadArea || !uploadButton) {
            console.warn('Image upload elements not found');
            return;
        }
        
        // Check if already initialized
        if (imageInput.hasAttribute('data-initialized')) {
            console.log('Image upload already initialized');
            return;
        }
        
        imageInput.setAttribute('data-initialized', 'true');
        uploadArea.setAttribute('data-initialized', 'true');
        uploadButton.setAttribute('data-initialized', 'true');
        
        // Click to upload
        uploadArea.addEventListener('click', () => {
            console.log('Upload area clicked');
            imageInput.click();
        });
        
        uploadButton.addEventListener('click', (e) => {
            console.log('Upload button clicked');
            e.preventDefault();
            e.stopPropagation();
            imageInput.click();
        });
        
        // Drag and drop
        uploadArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            uploadArea.classList.add('dragover');
        });
        
        uploadArea.addEventListener('dragleave', (e) => {
            e.preventDefault();
            uploadArea.classList.remove('dragover');
        });
        
        uploadArea.addEventListener('drop', (e) => {
            e.preventDefault();
            uploadArea.classList.remove('dragover');
            const files = Array.from(e.dataTransfer.files);
            this.handleFileSelection(files);
        });
        
        // File input change
        imageInput.addEventListener('change', (e) => {
            console.log('File input changed, files:', e.target.files.length);
            const files = Array.from(e.target.files);
            this.handleFileSelection(files);
            // Clear input to allow selecting the same file again
            e.target.value = '';
        });
    },
    
    // Handle file selection
    handleFileSelection: function(files) {
        const validFiles = [];
        
        for (let file of files) {
            // Check file type
            if (!file.type.startsWith('image/')) {
                alert(`Il file "${file.name}" non è un'immagine valida.`);
                continue;
            }
            
            // Check file size
            if (file.size > this.maxFileSize) {
                alert(`Il file "${file.name}" è troppo grande. Dimensione massima: 5MB.`);
                continue;
            }
            
            // Check max files limit
            if (this.uploadedFiles.length + validFiles.length >= this.maxFiles) {
                alert(`Puoi caricare massimo ${this.maxFiles} immagini.`);
                break;
            }
            
            validFiles.push(file);
        }
        
        // Add valid files
        validFiles.forEach(file => {
            this.addFileToPreview(file);
        });
        
        this.renderImagePreview();
        this.updatePreview();
    },
    
    // Add file to preview
    addFileToPreview: function(file) {
        const fileObj = {
            file: file,
            id: Date.now() + Math.random(),
            isMain: this.uploadedFiles.length === 0 // First image is main
        };
        
        this.uploadedFiles.push(fileObj);
        
        // Create preview element
        const reader = new FileReader();
        reader.onload = (e) => {
            fileObj.dataUrl = e.target.result;
            this.renderImagePreview();
        };
        reader.readAsDataURL(file);
    },
    
    // Render image preview
    renderImagePreview: function() {
        const previewGrid = document.getElementById('imagePreviewGrid');
        const imagesPreview = document.getElementById('imagesPreview');
        
        if (!previewGrid || !imagesPreview) return;
        
        // Show/hide preview section
        if (this.uploadedFiles.length > 0) {
            imagesPreview.classList.add('visible');
        } else {
            imagesPreview.classList.remove('visible');
        }
        
        // Clear current preview
        previewGrid.innerHTML = '';
        
        // Render each image
        this.uploadedFiles.forEach((fileObj, index) => {
            if (!fileObj.dataUrl) return;
            
            const previewItem = document.createElement('div');
            previewItem.className = 'image-preview-item';
            previewItem.innerHTML = `
                <img src="${fileObj.dataUrl}" alt="Anteprima ${index + 1}">
                <button type="button" class="remove-image" data-image-id="${fileObj.id}">
                    <i class="fas fa-times"></i>
                </button>
                ${fileObj.isMain ? '<div class="main-badge">Copertina</div>' : ''}
            `;
            
            // Add event listener for remove button
            const removeBtn = previewItem.querySelector('.remove-image');
            removeBtn.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                this.removeImage(fileObj.id);
            });
            
            previewGrid.appendChild(previewItem);
        });
        
        this.updateImageInfo();
    },
    
    // Remove image
    removeImage: function(imageId) {
        this.uploadedFiles = this.uploadedFiles.filter(fileObj => fileObj.id !== imageId);
        
        // If removed image was main, make first image main
        if (this.uploadedFiles.length > 0) {
            this.uploadedFiles[0].isMain = true;
        }
        
        this.renderImagePreview();
        this.updatePreview();
    },
    
    // Update image info
    updateImageInfo: function() {
        const imageCount = document.getElementById('imageCount');
        const totalSize = document.getElementById('totalSize');
        
        if (imageCount) {
            imageCount.textContent = this.uploadedFiles.length;
        }
        
        if (totalSize) {
            const total = this.uploadedFiles.reduce((sum, fileObj) => sum + fileObj.file.size, 0);
            const totalMB = (total / (1024 * 1024)).toFixed(1);
            totalSize.textContent = totalMB;
        }
    },
    
    // Setup navigation
    setupNavigation: function() {
        const prevBtn = document.getElementById('prevBtn');
        const nextBtn = document.getElementById('nextBtn');
        const submitBtn = document.getElementById('submitBtn');
        
        if (prevBtn && !prevBtn.hasAttribute('data-initialized')) {
            prevBtn.setAttribute('data-initialized', 'true');
            prevBtn.addEventListener('click', () => this.previousStep());
        }
        
        if (nextBtn && !nextBtn.hasAttribute('data-initialized')) {
            nextBtn.setAttribute('data-initialized', 'true');
            nextBtn.addEventListener('click', () => this.nextStep());
        }
        
        if (submitBtn && !submitBtn.hasAttribute('data-initialized')) {
            submitBtn.setAttribute('data-initialized', 'true');
            submitBtn.addEventListener('click', (e) => this.submitForm(e));
        }
    },
    
    // Navigation functions
    nextStep: function() {
        if (this.validateCurrentStep() && this.currentStep < this.maxSteps) {
            this.currentStep++;
            this.showStep(this.currentStep);
            this.updateProgress();
            this.updateNavigationButtons();
            
            if (this.currentStep === 3) {
                this.updatePreview();
            }
        }
    },
    
    previousStep: function() {
        if (this.currentStep > 1) {
            this.currentStep--;
            this.showStep(this.currentStep);
            this.updateProgress();
            this.updateNavigationButtons();
        }
    },
    
    // Show specific step
    showStep: function(step) {
        // Hide all steps
        document.querySelectorAll('.form-step').forEach(stepEl => {
            stepEl.classList.remove('active');
        });
        
        // Show current step
        const currentStepEl = document.getElementById(`step${step}`);
        if (currentStepEl) {
            currentStepEl.classList.add('active');
        }
        
        // Update step indicators
        document.querySelectorAll('.step').forEach((stepEl, index) => {
            stepEl.classList.remove('active', 'completed');
            const stepNumber = index + 1;
            
            if (stepNumber === step) {
                stepEl.classList.add('active');
            } else if (stepNumber < step) {
                stepEl.classList.add('completed');
            }
        });
    },
    
    // Update progress line
    updateProgress: function() {
        const progressLine = document.getElementById('progressLine');
        if (progressLine && progressLine.querySelector('::before')) {
            const progress = ((this.currentStep - 1) / (this.maxSteps - 1)) * 100;
            progressLine.style.setProperty('--progress', `${progress}%`);
        }
    },
    
    // Update navigation buttons
    updateNavigationButtons: function() {
        const prevBtn = document.getElementById('prevBtn');
        const nextBtn = document.getElementById('nextBtn');
        const submitBtn = document.getElementById('submitBtn');
        
        if (prevBtn) {
            prevBtn.disabled = this.currentStep === 1;
        }
        
        if (nextBtn && submitBtn) {
            if (this.currentStep === this.maxSteps) {
                nextBtn.style.display = 'none';
                submitBtn.style.display = 'flex';
            } else {
                nextBtn.style.display = 'flex';
                submitBtn.style.display = 'none';
            }
        }
    },
    
    // Validate current step
    validateCurrentStep: function() {
        switch (this.currentStep) {
            case 1:
                // Images step - optional, so always valid
                return true;
                
            case 2:
                // Details step
                const title = document.getElementById('title').value.trim();
                const authorId = document.getElementById('authorId').value;
                const publicationYear = document.getElementById('publicationYear').value;
                
                if (!title) {
                    alert('Il titolo è obbligatorio');
                    document.getElementById('title').focus();
                    return false;
                }
                
                if (!authorId) {
                    alert('Seleziona un autore');
                    document.getElementById('authorId').focus();
                    return false;
                }
                
                // Validate publication year
                if (publicationYear && !this.validatePublicationYear(publicationYear, authorId)) {
                    return false;
                }
                
                return true;
                
            case 3:
                // Preview step - always valid
                return true;
                
            default:
                return true;
        }
    },
    
    // Validate publication year against author's life dates
    validatePublicationYear: function(year, authorId) {
        const yearNum = parseInt(year);
        const publicationYearInput = document.getElementById('publicationYear');
        
        // Clear previous error styling
        if (publicationYearInput) {
            publicationYearInput.classList.remove('error');
        }
        
        // Check if year is not greater than 2025
        if (yearNum > 2025) {
            alert('L\'anno di pubblicazione non può essere successivo al 2025');
            if (publicationYearInput) {
                publicationYearInput.focus();
                publicationYearInput.classList.add('error');
            }
            return false;
        }
        
        // Find author data
        const authorSelect = document.getElementById('authorId');
        const selectedOption = authorSelect.querySelector(`option[value="${authorId}"]`);
        
        if (selectedOption) {
            // Get author birth and death dates from data attributes
            const birthYear = selectedOption.getAttribute('data-birth-year');
            const deathYear = selectedOption.getAttribute('data-death-year');
            
            if (birthYear) {
                const birthYearNum = parseInt(birthYear);
                
                // Check if publication year is before author's birth
                if (yearNum < birthYearNum) {
                    alert(`L'anno di pubblicazione (${yearNum}) non può essere precedente alla nascita dell'autore (${birthYearNum})`);
                    if (publicationYearInput) {
                        publicationYearInput.focus();
                        publicationYearInput.classList.add('error');
                    }
                    return false;
                }
                
                // Check if publication year is after author's death (if applicable)
                if (deathYear) {
                    const deathYearNum = parseInt(deathYear);
                    if (yearNum > deathYearNum) {
                        alert(`L'anno di pubblicazione (${yearNum}) non può essere successivo alla morte dell'autore (${deathYearNum})`);
                        if (publicationYearInput) {
                            publicationYearInput.focus();
                            publicationYearInput.classList.add('error');
                        }
                        return false;
                    }
                }
            }
        }
        
        return true;
    },
    
    // Setup form validation
    setupFormValidation: function() {
        const title = document.getElementById('title');
        const description = document.getElementById('description');
        
        // Title validation
        if (title) {
            title.addEventListener('blur', function() {
                if (this.value.trim().length === 0) {
                    this.classList.add('error');
                } else {
                    this.classList.remove('error');
                }
            });
        }
        
        // Description character count
        if (description) {
            description.addEventListener('input', function() {
                const maxLength = 1000;
                const currentLength = this.value.length;
                
                if (currentLength > maxLength) {
                    this.value = this.value.substring(0, maxLength);
                }
            });
        }
    },
    
    // Setup preview updates
    setupPreviewUpdates: function() {
        const inputs = ['title', 'authorId', 'publicationYear', 'description'];
        
        inputs.forEach(inputId => {
            const input = document.getElementById(inputId);
            if (input) {
                input.addEventListener('input', () => {
                    if (this.currentStep === 3) {
                        this.updatePreview();
                    }
                });
            }
        });
    },
    
    // Update preview
    updatePreview: function() {
        const title = document.getElementById('title').value.trim() || 'Titolo del libro';
        const authorSelect = document.getElementById('authorId');
        const authorText = authorSelect && authorSelect.selectedIndex > 0 
            ? authorSelect.options[authorSelect.selectedIndex].text 
            : 'Autore non specificato';
        const year = document.getElementById('publicationYear').value || 'Anno non specificato';
        const description = document.getElementById('description').value.trim() || 'Descrizione non disponibile';
        
        // Update preview elements
        const previewTitle = document.getElementById('previewTitle');
        const previewAuthor = document.getElementById('previewAuthor');
        const previewYear = document.getElementById('previewYear');
        const previewDescription = document.getElementById('previewDescription');
        const previewMainImage = document.getElementById('previewMainImage');
        const previewPlaceholder = document.getElementById('previewPlaceholder');
        const previewThumbnails = document.getElementById('previewThumbnails');
        
        if (previewTitle) previewTitle.textContent = title;
        if (previewAuthor) previewAuthor.textContent = `Autore: ${authorText}`;
        if (previewYear) previewYear.textContent = `Anno: ${year}`;
        if (previewDescription) previewDescription.textContent = description;
        
        // Update images
        if (this.uploadedFiles.length > 0 && this.uploadedFiles[0].dataUrl) {
            if (previewMainImage) {
                previewMainImage.src = this.uploadedFiles[0].dataUrl;
                previewMainImage.style.display = 'block';
            }
            if (previewPlaceholder) {
                previewPlaceholder.style.display = 'none';
            }
            
            // Update thumbnails
            if (previewThumbnails) {
                previewThumbnails.innerHTML = '';
                this.uploadedFiles.slice(1, 5).forEach(fileObj => {
                    if (fileObj.dataUrl) {
                        const thumb = document.createElement('img');
                        thumb.src = fileObj.dataUrl;
                        thumb.addEventListener('click', () => {
                            if (previewMainImage) {
                                previewMainImage.src = fileObj.dataUrl;
                            }
                        });
                        previewThumbnails.appendChild(thumb);
                    }
                });
            }
        } else {
            if (previewMainImage) {
                previewMainImage.style.display = 'none';
            }
            if (previewPlaceholder) {
                previewPlaceholder.style.display = 'flex';
            }
            if (previewThumbnails) {
                previewThumbnails.innerHTML = '';
            }
        }
    },
    
    // Submit form
    submitForm: function(e) {
        e.preventDefault();
        
        const submitBtn = document.getElementById('submitBtn');
        const form = document.getElementById('bookForm');
        
        if (!form) {
            console.error('Form not found');
            return;
        }
        
        // Validate all steps
        for (let step = 1; step <= this.maxSteps; step++) {
            const tempStep = this.currentStep;
            this.currentStep = step;
            if (!this.validateCurrentStep()) {
                this.currentStep = tempStep;
                this.showStep(step);
                this.updateProgress();
                this.updateNavigationButtons();
                return;
            }
            this.currentStep = tempStep;
        }
        
        // Show loading state
        if (submitBtn) {
            submitBtn.classList.add('loading');
            submitBtn.disabled = true;
        }
        
        // Create FormData
        const formData = new FormData();
        
        // Add form fields
        const formElements = form.elements;
        for (let element of formElements) {
            if (element.name && element.type !== 'file' && element.type !== 'submit') {
                if (element.type === 'checkbox' || element.type === 'radio') {
                    if (element.checked) {
                        formData.append(element.name, element.value);
                    }
                } else {
                    formData.append(element.name, element.value);
                }
            }
        }
        
        // Add uploaded files
        console.log('Adding', this.uploadedFiles.length, 'files to form');
        this.uploadedFiles.forEach((fileObj, index) => {
            console.log(`Adding file ${index + 1}:`, fileObj.file.name, 'Size:', fileObj.file.size, 'Type:', fileObj.file.type);
            formData.append('images', fileObj.file);
        });
        
        // Log FormData contents
        console.log('FormData contents:');
        for (let [key, value] of formData.entries()) {
            console.log(key, ':', value);
        }
        
        // Submit form
        fetch(form.action, {
            method: 'POST',
            body: formData,
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => {
            console.log('Response status:', response.status);
            if (response.ok) {
                // Check if it's a redirect
                if (response.redirected) {
                    window.location.href = response.url;
                    return;
                }
                
                // Handle success response
                return response.text().then(text => {
                    console.log('Response text:', text);
                    
                    // Check if response contains redirect info
                    if (text.includes('redirect') || text.includes('/book/')) {
                        alert('Libro creato con successo!');
                        window.location.href = '/book';
                    } else {
                        // Try to parse as JSON for more info
                        try {
                            const jsonResponse = JSON.parse(text);
                            if (jsonResponse.success) {
                                alert('Libro creato con successo!');
                                window.location.href = jsonResponse.redirectUrl || '/book';
                            } else {
                                throw new Error(jsonResponse.message || 'Errore durante il salvataggio');
                            }
                        } catch (parseError) {
                            // Fallback: assume success and redirect
                            alert('Libro creato con successo!');
                            window.location.href = '/book';
                        }
                    }
                });
            } else {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Errore durante il salvataggio del libro: ' + error.message);
        })
        .finally(() => {
            // Remove loading state
            if (submitBtn) {
                submitBtn.classList.remove('loading');
                submitBtn.disabled = false;
            }
        });
    }
};

// Initialize when DOM is ready
function initializeBookCreator() {
    bookCreator.init();
    
    // Setup back to top button
    setupBackToTopButton();
    
    // Make available globally for onclick handlers
    window.bookCreator = bookCreator;
}

// Setup back to top button functionality
function setupBackToTopButton() {
    const backToTopBtn = document.getElementById('backToTop');
    
    if (backToTopBtn && !backToTopBtn.hasAttribute('data-initialized')) {
        backToTopBtn.setAttribute('data-initialized', 'true');
        
        window.addEventListener('scroll', function() {
            if (window.pageYOffset > 300) {
                backToTopBtn.classList.add('visible');
            } else {
                backToTopBtn.classList.remove('visible');
            }
        });
        
        backToTopBtn.addEventListener('click', function() {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }
}

// Author form function
function showAuthorForm() {
    if (confirm('Vuoi aggiungere un nuovo autore? Sarai reindirizzato alla pagina di creazione autore.')) {
        window.open('/author/create', '_blank');
    }
}

// Utility function to update auto-save indicator
function updateAutoSaveStatus(status = 'Bozza salvata automaticamente') {
    const indicator = document.getElementById('autoSaveStatus');
    if (indicator) {
        indicator.textContent = status;
    }
} 

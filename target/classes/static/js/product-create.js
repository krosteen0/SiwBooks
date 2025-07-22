// SOLUZIONE SEMPLICE: File input sovrapposto
console.log('=== PRODUCT CREATE JS LOADED ===');

// RIMOSSO IL PRIMO GESTORE - ora tutto viene gestito dalla classe EnhancedProductCreator

// Enhanced Product Creation JavaScript - Sitarello
console.log('Enhanced Product Create JS loaded');

class EnhancedProductCreator {
    constructor() {
        // Configuration
        this.currentStep = 1;
        this.totalSteps = 3;
        this.images = [];
        this.maxImages = 10;
        this.maxFileSize = 5 * 1024 * 1024; // 5MB
        this.allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
        
        // Auto-save
        this.autoSaveTimer = null;
        this.formData = {};
        this.hasUnsavedChanges = false;
        
        // Validation rules for both product and book forms
        this.validationRules = {
            // Book form fields
            title: { required: true, minLength: 3, maxLength: 100 },
            authorId: { required: true },
            description: { required: false, minLength: 10, maxLength: 1000 },
            publicationYear: { required: false, min: 1000, max: 2024 },
            // Product form fields
            productName: { required: true, minLength: 3, maxLength: 100 },
            productPrice: { required: true, min: 0 },
            productCategory: { required: true },
            productDescription: { required: false, minLength: 10, maxLength: 1000 }
        };
        
        // Price suggestions data (mock)
        this.priceSuggestions = {};
        
        // Category suggestions
        this.categorySuggestions = {
            'iphone': ['Smartphone', 'Elettronica'],
            'samsung': ['Smartphone', 'Elettronica'],
            'laptop': ['Computer', 'Elettronica'],
            'macbook': ['Computer', 'Elettronica'],
            'nike': ['Scarpe', 'Abbigliamento'],
            'adidas': ['Scarpe', 'Abbigliamento']
        };
        
        this.init();
    }
    
    init() {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.setupAll());
        } else {
            this.setupAll();
        }
    }
    
    setupAll() {
        console.log('Setting up enhanced product creator...');
        
        this.setupEventListeners();
        this.setupImageUpload();
        this.setupFormValidation();
        this.setupPreview();
        this.setupAutoSave();
        this.setupSmartFeatures();
        this.updateProgressBar();
        this.showStep(this.currentStep);
        
        console.log('Enhanced product creator initialized');
    }
    
    setupEventListeners() {
        // Navigation buttons
        const nextBtn = document.getElementById('nextBtn');
        const prevBtn = document.getElementById('prevBtn');
        const submitBtn = document.getElementById('submitBtn');
        
        if (nextBtn) nextBtn.addEventListener('click', () => this.nextStep());
        if (prevBtn) prevBtn.addEventListener('click', () => this.prevStep());
        
        // Only add submit listener if not already added by page-specific code
        if (submitBtn && !submitBtn.hasAttribute('data-listener-added')) {
            submitBtn.setAttribute('data-listener-added', 'true');
            submitBtn.addEventListener('click', (e) => this.submitForm(e));
        }
        
        // Form inputs for live preview and auto-save - support both product and book forms
        const productInputs = ['productName', 'productPrice', 'productDescription', 'productCategory'];
        const bookInputs = ['title', 'authorId', 'description', 'publicationYear'];
        const allInputs = [...productInputs, ...bookInputs];
        
        allInputs.forEach(inputId => {
            const element = document.getElementById(inputId);
            if (element) {
                element.addEventListener('input', () => {
                    this.updatePreview();
                    this.markUnsavedChanges();
                    this.scheduleAutoSave();
                });
                element.addEventListener('blur', () => this.validateField(inputId));
            }
        });
        
        // Condition radio buttons
        document.querySelectorAll('input[name="condition"]').forEach(radio => {
            radio.addEventListener('change', () => {
                this.updatePreview();
                this.markUnsavedChanges();
                this.scheduleAutoSave();
            });
        });
        
        // Character counters
        this.setupCharacterCounters();
        
        // Tips toggle
        this.setupTipsToggle();
        
        // Format buttons (if present)
        this.setupFormatButtons();
    }
    
    setupImageUpload() {
        const uploadArea = document.getElementById('imageUploadArea');
        const fileInput = document.getElementById('imageInput');
        const uploadButton = document.getElementById('uploadButton');
        
        if (!uploadArea || !fileInput || !uploadButton) {
            console.error('Upload elements not found');
            return;
        }
        
        console.log('Setting up image upload handlers...');
        
        // File input configuration
        fileInput.setAttribute('accept', 'image/jpeg,image/jpg,image/png,image/webp');
        fileInput.setAttribute('multiple', 'true');
        
        // Flags per evitare doppie aperture
        let isFileDialogOpen = false;
        
        // Funzione per aprire il file dialog con protezione contro doppie aperture
        const openFileDialog = (source) => {
            if (isFileDialogOpen) {
                console.log('🚫 File dialog già aperto, ignoro click da:', source);
                return;
            }
            
            console.log('🎯 Opening file dialog from:', source);
            isFileDialogOpen = true;
            
            // Reset flag dopo un breve delay
            setTimeout(() => {
                isFileDialogOpen = false;
            }, 500);
            
            fileInput.click();
        };
        
        // Gestore per il bottone di upload
        uploadButton.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            openFileDialog('button');
        });
        
        // Gestore per l'area di upload (solo se il click non viene dal bottone)
        uploadArea.addEventListener('click', (e) => {
            // Ignora i click che vengono dal bottone
            if (e.target === uploadButton || uploadButton.contains(e.target)) {
                return;
            }
            
            e.preventDefault();
            e.stopPropagation();
            openFileDialog('upload-area');
        });
        
        // File input change
        fileInput.addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                console.log(`🎉 ${e.target.files.length} file(s) selected`);
                this.handleFileSelect(e.target.files);
                e.target.value = ''; // Reset input
            }
        });
        
        // Drag and drop
        uploadArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            uploadArea.classList.add('dragover');
        });
        
        uploadArea.addEventListener('dragenter', (e) => {
            e.preventDefault();
            uploadArea.classList.add('dragover');
        });
        
        uploadArea.addEventListener('dragleave', (e) => {
            e.preventDefault();
            if (!uploadArea.contains(e.relatedTarget)) {
                uploadArea.classList.remove('dragover');
            }
        });
        
        uploadArea.addEventListener('drop', (e) => {
            e.preventDefault();
            uploadArea.classList.remove('dragover');
            this.handleFileSelect(e.dataTransfer.files);
        });
        
        // Prevent default drag behaviors
        document.addEventListener('dragover', (e) => e.preventDefault());
        document.addEventListener('drop', (e) => e.preventDefault());
    }
    
    handleFileSelect(files) {
        if (!files || files.length === 0) return;
        
        const remainingSlots = this.maxImages - this.images.length;
        
        Array.from(files).slice(0, remainingSlots).forEach((file, index) => {
            if (!this.allowedTypes.includes(file.type)) {
                this.showNotification(`Formato non supportato: ${file.name}`, 'error');
                return;
            }
            
            if (file.size > this.maxFileSize) {
                this.showNotification(`File troppo grande: ${file.name} (max 5MB)`, 'error');
                return;
            }
            
            this.addImage(file);
        });
        
        if (files.length > remainingSlots) {
            this.showNotification(`Puoi caricare solo ${remainingSlots} immagini aggiuntive`, 'warning');
        }
    }
    
    addImage(file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            const imageData = {
                file: file,
                dataUrl: e.target.result,
                id: Date.now() + Math.random(),
                size: file.size
            };
            
            this.images.push(imageData);
            this.renderImagePreviews();
            this.updateUploadInfo();
            this.updatePreview();
            this.markUnsavedChanges();
            this.scheduleAutoSave();
        };
        reader.readAsDataURL(file);
    }
    
    removeImage(imageId) {
        this.images = this.images.filter(img => img.id !== imageId);
        this.renderImagePreviews();
        this.updateUploadInfo();
        this.updatePreview();
        this.markUnsavedChanges();
        this.scheduleAutoSave();
    }
    
    renderImagePreviews() {
        const container = document.getElementById('imagePreviewGrid');
        if (!container) return;
        
        container.innerHTML = '';
        
        this.images.forEach((image, index) => {
            const item = document.createElement('div');
            item.className = 'image-preview-item';
            item.innerHTML = `
                <img src="${image.dataUrl}" alt="Preview ${index + 1}">
                <button type="button" class="image-remove-btn" onclick="productCreator.removeImage(${image.id})" title="Rimuovi immagine">
                    <i class="fas fa-times"></i>
                </button>
            `;
            container.appendChild(item);
        });
        
        // Hide upload area if max images reached
        const uploadArea = document.getElementById('imageUploadArea');
        if (uploadArea) {
            uploadArea.style.display = this.images.length >= this.maxImages ? 'none' : 'block';
        }
    }
    
    updateUploadInfo() {
        const imageCount = document.getElementById('imageCount');
        const totalSize = document.getElementById('totalSize');
        
        if (imageCount) {
            imageCount.textContent = this.images.length;
        }
        
        if (totalSize) {
            const totalSizeBytes = this.images.reduce((sum, img) => sum + img.size, 0);
            const totalSizeMB = (totalSizeBytes / (1024 * 1024)).toFixed(1);
            totalSize.textContent = `${totalSizeMB} MB`;
        }
    }
    
    setupCharacterCounters() {
        const fields = [
            { id: 'title', max: 100 },
            { id: 'description', max: 1000 }
        ];
        
        fields.forEach(field => {
            const element = document.getElementById(field.id);
            const counter = document.getElementById(field.id + 'Counter');
            
            if (element && counter) {
                const updateCounter = () => {
                    const length = element.value.length;
                    counter.textContent = length;
                    
                    counter.className = 'input-counter';
                    if (length > field.max * 0.8) counter.classList.add('warning');
                    if (length > field.max) counter.classList.add('error');
                };
                
                element.addEventListener('input', updateCounter);
                updateCounter(); // Initialize
            }
        });
    }
    
    setupTipsToggle() {
        const tipsHeader = document.querySelector('.tips-header');
        const tipsContent = document.querySelector('.tips-content');
        
        if (tipsHeader && tipsContent) {
            tipsHeader.addEventListener('click', () => {
                tipsHeader.classList.toggle('expanded');
                tipsContent.classList.toggle('expanded');
            });
        }
    }
    
    setupFormatButtons() {
        document.querySelectorAll('.format-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const format = btn.dataset.format;
                const textarea = document.getElementById('productDescription');
                
                if (textarea && format) {
                    this.applyTextFormat(textarea, format);
                }
            });
        });
    }
    
    applyTextFormat(textarea, format) {
        const start = textarea.selectionStart;
        const end = textarea.selectionEnd;
        const selectedText = textarea.value.substring(start, end);
        
        let formattedText = '';
        switch (format) {
            case 'bold':
                formattedText = `**${selectedText || 'testo in grassetto'}**`;
                break;
            case 'italic':
                formattedText = `*${selectedText || 'testo in corsivo'}*`;
                break;
            case 'list':
                formattedText = selectedText ? selectedText.split('\n').map(line => `• ${line}`).join('\n') : '• Elemento lista';
                break;
        }
        
        const newValue = textarea.value.substring(0, start) + formattedText + textarea.value.substring(end);
        textarea.value = newValue;
        textarea.focus();
        
        // Update counter and preview
        textarea.dispatchEvent(new Event('input'));
    }
    
    setupFormValidation() {
        // Setup real-time validation for all fields
        Object.keys(this.validationRules).forEach(fieldId => {
            const field = document.getElementById(fieldId) || document.querySelector(`input[name="${fieldId}"]`);
            if (field) {
                field.addEventListener('blur', () => this.validateField(fieldId));
                field.addEventListener('input', () => this.clearFieldError(fieldId));
            }
        });
    }
    
    validateField(fieldId) {
        let field, value;
        
        // Gestione speciale per radio buttons (condition)
        if (fieldId === 'condition') {
            const checkedRadio = document.querySelector(`input[name="${fieldId}"]:checked`);
            field = checkedRadio;
            value = checkedRadio ? checkedRadio.value : '';
        } else {
            field = document.getElementById(fieldId);
            value = field ? field.value.trim() : '';
        }
        
        const rules = this.validationRules[fieldId];
        
        if (!rules) {
            return true;
        }
        
        let isValid = true;
        let errorMessage = '';
        
        // Required validation
        if (rules.required && !value) {
            isValid = false;
            errorMessage = 'Questo campo è obbligatorio';
        }
        
        // Length validation
        if (value && rules.minLength && value.length < rules.minLength) {
            isValid = false;
            errorMessage = `Minimo ${rules.minLength} caratteri`;
        }
        
        if (value && rules.maxLength && value.length > rules.maxLength) {
            isValid = false;
            errorMessage = `Massimo ${rules.maxLength} caratteri`;
        }
        
        // Number validation
        if (value && rules.min !== undefined && parseFloat(value) < rules.min) {
            isValid = false;
            errorMessage = `Valore minimo: ${rules.min}`;
        }
        
        this.showFieldError(fieldId, isValid, errorMessage);
        return isValid;
    }
    
    showFieldError(fieldId, isValid, message) {
        const field = document.getElementById(fieldId) || document.querySelector(`input[name="${fieldId}"]`);
        if (!field) return;
        
        const formGroup = field.closest('.form-group');
        if (!formGroup) return;
        
        let errorElement = formGroup.querySelector('.error-message');
        
        if (isValid) {
            field.classList.remove('form-error');
            if (errorElement) {
                errorElement.classList.remove('show');
                setTimeout(() => errorElement.remove(), 200);
            }
        } else {
            field.classList.add('form-error');
            
            if (!errorElement) {
                errorElement = document.createElement('div');
                errorElement.className = 'error-message';
                formGroup.appendChild(errorElement);
            }
            
            errorElement.innerHTML = `<i class="fas fa-exclamation-triangle"></i> ${message}`;
            errorElement.classList.add('show');
        }
    }
    
    clearFieldError(fieldId) {
        const field = document.getElementById(fieldId);
        if (field) {
            field.classList.remove('form-error');
        }
    }
    
    validateStep1() {
        const isValid = this.images.length > 0;
        return isValid;
    }
    
    validateStep2() {
        // Check if this is a book form or product form
        const isBookForm = document.getElementById('title') !== null;
        
        let fields = [];
        if (isBookForm) {
            fields = ['title', 'authorId'];
        } else {
            fields = ['productName', 'productPrice', 'productCategory'];
        }
        
        let allValid = true;
        
        fields.forEach(fieldId => {
            const fieldValid = this.validateField(fieldId);
            if (!fieldValid) {
                allValid = false;
            }
        });
        
        return allValid;
    }
    
    setupPreview() {
        this.updatePreview();
    }
    
    updatePreview() {
        // Update preview title
        const titleElement = document.getElementById('previewTitle');
        const titleInput = document.getElementById('title');
        if (titleElement && titleInput) {
            titleElement.textContent = titleInput.value || 'Titolo del libro';
        }
        
        // Update preview author
        const authorElement = document.getElementById('previewAuthor');
        const authorSelect = document.getElementById('authorId');
        if (authorElement && authorSelect) {
            const selectedOption = authorSelect.options[authorSelect.selectedIndex];
            authorElement.textContent = selectedOption ? selectedOption.text : 'Autore';
        }
        
        // Update preview year
        const yearElement = document.getElementById('previewYear');
        const yearInput = document.getElementById('publicationYear');
        if (yearElement && yearInput) {
            yearElement.textContent = yearInput.value || 'Anno non specificato';
        }
        
        // Update preview description
        const descElement = document.getElementById('previewDescription');
        const descInput = document.getElementById('description');
        if (descElement && descInput) {
            descElement.textContent = descInput.value || 'Descrizione del libro...';
        }
        
        // Update preview images
        this.updatePreviewImages();
        
        // Update condition badge
        this.updateConditionBadge();
    }
    
    updatePreviewImages() {
        const mainImageContainer = document.getElementById('previewMainImageContainer');
        const thumbnailsContainer = document.getElementById('previewThumbnails');
        const placeholder = document.getElementById('previewPlaceholder');
        const mainImage = document.getElementById('previewMainImage');
        
        if (this.images.length > 0) {
            // Show main image
            if (mainImage && placeholder) {
                mainImage.src = this.images[0].dataUrl;
                mainImage.style.display = 'block';
                placeholder.style.display = 'none';
            }
            
            // Show thumbnails
            if (thumbnailsContainer) {
                thumbnailsContainer.innerHTML = '';
                this.images.forEach((image, index) => {
                    const thumb = document.createElement('div');
                    thumb.className = `preview-thumbnail ${index === 0 ? 'active' : ''}`;
                    thumb.innerHTML = `<img src="${image.dataUrl}" alt="Thumbnail ${index + 1}">`;
                    thumb.addEventListener('click', () => this.switchMainImage(index));
                    thumbnailsContainer.appendChild(thumb);
                });
            }
        } else {
            // Show placeholder
            if (mainImage && placeholder) {
                mainImage.style.display = 'none';
                placeholder.style.display = 'flex';
            }
            
            if (thumbnailsContainer) {
                thumbnailsContainer.innerHTML = '';
            }
        }
    }
    
    switchMainImage(index) {
        if (index < 0 || index >= this.images.length) return;
        
        const mainImage = document.getElementById('previewMainImage');
        if (mainImage) {
            mainImage.src = this.images[index].dataUrl;
        }
        
        // Update thumbnail active state
        document.querySelectorAll('.preview-thumbnail').forEach((thumb, i) => {
            thumb.classList.toggle('active', i === index);
        });
    }
    
    updateConditionBadge() {
        const badge = document.getElementById('conditionBadge');
        const conditionInput = document.querySelector('input[name="condition"]:checked');
        
        if (badge && conditionInput) {
            const conditionValue = conditionInput.value;
            const conditionText = conditionInput.closest('label').querySelector('.condition-title').textContent;
            
            badge.innerHTML = `<i class="fas fa-star"></i><span>${conditionText}</span>`;
            badge.style.display = 'flex';
        } else if (badge) {
            badge.style.display = 'none';
        }
    }
    
    setupAutoSave() {
        // Load saved data on page load
        this.loadAutoSavedData();
        
        // Save data before page unload
        window.addEventListener('beforeunload', (e) => {
            if (this.hasUnsavedChanges) {
                this.autoSave();
                e.preventDefault();
                e.returnValue = 'Hai modifiche non salvate. Sei sicuro di voler uscire?';
            }
        });
    }
    
    markUnsavedChanges() {
        this.hasUnsavedChanges = true;
    }
    
    scheduleAutoSave() {
        if (this.autoSaveTimer) {
            clearTimeout(this.autoSaveTimer);
        }
        
        this.autoSaveTimer = setTimeout(() => {
            this.autoSave();
        }, 2000); // Auto-save after 2 seconds of inactivity
    }
    
    autoSave() {
        const formData = this.gatherFormData();
        
        try {
            localStorage.setItem('productCreateAutoSave', JSON.stringify({
                data: formData,
                timestamp: Date.now(),
                images: this.images.map(img => ({ ...img, file: null })) // Don't save file objects
            }));
            
            this.showAutoSaveIndicator();
            this.hasUnsavedChanges = false;
        } catch (error) {
            console.warn('Auto-save failed:', error);
        }
    }
    
    loadAutoSavedData() {
        try {
            const saved = localStorage.getItem('productCreateAutoSave');
            if (saved) {
                const { data, timestamp } = JSON.parse(saved);
                
                // Only load if saved within last 24 hours
                if (Date.now() - timestamp < 24 * 60 * 60 * 1000) {
                    this.populateForm(data);
                    this.showNotification('Dati precedenti ripristinati automaticamente', 'info');
                }
            }
        } catch (error) {
            console.warn('Failed to load auto-saved data:', error);
        }
    }
    
    showAutoSaveIndicator() {
        const indicator = document.getElementById('autoSaveStatus');
        if (indicator) {
            indicator.parentElement.classList.add('show');
            setTimeout(() => {
                indicator.parentElement.classList.remove('show');
            }, 2000);
        }
    }
    
    gatherFormData() {
        // Check if this is a book form or product form
        const isBookForm = document.getElementById('title') !== null;
        
        if (isBookForm) {
            return {
                title: document.getElementById('title')?.value || '',
                authorId: document.getElementById('authorId')?.value || '',
                description: document.getElementById('description')?.value || '',
                publicationYear: document.getElementById('publicationYear')?.value || ''
            };
        } else {
            return {
                name: document.getElementById('productName')?.value || '',
                price: document.getElementById('productPrice')?.value || '',
                description: document.getElementById('productDescription')?.value || '',
                category: document.getElementById('productCategory')?.value || '',
                condition: document.querySelector('input[name="condition"]:checked')?.value || ''
            };
        }
    }
    
    populateForm(data) {
        Object.entries(data).forEach(([key, value]) => {
            if (key === 'condition') {
                const radio = document.querySelector(`input[name="condition"][value="${value}"]`);
                if (radio) radio.checked = true;
            } else {
                const field = document.getElementById(`product${key.charAt(0).toUpperCase() + key.slice(1)}`);
                if (field) field.value = value;
            }
        });
        
        this.updatePreview();
    }
    
    setupSmartFeatures() {
        this.setupNameSuggestions();
        this.setupPriceSuggestions();
    }
    
    setupNameSuggestions() {
        const nameInput = document.getElementById('productName');
        const suggestionsContainer = document.getElementById('nameSuggestions');
        
        if (nameInput && suggestionsContainer) {
            nameInput.addEventListener('input', () => {
                const value = nameInput.value.toLowerCase();
                this.showNameSuggestions(value, suggestionsContainer);
            });
        }
    }
    
    showNameSuggestions(query, container) {
        container.innerHTML = '';
        
        if (query.length < 2) return;
        
        const suggestions = [];
        Object.entries(this.categorySuggestions).forEach(([keyword, categories]) => {
            if (keyword.includes(query)) {
                suggestions.push(`${keyword.charAt(0).toUpperCase() + keyword.slice(1)} - ${categories[0]}`);
            }
        });
        
        if (suggestions.length > 0) {
            container.style.display = 'block';
            suggestions.slice(0, 5).forEach(suggestion => {
                const item = document.createElement('div');
                item.className = 'suggestion-item';
                item.textContent = suggestion;
                item.addEventListener('click', () => {
                    document.getElementById('productName').value = suggestion.split(' - ')[0];
                    container.style.display = 'none';
                    this.updatePreview();
                });
                container.appendChild(item);
            });
        } else {
            container.style.display = 'none';
        }
    }
    
    setupPriceSuggestions() {
        const priceInput = document.getElementById('productPrice');
        const categorySelect = document.getElementById('productCategory');
        
        if (priceInput && categorySelect) {
            const showPriceSuggestion = () => {
                const category = categorySelect.value;
                const name = document.getElementById('productName')?.value.toLowerCase() || '';
                
                // Mock price suggestion logic
                if (category && name) {
                    this.showPriceSuggestion(category, name);
                }
            };
            
            priceInput.addEventListener('blur', showPriceSuggestion);
            categorySelect.addEventListener('change', showPriceSuggestion);
        }
    }
    
    showPriceSuggestion(category, name) {
        const container = document.getElementById('priceSuggestions');
        if (!container) return;
        
        // Mock price suggestion (in real app, this would call an API)
        let suggestedPrice = 0;
        if (name.includes('iphone')) suggestedPrice = 500;
        else if (name.includes('laptop')) suggestedPrice = 800;
        else if (name.includes('scarpe')) suggestedPrice = 80;
        
        if (suggestedPrice > 0) {
            container.innerHTML = `
                <div class="price-suggestion">
                    <i class="fas fa-lightbulb"></i>
                    <span>Prezzo suggerito: €${suggestedPrice}</span>
                    <button type="button" onclick="document.getElementById('productPrice').value=${suggestedPrice}; productCreator.updatePreview()">
                        Usa
                    </button>
                </div>
            `;
            container.style.display = 'block';
        } else {
            container.style.display = 'none';
        }
    }
    
    showStep(step) {
        // Hide all steps
        document.querySelectorAll('.form-step').forEach(stepEl => {
            stepEl.classList.remove('active');
        });
        
        // Show current step
        const currentStepEl = document.getElementById(`step${step}`);
        if (currentStepEl) {
            currentStepEl.classList.add('active');
        }
        
        // Update navigation buttons
        this.updateNavigationButtons();
        
        // Update step indicator
        this.updateStepIndicator();
        
        // Update progress bar
        this.updateProgressBar();
        
        // Update preview when on step 3
        if (step === 3) {
            this.updatePreview();
        }
    }
    
    updateNavigationButtons() {
        const prevBtn = document.getElementById('prevBtn');
        const nextBtn = document.getElementById('nextBtn');
        const submitBtn = document.getElementById('submitBtn');
        
        if (prevBtn) {
            prevBtn.style.display = this.currentStep > 1 ? 'inline-flex' : 'none';
        }
        
        if (nextBtn) {
            nextBtn.style.display = this.currentStep < this.totalSteps ? 'inline-flex' : 'none';
            // I bottoni sono sempre abilitati - la validazione avviene solo al click
            nextBtn.disabled = false;
        }
        
        if (submitBtn) {
            submitBtn.style.display = this.currentStep === this.totalSteps ? 'inline-flex' : 'none';
            // Il bottone submit è sempre abilitato - la validazione avviene solo al click
            submitBtn.disabled = false;
        }
    }
    
    updateStepIndicator() {
        const indicator = document.getElementById('currentStepText');
        if (indicator) {
            indicator.textContent = `Passo ${this.currentStep} di ${this.totalSteps}`;
        }
    }
    
    updateProgressBar() {
        const progressLine = document.getElementById('progressLine');
        const steps = document.querySelectorAll('.step');
        
        // Update progress line width
        if (progressLine) {
            const progressPercentage = ((this.currentStep - 1) / (this.totalSteps - 1)) * 100;
            progressLine.style.width = `${progressPercentage}%`;
        }
        
        // Update step states
        steps.forEach((stepEl, index) => {
            const stepNumber = index + 1;
            stepEl.classList.remove('active', 'completed');
            
            if (stepNumber < this.currentStep) {
                stepEl.classList.add('completed');
            } else if (stepNumber === this.currentStep) {
                stepEl.classList.add('active');
            }
        });
    }
    
    nextStep() {
        let canProceed = false;
        let errorMessage = '';
        
        switch (this.currentStep) {
            case 1:
                canProceed = this.validateStep1();
                if (!canProceed) {
                    errorMessage = 'Carica almeno una foto del prodotto prima di continuare';
                }
                break;
            case 2:
                canProceed = this.validateStep2();
                if (!canProceed) {
                    errorMessage = 'Completa tutti i campi obbligatori prima di continuare';
                }
                break;
            default:
                canProceed = true;
        }
        
        if (canProceed && this.currentStep < this.totalSteps) {
            this.currentStep++;
            this.showStep(this.currentStep);
        } else if (!canProceed) {
            this.showNotification(errorMessage, 'error');
        }
    }
    
    prevStep() {
        if (this.currentStep > 1) {
            this.currentStep--;
            this.showStep(this.currentStep);
        }
    }
    
    async submitForm(e) {
        e.preventDefault();
        console.log('📝 Starting form submission...');
        
        // Validazione completa: campi obbligatori + almeno una foto
        console.log('🔍 Validating step 1...');
        if (!this.validateStep1()) {
            console.log('❌ Step 1 validation failed');
            this.showNotification('Carica almeno una foto del prodotto', 'error');
            return;
        }
        console.log('✅ Step 1 validation passed');
        
        console.log('🔍 Validating step 2...');
        if (!this.validateStep2()) {
            console.log('❌ Step 2 validation failed');
            this.showNotification('Completa tutti i campi obbligatori', 'error');
            return;
        }
        console.log('✅ Step 2 validation passed');
        
        const submitBtn = document.getElementById('submitBtn');
        const loadingOverlay = document.getElementById('loadingOverlay');
        
        try {
            console.log('🔄 Starting submission process...');
            // Show loading
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.querySelector('.btn-loading').style.display = 'inline-block';
            }
            if (loadingOverlay) loadingOverlay.style.display = 'flex';
            
            // Prepare form data
            console.log('📦 Gathering form data...');
            const formData = new FormData();
            const data = this.gatherFormData();
            console.log('📋 Form data gathered:', data);
            
            Object.entries(data).forEach(([key, value]) => {
                formData.append(key, value);
            });
            
            // Add images
            console.log('🖼️ Adding images, count:', this.images.length);
            this.images.forEach((image, index) => {
                if (image.file) {
                    formData.append('images', image.file);
                    console.log(`📷 Added image ${index}:`, image.file.name);
                }
            });
            
            // Get CSRF token
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
            console.log('🔐 CSRF token:', csrfToken ? 'Found' : 'Not found');
            
            // Prepare headers
            const headers = {};
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }
            
            // Get form action dynamically
            const form = document.getElementById('bookForm') || document.getElementById('productForm') || document.querySelector('form');
            const formAction = form ? form.getAttribute('action') : '/product/create';
            console.log('🎯 Submitting to:', formAction);
            
            // Submit form
            console.log('🚀 Sending request...');
            const response = await fetch(formAction, {
                method: 'POST',
                headers: headers,
                body: formData
            });
            
            console.log('📨 Response received:', response.status, response.statusText);
            
            if (response.ok) {
                console.log('✅ Submission successful!');
                // Clear auto-saved data
                localStorage.removeItem('productCreateAutoSave');
                
                // Check if this is a book form or product form
                const isBookForm = document.getElementById('title') !== null;
                
                if (isBookForm) {
                    // For book form, show success notification and redirect
                    this.showNotification('Libro salvato con successo!', 'success');
                    setTimeout(() => {
                        window.location.href = '/book';
                    }, 2000);
                } else {
                    // For product form, show success modal
                    this.showSuccessModal();
                }
            } else {
                console.log('❌ Response not ok:', response.status);
                const errorText = await response.text();
                console.log('Error details:', errorText);
                throw new Error('Errore durante il salvataggio');
            }
            
        } catch (error) {
            console.error('Submit error:', error);
            this.showNotification('Errore durante il salvataggio del prodotto', 'error');
        } finally {
            // Hide loading
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.querySelector('.btn-loading').style.display = 'none';
            }
            if (loadingOverlay) loadingOverlay.style.display = 'none';
        }
    }
    
    showSuccessModal() {
        const modal = document.getElementById('successModal');
        if (modal) {
            modal.style.display = 'flex';
        }
    }
    
    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <i class="fas fa-${this.getNotificationIcon(type)}"></i>
                <span>${message}</span>
                <button class="notification-close">&times;</button>
            </div>
        `;
        
        // Add to page
        document.body.appendChild(notification);
        
        // Show with animation
        setTimeout(() => notification.classList.add('show'), 100);
        
        // Auto-hide after 5 seconds
        setTimeout(() => this.hideNotification(notification), 5000);
        
        // Close button
        notification.querySelector('.notification-close').addEventListener('click', () => {
            this.hideNotification(notification);
        });
    }
    
    hideNotification(notification) {
        notification.classList.remove('show');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }
    
    getNotificationIcon(type) {
        switch (type) {
            case 'success': return 'check-circle';
            case 'error': return 'exclamation-triangle';
            case 'warning': return 'exclamation-circle';
            default: return 'info-circle';
        }
    }
}

// Initialize when page loads
let productCreator;
document.addEventListener('DOMContentLoaded', () => {
    productCreator = new EnhancedProductCreator();
    // Make it globally accessible for onclick handlers
    window.productCreator = productCreator;
});

// Global function for backward compatibility
function changeStep(direction) {
    if (productCreator) {
        if (direction > 0) {
            productCreator.nextStep();
        } else {
            productCreator.prevStep();
        }
    }
}

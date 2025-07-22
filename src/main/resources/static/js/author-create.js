// Author Create Page - Biblioteca Sitarello
document.addEventListener('DOMContentLoaded', function() {
    let currentStep = 1;
    const totalSteps = 3;
    let autoSaveTimer;

    // Elementi DOM
    const steps = document.querySelectorAll('.step');
    const formSteps = document.querySelectorAll('.form-step');
    const progressLine = document.getElementById('progressLine');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const form = document.getElementById('authorForm');
    const bioTextarea = document.getElementById('biography');
    const bioCharCount = document.getElementById('bioCharCount');
    const uploadArea = document.getElementById('uploadArea');
    const photoFile = document.getElementById('photoFile');
    const imagePreview = document.getElementById('imagePreview');
    const previewImg = document.getElementById('previewImg');
    const autoSaveStatus = document.getElementById('autoSaveStatus');

    // Inizializzazione
    init();

    function init() {
        updateProgressBar();
        updateNavigationButtons();
        setupEventListeners();
        updatePreview();
        
        // Auto-save setup
        setupAutoSave();
        
        // Character counter setup
        if (bioTextarea && bioCharCount) {
            updateCharacterCount();
        }
        
        // Upload area setup
        setupFileUpload();
        
        // Smooth scroll to top
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function setupEventListeners() {
        // Navigation buttons
        if (prevBtn) {
            prevBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                previousStep();
            });
        }
        
        if (nextBtn) {
            nextBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                nextStep();
            });
        }

        // Preview step buttons
        const previewPrevBtn = document.getElementById('previewPrevBtn');
        if (previewPrevBtn) {
            previewPrevBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                previousStep();
            });
        }

        // Error close button
        const errorCloseBtn = document.getElementById('errorCloseBtn');
        if (errorCloseBtn) {
            errorCloseBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                hideError();
            });
        }

        // Remove image button
        const removeImageBtn = document.getElementById('removeImageBtn');
        if (removeImageBtn) {
            removeImageBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                removeImage();
            });
        }

        // Form inputs for auto-save and preview update
        const inputs = form.querySelectorAll('input, textarea, select');
        inputs.forEach(input => {
            input.addEventListener('input', handleInputChange);
            input.addEventListener('blur', updatePreview);
        });

        // Character counter
        if (bioTextarea) {
            bioTextarea.addEventListener('input', updateCharacterCount);
        }

        // Date validation
        const birthDateInput = document.getElementById('birthDate');
        const deathDateInput = document.getElementById('deathDate');
        
        if (birthDateInput) {
            birthDateInput.addEventListener('change', validateDates);
            birthDateInput.addEventListener('blur', validateDates);
        }
        
        if (deathDateInput) {
            deathDateInput.addEventListener('change', validateDates);
            deathDateInput.addEventListener('blur', validateDates);
        }

        // Form submission
        form.addEventListener('submit', handleFormSubmit);

        // Prevent form submission on Enter key (except in textarea)
        form.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' && e.target.tagName !== 'TEXTAREA') {
                e.preventDefault();
                if (currentStep < totalSteps) {
                    nextStep();
                } else {
                    handleFormSubmit(e);
                }
            }
        });
    }

    function setupAutoSave() {
        // Simulated auto-save functionality
        const inputs = form.querySelectorAll('input, textarea, select');
        inputs.forEach(input => {
            input.addEventListener('input', function() {
                clearTimeout(autoSaveTimer);
                updateAutoSaveStatus('saving');
                
                autoSaveTimer = setTimeout(() => {
                    // Simulate saving to localStorage or server
                    saveFormData();
                    updateAutoSaveStatus('saved');
                }, 1000);
            });
        });

        // Load saved data on page load
        loadFormData();
    }

    function saveFormData() {
        const formData = new FormData(form);
        const data = {};
        for (let [key, value] of formData.entries()) {
            if (key !== 'photoFile') { // Don't save file data
                data[key] = value;
            }
        }
        localStorage.setItem('authorCreateData', JSON.stringify(data));
    }

    function loadFormData() {
        const savedData = localStorage.getItem('authorCreateData');
        if (savedData) {
            try {
                const data = JSON.parse(savedData);
                Object.keys(data).forEach(key => {
                    const input = form.querySelector(`[name="${key}"]`);
                    if (input && input.type !== 'file') {
                        input.value = data[key];
                    }
                });
                updatePreview();
                updateCharacterCount();
            } catch (e) {
                console.warn('Failed to load saved form data');
            }
        }
    }

    function updateAutoSaveStatus(status) {
        const statusElement = autoSaveStatus;
        if (!statusElement) return;

        switch (status) {
            case 'saving':
                statusElement.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Salvataggio...';
                statusElement.parentElement.style.background = 'linear-gradient(135deg, #FF8C00, #FFA500)';
                break;
            case 'saved':
                statusElement.innerHTML = '<i class="fas fa-check"></i> Salvato';
                statusElement.parentElement.style.background = 'linear-gradient(135deg, #228B22, #32CD32)';
                break;
            case 'error':
                statusElement.innerHTML = '<i class="fas fa-exclamation-triangle"></i> Errore';
                statusElement.parentElement.style.background = 'linear-gradient(135deg, #DC143C, #FF6B6B)';
                break;
        }
    }

    function validateDates() {
        const birthDate = document.getElementById('birthDate');
        const deathDate = document.getElementById('deathDate');
        
        if (!birthDate || !deathDate) return;

        // Clear previous errors
        birthDate.classList.remove('error');
        deathDate.classList.remove('error');

        if (birthDate.value && deathDate.value) {
            const birth = new Date(birthDate.value);
            const death = new Date(deathDate.value);
            
            if (birth >= death) {
                deathDate.classList.add('error');
                return false;
            }
        }

        if (birthDate.value) {
            const birth = new Date(birthDate.value);
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            
            if (birth > today) {
                birthDate.classList.add('error');
                return false;
            }
        }

        return true;
    }

    function handleInputChange() {
        clearTimeout(autoSaveTimer);
        updateAutoSaveStatus('saving');
        
        autoSaveTimer = setTimeout(() => {
            saveFormData();
            updateAutoSaveStatus('saved');
        }, 1000);
    }

    function updateCharacterCount() {
        if (!bioTextarea || !bioCharCount) return;
        
        const count = bioTextarea.value.length;
        const maxCount = 2000;
        
        bioCharCount.textContent = count;
        
        // Update color based on count
        const parent = bioCharCount.parentElement;
        if (count > maxCount) {
            parent.style.color = '#DC143C';
            bioCharCount.style.fontWeight = 'bold';
        } else if (count > maxCount * 0.8) {
            parent.style.color = '#FF8C00';
            bioCharCount.style.fontWeight = '600';
        } else {
            parent.style.color = '#5D4E37';
            bioCharCount.style.fontWeight = 'normal';
        }
    }

    function setupFileUpload() {
        if (!uploadArea || !photoFile) return;

        // Click to upload
        uploadArea.addEventListener('click', (e) => {
            if (e.target.classList.contains('remove-image')) return;
            photoFile.click();
        });

        // File selection
        photoFile.addEventListener('change', handleFileSelection);

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
            
            const files = e.dataTransfer.files;
            if (files.length > 0) {
                handleFileSelection({ target: { files: files } });
            }
        });
    }

    function handleFileSelection(e) {
        const file = e.target.files[0];
        if (!file) return;

        // Validate file type
        if (!file.type.startsWith('image/')) {
            showError('Per favore seleziona un file immagine valido (JPG, PNG, GIF).');
            return;
        }

        // Validate file size (5MB max)
        if (file.size > 5 * 1024 * 1024) {
            showError('Il file è troppo grande. Massimo 5MB consentiti.');
            return;
        }

        // Preview image
        const reader = new FileReader();
        reader.onload = function(e) {
            previewImg.src = e.target.result;
            imagePreview.style.display = 'block';
            uploadArea.style.display = 'none';
        };
        reader.readAsDataURL(file);

        updatePreview();
    }

    function removeImage() {
        photoFile.value = '';
        imagePreview.style.display = 'none';
        uploadArea.style.display = 'block';
        
        // Reset preview
        const previewAuthorImage = document.getElementById('previewAuthorImage');
        if (previewAuthorImage) {
            previewAuthorImage.src = '/immagini/default-author.jpg';
        }
    }

    // Make removeImage function global
    window.removeImage = removeImage;

    function nextStep() {
        if (!validateStep(currentStep)) {
            return;
        }

        if (currentStep < totalSteps) {
            // Hide current step
            const currentStepElement = formSteps[currentStep - 1];
            const currentStepIndicator = steps[currentStep - 1];
            
            currentStepElement.classList.remove('active');
            currentStepIndicator.classList.remove('active');
            currentStepIndicator.classList.add('completed');

            currentStep++;

            // Show next step
            const nextStepElement = formSteps[currentStep - 1];
            const nextStepIndicator = steps[currentStep - 1];
            
            nextStepElement.classList.add('active');
            nextStepIndicator.classList.add('active');

            updateProgressBar();
            updateNavigationButtons();
            updatePreview();

            // Smooth scroll to top of form
            nextStepElement.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    }

    function previousStep() {
        if (currentStep > 1) {
            // Hide current step
            formSteps[currentStep - 1].classList.remove('active');
            steps[currentStep - 1].classList.remove('active');

            currentStep--;

            // Show previous step
            formSteps[currentStep - 1].classList.add('active');
            steps[currentStep - 1].classList.add('active');
            steps[currentStep - 1].classList.remove('completed');

            updateProgressBar();
            updateNavigationButtons();

            // Smooth scroll to top of form
            document.querySelector('.form-step.active').scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    }

    function updateProgressBar() {
        const progress = ((currentStep - 1) / (totalSteps - 1)) * 100;
        progressLine.style.width = progress + '%';
    }

    function updateNavigationButtons() {
        if (!prevBtn || !nextBtn) return;

        // Update previous button
        prevBtn.style.display = currentStep > 1 ? 'flex' : 'none';

        // Update next button
        if (currentStep === totalSteps) {
            nextBtn.style.display = 'none';
        } else {
            nextBtn.style.display = 'flex';
        }
    }

    function validateStep(step) {
        let isValid = true;
        let firstError = null;
        const currentFormStep = formSteps[step - 1];
        
        if (!currentFormStep) {
            return false;
        }
        
        const requiredInputs = currentFormStep.querySelectorAll('[required]');

        // Clear previous errors
        currentFormStep.querySelectorAll('.error').forEach(input => {
            input.classList.remove('error');
        });

        requiredInputs.forEach((input, index) => {
            if (!input.value.trim()) {
                isValid = false;
                input.classList.add('error');
                if (!firstError) { // Only store first error
                    firstError = input;
                }
            }
        });

        // Validate dates on step 1
        if (step === 1) {
            const birthDate = document.getElementById('birthDate');
            const deathDate = document.getElementById('deathDate');
            
            if (birthDate && deathDate && birthDate.value && deathDate.value) {
                const birth = new Date(birthDate.value);
                const death = new Date(deathDate.value);
                
                if (birth >= death) {
                    isValid = false;
                    deathDate.classList.add('error');
                    if (!firstError) {
                        firstError = deathDate;
                    }
                }
            }

            // Validate that birth date is not in the future
            if (birthDate && birthDate.value) {
                const birth = new Date(birthDate.value);
                const today = new Date();
                today.setHours(0, 0, 0, 0);
                
                if (birth > today) {
                    isValid = false;
                    birthDate.classList.add('error');
                    if (!firstError) {
                        firstError = birthDate;
                    }
                }
            }
        }

        // Show error and focus first error field
        if (!isValid && firstError) {
            firstError.focus();
            const label = firstError.previousElementSibling;
            const fieldName = label ? label.textContent.replace('*', '').trim() : 'Campo';
            
            if (firstError.id === 'deathDate' && firstError.classList.contains('error')) {
                showError('La data di morte deve essere successiva alla data di nascita.');
            } else if (firstError.id === 'birthDate' && firstError.classList.contains('error')) {
                showError('La data di nascita non può essere nel futuro.');
            } else {
                showError(`Il campo "${fieldName}" è obbligatorio.`);
            }
        }

        return isValid;
    }

    function updatePreview() {
        // Update author name
        const firstName = document.getElementById('firstName').value;
        const lastName = document.getElementById('lastName').value;
        const fullName = `${firstName} ${lastName}`.trim() || 'Nome Autore';
        
        const previewName = document.getElementById('previewName');
        if (previewName) previewName.textContent = fullName;

        // Update dates
        const birthDate = document.getElementById('birthDate').value;
        const deathDate = document.getElementById('deathDate').value;
        
        let dateString = '';
        if (birthDate) {
            const birth = new Date(birthDate).toLocaleDateString('it-IT');
            dateString = `${birth}`;
            if (deathDate) {
                const death = new Date(deathDate).toLocaleDateString('it-IT');
                dateString += ` - ${death}`;
            } else {
                dateString += ' - presente';
            }
        }
        
        const previewBirthDate = document.getElementById('previewBirthDate');
        const previewDeathDate = document.getElementById('previewDeathDate');
        if (previewBirthDate) previewBirthDate.textContent = dateString;

        // Update nationality
        const nationality = document.getElementById('nationality').value;
        const previewNationality = document.getElementById('previewNationality');
        if (previewNationality) {
            previewNationality.textContent = nationality || 'Nazionalità non specificata';
        }

        // Update biography
        const biography = document.getElementById('biography').value;
        const previewBiography = document.getElementById('previewBiography');
        if (previewBiography) {
            previewBiography.textContent = biography || 'Nessuna biografia inserita';
        }

        // Update image if file is selected
        if (photoFile.files.length > 0) {
            const previewAuthorImage = document.getElementById('previewAuthorImage');
            if (previewAuthorImage && previewImg.src) {
                previewAuthorImage.src = previewImg.src;
            }
        }
    }

    function handleFormSubmit(e) {
        if (!validateStep(currentStep)) {
            e.preventDefault();
            return false;
        }

        // Show loading state
        const submitBtn = document.getElementById('submitBtn');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creazione...';
        }

        // Clear saved form data on successful submission
        localStorage.removeItem('authorCreateData');
        
        return true;
    }

    function showError(message) {
        const errorBanner = document.getElementById('errorBanner');
        const errorMessage = document.getElementById('errorMessage');
        
        if (errorBanner && errorMessage) {
            errorMessage.textContent = message;
            errorBanner.style.display = 'flex';
            
            // Auto-hide after 5 seconds
            setTimeout(() => {
                hideError();
            }, 5000);
            
            // Scroll to error
            errorBanner.scrollIntoView({
                behavior: 'smooth',
                block: 'center'
            });
        }
    }

    function showSuccess(message) {
        const successBanner = document.getElementById('successBanner');
        const successMessage = document.getElementById('successMessage');
        
        if (successBanner && successMessage) {
            successMessage.textContent = message;
            successBanner.style.display = 'flex';
            
            // Auto-hide after 3 seconds
            setTimeout(() => {
                successBanner.style.display = 'none';
            }, 3000);
        }
    }

    function hideError() {
        const errorBanner = document.getElementById('errorBanner');
        if (errorBanner) {
            errorBanner.style.display = 'none';
        }
    }

    // Advanced features
    function setupAdvancedFeatures() {
        // Keyboard shortcuts
        document.addEventListener('keydown', function(e) {
            if (e.ctrlKey || e.metaKey) {
                switch (e.key) {
                    case 's':
                        e.preventDefault();
                        saveFormData();
                        updateAutoSaveStatus('saved');
                        showSuccess('Bozza salvata');
                        break;
                    case 'Enter':
                        if (currentStep < totalSteps) {
                            e.preventDefault();
                            nextStep();
                        }
                        break;
                }
            }

            // ESC to go back
            if (e.key === 'Escape' && currentStep > 1) {
                previousStep();
            }
        });

        // Form field animations
        const inputs = form.querySelectorAll('.form-input, .form-textarea, .form-select');
        inputs.forEach(input => {
            input.addEventListener('focus', function() {
                this.parentElement.classList.add('focused');
            });
            
            input.addEventListener('blur', function() {
                this.parentElement.classList.remove('focused');
                if (!this.value) {
                    this.parentElement.classList.remove('filled');
                } else {
                    this.parentElement.classList.add('filled');
                }
            });

            // Check if field is pre-filled
            if (input.value) {
                input.parentElement.classList.add('filled');
            }
        });
    }

    // Initialize advanced features
    setupAdvancedFeatures();

    // Cleanup on page unload
    window.addEventListener('beforeunload', function(e) {
        // Save form data before leaving
        saveFormData();
        
        // Show confirmation if form has unsaved changes
        const inputs = form.querySelectorAll('input, textarea, select');
        let hasChanges = false;
        
        inputs.forEach(input => {
            if (input.value && input.type !== 'hidden') {
                hasChanges = true;
            }
        });

        if (hasChanges) {
            e.preventDefault();
            e.returnValue = 'Hai modifiche non salvate. Sei sicuro di voler uscire?';
            return e.returnValue;
        }
    });

    console.log('Author Create page initialized successfully');
});

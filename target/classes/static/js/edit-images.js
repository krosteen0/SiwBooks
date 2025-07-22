function validateImages(input) {
    const files = input.files;
    const imageCount = document.getElementById('imageCount');
    const errorMessage = document.getElementById('errorMessage');
    const submitButton = document.getElementById('submitButton');
    const preview = document.getElementById('newImagePreview');
    
    // Clear previous preview
    preview.innerHTML = '';
    errorMessage.textContent = '';
    
    imageCount.textContent = 'Immagini selezionate: ' + files.length;
    
    if (files.length < 2 || files.length > 10) {
        errorMessage.textContent = 'Seleziona da 2 a 10 immagini.';
        submitButton.disabled = true;
        return;
    }
    
    // Validate file types
    for (let i = 0; i < files.length; i++) {
        if (!files[i].type.startsWith('image/')) {
            errorMessage.textContent = 'Tutti i file devono essere immagini.';
            submitButton.disabled = true;
            return;
        }
    }
    
    // Show preview
    for (let i = 0; i < files.length; i++) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.style.maxWidth = '200px';
            img.style.maxHeight = '200px';
            img.style.margin = '5px';
            preview.appendChild(img);
        }
        reader.readAsDataURL(files[i]);
    }
    
    submitButton.disabled = false;
}

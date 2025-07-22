document.addEventListener('DOMContentLoaded', function() {
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const submitBtn = document.getElementById('submitBtn');
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    
    function validatePassword() {
        const password = passwordInput.value;
        const lengthCheck = document.getElementById('lengthCheck');
        const upperCheck = document.getElementById('upperCheck');
        const numberCheck = document.getElementById('numberCheck');
        
        // Check length
        if (password.length >= 8) {
            lengthCheck.className = 'valid';
            lengthCheck.innerHTML = '<i class="fas fa-check"></i> Almeno 8 caratteri';
        } else {
            lengthCheck.className = 'invalid';
            lengthCheck.innerHTML = '<i class="fas fa-times"></i> Almeno 8 caratteri';
        }
        
        // Check uppercase
        if (/[A-Z]/.test(password)) {
            upperCheck.className = 'valid';
            upperCheck.innerHTML = '<i class="fas fa-check"></i> Almeno una lettera maiuscola';
        } else {
            upperCheck.className = 'invalid';
            upperCheck.innerHTML = '<i class="fas fa-times"></i> Almeno una lettera maiuscola';
        }
        
        // Check number
        if (/[0-9]/.test(password)) {
            numberCheck.className = 'valid';
            numberCheck.innerHTML = '<i class="fas fa-check"></i> Almeno un numero';
        } else {
            numberCheck.className = 'invalid';
            numberCheck.innerHTML = '<i class="fas fa-times"></i> Almeno un numero';
        }
        
        return password.length >= 8 && /[A-Z]/.test(password) && /[0-9]/.test(password);
    }
    
    function validatePasswordMatch() {
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        const matchError = document.getElementById('passwordMatchError');
        
        if (confirmPassword && password !== confirmPassword) {
            matchError.style.display = 'block';
            confirmPasswordInput.classList.add('invalid');
            return false;
        } else if (confirmPassword) {
            matchError.style.display = 'none';
            confirmPasswordInput.classList.remove('invalid');
            confirmPasswordInput.classList.add('valid');
            return true;
        }
        return confirmPassword === '';
    }
    
    function validateUsername() {
        const username = usernameInput.value;
        const isValid = username.length >= 3 && username.length <= 20 && /^[a-zA-Z0-9_]+$/.test(username);
        
        if (username && isValid) {
            usernameInput.classList.remove('invalid');
            usernameInput.classList.add('valid');
        } else if (username) {
            usernameInput.classList.remove('valid');
            usernameInput.classList.add('invalid');
        }
        
        return isValid;
    }
    
    function validateEmail() {
        const email = emailInput.value;
        const isValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
        
        if (email && isValid) {
            emailInput.classList.remove('invalid');
            emailInput.classList.add('valid');
        } else if (email) {
            emailInput.classList.remove('valid');
            emailInput.classList.add('invalid');
        }
        
        return isValid;
    }
    
    function updateSubmitButton() {
        const isPasswordValid = validatePassword();
        const isPasswordMatch = validatePasswordMatch();
        const isUsernameValid = validateUsername();
        const isEmailValid = validateEmail();
        
        submitBtn.disabled = !(isPasswordValid && isPasswordMatch && isUsernameValid && isEmailValid);
    }
    
    // Event listeners
    passwordInput.addEventListener('input', updateSubmitButton);
    confirmPasswordInput.addEventListener('input', updateSubmitButton);
    usernameInput.addEventListener('input', updateSubmitButton);
    emailInput.addEventListener('input', updateSubmitButton);
    
    // Initial validation
    updateSubmitButton();
});

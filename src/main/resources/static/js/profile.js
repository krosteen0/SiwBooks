// Profile page JavaScript functionality
document.addEventListener('DOMContentLoaded', function() {
    // Tab switching functionality
    window.showTab = function(tabName) {
        // Hide all tab contents
        const tabContents = document.querySelectorAll('.tab-content');
        tabContents.forEach(tab => {
            tab.classList.remove('active');
        });
        
        // Remove active class from all menu items
        const menuLinks = document.querySelectorAll('.profile-menu a');
        menuLinks.forEach(link => {
            link.classList.remove('active');
        });
        
        // Show selected tab
        const selectedTab = document.getElementById(tabName);
        if (selectedTab) {
            selectedTab.classList.add('active');
        }
        
        // Add active class to corresponding menu item
        const activeLink = document.querySelector(`.profile-menu a[href="#${tabName}"]`);
        if (activeLink) {
            activeLink.classList.add('active');
        }
    };
    
    // Set default active tab
    showTab('dashboard');
    
    // Handle menu clicks
    const menuLinks = document.querySelectorAll('.profile-menu a[onclick]');
    menuLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const onclick = this.getAttribute('onclick');
            if (onclick && onclick.includes("showTab")) {
                const tabName = onclick.match(/showTab\('([^']+)'\)/);
                if (tabName && tabName[1]) {
                    showTab(tabName[1]);
                }
            }
        });
    });
    
    // Form validation for password change
    const passwordForm = document.querySelector('.security-form');
    if (passwordForm) {
        passwordForm.addEventListener('submit', function(e) {
            const newPassword = this.querySelector('input[name="newPassword"]').value;
            const confirmPassword = this.querySelector('input[name="confirmPassword"]').value;
            
            if (newPassword !== confirmPassword) {
                e.preventDefault();
                alert('Le password non corrispondono!');
                return false;
            }
            
            // Basic password validation
            if (newPassword.length < 8) {
                e.preventDefault();
                alert('La password deve essere di almeno 8 caratteri!');
                return false;
            }
            
            if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(newPassword)) {
                e.preventDefault();
                alert('La password deve contenere almeno una lettera maiuscola, una minuscola e un numero!');
                return false;
            }
        });
    }
    
    // Auto-hide alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-20px)';
            setTimeout(() => {
                alert.style.display = 'none';
            }, 300);
        }, 5000);
    });
    
    // Smooth transitions for alerts
    alerts.forEach(alert => {
        alert.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
    });
});

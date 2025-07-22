package it.uniroma3.siw.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.dto.UsersDTO;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.UsersRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private void addAuthenticationAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        model.addAttribute("isAuthenticated", isAuthenticated);
        if (isAuthenticated && auth != null) {
            model.addAttribute("username", auth.getName());
            Users user = usersRepository.findByUsername(auth.getName()).orElse(null);
            model.addAttribute("isAdmin", user != null && user.hasAdminPrivileges());
            model.addAttribute("isSuperAdmin", user != null && user.isSuperAdmin());
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "accessDenied", required = false) String accessDenied,
                               Model model) {
        addAuthenticationAttributes(model);
        
        if (error != null) {
            model.addAttribute("error", "Username o password non validi");
        }
        if (accessDenied != null) {
            model.addAttribute("accessDenied", "Accesso negato. Effettua il login per continuare.");
        }
        
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        addAuthenticationAttributes(model);
        model.addAttribute("usersDTO", new UsersDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("usersDTO") UsersDTO usersDTO,
                              BindingResult bindingResult, 
                              Model model) {
        addAuthenticationAttributes(model);
        
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // Verifica se l'username esiste già
        if (usersRepository.findByUsername(usersDTO.getUsername()).isPresent()) {
            model.addAttribute("usernameError", "Username già esistente");
            return "register";
        }

        // Verifica se l'email esiste già tramite UsersService
        // Per ora saltiamo questo controllo o implementiamo una query personalizzata
        // if (usersRepository.existsByEmail(usersDTO.getEmail())) {
        //     model.addAttribute("emailError", "Email già registrata");
        //     return "register";
        // }

        // Verifica che le password coincidano
        if (!usersDTO.getPassword().equals(usersDTO.getConfirmPassword())) {
            model.addAttribute("passwordError", "Le password non coincidono");
            return "register";
        }

        try {
            // Crea nuovo utente
            Users newUser = new Users();
            newUser.setUsername(usersDTO.getUsername());
            newUser.setEmail(usersDTO.getEmail());
            newUser.setPassword(passwordEncoder.encode(usersDTO.getPassword()));
            newUser.setRole("USER"); // Ruolo predefinito

            usersRepository.save(newUser);
            logger.info("Nuovo utente registrato: {}", newUser.getUsername());
            
            model.addAttribute("successMessage", "Registrazione completata con successo! Ora puoi effettuare il login.");
            return "login";
            
        } catch (Exception e) {
            logger.error("Errore durante la registrazione dell'utente: {}", e.getMessage(), e);
            model.addAttribute("error", "Errore durante la registrazione. Riprova più tardi.");
            return "register";
        }
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        addAuthenticationAttributes(model);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/users/login";
        }

        Users user = usersRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/users/login";
        }

        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam("username") String newUsername,
                               Model model) {
        addAuthenticationAttributes(model);
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return "redirect:/users/login";
            }

            String currentUsername = auth.getName();
            Users currentUser = usersRepository.findByUsername(currentUsername).orElse(null);
            
            if (currentUser == null) {
                model.addAttribute("profileError", "Utente non trovato");
                model.addAttribute("user", currentUser);
                return "profile";
            }

            // Verifica che il nuovo username non sia già in uso (se diverso dall'attuale)
            if (!currentUsername.equals(newUsername)) {
                if (usersRepository.findByUsername(newUsername).isPresent()) {
                    model.addAttribute("profileError", "Questo nome utente è già in uso");
                    model.addAttribute("user", currentUser);
                    return "profile";
                }
                
                // Aggiorna l'username
                currentUser.setUsername(newUsername);
                usersRepository.save(currentUser);
                
                // Logout per forzare il re-login con il nuovo username
                SecurityContextHolder.clearContext();
                return "redirect:/users/login?profileUpdated=true";
            }

            model.addAttribute("profileSuccess", "Profilo aggiornato con successo");
            model.addAttribute("user", currentUser);
            return "profile";
            
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento del profilo: {}", e.getMessage(), e);
            model.addAttribute("profileError", "Errore durante l'aggiornamento del profilo");
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Users user = usersRepository.findByUsername(auth.getName()).orElse(null);
            model.addAttribute("user", user);
            return "profile";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {
        addAuthenticationAttributes(model);
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return "redirect:/users/login";
            }

            Users user = usersRepository.findByUsername(auth.getName()).orElse(null);
            if (user == null) {
                model.addAttribute("passwordError", "Utente non trovato");
                model.addAttribute("user", user);
                return "profile";
            }

            // Verifica password attuale
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                model.addAttribute("passwordError", "Password attuale non corretta");
                model.addAttribute("user", user);
                return "profile";
            }

            // Verifica che le nuove password corrispondano
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("passwordError", "Le nuove password non corrispondono");
                model.addAttribute("user", user);
                return "profile";
            }

            // Validazione password (minimo 8 caratteri, almeno una maiuscola e un numero)
            if (newPassword.length() < 8 || 
                !newPassword.matches(".*[A-Z].*") || 
                !newPassword.matches(".*[0-9].*")) {
                model.addAttribute("passwordError", "La password deve essere di almeno 8 caratteri e contenere almeno una lettera maiuscola e un numero");
                model.addAttribute("user", user);
                return "profile";
            }

            // Aggiorna la password
            user.setPassword(passwordEncoder.encode(newPassword));
            usersRepository.save(user);
            
            logger.info("Password modificata per l'utente: {}", user.getUsername());
            model.addAttribute("passwordSuccess", "Password modificata con successo");
            model.addAttribute("user", user);
            return "profile";
            
        } catch (Exception e) {
            logger.error("Errore durante il cambio password: {}", e.getMessage(), e);
            model.addAttribute("passwordError", "Errore durante il cambio password");
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Users user = usersRepository.findByUsername(auth.getName()).orElse(null);
            model.addAttribute("user", user);
            return "profile";
        }
    }
}

package it.uniroma3.siw.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.UsersRepository;

@Controller
@RequestMapping("/admin")
public class UserManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
    
    @Autowired
    private UsersRepository usersRepository;
    
    private Users getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            return usersRepository.findByUsername(authentication.getName()).orElse(null);
        }
        return null;
    }
    
    private void addAuthenticationAttributes(Model model) {
        Users user = getAuthenticatedUser();
        model.addAttribute("isAuthenticated", user != null);
        model.addAttribute("currentUser", user);
        if (user != null) {
            model.addAttribute("isAdmin", user.hasAdminPrivileges());
            model.addAttribute("isSuperAdmin", user.isSuperAdmin());
        }
    }
    
    @GetMapping("/users")
    public String listUsers(Model model) {
        Users currentUser = getAuthenticatedUser();
        if (currentUser == null || !currentUser.isSuperAdmin()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo i super-amministratori possono gestire gli utenti.");
            return "error";
        }
        
        addAuthenticationAttributes(model);
        
        List<Users> allUsers = usersRepository.findAll();
        model.addAttribute("users", allUsers);
        model.addAttribute("availableRoles", List.of("USER", "ADMIN", "SUPER_ADMIN"));
        
        // Calcolo statistiche per il template
        long adminCount = allUsers.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long superAdminCount = allUsers.stream().filter(u -> "SUPER_ADMIN".equals(u.getRole())).count();
        long userCount = allUsers.stream().filter(u -> "USER".equals(u.getRole())).count();
        
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("superAdminCount", superAdminCount);
        model.addAttribute("userCount", userCount);
        
        return "admin/user-management";
    }
    
    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable Long id, @RequestParam String newRole, Model model) {
        Users currentUser = getAuthenticatedUser();
        if (currentUser == null || !currentUser.isSuperAdmin()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo i super-amministratori possono modificare i ruoli.");
            return "error";
        }
        
        try {
            Optional<Users> userOpt = usersRepository.findById(id);
            if (userOpt.isPresent()) {
                Users userToUpdate = userOpt.get();
                
                // Impedisce di modificare il proprio ruolo
                if (userToUpdate.getId().equals(currentUser.getId())) {
                    model.addAttribute("errorMessage", "Non puoi modificare il tuo stesso ruolo.");
                    return "redirect:/admin/users?error=self-modify";
                }
                
                // Valida il nuovo ruolo
                if (!List.of("USER", "ADMIN", "SUPER_ADMIN").contains(newRole)) {
                    model.addAttribute("errorMessage", "Ruolo non valido.");
                    return "redirect:/admin/users?error=invalid-role";
                }
                
                String oldRole = userToUpdate.getRole();
                userToUpdate.setRole(newRole);
                usersRepository.save(userToUpdate);
                
                logger.info("Ruolo utente {} cambiato da {} a {} da super-admin {}", 
                           userToUpdate.getUsername(), oldRole, newRole, currentUser.getUsername());
                
                return "redirect:/admin/users?success=role-updated";
            } else {
                model.addAttribute("errorMessage", "Utente non trovato.");
                return "redirect:/admin/users?error=user-not-found";
            }
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento del ruolo utente: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento del ruolo: " + e.getMessage());
            return "redirect:/admin/users?error=update-failed";
        }
    }
    
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, Model model) {
        Users currentUser = getAuthenticatedUser();
        if (currentUser == null || !currentUser.isSuperAdmin()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo i super-amministratori possono eliminare utenti.");
            return "error";
        }
        
        try {
            Optional<Users> userOpt = usersRepository.findById(id);
            if (userOpt.isPresent()) {
                Users userToDelete = userOpt.get();
                
                // Impedisce di eliminare se stesso
                if (userToDelete.getId().equals(currentUser.getId())) {
                    model.addAttribute("errorMessage", "Non puoi eliminare il tuo stesso account.");
                    return "redirect:/admin/users?error=self-delete";
                }
                
                String deletedUsername = userToDelete.getUsername();
                usersRepository.deleteById(id);
                
                logger.info("Utente {} eliminato da super-admin {}", deletedUsername, currentUser.getUsername());
                
                return "redirect:/admin/users?success=user-deleted";
            } else {
                return "redirect:/admin/users?error=user-not-found";
            }
        } catch (Exception e) {
            logger.error("Errore durante l'eliminazione dell'utente: {}", e.getMessage(), e);
            return "redirect:/admin/users?error=delete-failed";
        }
    }
}

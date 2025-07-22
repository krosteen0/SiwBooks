package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.UsersRepository;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private UsersRepository usersRepository;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !(auth.getName() != null && auth.getName().equals("anonymousUser"));
        
        model.addAttribute("isAuthenticated", isAuthenticated);
        
        if (isAuthenticated && auth != null && auth.getName() != null) {
            String username = auth.getName();
            model.addAttribute("username", username);
            
            // Verifica se l'utente è un amministratore
            Users user = usersRepository.findByUsername(username).orElse(null);
            boolean isAdmin = user != null && user.hasAdminPrivileges();
            boolean isSuperAdmin = user != null && user.isSuperAdmin();
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("isSuperAdmin", isSuperAdmin);
        } else {
            model.addAttribute("isAdmin", false);
            model.addAttribute("isSuperAdmin", false);
        }
    }
}

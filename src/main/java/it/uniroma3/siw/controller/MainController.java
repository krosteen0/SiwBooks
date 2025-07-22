package it.uniroma3.siw.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.UsersRepository;
import it.uniroma3.siw.service.AuthorService;
import it.uniroma3.siw.service.BookService;

@Controller
public class MainController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private AuthorService authorService;
    
    @Autowired
    private UsersRepository usersRepository;

    private Users getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return usersRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    private void addAuthenticationAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        
        model.addAttribute("isAuthenticated", isAuthenticated);
        if (isAuthenticated && auth != null) {
            model.addAttribute("username", auth.getName());
            Users user = getAuthenticatedUser();
            if (user != null) {
                model.addAttribute("isAdmin", user.hasAdminPrivileges());
                model.addAttribute("isSuperAdmin", user.isSuperAdmin());
            } else {
                model.addAttribute("isAdmin", false);
                model.addAttribute("isSuperAdmin", false);
            }
        } else {
            model.addAttribute("isAdmin", false);
            model.addAttribute("isSuperAdmin", false);
        }
    }

    @GetMapping("/")
    public String index(Model model) {
        addAuthenticationAttributes(model);
        
        // Ottieni alcuni libri recenti per la homepage
        List<Book> recentBooks = bookService.findAll();
        
        // Limita a 6 libri per la homepage
        if (recentBooks.size() > 6) {
            recentBooks = recentBooks.subList(0, 6);
        }
        
        model.addAttribute("recentBooks", recentBooks);
        model.addAttribute("totalBooks", bookService.countAll());
        model.addAttribute("totalAuthors", authorService.countAll());
        
        // Aggiungi alcuni autori in evidenza (se disponibili)
        try {
            model.addAttribute("featuredAuthors", authorService.findAll().stream().limit(4).toList());
        } catch (Exception e) {
            model.addAttribute("featuredAuthors", List.of());
        }
        
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        addAuthenticationAttributes(model);
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        addAuthenticationAttributes(model);
        return "contact";
    }
    
    @GetMapping("/roles-info")
    public String showRolesInfo(Model model) {
        addAuthenticationAttributes(model);
        return "roles-info";
    }
}

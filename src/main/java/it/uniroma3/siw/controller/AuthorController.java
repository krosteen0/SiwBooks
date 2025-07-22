package it.uniroma3.siw.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.dto.AuthorDTO;
import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.UsersRepository;
import it.uniroma3.siw.service.AuthorService;
import it.uniroma3.siw.service.ReviewService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/author")
public class AuthorController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);
    
    @Autowired
    private AuthorService authorService;
    
    @Autowired
    private ReviewService reviewService;
    
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
            model.addAttribute("isAdmin", user != null && user.hasAdminPrivileges());
        }
    }

    @GetMapping
    public String showAllAuthors(Model model) {
        addAuthenticationAttributes(model);
        List<Author> authors = authorService.findAll();
        model.addAttribute("authors", authors);
        model.addAttribute("nationalities", authorService.findAllNationalities());
        return "authors";
    }

    @GetMapping("/details/{id}")
    public String showAuthorDetails(@PathVariable Long id, Model model) {
        try {
            addAuthenticationAttributes(model);
            Author author = authorService.findByIdWithBooks(id);
            if (author == null) {
                model.addAttribute("errorMessage", "Autore non trovato.");
                return "error";
            }
            
            // Calcola il rating medio dell'autore basato sui suoi libri
            Double averageRating = reviewService.findAverageRatingByAuthorId(id);
            Integer totalReviews = reviewService.countReviewsByAuthorId(id);
            
            model.addAttribute("author", author);
            model.addAttribute("averageRating", averageRating);
            model.addAttribute("totalReviews", totalReviews);
            return "author-details";
        } catch (Exception e) {
            logger.error("Error showing author details: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore nel caricamento dei dettagli dell'autore.");
            return "error";
        }
    }

    @GetMapping("/create")
    public String showCreateAuthorPage(Model model) {
        addAuthenticationAttributes(model);
        
        Users user = getAuthenticatedUser();
        if (user == null || !user.hasAdminPrivileges()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono creare autori.");
            return "error";
        }
        
        model.addAttribute("authorDTO", new AuthorDTO());
        model.addAttribute("nationalities", authorService.findAllNationalities());
        return "author-create";
    }

    @PostMapping("/create")
    public String createAuthor(@Valid @ModelAttribute AuthorDTO authorDTO, BindingResult result, Model model,
                             @RequestParam(value = "photoFile", required = false) MultipartFile photoFile) {
        addAuthenticationAttributes(model);
        
        Users user = getAuthenticatedUser();
        if (user == null || !user.hasAdminPrivileges()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono creare autori.");
            return "error";
        }
        
        if (result.hasErrors()) {
            model.addAttribute("nationalities", authorService.findAllNationalities());
            return "author-create";
        }
        
        try {
            Author savedAuthor = authorService.save(authorDTO, photoFile);
            return "redirect:/author/details/" + savedAuthor.getId();
        } catch (Exception e) {
            logger.error("Error creating author: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante la creazione dell'autore: " + e.getMessage());
            model.addAttribute("nationalities", authorService.findAllNationalities());
            return "author-create";
        }
    }

    
    @GetMapping("/edit/{id}")
    public String showEditAuthorPage(@PathVariable Long id, Model model) {
        addAuthenticationAttributes(model);
        
        Users user = getAuthenticatedUser();
        if (user == null || !user.hasAdminPrivileges()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono modificare autori.");
            return "error";
        }
        
        Optional<Author> authorOpt = authorService.findById(id);
        if (authorOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Autore non trovato.");
            return "error";
        }
        
        Author author = authorOpt.get();
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setId(author.getId());
        authorDTO.setFirstName(author.getFirstName());
        authorDTO.setLastName(author.getLastName());
        authorDTO.setBirthDate(author.getBirthDate());
        authorDTO.setDeathDate(author.getDeathDate());
        authorDTO.setNationality(author.getNationality());
        authorDTO.setBiography(author.getBiography());
        
        model.addAttribute("authorDTO", authorDTO);
        model.addAttribute("author", author);
        model.addAttribute("nationalities", authorService.findAllNationalities());
        return "author-edit";
    }

    @PostMapping("/edit/{id}")
    public String editAuthor(@PathVariable Long id, @Valid @ModelAttribute AuthorDTO authorDTO, 
                           BindingResult result, Model model,
                           @RequestParam(value = "photoFile", required = false) MultipartFile photoFile) {
        addAuthenticationAttributes(model);
        
        Users user = getAuthenticatedUser();
        if (user == null || !user.hasAdminPrivileges()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono modificare autori.");
            return "error";
        }
        
        // Debug logging
        logger.info("Received AuthorDTO: {}", authorDTO);
        logger.info("Validation errors: {}", result.hasErrors());
        if (result.hasErrors()) {
            logger.info("Validation error details:");
            result.getAllErrors().forEach(error -> logger.info("Error: {}", error));
        }
        
        if (result.hasErrors()) {
            Optional<Author> authorOpt = authorService.findById(id);
            if (authorOpt.isPresent()) {
                model.addAttribute("author", authorOpt.get());
            }
            model.addAttribute("nationalities", authorService.findAllNationalities());
            return "author-edit";
        }
        
        try {
            Author updatedAuthor = authorService.update(id, authorDTO, photoFile);
            return "redirect:/author/details/" + updatedAuthor.getId();
        } catch (Exception e) {
            logger.error("Error updating author: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento dell'autore: " + e.getMessage());
            Optional<Author> authorOpt = authorService.findById(id);
            if (authorOpt.isPresent()) {
                model.addAttribute("author", authorOpt.get());
            }
            model.addAttribute("nationalities", authorService.findAllNationalities());
            return "author-edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable Long id, Model model) {
        Users user = getAuthenticatedUser();
        if (user == null || !user.hasAdminPrivileges()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono eliminare autori.");
            return "error";
        }
        
        try {
            authorService.deleteById(id);
            return "redirect:/author";
        } catch (Exception e) {
            logger.error("Error deleting author: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'eliminazione dell'autore: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/search")
    public String searchAuthors(@RequestParam(required = false) String firstName,
                              @RequestParam(required = false) String lastName,
                              @RequestParam(required = false) String nationality,
                              Model model) {
        addAuthenticationAttributes(model);
        
        List<Author> authors;
        
        if (firstName != null && !firstName.trim().isEmpty()) {
            authors = authorService.findByFirstNameContaining(firstName.trim());
        } else if (lastName != null && !lastName.trim().isEmpty()) {
            authors = authorService.findByLastNameContaining(lastName.trim());
        } else if (nationality != null && !nationality.trim().isEmpty()) {
            authors = authorService.findByNationality(nationality.trim());
        } else {
            authors = authorService.findAll();
        }
        
        model.addAttribute("authors", authors);
        model.addAttribute("searchFirstName", firstName);
        model.addAttribute("searchLastName", lastName);
        model.addAttribute("searchNationality", nationality);
        model.addAttribute("nationalities", authorService.findAllNationalities());
        
        return "authors";
    }
    
    @GetMapping("/photo/{id}")
    public ResponseEntity<byte[]> getAuthorPhoto(@PathVariable Long id) {
        try {
            Optional<Author> authorOpt = authorService.findById(id);
            if (authorOpt.isEmpty() || authorOpt.get().getPhoto() == null) {
                return ResponseEntity.notFound().build();
            }
            
            Author author = authorOpt.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                author.getPhotoContentType() != null ? author.getPhotoContentType() : "image/jpeg"
            ));
            headers.setContentLength(author.getPhoto().length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(author.getPhoto());
        } catch (Exception e) {
            logger.error("Error serving author photo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

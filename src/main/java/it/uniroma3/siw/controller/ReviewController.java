package it.uniroma3.siw.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import it.uniroma3.siw.dto.ReviewDTO;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.BookRepository;
import it.uniroma3.siw.repository.UsersRepository;
import it.uniroma3.siw.service.ReviewService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private BookRepository bookRepository;
    
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

    @GetMapping("/book/{bookId}")
    @Transactional(readOnly = true)
    public String showBookReviews(@PathVariable Long bookId, Model model) {
        try {
            addAuthenticationAttributes(model);
            
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (bookOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Libro non trovato.");
                return "error";
            }
            
            Book book = bookOpt.get();
            List<Review> reviews = reviewService.findByBookId(bookId);
            Double averageRating = reviewService.findAverageRatingByBookId(bookId);
            Integer reviewCount = reviewService.countRatingsByBookId(bookId);
            
            // Controlla se l'utente ha già inserito una recensione
            Users authenticatedUser = getAuthenticatedUser();
            boolean hasUserReviewed = false;
            Review userReview = null;
            boolean canWriteReview = false;
            
            if (authenticatedUser != null) {
                // Gli utenti registrati (non amministratori) possono scrivere recensioni
                canWriteReview = !authenticatedUser.hasAdminPrivileges();
                
                Optional<Review> userReviewOpt = reviewService.findByBookIdAndUserId(bookId, authenticatedUser.getId());
                hasUserReviewed = userReviewOpt.isPresent();
                userReview = userReviewOpt.orElse(null);
            }
            
            model.addAttribute("book", book);
            model.addAttribute("reviews", reviews);
            model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
            model.addAttribute("reviewCount", reviewCount != null ? reviewCount : 0);
            model.addAttribute("hasUserReviewed", hasUserReviewed);
            model.addAttribute("userReview", userReview);
            model.addAttribute("canWriteReview", canWriteReview);
            
            // Inizializza il DTO con bookId preimpostato
            ReviewDTO reviewDTO = new ReviewDTO();
            reviewDTO.setBookId(bookId);
            if (userReview != null) {
                reviewDTO.setTitle(userReview.getTitle());
                reviewDTO.setRating(userReview.getRating());
                reviewDTO.setText(userReview.getText());
            }
            model.addAttribute("reviewDTO", reviewDTO);
            
            return "book-reviews";
        } catch (Exception e) {
            logger.error("Error showing book reviews: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore nel caricamento delle recensioni: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/submit")
    public String submitReview(@Valid ReviewDTO reviewDTO, BindingResult bindingResult, Model model,
                              @RequestParam(value = "reviewId", required = false) Long reviewId,
                              @RequestParam(value = "isEditing", required = false, defaultValue = "false") boolean isEditing) {
        addAuthenticationAttributes(model);
        
        // Verifica se l'utente è autenticato
        Users authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            model.addAttribute("errorMessage", "Devi essere autenticato per scrivere una recensione.");
            return "error";
        }
        
        // Gli amministratori non possono scrivere recensioni
        if (authenticatedUser.hasAdminPrivileges()) {
            model.addAttribute("errorMessage", "Gli amministratori non possono scrivere recensioni.");
            return "error";
        }
        
        // Se siamo in modalità editing, imposta l'ID nel DTO
        if (isEditing && reviewId != null) {
            reviewDTO.setId(reviewId);
            logger.info("Editing review ID: {} for book ID: {} with rating: {}", reviewId, reviewDTO.getBookId(), reviewDTO.getRating());
        } else {
            logger.info("Creating new review for book ID: {} with rating: {}", reviewDTO.getBookId(), reviewDTO.getRating());
        }
        
        // Verifica se il libro esiste
        Optional<Book> bookOpt = bookRepository.findById(reviewDTO.getBookId());
        if (bookOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Libro non trovato.");
            return "error";
        }
        
        Book book = bookOpt.get();
        
        if (bindingResult.hasErrors()) {
            logger.warn("Binding errors detected: {}", bindingResult.getAllErrors());
            model.addAttribute("book", book);
            model.addAttribute("reviews", reviewService.findByBookId(book.getId()));
            model.addAttribute("averageRating", reviewService.findAverageRatingByBookId(book.getId()));
            model.addAttribute("reviewCount", reviewService.countRatingsByBookId(book.getId()));
            model.addAttribute("hasUserReviewed", reviewService.hasUserReviewedBook(book, authenticatedUser));
            return "book-reviews";
        }
        
        try {
            // Salva la recensione - questa operazione è transazionale nel service
            Review savedReview = reviewService.saveReview(reviewDTO, authenticatedUser);
            logger.info("Review saved successfully with ID: {}", savedReview.getId());
            
            // Redirect alla pagina delle recensioni per vedere immediatamente il risultato
            return "redirect:/reviews/book/" + book.getId();
        } catch (Exception e) {
            logger.error("Error submitting review: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'invio della recensione: " + e.getMessage());
            model.addAttribute("book", book);
            return "error";
        }
    }

    @DeleteMapping("/{reviewId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        try {
            Users authenticatedUser = getAuthenticatedUser();
            if (authenticatedUser == null) {
                return ResponseEntity.status(401).body("Devi essere autenticato per eliminare una recensione.");
            }
            
            reviewService.deleteReview(reviewId, authenticatedUser);
            return ResponseEntity.ok("Recensione eliminata con successo.");
        } catch (Exception e) {
            logger.error("Error deleting review: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Errore durante l'eliminazione della recensione: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public String showAllReviews(Model model) {
        addAuthenticationAttributes(model);
        List<Review> reviews = reviewService.findAllOrderByCreatedAtDesc();
        model.addAttribute("reviews", reviews);
        return "all-reviews";
    }
}

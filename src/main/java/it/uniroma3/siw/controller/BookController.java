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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.dto.BookDTO;
import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.BookImage;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.UsersRepository;
import it.uniroma3.siw.service.AuthorService;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.ReviewService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/book")
public class BookController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    
    @Autowired
    private BookService bookService;
    
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

    @GetMapping
    public String showAllBooks(Model model) {
        addAuthenticationAttributes(model);
        List<Book> books = bookService.findAll();
        model.addAttribute("books", books);
        return "books";
    }

    @GetMapping("/details/{id}")
    @Transactional(readOnly = true)
    public String showBookDetails(@PathVariable Long id, Model model) {
        try {
            addAuthenticationAttributes(model);
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Libro non trovato.");
                return "error";
            }
            
            Book book = bookOpt.get();
            model.addAttribute("book", book);
            
            // Informazioni sulle recensioni
            Double avgRating = reviewService.findAverageRatingByBookId(id);
            Integer reviewCount = reviewService.countRatingsByBookId(id);
            
            model.addAttribute("averageRating", avgRating);
            model.addAttribute("reviewCount", reviewCount);
            
            // Verifica se l'utente ha già recensito questo libro
            Users authenticatedUser = getAuthenticatedUser();
            if (authenticatedUser != null) {
                boolean hasUserReviewed = reviewService.hasUserReviewedBook(book, authenticatedUser);
                model.addAttribute("hasUserReviewed", hasUserReviewed);
                // Gli utenti registrati (non amministratori) possono scrivere recensioni
                model.addAttribute("canWriteReview", !authenticatedUser.hasAdminPrivileges());
            } else {
                model.addAttribute("canWriteReview", false);
            }
            
            return "book-details";
        } catch (Exception e) {
            logger.error("Error showing book details: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore nel caricamento dei dettagli del libro.");
            return "error";
        }
    }

    @GetMapping("/create")
    public String showCreateBookPage(Model model) {
        addAuthenticationAttributes(model);
        
        Users user = getAuthenticatedUser();
        if (user == null || !user.hasAdminPrivileges()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono creare libri.");
            return "error";
        }
        
        model.addAttribute("bookDTO", new BookDTO());
        model.addAttribute("authors", authorService.findAll());
        return "book-create";
    }

    @PostMapping("/create")
    public String createBook(@Valid @ModelAttribute BookDTO bookDTO, BindingResult result, Model model,
                           @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        addAuthenticationAttributes(model);
        
        logger.info("Creating book with title: {}", bookDTO.getTitle());
        logger.info("Received {} image files", images != null ? images.size() : 0);
        
        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                logger.info("Image {}: name={}, size={}, type={}, empty={}", 
                    i+1, image.getOriginalFilename(), image.getSize(), image.getContentType(), image.isEmpty());
            }
        }
        
        Users user = getAuthenticatedUser();
        if (user == null || !user.hasAdminPrivileges()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono creare libri.");
            return "error";
        }
        
        if (result.hasErrors()) {
            model.addAttribute("authors", authorService.findAll());
            return "book-create";
        }
        
        // Custom validation for publication year
        String publicationYearError = validatePublicationYear(bookDTO);
        if (publicationYearError != null) {
            model.addAttribute("errorMessage", publicationYearError);
            model.addAttribute("authors", authorService.findAll());
            return "book-create";
        }
        
        try {
            Book savedBook = bookService.save(bookDTO);
            logger.info("Book saved with ID: {}", savedBook.getId());
            
            // Gestione delle immagini
            if (images != null && !images.isEmpty()) {
                logger.info("Processing {} images", images.size());
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        BookImage bookImage = new BookImage();
                        bookImage.setImageData(image.getBytes());
                        bookImage.setContentType(image.getContentType());
                        bookImage.setBook(savedBook);
                        savedBook.getImages().add(bookImage);
                        logger.info("Added image: {} ({})", image.getOriginalFilename(), image.getContentType());
                    }
                }
                // IMPORTANTE: Salva di nuovo il libro con le immagini
                savedBook = bookService.saveBook(savedBook);
                logger.info("Book saved with images. Total images: {}", savedBook.getImages().size());
            } else {
                logger.warn("No images received or all images are empty");
            }
            
            return "redirect:/book/details/" + savedBook.getId();
        } catch (Exception e) {
            logger.error("Error creating book: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante la creazione del libro: " + e.getMessage());
            model.addAttribute("authors", authorService.findAll());
            return "book-create";
        }
    }
    
    /**
     * Validates the publication year against the author's life dates
     * @param bookDTO The book data to validate
     * @return Error message if validation fails, null if validation passes
     */
    private String validatePublicationYear(BookDTO bookDTO) {
        if (bookDTO.getPublicationYear() == null || bookDTO.getAuthorId() == null) {
            return null; // No validation needed if year or author is not provided
        }
        
        int publicationYear = bookDTO.getPublicationYear();
        
        // Check if year is not greater than 2025
        if (publicationYear > 2025) {
            return "L'anno di pubblicazione non può essere successivo al 2025";
        }
        
        // Get author information
        Optional<Author> authorOptional = authorService.findById(bookDTO.getAuthorId());
        if (!authorOptional.isPresent()) {
            return "Autore non trovato";
        }
        
        Author author = authorOptional.get();
        
        // Check against author's birth year
        if (author.getBirthDate() != null) {
            int birthYear = author.getBirthDate().getYear();
            if (publicationYear < birthYear) {
                return String.format("L'anno di pubblicazione (%d) non può essere precedente alla nascita dell'autore (%d)", 
                    publicationYear, birthYear);
            }
        }
        
        // Check against author's death year (if applicable)
        if (author.getDeathDate() != null) {
            int deathYear = author.getDeathDate().getYear();
            if (publicationYear > deathYear) {
                return String.format("L'anno di pubblicazione (%d) non può essere successivo alla morte dell'autore (%d)", 
                    publicationYear, deathYear);
            }
        }
        
        return null; // Validation passed
    }

    @GetMapping("/edit/{id}")
    public String showEditBookPage(@PathVariable Long id, Model model) {
        try {
            addAuthenticationAttributes(model);
            
            Users user = getAuthenticatedUser();
            if (user == null) {
                logger.warn("Tentativo di accesso alla modifica libro senza autenticazione per ID: {}", id);
                return "redirect:/users/login?accessDenied=true";
            }
            
            if (!user.hasAdminPrivileges()) {
                logger.warn("Tentativo di accesso alla modifica libro senza privilegi admin da utente: {} per libro ID: {}", user.getUsername(), id);
                model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono modificare libri.");
                return "error";
            }
            
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isEmpty()) {
                logger.warn("Tentativo di modifica libro con ID inesistente: {}", id);
                model.addAttribute("errorMessage", "Libro non trovato.");
                return "error";
            }
            
            Book book = bookOpt.get();
            logger.info("Accesso alla pagina di modifica libro: {} (ID: {}) da utente: {}", book.getTitle(), id, user.getUsername());
            model.addAttribute("book", book);
            return "book-edit";
            
        } catch (Exception e) {
            logger.error("Errore durante l'accesso alla pagina di modifica libro con ID: {}", id, e);
            model.addAttribute("errorMessage", "Si è verificato un errore durante il caricamento della pagina di modifica: " + e.getMessage());
            return "error";
        }
    }

    // Pagina per modificare solo i dettagli del libro
    @GetMapping("/edit/{id}/details")
    public String showEditBookDetailsPage(@PathVariable Long id, Model model) {
        try {
            addAuthenticationAttributes(model);
            
            Users user = getAuthenticatedUser();
            if (user == null) {
                logger.warn("Tentativo di accesso alla modifica dettagli libro senza autenticazione per ID: {}", id);
                return "redirect:/users/login?accessDenied=true";
            }
            
            if (!user.hasAdminPrivileges()) {
                logger.warn("Tentativo di accesso alla modifica dettagli libro senza privilegi admin da utente: {} per libro ID: {}", user.getUsername(), id);
                model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono modificare libri.");
                return "error";
            }
            
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isEmpty()) {
                logger.warn("Tentativo di modifica dettagli libro con ID inesistente: {}", id);
                model.addAttribute("errorMessage", "Libro non trovato.");
                return "error";
            }
            
            Book book = bookOpt.get();
            BookDTO bookDTO = new BookDTO();
            bookDTO.setId(book.getId());
            bookDTO.setTitle(book.getTitle());
            bookDTO.setPublicationYear(book.getPublicationYear());
            bookDTO.setDescription(book.getDescription());
            
            // Imposta l'authorId se il libro ha almeno un autore
            if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
                Author firstAuthor = book.getAuthors().iterator().next();
                bookDTO.setAuthorId(firstAuthor.getId());
            }
            
            logger.info("Accesso alla pagina di modifica dettagli libro: {} (ID: {}) da utente: {}", book.getTitle(), id, user.getUsername());
            model.addAttribute("bookDTO", bookDTO);
            model.addAttribute("book", book);
            model.addAttribute("authors", authorService.findAll());
            return "book-edit-details";
            
        } catch (Exception e) {
            logger.error("Errore durante l'accesso alla pagina di modifica dettagli libro con ID: {}", id, e);
            model.addAttribute("errorMessage", "Si è verificato un errore durante il caricamento della pagina di modifica dettagli: " + e.getMessage());
            return "error";
        }
    }

    // Salva le modifiche ai dettagli del libro
    @PostMapping("/edit/{id}/details")
    public String editBookDetails(@PathVariable Long id, @Valid @ModelAttribute BookDTO bookDTO, 
                                BindingResult result, Model model) {
        addAuthenticationAttributes(model);
        
        logger.info("Attempting to edit book details with ID: {}", id);
        logger.info("BookDTO data: title={}, authorId={}, publicationYear={}", 
                   bookDTO.getTitle(), bookDTO.getAuthorId(), bookDTO.getPublicationYear());
        
        Users user = getAuthenticatedUser();
        if (user == null || !user.hasAdminPrivileges()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono modificare libri.");
            return "error";
        }
        
        if (result.hasErrors()) {
            logger.warn("Validation errors in book details edit: {}", result.getAllErrors());
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isPresent()) {
                model.addAttribute("book", bookOpt.get());
            }
            model.addAttribute("authors", authorService.findAll());
            return "book-edit-details";
        }
        
        // Custom validation for publication year
        String publicationYearError = validatePublicationYear(bookDTO);
        if (publicationYearError != null) {
            model.addAttribute("errorMessage", publicationYearError);
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isPresent()) {
                model.addAttribute("book", bookOpt.get());
            }
            model.addAttribute("authors", authorService.findAll());
            return "book-edit-details";
        }
        
        try {
            Book updatedBook = bookService.update(id, bookDTO);
            logger.info("Book details updated successfully with ID: {}", updatedBook.getId());
            return "redirect:/book/edit/" + updatedBook.getId() + "?success=update";
        } catch (Exception e) {
            logger.error("Error updating book details: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento del libro: " + e.getMessage());
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isPresent()) {
                model.addAttribute("book", bookOpt.get());
            }
            model.addAttribute("authors", authorService.findAll());
            return "book-edit-details";
        }
    }

    // Pagina per modificare solo le immagini del libro
    @GetMapping("/edit/{id}/images")
    public String showEditBookImagesPage(@PathVariable Long id, Model model) {
        try {
            addAuthenticationAttributes(model);
            
            Users user = getAuthenticatedUser();
            if (user == null) {
                logger.warn("Tentativo di accesso alla modifica immagini libro senza autenticazione per ID: {}", id);
                return "redirect:/users/login?accessDenied=true";
            }
            
            if (!user.hasAdminPrivileges()) {
                logger.warn("Tentativo di accesso alla modifica immagini libro senza privilegi admin da utente: {} per libro ID: {}", user.getUsername(), id);
                model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono modificare libri.");
                return "error";
            }
            
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isEmpty()) {
                logger.warn("Tentativo di modifica immagini libro con ID inesistente: {}", id);
                model.addAttribute("errorMessage", "Libro non trovato.");
                return "error";
            }
            
            Book book = bookOpt.get();
            logger.info("Accesso alla pagina di modifica immagini libro: {} (ID: {}) da utente: {}", book.getTitle(), id, user.getUsername());
            model.addAttribute("book", book);
            return "book-edit-images";
            
        } catch (Exception e) {
            logger.error("Errore durante l'accesso alla pagina di modifica immagini libro con ID: {}", id, e);
            model.addAttribute("errorMessage", "Si è verificato un errore durante il caricamento della pagina di modifica immagini: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteBook(@PathVariable Long id, Model model) {
        Users user = getAuthenticatedUser();
        if (user == null || !user.hasAdminPrivileges()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono eliminare libri.");
            return "error";
        }
        
        try {
            bookService.deleteById(id);
            return "redirect:/book";
        } catch (Exception e) {
            logger.error("Error deleting book: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'eliminazione del libro: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/search")
    public String searchBooks(@RequestParam(required = false) String title,
                            @RequestParam(required = false) String author,
                            @RequestParam(required = false) Integer year,
                            Model model) {
        addAuthenticationAttributes(model);
        
        List<Book> books;
        
        // Se tutti i parametri sono vuoti, mostra tutti i libri
        if ((title == null || title.trim().isEmpty()) && 
            (author == null || author.trim().isEmpty()) && 
            year == null) {
            books = bookService.findAll();
        } else {
            // Usa la ricerca combinata per permettere filtri multipli
            String titleParam = (title != null && !title.trim().isEmpty()) ? title.trim() : null;
            String authorParam = (author != null && !author.trim().isEmpty()) ? author.trim() : null;
            books = bookService.searchBooks(titleParam, authorParam, year);
        }
        
        model.addAttribute("books", books);
        model.addAttribute("searchTitle", title);
        model.addAttribute("searchAuthor", author);
        model.addAttribute("searchYear", year);
        
        return "books";
    }
    
    @GetMapping("/image/{imageId}")
    public ResponseEntity<byte[]> getBookImage(@PathVariable Long imageId) {
        try {
            Optional<BookImage> imageOpt = bookService.findImageById(imageId);
            if (imageOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            BookImage image = imageOpt.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(image.getContentType()));
            headers.setContentLength(image.getImageData().length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(image.getImageData());
        } catch (Exception e) {
            logger.error("Error serving book image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/edit/{id}/images")
    public String uploadBookImagesEdit(@PathVariable Long id, 
                                      @RequestParam("imageFiles") List<MultipartFile> imageFiles,
                                      Model model) {
        try {
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Libro non trovato");
                return "error";
            }
            
            Book book = bookOpt.get();
            
            // Controllo se l'utente ha i permessi per modificare il libro
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !hasEditPermission(auth)) {
                model.addAttribute("errorMessage", "Non hai i permessi per modificare questo libro");
                return "error";
            }
            
            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (MultipartFile imageFile : imageFiles) {
                    if (!imageFile.isEmpty()) {
                        // Validazione del file
                        if (imageFile.getSize() > 5 * 1024 * 1024) { // 5MB
                            model.addAttribute("errorMessage", "File troppo grande: " + imageFile.getOriginalFilename());
                            return "redirect:/book/edit/" + id + "/images?error=filesize";
                        }
                        
                        String contentType = imageFile.getContentType();
                        if (contentType == null || (!contentType.startsWith("image/"))) {
                            model.addAttribute("errorMessage", "Formato file non supportato: " + imageFile.getOriginalFilename());
                            return "redirect:/book/edit/" + id + "/images?error=filetype";
                        }
                        
                        BookImage bookImage = new BookImage();
                        bookImage.setImageData(imageFile.getBytes());
                        bookImage.setContentType(imageFile.getContentType());
                        bookImage.setBook(book);
                        book.getImages().add(bookImage);
                    }
                }
                
                bookService.saveBook(book);
                logger.info("Immagini caricate con successo per il libro con ID: {}", id);
            }
            
            return "redirect:/book/edit/" + id + "/images?success=upload";
            
        } catch (Exception e) {
            logger.error("Errore durante il caricamento delle immagini per il libro {}: {}", id, e.getMessage(), e);
            return "redirect:/book/edit/" + id + "/images?error=upload";
        }
    }

    @PostMapping("/{id}/images/upload")
    public String uploadBookImages(@PathVariable Long id, 
                                   @RequestParam("imageFiles") List<MultipartFile> imageFiles,
                                   Model model) {
        try {
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Libro non trovato");
                return "error";
            }
            
            Book book = bookOpt.get();
            
            // Controllo se l'utente ha i permessi per modificare il libro
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !hasEditPermission(auth)) {
                model.addAttribute("errorMessage", "Non hai i permessi per modificare questo libro");
                return "error";
            }
            
            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (MultipartFile imageFile : imageFiles) {
                    if (!imageFile.isEmpty()) {
                        // Validazione del file
                        if (imageFile.getSize() > 5 * 1024 * 1024) { // 5MB
                            model.addAttribute("errorMessage", "File troppo grande: " + imageFile.getOriginalFilename());
                            return "redirect:/book/edit/" + id + "?error=filesize";
                        }
                        
                        String contentType = imageFile.getContentType();
                        if (contentType == null || (!contentType.startsWith("image/"))) {
                            model.addAttribute("errorMessage", "Formato file non supportato: " + imageFile.getOriginalFilename());
                            return "redirect:/book/edit/" + id + "?error=filetype";
                        }
                        
                        BookImage bookImage = new BookImage();
                        bookImage.setImageData(imageFile.getBytes());
                        bookImage.setContentType(imageFile.getContentType());
                        bookImage.setBook(book);
                        book.getImages().add(bookImage);
                    }
                }
                
                bookService.saveBook(book);
                logger.info("Immagini caricate con successo per il libro con ID: {}", id);
            }
            
            return "redirect:/book/edit/" + id + "?success=upload";
            
        } catch (Exception e) {
            logger.error("Errore durante il caricamento delle immagini per il libro {}: {}", id, e.getMessage(), e);
            return "redirect:/book/edit/" + id + "?error=upload";
        }
    }
    
    @PostMapping("/edit/{bookId}/images/delete/{imageId}")
    public String deleteBookImageEdit(@PathVariable Long bookId, 
                                     @PathVariable Long imageId,
                                     Model model) {
        try {
            Optional<Book> bookOpt = bookService.findById(bookId);
            if (bookOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Libro non trovato");
                return "error";
            }
            
            // Controllo se l'utente ha i permessi per modificare il libro
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !hasEditPermission(auth)) {
                model.addAttribute("errorMessage", "Non hai i permessi per modificare questo libro");
                return "error";
            }
            
            Optional<BookImage> imageOpt = bookService.findImageById(imageId);
            if (imageOpt.isEmpty()) {
                return "redirect:/book/edit/" + bookId + "/images?error=imagenotfound";
            }
            
            BookImage image = imageOpt.get();
            
            // Verifica che l'immagine appartenga al libro corretto
            if (!image.getBook().getId().equals(bookId)) {
                return "redirect:/book/edit/" + bookId + "/images?error=unauthorized";
            }
            
            bookService.deleteImage(imageId);
            logger.info("Immagine eliminata con successo per il libro con ID: {}", bookId);
            
            return "redirect:/book/edit/" + bookId + "/images?success=delete";
            
        } catch (Exception e) {
            logger.error("Errore durante l'eliminazione dell'immagine {} per il libro {}: {}", imageId, bookId, e.getMessage(), e);
            return "redirect:/book/edit/" + bookId + "/images?error=delete";
        }
    }

    @PostMapping("/{bookId}/image/{imageId}/delete")
    public String deleteBookImage(@PathVariable Long bookId, 
                                  @PathVariable Long imageId,
                                  Model model) {
        try {
            Optional<Book> bookOpt = bookService.findById(bookId);
            if (bookOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Libro non trovato");
                return "error";
            }
            
            // Controllo se l'utente ha i permessi per modificare il libro
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !hasEditPermission(auth)) {
                model.addAttribute("errorMessage", "Non hai i permessi per modificare questo libro");
                return "error";
            }
            
            Optional<BookImage> imageOpt = bookService.findImageById(imageId);
            if (imageOpt.isEmpty()) {
                return "redirect:/book/edit/" + bookId + "?error=imagenotfound";
            }
            
            BookImage image = imageOpt.get();
            
            // Verifica che l'immagine appartenga al libro corretto
            if (!image.getBook().getId().equals(bookId)) {
                return "redirect:/book/edit/" + bookId + "?error=unauthorized";
            }
            
            bookService.deleteImage(imageId);
            logger.info("Immagine eliminata con successo per il libro con ID: {}", bookId);
            
            return "redirect:/book/edit/" + bookId + "?success=delete";
            
        } catch (Exception e) {
            logger.error("Errore durante l'eliminazione dell'immagine {} per il libro {}: {}", imageId, bookId, e.getMessage(), e);
            return "redirect:/book/edit/" + bookId + "?error=delete";
        }
    }
    
    @GetMapping("/cover/{id}")
    public ResponseEntity<byte[]> getBookCover(@PathVariable Long id) {
        try {
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isEmpty() || bookOpt.get().getCoverImage() == null) {
                return ResponseEntity.notFound().build();
            }
            
            Book book = bookOpt.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                book.getCoverImageContentType() != null ? book.getCoverImageContentType() : "image/jpeg"
            ));
            headers.setContentLength(book.getCoverImage().length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(book.getCoverImage());
        } catch (Exception e) {
            logger.error("Error serving book cover: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private boolean hasEditPermission(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN") || 
                                     authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}

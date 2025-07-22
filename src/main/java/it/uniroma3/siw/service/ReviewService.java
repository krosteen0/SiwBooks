package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.dto.ReviewDTO;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.BookRepository;
import it.uniroma3.siw.repository.ReviewRepository;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private BookRepository bookRepository;

    @Transactional(readOnly = true)
    public List<Review> findByBookId(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public Double findAverageRatingByBookId(Long bookId) {
        Double avgRating = reviewRepository.findAverageRatingByBookId(bookId);
        return avgRating != null ? avgRating : 0.0;
    }

    @Transactional(readOnly = true)
    public Integer countRatingsByBookId(Long bookId) {
        return reviewRepository.countByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public Optional<Review> findByBookIdAndUserId(Long bookId, Long userId) {
        return reviewRepository.findByBookIdAndUserId(bookId, userId);
    }

    @Transactional(readOnly = true)
    public boolean hasUserReviewedBook(Book book, Users user) {
        return reviewRepository.existsByBookAndUser(book, user);
    }

    @Transactional
    public Review saveReview(ReviewDTO reviewDTO, Users user) {
        if (reviewDTO == null || reviewDTO.getBookId() == null || user == null) {
            throw new IllegalArgumentException("I dati della recensione o dell'utente sono nulli");
        }

        Optional<Book> bookOpt = bookRepository.findById(reviewDTO.getBookId());
        if (bookOpt.isEmpty()) {
            throw new IllegalArgumentException("Libro non trovato con ID: " + reviewDTO.getBookId());
        }

        Book book = bookOpt.get();
        Review review;

        // Se è fornito un ID specifico, cerca di aggiornare quella recensione
        if (reviewDTO.getId() != null) {
            Optional<Review> existingReviewOpt = reviewRepository.findById(reviewDTO.getId());
            if (existingReviewOpt.isPresent()) {
                review = existingReviewOpt.get();
                // Verifica che la recensione appartenga all'utente corrente
                if (!review.getUser().getId().equals(user.getId())) {
                    throw new IllegalArgumentException("Non puoi modificare una recensione di un altro utente");
                }
                // Aggiorna la recensione esistente
                review.setTitle(reviewDTO.getTitle());
                review.setRating(reviewDTO.getRating());
                review.setText(reviewDTO.getText());
            } else {
                throw new IllegalArgumentException("Recensione non trovata con ID: " + reviewDTO.getId());
            }
        } else {
            // Caso normale: verifica se l'utente ha già recensito questo libro
            Optional<Review> existingReviewOpt = reviewRepository.findByBookAndUser(book, user);
            
            if (existingReviewOpt.isPresent()) {
                // Aggiorna la recensione esistente
                review = existingReviewOpt.get();
                review.setTitle(reviewDTO.getTitle());
                review.setRating(reviewDTO.getRating());
                review.setText(reviewDTO.getText());
            } else {
                // Crea una nuova recensione
                review = new Review();
                review.setBook(book);
                review.setUser(user);
                review.setTitle(reviewDTO.getTitle());
                review.setRating(reviewDTO.getRating());
                review.setText(reviewDTO.getText());
            }
        }

        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Users user) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();
            // Verifica che l'utente sia il proprietario della recensione o sia admin
            // Gli amministratori possono cancellare qualsiasi recensione (ma non possono modificarle)
            if (review.getUser().getId().equals(user.getId()) || user.isAdmin()) {
                reviewRepository.delete(review);
            } else {
                throw new IllegalArgumentException("Non hai i permessi per eliminare questa recensione");
            }
        } else {
            throw new IllegalArgumentException("Recensione non trovata");
        }
    }

    @Transactional(readOnly = true)
    public List<Review> findAllOrderByCreatedAtDesc() {
        return reviewRepository.findAllOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Review> findByUser(Users user) {
        return reviewRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public Double findAverageRatingByAuthorId(Long authorId) {
        Double avgRating = reviewRepository.findAverageRatingByAuthorId(authorId);
        return avgRating != null ? avgRating : 0.0;
    }

    @Transactional(readOnly = true)
    public Integer countReviewsByAuthorId(Long authorId) {
        return reviewRepository.countReviewsByAuthorId(authorId);
    }
}

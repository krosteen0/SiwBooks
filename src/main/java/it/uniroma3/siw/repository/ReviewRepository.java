package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.Users;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByBook(Book book);
    
    List<Review> findByUser(Users user);
    
    @Query("SELECT r FROM Review r WHERE r.book.id = :bookId")
    List<Review> findByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double findAverageRatingByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId")
    Integer countByBookId(@Param("bookId") Long bookId);
    
    Optional<Review> findByBookAndUser(Book book, Users user);
    
    @Query("SELECT r FROM Review r WHERE r.book.id = :bookId AND r.user.id = :userId")
    Optional<Review> findByBookIdAndUserId(@Param("bookId") Long bookId, @Param("userId") Long userId);
    
    boolean existsByBookAndUser(Book book, Users user);
    
    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    List<Review> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.book b JOIN b.authors a WHERE a.id = :authorId")
    Double findAverageRatingByAuthorId(@Param("authorId") Long authorId);
    
    @Query("SELECT COUNT(r) FROM Review r JOIN r.book b JOIN b.authors a WHERE a.id = :authorId")
    Integer countReviewsByAuthorId(@Param("authorId") Long authorId);
}

package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
    
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    List<Book> findByPublicationYear(Integer year);
    
    @Query("SELECT b FROM Book b JOIN FETCH b.images WHERE b.id = :id")
    Optional<Book> findByIdWithImages(@Param("id") Long id);
    
    @Query("SELECT b FROM Book b JOIN FETCH b.authors WHERE b.id = :id")
    Optional<Book> findByIdWithAuthors(@Param("id") Long id);
    
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.images LEFT JOIN FETCH b.authors WHERE b.id = :id")
    Optional<Book> findByIdWithAllData(@Param("id") Long id);
    
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.images LEFT JOIN FETCH b.authors")
    List<Book> findAllWithAuthorsAndImages();
    
    List<Book> findByAuthorsContaining(Author author);
    
    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE LOWER(a.lastName) LIKE LOWER(CONCAT('%', :authorName, '%')) OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    List<Book> findByAuthorNameContaining(@Param("authorName") String authorName);
    
    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE " +
           "(:title IS NULL OR :title = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:authorName IS NULL OR :authorName = '' OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :authorName, '%')) OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :authorName, '%'))) AND " +
           "(:year IS NULL OR b.publicationYear = :year)")
    List<Book> searchBooksWithFilters(@Param("title") String title, @Param("authorName") String authorName, @Param("year") Integer year);
    
    @Query("SELECT COUNT(b) FROM Book b")
    Long countAllBooks();
}

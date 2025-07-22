package it.uniroma3.siw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    
    List<Author> findByLastNameContainingIgnoreCase(String lastName);
    
    List<Author> findByFirstNameContainingIgnoreCase(String firstName);
    
    @Query("SELECT a FROM Author a WHERE a.firstName LIKE %:firstName% AND a.lastName LIKE %:lastName%")
    List<Author> findByFullNameContaining(@Param("firstName") String firstName, @Param("lastName") String lastName);
    
    List<Author> findByNationalityIgnoreCase(String nationality);
    
    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.id = :id")
    Author findByIdWithBooks(@Param("id") Long id);
    
    @Query("SELECT COUNT(a) FROM Author a")
    Long countAllAuthors();
    
    @Query("SELECT DISTINCT a.nationality FROM Author a ORDER BY a.nationality")
    List<String> findAllNationalities();
}

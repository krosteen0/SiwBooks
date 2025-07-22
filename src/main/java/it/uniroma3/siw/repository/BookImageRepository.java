package it.uniroma3.siw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.BookImage;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {
    
    List<BookImage> findByBook(Book book);
    
    void deleteByBook(Book book);
}

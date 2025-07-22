package it.uniroma3.siw.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.dto.BookDTO;
import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.BookImage;
import it.uniroma3.siw.repository.AuthorRepository;
import it.uniroma3.siw.repository.BookImageRepository;
import it.uniroma3.siw.repository.BookRepository;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private AuthorRepository authorRepository;
    
    @Autowired
    private BookImageRepository bookImageRepository;

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAllWithAuthorsAndImages();
    }

    @Transactional(readOnly = true)
    public Optional<Book> findById(Long id) {
        return bookRepository.findByIdWithAllData(id);
    }

    @Transactional(readOnly = true)
    public Optional<Book> findByIdWithImages(Long id) {
        return bookRepository.findByIdWithImages(id);
    }

    @Transactional(readOnly = true)
    public List<Book> findByTitleContaining(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    @Transactional(readOnly = true)
    public List<Book> findByAuthorName(String authorName) {
        return bookRepository.findByAuthorNameContaining(authorName);
    }

    @Transactional(readOnly = true)
    public List<Book> searchBooks(String title, String authorName, Integer year) {
        return bookRepository.searchBooksWithFilters(title, authorName, year);
    }

    @Transactional(readOnly = true)
    public List<Book> findByPublicationYear(Integer year) {
        return bookRepository.findByPublicationYear(year);
    }

    @Transactional
    public Book save(BookDTO bookDTO) {
        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setPublicationYear(bookDTO.getPublicationYear());
        book.setDescription(bookDTO.getDescription());
        
        // Gestione degli autori
        if (bookDTO.getAuthorIds() != null && !bookDTO.getAuthorIds().isEmpty()) {
            List<Author> authors = authorRepository.findAllById(bookDTO.getAuthorIds());
            book.setAuthors(new HashSet<>(authors));
        }
        
        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Long id, BookDTO bookDTO) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setTitle(bookDTO.getTitle());
            book.setPublicationYear(bookDTO.getPublicationYear());
            book.setDescription(bookDTO.getDescription());
            
            // Aggiorna gli autori - gestisce sia authorId che authorIds
            if (bookDTO.getAuthorIds() != null && !bookDTO.getAuthorIds().isEmpty()) {
                List<Author> authors = authorRepository.findAllById(bookDTO.getAuthorIds());
                book.setAuthors(new HashSet<>(authors));
            } else if (bookDTO.getAuthorId() != null) {
                // Backward compatibility for single author
                Optional<Author> authorOpt = authorRepository.findById(bookDTO.getAuthorId());
                if (authorOpt.isPresent()) {
                    HashSet<Author> authors = new HashSet<>();
                    authors.add(authorOpt.get());
                    book.setAuthors(authors);
                }
            }
            
            return bookRepository.save(book);
        }
        throw new IllegalArgumentException("Libro non trovato con ID: " + id);
    }

    @Transactional
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Long countAll() {
        return bookRepository.countAllBooks();
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return bookRepository.existsById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<BookImage> findImageById(Long imageId) {
        return bookImageRepository.findById(imageId);
    }
    
    @Transactional
    public void deleteImage(Long imageId) {
        Optional<BookImage> imageOpt = bookImageRepository.findById(imageId);
        if (imageOpt.isPresent()) {
            BookImage image = imageOpt.get();
            Book book = image.getBook();
            
            // Rimuovi l'immagine dalla collezione del libro
            if (book != null && book.getImages() != null) {
                book.getImages().remove(image);
                // Salva il libro per aggiornare la relazione
                bookRepository.save(book);
            }
            
            // Elimina l'immagine dal database
            bookImageRepository.deleteById(imageId);
        }
    }
    
    @Transactional
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }
}

package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.dto.AuthorDTO;
import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.repository.AuthorRepository;

@Service
public class AuthorService {
    
    @Autowired
    private AuthorRepository authorRepository;

    @Transactional(readOnly = true)
    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Author> findById(Long id) {
        return authorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Author findByIdWithBooks(Long id) {
        return authorRepository.findByIdWithBooks(id);
    }

    @Transactional(readOnly = true)
    public List<Author> findByLastNameContaining(String lastName) {
        return authorRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    @Transactional(readOnly = true)
    public List<Author> findByFirstNameContaining(String firstName) {
        return authorRepository.findByFirstNameContainingIgnoreCase(firstName);
    }

    @Transactional(readOnly = true)
    public List<Author> findByNationality(String nationality) {
        return authorRepository.findByNationalityIgnoreCase(nationality);
    }

    @Transactional(readOnly = true)
    public List<String> findAllNationalities() {
        return authorRepository.findAllNationalities();
    }

    @Transactional
    public Author save(AuthorDTO authorDTO, MultipartFile photoFile) {
        Author author = new Author();
        author.setFirstName(authorDTO.getFirstName());
        author.setLastName(authorDTO.getLastName());
        author.setBirthDate(authorDTO.getBirthDate());
        author.setDeathDate(authorDTO.getDeathDate());
        author.setNationality(authorDTO.getNationality());
        author.setBiography(authorDTO.getBiography());
        
        // Gestione della foto se presente
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                author.setPhoto(photoFile.getBytes());
                author.setPhotoContentType(photoFile.getContentType());
            } catch (Exception e) {
                throw new RuntimeException("Errore durante il caricamento della foto", e);
            }
        }
        
        return authorRepository.save(author);
    }

    @Transactional
    public Author update(Long id, AuthorDTO authorDTO, MultipartFile photoFile) {
        Optional<Author> authorOpt = authorRepository.findById(id);
        if (authorOpt.isPresent()) {
            Author author = authorOpt.get();
            author.setFirstName(authorDTO.getFirstName());
            author.setLastName(authorDTO.getLastName());
            author.setBirthDate(authorDTO.getBirthDate());
            author.setDeathDate(authorDTO.getDeathDate());
            author.setNationality(authorDTO.getNationality());
            author.setBiography(authorDTO.getBiography());
            
            // Aggiorna la foto solo se è stata fornita una nuova foto
            if (photoFile != null && !photoFile.isEmpty()) {
                try {
                    author.setPhoto(photoFile.getBytes());
                    author.setPhotoContentType(photoFile.getContentType());
                } catch (Exception e) {
                    throw new RuntimeException("Errore durante il caricamento della foto", e);
                }
            }
            
            return authorRepository.save(author);
        }
        throw new IllegalArgumentException("Autore non trovato con ID: " + id);
    }

    @Transactional
    public void deleteById(Long id) {
        authorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Long countAll() {
        return authorRepository.countAllAuthors();
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return authorRepository.existsById(id);
    }
}

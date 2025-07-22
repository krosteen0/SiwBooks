package it.uniroma3.siw.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Il nome è obbligatorio")
    private String firstName;

    @NotBlank(message = "Il cognome è obbligatorio")
    private String lastName;

    @NotNull(message = "La data di nascita è obbligatoria")
    private LocalDate birthDate;

    private LocalDate deathDate;

    @NotBlank(message = "La nazionalità è obbligatoria")
    private String nationality;
    
    private String biography; // Biografia dell'autore

    @Lob
    private byte[] photo; // Fotografia dell'autore
    private String photoContentType;

    @ManyToMany(mappedBy = "authors")
    private List<Book> books;

    public Author() {
        this.books = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    public LocalDate getDeathDate() { return deathDate; }
    public void setDeathDate(LocalDate deathDate) { this.deathDate = deathDate; }
    
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    
    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
    
    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }
    
    public String getPhotoContentType() { return photoContentType; }
    public void setPhotoContentType(String photoContentType) { this.photoContentType = photoContentType; }
    
    public List<Book> getBooks() { return books; }
    public void setBooks(List<Book> books) { this.books = books != null ? books : new ArrayList<>(); }

    // Metodi utility
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAlive() {
        return deathDate == null;
    }

    public Integer getAge() {
        if (birthDate == null) return null;
        LocalDate endDate = deathDate != null ? deathDate : LocalDate.now();
        return endDate.getYear() - birthDate.getYear();
    }
}

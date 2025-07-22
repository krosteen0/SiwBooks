package it.uniroma3.siw.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Il titolo è obbligatorio")
    private String title;

    @NotNull(message = "L'anno di pubblicazione è obbligatorio")
    private Integer publicationYear;

    private String description;

    // Copertina principale del libro
    @Lob
    private byte[] coverImage;
    private String coverImageContentType;

    @ManyToMany
    @JoinTable(
        name = "book_author",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookImage> images;
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    public Book() {
        this.authors = new HashSet<>();
        this.images = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public byte[] getCoverImage() { return coverImage; }
    public void setCoverImage(byte[] coverImage) { this.coverImage = coverImage; }
    
    public String getCoverImageContentType() { return coverImageContentType; }
    public void setCoverImageContentType(String coverImageContentType) { this.coverImageContentType = coverImageContentType; }
    
    public Set<Author> getAuthors() { return authors; }
    public void setAuthors(Set<Author> authors) { this.authors = authors != null ? authors : new HashSet<>(); }
    
    public List<BookImage> getImages() { return images; }
    public void setImages(List<BookImage> images) { this.images = images != null ? images : new ArrayList<>(); }
    
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews != null ? reviews : new ArrayList<>(); }
    
    // Metodi utility per le recensioni
    public Double getAverageRating() {
        if (this.reviews == null || this.reviews.isEmpty()) {
            return 0.0;
        }
        return this.reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
    
    public Integer getReviewCount() {
        return this.reviews != null ? this.reviews.size() : 0;
    }

    // Metodo utility per ottenere gli autori come stringa
    public String getAuthorsAsString() {
        if (authors == null || authors.isEmpty()) {
            return "Autore sconosciuto";
        }
        return authors.stream()
                .map(author -> author.getFirstName() + " " + author.getLastName())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Autore sconosciuto");
    }
}

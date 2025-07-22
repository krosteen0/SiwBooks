package it.uniroma3.siw.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReviewDTO {
    
    private Long id;
    
    @NotBlank(message = "Il titolo della recensione è obbligatorio")
    private String title;
    
    @NotNull(message = "Il voto è obbligatorio")
    @Min(value = 1, message = "Il voto minimo è 1")
    @Max(value = 5, message = "Il voto massimo è 5")
    private Integer rating;
    
    @NotBlank(message = "Il testo della recensione è obbligatorio")
    private String text;
    
    private Long bookId;

    // Constructors
    public ReviewDTO() {}

    public ReviewDTO(String title, Integer rating, String text, Long bookId) {
        this.title = title;
        this.rating = rating;
        this.text = text;
        this.bookId = bookId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
}

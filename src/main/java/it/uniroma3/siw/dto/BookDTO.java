package it.uniroma3.siw.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class BookDTO {
    
    private Long id;
    
    @NotBlank(message = "Il titolo è obbligatorio")
    private String title;
    
    @Min(value = 1000, message = "L'anno di pubblicazione deve essere almeno 1000")
    private Integer publicationYear;
    
    private String description;
    
    // Backward compatibility for single author selection
    private Long authorId;
    
    private List<Long> authorIds;

    // Constructors
    public BookDTO() {}

    public BookDTO(String title, Integer publicationYear, String description) {
        this.title = title;
        this.publicationYear = publicationYear;
        this.description = description;
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
    
    // Single author for backward compatibility
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { 
        this.authorId = authorId;
        // Also set in authorIds list for consistency
        if (authorId != null) {
            this.authorIds = List.of(authorId);
        }
    }
    
    public List<Long> getAuthorIds() { return authorIds; }
    public void setAuthorIds(List<Long> authorIds) { this.authorIds = authorIds; }
}

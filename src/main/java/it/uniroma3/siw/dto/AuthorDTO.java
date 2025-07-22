package it.uniroma3.siw.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

public class AuthorDTO {
    
    private Long id;
    
    @NotBlank(message = "Il nome è obbligatorio")
    private String firstName;
    
    @NotBlank(message = "Il cognome è obbligatorio")
    private String lastName;
    
    @NotNull(message = "La data di nascita è obbligatoria")
    @Past(message = "La data di nascita deve essere nel passato")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deathDate;
    
    @NotBlank(message = "La nazionalità è obbligatoria")
    private String nationality;
    
    private String biography;

    // Constructors
    public AuthorDTO() {}

    public AuthorDTO(String firstName, String lastName, LocalDate birthDate, String nationality) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.nationality = nationality;
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

    // Metodi utility
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @Override
    public String toString() {
        return "AuthorDTO{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                ", deathDate=" + deathDate +
                ", nationality='" + nationality + '\'' +
                ", biography='" + (biography != null ? biography.substring(0, Math.min(50, biography.length())) + "..." : null) + '\'' +
                '}';
    }
}

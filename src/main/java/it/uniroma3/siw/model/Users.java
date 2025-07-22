package it.uniroma3.siw.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Users implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "L'username è obbligatorio")
    private String username;

    @NotBlank(message = "La password è obbligatoria")
    private String password;

    @NotBlank(message = "L'email è obbligatoria")
    private String email;

    /**
     * Ruolo dell'utente nel sistema:
     * - USER: Utente registrato che può scrivere recensioni (una per libro)
     * - ADMIN: Amministratore che può gestire libri, autori e cancellare recensioni
     * - SUPER_ADMIN: Super-amministratore che può gestire libri, autori, recensioni e ruoli degli utenti
     * Gli utenti occasionali (non registrati) possono solo consultare le informazioni
     */
    private String role = "USER"; // Default role (USER, ADMIN, or SUPER_ADMIN)

    @OneToMany(mappedBy = "user")
    private List<Review> reviews;

    public Users() {
        this.reviews = new ArrayList<>();
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }

    // Metodi utility
    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isSuperAdmin() { return "SUPER_ADMIN".equals(role); }
    public boolean hasAdminPrivileges() { return isAdmin() || isSuperAdmin(); }
}
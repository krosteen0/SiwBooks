package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.dto.UsersDTO;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.UsersRepository;

@Service
public class UsersService implements UserDetailsService {
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Users saveUser(UsersDTO usersDTO) {
        Users user = new Users();
        user.setUsername(usersDTO.getUsername());
        user.setEmail(usersDTO.getEmail());
        user.setPassword(passwordEncoder.encode(usersDTO.getPassword()));
        user.setRole("USER");
        return usersRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + username));
    }
    
    /**
     * Trova un utente dal suo username
     * @param username username dell'utente da cercare
     * @return l'oggetto Users o null se non trovato
     */
    public Users findByUsername(String username) {
        return usersRepository.findByUsername(username).orElse(null);
    }
    
    /**
     * Cambia la password di un utente verificando prima quella attuale
     * @param username Username dell'utente
     * @param currentPassword Password attuale
     * @param newPassword Nuova password
     * @return true se la password è stata cambiata con successo, false altrimenti
     */
    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        Users user = findByUsername(username);
        if (user == null) {
            return false;
        }
        
        // Verifica che la password attuale sia corretta
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        
        // Imposta la nuova password
        user.setPassword(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
        
        return true;
    }
    
    /**
     * Aggiorna i dati del profilo di un utente
     * @param username Username dell'utente da aggiornare
     * @param updatedUser Oggetto con i nuovi dati dell'utente
     * @return true se l'aggiornamento è avvenuto con successo, false altrimenti
     */
    @Transactional
    public boolean updateUserProfile(String username, Users updatedUser) {
        Users existingUser = findByUsername(username);
        if (existingUser == null) {
            return false;
        }
        
        // Aggiorna solo i campi modificabili
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        
        // Se si volessero aggiungere altri campi al profilo utente (nome, cognome, telefono, ecc.)
        // si potrebbero aggiornare qui
        
        usersRepository.save(existingUser);
        return true;
    }
    
    /**
     * Aggiorna l'username di un utente
     * @param currentUsername Username attuale dell'utente
     * @param newUsername Nuovo username
     * @return true se l'aggiornamento è avvenuto con successo, false altrimenti
     */
    @Transactional
    public boolean updateUsername(String currentUsername, String newUsername) {
        Users existingUser = findByUsername(currentUsername);
        if (existingUser == null) {
            return false;
        }
        
        // Verifica che il nuovo username non sia già in uso
        if (!currentUsername.equals(newUsername) && findByUsername(newUsername) != null) {
            return false;
        }
        
        // Aggiorna l'username
        existingUser.setUsername(newUsername);
        usersRepository.save(existingUser);
        
        return true;
    }
}
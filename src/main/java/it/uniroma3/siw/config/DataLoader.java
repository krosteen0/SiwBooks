package it.uniroma3.siw.config;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.AuthorRepository;
import it.uniroma3.siw.repository.BookRepository;
import it.uniroma3.siw.repository.UsersRepository;

/**
 * Classe per inizializzare il database con dati di esempio
 * Viene eseguita all'avvio dell'applicazione
 */
@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private AuthorRepository authorRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Controlla se il database è già popolato
        if (authorRepository.count() > 0 || bookRepository.count() > 0) {
            System.out.println("Database già popolato. Salto l'inizializzazione.");
            return;
        }
        
        System.out.println("Inizializzazione del database con dati di esempio...");
        
        // Crea un utente amministratore di default se non esiste
        createDefaultAdmin();
        
        // Crea autori italiani classici
        Author eco = createAuthor("Umberto", "Eco", LocalDate.of(1932, 1, 5), LocalDate.of(2016, 2, 19), "Italiana", 
            "Umberto Eco è stato uno scrittore, filosofo, semiologo e accademico italiano. Professore universitario e saggista prolifico, è divenuto famoso in tutto il mondo per i suoi romanzi, a cominciare da 'Il nome della rosa'.");
        Author calvino = createAuthor("Italo", "Calvino", LocalDate.of(1923, 10, 15), LocalDate.of(1985, 9, 19), "Italiana",
            "Italo Calvino è stato uno scrittore e partigiano italiano. È considerato uno dei maggiori scrittori italiani del XX secolo per la sua prosa elegante e la sua capacità di unire fantasia e rigore intellettuale.");
        Author ferrante = createAuthor("Elena", "Ferrante", LocalDate.of(1943, 4, 1), null, "Italiana",
            "Elena Ferrante è lo pseudonimo di una scrittrice italiana la cui identità è tuttora sconosciuta. È diventata famosa internazionalmente per la tetralogia 'L'amica geniale'.");
        Author manzoni = createAuthor("Alessandro", "Manzoni", LocalDate.of(1785, 3, 7), LocalDate.of(1873, 5, 22), "Italiana",
            "Alessandro Manzoni è stato uno scrittore, poeta e drammaturgo italiano. È considerato uno dei maggiori romanzieri italiani di tutti i tempi per il suo capolavoro 'I promessi sposi'.");
        Author dante = createAuthor("Dante", "Alighieri", LocalDate.of(1265, 1, 1), LocalDate.of(1321, 9, 14), "Italiana",
            "Dante Alighieri è stato un poeta, scrittore e politico italiano. È considerato il padre della lingua italiana e il più grande poeta della letteratura italiana. Autore della Divina Commedia.");
        
        // Autori italiani contemporanei
        Author baricco = createAuthor("Alessandro", "Baricco", LocalDate.of(1958, 1, 25), null, "Italiana",
            "Alessandro Baricco è uno scrittore, saggista e musicologo italiano. Noto per il suo stile narrativo elegante e per romanzi come 'Seta' e 'Oceano Mare'.");
        Author tamaro = createAuthor("Susanna", "Tamaro", LocalDate.of(1957, 12, 12), null, "Italiana",
            "Susanna Tamaro è una scrittrice italiana. Il suo romanzo 'Va' dove ti porta il cuore' è uno dei maggiori successi editoriali italiani degli anni Novanta.");
        Author giordano = createAuthor("Paolo", "Giordano", LocalDate.of(1982, 12, 19), null, "Italiana",
            "Paolo Giordano è un fisico e scrittore italiano. Ha vinto il Premio Strega nel 2008 con 'La solitudine dei numeri primi', il più giovane vincitore nella storia del premio.");
        Author saviano = createAuthor("Roberto", "Saviano", LocalDate.of(1979, 9, 22), null, "Italiana",
            "Roberto Saviano è uno scrittore e giornalista italiano. È diventato famoso con il libro-inchiesta 'Gomorra' sulla camorra napoletana.");
        Author camilleri = createAuthor("Andrea", "Camilleri", LocalDate.of(1925, 9, 6), LocalDate.of(2019, 7, 17), "Italiana",
            "Andrea Camilleri è stato uno scrittore italiano, famoso per la serie di romanzi gialli che hanno come protagonista il commissario Montalbano.");
        
        // Autori internazionali classici
        Author garcia = createAuthor("Gabriel", "García Márquez", LocalDate.of(1927, 3, 6), LocalDate.of(2014, 4, 17), "Colombiana",
            "Gabriel García Márquez è stato uno scrittore, giornalista e saggista colombiano. Premio Nobel per la Letteratura nel 1982, è considerato uno dei più grandi scrittori del XX secolo e maestro del realismo magico.");
        Author orwell = createAuthor("George", "Orwell", LocalDate.of(1903, 6, 25), LocalDate.of(1950, 1, 21), "Britannica",
            "George Orwell è stato uno scrittore e giornalista britannico. Le sue opere '1984' e 'La fattoria degli animali' sono considerate capolavori della letteratura distopica del XX secolo.");
        Author tolkien = createAuthor("John Ronald Reuel", "Tolkien", LocalDate.of(1892, 1, 3), LocalDate.of(1973, 9, 2), "Britannica",
            "J.R.R. Tolkien è stato uno scrittore e filologo britannico, creatore del mondo della Terra di Mezzo e autore de 'Il Signore degli Anelli' e 'Lo Hobbit'.");
        Author christie = createAuthor("Agatha", "Christie", LocalDate.of(1890, 9, 15), LocalDate.of(1976, 1, 12), "Britannica",
            "Agatha Christie è stata una scrittrice britannica. È considerata la regina del giallo e una delle autrici più pubblicate al mondo, creatrice di Hercule Poirot e Miss Marple.");
        Author woolf = createAuthor("Virginia", "Woolf", LocalDate.of(1882, 1, 25), LocalDate.of(1941, 3, 28), "Britannica",
            "Virginia Woolf è stata una scrittrice, saggista e attivista britannica. È considerata una delle principali figure del modernismo letterario del XX secolo.");
        
        // Autori contemporanei internazionali
        Author murakami = createAuthor("Haruki", "Murakami", LocalDate.of(1949, 1, 12), null, "Giapponese",
            "Haruki Murakami è uno scrittore giapponese contemporaneo. I suoi romanzi e racconti combinano elementi del magico e del surreale con temi della vita urbana contemporanea.");
        Author rowling = createAuthor("Joanne", "Rowling", LocalDate.of(1965, 7, 31), null, "Britannica",
            "J.K. Rowling è una scrittrice britannica, nota soprattutto per essere l'autrice della serie di romanzi fantasy Harry Potter.");
        Author atwood = createAuthor("Margaret", "Atwood", LocalDate.of(1939, 11, 18), null, "Canadese",
            "Margaret Atwood è una scrittrice canadese. È nota per i suoi romanzi distopici, in particolare 'Il racconto dell'ancella' e la trilogia di MaddAddam.");
        Author king = createAuthor("Stephen", "King", LocalDate.of(1947, 9, 21), null, "Americana",
            "Stephen King è uno scrittore statunitense, uno dei più celebri autori di letteratura horror, fantasy e fantascienza.");
        Author coelho = createAuthor("Paulo", "Coelho", LocalDate.of(1947, 8, 24), null, "Brasiliana",
            "Paulo Coelho è uno scrittore brasiliano. Il suo romanzo più famoso, 'L'alchimista', è uno dei libri più venduti nella storia.");
        
        // Salva gli autori nel database
        authorRepository.saveAll(Arrays.asList(eco, calvino, ferrante, manzoni, dante, baricco, tamaro, 
            giordano, saviano, camilleri, garcia, orwell, tolkien, christie, woolf, murakami, rowling, 
            atwood, king, coelho));
        
        // Crea libri di esempio
        createBook("Il nome della rosa", 1980, 
                  "Un romanzo storico ambientato in un monastero medievale dove si susseguono misteriosi omicidi. Un'indagine condotta da Guglielmo da Baskerville rivela intrighi, eresie e segreti nascosti tra i manoscritti di una biblioteca labirintica.",
                  Set.of(eco));
        
        createBook("Se una notte d'inverno un viaggiatore", 1979,
                  "Un romanzo metanarrativo che gioca con le convenzioni della lettura stessa. Il protagonista è 'tu', il lettore, che cerca di leggere un libro che si rivela essere sempre un altro libro.",
                  Set.of(calvino));
                  
        createBook("Le città invisibili", 1972,
                  "Marco Polo descrive a Kublai Khan le meravigliose città del suo impero. Un'opera poetica sulla memoria, l'immaginazione e il rapporto tra realtà e fantasia.",
                  Set.of(calvino));
        
        createBook("L'amica geniale", 2011,
                  "Il primo volume della tetralogia napoletana che racconta l'amicizia tra Elena e Lila, due bambine in un rione popolare di Napoli negli anni '50.",
                  Set.of(ferrante));
        
        createBook("I promessi sposi", 1827,
                  "Il grande romanzo storico italiano che narra le vicende di Renzo e Lucia nella Lombardia del XVII secolo, tra pestilenze, guerra e oppressioni.",
                  Set.of(manzoni));
        
        createBook("Cent'anni di solitudine", 1967,
                  "La saga della famiglia Buendía nel villaggio immaginario di Macondo. Un capolavoro del realismo magico che racconta la storia dell'America Latina.",
                  Set.of(garcia));
        
        createBook("1984", 1949,
                  "Il romanzo distopico per eccellenza. Winston Smith vive in un mondo totalitario controllato dal Grande Fratello, dove la verità è manipolata e la libertà è un'illusione.",
                  Set.of(orwell));
        
        createBook("La fattoria degli animali", 1945,
                  "Una favola allegorica che racconta la rivoluzione degli animali di una fattoria contro il proprietario umano, rivelando i meccanismi del potere e della corruzione.",
                  Set.of(orwell));
        
        createBook("Norwegian Wood", 1987,
                  "Un romanzo di formazione ambientato nella Tokyo degli anni '60. La storia d'amore tra Toru Watanabe e due donne molto diverse, sullo sfondo della controcultura giovanile.",
                  Set.of(murakami));
        
        createBook("Kafka sulla spiaggia", 2002,
                  "Due storie parallele si intrecciano in un racconto surreale che mescla realtà e fantasia. Un quindicenne in fuga e un anziano con poteri speciali in un Giappone magico e misterioso.",
                  Set.of(murakami));
        
        createBook("Mrs. Dalloway", 1925,
                  "Un giorno nella vita di Clarissa Dalloway a Londra, raccontato attraverso la tecnica del flusso di coscienza. Un'esplorazione della psiche umana e del tempo.",
                  Set.of(woolf));
        
        createBook("Gita al faro", 1927,
                  "Il romanzo segue la famiglia Ramsay durante le loro vacanze estive sull'isola di Skye. Un'opera modernista sulla memoria, il tempo e le relazioni familiari.",
                  Set.of(woolf));
        
        System.out.println("Database inizializzato con successo!");
        System.out.println("- " + authorRepository.count() + " autori creati");
        System.out.println("- " + bookRepository.count() + " libri creati");
    }
    
    private Author createAuthor(String firstName, String lastName, LocalDate birthDate, LocalDate deathDate, String nationality, String biography) {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setBirthDate(birthDate);
        author.setDeathDate(deathDate);
        author.setNationality(nationality);
        author.setBiography(biography);
        
        // Prova a caricare la foto dell'autore se esiste
        try {
            String fileName = firstName.toLowerCase() + "_" + lastName.toLowerCase().replace(" ", "_") + ".jpg";
            byte[] photoData = loadImageFromResources("static/immagini/autori/" + fileName);
            if (photoData != null) {
                author.setPhoto(photoData);
                author.setPhotoContentType("image/jpeg");
                System.out.println("Caricata foto per: " + firstName + " " + lastName);
            }
        } catch (Exception e) {
            // Se la foto non esiste, continua senza errore
            System.out.println("Nessuna foto trovata per: " + firstName + " " + lastName);
        }
        
        return author;
    }
    
    private void createBook(String title, Integer year, String description, Set<Author> authors) {
        Book book = new Book();
        book.setTitle(title);
        book.setPublicationYear(year);
        book.setDescription(description);
        book.setAuthors(authors);
        
        // Prova a caricare la copertina del libro se esiste
        try {
            String fileName = title.toLowerCase()
                .replace(" ", "_")
                .replace("'", "")
                .replace("à", "a")
                .replace("è", "e")
                .replace("ì", "i")
                .replace("ò", "o")
                .replace("ù", "u") + ".jpg";
            byte[] coverData = loadImageFromResources("static/immagini/libri/" + fileName);
            if (coverData != null) {
                book.setCoverImage(coverData);
                book.setCoverImageContentType("image/jpeg");
                System.out.println("Caricata copertina per: " + title);
            }
        } catch (Exception e) {
            // Se la copertina non esiste, continua senza errore
            System.out.println("Nessuna copertina trovata per: " + title);
        }
        
        bookRepository.save(book);
    }
    
    /**
     * Carica un'immagine dalle risorse del classpath
     */
    private byte[] loadImageFromResources(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (resource.exists()) {
                return Files.readAllBytes(Paths.get(resource.getURI()));
            }
        } catch (Exception e) {
            // File non trovato o errore di lettura
        }
        return null;
    }
    
    private void createDefaultAdmin() {
        // Crea un SUPER_ADMIN di default se non esiste
        if (!usersRepository.findByUsername("superadmin").isPresent()) {
            Users superAdmin = new Users();
            superAdmin.setUsername("superadmin");
            superAdmin.setEmail("superadmin@biblioteca.it");
            superAdmin.setPassword(passwordEncoder.encode("super123"));
            superAdmin.setRole("SUPER_ADMIN");
            usersRepository.save(superAdmin);
            System.out.println("✅ Super Amministratore creato: username=superadmin, password=super123");
        }
        
        // Crea un utente amministratore di default se non esiste
        if (!usersRepository.findByUsername("admin").isPresent()) {
            Users admin = new Users();
            admin.setUsername("admin");
            admin.setEmail("admin@biblioteca.it");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            usersRepository.save(admin);
            System.out.println("✅ Amministratore creato: username=admin, password=admin123");
        }
        
        // Crea un utente di esempio se non esiste
        if (!usersRepository.findByUsername("utente").isPresent()) {
            Users user = new Users();
            user.setUsername("utente");
            user.setEmail("utente@biblioteca.it");
            user.setPassword(passwordEncoder.encode("utente123"));
            user.setRole("USER");
            usersRepository.save(user);
            System.out.println("✅ Utente di esempio creato: username=utente, password=utente123");
        }
        
        System.out.println("\n=== 🚀 UTENTI DI TEST DISPONIBILI ===");
        System.out.println("Super Admin: superadmin / super123 (può gestire tutti gli utenti)");
        System.out.println("Admin: admin / admin123 (può gestire libri e autori)");
        System.out.println("User: utente / utente123 (può scrivere recensioni)");
        System.out.println("====================================\n");
    }
}

package it.uniroma3.siw.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import it.uniroma3.siw.service.UsersService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private UsersService usersService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Risorse pubbliche accessibili a tutti (utenti occasionali)
                        .requestMatchers("/", "/users/register", "/users/login", "/login", "/css/**", "/js/**", "/images/**", "/immagini/**", "/roles-info", "/about").permitAll()
                        // Consultazione libri, autori e recensioni - accessibile a tutti
                        .requestMatchers("/book", "/book/details/**", "/book/search", "/author", "/author/details/**", "/author/search", "/reviews/book/**", "/reviews/all").permitAll()
                        // Operazioni riservate agli amministratori e super-amministratori
                        .requestMatchers("/book/create", "/book/edit/**", "/book/delete/**", "/author/create", "/author/edit/**", "/author/delete/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        // Eliminazione recensioni - solo amministratori e super-amministratori
                        .requestMatchers("/reviews/*/delete").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        // Gestione utenti - solo super-amministratori
                        .requestMatchers("/admin/users/**").hasRole("SUPER_ADMIN")
                        // Scrittura recensioni e profilo - solo utenti registrati
                        .requestMatchers("/reviews/submit", "/users/profile", "/users/change-password", "/users/update-profile").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/users/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/users/login?accessDenied=true");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .userDetailsService(usersService);
        return http.build();
    }
}
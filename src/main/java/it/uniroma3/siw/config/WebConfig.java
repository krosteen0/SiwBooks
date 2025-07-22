package it.uniroma3.siw.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        // Formatter per LocalDate per supportare il formato ISO date (yyyy-MM-dd)
        registry.addFormatter(new DateFormatter("yyyy-MM-dd"));
    }
}

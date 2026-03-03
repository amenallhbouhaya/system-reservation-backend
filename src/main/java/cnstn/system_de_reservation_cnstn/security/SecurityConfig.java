package cnstn.system_de_reservation_cnstn.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor

public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> {});

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/admin/**").hasRole("Admin").requestMatchers("/api/responsable-salle/**").hasRole("ResponsableSalle")
                .requestMatchers("/api/responsable-securite/**").hasRole("ResponsableSecurite")
                .requestMatchers("/api/directeur-dsn/**").hasRole("DirecteurDsn")
                .requestMatchers("/Salle/**").hasAnyRole("Admin", "ResponsableSalle")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/Evenement/add-full")
                .hasAnyRole("Employe","ResponsableSalle","ResponsableSecurite","DirecteurDsn","Admin")

                .requestMatchers(org.springframework.http.HttpMethod.GET, "/Evenement/salles", "/Evenement/equipements", "/Evenement/my")
                .authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/Evenement/add-full")
                .hasAnyRole("Employe","ResponsableSalle","ResponsableSecurite","DirecteurDsn","Admin")
                .requestMatchers("/error").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll().requestMatchers("/api/directeur-dsn/documents/**").hasRole("DirecteurDsn")
                .requestMatchers(HttpMethod.GET, "/Document/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/Document/*/download").authenticated()
                .requestMatchers("/Document/add", "/Document/all", "/Document/**").hasRole("Admin")
                .requestMatchers("/api/directeur-dsn/**").hasRole("DirecteurDsn")
                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:4200"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
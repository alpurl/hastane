package org.example.config;

import org.example.security.CustomUserDetailsService;
import org.example.security.JwtAuthenticationFilter;
import org.example.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
// import org.springframework.boot.web.servlet.FilterRegistrationBean; // ARTIK BUNA GEREK YOK
// import org.springframework.core.Ordered; // ARTIK BUNA GEREK YOK


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtTokenProvider jwtTokenProvider) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // ********************************************
    // JWT Authentication Filtresini @Bean olarak tanımlıyoruz
    // FilterRegistrationBean yerine HttpSecurity ile açıkça ekleyeceğiz
    // ********************************************
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() { // Metot adı jwtFilterRegistration'dan jwtAuthenticationFilter'a değişti
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter();
        jwtFilter.setJwtTokenProvider(jwtTokenProvider);
        jwtFilter.setCustomUserDetailsService(customUserDetailsService);
        return jwtFilter;
    }

    // ********************************************
    // CORS Filtresi Tanımlaması (Bu kısım aynı kalabilir)
    // ********************************************
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000"); // Frontend'in URL'si
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/**")).permitAll()
                // /api/users/doctors endpoint'i için açıkça kimlik doğrulaması gerekiyor kuralı
                // Eğer hasRole('PATIENT') istiyorsanız, bu kuralı kullanın:
                // .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/users/doctors")).hasRole("PATIENT")
                // Veya şimdilik sadece kimliği doğrulanmış olmasını istiyorsanız:
                .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/users/doctors")).authenticated() // <-- DÜZELTME BURADA!
                .anyRequest().authenticated()
            );

        // ********************************************
        // JWT Authentication Filtresini HttpSecurity'e ekliyoruz
        // UsernamePasswordAuthenticationFilter'dan ÖNCE çalışmasını sağlıyoruz.
        // ********************************************
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class); // <-- DÜZELTME BURADA!

        // CorsFilter'ı da SecurityFilterChain'e eklemek istiyorsanız (genellikle en başa gelir)
        // http.addFilterBefore(corsFilter(), ChannelProcessingFilter.class); // Veya başka bir erken filtre

        return http.build();
    }
}
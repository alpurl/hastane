package org.example.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap; // Claims eklemek için
import java.util.Map;     // Claims eklemek için
import org.example.security.UserPrincipal; // UserPrincipal sınıfınızı import edin

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Kimlik doğrulanmış bir kullanıcıdan JWT token oluşturur.
     * Kullanıcı rolü ve ID'si JWT payload'ına claim olarak eklenir.
     *
     * @param authentication Spring Security Authentication nesnesi
     * @return Oluşturulan JWT token (String)
     */
public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userPrincipal.getId());
        // BURADAKİ SATIRI DÜZELTİN: "ROLE_" önekini ekleyin
        claims.put("role", "ROLE_" + userPrincipal.getRole().name()); // <-- DEĞİŞİKLİK BURADA!

        // Eğer UserPrincipal'ınızda firstName, lastName, specialty gibi ek bilgiler varsa onları da buraya ekleyebilirsiniz:
        // claims.put("firstName", userPrincipal.getFirstName());
        // claims.put("lastName", userPrincipal.getLastName());
        // claims.put("specialty", userPrincipal.getSpecialty());

        return Jwts.builder()
                .setClaims(claims) // Oluşturulan custom claim'leri ekle
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Bir JWT token'dan kullanıcı adını (subject) alır.
     *
     * @param token JWT token (String)
     * @return Kullanıcı adı (String)
     */
    public String getUsernameFromJwt(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Bir JWT token'ın geçerliliğini doğrular.
     *
     * @param authToken Doğrulanacak JWT token (String)
     * @return Token geçerliyse true, değilse false
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Geçersiz JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Süresi dolmuş JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Desteklenmeyen JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string'i boş");
        } catch (io.jsonwebtoken.security.SignatureException ex) { // JWT 0.10.0+ için
            logger.error("JWT imza hatası");
        }
        return false;
    }
}
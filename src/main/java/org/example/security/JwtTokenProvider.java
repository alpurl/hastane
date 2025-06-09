package org.example.security;

import io.jsonwebtoken.*; // io.jsonwebtoken kütüphanesini eklemeniz gerekecek (pom.xml'e bakınız)
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys; // security modülü için

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import org.example.security.UserPrincipal;



@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // application.properties dosyasından okunacak JWT gizli anahtarı
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    // application.properties dosyasından okunacak JWT geçerlilik süresi (ms cinsinden)
    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    // JWT'yi oluşturmak için kullanılacak anahtarı döndürür
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Kimlik doğrulanmış bir kullanıcıdan JWT token oluşturur.
     *
     * @param authentication Spring Security Authentication nesnesi
     * @return Oluşturulan JWT token (String)
     */
    public String generateToken(Authentication authentication) {
        // authentication.getPrincipal() metodu UserPrincipal objesini döndürmelidir
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // Token'ın kime ait olduğunu belirtir (kullanıcı adı)
                .setIssuedAt(new Date()) // Token'ın ne zaman oluşturulduğu
                .setExpiration(expiryDate) // Token'ın son kullanma tarihi
                .signWith(key(), SignatureAlgorithm.HS512) // Token'ı imzalamak için kullanılan anahtar ve algoritma
                .compact(); // Token'ı bir String'e dönüştürür
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
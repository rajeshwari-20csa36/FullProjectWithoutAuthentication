package com.ust.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.MalformedJwtException;
import lombok.Value;
import org.springframework.stereotype.Component;
//
//import io.jsonwebtoken.*;

import org.springframework.security.core.Authentication;


import java.security.SignatureException;
import java.util.Date;

@Component
public class JwtUtil {


    private String jwtSecret = "dGhpc2lzYXZlcnlsb25nYW5kc2VjdXJlc2VjcmV0a2V5Zm9yeW91cmp3dHRva2Vuc2Rvbm90c2hhcmU=";

    private int jwtExpirationMs =86400000;

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsernameFromJWT(String token) {

        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            System.out.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.out.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.out.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.out.println("JWT claims string is empty");
        }
        return false;
    }
}

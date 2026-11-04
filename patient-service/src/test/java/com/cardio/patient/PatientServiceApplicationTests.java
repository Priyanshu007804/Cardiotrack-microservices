package com.cardio.patient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cardio.patient.security.JwtService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PatientServiceApplicationTests {

    @Autowired
    private JwtService jwtService;

	@Test
	void contextLoads() {
	}

    @Test
    void testExtractUserId() {
        String secret = "sjydtrdiyckhgxrxtrkfvkhcdjrtydyuctfluyfkuyfkuyktycdytdckychbkjvh";
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        var key = Keys.hmacShaKeyFor(keyBytes);

        String token = Jwts.builder()
                .subject("test@example.com")
                .claim("userId", 1L)
                .claim("role", "PATIENT")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();

        Long userId = jwtService.extractUserId(token);
        assertEquals(1L, userId);
    }

}

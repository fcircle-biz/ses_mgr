package jp.co.example.sesapp.common.auth.service.impl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import jp.co.example.sesapp.common.auth.service.JwtProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JwtProviderインターフェースの実装クラス
 */
@Service
public class JwtProviderImpl implements JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProviderImpl.class);

    @Value("${application.security.jwt.secret-key}")
    private String jwtSecret;

    @Value("${application.security.jwt.access-token-expiration}")
    private long accessTokenExpirationMs;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    @Value("${application.security.jwt.issuer}")
    private String issuer;

    private final RoleRepository roleRepository;

    public JwtProviderImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAccessToken(User user, List<String> permissions) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        Optional<Role> roleOpt = roleRepository.findById(user.getRoleId());
        String roleName = roleOpt.map(Role::getName).orElse("USER");

        // Build JWT token
        JwtBuilder jwtBuilder = Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer(issuer)
                .claim("userId", user.getId().toString())
                .claim("role", roleName)
                .claim("email", user.getEmail())
                .claim("name", user.getName());

        // Add permissions if available
        if (permissions != null && !permissions.isEmpty()) {
            jwtBuilder.claim("permissions", String.join(",", permissions));
        }

        return jwtBuilder.signWith(getSigningKey()).compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer(issuer)
                .claim("tokenType", "refresh")
                .claim("userId", user.getId().toString())
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (SecurityException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "refresh".equals(claims.get("tokenType"));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UUID getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        String userIdStr = claims.get("userId", String.class);
        return UUID.fromString(userIdStr);
    }

    @Override
    public String getUsernameFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    @Override
    public Date getExpirationFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration();
    }

    @Override
    public Authentication getAuthentication(String token) {
        Claims claims = extractAllClaims(token);

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role with ROLE_ prefix
        String role = claims.get("role", String.class);
        if (role != null && !role.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        }
        
        // Add individual permissions if available
        String permissionsStr = claims.get("permissions", String.class);
        if (permissionsStr != null && !permissionsStr.isEmpty()) {
            Arrays.stream(permissionsStr.split(","))
                    .forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        }

        // Create UserDetails principal
        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User(
                        claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    @Override
    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
    
    @Override
    public String getRoleFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }
}
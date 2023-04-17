package com.example.gromit.service;

import com.example.gromit.dto.user.TokenDto;
import com.example.gromit.entity.UserAccount;
import com.example.gromit.exception.NotFoundException;
import com.example.gromit.exception.UnauthorizedException;
import com.example.gromit.repository.UserAccountRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;

import static com.example.gromit.exception.ErrorCode.EXPIRED_TOKEN;
import static com.example.gromit.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final CustomUserDetailService customUserDetailService;

    @Value("${jwt.secret}")
    private String JWT_SECRET;
    private final UserAccountRepository userAccountRepository;

    public String encodeJwtToken(TokenDto tokenDto) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer("gromit")
                .setIssuedAt(now)
                .setSubject(tokenDto.getUserAccountId().toString())
                .setExpiration(new Date(now.getTime() + Duration.ofDays(180).toMillis()))
                .claim("userAccountId", tokenDto.getUserAccountId())
                .claim("roles", "USER")
                .signWith(SignatureAlgorithm.HS256,
                        Base64.getEncoder().encodeToString(("" + JWT_SECRET).getBytes(
                                StandardCharsets.UTF_8)))
                .compact();
    }

    public String encodeJwtRefreshToken(Long userAccountId) {
        Date now = new Date();
        return Jwts.builder()
                .setIssuedAt(now)
                .setSubject(userAccountId.toString())
                .setExpiration(new Date(now.getTime() + Duration.ofMinutes(20160).toMillis()))
                .claim("userAccountId", userAccountId)
                .claim("roles", "USER")
                .signWith(SignatureAlgorithm.HS256,
                        Base64.getEncoder().encodeToString(("" + JWT_SECRET).getBytes(
                                StandardCharsets.UTF_8)))
                .compact();
    }

    public Long getUserAccountIdFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(("" + JWT_SECRET).getBytes(
                        StandardCharsets.UTF_8)))
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = customUserDetailService.loadUserByUsername(
                this.getUserAccountIdFromJwtToken(token).toString());
        return new UsernamePasswordAuthenticationToken(userDetails, "",
                userDetails.getAuthorities());
    }


    public Boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(Base64.getEncoder().encodeToString(("" + JWT_SECRET).getBytes(
                            StandardCharsets.UTF_8))).parseClaimsJws(token);
            if (claims.getBody().getExpiration().before(new Date())) {
                throw new UnauthorizedException(EXPIRED_TOKEN);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean validateRefreshToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(Base64.getEncoder().encodeToString(("" + JWT_SECRET).getBytes(
                            StandardCharsets.UTF_8))).parseClaimsJws(token);
            if (claims.getBody().getExpiration().before(new Date())) {
                throw new UnauthorizedException(EXPIRED_TOKEN);
            }

            // 유저 리프레쉬 토큰 확인
            Long userAccountId = getUserAccountIdFromJwtToken(token);

            UserAccount userAccount = userAccountRepository.findByIdAndIsDeleted(userAccountId, false)
                    .orElseThrow(()-> new NotFoundException(USER_NOT_FOUND));

            if(userAccount.getRefreshToken().equals(token)){
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public String getToken(HttpServletRequest request) {
        return request.getHeader("X-AUTH-TOKEN");
    }

    public String getRefreshToken(HttpServletRequest request){
        return request.getHeader("X-REFRESH-TOKEN");
    }
}

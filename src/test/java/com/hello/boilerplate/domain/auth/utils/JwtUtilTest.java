package com.hello.boilerplate.domain.auth.utils;

import com.hello.boilerplate.domain.auth.service.RedisTokenService;
import com.hello.boilerplate.domain.user.entity.User;
import com.hello.boilerplate.support.fixture.UserFixture;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @Mock
    private RedisTokenService redisTokenService;
    
    private static final String TEST_ACCESS_SECRET = "accessabcdefghijklmnopqrstuvwxyz";
    private static final String TEST_REFRESH_SECRET = "refreshabcdefghijklmnopqrstuvwxyz";
    private static final Long TEST_EXPIRATION = 3_600L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
                redisTokenService,
                TEST_ACCESS_SECRET,
                TEST_REFRESH_SECRET,
                TEST_EXPIRATION,
                TEST_EXPIRATION
        );
    }

    @Test
    void shouldCreateAccessTokenWhenUserAndNowDateGiven() {
        //given
        User user = UserFixture.USER_FIXTURE_1.create();
        Date now = new Date();
        
        //when
        String accessToken = jwtUtil.createAccessToken(user, now);
        
        //then
        Claims claims = getClaims(accessToken, TEST_ACCESS_SECRET);

        assertAll(
                () -> assertThat(accessToken).isNotNull(),
                () -> assertThat(claims.getSubject()).isEqualTo(user.getLoginId())
        );
    }

    @Test
    void shouldCreateRefreshTokenWhenUserAndNowDateGiven() {
        //given
        User user = UserFixture.USER_FIXTURE_1.create();
        Date now = new Date();

        //when
        String refreshToken = jwtUtil.createRefreshToken(user, now);

        //then
        Claims claims = getClaims(refreshToken, TEST_REFRESH_SECRET);

        assertAll(
                () -> assertThat(refreshToken).isNotNull(),
                () -> assertThat(claims.getSubject()).isEqualTo(user.getLoginId())
        );
        verify(redisTokenService, times(1)).saveRefreshToken(user.getLoginId(), refreshToken, TEST_EXPIRATION);
    }
    
    @Test
    void shouldValidateAccessTokenWhenAccessTokenGiven() {
        //given
        User user = UserFixture.USER_FIXTURE_1.create();
        Date now = new Date();
        String accessToken = createToken(user.getLoginId(), TEST_ACCESS_SECRET, now);

        //when & then
        assertDoesNotThrow(() -> jwtUtil.validateAccessToken(accessToken));
    }
    
    @Test
    void shouldValidateRefreshTokenWhenRefreshTokenGiven() {
        //given
        User user = UserFixture.USER_FIXTURE_1.create();
        Date now = new Date();
        String refreshToken = createToken(user.getLoginId(), TEST_REFRESH_SECRET, now);
        String subject = getClaims(refreshToken, TEST_REFRESH_SECRET).getSubject();
        when(redisTokenService.getRefreshToken(subject)).thenReturn(refreshToken);

        //when & then
        assertDoesNotThrow(() -> jwtUtil.validateRefreshToken(subject, refreshToken));
        verify(redisTokenService, times(1)).getRefreshToken(subject);
    }
    
    @Test
    void shouldGetAccessTokenClaimsWhenAccessTokenGiven() {
        //given
        User user = UserFixture.USER_FIXTURE_1.create();
        Date now = new Date();
        String accessToken = createToken(user.getLoginId(), TEST_ACCESS_SECRET, now);
        
        //when
        Claims claims = jwtUtil.getAccessTokenClaims(accessToken);

        //then
        assertThat(claims.getSubject()).isEqualTo(user.getLoginId());
    }
    
    @Test
    void shouldInvalidateRefreshTokenWhenSubjectGiven() {
        //given
        User user = UserFixture.USER_FIXTURE_1.create();
        Date now = new Date();
        String accessToken = createToken(user.getLoginId(), TEST_ACCESS_SECRET, now);
        String subject = getClaims(accessToken, TEST_ACCESS_SECRET).getSubject();

        //when
        jwtUtil.invalidateRefreshToken(subject);

        //then
        verify(redisTokenService, times(1)).deleteRefreshToken(subject);
    }

    @Test
    void shouldResolveAccessTokenWhenRequestAccessTokenInHeaderGiven() {
        //given
        User user = UserFixture.USER_FIXTURE_1.create();
        Date now = new Date();
        String accessToken = createToken(user.getLoginId(), TEST_ACCESS_SECRET, now);
        String requestAccessTokenInHeader = "Bearer " + accessToken;

        //when
        String resultAccessToken = jwtUtil.resolveAccessToken(requestAccessTokenInHeader);

        //then
        assertThat(accessToken).isEqualTo(resultAccessToken);
    }
    

    private String createToken(String subject, String secretKey, Date now) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + TEST_EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
    
    private Claims getClaims(String token, String secretKey) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
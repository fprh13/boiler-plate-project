package com.hello.boilerplate.auth.infrastructure.jwt;

import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.support.fixture.UserFixture;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String TEST_ACCESS_SECRET = "accessabcdefghijklmnopqrstuvwxyz";
    private static final String TEST_REFRESH_SECRET = "refreshabcdefghijklmnopqrstuvwxyz";
    private static final Long TEST_EXPIRATION_DAYS = 21L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
			TEST_ACCESS_SECRET,
			TEST_REFRESH_SECRET,
			TEST_EXPIRATION_DAYS,
			TEST_EXPIRATION_DAYS
        );
    }

    @Test
    void 엑세스_토큰을_생성한다() {
        //given
        User user = UserFixture.USER_FIXTURE_1.create();
        Date now = new Date();
        
        //when
        String accessToken = jwtUtil.createAccessToken(user, now);
        
        //then
        Claims claims = jwtUtil.getAccessTokenClaims(accessToken);

        assertAll(
                () -> assertThat(accessToken).isNotNull(),
                () -> assertThat(claims.getSubject()).isEqualTo(user.getLoginId())
        );
    }

    @Test
    void 재발급_토큰을_생성한다() {
        //given
        User user = UserFixture.USER_FIXTURE_1.create();
        Date now = new Date();

        //when
        String refreshToken = jwtUtil.createRefreshToken(user, now);

        //then
        Claims claims = getRefreshTokenClaims(refreshToken);

        assertAll(
                () -> assertThat(refreshToken).isNotNull(),
                () -> assertThat(claims.getSubject()).isEqualTo(user.getLoginId())
        );
    }
    
    @Test
    void 엑세스토큰을_검증한다() {
        //given
        User user = UserFixture.USER_FIXTURE_1.create();
        Date now = new Date();
        String accessToken = jwtUtil.createAccessToken(user, now);

        //when & then
        assertDoesNotThrow(() -> jwtUtil.validateAccessToken(accessToken));
    }
    
    @Test
    void 엑세스_토큰의_Claims를_추출한다() {
        //given
        User user = UserFixture.USER_FIXTURE_1.create();
        Date now = new Date();
        String accessToken = jwtUtil.createAccessToken(user, now);
        
        //when
        Claims claims = jwtUtil.getAccessTokenClaims(accessToken);

        //then
        assertThat(claims.getSubject()).isEqualTo(user.getLoginId());
    }
	
    private Claims getRefreshTokenClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(TEST_REFRESH_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
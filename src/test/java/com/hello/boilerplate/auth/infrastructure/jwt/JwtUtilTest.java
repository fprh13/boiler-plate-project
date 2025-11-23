package com.hello.boilerplate.auth.infrastructure.jwt;

import com.hello.boilerplate.common.exception.UnauthorizedException;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.support.fixture.UserFixture;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

	@Nested
	@DisplayName("엑세스 토큰 생성 기능")
	class CreateAccessToken {
		@Test
		void 엑세스_토큰을_생성한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			String claimKey = "role";
			long expirationMs = TEST_EXPIRATION_DAYS * 24 * 60 * 60 * 1_000L;

			Instant nowInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
			Date now = Date.from(nowInstant);

			//when
			String accessToken = jwtUtil.createAccessToken(user, now);

			//then
			Claims claims = getClaims(accessToken, TEST_ACCESS_SECRET);

			assertAll(
				() -> assertThat(accessToken).isNotNull(),
				() -> assertThat(claims.getSubject()).isEqualTo(user.getLoginId()),
				() -> assertThat(claims.get(claimKey)).isEqualTo(user.getRole().getKey()),
				() -> assertThat(claims.getIssuedAt()).isEqualTo(now),
				() -> assertThat(claims.getExpiration()).isEqualTo(new Date(now.getTime() + expirationMs))
			);
		}
	}

	@Nested
	@DisplayName("재발급 토큰 생성 기능")
	class CreateRefreshToken {
		@Test
		void 재발급_토큰을_생성한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			long expirationMs = TEST_EXPIRATION_DAYS * 24 * 60 * 60 * 1_000L;

			Instant nowInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
			Date now = Date.from(nowInstant);

			//when
			String refreshToken = jwtUtil.createRefreshToken(user, now);

			//then
			Claims claims = getClaims(refreshToken, TEST_REFRESH_SECRET);

			assertAll(
				() -> assertThat(refreshToken).isNotNull(),
				() -> assertThat(claims.getSubject()).isEqualTo(user.getLoginId()),
				() -> assertThat(claims.getIssuedAt()).isEqualTo(now),
				() -> assertThat(claims.getExpiration()).isEqualTo(new Date(now.getTime() + expirationMs))
			);
		}
	}

	@Nested
	@DisplayName("엑세스 토큰 Claims 추출 기능")
	class GetAccessTokenClaims {
		@Test
		void JWT_Claims을_추출한다() {
			//given
			String claimKey = "role";
			User user = UserFixture.USER_FIXTURE_1.create();

			long expirationMs = TEST_EXPIRATION_DAYS * 24 * 60 * 60 * 1_000L;
			Instant nowInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
			Date now = Date.from(nowInstant);

			String accessToken = jwtUtil.createAccessToken(user, now);

			//when
			Claims claims = jwtUtil.getAccessTokenClaims(accessToken);

			//then
			assertAll(
				() -> assertThat(accessToken).isNotNull(),
				() -> assertThat(claims.getSubject()).isEqualTo(user.getLoginId()),
				() -> assertThat(claims.get(claimKey)).isEqualTo(user.getRole().getKey()),
				() -> assertThat(claims.getIssuedAt()).isEqualTo(now),
				() -> assertThat(claims.getExpiration()).isEqualTo(new Date(now.getTime() + expirationMs))
			);
		}
		
		@Test
		void 잘못된_JWT_형식으로_파싱에_실패한_경우_예외를_반환한다() {
		    //given
			String accessToken = jwtUtil.createAccessToken(UserFixture.USER_FIXTURE_1.create(), new Date());
		    
		    //when & then
			assertThatThrownBy(() -> jwtUtil.getAccessTokenClaims("hacking" + accessToken))
				.isInstanceOf(UnauthorizedException.class);
		}
		
		@Test
		void JWT가_null인_경우_예외를_반환한다() {
		    //given
			String accessToken = null;
		    
		    //when & then
			assertThatThrownBy(() -> jwtUtil.getAccessTokenClaims(accessToken))
				.isInstanceOf(UnauthorizedException.class);
		    
		}
		
		@Test
		void JWT가_공백인_경우_예외를_반환한다() {
		    //given
		    String accessToken = "";
			
		    //when & then
			assertThatThrownBy(() -> jwtUtil.getAccessTokenClaims(accessToken))
				.isInstanceOf(UnauthorizedException.class);
		    
		}
		
		@Test
		void Claims의_subject가_없다면_예외를_반환한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			String claimsKey = "role";

			String accessToken = Jwts.builder()
				.claim(claimsKey, user.getRole().getKey())
				.compact();

			//when & then
			assertThatThrownBy(() -> jwtUtil.getAccessTokenClaims(accessToken))
				.isInstanceOf(UnauthorizedException.class);
		}
		
		@Test
		void Claims의_권한이_누락되면_예외를_반환한다() {
		    //given
			String accessToken = Jwts.builder()
				.subject("testLoginId")
				.compact();
		    
		    //when & then
			assertThatThrownBy(() -> jwtUtil.getAccessTokenClaims(accessToken))
				.isInstanceOf(UnauthorizedException.class);
		}
	}
	
    private Claims getClaims(String token, String secret) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
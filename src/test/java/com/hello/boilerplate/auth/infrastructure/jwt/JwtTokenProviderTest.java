package com.hello.boilerplate.auth.infrastructure.jwt;

import com.hello.boilerplate.auth.domain.VerificationPurpose;
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
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_ACCESS_SECRET = "accessabcdefghijklmnopqrstuvwxyz";
    private static final String TEST_REFRESH_SECRET = "refreshabcdefghijklmnopqrstuvwxyz";
	private static final String TEST_VERIFY_SECRET = "verificationabcdefghijklmnopqrstu";
    private static final Long TEST_EXPIRATION_DAYS = 21L;
	private static final Long TEST_EXPIRATION_SECONDS = 60L * 10;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
			TEST_ACCESS_SECRET,
			TEST_REFRESH_SECRET,
			TEST_VERIFY_SECRET,
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
			String accessToken = jwtTokenProvider.createAccessToken(user, now);

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
			String refreshToken = jwtTokenProvider.createRefreshToken(user, now);

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
	@DisplayName("인증(임시) 토큰 생성 기능")
	class CreateVerificationToken {
		@Test
		void 인증_토큰을_생성한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			long expirationMs = TEST_EXPIRATION_SECONDS * 1_000L;
			String claimKey = "purpose";

			Instant nowInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
			Date now = Date.from(nowInstant);

			//when
			String verificationToken = jwtTokenProvider.createVerificationToken(
				VerificationPurpose.PASSWORD_RESET, user.getLoginId(), now
			);

			//then
			Claims claims = getClaims(verificationToken, TEST_VERIFY_SECRET);

			assertAll(
				() -> assertThat(verificationToken).isNotNull(),
				() -> assertThat(claims.getSubject()).isEqualTo(user.getLoginId()),
				() -> assertThat(claims.get(claimKey)).isEqualTo(VerificationPurpose.PASSWORD_RESET.toString()),
				() -> assertThat(claims.getIssuedAt()).isEqualTo(now),
				() -> assertThat(claims.getExpiration()).isEqualTo(new Date(now.getTime() + expirationMs))
			);
		}
	}

	@Nested
	@DisplayName("엑세스 토큰 Claims 추출 기능")
	class GetAccessTokenClaims {
		@Test
		void AccessToken_Claims을_추출한다() {
			//given
			String claimKey = "role";
			User user = UserFixture.USER_FIXTURE_1.create();

			long expirationMs = TEST_EXPIRATION_DAYS * 24 * 60 * 60 * 1_000L;
			Instant nowInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
			Date now = Date.from(nowInstant);

			String accessToken = jwtTokenProvider.createAccessToken(user, now);

			//when
			Claims claims = jwtTokenProvider.parseAccessToken(accessToken);

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
		void 잘못된_AccessToken_형식으로_파싱에_실패한_경우_예외를_반환한다() {
		    //given
			String accessToken = jwtTokenProvider.createAccessToken(UserFixture.USER_FIXTURE_1.create(), new Date());
		    
		    //when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken("hacking" + accessToken))
				.isInstanceOf(UnauthorizedException.class);
		}
		
		@Test
		void AccessToken이_null인_경우_예외를_반환한다() {
		    //given
			String accessToken = null;
		    
		    //when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(accessToken))
				.isInstanceOf(UnauthorizedException.class);
		    
		}
		
		@Test
		void AccessToken이_공백인_경우_예외를_반환한다() {
		    //given
		    String accessToken = "";
			
		    //when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(accessToken))
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
			assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(accessToken))
				.isInstanceOf(UnauthorizedException.class);
		}
		
		@Test
		void Claims의_권한이_누락되면_예외를_반환한다() {
		    //given
			String accessToken = Jwts.builder()
				.subject("testLoginId")
				.compact();
		    
		    //when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(accessToken))
				.isInstanceOf(UnauthorizedException.class);
		}
	}

	@Nested
	@DisplayName("재발급 토큰 Claims 추출 기능")
	class GetRefreshTokenClaims {
		@Test
		void RefreshToken_Claims을_추출한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();

			long expirationMs = TEST_EXPIRATION_DAYS * 24 * 60 * 60 * 1_000L;
			Instant nowInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
			Date now = Date.from(nowInstant);

			String refreshToken = jwtTokenProvider.createRefreshToken(user, now);

			//when
			Claims claims = jwtTokenProvider.parseRefreshToken(refreshToken);

			//then
			assertAll(
				() -> assertThat(refreshToken).isNotNull(),
				() -> assertThat(claims.getSubject()).isEqualTo(user.getLoginId()),
				() -> assertThat(claims.getIssuedAt()).isEqualTo(now),
				() -> assertThat(claims.getExpiration()).isEqualTo(new Date(now.getTime() + expirationMs))
			);
		}

		@Test
		void 잘못된_RefreshToken_형식으로_파싱에_실패한_경우_예외를_반환한다() {
			//given
			String refreshToken = jwtTokenProvider.createRefreshToken(UserFixture.USER_FIXTURE_1.create(), new Date());

			//when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseRefreshToken("hacking" + refreshToken))
				.isInstanceOf(UnauthorizedException.class);
		}

		@Test
		void RefreshToken이_null인_경우_예외를_반환한다() {
			//given
			String refreshToken = null;

			//when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseRefreshToken(refreshToken))
				.isInstanceOf(UnauthorizedException.class);

		}

		@Test
		void RefreshToken이_공백인_경우_예외를_반환한다() {
			//given
			String refreshToken = "";

			//when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseRefreshToken(refreshToken))
				.isInstanceOf(UnauthorizedException.class);

		}

		@Test
		void Claims의_subject가_없다면_예외를_반환한다() {
			//given
			String refreshToken = Jwts.builder().compact();

			//when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseRefreshToken(refreshToken))
				.isInstanceOf(UnauthorizedException.class);
		}
	}

	@Nested
	@DisplayName("인증(임시) 토큰 Claims 추출 기능")
	class GetVerificationTokenClaims {
		@Test
		void VerificationToken_Claims을_추출한다() {
			//given
			String claimKey = "purpose";
			User user = UserFixture.USER_FIXTURE_1.create();

			long expirationMs = TEST_EXPIRATION_SECONDS * 1_000L;
			Instant nowInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
			Date now = Date.from(nowInstant);

			String verificationToken = jwtTokenProvider.createVerificationToken(VerificationPurpose.PASSWORD_RESET, user.getLoginId(), now);

			//when
			Claims claims = jwtTokenProvider.parseVerificationToken(VerificationPurpose.PASSWORD_RESET, verificationToken);

			//then
			assertAll(
				() -> assertThat(verificationToken).isNotNull(),
				() -> assertThat(claims.getSubject()).isEqualTo(user.getLoginId()),
				() -> assertThat(claims.get(claimKey)).isEqualTo(VerificationPurpose.PASSWORD_RESET.toString()),
				() -> assertThat(claims.getIssuedAt()).isEqualTo(now),
				() -> assertThat(claims.getExpiration()).isEqualTo(new Date(now.getTime() + expirationMs))
			);
		}

		@Test
		void 잘못된_VerificationToken_형식으로_파싱에_실패한_경우_예외를_반환한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			String verificationToken = jwtTokenProvider.createVerificationToken(
				VerificationPurpose.PASSWORD_RESET, user.getLoginId(), new Date()
			);

			//when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseVerificationToken(VerificationPurpose.PASSWORD_RESET, "hacking" + verificationToken))
				.isInstanceOf(UnauthorizedException.class);
		}

		@Test
		void VerificationToken이_null인_경우_예외를_반환한다() {
			//given
			String verificationToken = null;

			//when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseVerificationToken(VerificationPurpose.PASSWORD_RESET, verificationToken))
				.isInstanceOf(UnauthorizedException.class);

		}

		@Test
		void VerificationToken이_공백인_경우_예외를_반환한다() {
			//given
			String verificationToken = "";

			//when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseVerificationToken(VerificationPurpose.PASSWORD_RESET, verificationToken))
				.isInstanceOf(UnauthorizedException.class);

		}

		@Test
		void Claims의_subject가_없다면_예외를_반환한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			String claimsKey = "purpose";

			String verificationToken = Jwts.builder()
				.claim(claimsKey, VerificationPurpose.PASSWORD_RESET)
				.compact();

			//when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseVerificationToken(VerificationPurpose.PASSWORD_RESET, verificationToken))
				.isInstanceOf(UnauthorizedException.class);
		}

		@Test
		void Claims의_인증_목적이_다르다면_예외를_반환한다() {
			//given
			String claimsKey = "purpose";

			String verificationToken = Jwts.builder()
				.subject("testLoginId")
				.claim(claimsKey, "otherPurpose")
				.compact();

			//when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseVerificationToken(VerificationPurpose.PASSWORD_RESET, verificationToken))
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
package com.hello.boilerplate.domain.user.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hello.boilerplate.support.fixture.UserFixture;

class UserTest {

	private User user;

	@BeforeEach
	public void setUp() {
		user = UserFixture.USER_FIXTURE_1.create();
	}

	@Test
	void 회원_정보를_업데이트한다() {
	    //given
		String changedName = "이름바꾸기";

	    //when
	    user.updateInfo(changedName);

	    //then
		Assertions.assertThat(user.getName()).isEqualTo(changedName);
	}
}
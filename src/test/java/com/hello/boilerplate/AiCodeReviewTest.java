package com.hello.boilerplate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AiCodeReviewTest {

    @Test
    public void CoedeReviewTest() throws Exception {
        //given
        String naem = "james";
        String age = "26";

        //when
        String info = naem + " " + age + "살";

        //then
        Assertions.assertTrue(info instanceof String);
    }
}

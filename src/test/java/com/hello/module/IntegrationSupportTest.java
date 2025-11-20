package com.hello.module;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.hello.boilerplate.support.config.AsyncTestConfig;

@SpringBootTest
@Transactional
@Import(AsyncTestConfig.class)
@ActiveProfiles("test")
public abstract class IntegrationSupportTest {

}

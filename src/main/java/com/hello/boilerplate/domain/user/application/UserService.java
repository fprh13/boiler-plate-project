package com.hello.boilerplate.domain.user.application;

import com.hello.boilerplate.domain.user.domain.UserRepository;
import com.hello.boilerplate.domain.user.presentation.dto.request.RegisterUser;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public Long register(final RegisterUser registerUser) {
        String encodedPassword = bCryptPasswordEncoder.encode(registerUser.password());
        return userRepository.save(registerUser.toEntity(encodedPassword)).getId();
    }
}

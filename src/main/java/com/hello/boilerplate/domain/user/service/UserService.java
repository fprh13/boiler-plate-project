package com.hello.boilerplate.domain.user.service;

import com.hello.boilerplate.domain.user.dto.UserRequestDto;
import com.hello.boilerplate.domain.user.repository.UserRepository;
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
    public void register(final UserRequestDto.Register request) {
        String encodedPassword = bCryptPasswordEncoder.encode(request.password());
        userRepository.save(request.toEntity(encodedPassword));
    }
}

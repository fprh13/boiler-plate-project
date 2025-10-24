package com.hello.boilerplate.domain.auth.service;

import com.hello.boilerplate.domain.auth.principal.UserDetailsImpl;
import com.hello.boilerplate.domain.user.entity.User;
import com.hello.boilerplate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByLoginId(username);
        if (user == null) {
            throw new UsernameNotFoundException(username + "은 없는 아이디입니다.");
        }
        return new UserDetailsImpl(user);
    }
}

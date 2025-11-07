package com.hello.boilerplate.domain.auth.resolver;

import com.hello.boilerplate.domain.auth.exception.AuthorizationErrorMessages;
import com.hello.boilerplate.domain.user.entity.User;
import com.hello.boilerplate.domain.user.repository.UserRepository;
import com.hello.boilerplate.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
public class AuthUserResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        return userRepository.findUserByLoginId(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException(AuthorizationErrorMessages.USER_NOT_FOUND_EXCEPTION));
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return User.class.isAssignableFrom(parameter.getParameterType());
    }
}

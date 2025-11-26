package com.hello.boilerplate.common.infrastructure.logging;

@FunctionalInterface
public interface ThrowableCallable<V> {
    V call() throws Throwable;
}

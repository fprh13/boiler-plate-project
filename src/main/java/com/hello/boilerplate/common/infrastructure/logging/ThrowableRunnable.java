package com.hello.boilerplate.common.infrastructure.logging;

@FunctionalInterface
public interface ThrowableRunnable {
    void run() throws Throwable;
}

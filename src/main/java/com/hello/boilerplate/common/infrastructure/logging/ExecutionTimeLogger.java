package com.hello.boilerplate.common.infrastructure.logging;

import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExecutionTimeLogger {

    public <V> V executeWithResult(ThrowableCallable<V> callable, String target) throws Throwable {
        StopWatch stopWatch = getStartedWatch();
        try {
            return callable.call();
        } finally {
            logTime(target, stopWatch);
        }
    }

    public void execute(ThrowableRunnable runnable, String target) throws Throwable {
        StopWatch stopWatch = getStartedWatch();
        try {
            runnable.run();
        } finally {
            logTime(target, stopWatch);
        }
    }

    private StopWatch getStartedWatch() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        return stopWatch;
    }

    private void logTime(String target, StopWatch stopWatch) {
        stopWatch.stop();
		long totalMillis = stopWatch.getTotalTimeMillis();

		if (totalMillis > 1_000L) {
			log.warn("{} 실행 시간 : {}ms", target, totalMillis);
		} else {
			log.info("{} 실행 시간 : {}ms", target, totalMillis);
		}
    }
}

package com.hello.boilerplate.common.infrastructure.logging.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.hello.boilerplate.common.infrastructure.logging.ExecutionTimeLogger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
public class ExecutionTimeAspect {

	private final ExecutionTimeLogger executionTimeLogger;

	@Around("com.hello.boilerplate.common.infrastructure.logging.aop.ExecutionTimePointcut.timeLogPointCut()")
	public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
		return executionTimeLogger.executeWithResult(
			joinPoint::proceed,
			generateTargetName(joinPoint)
		);
	}

	private String generateTargetName(ProceedingJoinPoint joinPoint) {
		String targetClassName = joinPoint.getTarget().getClass().getSimpleName();
		String methodName = joinPoint.getSignature().getName();
		return targetClassName + "." + methodName;
	}
}

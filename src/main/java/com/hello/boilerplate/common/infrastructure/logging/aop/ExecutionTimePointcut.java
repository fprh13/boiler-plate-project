package com.hello.boilerplate.common.infrastructure.logging.aop;

import org.aspectj.lang.annotation.Pointcut;

public class ExecutionTimePointcut {

	@Pointcut("execution(* com.hello.boilerplate..application..*.*(..))")
	private void applicationPackagePointCut() {}

	@Pointcut("@target(org.springframework.stereotype.Service)")
	private void servicePointCut() {}

	@Pointcut("applicationPackagePointCut() && servicePointCut()")
	public void timeLogPointCut() {}
}

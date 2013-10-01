package org.springframework.integration.aws.sns;

import org.springframework.integration.aws.sns.core.SnsExecutor;

public class SnsExecutorProxy {

	private final SnsExecutor snsExecutor;

	public SnsExecutorProxy(SnsExecutor snsExecutor) {
		super();
		this.snsExecutor = snsExecutor;
	}

	public String getTopicArn() {
		return snsExecutor.getTopicArn();
	}
}

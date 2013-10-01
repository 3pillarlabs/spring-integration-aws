package org.springframework.integration.aws.sqs;

import org.springframework.integration.aws.sqs.core.SqsExecutor;

public class SqsExecutorProxy {

	private final SqsExecutor sqsExecutor;

	public SqsExecutorProxy(SqsExecutor sqsExecutor) {
		super();
		this.sqsExecutor = sqsExecutor;
	}

	public String getQueueArn() {
		return sqsExecutor.getQueueArn();
	}

	public String getQueueUrl() {
		return sqsExecutor.getQueueUrl();
	}
}

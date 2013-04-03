package com.threepillar.labs.si.sns;

import com.threepillar.labs.si.sns.core.SnsExecutor;

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

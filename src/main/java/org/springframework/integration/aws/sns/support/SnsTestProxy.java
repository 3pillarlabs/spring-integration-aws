package org.springframework.integration.aws.sns.support;

import java.util.concurrent.BlockingQueue;

import org.springframework.integration.aws.sns.core.HttpEndpoint;


public interface SnsTestProxy {

	public abstract void setQueue(BlockingQueue<String> queue);

	public abstract void setHttpEndpoint(HttpEndpoint httpEndpoint);

	public abstract void dispatchMessage(String jsonPayload);

}
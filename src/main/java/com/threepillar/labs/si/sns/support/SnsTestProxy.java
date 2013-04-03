package com.threepillar.labs.si.sns.support;

import java.util.concurrent.BlockingQueue;

import com.threepillar.labs.si.sns.core.HttpEndpoint;

public interface SnsTestProxy {

	public abstract void setQueue(BlockingQueue<String> queue);

	public abstract void setHttpEndpoint(HttpEndpoint httpEndpoint);

	public abstract void dispatchMessage(String jsonPayload);

}
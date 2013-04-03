package com.threepillar.labs.si.test.support;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class BlockingQueueFactory {

	public static BlockingQueue<String> createBlockingQueue() {
		return new LinkedBlockingQueue<String>();
	}
}

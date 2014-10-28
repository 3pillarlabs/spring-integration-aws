package org.springframework.integration.aws.sqs.inbound;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.aws.sqs.SqsHeaders;
import org.springframework.integration.aws.sqs.core.SqsExecutor;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;


public class SqsSubscribableChannelAdapter extends MessageProducerSupport
		implements MessageSource<Object> {

	private final Log log = LogFactory
			.getLog(SqsSubscribableChannelAdapter.class);

	private int concurrentConsumers = 1;
	private boolean pollableMode;
	private int workerShutdownTimeout = 60;
	private ExecutorService pollerThreadExecutor;

	private volatile Boolean messageLoop;
	private volatile SqsExecutor sqsExecutor;
	private volatile ExecutorService workerThreadPool;

	public SqsSubscribableChannelAdapter() {
		super();
		this.messageLoop = false;
		this.pollableMode = false;
	}

	public void setSqsExecutor(SqsExecutor sqsExecutor) {
		this.sqsExecutor = sqsExecutor;
	}

	public void setConcurrentConsumers(int concurrentConsumers) {
		Assert.state(concurrentConsumers > 0,
				"'concurrentConsumers' must be greater than 0");
		this.concurrentConsumers = concurrentConsumers;
	}

	public void setPollableMode(boolean pollableMode) {
		this.pollableMode = pollableMode;
	}

	public void setWorkerShutdownTimeout(int workerShutdownTimeout) {
		this.workerShutdownTimeout = workerShutdownTimeout;
	}

	@Override
	public String getComponentType() {
		return "sqs:inbound-channel-adapter";
	}

	@Override
	protected void doStart() {
		super.doStart();
		if (pollableMode) {
			return;
		}
		messageLoop = true;
		if (concurrentConsumers > 1) {
			workerThreadPool = Executors
					.newFixedThreadPool(concurrentConsumers);
		} else {
			workerThreadPool = Executors.newCachedThreadPool();
		}
		pollerThreadExecutor = Executors.newSingleThreadExecutor();
		pollerThreadExecutor.execute(new Poller());

		log.info(getComponentName() + "[" + this.getClass().getName()
				+ "] started listening for messages...");
	}

	@Override
	protected void doStop() {
		super.doStop();
		if (pollableMode) {
			return;
		}
		messageLoop = false;
		try {
			workerThreadPool.shutdown();
			workerThreadPool.awaitTermination(workerShutdownTimeout,
					TimeUnit.SECONDS);
			pollerThreadExecutor.shutdown();
			pollerThreadExecutor.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn(e.getMessage(), e);
		}

		log.info(getComponentName() + "[" + this.getClass().getName()
				+ "] listener stopped");
	}

	@Override
	protected void onInit() {
		super.onInit();
		Assert.notNull(sqsExecutor, "'sqsExecutor' must not be null");

		log.info("Initialized " + getComponentName() + "["
				+ this.getClass().getName() + "]");
	}

	private class Poller implements Runnable {

		@Override
		public void run() {
			while (messageLoop) {
				final Message<?> message = sqsExecutor.poll();
				if (message != null) {
					try {
						workerThreadPool.execute(new Runnable() {

							@Override
							public void run() {
								sendMessage(message);
								sqsExecutor.acknowlegdeReceipt(message);
								log.debug("Message sent...");
							}
						});
					} catch (Throwable t) {
						log.warn(t.getMessage(), t);
					}
				}
			}
		}
	}

	@Override
	public Message<Object> receive() {
		Message<Object> message = null;
		Message<?> incoming = null;
		try {
			incoming = sqsExecutor.poll();
		} catch (Throwable t) {
			log.warn(t.getMessage(), t);
		}
		if (incoming != null) {
			Object payload = incoming.getPayload();
			final Message<?> callBackRef = incoming;
			message = MessageBuilder.withPayload(payload)
					.copyHeaders(incoming.getHeaders())
					.setHeader(SqsHeaders.ACK_CALLBACK, new Callable<String>() {

						@Override
						public String call() throws Exception {
							return sqsExecutor.acknowlegdeReceipt(callBackRef);
						}
					}).build();
		}
		return message;
	}

}

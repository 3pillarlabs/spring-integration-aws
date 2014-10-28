package org.springframework.integration.aws.sqs.channel;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.integration.aws.sqs.SqsHeaders;
import org.springframework.integration.aws.sqs.core.SqsExecutor;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.dispatcher.MessageDispatcher;
import org.springframework.integration.dispatcher.RoundRobinLoadBalancingStrategy;
import org.springframework.integration.dispatcher.UnicastingDispatcher;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.util.Assert;


public class SubscribableSqsChannel extends AbstractMessageChannel implements
		SubscribableChannel, SmartLifecycle, DisposableBean, PollableChannel {

	private final Log log = LogFactory.getLog(SubscribableSqsChannel.class);

	private int phase = 0;
	private int concurrentConsumers = 1;
	private boolean messageDriven;
	private int workerShutdownTimeout = 60;
	private ExecutorService pollerThreadExecutor;

	private volatile SqsExecutor sqsExecutor;
	private volatile MessageDispatcher dispatcher;
	private volatile Boolean messageLoop;
	private volatile ExecutorService workerThreadPool;
	private volatile ExecutorService senderThreadPool;

	public SubscribableSqsChannel() {
		super();
		this.messageLoop = false;
		this.messageDriven = true;
		this.dispatcher = new UnicastingDispatcher();
		((UnicastingDispatcher) dispatcher)
				.setLoadBalancingStrategy(new RoundRobinLoadBalancingStrategy());
	}

	public void setSqsExecutor(SqsExecutor sqsExecutor) {
		this.sqsExecutor = sqsExecutor;
	}

	public void setConcurrentConsumers(int concurrentConsumers) {
		Assert.state(concurrentConsumers > 0,
				"'concurrentConsumers' must be greater than 0");
		this.concurrentConsumers = concurrentConsumers;
	}

	public void setMessageDriven(boolean messageDriven) {
		this.messageDriven = messageDriven;
	}

	public void setWorkerShutdownTimeout(int workerShutdownTimeout) {
		this.workerShutdownTimeout = workerShutdownTimeout;
	}

	@Override
	public void start() {
		if (!messageDriven) {
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
	public void stop() {
		if (!messageDriven) {
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
	public boolean isRunning() {
		return messageLoop;
	}

	@Override
	public int getPhase() {
		return phase;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	@Override
	public void destroy() throws Exception {
		stop();
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

	@Override
	public boolean subscribe(MessageHandler handler) {
		return dispatcher.addHandler(handler);
	}

	@Override
	public boolean unsubscribe(MessageHandler handler) {
		return dispatcher.removeHandler(handler);
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
		Assert.notNull(sqsExecutor, "'sqsExecutor' must not be null");
		senderThreadPool = Executors.newCachedThreadPool();

		log.info("Initialized " + getComponentName() + "["
				+ this.getClass().getName() + "]");
	}

	private class Poller implements Runnable {

		@Override
		public void run() {
			while (messageLoop) {
				final Message<?> message = sqsExecutor.poll();
				if (message != null) {
					workerThreadPool.execute(new Runnable() {

						@Override
						public void run() {
							try {
								dispatcher.dispatch(message);
								sqsExecutor.acknowlegdeReceipt(message);
								log.debug("Message dispatched...");
							} catch (Throwable t) {
								log.warn(t.getMessage(), t);
							}
						}

					});
				}
			}
		}
	}

	@Override
	public Message<?> receive() {
		return this.receive(0);
	}

	@Override
	public Message<?> receive(long timeout) {
		Message<?> message = null;
		Message<?> incoming = null;
		try {
			if (timeout > 0) {
				incoming = sqsExecutor.poll(timeout);
			} else {
				incoming = sqsExecutor.poll();
			}
		} catch (Throwable t) {
			log.warn(t.getMessage(), t);
		}
		if (incoming != null) {
			final Message<?> callBackRef = incoming;
			message = MessageBuilder.fromMessage(incoming)
					.setHeader(SqsHeaders.ACK_CALLBACK, new Callable<String>() {

						@Override
						public String call() throws Exception {
							return sqsExecutor.acknowlegdeReceipt(callBackRef);
						}
					}).build();
		}
		return message;
	}

	@Override
	protected boolean doSend(final Message<?> message, long timeout) {
		Boolean sent = Boolean.FALSE;
		Callable<Boolean> task = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				sqsExecutor.executeOutboundOperation(message);
				return Boolean.TRUE;
			}
		};
		Future<Boolean> future = senderThreadPool.submit(task);
		try {
			if (timeout < 0) {
				sent = future.get();
			} else {
				sent = future.get(timeout, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e) {
			log.warn(e.getMessage(), e);
		} catch (Exception e) {
			throw new MessagingException(e.getMessage(), e);
		} finally {
			if (!sent && !future.isDone()) {
				future.cancel(true);
			}
		}

		return sent;
	}

}

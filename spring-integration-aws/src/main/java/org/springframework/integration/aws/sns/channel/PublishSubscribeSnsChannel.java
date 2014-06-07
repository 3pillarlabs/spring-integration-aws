package org.springframework.integration.aws.sns.channel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.integration.Message;
import org.springframework.integration.aws.sns.core.NotificationHandler;
import org.springframework.integration.aws.sns.core.SnsExecutor;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.dispatcher.BroadcastingDispatcher;
import org.springframework.integration.dispatcher.MessageDispatcher;
import org.springframework.util.Assert;


public class PublishSubscribeSnsChannel extends AbstractMessageChannel
		implements SubscribableChannel, SmartLifecycle, DisposableBean {

	private final Log log = LogFactory.getLog(PublishSubscribeSnsChannel.class);

	private int phase = 0;
	private boolean autoStartup = true;
	private boolean running = false;
	private NotificationHandler notificationHandler;

	private volatile SnsExecutor snsExecutor;
	private volatile MessageDispatcher dispatcher;

	public PublishSubscribeSnsChannel() {
		super();
		this.dispatcher = new BroadcastingDispatcher();
	}

	public void setSnsExecutor(SnsExecutor snsExecutor) {
		this.snsExecutor = snsExecutor;
	}

	@Override
	protected boolean doSend(Message<?> message, long timeout) {
		snsExecutor.executeOutboundOperation(message);
		return true;
	}

	@Override
	public void start() {
		snsExecutor.registerHandler(notificationHandler);
		running = true;
		log.info(getComponentName() + "[" + this.getClass().getName()
				+ "] started listening for messages...");
	}

	@Override
	public void stop() {
		if (running) {
			running = false;
			log.info(getComponentName() + "[" + this.getClass().getName()
					+ "] listener stopped");
		}
	}

	@Override
	public boolean isRunning() {
		return running;
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
		return autoStartup;
	}

	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
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
		Assert.notNull(snsExecutor, "'snsExecutor' must not be null");
		notificationHandler = new NotificationHandler() {

			@Override
			protected void dispatch(Message<?> message) {
				dispatcher.dispatch(message);
			}
		};

		log.info("Initialized SNS Channel: [" + getComponentName() + "]");

	}

	@Override
	public String getComponentType() {
		return "publish-subscribe-channel";
	}

}

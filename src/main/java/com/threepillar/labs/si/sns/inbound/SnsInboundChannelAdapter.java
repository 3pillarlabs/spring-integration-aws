package com.threepillar.labs.si.sns.inbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.Message;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.util.Assert;

import com.threepillar.labs.si.sns.core.NotificationHandler;
import com.threepillar.labs.si.sns.core.SnsExecutor;

/**
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SnsInboundChannelAdapter extends MessageProducerSupport {

	private final Log log = LogFactory.getLog(SnsInboundChannelAdapter.class);

	private volatile SnsExecutor snsExecutor;

	private NotificationHandler notificationHandler;

	public SnsInboundChannelAdapter() {
		super();
	}

	public void setSnsExecutor(SnsExecutor snsExecutor) {
		this.snsExecutor = snsExecutor;
	}

	@Override
	protected void doStart() {
		super.doStart();

		snsExecutor.registerHandler(notificationHandler);

		log.info("SNS inbound adapter: [" + getComponentName()
				+ "], started...");
	}

	@Override
	protected void doStop() {

		super.doStop();

		log.info("SNS inbound adapter: [" + getComponentName() + "], stopped.");
	}

	/**
	 * Check for mandatory attributes
	 */
	@Override
	protected void onInit() {
		super.onInit();

		Assert.notNull(snsExecutor, "snsExecutor must not be null.");

		this.notificationHandler = new NotificationHandler() {

			@Override
			protected void dispatch(Message<?> message) {
				sendMessage(message);
			}
		};

		log.info("SNS inbound adapter: [" + getComponentName()
				+ "], initialized...");
	}

	@Override
	public String getComponentType() {
		return "sns:inbound-channel-adapter";
	}

}

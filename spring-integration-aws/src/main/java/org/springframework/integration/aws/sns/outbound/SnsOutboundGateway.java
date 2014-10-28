package org.springframework.integration.aws.sns.outbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.integration.aws.sns.core.SnsExecutor;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SnsOutboundGateway extends AbstractReplyProducingMessageHandler
		implements DisposableBean {

	private final Log log = LogFactory.getLog(SnsOutboundGateway.class);

	private volatile SnsExecutor snsExecutor;
	private boolean producesReply = true; // false for outbound-channel-adapter,
											// true for outbound-gateway

	public SnsOutboundGateway() {
		super();
	}

	public void setSnsExecutor(SnsExecutor snsExecutor) {
		this.snsExecutor = snsExecutor;
	}

	@Override
	protected void doInit() {
		super.doInit();
		Assert.notNull(snsExecutor, "'snsExecutor' must not be null");

		log.info(getComponentName() + "[" + this.getClass().getName()
				+ "] ready to send messages...");
	}

	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {

		final Object result;

		result = this.snsExecutor.executeOutboundOperation(requestMessage);

		if (result == null || !producesReply) {
			return null;
		}

		return MessageBuilder.withPayload(result)
				.copyHeaders(requestMessage.getHeaders()).build();

	}

	/**
	 * If set to 'false', this component will act as an Outbound Channel
	 * Adapter. If not explicitly set this property will default to 'true'.
	 * 
	 * @param producesReply
	 *            Defaults to 'true'.
	 * 
	 */
	public void setProducesReply(boolean producesReply) {
		this.producesReply = producesReply;
	}

	@Override
	public void destroy() throws Exception {
		// no op
	}

}

package com.threepillar.labs.si.sqs.outbound;

import org.springframework.integration.Message;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

import com.threepillar.labs.si.sqs.core.SqsExecutor;

/**
 * 
 * @author Sayantam Dey
 * @since 1.0
 * 
 */
public class SqsOutboundGateway extends AbstractReplyProducingMessageHandler {

	private SqsExecutor sqsExecutor;
	private boolean producesReply = true; // false for
											// outbound-channel-adapter, true
											// for outbound-gateway

	public SqsOutboundGateway() {
		super();
	}

	public void setSqsExecutor(SqsExecutor sqsExecutor) {
		this.sqsExecutor = sqsExecutor;
	}

	@Override
	protected void onInit() {
		super.onInit();
		Assert.notNull(sqsExecutor, "'sqsExecutor' must not be null");
	}

	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {

		final Object result = this.sqsExecutor
				.executeOutboundOperation(requestMessage);

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

}

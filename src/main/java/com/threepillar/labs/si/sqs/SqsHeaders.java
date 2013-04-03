package com.threepillar.labs.si.sqs;

/**
 * Sqs adapter specific message headers.
 * 
 * @author Sayantam Dey
 * @since 1.0
 */
public abstract class SqsHeaders {

	private static final String PREFIX = "sqs_";

	public static final String ACK_CALLBACK = PREFIX + "ack_callback";

	public static final String MSG_RECEIPT_HANDLE = PREFIX
			+ "msg_receipt_handle";

	public static final String AWS_MESSAGE_ID = PREFIX + "aws_message_id";

	public static final String RECEIVE_COUNT = PREFIX
			+ "approximate_receive_count";

	public static final String SENT_AT = PREFIX + "sent_timestamp";

	public static final String FIRST_RECEIVED_AT = PREFIX + "first_received_at";

	public static final String SENDER_AWS_ID = PREFIX + "sender_id";

}

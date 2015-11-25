package org.springframework.integration.aws.sqs;

import static junit.framework.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.messaging.Message;
import org.springframework.integration.aws.JsonMessageMarshaller;
import org.springframework.integration.aws.MessageMarshaller;
import org.springframework.integration.aws.MessageMarshallerException;
import org.springframework.integration.aws.sqs.core.SqsExecutor;
import org.springframework.integration.support.MessageBuilder;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.util.Md5Utils;

@RunWith(JUnit4.class)
public class SqsExecutorTest {

	private MessageMarshaller messageMarshaller;
	private static AmazonSQS mockSQS = null;
	private SqsExecutor executor;

	@Before
	public void setup() {
		messageMarshaller = new JsonMessageMarshaller();
		if (mockSQS == null) {
			mockSQS = mock(AmazonSQS.class);
		}
		executor = new SqsExecutor();
		executor.setSqsClient(mockSQS);
		executor.setMessageMarshaller(messageMarshaller);
	}

	@Test
	public void incorrectMD5Test() throws MessageMarshallerException {

		String payload = "Hello, World";
		String messageBody = messageMarshaller.serialize(MessageBuilder
				.withPayload(payload).build());
		com.amazonaws.services.sqs.model.Message sqsMessage = new com.amazonaws.services.sqs.model.Message();
		sqsMessage.setBody(messageBody);
		sqsMessage.setMD5OfBody(messageBody);

		ReceiveMessageResult result = new ReceiveMessageResult();
		result.setMessages(Collections.singletonList(sqsMessage));
		when(mockSQS.receiveMessage(any(ReceiveMessageRequest.class)))
				.thenReturn(result);

		Message<?> recvMessage = executor.poll();
		assertNull("No message since MD5 checksum failed", recvMessage);
	}

	@Test
	public void correctMD5Test() throws Exception {

		String payload = "Hello, World";
		String messageBody = messageMarshaller.serialize(MessageBuilder
				.withPayload(payload).build());
		com.amazonaws.services.sqs.model.Message sqsMessage = new com.amazonaws.services.sqs.model.Message();
		sqsMessage.setBody(messageBody);
		sqsMessage.setMD5OfBody(new String(Hex.encodeHex(Md5Utils
				.computeMD5Hash(messageBody.getBytes("UTF-8")))));

		ReceiveMessageResult result = new ReceiveMessageResult();
		result.setMessages(Collections.singletonList(sqsMessage));
		when(mockSQS.receiveMessage(any(ReceiveMessageRequest.class)))
				.thenReturn(result);

		Message<?> recvMessage = executor.poll();
		assertNotNull("message is not null", recvMessage);

		Message<?> enclosed = messageMarshaller
				.deserialize((String) recvMessage.getPayload());
		String recvPayload = (String) enclosed.getPayload();
		assertEquals("payload must match", payload, recvPayload);
	}
}

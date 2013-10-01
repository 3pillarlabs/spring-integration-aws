package org.springframework.integration.aws.sqs;

import static junit.framework.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.integration.Message;
import org.springframework.integration.aws.MessagePacket;
import org.springframework.integration.aws.sqs.core.SqsExecutor;
import org.springframework.integration.support.MessageBuilder;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.util.Md5Utils;

@RunWith(JUnit4.class)
public class SqsExecutorTest {

	@Test
	public void incorrectMD5Test() {

		AmazonSQS mockSQS = mock(AmazonSQS.class);

		SqsExecutor executor = new SqsExecutor();
		executor.setSqsClient(mockSQS);

		String payload = "Hello, World";
		MessagePacket packet = new MessagePacket(MessageBuilder.withPayload(
				payload).build());
		String messageBody = packet.toJSON();
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

		AmazonSQS mockSQS = mock(AmazonSQS.class);

		SqsExecutor executor = new SqsExecutor();
		executor.setSqsClient(mockSQS);

		String payload = "Hello, World";
		MessagePacket packet = new MessagePacket(MessageBuilder.withPayload(
				payload).build());
		String messageBody = packet.toJSON();
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
		MessagePacket recvPacket = MessagePacket.fromJSON((String) recvMessage
				.getPayload());

		Message<?> enclosed = recvPacket.assemble();
		String recvPayload = (String) enclosed.getPayload();
		assertEquals("payload must match", payload, recvPayload);
	}
}

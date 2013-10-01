package org.springframework.integration.aws;

import static junit.framework.Assert.*;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.aws.MessagePacket;
import org.springframework.integration.aws.support.TestPojo;
import org.springframework.integration.support.MessageBuilder;


@RunWith(JUnit4.class)
public class MessagePacketTest {

	@Test
	public void testSerialization() {
		TestPojo pojo = new TestPojo();
		pojo.setName("John Doe");
		pojo.setEmail("user@example.com");
		MessagePacket packet = new MessagePacket(MessageBuilder.withPayload(
				pojo).build());

		MessagePacket otherPacket = MessagePacket.fromJSON(packet.toJSON());
		TestPojo otherPojo = (TestPojo) otherPacket.assemble().getPayload();
		assertEquals(pojo, otherPojo);
	}

	@Test
	public void testMessageHeaders() {
		TestPojo pojo = new TestPojo();
		pojo.setName("John Doe");
		pojo.setEmail("user@example.com");
		long expiryTime = new Date().getTime();
		Long[] ary = new Long[] { 1L, 2L, 3L };
		Message<String> sent = MessageBuilder.withPayload("Hello, World")
				.setCorrelationId("ABC").setExpirationDate(expiryTime)
				.setPriority(2).setSequenceNumber(2).setSequenceSize(5)
				.setHeader("fubar", "FUBAR").setHeader("pojo", pojo)
				.setHeader("ary", ary).build();

		MessagePacket original = new MessagePacket(sent);

		MessagePacket other = MessagePacket.fromJSON(original.toJSON());
		Message<?> received = other.assemble();

		assertEquals(sent.getPayload(), received.getPayload());
		MessageHeaders sentHeaders = sent.getHeaders();
		MessageHeaders recvHeaders = received.getHeaders();
		assertEquals(sentHeaders.getCorrelationId(),
				recvHeaders.getCorrelationId());
		assertEquals(new Long(expiryTime), recvHeaders.getExpirationDate());
		assertEquals(sentHeaders.getPriority(), recvHeaders.getPriority());
		assertEquals(sentHeaders.getSequenceNumber(),
				recvHeaders.getSequenceNumber());
		assertEquals(sentHeaders.getSequenceSize(),
				recvHeaders.getSequenceSize());
		assertEquals(sentHeaders.get("fubar", String.class),
				recvHeaders.get("fubar", String.class));
		assertEquals(pojo, recvHeaders.get("pojo", TestPojo.class));
		assertTrue(Arrays.deepEquals(ary, (Object[]) recvHeaders.get("ary")));
	}

	@Test
	public void testPrimitivesPayload() {

		Integer i = new Integer(1);
		MessagePacket packet = new MessagePacket(MessageBuilder.withPayload(i)
				.build());
		MessagePacket otherPacket = MessagePacket.fromJSON(packet.toJSON());
		Integer j = (Integer) otherPacket.assemble().getPayload();

		assertEquals(i, j);
	}

	@Test
	public void testArrayofPrimitivesPayload() {

		Integer[] aryIn = new Integer[] { 1, 2 };

		MessagePacket packet = new MessagePacket(MessageBuilder.withPayload(
				aryIn).build());
		MessagePacket otherPacket = MessagePacket.fromJSON(packet.toJSON());
		Integer[] aryOut = (Integer[]) otherPacket.assemble().getPayload();

		assertTrue(Arrays.deepEquals(aryIn, aryOut));
	}

	@Test
	public void testArrayOfPojo() {

		TestPojo[] aryIn = new TestPojo[2];
		aryIn[0] = new TestPojo();
		aryIn[0].setName("John Doe");
		aryIn[0].setEmail("user@example.com");
		aryIn[1] = new TestPojo();
		aryIn[1].setName("Lionel Messi");
		aryIn[1].setEmail("messi@example.com");

		MessagePacket packet = new MessagePacket(MessageBuilder.withPayload(
				aryIn).build());
		MessagePacket otherPacket = MessagePacket.fromJSON(packet.toJSON());

		TestPojo[] aryOut = (TestPojo[]) otherPacket.assemble().getPayload();

		assertTrue(Arrays.deepEquals(aryIn, aryOut));

	}

	@Test
	public void testSimpleMessage() {

		String simpleMessage = "Hello World";
		MessagePacket packet = MessagePacket.fromJSON(simpleMessage);

		assertEquals(simpleMessage, packet.assemble().getPayload());
	}

}

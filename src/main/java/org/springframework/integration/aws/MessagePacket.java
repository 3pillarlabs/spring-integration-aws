package org.springframework.integration.aws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.MessagingException;
import org.springframework.integration.support.MessageBuilder;

public class MessagePacket {

	private static final String HEADER_VALUE_KEY = "value";
	private static final String HEADER_CLAZZ_KEY = "clazz";
	private static final String PAYLOAD_KEY = "payload";
	private static final String HEADERS_KEY = "headers";
	private static final String PROPERTIES_KEY = "properties";

	private Object payload;
	private MessageHeaders messageHeaders;
	private JSONObject headers;
	private JSONObject properties;

	public MessagePacket(Message<?> message) {
		super();
		this.payload = message.getPayload();
		this.messageHeaders = message.getHeaders();
	}

	private MessagePacket(String json) {
		try {
			JSONObject jsonObject = null;
			Class<?> payloadClass = null;
			try {
				jsonObject = new JSONObject(json);
			} catch (JSONException e) {
				// plain text
				payloadClass = String.class;
				this.payload = json;
			}
			if (payloadClass == null) {
				payloadClass = Class.forName(jsonObject
						.getString("payloadClazz"));

				String content = null;
				if (payloadClass.equals(String.class)) {
					this.payload = jsonObject.getString(PAYLOAD_KEY);

				} else if (payloadClass.isArray()) {
					content = jsonObject.getJSONArray(PAYLOAD_KEY).toString();

				} else if (Number.class.isAssignableFrom(payloadClass)) {
					this.payload = jsonObject.get(PAYLOAD_KEY);

				} else {
					JSONObject payloadJSONObject = jsonObject
							.getJSONObject(PAYLOAD_KEY);
					content = payloadJSONObject.toString();
				}

				if (content != null) {
					ObjectMapper mapper = new ObjectMapper();
					this.payload = mapper.readValue(content, payloadClass);
				}

				properties = jsonObject.getJSONObject(PROPERTIES_KEY);
				headers = jsonObject.getJSONObject(HEADERS_KEY);
			}

		} catch (Exception e) {
			throw new MessagingException(e.getMessage(), e);
		}
	}

	public Message<?> assemble() {
		MessageBuilder<Object> builder = null;
		try {
			builder = MessageBuilder.withPayload(payload);
			if (properties != null) {
				setProperties(builder);
			}
			if (headers != null) {
				copyHeaders(builder);
			}
		} catch (Exception e) {
			throw new MessagingException(e.getMessage(), e);
		}
		return builder.build();
	}

	public String toJSON() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			final Map<String, String> messageProperties = extractMessageProperties();
			final Map<String, String> headersMap = convertHeadersToMap();
			return mapper.writeValueAsString(new Object() {

				@SuppressWarnings("unused")
				public Object getPayload() {
					return payload;
				}

				@SuppressWarnings("unused")
				public String getPayloadClazz() {
					return payload.getClass().getName();
				}

				@SuppressWarnings("unused")
				public Map<String, String> getHeaders() {
					return headersMap;
				}

				@SuppressWarnings("unused")
				public Map<String, String> getProperties() {
					return messageProperties;
				}
			});
		} catch (Exception e) {
			throw new MessagingException(e.getMessage(), e);
		}
	}

	private Map<String, String> extractMessageProperties() {
		Map<String, String> map = new HashMap<String, String>();
		if (messageHeaders.getCorrelationId() != null) {
			map.put(HeaderKeys.CORRELATION_ID, messageHeaders
					.getCorrelationId().toString());
		}
		if (messageHeaders.getExpirationDate() != null) {
			map.put(HeaderKeys.EXPIRATION_DATE, messageHeaders
					.getExpirationDate().toString());
		}
		if (messageHeaders.getPriority() != null) {
			map.put(HeaderKeys.PRIORITY, messageHeaders.getPriority()
					.toString());
		}
		if (messageHeaders.getSequenceNumber() != null) {
			map.put(HeaderKeys.SEQUENCE_NUMBER, messageHeaders
					.getSequenceNumber().toString());
		}
		if (messageHeaders.getSequenceSize() != null) {
			map.put(HeaderKeys.SEQUENCE_SIZE, messageHeaders.getSequenceSize()
					.toString());
		}
		return map;
	}

	private void setProperties(MessageBuilder<Object> builder)
			throws JSONException {
		if (properties.has(HeaderKeys.CORRELATION_ID)) {
			builder.setCorrelationId(properties
					.getString(HeaderKeys.CORRELATION_ID));
		}
		if (properties.has(HeaderKeys.EXPIRATION_DATE)) {
			builder.setExpirationDate(Long.valueOf(properties
					.getString(HeaderKeys.EXPIRATION_DATE)));
		}
		if (properties.has(HeaderKeys.PRIORITY)) {
			builder.setPriority(Integer.valueOf(properties
					.getString(HeaderKeys.PRIORITY)));
		}
		if (properties.has(HeaderKeys.SEQUENCE_NUMBER)) {
			builder.setSequenceNumber(Integer.valueOf(properties
					.getString(HeaderKeys.SEQUENCE_NUMBER)));
		}
		if (properties.has(HeaderKeys.SEQUENCE_SIZE)) {
			builder.setSequenceSize(Integer.valueOf(properties
					.getString(HeaderKeys.SEQUENCE_SIZE)));
		}
	}

	private Map<String, String> convertHeadersToMap() throws JSONException,
			IOException {

		Map<String, String> map = new HashMap<String, String>();
		JSONObject headerObject = new JSONObject();
		ObjectMapper mapper = new ObjectMapper();
		for (Map.Entry<String, Object> element : messageHeaders.entrySet()) {
			String key = element.getKey();
			if (key.equals("id") || key.equals("timestamp")) {
				// these never get overwritten so, no point storing them
				continue;
			}
			Object value = element.getValue();
			headerObject.put(HEADER_CLAZZ_KEY, value.getClass().getName());
			if (!String.class.isInstance(value)
					&& !Number.class.isInstance(value)) {
				headerObject.put(HEADER_VALUE_KEY,
						mapper.writeValueAsString(value));
			} else {
				headerObject.put(HEADER_VALUE_KEY, value);
			}
			map.put(key, headerObject.toString());
		}
		return map;
	}

	private void copyHeaders(MessageBuilder<Object> builder)
			throws JSONException, ClassNotFoundException, IOException {

		Map<String, Object> map = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		String[] fieldNames = JSONObject.getNames(headers);
		if (fieldNames == null) {
			return;
		}
		for (String fieldName : fieldNames) {
			JSONObject headerObject = new JSONObject(
					headers.getString(fieldName));
			Class<?> clazz = Class.forName(headerObject
					.getString(HEADER_CLAZZ_KEY));
			if (String.class.equals(clazz)) {
				map.put(fieldName, headerObject.getString(HEADER_VALUE_KEY));
			} else if (Number.class.isAssignableFrom(clazz)) {
				map.put(fieldName, headerObject.get(HEADER_VALUE_KEY));
			} else {
				String source = headerObject.getString(HEADER_VALUE_KEY);
				map.put(fieldName, mapper.readValue(source, clazz));
			}
		}
		builder.copyHeaders(map);
	}

	@Override
	public String toString() {
		return toJSON();
	}

	public static MessagePacket fromJSON(String json) {
		return new MessagePacket(json);
	}

	private abstract class HeaderKeys {
		public static final String CORRELATION_ID = "CorrelationId";
		private static final String EXPIRATION_DATE = "ExpirationDate";
		private static final String PRIORITY = "Priority";
		private static final String SEQUENCE_NUMBER = "SequenceNumber";
		private static final String SEQUENCE_SIZE = "SequenceSize";
	}
}

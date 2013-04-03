package com.threepillar.labs.si.sns.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.RequestMethod;

import com.amazonaws.services.sns.util.SignatureChecker;

public class HttpEndpoint implements HttpRequestHandler {

	private static final String SUBSCRIPTION_CONFIRMATION = "SubscriptionConfirmation";
	private static final String NOTIFICATION = "Notification";
	private static final String SNS_MESSAGE_TYPE = "x-amz-sns-message-type";

	private final Log log = LogFactory.getLog(HttpEndpoint.class);

	private boolean passThru;
	private volatile NotificationHandler notificationHandler;

	public HttpEndpoint(NotificationHandler notificationHandler) {
		this();
		this.notificationHandler = notificationHandler;
	}

	public HttpEndpoint() {
		super();
		this.passThru = false;
	}

	public void setNotificationHandler(NotificationHandler notificationHandler) {
		this.notificationHandler = notificationHandler;
	}

	public void setPassThru(boolean passThru) {
		this.passThru = passThru;
	}

	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		boolean unprocessable = true;
		if (request.getMethod().equals(RequestMethod.POST.toString())) {

			if (SUBSCRIPTION_CONFIRMATION.equals(request
					.getHeader(SNS_MESSAGE_TYPE))) {

				unprocessable = false;
				handleSubscriptionConfirmation(request, response);

			} else if (NOTIFICATION.equals(request.getHeader(SNS_MESSAGE_TYPE))) {

				if (notificationHandler != null) {
					unprocessable = false;
					handleNotification(request, response);
				}
			}

		}
		if (unprocessable) {
			log.warn("Unprocessable request: "
					+ request.getRequestURL().toString());
			response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
		}
	}

	private void handleSubscriptionConfirmation(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		try {
			String source = readBody(request);
			log.debug("Subscription confirmation:\n" + source);
			JSONObject confirmation = new JSONObject(source);

			if (validSignature(source, confirmation)) {
				URL subscribeUrl = new URL(
						confirmation.getString("SubscribeURL"));
				HttpURLConnection http = (HttpURLConnection) subscribeUrl
						.openConnection();
				http.setDoOutput(false);
				http.setDoInput(true);
				StringBuffer buffer = new StringBuffer();
				byte[] buf = new byte[4096];
				while ((http.getInputStream().read(buf)) >= 0) {
					buffer.append(new String(buf));
				}
				log.debug("SubscribeURL response:\n" + buffer.toString());
			}
			response.setStatus(HttpServletResponse.SC_OK);

		} catch (JSONException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	private void handleNotification(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		try {

			String source = readBody(request);
			log.debug("Message received:\n" + source);
			JSONObject notification = new JSONObject(source);
			if (passThru || validSignature(source, notification)) {
				notificationHandler.onNotification(notification
						.getString("Message"));
			}
			response.setStatus(HttpServletResponse.SC_ACCEPTED);
		} catch (JSONException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	private String readBody(HttpServletRequest request) throws IOException {

		StringBuffer buffer = new StringBuffer(request.getContentLength());
		String line = null;
		BufferedReader reader = request.getReader();
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}

		return buffer.toString();
	}

	private boolean validSignature(String source, JSONObject jsonObject)
			throws JSONException, IOException {

		PublicKey pubKey = fetchPubKey(jsonObject.getString("SigningCertURL"));
		SignatureChecker signatureChecker = new SignatureChecker();

		return signatureChecker.verifyMessageSignature(source, pubKey);
	}

	private PublicKey fetchPubKey(String signingCertURL) throws IOException {

		try {
			URL url = new URL(signingCertURL);
			InputStream inStream = url.openStream();
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf
					.generateCertificate(inStream);
			inStream.close();

			return cert.getPublicKey();

		} catch (Exception e) {
			throw new IOException(e);
		}
	}

}

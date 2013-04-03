package com.threepillar.labs.si.aws;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

public class SimpleAWSCredentialsProvider implements AWSCredentialsProvider,
		InitializingBean {

	private String accessKey;
	private String secretKey;

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	@Override
	public AWSCredentials getCredentials() {
		return new BasicAWSCredentials(accessKey, secretKey);
	}

	@Override
	public void refresh() {
		// no op
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasLength(accessKey, "accessKey must not be null or empty");
		Assert.hasLength(secretKey, "secretKey must not be null or empty");
	}

}

package org.springframework.integration.aws.sns;

/**
 * Sns adapter specific message headers.
 *
 * @author Sayantam Dey
 * @since 1.0
 */
public class SnsHeaders {

	private static final String PREFIX = "sns_";

	public static final String EXAMPLE = PREFIX + "example_";

	/** Noninstantiable utility class */
	private SnsHeaders() {
		throw new AssertionError();
	}

}

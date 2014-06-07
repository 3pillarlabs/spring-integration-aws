package org.springframework.integration.aws;

import java.util.Set;

/**
 * Common permission scheme for SNS and SQS.
 * 
 * @author Sayantam Dey
 * @since 2.0.0
 * 
 */
public class Permission {

	/**
	 * The unique identification of the permission. See
	 * {@link com.amazonaws.services.sqs.model.AddPermissionRequest#setLabel(String)}
	 * for constraints.
	 */
	private String label;

	/**
	 * AWS account numbers to be given the permission.
	 */
	private Set<String> awsAccountIds;

	/**
	 * The actions to be allowed for the specified AWS accounts
	 */
	private Set<String> actions;

	public Permission() {
	}

	public Permission(String label, Set<String> aWSAccountIds,
			Set<String> actions) {
		this();
		this.label = label;
		this.awsAccountIds = aWSAccountIds;
		this.actions = actions;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Set<String> getAwsAccountIds() {
		return awsAccountIds;
	}

	public void setAwsAccountIds(Set<String> awsAccountIds) {
		this.awsAccountIds = awsAccountIds;
	}

	public Set<String> getActions() {
		return actions;
	}

	public void setActions(Set<String> actions) {
		this.actions = actions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((awsAccountIds == null) ? 0 : awsAccountIds.hashCode());
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Permission other = (Permission) obj;
		if (awsAccountIds == null) {
			if (other.awsAccountIds != null)
				return false;
		} else if (!awsAccountIds.equals(other.awsAccountIds))
			return false;
		if (actions == null) {
			if (other.actions != null)
				return false;
		} else if (!actions.equals(other.actions))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

}

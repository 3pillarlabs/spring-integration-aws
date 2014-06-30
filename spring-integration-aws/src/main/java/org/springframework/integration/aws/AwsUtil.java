package org.springframework.integration.aws;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.integration.MessagingException;

public abstract class AwsUtil {

	public abstract static class AddPermissionHandler {

		public abstract void execute(Permission p);
	}

	public static void addPermissions(Map<String, String> attributes,
			Set<Permission> permissions, AddPermissionHandler handler) {

		String policyStr = attributes.get("Policy");
		Set<String> existingLabels = new HashSet<String>();
		if (policyStr != null && policyStr.isEmpty() == false) {
			try {
				JSONObject policyJSON = new JSONObject(policyStr);
				JSONArray statements = policyJSON.getJSONArray("Statement");
				for (int i = 0; i < statements.length(); i++) {
					existingLabels.add(statements.getJSONObject(i).getString(
							"Sid"));
				}
			} catch (JSONException e) {
				throw new MessagingException(e.getMessage(), e);
			}
		}
		for (Permission p : permissions) {
			if (existingLabels.contains(p.getLabel()) == false) {
				handler.execute(p);
			}
		}

	}

}

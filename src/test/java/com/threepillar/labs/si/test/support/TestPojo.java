package com.threepillar.labs.si.test.support;

public class TestPojo {

	private String name;
	private String email;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public boolean equals(Object obj) {
		boolean same = false;
		if (this.getClass().isInstance(obj)) {
			TestPojo other = (TestPojo) obj;
			same = ((this.name == null || this.name.equals(other.name)) && (this.email == null || this.email
					.equals(other.email)));
		}
		return same;
	}

}

package com.external;

public class ExternalClass {
	public void unpureMethodUndefinedInConfig() {
	}

	public void unpureMethodWhitelistedInConfig() {
	}

	public static void main(String[] args) {
		for (String key : System.getProperties().stringPropertyNames()) {
			System.out.println(key + ": " + System.getProperty(key));
		}
	}
}

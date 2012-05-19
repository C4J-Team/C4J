package com.external;

public class ExternalClass {
	public static int STATIC_FIELD;

	public void methodUndefinedInConfig() {
	}

	public void pureMethodWhitelistedInConfig() {
	}

	public void unpureMethodBlacklistedInConfig() {
	}

	public static void staticMethodUndefinedInConfig() {
	}

	public static void pureStaticMethodWhitelistedInConfig() {
	}

	public static void unpureStaticMethodBlacklistedInConfig() {
	}
}

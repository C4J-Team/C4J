package de.vksi.c4j.internal.configuration;

public interface LocalConfigurationCallback {
	void scanExternalContracts(XmlLocalConfiguration xmlLocalConfiguration) throws Exception;
}
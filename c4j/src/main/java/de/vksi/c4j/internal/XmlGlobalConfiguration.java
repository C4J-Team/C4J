package de.vksi.c4j.internal;

import de.vksi.c4j.internal.configuration.C4JGlobal;

public class XmlGlobalConfiguration {
	private final C4JGlobal xmlConfiguration;

	public XmlGlobalConfiguration(C4JGlobal xmlConfiguration) {
		this.xmlConfiguration = xmlConfiguration;
	}

	public boolean writeTransformedClasses() {
		return xmlConfiguration.getWriteTransformedClasses().isValue();
	}

	public String writeTransformedClassesDirectory() {
		return xmlConfiguration.getWriteTransformedClasses().getDirectory();
	}
}

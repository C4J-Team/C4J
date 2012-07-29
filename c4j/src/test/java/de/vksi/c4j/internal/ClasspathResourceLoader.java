package de.vksi.c4j.internal;

import java.io.InputStream;
import java.net.URL;

import org.junit.rules.ExternalResource;

public class ClasspathResourceLoader extends ExternalResource {
	public InputStream loadStream(String path) {
		return getClass().getResourceAsStream("/" + path);
	}

	public URL getUrl(String path) {
		return getClass().getResource("/" + path);
	}
}

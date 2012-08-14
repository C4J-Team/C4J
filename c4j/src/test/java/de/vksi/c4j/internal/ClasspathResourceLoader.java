package de.vksi.c4j.internal;

import java.io.InputStream;
import java.net.URL;

public class ClasspathResourceLoader {
	public InputStream loadStream(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

	public URL getUrl(String path) {
		return getClass().getClassLoader().getResource(path);
	}
}

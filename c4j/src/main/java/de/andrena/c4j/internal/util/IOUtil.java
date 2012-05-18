package de.andrena.c4j.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtil {
	public byte[] readInputStream(InputStream stream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int read;
		byte[] data = new byte[4096];
		while ((read = stream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, read);
		}
		buffer.flush();
		return buffer.toByteArray();
	}
}

package de.vksi.c4j.internal.classfile;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.LoaderClassPath;

import org.junit.Before;
import org.junit.Test;

public class ClassFilePoolTest {
	private ClassFilePool pool;
	private ClassPool poolMock;

	@Before
	public void setUp() {
		poolMock = mock(ClassPool.class);
		pool = new ClassFilePool() {

			@Override
			protected ClassPool createClassPool() {
				return poolMock;
			};
		};
	}

	@Test
	public void testUpdateClassPathWithClassLoader() {
		ClassLoader classLoader = mock(ClassLoader.class);
		pool.addClassLoader(classLoader);
		verify(poolMock).insertClassPath(any(LoaderClassPath.class));
	}

	@Test
	public void testUpdateClassPathWithByteArray() {
		pool.addClassFile(null, null);
		verify(poolMock).insertClassPath(any(ByteArrayClassPath.class));
	}
}

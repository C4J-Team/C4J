package de.vksi.c4j.internal;

import static de.vksi.c4j.internal.XmlConfigurationManager.C4J_GLOBAL_XML;
import static de.vksi.c4j.internal.XmlConfigurationManager.C4J_LOCAL_XML;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.vksi.c4j.internal.configuration.DefaultPreconditionType;

public class XmlConfigurationManagerTest {
	private static final String C4J_GLOBAL_WRITE_TRUE_XML = "c4j-global_write-true.xml";
	private static final String C4J_GLOBAL_WRITE_FALSE_XML = "c4j-global_write-false.xml";
	private static final String C4J_LOCAL_DEFAULT_XML = "c4j-local_default.xml";
	private static final String C4J_LOCAL_SAME_PACKAGE_XML = "c4j-local_same-package.xml";
	private static final String C4J_LOCAL_DIFFERENT_PACKAGE_XML = "c4j-local_different-package.xml";

	private final class AnswerWithNewInputStream implements Answer<InputStream> {
		private final String realPath;

		private AnswerWithNewInputStream(String realPath) {
			this.realPath = realPath;
		}

		@Override
		public InputStream answer(InvocationOnMock invocation) throws Throwable {
			return classpathResourceLoader.loadStream(realPath);
		}
	}

	private ClasspathResourceLoader classpathResourceLoader = new ClasspathResourceLoader();
	private XmlConfigurationManager manager;

	@Before
	public void before() {
		manager = new XmlConfigurationManager();
	}

	@Test
	public void testDefaultLocalConfiguration() {
		XmlLocalConfiguration configuration = manager.getConfiguration(XmlConfigurationManager.class);
		assertThat(configuration.getDefaultPrecondition(), is(DefaultPreconditionType.UNDEFINED));
		assertThat(configuration.isPureSkipInvariants(), is(true));
		assertThat(configuration.isPureValidate(), is(false));
		assertThat(configuration.isStrengtheningPreconditionsAllowed(), is(false));
	}

	@Test
	public void testDefaultGlobalConfiguration() {
		XmlGlobalConfiguration globalConfig = manager.getGlobalConfiguration();
		assertThat(globalConfig.writeTransformedClasses(), is(false));
		assertThat(globalConfig.writeTransformedClassesDirectory(), is("."));
		assertThat(globalConfig.getContractViolationActions().size(), is(0));
	}

	@Test
	public void testRegisterLocalConfiguration() throws Exception {
		ClassLoader classLoader = createClassLoaderMock(C4J_LOCAL_XML, C4J_LOCAL_DEFAULT_XML);
		manager.registerClassLoader(classLoader);
		XmlLocalConfiguration configuration = manager.getConfiguration(XmlConfigurationManager.class);
		assertThat(configuration.isPureValidate(), is(true));
		assertThat(configuration.isPureSkipInvariants(), is(true));
	}

	@Test
	public void testRegisterMultipleLocalConfigurations_SamePackage() throws Exception {
		ClassLoader classLoader = createClassLoaderMock(C4J_LOCAL_XML, C4J_LOCAL_DEFAULT_XML,
				C4J_LOCAL_SAME_PACKAGE_XML);
		manager.registerClassLoader(classLoader);
		XmlLocalConfiguration configuration = manager.getConfiguration(XmlConfigurationManager.class);
		assertThat(configuration.isPureValidate(), is(configuration.isPureSkipInvariants()));
	}

	@Test
	public void testRegisterMultipleLocalConfigurations_DifferentPackage() throws Exception {
		ClassLoader classLoader = createClassLoaderMock(C4J_LOCAL_XML, C4J_LOCAL_DEFAULT_XML,
				C4J_LOCAL_DIFFERENT_PACKAGE_XML);
		manager.registerClassLoader(classLoader);
		XmlLocalConfiguration configuration = manager.getConfiguration(XmlConfigurationManager.class);
		assertThat(configuration.isPureValidate(), is(true));
		assertThat(configuration.isPureSkipInvariants(), is(true));
		XmlLocalConfiguration configurationForDifferentPackage = manager
				.getConfiguration("com.external.DifferentClass");
		assertThat(configurationForDifferentPackage.isPureValidate(), is(false));
		assertThat(configurationForDifferentPackage.isPureSkipInvariants(), is(false));
	}

	private ClassLoader createClassLoaderMock(String mockPath, final String... realPaths) throws IOException {
		ClassLoader classLoader = mock(ClassLoader.class);
		when(classLoader.getResourceAsStream(eq(mockPath))).thenAnswer(new AnswerWithNewInputStream(realPaths[0]));
		Vector<URL> xmlConfigs = new Vector<URL>();
		for (String realPath : realPaths) {
			xmlConfigs.add(classpathResourceLoader.getUrl(realPath));
		}
		when(classLoader.getResources(eq(mockPath))).thenReturn(xmlConfigs.elements());
		when(classLoader.getResource(eq(mockPath))).thenReturn(classpathResourceLoader.getUrl(realPaths[0]));
		return classLoader;
	}

	@Test
	public void testRegisterGlobalConfiguration() throws Exception {
		ClassLoader classLoader = createClassLoaderMock(C4J_GLOBAL_XML, C4J_GLOBAL_WRITE_TRUE_XML);
		manager.registerClassLoader(classLoader);
		XmlGlobalConfiguration globalConfig = manager.getGlobalConfiguration();
		assertThat(globalConfig.getContractViolationActions().size(), is(0));
		assertThat(globalConfig.writeTransformedClasses(), is(true));
	}

	@Test
	public void testRegisterGlobalConfigurations_Twice() throws Exception {
		ClassLoader classLoader = createClassLoaderMock(C4J_GLOBAL_XML, C4J_GLOBAL_WRITE_TRUE_XML);
		manager.registerClassLoader(classLoader);
		manager.registerClassLoader(classLoader);
		XmlGlobalConfiguration globalConfig = manager.getGlobalConfiguration();
		assertThat(globalConfig.getContractViolationActions().size(), is(0));
		assertThat(globalConfig.writeTransformedClasses(), is(true));
	}

	@Test
	public void testRegisterGlobalConfigurations_WithDifferentClassLoaders() throws Exception {
		ClassLoader classLoader = createClassLoaderMock(C4J_GLOBAL_XML, C4J_GLOBAL_WRITE_TRUE_XML);
		ClassLoader classLoader2 = createClassLoaderMock(C4J_GLOBAL_XML, C4J_GLOBAL_WRITE_FALSE_XML);
		manager.registerClassLoader(classLoader);
		manager.registerClassLoader(classLoader2);
		XmlGlobalConfiguration globalConfig = manager.getGlobalConfiguration();
		assertThat(globalConfig.getContractViolationActions().size(), is(0));
		assertThat(globalConfig.writeTransformedClasses(), is(true));
	}
}

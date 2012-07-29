package de.vksi.c4j.internal;

import static de.vksi.c4j.internal.XMLConfigurationManager.C4J_GLOBAL_XML;
import static de.vksi.c4j.internal.XMLConfigurationManager.C4J_LOCAL_XML;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.vksi.c4j.internal.configuration.C4JGlobal;
import de.vksi.c4j.internal.configuration.C4JLocal.Configuration;
import de.vksi.c4j.internal.configuration.DefaultPreconditionType;

public class XMLConfigurationManagerTest {
	@Rule
	public ClasspathResourceLoader classpathResourceLoader = new ClasspathResourceLoader();
	private XMLConfigurationManager manager;

	@Before
	public void before() {
		manager = new XMLConfigurationManager();
	}

	@Test
	public void testDefaultLocalConfiguration() {
		Configuration configuration = manager.getConfiguration(XMLConfigurationManager.class);
		assertThat(configuration.getContractScanPackage(), is(empty()));
		assertThat(configuration.getDefaultPrecondition(), is(DefaultPreconditionType.UNDEFINED));
		assertThat(configuration.getPureRegistryImport(), is(empty()));
		assertThat(configuration.getRootPackage(), is(empty()));
		assertThat(configuration.isPureSkipInvariants(), is(true));
		assertThat(configuration.isPureValidate(), is(false));
		assertThat(configuration.isStrengtheningPreconditionsAllowed(), is(false));
	}

	@Test
	public void testDefaultGlobalConfiguration() {
		C4JGlobal globalConfig = manager.getGlobalConfiguration();
		assertThat(globalConfig.getContractViolationAction().getDefaultOrPackageOrClazz(), is(empty()));
		assertThat(globalConfig.getWriteTransformedClasses().isValue(), is(false));
	}

	@Test
	public void testRegisterLocalConfiguration() throws Exception {
		ClassLoader classLoader = createClassLoaderMock(C4J_LOCAL_XML, C4J_LOCAL_XML);
		manager.registerClassLoader(classLoader);
		Configuration configuration = manager.getConfiguration(XMLConfigurationManager.class);
		assertThat(configuration.isPureValidate(), is(true));
		assertThat(configuration.isPureSkipInvariants(), is(true));
	}

	private ClassLoader createClassLoaderMock(String mockPath, final String realPath) {
		ClassLoader classLoader = mock(ClassLoader.class);
		when(classLoader.getResourceAsStream(eq(mockPath))).thenAnswer(new Answer<InputStream>() {

			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				return classpathResourceLoader.loadStream(realPath);
			}
		});
		when(classLoader.getResource(eq(mockPath))).thenReturn(classpathResourceLoader.getUrl(realPath));
		return classLoader;
	}

	@Test
	public void testRegisterGlobalConfiguration() throws Exception {
		ClassLoader classLoader = createClassLoaderMock(C4J_GLOBAL_XML, C4J_GLOBAL_XML);
		manager.registerClassLoader(classLoader);
		C4JGlobal globalConfig = manager.getGlobalConfiguration();
		assertThat(globalConfig.getContractViolationAction().getDefaultOrPackageOrClazz(), is(empty()));
		assertThat(globalConfig.getWriteTransformedClasses().isValue(), is(true));
	}

	@Test
	public void testRegisterGlobalConfigurations_Twice() throws Exception {
		ClassLoader classLoader = createClassLoaderMock(C4J_GLOBAL_XML, C4J_GLOBAL_XML);
		manager.registerClassLoader(classLoader);
		manager.registerClassLoader(classLoader);
		C4JGlobal globalConfig = manager.getGlobalConfiguration();
		assertThat(globalConfig.getContractViolationAction().getDefaultOrPackageOrClazz(), is(empty()));
		assertThat(globalConfig.getWriteTransformedClasses().isValue(), is(true));
	}

	@Test
	public void testRegisterGlobalConfigurations_WithDifferentClassLoaders() throws Exception {
		ClassLoader classLoader = createClassLoaderMock(C4J_GLOBAL_XML, C4J_GLOBAL_XML);
		ClassLoader classLoader2 = createClassLoaderMock(C4J_GLOBAL_XML, "c4j-global2.xml");
		manager.registerClassLoader(classLoader);
		manager.registerClassLoader(classLoader2);
		C4JGlobal globalConfig = manager.getGlobalConfiguration();
		assertThat(globalConfig.getContractViolationAction().getDefaultOrPackageOrClazz(), is(empty()));
		assertThat(globalConfig.getWriteTransformedClasses().isValue(), is(true));
	}
}

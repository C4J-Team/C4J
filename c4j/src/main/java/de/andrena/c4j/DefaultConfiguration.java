package de.andrena.c4j;

import java.util.Collections;
import java.util.Set;

public final class DefaultConfiguration extends AbstractConfiguration {
	/**
	 * {@inheritDoc}
	 * 
	 * @return {@inheritDoc}
	 *         <p>
	 *         Defaults to an empty set. Only the DefaultConfiguration can have empty root packages.
	 */
	@Override
	public Set<String> getRootPackages() {
		return Collections.emptySet();
	}
}

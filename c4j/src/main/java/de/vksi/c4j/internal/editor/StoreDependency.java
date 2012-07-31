package de.vksi.c4j.internal.editor;

public class StoreDependency {
	private final byte[] dependency;
	private final boolean unchangeable;

	public StoreDependency(byte[] dependency, boolean unchangeable) {
		this.dependency = dependency;
		this.unchangeable = unchangeable;
	}

	public byte[] getDependency() {
		return dependency;
	}

	public boolean isUnchangeable() {
		return unchangeable;
	}

}

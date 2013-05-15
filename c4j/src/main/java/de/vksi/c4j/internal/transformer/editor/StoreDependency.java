package de.vksi.c4j.internal.transformer.editor;

public class StoreDependency {
	private final byte[] dependency;
	private final boolean unchangeable;
	private final int index;

	public StoreDependency(byte[] dependency, boolean unchangeable, int index) {
		this.dependency = dependency;
		this.unchangeable = unchangeable;
		this.index = index;
	}

	public byte[] getDependency() {
		return dependency;
	}

	public boolean isUnchangeable() {
		return unchangeable;
	}

	public int getIndex() {
		return index;
	}

}

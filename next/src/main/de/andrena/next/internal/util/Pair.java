package de.andrena.next.internal.util;

public class Pair<T, U> {
	private T first;
	private U second;

	public Pair(T first, U second) {
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return first;
	}

	public U getSecond() {
		return second;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Pair)) {
			return false;
		}
		Pair<?, ?> other = (Pair<?, ?>) obj;
		return other.getFirst().equals(first) && other.getSecond().equals(second);
	}

	@Override
	public int hashCode() {
		return 31 * first.hashCode() + second.hashCode();
	}
}

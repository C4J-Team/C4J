package de.andrena.c4j.acceptancetest.stack;

import de.andrena.next.Contract;
import de.andrena.next.Pure;

@Contract(StackSpecContract.class)
public interface StackSpec<T>
{
	
	@Pure
	int capacity ();

	@Pure
	int count ();

	void push (T x);

	void pop ();

	@Pure
	T top ();

	@Pure
	boolean isFull ();

	@Pure
	boolean isEmpty ();

    @Pure
    T get (int index);
    
}


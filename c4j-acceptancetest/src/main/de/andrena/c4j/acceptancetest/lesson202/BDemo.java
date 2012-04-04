package de.andrena.c4j.acceptancetest.lesson202;

public class BDemo {

	public static void main(String[] args) {
		B b = new B();
		b.queryB();
		b.query(1, 2);
		b.commandB(1);
		b.command(2);
	}

}

package de.vksi.c4j.acceptancetest.lesson103;

public class CDemo {

	public static void main(String[] args) {
		C c = new C();
		c.queryC();
		c.queryB();
		c.query(1, 2);
		c.commandC(1);
		c.commandB(1);
		c.command(2);

	}

}

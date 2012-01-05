package de.andrena.next.acceptancetest.timeofday;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.andrena.next.systemtest.TransformerAwareRule;

public class TimeOfDayTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private TimeOfDay classUnderTest;

	@Before
	public void setUp() {
		classUnderTest = new TimeOfDay();
	}

	@After
	public void tearDown() {
		classUnderTest = null;
	}

	@Test
	public void allRight() {
		classUnderTest.setHour(16);
		classUnderTest.setMinute(56);
		classUnderTest.setSecond(20);
		classUnderTest.getHour();
		classUnderTest.getMinute();
		classUnderTest.getSecond();
		classUnderTest.setHour(17);
		classUnderTest.setMinute(7);
		classUnderTest.setSecond(59);
	}

	@Test
	public void setHourErrorCondition1() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("hour >= 0");
		classUnderTest.setHour(-1);
	}

	@Test
	public void setHourErrorCondition2() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("hour <= 23");
		classUnderTest.setHour(24);
	}

	@Test
	public void setMinuteErrorCondition1() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("minute >= 0");
		classUnderTest.setMinute(-1);
	}

	@Test
	public void setMinuteErrorCondition2() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("minute <= 59");
		classUnderTest.setMinute(60);
	}

	@Test
	public void setSecondErrorCondition1() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("second >= 0");
		classUnderTest.setSecond(-1);
	}

	@Test
	public void setSecondErrorCondition2() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("second <= 59");
		classUnderTest.setSecond(60);
	}

}

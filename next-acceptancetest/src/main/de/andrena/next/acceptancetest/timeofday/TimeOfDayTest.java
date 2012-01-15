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
	public void setUpTest() {
		classUnderTest = new TimeOfDay();
	}

	@After
	public void tearDownTest() {
		classUnderTest = null;
	}

	@Test
	public void TimeOfDayTestRight() {
		// Test post-condition for "hour == 0"
		// assert classUnderTest.getHour() == 0 : "hour == 0";
		new TimeOfDay();
	}

	@Test
	public void TimeOfDayTestRight1() {
		// Test post-condition for "minute == 0"
		// assert classUnderTest.getMinute() == 0 : "minute == 0";
		new TimeOfDay();
	}

	@Test
	public void TimeOfDayTestRight2() {
		// Test post-condition for "second == 0"
		// assert classUnderTest.getSecond() == 0 : "second == 0";
		new TimeOfDay();
	}

	@Test
	public void getHourTestRight() {
		// Test post-condition for "result >= 0"
		// assert returnValue >= 0 : "result >= 0";
		classUnderTest.setHour(0);
		classUnderTest.getHour();
	}

	@Test
	public void getHourTestRight1() {
		// Test post-condition for "result <= 23"
		// assert returnValue <= 23 : "result <= 23";
		classUnderTest.setHour(23);
		classUnderTest.getHour();
	}

	@Test
	public void getMinuteTestRight() {
		// Test post-condition for "result >= 0"
		// assert returnValue >= 0 : "result >= 0";
		classUnderTest.setMinute(0);
		classUnderTest.getMinute();
	}

	@Test
	public void getMinuteTestRight1() {
		// Test post-condition for "result <= 59"
		// assert returnValue <= 59 : "result <= 59";
		classUnderTest.setMinute(59);
		classUnderTest.getMinute();
	}

	@Test
	public void getSecondTestRight() {
		// Test post-condition for "result >= 0"
		// assert returnValue >= 0 : "result >= 0";
		classUnderTest.setSecond(0);
		classUnderTest.getSecond();
	}

	@Test
	public void getSecondTestRight1() {
		// Test post-condition for "result <= 59"
		// assert returnValue <= 59 : "result <= 59";
		classUnderTest.setSecond(59);
		classUnderTest.getSecond();
	}

	@Test(expected = AssertionError.class)
	public void setHourTestErrorCondition1() {
		// Test error condition for "hour >= 0"
		// assert hour >= 0 : "hour >= 0";
		classUnderTest.setHour(-1);
	}

	@Test(expected = AssertionError.class)
	public void setHourTestErrorCondition2() {
		// Test error condition for "hour <= 23"
		// assert hour <= 23 : "hour <= 23";
		classUnderTest.setHour(24);
	}

	@Test
	public void setHourTestRight() {
		// Test post-condition for "hour set"
		// assert classUnderTest.getHour() == hour : "hour set";
		classUnderTest.setHour(12);
	}

	@Test
	public void setHourTestRight1() {
		// Test post-condition for "minute unchanged"
		// assert old_minute == classUnderTest.getMinute() : "minute unchanged";
		classUnderTest.setMinute(59);
		classUnderTest.setHour(23);
	}

	@Test
	public void setHourTestRight2() {
		// Test post-condition for "second unchanged"
		// assert old_second == classUnderTest.getSecond() : "second unchanged";
		classUnderTest.setSecond(59);
		classUnderTest.setHour(23);
	}

	@Test(expected = AssertionError.class)
	public void setMinuteTestErrorCondition1() {
		// Test error condition for "minute >= 0"
		// assert minute >= 0 : "minute >= 0";
		classUnderTest.setMinute(-1);
	}

	@Test(expected = AssertionError.class)
	public void setMinuteTestErrorCondition2() {
		// Test error condition for "minute <= 59"
		// assert minute <= 59 : "minute <= 59";
		classUnderTest.setMinute(60);
	}

	@Test
	public void setMinuteTestRight() {
		// Test post-condition for "minute set"
		// assert classUnderTest.getMinute() == minute : "minute set";
		classUnderTest.setMinute(59);
	}

	@Test
	public void setMinuteTestRight1() {
		// Test post-condition for "hour unchanged"
		// assert old_hour == classUnderTest.getHour() : "hour unchanged";
		classUnderTest.setHour(23);
		classUnderTest.setMinute(59);
	}

	@Test
	public void setMinuteTestRight2() {
		// Test post-condition for "second unchanged"
		// assert old_second == classUnderTest.getSecond() : "second unchanged";
		classUnderTest.setSecond(59);
		classUnderTest.setMinute(59);
	}

	@Test(expected = AssertionError.class)
	public void setSecondTestErrorCondition1() {
		// Test error condition for "second >= 0"
		// assert second >= 0 : "second >= 0";
		classUnderTest.setSecond(-1);
	}

	@Test(expected = AssertionError.class)
	public void setSecondTestErrorCondition2() {
		// Test error condition for "second <= 59"
		// assert second <= 59 : "second <= 59";
		classUnderTest.setSecond(60);
	}

	@Test
	public void setSecondTestRight() {
		// Test post-condition for "second set"
		// assert classUnderTest.getSecond() == second : "second set";
		classUnderTest.setSecond(59);
	}

	@Test
	public void setSecondTestRight1() {
		// Test post-condition for "hour unchanged"
		// assert old_hour == classUnderTest.getHour() : "hour unchanged";
		classUnderTest.setHour(23);
		classUnderTest.setSecond(59);
	}

	@Test
	public void setSecondTestRight2() {
		// Test post-condition for "minute unchanged"
		// assert old_minute == classUnderTest.getMinute() : "minute unchanged";
		classUnderTest.setMinute(59);
		classUnderTest.setSecond(59);
	}

}

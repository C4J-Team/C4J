package de.vksi.c4j.acceptancetest.timeofday;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.systemtest.TransformerAwareRule;
import de.vksi.c4j.acceptancetest.timeofday.RicherTimeOfDay;

public class RicherTimeOfDayTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private RicherTimeOfDay classUnderTest;

	@Before
	public void setUpTest() {
		classUnderTest = new RicherTimeOfDay();
	}

	@After
	public void tearDownTest() {
		classUnderTest = null;
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
		classUnderTest.setHour(23);
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
		classUnderTest.setMinute(23);
	}

	@Test
	public void RicherTimeOfDayTestRight() {
		// Test post-condition for "nearest hour == 0"
		// assert classUnderTest.getNearestHour() == 0 : "nearest hour == 0";
		new RicherTimeOfDay();
	}

	@Test
	public void getNearestHourTestRight() {
		// Test post-condition for "if minute < 30 then hour == nearest hour"
		// assert !(minute < 30) || (returnValue == hour) :
		// "if minute < 30 then hour == nearest hour";
		classUnderTest.setHour(22);
		classUnderTest.setMinute(29);
		classUnderTest.getNearestHour();
		classUnderTest.setHour(23);
		classUnderTest.getNearestHour();
	}

	@Test
	public void getNearestHourTestRight1() {
		// Test post-condition for
		// "if minute >= 30 && hour < 23 then hour == hour + 1"
		// assert !(minute >= 30 && hour < 23) || (returnValue == hour + 1) :
		// "if minute >= 30 && hour < 23 then hour == hour + 1";
		classUnderTest.setHour(22);
		classUnderTest.setMinute(30);
		classUnderTest.getNearestHour();
	}

	@Test
	public void getNearestHourTestRight2() {
		// Test post-condition for
		// "if minute >= 30 && hour == 23 then hour == 23"
		// assert !(minute >= 30 && hour == 23) || (returnValue == 0) :
		// "if minute >= 30 && hour == 23 then hour == 23";
		classUnderTest.setHour(23);
		classUnderTest.setMinute(30);
		classUnderTest.getNearestHour();
	}

}

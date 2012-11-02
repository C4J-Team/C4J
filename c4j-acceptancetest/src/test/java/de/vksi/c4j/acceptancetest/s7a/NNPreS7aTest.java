package de.vksi.c4j.acceptancetest.s7a;

import static org.hamcrest.Matchers.containsString;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.acceptancetest.floatingwindow.NorthEastAndSouthWestFloatingWindow;
import de.vksi.c4j.acceptancetest.floatingwindow.NorthEastFloatingWindowSpec;
import de.vksi.c4j.acceptancetest.floatingwindow.Vector;
import de.vksi.c4j.acceptancetest.workingstudent.EmployeeSpecContract;
import de.vksi.c4j.acceptancetest.workingstudent.WorkingStudent;
import de.vksi.c4j.acceptancetest.workingstudent.YoungWorkingStudent;
import de.vksi.c4j.acceptancetest.workingstudent.YoungWorkingStudentContract;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class NNPreS7aTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private NorthEastFloatingWindowSpec northEastFloatingWindow;
	private WorkingStudent workingStudent;
	private YoungWorkingStudent youngWorkingStudent;

	@Test
	public void testErrorByMultipleInheritance() {
		northEastFloatingWindow = new NorthEastAndSouthWestFloatingWindow(new Vector(0, 0), 200, 200);
		//Move window to SouthWest 
		thrown.expect(AssertionError.class);
		thrown.expectMessage(containsString("Invalid multiple inheritance"));
		northEastFloatingWindow.move(new Vector(-2, -5));
	}

	@Test
	public void testErrorMessageByStrengtheningOfPrecondition() {
		transformerAware.expectGlobalLog(Level.ERROR, "Found strengthening pre-condition in "
				+ YoungWorkingStudentContract.class.getName() + ".setAge(int)" + " which is already defined from "
				+ EmployeeSpecContract.class.getName() + " - ignoring the pre-condition.");

		youngWorkingStudent = new YoungWorkingStudent();
		youngWorkingStudent.setAge(60);
	}

	@Test
	public void testMultipleInheritanceOk_PreTrue() {
		workingStudent = new WorkingStudent();
		workingStudent.setAge(99);
	}

	@Test
	public void testMultipleInheritanceOk_PreFalse() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("age < 100");

		workingStudent = new WorkingStudent();
		workingStudent.setAge(101);
	}

}

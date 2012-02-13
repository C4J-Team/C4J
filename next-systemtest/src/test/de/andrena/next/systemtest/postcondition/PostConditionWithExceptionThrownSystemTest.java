package de.andrena.next.systemtest.postcondition;

import static de.andrena.next.Condition.exceptionThrownOfType;
import static de.andrena.next.Condition.post;

import java.io.File;
import java.io.IOException;

import de.andrena.next.Contract;
import de.andrena.next.Pure;
import de.andrena.next.Target;

public class PostConditionWithExceptionThrownSystemTest {

	@Contract(SampleClassContract.class)
	public static class SampleClass {
		private File sampleFile;

		public void methodThrowingException() throws IOException {
			sampleFile = new File("123");
			sampleFile.createNewFile();
		}

		@Pure
		public File getSampleFile() {
			return sampleFile;
		}

	}

	public static class SampleClassContract extends SampleClass {
		@Target
		private SampleClass target;

		@Override
		public void methodThrowingException() throws IOException {
			if (post()) {
				if (!exceptionThrownOfType(IOException.class)) {
					assert target.getSampleFile().canRead();
				}
			}
		}
	}
}

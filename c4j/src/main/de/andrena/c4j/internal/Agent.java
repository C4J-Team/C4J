package de.andrena.c4j.internal;

import java.lang.instrument.Instrumentation;

public class Agent {
	public static void premain(String agentArgs, Instrumentation inst) throws Exception {
		RootTransformer.INSTANCE.init(agentArgs);
		inst.addTransformer(RootTransformer.INSTANCE);
	}
}

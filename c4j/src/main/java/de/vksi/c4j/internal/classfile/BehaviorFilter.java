package de.vksi.c4j.internal.classfile;

import javassist.CtBehavior;

public enum BehaviorFilter {
	MODIFIABLE {
		@Override
		public boolean filter(CtBehavior behavior) {
			return ClassAnalyzer.isModifiable(behavior);
		}
	},
	DYNAMIC {
		@Override
		public boolean filter(CtBehavior behavior) {
			return ClassAnalyzer.isDynamic(behavior);
		}
	},
	STATIC {
		@Override
		public boolean filter(CtBehavior behavior) {
			return !ClassAnalyzer.isDynamic(behavior);
		}
	},
	VISIBLE {
		@Override
		public boolean filter(CtBehavior behavior) {
			return !ClassAnalyzer.isPrivate(behavior);
		}
	};

	public abstract boolean filter(CtBehavior behavior);
}
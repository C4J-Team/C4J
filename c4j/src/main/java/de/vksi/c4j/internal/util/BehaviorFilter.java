package de.vksi.c4j.internal.util;

import javassist.CtBehavior;

public enum BehaviorFilter {
	MODIFIABLE {
		@Override
		public boolean filter(CtBehavior behavior) {
			return ReflectionHelper.isModifiable(behavior);
		}
	},
	DYNAMIC {
		@Override
		public boolean filter(CtBehavior behavior) {
			return ReflectionHelper.isDynamic(behavior);
		}
	},
	STATIC {
		@Override
		public boolean filter(CtBehavior behavior) {
			return !ReflectionHelper.isDynamic(behavior);
		}
	},
	VISIBLE {
		@Override
		public boolean filter(CtBehavior behavior) {
			return !ReflectionHelper.isPrivate(behavior);
		}
	};

	public abstract boolean filter(CtBehavior behavior);
}
package next.internal.transformer;

public class TargetClassTransformer extends DelegateTransformer {
	private ClassTransformer[] transformers = new ClassTransformer[] { new BeforeAndAfterTriggerTransformer() };

	@Override
	protected ClassTransformer[] getTransformers() {
		return transformers;
	}
}

package next.internal.transformer;

public class ContractClassTransformer extends DelegateTransformer {

	private ClassTransformer[] transformers = new ClassTransformer[] { new PreAndPostExpressionTransformer(),
			new ContractExpressionTransformer() };

	@Override
	protected ClassTransformer[] getTransformers() {
		return transformers;
	}

}

package pexyn.guardInference;

/**
 * A builder of Boolean formulae.
 * 
 * @author romanm
 *
 * @param <ExampleType>
 *            The type of examples.
 * @param <FormulaType>
 *            The type of formulae.
 */
public interface BooleanDomain<ExampleType, FormulaType> {
	public FormulaType getTrue();

	public FormulaType getFalse();

	public FormulaType and(FormulaType first, FormulaType second);

	public FormulaType or(FormulaType first, FormulaType second);

	public boolean test(ExampleType example, FormulaType feature);
}

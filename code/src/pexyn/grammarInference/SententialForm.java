package pexyn.grammarInference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A sequence of symbols in a given grammar.
 * 
 * @author romanm
 *
 */
public class SententialForm extends ArrayList<Symbol> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Condition condition;

	final Comparator<SententialForm> longestProd = new Comparator<SententialForm>() {
		public int compare(SententialForm nt1, SententialForm nt2) {
			// this only compares size since our only multiple alternatives are
			// when using recursion (2 alternatives).
			int size1 = nt1.size(), size2 = nt2.size();
			return size2 - size1;
		}
	};

	public SententialForm() {
	}

	public SententialForm(List<Symbol> from) {
		this.addAll(from);
	}

	public SententialForm copy() {
		SententialForm result = new SententialForm();
		result.addAll(this);
		result.condition = this.condition;
		return result;
	}
}
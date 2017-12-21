package grammar;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import gp.AStar;
import gp.SearchResultType;
import gp.Searcher.SearchResult;
import gp.TR;

/**
 * A search-based LL (left-to-right, leftmost derivation) parser.
 * 
 * @author romanm
 */
public class NaiveLLParser implements Parser, TR<NaiveLLParser.ParseState, Node> {
	protected final Grammar g;
	protected List<Token> word;

	protected static class ParseState {
		public final int position;
		public final Stack<Node> parseStack;
		public Node dtree;

		public ParseState(int position, Node dtree, Stack<Node> parseStack) {
			this.position = position;
			this.dtree = dtree;
			this.parseStack = parseStack;
		}
	}

	public static NaiveLLParser fromGrammar(Grammar g) {
		throw new UnsupportedOperationException("unimplemented!");
	}

	@Override
	public Optional<Node> parse(List<Token> word) {
		this.word = word;
		AStar<ParseState, Node> searcher = new AStar<ParseState, Node>(this);
		ParseState initial = new ParseState(0, g.start, new Stack<>());
		SearchResult<ParseState> result = searcher.findState(initial,
				state -> state.parseStack.isEmpty() && state.position == word.size() - 1);
		if (result.resultType() == SearchResultType.OK) {
			Node derivationTree = result.get().dtree;
			return Optional.of(derivationTree);
		} else {
			return Optional.empty();
		}
	}

	private NaiveLLParser(Grammar g) {
		this.g = g;
	}

	@Override
	public Collection<Node> enabledActions(ParseState state) {
		Nonterminal leftmostNonterminal = state.dtree.leftmostNonterminal();
		if (leftmostNonterminal != null) {
			return leftmostNonterminal.productions();
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public float transitionCost(ParseState src, Node rhs, ParseState dst) {
		return 1;
	}

	@Override
	public Collection<ParseState> apply(ParseState state, Node rhs) {
		//Node successor = state.dtree.substituteLeftmostNonterminal(rhs);
		throw new UnsupportedOperationException("unimplemented");
	}

	@Override
	public float estimateDistToGoal(ParseState state) {
		return 0;
	}
}
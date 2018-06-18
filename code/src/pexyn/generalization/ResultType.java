package pexyn.generalization;

/**
 * The possible results of a generalization algorithm.
 * 
 * @author romanm
 */
public enum ResultType {
	/**
	 * Generalization succeeded, resulting in a deterministic automaton.
	 */
	OK,

	/**
	 * No deterministic automaton exists.
	 */
	NON_DETERMINISTIC,

	/**
	 * Generalization terminated due to resource exhaustion.
	 */
	OUT_OF_RESOURCES
}
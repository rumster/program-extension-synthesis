package gp;

/**
 * An argument for an operator or a condition.
 * 
 * @author romanm
 */
public class Arg {
	public final String name;
	public final ArgType type;

	public Arg(String name, ArgType type) {
		this.name = name;
		this.type = type;
	}
}
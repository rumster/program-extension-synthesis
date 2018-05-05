package gp.tmti;

import java.util.HashSet;
import java.util.Set;

/**
 * An automaton control state.
 * 
 * @author romanm
 */
public class State {
	public Set<TracePoint> points = new HashSet<>();
	public final String id;

	public State(String id) {
		this.id = id;
	}

	public void addTracePoint(TracePoint point) {
		points.add(point);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		return o == this;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}

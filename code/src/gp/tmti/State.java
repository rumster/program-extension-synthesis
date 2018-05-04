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

	public void addTracePoint(TracePoint point) {
		points.add(point);
	}
}

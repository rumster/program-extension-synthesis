package pexyn.generalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pexyn.Semantics.Cmd;
import pexyn.Semantics.Store;

/**
 * An automaton control state.
 * 
 * @author romanm
 */
public class State {
	public final String id;
	private Set<TracePoint> points = new HashSet<>();

	/**
	 * Partitions the values in the set of trace points relative to their next
	 * update.
	 */
	private Map<Cmd, Collection<Store>> updateToValues = new HashMap<>();

	public State(String id) {
		this.id = id;
	}

	public void addTracePoint(TracePoint point) {
		points.add(point);
		updateWithPoints(point);
	}

	public void addAllTracePoints(Set<TracePoint> points) {
		this.points.addAll(points);
		for (var point : points) {
			updateWithPoints(point);
		}
	}

	public Map<Cmd, Collection<Store>> updateToValues() {
		return Collections.unmodifiableMap(updateToValues);
	}

	public Set<TracePoint> getPoints() {
		return Collections.unmodifiableSet(points);
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
		return super.hashCode();
	}

	@Override
	public String toString() {
		return id;
	}

	/**
	 * TODO: handle trace points at the last position (where there is no next
	 * update).
	 */
	private void updateWithPoints(TracePoint point) {
		var update = point.plan.actionAt(point.pos);
		var value = point.plan.stateAt(point.pos);
		var values = updateToValues.get(update);
		if (values == null) {
			values = new ArrayList<>();
			updateToValues.put(update, values);
		}
		values.add(value);
	}
}

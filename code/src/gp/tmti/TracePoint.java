package gp.tmti;

import gp.Plan;
import gp.Domain.Update;
import gp.Domain.Value;

/**
 * A point along a trace. Trace points are immutable.
 * 
 * @author romanm
 */
public class TracePoint {
	public final Plan<? extends Value, ? extends Update> plan;
	public final int pos;

	public TracePoint(Plan<? extends Value, ? extends Update> plan, int pos) {
		assert plan != null;
		assert pos >= 0 && pos < plan.size();
		this.plan = plan;
		this.pos = pos;
	}
}
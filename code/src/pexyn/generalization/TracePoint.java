package pexyn.generalization;

import pexyn.Trace;
import pexyn.Domain.Cmd;
import pexyn.Domain.Store;

/**
 * A point along a trace. Trace points are immutable.
 * 
 * @author romanm
 */
public class TracePoint {
	public final Trace<? extends Store, ? extends Cmd> plan;
	public final int pos;

	public TracePoint(Trace<? extends Store, ? extends Cmd> plan, int pos) {
		assert plan != null;
		assert pos >= 0 && pos < plan.size();
		this.plan = plan;
		this.pos = pos;
	}
}
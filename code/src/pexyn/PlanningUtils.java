package pexyn;

import java.util.Optional;
import java.util.logging.Logger;

import bgu.cs.util.Union2;
import jminor.JmStore.JmErrorStore;
import pexyn.Semantics.Guard;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Store;
import pexyn.planning.Planner;
import pexyn.planning.SearchResultType;

/**
 * Utilities related to planning and plans.
 * 
 * @author romanm
 */
public class PlanningUtils {
	public static <StoreType extends Store, CmdType extends Cmd, GuardType extends Guard> Optional<Trace<StoreType, CmdType>> exampleToPlan(
			Semantics<StoreType, CmdType, GuardType> semantics, Planner<StoreType, CmdType> planner,
			Example<StoreType, CmdType> example, Logger logger) {
		assert example.size() > 0;
		if (example.size() == 1 && example.step(0).isT1()) {
			// An example for a possible input.
			return Optional.empty();
		}

		StoreType current;
		Union2<StoreType, CmdType> firstStep = example.step(0);
		if (firstStep.isT1()) {
			current = example.input();
		} else {
			throw new IllegalArgumentException("Encountered an example starting with a statement!");
		}

		logger.info("Planning for example " + example.name + "...");
		Trace<StoreType, CmdType> plan = new ArrayListTrace<>(current);
		for (int i = 1; i < example.steps.size(); ++i) {
			Union2<StoreType, CmdType> step = example.steps.get(i);
			if (step.isT1()) {
				var stateGoal = step.getT1();
				SearchResultType planResult = planner.findPlan(current, state -> {
					return semantics.match(state, stateGoal);
				}, plan);
				switch (planResult) {
				case OK:
					current = plan.lastState();
					break;
				case NO_SOLUTION_EXISTS:
					if (logger != null) {
						logger.info("No plan exists for example " + example.name + "! Skipping example.");
					}
					return Optional.empty();
				case OUT_OF_RESOURCES:
					if (logger != null) {
						logger.info("Timed out on example " + example.name + "! Skipping example.");
					}
					return Optional.empty();
				}
			} else {
				CmdType action = step.getT2();
				Optional<StoreType> next = semantics.apply(action, current);
				if (next.isPresent() && !(next.get() instanceof JmErrorStore)) {
					current = next.get();
					plan.append(action, current);
				} else {
					if (logger != null) {
						logger.info("Action " + action.toString() + " of example " + example.name + " at step " + i
								+ " is not applicable for the respective state! Skipping example.");
					}
					return Optional.empty();
				}
			}
		}
		return Optional.of(plan);
	}
}
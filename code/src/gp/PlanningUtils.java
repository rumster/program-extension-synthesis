package gp;

import java.util.Optional;
import java.util.logging.Logger;

import bgu.cs.util.Union2;
import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;
import gp.planning.Planner;
import gp.planning.SearchResultType;
import heap.Store.ErrorStore;

public class PlanningUtils {
	public static <ValueType extends Value, UpdateType extends Update, GuardType extends Guard> Optional<Plan<ValueType, UpdateType>> exampleToPlan(
			Domain<ValueType, UpdateType, GuardType> domain, Planner<ValueType, UpdateType> planner,
			Example<ValueType, UpdateType> example, Logger logger) {
		assert example.size() > 0;
		if (example.size() == 1 && example.step(0).isT1()) {
			// An example for a possible input.
			return Optional.empty();
		}

		ValueType current;
		Union2<ValueType, UpdateType> firstStep = example.step(0);
		if (firstStep.isT1()) {
			current = example.input();
		} else {
			throw new IllegalArgumentException("Encountered an example starting with a statement!");
		}

		logger.info("Planning for example " + example.name + "...");
		Plan<ValueType, UpdateType> plan = new ArrayListPlan<>(current);
		for (int i = 1; i < example.steps.size(); ++i) {
			Union2<ValueType, UpdateType> step = example.steps.get(i);
			if (step.isT1()) {
				var stateGoal = step.getT1();
				final var finalCurrent = current;
				SearchResultType planResult = planner.findPlan(current, state -> {
					return domain.match(finalCurrent, stateGoal);
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
				UpdateType action = step.getT2();
				Optional<ValueType> next = domain.apply(action, current);
				if (next.isPresent() && !(next.get() instanceof ErrorStore)) {
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
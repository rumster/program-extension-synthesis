package pexyn.guardInference;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import pexyn.Domain;
import pexyn.Trace;
import pexyn.Domain.Guard;
import pexyn.Domain.Cmd;
import pexyn.Domain.Store;

/**
 * An inferencer that simply iterates over a list of given predicates and
 * returns the first one that separates.
 * 
 * @author romanm
 */
@Deprecated
public class LinearInferencer<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard>
		extends ConditionInferencer<StoreType, CmdType, GuardType> {
	/**
	 * The domain comprised of values and predicates.
	 */
	public Domain<StoreType, CmdType, GuardType> domain;

	private final List<GuardType> guards;

	public LinearInferencer(Domain<StoreType, CmdType, GuardType> domain, List<Trace<StoreType, CmdType>> plans) {
		this.domain = domain;
		this.guards = domain.generateGuards(plans);
	}

	@Override
	public Optional<GuardType> infer(Collection<? extends Store> first, Collection<? extends Store> second) {
		for (var guard : guards) {
			var separates = true;
			for (var val1 : first) {
				@SuppressWarnings("unchecked")
				var val1Typed = (StoreType) val1;
				if (!domain.test(guard, val1Typed)) {
					separates = false;
					break;
				}
			}
			if (!separates) {
				continue;
			}
			for (var val2 : second) {
				@SuppressWarnings("unchecked")
				var val2Typed = (StoreType) val2;
				if (domain.test(guard, val2Typed)) {
					separates = false;
					break;
				}
			}
			if (separates) {
				return Optional.of(guard);
			}
		}
		return Optional.empty();
	}

	@Override
	public List<GuardType> guards() {
		return guards;
	}
}
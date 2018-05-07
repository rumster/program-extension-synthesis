package gp.tmti;

import gp.Domain.Update;

class HaltUpdate implements Update {
	public static final HaltUpdate v = new HaltUpdate();

	@Override
	public boolean equals(Object o) {
		return o == v;
	}

	@Override
	public int hashCode() {
		return HaltUpdate.class.hashCode();
	}
	
	@Override
	public String toString() {
		return "Halt";
	}
}
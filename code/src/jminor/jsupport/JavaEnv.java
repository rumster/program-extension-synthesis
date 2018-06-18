package jminor.jsupport;

import bgu.cs.util.ReflectionUtils;

/**
 * Represents a set of method arguments and local variables of a method to be
 * synthesized.
 * 
 * @author romanm
 */
public abstract class JavaEnv {
	public Object getReturn() {
		return getParam(JavaProblemGenerator.RET_PARAM);
	}

	public Object getParam(String name) {
		try {
			return ReflectionUtils.getFieldValue(this, name);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {			
			return null;
		}
	}

	public void setParam(String name, Object val) {
		try {
			ReflectionUtils.setFieldValue(this, name, val);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
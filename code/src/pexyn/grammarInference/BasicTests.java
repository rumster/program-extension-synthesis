package pexyn.grammarInference;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;



class OperatorLetter extends Letter {
	final char letter;

	public OperatorLetter(char c) {
		super();
		this.letter = c;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return Character.toString(letter);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + letter;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OperatorLetter other = (OperatorLetter) obj;
		if (letter != other.letter)
			return false;
		return true;
	}



}
class Trace extends ArrayList<OperatorLetter>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
public class BasicTests {

	static Random rand = new Random();

	public static Trace getLoopTrace(int length) {
		Trace arr = new Trace();
		arr.add(new OperatorLetter('X'));
		for(int i=0; i< length; i++) {
			arr.add(new OperatorLetter('A'));
			arr.add(new OperatorLetter('B'));
			arr.add(new OperatorLetter('C'));
		}
		arr.add(new OperatorLetter('Y'));
		return arr;
	}
	public static Trace getTwoLoopsTrace(int length) {
		Trace arr = new Trace();
		arr.add(new OperatorLetter('X'));
		for(int i=0; i< length; i++) {
			arr.add(new OperatorLetter('A'));
			arr.add(new OperatorLetter('B'));
			arr.add(new OperatorLetter('C'));
		}
		for(int i=0; i< length; i++) {
			arr.add(new OperatorLetter('A'));
			arr.add(new OperatorLetter('T'));
			arr.add(new OperatorLetter('H'));
		}
		arr.add(new OperatorLetter('Y'));
		return arr;
	}

	public static Trace getIfTrace(int length) {
		
		Trace arr = new Trace();
		arr.add(new OperatorLetter('X'));
		for(int i=0; i< length; i++) {
			arr.add(new OperatorLetter('A'));
			arr.add(new OperatorLetter('B'));
		}
		if(rand.nextInt(100) >= 50) 
			arr.add(new OperatorLetter('C'));
		arr.add(new OperatorLetter('Y'));
		return arr;
	}

	public static Trace getIfElseTrace(int length) {
		
		Trace arr = new Trace();
		arr.add(new OperatorLetter('X'));
		for(int i=0; i< length; i++) {
			arr.add(new OperatorLetter('A'));
			arr.add(new OperatorLetter('Y'));
		}
		if(rand.nextInt(100) >= 50) 
			arr.add(new OperatorLetter('B'));
		else 
			arr.add(new OperatorLetter('C'));
		arr.add(new OperatorLetter('Z'));
		return arr;
	}

	public static Trace getIfLoopTrace(int length) {
		Trace arr = new Trace();
		arr.add(new OperatorLetter('X'));
		for(int i=0; i< length; i++) {
			arr.add(new OperatorLetter('A'));
			if(rand.nextInt(100) >= 50) 
				arr.add(new OperatorLetter('B'));
			arr.add(new OperatorLetter('C'));
		}
		arr.add(new OperatorLetter('Y'));
		return arr;
	}

	public static Trace getIfElseLoopTrace(int length) {
		
		Trace arr = new Trace();
		arr.add(new OperatorLetter('X'));
		for(int i=0; i< length; i++) {
			arr.add(new OperatorLetter('A'));
			if(rand.nextInt(100) >= 50) 
				arr.add(new OperatorLetter('B'));
			else 
				arr.add(new OperatorLetter('C'));
			arr.add(new OperatorLetter('Y'));
		}
		arr.add(new OperatorLetter('Z'));
		return arr;
	}

	//@SuppressWarnings("unused")
	public static void main(String[] args) {
		/**/System.out.println("Generating Loop trace");
		testConvergence(BasicTests::getLoopTrace);
		System.out.println("Generating Two Loops trace");
		testConvergence(BasicTests::getTwoLoopsTrace);
		System.out.println("Generating If trace");
		testConvergence(BasicTests::getIfTrace);
		System.out.println("Generating If Loop trace");
		testConvergence(BasicTests::getIfLoopTrace);/*
		System.out.println("Generating If/Else trace");
		testConvergence(BasicTests::getIfElseTrace);
		System.out.println("Generating If/Else Loop trace");
		testConvergence(BasicTests::getIfElseLoopTrace);*/
	}

	private static void testConvergence(Function<Integer, Trace> traceGen) {
		Sequential x = new Sequential();
		Grammar stableGrammar = new Grammar();
		int stableLen = 0;
		int maxLen = 20;
		for (int tLen = 1; tLen < maxLen; ++tLen) {
			Trace t = traceGen.apply(tLen);

			Grammar currGrammar = x.addExample(t);

			if (!currGrammar.equals(stableGrammar)) {
				stableGrammar = new Grammar(currGrammar);
				stableLen = tLen;
			}
		}
		System.out.println("Converging trace with len=" + String.valueOf(stableLen) + " out of " + maxLen +" :");
		System.out.println(traceGen.apply(stableLen));
		System.out.print("Final grammar:");
		System.out.println(stableGrammar.toString());
		System.out.println("-------------------------------------------------------------\n\n");
	}
}

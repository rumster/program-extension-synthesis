package pexyn.grammarInference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class CommonSentDesc{
	int Myindx, Oindx, length;
}
/**
 * A sequence of symbols in a given grammar.
 * 
 * @author romanm
 *
 */
public class SententialForm extends ArrayList<Symbol> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Condition condition;
	
	public int Rank() {
		int rank = 0;
		for(Symbol s : this) {
			rank = Math.max(rank, s.Rank() + 1);
		}
		return rank;
	}

	final Comparator<SententialForm> longestProd = new Comparator<SententialForm>() {
		public int compare(SententialForm nt1, SententialForm nt2) {
			// this only compares size since our only multiple alternatives are
			// when using recursion (2 alternatives).
			//int size1 = nt1.size(), size2 = nt2.size();
			//return size2 - size1;
			return nt2.Rank() - nt1.Rank();
		}
	};

	public SententialForm() {
	}

	public SententialForm(List<Symbol> from) {
		this.addAll(from);
	}

	public SententialForm copy() {
		SententialForm result = new SententialForm();
		result.addAll(this);
		result.condition = this.condition;
		return result;
	}

	@Override
	public String toString() {
		if(size() == 0) return "\u03B5";
		return super.toString();
	}

	public static CommonSentDesc getLongestCommonSubstring(List<Symbol> me, List<Symbol> oth){
		int n = me.size();
		int m = oth.size();
		CommonSentDesc ret = new CommonSentDesc();
		int[][] dp = new int[m][n];
	 
		for(int i=0; i<m; i++){
			for(int j=0; j<n; j++){
				if(oth.get(i).equals(me.get(j))){
					if(i==0 || j==0){
						dp[i][j]=1;
					}else{
						dp[i][j] = dp[i-1][j-1]+1;
					}
	 
					if(ret.length < dp[i][j]) {
						ret.length= dp[i][j];
						ret.Myindx = j;
						ret.Oindx = i;
					}
					
				}
	 
			}
		}
	 
		return ret;
	}
}
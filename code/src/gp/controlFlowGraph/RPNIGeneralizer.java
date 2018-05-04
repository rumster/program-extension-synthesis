package gp.controlFlowGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import bgu.cs.util.graph.MultiGraph;
import gp.GPDebugger;
import gp.Plan;
import gp.controlFlowGraph.CFG.ConditionalAction;
import gp.controlFlowGraph.CFG.Node;
import gp.separation.ConditionInferencer;
import gp.separation.InterpolatingConditionInferencer;
import grammar.CostBadConditions;
import grammar.CostFun;
import grammar.CostSize2;
import grammar.CostSum;
import heap.BoolExpr;
import heap.Stmt;
import heap.Store;

public class RPNIGeneralizer 
		extends CFGGeneralizer<Store, Stmt, BoolExpr> {
	protected final GPDebugger debugger;
	
	private CFG<Store, Stmt, BoolExpr> cfg;
	private ConditionInferencer<Store, BoolExpr> conditionInferencer;
	
	private static CostFun cost = new CostSum(new CostBadConditions(), new CostSize2());
	
	public RPNIGeneralizer(GPDebugger debugger, String outputDir) {
		this.debugger = debugger;
		cfg = new CFG<>();
		conditionInferencer = new InterpolatingConditionInferencer(outputDir);
	}

	@Override
	public Result generalize(Collection<Plan<Store, Stmt>> plans, CFG<Store, Stmt, BoolExpr> result) {

		for(Plan<Store, Stmt> plan : plans) {
			cfg.extendPTP(plan);
		}		
		
		RPNI(cfg);
		
		return Result.OK;
	}

//	public void addTrace(Trace trace) {		
//		cfg.extendPTP(trace);
//		//Globals.printer().printGraph(cfg, "Testme");
//		
//		CFG nextcfg = new CFG(cfg);	
//		System.out.println(nextcfg);
//		
//		
//		Globals.printer().printGraph(nextcfg, "RPNI before");
//		System.out.println("-----------------------------------------");
//		RPNI(nextcfg);
//		conditionInferencer.inferConditions(nextcfg);
//		System.out.println("-----------------------------------------");
//		System.out.println(nextcfg);
//		Globals.printer().printGraph(nextcfg, "RPNI after");	
//	}
	
	
	
	// custom RPNI-style 
	
	// original RPNI
	private void RPNI(CFG<Store, Stmt, BoolExpr> newcfg){
		TreeSet<Node<Store, Stmt>> red = new TreeSet<>(new LexLenPathCFGNodeComparator());
		TreeSet<Node<Store, Stmt>> blue = new TreeSet<>(new LexLenPathCFGNodeComparator());
		
		red.add(newcfg.entry());
		for (MultiGraph.Edge<Node<Store, Stmt>, ConditionalAction<Stmt, BoolExpr>> outEdge : newcfg.succEdges(newcfg.entry())) {
			if(!newcfg.exit().equals(outEdge.getDst())) {
				blue.add(outEdge.getDst());
			}
		}
		
		while(!blue.isEmpty()) {
			Node<Store, Stmt> qb = blue.pollFirst();
			
			Node<Store, Stmt> compatible = null;
			for(Node<Store, Stmt> qr : red) {
				//CFG modifiedCfg = new CFG(newcfg);
				Node<Store, Stmt> mergeResult = newcfg.mergeNodes(qb, qr, true);
				fold(newcfg, mergeResult, red, blue);
				
				newcfg.inferConditions(conditionInferencer);
				float postCost = conditionsCost(newcfg, cost);
				if(postCost != CostFun.INFINITY_COST){
					compatible = qr;
					newcfg.commitMerges();
					break;
				}
				else {
					newcfg.revertMerges();
				}
			}
			
			if(compatible != null) {				
				//CFGNode mergeResult = newcfg.mergeNodes(qb, compatible, true);
				//fold(newcfg, mergeResult, red, blue);				
				//conditionInferencer.inferConditions(newcfg);
				
				//Globals.printer().printGraph(newcfg, "RPNI merge" + qb + " " + compatible);
				
				for(Node<Store, Stmt> q : red) {
					for (MultiGraph.Edge<Node<Store, Stmt>, ConditionalAction<Stmt, BoolExpr>> outEdge : newcfg.succEdges(q)) {
						if(!red.contains(outEdge.getDst())&&
								!newcfg.exit().equals(outEdge.getDst())) {
							blue.add(outEdge.getDst());
						}
					}
				}
			}
			else {				
				//RPNI-promote
				red.add(qb);
				for (MultiGraph.Edge<Node<Store, Stmt>, ConditionalAction<Stmt, BoolExpr>> outEdge : newcfg.succEdges(qb)) {
					if(!red.contains(outEdge.getDst()) &&
							!newcfg.exit().equals(outEdge.getDst())) {
						blue.add(outEdge.getDst());
					}
				}
			}
		}
	}
	
	private void fold(CFG<Store, Stmt, BoolExpr> cfg, Node<Store, Stmt> source, 
			TreeSet<Node<Store, Stmt>> red, TreeSet<Node<Store, Stmt>> blue){
		List<Node<Store, Stmt>> stack = new ArrayList<>();
		stack.add(0, source);
		
		while(!stack.isEmpty()){
			Node<Store, Stmt> current = stack.remove(0);						
			for(Node<Store, Stmt> n : mergeTwoSameActionNodes(cfg, current, red, blue)) {
				stack.add(0, n);
			}
		}
	}
	
	private List<Node<Store, Stmt>> mergeTwoSameActionNodes(CFG<Store, Stmt, BoolExpr> cfg, Node<Store, Stmt> source, 
			TreeSet<Node<Store, Stmt>> red, TreeSet<Node<Store, Stmt>> blue){
		List<Node<Store, Stmt>> result = new ArrayList<>();
		if(!cfg.containsNode(source)){
			return result;
		}
		
		Map<Stmt, List<Node<Store, Stmt>>> actionsToNode = new HashMap<>();
		for(MultiGraph.Edge<Node<Store, Stmt>, ConditionalAction<Stmt, BoolExpr>> edge : cfg.succEdges(source)){
			List<Node<Store, Stmt>> nodes = actionsToNode.get(edge.getLabel().action());
			if(nodes == null){
				nodes = new ArrayList<>();
			}
			nodes.add(edge.getDst());
			actionsToNode.put(edge.getLabel().action(), nodes);
		}
		
		Node<Store, Stmt> nextMergedNode = null;
		for (Map.Entry<Stmt, List<Node<Store, Stmt>>> entry : actionsToNode.entrySet()){
			List<Node<Store, Stmt>> nodes = new ArrayList<>();
			for(Node<Store, Stmt> n : entry.getValue()) {
				nodes.add(n);
			}
			
			if(nodes.size() > 1){
				nextMergedNode = nodes.get(0);
				
				for(int i = 1; i < nodes.size(); i++){
					Node<Store, Stmt> from = nodes.get(i);
					Node<Store, Stmt> to = nextMergedNode;
					Node<Store, Stmt> tmp;
					
					//merge any color node into red OR white to blue
					if(red.contains(from) || 
							(blue.contains(from) && !red.contains(to))) {
						tmp = to;
						to = from;
						from = tmp;
					}
	
					nextMergedNode = cfg.mergeNodes(from, to, true);					
				}
				result.add(nextMergedNode);
			}
		}			
		return result;
	}
	
	private class LexLenNameCFGNodeComparator implements Comparator<Node<Store, Stmt>>{
		@Override
		public int compare(Node<Store, Stmt> n1, Node<Store, Stmt> n2) {
			int lenDiff = n1.getName().length() - n2.getName().length();
			if(lenDiff == 0) {
				return n1.getName().compareTo(n2.getName());
			}
			else {
				return lenDiff;
			}			
		}		
	}
	
	private class LexLenPathCFGNodeComparator implements Comparator<Node<Store, Stmt>>{

		@Override
		public int compare(Node<Store, Stmt> n1, Node<Store, Stmt> n2) {
			CostFun costfunc  = new CostSum(new CostBadConditions(), new CostSize2());
			
			List<Stmt> path1 = n1.getPath();
			List<Stmt> path2 = n2.getPath();		
			
			float result = path1.size() - path2.size();
			for(int i = 0; i < path1.size() && (result == 0); i++) {
				Stmt op1 = path1.get(i);
				Stmt op2 = path2.get(i);
				if(!op1.equals(op2)) {
					result = costfunc.apply(op1) - costfunc.apply(op2);
					if(result == 0) {
						result = op1.toString().compareTo(op2.toString());
					}
				}				
			}			
			return (int)Math.signum(result);
		}		
	}
	
	//condition inferencing
	//TODO - move
	public float conditionsCost(CFG<Store, Stmt, BoolExpr> cfg, CostFun cost) {
		float totalCost = 0;
		for (Node<Store, Stmt> node : cfg.getNodes()) {
			if (cfg.outDegree(node) < 2)
				continue;
			float nodeCost = conditionsCostForNode(cfg, node, cost);
			totalCost += nodeCost; //??? nodeCost^2 or nodeCost*factor
		}
		return totalCost;
	}

	public float conditionsCostForNode(CFG<Store, Stmt, BoolExpr> cfg, Node<Store, Stmt> node, CostFun cost) {
		float totalCost = 0;
		int outDegree = cfg.outDegree(node);
		for (MultiGraph.Edge<Node<Store, Stmt>, ConditionalAction<Stmt, BoolExpr>> edge : cfg.succEdges(node)) {
			ConditionalAction<Stmt, BoolExpr> action = edge.getLabel();
			if((outDegree >= 2) && (action.condition() == null)) {
				// a node with out-degree >= 2 must have conditions on all the out-edges
				// otherwise it is in invalid state (most likely condition inferencer failed on some edge)
				// entry/exit nodes are exceptions
				return CostFun.INFINITY_COST;
			}
			else{
				totalCost += cost.apply(action.condition());
			}
		}
		return totalCost;
	}
}

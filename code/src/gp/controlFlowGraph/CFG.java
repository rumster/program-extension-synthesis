package gp.controlFlowGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import bgu.cs.util.graph.HashMultiGraph;
import bgu.cs.util.graph.MultiGraph;
import bgu.cs.util.rel.HashRel2;
import gp.Domain;
import gp.separation.ConditionInferencer;

/**
 * A control-flow graph where nodes are associated with sets of states..
 * 
 * @author romanm
 *
 * @param <ActionType>
 *            The type of actions labeling edges.
 * @param <ConditionType>
 *            The type of conditions on branching nodes.
 */
public class CFG<StateType extends Domain.Value, ActionType extends Domain.Update, ConditionType extends Domain.Guard>
		extends HashMultiGraph<CFG.Node<StateType, ActionType>, CFG.ConditionalAction<ActionType, ConditionType>> 
		implements Interpreted <StateType, ActionType>{
	
	private Domain<StateType, ActionType, ConditionType> domain;
	
	private Node<StateType, ActionType> ENTRY;
	private Node<StateType, ActionType> EXIT;
	
	private List<Revert<StateType, ActionType, ConditionType>> revertStack = new ArrayList<>();

	public HashMultiGraph<StateType, Label<ActionType>> transitions = new HashMultiGraph<>();	


	public CFG(Domain<StateType, ActionType, ConditionType> domain) {
		this.domain = domain;
		
		ENTRY = new Node<>("entry");
		addNode(ENTRY);
		
		EXIT = new Node<>("exit");
		addNode(EXIT);
	}
	
	public CFG(CFG<StateType, ActionType, ConditionType> other){
		this(other.getDomain());	
		transitions = other.transitions;		//no need for deep copy, immutable once created
	
		for (Node<StateType, ActionType> node : other.getNodes()) {
			Node<StateType, ActionType> newsrc = getNode(node);
			//ENTRY/EXIT are singletons that were already allocated
			
			if(newsrc == null){
				newsrc = new Node<>(node);	
				addNode(newsrc);
				
				if(node.equals(other.entry())){
					ENTRY = newsrc;
				}
				else if(node.equals(other.exit())){
					EXIT = newsrc;
				}
			}//otherwise the node was already created		
			for (MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> edge : other.succEdges(node)) {
				Node<StateType, ActionType> newdst = getNode(edge.getDst());
				if(newdst == null){
					newdst = new Node<>(edge.getDst());
					addNode(newdst);
					
					if(edge.getDst().equals(other.entry())){
						ENTRY = newdst;
					}
					else if(edge.getDst().equals(other.exit())){
						EXIT = newdst;
					}
				}					
				addEdge(newsrc, newdst, new ConditionalAction<ActionType, ConditionType>(edge.getLabel()));
			}
		}
	}
		
//	public void extendPTP(Plan<StateType, ActionType> trace){
//		if (trace.isEmpty())
//			return;
//		
//		Node<StateType, ActionType> currentNode = ENTRY;
//		currentNode.states.add(trace.firstState());
//		StateType currentState = trace.firstState();
//		transitions.addNode(currentState);
//		
//		for (int i = 0; i < trace.size() - 1; ++i) {			
//			StateType nextState = trace.stateAt(i + 1);
//			ActionType nextOp = trace.actionAt(i);
//			
//			Node<StateType, ActionType> nextNode = null;
//			for (MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> edge : this.succEdges(currentNode)) {
//				if(nextOp.equals(edge.getLabel().action())) {
//					nextNode = edge.getDst();
//					break;
//				}				
//			}
//			if(nextNode == null) {
//				nextNode = new Node<StateType, ActionType>();
//				addNode(nextNode);
//				addEdge(currentNode, nextNode, new ConditionalAction<>(nextOp));
//				
//				List<ActionType> path = new ArrayList<>(currentNode.getPath());
//				path.add(nextOp);
//				nextNode.setPath(path);	
//			}
//			
//			CFG.Label<ActionType> nextLable = new CFG.Label<ActionType>(nextOp);			 
//			nextNode.states.add(nextState);
//			currentNode.addLable(nextLable);	
//
//			transitions.addNode(nextState);
//			transitions.addEdge(currentState, nextState, nextLable);
//			
//			currentState = nextState;
//			currentNode = nextNode;
//		}
//		
//		mergeNodes(currentNode, EXIT);
//		commitMerges();
//	}
	
	/*
	 * Merge nodes n1 and n2 according to nodes order
	 */
	public Node<StateType, ActionType> mergeNodes(Node<StateType, ActionType> n1, Node<StateType, ActionType> n2){
		return mergeNodes(n1, n2, false);
	}
	
	/*
	 * Merges 'from' into 'to', if directional is true,
	 * otherwise merge "small" into "big" node (according to nodes order)
	 */
	public Node<StateType, ActionType> mergeNodes(Node<StateType, ActionType> from, Node<StateType, ActionType> to, boolean directional) {
		if (from == to)
			return to;
		
		//1. deterministic merge: always merge "small" into "big" node, regardless arguments order
		//2. prevent disappearing ENTRY/EXIT nodes by always setting them as a destination 
		if((!directional && to.compareTo(from) < 0) || from.equals(EXIT)){
				//|| from.equals(ENTRY) ){
			Node<StateType, ActionType> tmp = to;
			to = from;
			from = tmp;
		}
		
		Revert<StateType, ActionType, ConditionType> revert = new Revert<>(to, from);

		for (MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> edge : succEdges(from)) {
			boolean changed = addEdge(to, edge.getDst(), edge.getLabel());
			
			if(changed) {
				revert.addEdgeToRemove(getEdge(to, edge.getDst(), edge.getLabel()));
			}
			revert.addEdgeToToAdd(edge);
		}
		for (MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> edge : predEdges(from)) {
			boolean changed = addEdge(edge.getSrc(), to, edge.getLabel());
			
			if(changed) {
				revert.addEdgeToRemove(getEdge(edge.getSrc(), to, edge.getLabel()));
			}
			revert.addEdgeToToAdd(edge);
		}
		to.states.addAll(from.states);
		removeNode(from);
		to.addSources(from.getSources());
		to.addLables(from.getLables());
		
		if(from.equals(ENTRY)) {
			ENTRY = to;
			
			revert.setEntry();
		}
		
		revertStack.add(0, revert);
		
		return to;
	}
	
	public void commitMerges() {
		revertStack.clear();
	}
	
	public void revertMerges() {
		while(!revertStack.isEmpty()) {
			Revert<StateType, ActionType, ConditionType> revert = revertStack.remove(0);

			addNode(revert.getOrigNode());
			for (MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> edge : revert.getEdgesToAdd()) {
				addEdge(edge.getSrc(), edge.getDst(), edge.getLabel());
			}
			
			for (MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> edge : revert.getEdgesToRemove()) {
				removeEdge(edge);
			}
			
			Node<StateType, ActionType> nodeToSplit = revert.getNodeToSplit();
			Node<StateType, ActionType> origNode = revert.getOrigNode();
			
			Set<StateType> toRemove = new HashSet<>();
			for(StateType st : nodeToSplit.states) {
				boolean wasInOrig = false;
				boolean wasInMerged = false;
				for (MultiGraph.Edge<StateType, Label<ActionType>> outTransition : transitions.succEdges(st)) {
					CFG.Label<ActionType> l = outTransition.getLabel();
					if(origNode.getLables().contains(l)){
						wasInOrig = true;
					}
					else {
						wasInMerged = true;
					}
				}
				
				if(wasInOrig && !wasInMerged) {
					toRemove.add(st);
				}
			}
			nodeToSplit.states.removeAll(toRemove);
			
			
			
			nodeToSplit.getSources().removeAll(origNode.getSources());
			nodeToSplit.getLables().removeAll(origNode.getLables());
			
			if(revert.isEntry) {
				ENTRY = origNode;
			}
		}
	}
	
	public Domain<StateType, ActionType, ConditionType> getDomain() {
		return domain;
	}
	
	public Node<StateType, ActionType> exit() {
		return EXIT;
	}
	
	public Node<StateType, ActionType> entry() {
		return ENTRY;
	}
	
	public Node<StateType, ActionType> getNode(Node<StateType, ActionType> node){
		for(Node<StateType, ActionType> n : succs.keySet()){
			if(n.equals(node)){
				return n;
			}
		}
		return null;
	}

	@Override
	public boolean removeNode(Node<StateType, ActionType> node) {
		//assert node != ENTRY && node != EXIT;
		return super.removeNode(node);
	}
	
	@Override
	//TODO move to HashMultiGraph
	public boolean removeEdge(MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> edge) {
		succs.get(edge.getSrc()).remove(edge);
		preds.get(edge.getDst()).remove(edge);
		return true;
	}
	
	public MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> getEdge(Node<StateType, ActionType> from, Node<StateType, ActionType> to, ConditionalAction<ActionType, ConditionType> label){
		for (MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> edge : succEdges(from)) {
			if(to.equals(edge.getDst()) && label.equals(edge.getLabel())) {
				return edge;
			}
		}
		return null;
	}
	
	//condition inferencing
	
	public boolean inferConditions(ConditionInferencer<StateType, ActionType, ConditionType> conditionInferencer) {
		//TODO - remove "cfg" after moving this
		CFG<StateType, ActionType, ConditionType> cfg = this;
		boolean foundConditions = true;
		for (Node<StateType, ActionType> node : cfg.getNodes()) {
			//entry node has no states, there is no meaning for separating conditions
			if (cfg.outDegree(node) < 2)
					//|| node.equals(cfg.ENTRY))
				continue;
			boolean foundSeparator = inferConditionsForNode(node, conditionInferencer);
//			if (!foundSeparator)
//				Globals.LOGGER.info("failed to infer cond for node " + node);
			foundConditions &= foundSeparator;
		}
		return foundConditions;
	}
	
	
	public boolean inferConditionsForNode(Node<StateType, ActionType> node, ConditionInferencer<StateType, ActionType, ConditionType> conditionInferencer) {
		//TODO - remove "cfg" after moving this
		CFG<StateType, ActionType, ConditionType> cfg = this;
		HashRel2<ActionType, StateType> operatorToStates = new HashRel2<>();
		
		for (StateType state : node.states) {
			for (MultiGraph.Edge<StateType, Label<ActionType>> outTransition : cfg.transitions.succEdges(state)) {
				CFG.Label<ActionType> l = outTransition.getLabel();
				if(node.getLables().contains(l)){
					ActionType op = l.getValue();
					operatorToStates.add(op, state);
				}
			}
		}		
		
		// Find a condition for each outgoing edge.		
		for (MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> edge : cfg.succEdges(node)) {
			ConditionalAction<ActionType, ConditionType> action = edge.getLabel();
			while(true){
				Collection<StateType> thisTerm = new ArrayList<>();			
				Collection<StateType> othersTerm = new ArrayList<>();
				for (MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> otherEdge : cfg.succEdges(node)) {
					ConditionalAction<ActionType, ConditionType> otherAction = otherEdge.getLabel();
					Collection<StateType> otherEdgeStates = operatorToStates.select1(otherAction.action());

					Collection<StateType> cur;
					if(edge != otherEdge){
						cur = othersTerm;
					}
					else{
						cur = thisTerm;
					}
					cur.addAll(otherEdgeStates);					
				}	

				ConditionType interpol = conditionInferencer.inferSeparator(thisTerm, othersTerm);
				if(interpol != null) { //|| (cost2.apply(interpol, threshold2) == CostFun.INFINITY_COST)){					
					action.setCondition(interpol);
					//StringBuilder debugStr = new StringBuilder();
					//debugStr.append("Found separator: " + interpol + "\n");
					//debugStr.append("-------------------------------------");
					//Globals.printer().printStepCodeFile("separators" + usageCounter + ".txt", debugStr.toString(),
							//"debug information for separators computation");
					//usageCounter++;
					break;
				}
				else {
					return false;
				}
			}
		}					
		return true;
	}
	
	@Override
	public Optional<StateType> execute(StateType input, int maxSteps) {
		Node<StateType, ActionType> current = ENTRY;
		StateType result = input;
		int time = 0;
		while (current != EXIT) {
			if (time > maxSteps) {
				//result = result.invalidState("Timed out after " + maxSteps + " steps!");
				//TODO return error state instance instead
				return Optional.empty();
			}
	
			Node<StateType, ActionType> next = null;
			ConditionalAction<ActionType, ConditionType> nextAction = null;
			int numOfSucc = outDegree(current);
			assert numOfSucc > 0 : "Encountered non-EXIT node with out-degree 0: " + current.toString();
			if (numOfSucc == 1) {
				HashEdge succEdge = succEdges(current).iterator().next();
				next = succEdge.dst;
				nextAction = succEdge.label;
			} else {
				for (HashEdge outEdge : succEdges(current)) {
					Node<StateType, ActionType> succNode = outEdge.dst;
					nextAction = outEdge.label;
					next = succNode;
					boolean guardTest = false;
					if (nextAction.condition != null)
						guardTest = domain.test(nextAction.condition, result);
					if (guardTest){
						break;
					}
				}
			}

			assert next != null;
			Optional<StateType> nextState = domain.apply(nextAction.action, result);
			if (!nextState.isPresent()) {
				break;
			}
			else {
				result = nextState.get();
			}
			current = next;
			time = time + 1;
		}
		return Optional.of(result);
	}

	//inner clases section
	
	/**
	 * A node in the graph.
	 * 
	 * @author romanm
	 */
	public static class Node<StateType, ActionType> implements Comparable<Node<StateType, ActionType>>{
		/**
		 * A set of states from traces that are associated with this node.
		 */
		public Set<StateType> states = new HashSet<>();	

		private Set<String> sources = new HashSet<>();
		private Set<CFG.Label<ActionType>> labels = new HashSet<>();	
		private List<ActionType> path = new ArrayList<>();

		private final String name;
		private static int instanceCounter = 0;

		public Node() {
			this("N" + instanceCounter++);
		}

		public Node(String name) {
			this.name = name;
			sources.add(name);
		}
		
		public Node(Node<StateType, ActionType> other) {
			this(other.name);
			states.addAll(other.states);
			sources.addAll(other.sources);
			labels.addAll(other.getLables());
			path.addAll(other.getPath());
		}
		
		public String getName() {
			return name;
		}
			
		public void addState(StateType state){
			states.add(state);
		}
		
		public void addStates(Set<StateType> otherstates){
			states.addAll(otherstates);
		}
		
		public Set<StateType> getStates() {
			return states;
		}

		public void addSource(String sourceName){
			sources.add(sourceName);
		}
		
		public void addSources(Set<String> othersources){
			sources.addAll(othersources);
		}
		
		public Set<String> getSources(){
			return sources;
		}
		
		public void addLable(CFG.Label<ActionType> l){
			labels.add(l);
		}
		
		public void addLables(Set<CFG.Label<ActionType>> l){
			labels.addAll(l);
		}
		
		public Set<CFG.Label<ActionType>> getLables(){
			return labels;
		}
		
		public List<ActionType> getPath() {
			return path;
		}

		public void setPath(List<ActionType> path) {
			this.path = path;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			Node<?, ?> other = (Node<?, ?>) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public int compareTo(Node<StateType, ActionType> arg0) {
			return name.compareTo(arg0.name);
		}
	}
	
	public static class ConditionalAction<ActionType, ConditionType> {
		private final ActionType action;
		private ConditionType condition;

		public ConditionalAction(ActionType action) {
			this.action = action;
		}
		
		public ConditionalAction(ConditionalAction<ActionType, ConditionType> other) {
			this.action = other.action();
			//ignore the guard of the other Action
			//the guard is context dependent and has no meaning in the new Action
		}

		public ActionType action() {
			return action;
		}

		public ConditionType condition() {
			return condition;
		}

		public void setCondition(ConditionType condition) {
			this.condition = condition;
		}

		@Override
		public String toString() {
			if (condition != null) {
				return condition.toString() + "=>" + action.toString();
			}
			else {
				return action.toString();
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((action == null) ? 0 : action.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ConditionalAction<?,?> other = (ConditionalAction<?,?>) obj;
			if (action == null) {
				if (other.action != null) {
					return false;
				}
			} else if (!action.equals(other.action)) {
				return false;
			}
			return true;
		}
	}
	
	public static class Label<T>{
		private T value;

		public Label(T value) {
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value.toString();
		}
	}
	
	public static class Revert<StateType, ActionType, ConditionType>{		
		private Node<StateType, ActionType> nodeToSplit;
		private Node<StateType, ActionType> origNode;
		private Set<MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>>> edgesToRemove;
		private Set<MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>>> edgesToAdd;
		
		private boolean isEntry;
		
		public Revert(Node<StateType, ActionType> nodeToSplit, Node<StateType, ActionType> origNode) {
			this.nodeToSplit = nodeToSplit;
			this.origNode = origNode;
			this.edgesToRemove = new HashSet<>();;
			this.edgesToAdd = new HashSet<>();;
			this.isEntry = false;
		}
		
		
		public Set<MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>>> getEdgesToRemove() {
			return edgesToRemove;
		}

		public void setEdgesToRemove(Set<MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>>> edgesToRemove) {
			this.edgesToRemove = edgesToRemove;
		}
		
		public void addEdgeToRemove(MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> edgeToRemove) {
			this.edgesToRemove.add(edgeToRemove);
		}

		public Set<MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>>> getEdgesToAdd() {
			return edgesToAdd;
		}

		public void setEdgesToAdd(Set<MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>>> edgesToAdd) {
			this.edgesToAdd = edgesToAdd;
		}
		
		public void addEdgeToToAdd(MultiGraph.Edge<Node<StateType, ActionType>, ConditionalAction<ActionType, ConditionType>> edgeToAdd) {
			this.edgesToAdd.add(edgeToAdd);
		}

		public boolean isEntry() {
			return isEntry;
		}


		public void setEntry() {
			this.isEntry = true;
		}


		public Node<StateType, ActionType> getNodeToSplit() {
			return nodeToSplit;
		}


		public void setNodeToSplit(Node<StateType, ActionType> nodeToSplit) {
			this.nodeToSplit = nodeToSplit;
		}


		public Node<StateType, ActionType> getOrigNode() {
			return origNode;
		}


		public void setOrigNode(Node<StateType, ActionType> origNode) {
			this.origNode = origNode;
		}
		
		@Override
		public String toString() {
			return nodeToSplit + "->" + nodeToSplit + "," + origNode;
		}
	}
}
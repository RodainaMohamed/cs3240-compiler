import java.util.HashSet;

public class State {
	private String label = "";
	private HashSet<Transition> transitions = new HashSet<Transition>();
	private Boolean accepts = false;
	private NFA nfa;
	public Terminals klass;
	
	private static int counter = 0;
	
	public StateSet set; // state set that was used to generate this state
	
	/* Constructors */
	public State() {
		this(""+counter);
		counter++;
	}
	
	public State(String label) {
		this.label = label;
	}

	/* Getters and setters */
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public NFA getNFA() {
		return nfa;
	}

	public void setNFA(NFA nfa) {
		this.nfa = nfa;
	}
	
	public HashSet<Transition> getTransitions() {
		return transitions;
	}

	public Boolean getAccepts() {
		return accepts;
	}

	public void setAccepts(Boolean accepts) {
		this.accepts = accepts;
		
		// If there's an NFA, tell it to update the accepting list
		if (nfa != null)
			nfa.setAccepts(this, accepts);
	}

	/* Transitions */
	public Boolean hasTransition(Character on, State to) {
		return false;
	}

	public Transition addTransition(Character on, State to) {
		Transition transition = new Transition(this, on, to);
		return addTransition(transition);
	}
	
	public Transition addTransition(Transition transition) {
		this.transitions.add(transition);
		return transition;
	}

	public Boolean deleteTransition(Character on, State to) {
		return false;
	}
	
	public boolean equals(Object other) {
		if (other instanceof State) {
			State state = (State)other;
			if (state == other) return true;
//			return label.equals(state.getLabel()) &&
//					accepts == state.getAccepts() &&
//					nfa.equals(state.getNFA());
			return false;
		} else {
			return false;
		}
	}
	
	public String toString() {
		String string = "";
		if (getAccepts()) string += "\u2713";
		string += label;
		return string;
	}
}

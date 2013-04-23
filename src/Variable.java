import java.util.HashSet;
import java.util.Set;


public class Variable extends RuleItem {
	private Grammar grammar;
	private String label;
	
	// First set
	private Set<TokenClass> first = new HashSet<TokenClass>();
	// Follow set
	private Set<TokenClass> follow = new HashSet<TokenClass>();

	// Constructors
	public Variable(Grammar grammar, String label) {
		super();
		this.grammar = grammar;
		this.label = label;
	}
	
//	public Variable(String label) {
//		super();
//		this.label = label;
//	}

	// Getters/setters
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Set<TokenClass> getFirst() {
		return first;
	}

	public Set<TokenClass> getFollow() {
		return follow;
	}
	
	// First/follow sets
	public boolean addToFirst(TokenClass klass) {
		return first.add(klass);
	}
	
	public boolean addAllToFirst(Set<TokenClass> klasses) {
		return first.addAll(klasses);
	}
	
	public boolean addToFollow(TokenClass klass) {
		return follow.add(klass);
	}
	
	public boolean addAllToFollow(Set<TokenClass> klasses) {
		return follow.addAll(klasses);
	}
	
	// Utility
	public String toString() {
		return "<"+label+">";
	}
	
	public int hashCode() {
		return label.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Variable))
			return false;

		Variable var = (Variable) obj;
		return this.label.equals(var.label);
	}
}

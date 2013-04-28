import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Stack;


public class ParseTable {
	private Grammar grammar;
	private Rule[][] table;
	private List<Variable> varList;
	private List<Terminal> termList;
	
	// Constructors
	public ParseTable(Grammar grammar) {
		this.grammar = grammar;
		constructTable();
	}

	// Getters/setters
	public Grammar getGrammar() {
		return grammar;
	}
	
	public Rule[][] getTable() {
		return table;
	}
	
	public void constructTable() {
		varList = grammar.getOrderedVars();
		termList = new ArrayList<Terminal>(grammar.getParser().getTokenClasses());
		termList.add(new DollarTerminal(grammar));
		
		table = new Rule[varList.size()][termList.size()];
		
		// For each rule
		for (Rule rule : grammar.getRules()) {
			int varI = varList.indexOf(rule.getVariable());
			
			boolean hasEpsilon = false;
			
			// For each token in First(rule)
			for (Terminal term : rule.getFirst()) {
				if (term.isEpsilon()) {
					hasEpsilon = true;
					continue;
				}
				
				int termI = termList.indexOf(term);
				
				if (table[varI][termI] == null) {
					table[varI][termI] = rule;
				} else {
					invalid(varI, termI, rule);
				}
			}
			
			// If epsilon in First(rule)
			if (hasEpsilon) {
				// For each token in Follow(rule)
				for (Terminal term : rule.getVariable().getFollow()) {
					int termI = termList.indexOf(term);
					
					if (table[varI][termI] == null) {
						table[varI][termI] = rule;
					} else {
						invalid(varI, termI, rule);
					}
				}
			}
		}
	}
	
	public void invalid(int var, int term, Rule rule) {
		System.out.println("The grammar is not LL(1)! Entries clashed.");
		System.out.println("Cell ["+var+","+term+"] currently has "+table[var][term]+" but tried to assign "+rule);
		System.exit(0);
	}
	
	public void walk(Scanner input) {
		List<Token> tokens = grammar.getParser().scan(input);
		
		TokenStream ts = new TokenStream(tokens);
		Stack<RuleItem> stack = new Stack<RuleItem>();
		
		// Push start variable
		stack.push(grammar.getStart());
		
		// While there is something on the stack
		// And we have input left
		while (!stack.isEmpty() && !ts.isConsumed()) {
			if (stack.peek().isVariable()) {
				// Find a substitution
				Rule rule = getRuleFor((Variable)stack.peek(), ts.peekToken().getKlass());
				
				// Is there a substitution rule?
				if (rule != null) {
					// Pop the variable
					stack.pop();
					
					// Push the rule items from right to left
					List<RuleItem> items = rule.getItems();
					ListIterator<RuleItem> li = items.listIterator(items.size());
					
					while(li.hasPrevious()) {
						stack.push(li.previous());
					}
				} else {
					invalid((Variable)stack.peek(), ts.peekToken().getKlass(), ts);
				}
			} else {
				// It's a terminal. Match it!
				if (ts.matchToken((TokenClass)stack.peek())) {
					// Pop the terminal.
					stack.pop();
				} else {
					invalid((TokenClass)stack.peek(), ts.peekToken().getKlass(), ts);
				}
			}
		}
		
		if (stack.isEmpty() && ts.isConsumed()) {
			System.out.println("Successfully parsed the token stream!");
		} else {
			System.out.println("Invalid input token stream.");
		}
	}
	
	public Rule getRuleFor(Variable var, Terminal term) {
		int vI = varList.indexOf(var);
		int tI = termList.indexOf(term);
		
		if (vI == -1 || tI == -1) return null;
		return table[vI][tI];
	}
	
	public void invalid(Variable var, Terminal term, TokenStream ts) {
		System.out.println("There is no rule for ["+var+","+term+"]!");
		System.out.println(ts.toString());
		System.exit(0);
	}
	
	public void invalid(Terminal sterm, Terminal iterm, TokenStream ts) {
		System.out.println("Stack and input terminals don't match: ["+sterm+","+iterm+"]!");
		System.out.println(ts.toString());
		System.exit(0);
	}
	
	public String toString() {
		MultiColumnPrinter tp = new MultiColumnPrinter(table[0].length+1, 2, "-", MultiColumnPrinter.CENTER, false);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		tp.stream = new PrintStream(baos);

		String[] headers = new String[termList.size()+1];
		headers[0] = "Variable";
		for (int i = 1; i < headers.length; i++) {
			headers[i] = termList.get(i-1).toString();
		}

		tp.addTitle(headers);

		for (int i = 0; i < table.length; i++) {
			String[] row = new String[table[i].length+1];
			row[0] = varList.get(i).toString();
			for (int j = 0; j < row.length-1; j++) {
				if (table[i][j] == null) {
					row[j+1] = "~~";
					continue;
				}
				row[j+1] = table[i][j].toString();
			}
			tp.add(row);
		}

		tp.print();
		try {
			return baos.toString("UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}
}

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Arrays;

/**
 *
 */

/**
 * @author Rochelle
 * 
 */
public class RegExpFunc {
    private static final String SPACE = " ", BSLASH = "/", STAR = "*",
    PLUS = "+", OR = "|", LBRAC = "[", RBRAC = "]", LPAREN = "(",
    RPAREN = ")", PERIOD = ".", APOS = "'", QUOT = "\"",
    IN = "IN", NOT = "^", DASH = "-", DOLLAR = "$";

    private static final Character ESCAPE = new Character('\\');

    private static final Character[] EX_RE_CHAR = { ' ', '\\', '*', '+', '?',
        '|', '[', ']', '(', ')', '.', '\'', '"', '$' };
    private static final Character[] EX_CLS_CHAR = { '\\', '^', '-', '[', ']',
    '$' };

    private static final HashSet<Character> RE_CHAR = new HashSet<Character>(
            Arrays.asList(EX_RE_CHAR));
    private static final HashSet<Character> CLS_CHAR = new HashSet<Character>(
            Arrays.asList(EX_CLS_CHAR));

    private ArrayList<Terminals> classes = Parser.getClasses();

    private String input;
    private InputStream is;
    private char lastCharAdded;

    /* Constructor */
    public RegExpFunc(String input) {
        this.input = new String(input);
        this.is = new InputStream(this.input);
    }

    private boolean matchToken(String token) {
        return is.matchToken(token);
    }

    private String peekToken() {
        if (is.peekToken() == null)
            return null;
        String s = String.valueOf(is.peekToken());
        System.out.println("Current regex char: " + s);
        return s;
    }

    private boolean peekToken(String token) {
        return is.peekToken(token);
    }

    private boolean peekEscaped(HashSet<Character> escaped) {
        debug();
        if (is.isConsumed())
            return false;

        // Escaped character?
        if (is.peekToken(ESCAPE)) {
            return escaped.contains(is.peekToken(1));
        } else {
            return !escaped.contains(is.peekToken());
        }
    }

    private Character matchEscaped(HashSet<Character> escaped) {
        if (is.isConsumed())
            return null;

        // Escaped character?
        Character token;
        if (is.peekToken(ESCAPE)) {
            token = is.peekToken(1);
            is.advancePointer(2);
        } else {
            token = is.peekToken();
            is.advancePointer();
        }

        return token;
    }

    private boolean peekReToken() {
        return peekEscaped(RE_CHAR);
    }

    private boolean peekClsToken() {
        return peekEscaped(CLS_CHAR);
    }

    private Character matchReToken() {
        return matchEscaped(CLS_CHAR);
    }

    private Character matchClsToken() {
        return matchEscaped(CLS_CHAR);
    }

    private void invalid() {
        System.out.println("Invalid Syntax");
        System.exit(0);
    }

    private void debug() {
    	if (true) {
	        System.out.println("Pointer at: " + peekToken());
	        System.out.println(Thread.currentThread().getStackTrace()[2]);
    	}
    }

    private String definedClass() {
        debug();

        for (int i = 0; i < classes.size(); i++) {
            String name = classes.get(i).getName();
            if (is.peekToken(name)) {
                is.matchToken(name);
                return name;
            }
        }
        return null;
    }

    public void addToHashSet(String className, Character c) {
        for (int i = 0; i < classes.size(); i++) {
            if (classes.get(i).getName().equals(className)){
                classes.get(i).addChar(c);
                lastCharAdded = c;
            }
        }
    }

    public HashSet completeHashSet(String className, Character c) {
        HashSet<Character> classHash;
        for (int i = 0; i < classes.size(); i++) {
            if (classes.get(i).getName().equals(className)) {
                classHash = classes.get(i).getChars();
                for (int j = lastCharAdded; j <= c; j++) {
                    classHash.add((char) j);
                }
                return classHash;
            }
        }
        return null;
    }
    
    public HashSet<Character> exclude(HashSet<Character> set1, HashSet<Character> set2){
        Iterator<Character> iter = set1.iterator();
        HashSet<Character> newHashSet = (HashSet<Character>) set2.clone();
        while(iter.hasNext()){
            Character c = iter.next();
            if(newHashSet.contains(c))
                newHashSet.remove(c);
        }
        return newHashSet;
    }

    public HashSet<Character> getClass(String className){
        for(int i=0; i<classes.size(); i++){
            if(classes.get(i).getName().equals(className))
                return classes.get(i).getChars();
        }
        return null;
    }

    private void setClass(String className, HashSet<Character> exclude) {
        for(int i=0; i<classes.size(); i++){
            if(classes.get(i).getName().equals(className))
                classes.get(i).setChars(exclude);
        }        
    }

    private NFA createNFA(HashSet set){
        State s = new State();
        State t = new State();
        NFA nfa = new NFA(s);
        nfa.setAccepts(t, true);
        Iterator<Character> iter = set.iterator();
        while (iter.hasNext()) {
            nfa.addTransition(s, iter.next(), t);
        }
        return nfa;
    }
    
    private NFA getNFA(String className){
        for(int i=0; i<classes.size(); i++){
            if(classes.get(i).getName().equals(className))
                return classes.get(i).getNFA();
        }
        return null;
    }
    
    public NFA origRegExp(String className) {
        debug();
        return regExp(className);
    }

    public NFA regExp(String className) {
        debug();
        NFA nfa = regExOne(className);
        if (peekToken(OR)) {
            nfa = NFA.union(nfa, regExPrime(className));
        }
        return nfa;
    }

    public NFA regExPrime(String className) {
        debug();
        if (peekToken(OR)) {
            matchToken(OR);
            NFA nfa = regExOne(className);
            nfa = NFA.concat(regExPrime(className), nfa);
            return nfa;
        } else {
            return null;
        }
    }

    public NFA regExOne(String className) {
        debug();
        NFA nfa = regExTwo(className);
        nfa = NFA.concat(nfa, regExOnePrime(className));
        return nfa;
    }

    public NFA regExOnePrime(String className) {
        debug();
        if (peekToken(LPAREN) || peekToken(PERIOD) || peekToken(LBRAC)
                || peekToken(DOLLAR) || peekReToken()) {
            NFA nfa = regExTwo(className);
            nfa = NFA.concat(nfa, regExOnePrime(className));
            return nfa;
        } else {
            return null;
        }
    }

    public NFA regExTwo(String className) {
        debug();
        NFA nfa = null;
        if (peekToken(LPAREN)) {
            matchToken(LPAREN);
            nfa = regExp(className);
            if (peekToken(RPAREN)) {
                matchToken(RPAREN);
                return regExTwoTail(className, nfa);
            } else {
                invalid();
                return null;
            }
        }
        else if (peekReToken()) {
            char reChar = matchReToken();
            addToHashSet(className, reChar);
            return regExTwoTail(className, nfa);
        }
        else {
            if (peekToken(PERIOD) || peekToken(LBRAC) || peekToken(DOLLAR)) {
                nfa = regExThree(className);
                return nfa;
            } else {
                invalid();
                return null;
            }
        }
    }

    public NFA regExTwoTail(String className, NFA nfa) {
        debug();
        if (peekToken(STAR)) {
            matchToken(STAR);
            return NFA.star(nfa);
        } else if (peekToken(PLUS)) {
            matchToken(PLUS);
            return NFA.plus(nfa);
        } else {
            return nfa;
        }
    }

    public NFA regExThree(String className) {
        debug();
        NFA nfa = null;
        if (peekToken(PERIOD) || peekToken(LBRAC) || peekToken(DOLLAR)) {
            nfa = charClass(className);
            return nfa;
        } else if (peekToken("null")) {
            return null;
        } else {
            invalid();
            return null;
        }
    }

    public NFA charClass(String className) {
        debug();
        NFA nfa = null;
        /** needs major fixing----------------------------------------------------------------------------*/
        if (peekToken(PERIOD)) {
            matchToken(PERIOD);
            State s = new State();
            State a = new State();
            nfa = new NFA(s);
            nfa.setAccepts(a, true);
            for(char c=' '; c >= '~'; c++){
                getClass(className).add(c);
            }
            return createNFA(getClass(className));
            /**-----------------------------------------------------------------------------------------------*/
        } else if (peekToken(LBRAC)) {
            matchToken(LBRAC);
            nfa = charClassOne(className);
            return nfa;
        }
        else if (peekToken(DOLLAR)) {
            matchToken(DOLLAR);
            String name = definedClass();
            return getNFA(name);
        } else {
            invalid();
            return null;
        }
    }

    public NFA charClassOne(String className) {
        debug();
        if (peekClsToken() || peekToken(RBRAC)) {
            NFA nfa = charSetList(className);
            return nfa;
        } else if (peekToken(NOT)) {
            NFA nfa = excludeSet(className);
            return nfa;
        } else {
            invalid();
            return null;
        }
    }

    public NFA charSetList(String className) {
        debug();
        if (peekClsToken()) {
            charSet(className);
            charSetList(className);
            return createNFA(getClass(className));
        } else if (peekToken(RBRAC)) {
            matchToken(RBRAC);
            return null;
        } else {
            invalid();
            return null;
        }
    }

    public void charSet(String className) {
        debug();
        System.out.println(peekToken());
        System.out.println(is.getPointer());
        if (peekClsToken()) {
            Character hashChar = matchClsToken();
            addToHashSet(className, hashChar);
            charSetTail(className);
        } else {
            invalid();
        }
    }

    public void charSetTail(String className) {
        debug();
        if (peekToken(DASH)) {
            matchToken(DASH);
            if (peekClsToken()) {
                char c = matchClsToken();
                completeHashSet(className, c);
            } else {
                invalid();
            }
        } else {
            return;
        }
    }

    /**
     * ------------------------------------------------------------------------
     * --------------------------
     */
    // Special case with IN exclusion so make sure to pay detailed attention to
    // this part
    public NFA excludeSet(String className) {
        debug();
        NFA nfa = null;
        // Assumption made that there is an " " then "IN" then another " "
        if (peekToken(NOT)) {
            matchToken(NOT);
            charSet(className);
            if (peekToken(RBRAC)) {
                matchToken(RBRAC);
                if (peekToken(IN)) {
                    matchToken(IN);
                    if (peekToken(DOLLAR)) {
                        excludeSetTail(className);
                        nfa = createNFA(getClass(className));
                    }
                    return nfa;
                } else {
                    invalid();
                    return null;
                }
            }
            else {
                invalid();
                return null;
            }
        }
        else {
            invalid();
            return null;
        }
    }

        public void excludeSetTail(String className) {
            debug();
            if (peekToken(LBRAC)) {
                matchToken(LBRAC);
                charSet(className);
                if (peekToken(RBRAC)) {
                    matchToken(RBRAC);
                } else {
                    invalid();
                }
                
            }
            // ***********************************Not sure what to do for
            // transition***********************************
            else {
                matchToken(DOLLAR);
                String name = definedClass();
                HashSet<Character> hashSet1 = getClass(className);
                HashSet<Character> hashSet2 = getClass(name);
                setClass(className, exclude(hashSet1, hashSet2));
            }
        }
    }

import java.util.ArrayList;
import java.util.Collections;

public class CNFExpression {
	public ArrayList<CNFSymbol> symbols;
	
	public CNFExpression() {
		this.symbols = new ArrayList<CNFSymbol>();
	}
	
	public static ArrayList<CNFExpression> expressionToCNFList(Expression e) throws Exception	{
		ArrayList<CNFExpression> expressions = new ArrayList<CNFExpression>();
		if(e.operation!=Expression.OpType.And) {
			expressions.add(new CNFExpression(e));
		}	else	{
			for(Expression child : e.children) {
				expressions.add(new CNFExpression(child));
			}
		}
		for(CNFExpression c : expressions)	{
			Collections.sort(c.symbols);
		}
		
		CNFExpression.filterOutTrueCNFExp(expressions);		
		return expressions;
	}
	
	public CNFExpression(Expression e) throws Exception	{
		//e must be in CNF form and have an OR operator, NOT operator, or be a SYMBOL
		symbols = new ArrayList<CNFSymbol>();
		if(e.symbol!=null) {
			symbols.add(new CNFSymbol(e.symbol.name));
			return;
		}
		switch(e.operation) {
		case Or:
			for(Expression child : e.children) {
				CNFSymbol symbolToAdd;
				if(child.symbol!=null) {
					symbolToAdd = new CNFSymbol(child.symbol.name);
				}	else	{
					symbolToAdd = new CNFSymbol(child.children.get(0).symbol.name);
					symbolToAdd.isNegated = true;
				}
				this.symbols.add(symbolToAdd);
			}
			return;
		case Not:
			CNFSymbol symbolToAdd = new CNFSymbol(e.children.get(0).symbol.name);
			symbolToAdd.isNegated = true;
			symbols.add(symbolToAdd);	
			return;
		default:
			throw new Exception("Expression " + e.expressionText() + " could not convert to a CNFExpression");			
		}
	}
	
	public void removeDuplicates()	{
		ArrayList<CNFSymbol> symbolsToRemove = new ArrayList<CNFSymbol>();
		for(int i=0; i<this.symbols.size()-1; i++)	{
			CNFSymbol s1 = this.symbols.get(i);
			for(int j=i+1; j<this.symbols.size(); j++)	{
				CNFSymbol s2 = this.symbols.get(j);
				if(s1.name.equals(s2.name) && !CNFSymbol.IsCompliment(s1, s2)) {symbolsToRemove.add(s1);}
			}
		}
		this.symbols.removeAll(symbolsToRemove);
	}
	
	public static void fullyInferFrom(ArrayList<CNFExpression> statements, ArrayList<CNFExpression> KB)	{
		CNFExpression.filterOutTrueCNFExp(statements);
		for(CNFExpression KBStatement : KB) {
			ArrayList<CNFExpression> inferredExpressions = new ArrayList<CNFExpression>();
			for(CNFExpression statement : statements) {
				CNFExpression inferredExpression = statement.attemptCombine(KBStatement);
				if(inferredExpression!=null && !statements.contains(inferredExpression) && !inferredExpressions.contains(inferredExpression)) {					
					inferredExpressions.add(inferredExpression);
				}
			}
			CNFExpression.filterOutTrueCNFExp(inferredExpressions);
			statements.addAll(inferredExpressions);		
			System.out.println("Statements: ");
			CNFExpression.printList(statements);
		}
	}
	
	public static boolean listIsFalseTautology(ArrayList<CNFExpression> expressions) {
		CNFExpression metaExp = new CNFExpression();
		for(CNFExpression e : expressions) {
			if(e.symbols.size()==1) {
				metaExp.symbols.add(e.symbols.get(0));
			}
		}
		if(metaExp.isTrueTautology()) {return true;}	else	{
			return false;
		}
	}
	
	public static void filterOutTrueCNFExp(ArrayList<CNFExpression> expressions)	{
		ArrayList<CNFExpression> trueTauts = new ArrayList<CNFExpression>();
		for(CNFExpression e : expressions)	{
			if(e.isTrueTautology())	{trueTauts.add(e);}
		}
		expressions.removeAll(trueTauts);
	}
	
	public boolean isTrueTautology()	{
		for(int i=0; i<this.symbols.size()-1; i++)	{
			CNFSymbol s1 = this.symbols.get(i);
			for(int j=i+1; j<this.symbols.size(); j++)	{
				CNFSymbol s2 = this.symbols.get(j);
				if(s1.name.equals(s2.name) && CNFSymbol.IsCompliment(s1, s2)) {return true;}
			}
		}
		return false;
	}
	
	public boolean isEqualTo(CNFExpression e) {
		Collections.sort(this.symbols);
		Collections.sort(e.symbols);
		if(this.symbols.size()!=e.symbols.size()) {return false;}
		for(int i=0; i<this.symbols.size(); i++)	{
			CNFSymbol s1 = this.symbols.get(i);
			CNFSymbol s2 = e.symbols.get(i);
			if(!(s1.equals(s2))) {return false;}
		}
		return true;
	}
	
	public CNFExpression attemptCombine(CNFExpression e) {
		if(this.isEqualTo(e)) {return null;}
		CNFExpression combined = new CNFExpression();
		combined.symbols.addAll(this.symbols);
		combined.symbols.addAll(e.symbols);
		combined.removeDuplicates();
		
		CNFSymbol removed1, removed2;
		
		for(CNFSymbol s1 : this.symbols) {
			
			for(CNFSymbol s2 : e.symbols) {
				if(CNFSymbol.IsCompliment(s1, s2))	{
					removed1 = s1;
					removed2 = s2;
					combined.symbols.remove(removed1);
					combined.symbols.remove(removed2);
					return combined;
				}
			}
		}
		
		return null;
		
	}
	
	@Override
	public String toString()	{
		String returnStr = "{";
		for(CNFSymbol s : this.symbols) {
			if(s.isNegated) {returnStr += "-";}
			returnStr+=s.name + ",";
		}
		returnStr = returnStr.substring(0, returnStr.length()-1);
		returnStr+="}";
		return returnStr;
	}
	
	public static void printList(ArrayList<CNFExpression> list)	{
		System.out.println("\nstatements:");
		for(CNFExpression e : list) {
			System.out.println(e.toString());
		}
	}
	
}

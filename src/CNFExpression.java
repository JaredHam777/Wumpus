import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class CNFExpression implements Comparable<CNFExpression> {
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
		for(CNFSymbol s : symbolsToRemove)	{
			this.symbols.remove(this.symbols.indexOf(s));
		}
		
		
	}
	
	public static boolean isConsistentSetOfLiterals(ArrayList<CNFExpression> statements) {
		CNFExpression metaExp = new CNFExpression();
		for(CNFExpression e : statements) {
			if(e.symbols.size()!=1) {return false;}
			metaExp.symbols.add(e.symbols.get(0));
		}
		if(metaExp.isTrueTautology()) {
			return false;
		}
		return true;
	}
	
	public static ArrayList<CNFExpression> unitPropagate(CNFSymbol l, ArrayList<CNFExpression> statements)	{
		ArrayList<CNFExpression> expressionsToRemove = new ArrayList<CNFExpression>();
		for(CNFExpression e : statements) {
			ArrayList<CNFSymbol> symbolsToRemove = new ArrayList<CNFSymbol>();
			for(CNFSymbol s : e.symbols) {
				if(l.equals(s) && e.symbols.size()>1) {
					expressionsToRemove.add(e);
				}
				if(CNFSymbol.IsCompliment(l, s))	{
					symbolsToRemove.add(s);
					if(e.symbols.size()==1)	{
						//System.out.println("f");
					}
				}
			}
			
			e.symbols.removeAll(symbolsToRemove);
			
		}
		return expressionsToRemove;
	}
	
	public static void pureLiteralAssign(ArrayList<CNFSymbol> pures, ArrayList<CNFExpression> statements) {
		ArrayList<CNFExpression> expressionsToRemove = new ArrayList<CNFExpression>();
		for(CNFExpression e : statements)	{
			
			for(CNFSymbol s : pures) {
				if(e.symbols.contains(s))	{					
					expressionsToRemove.add(e);
					break;
				}
			}
			
		}
		statements.removeAll(expressionsToRemove);
	}
	
	public static boolean containsEmptyClause(ArrayList<CNFExpression> statements) { 
		for(CNFExpression e : statements) {
			if(e.symbols.size()==0) {return true;}
		}
		return false;
	}
	
	
	
	public static ArrayList<CNFSymbol> getPureLiterals(ArrayList<CNFExpression> statements) {
		ArrayList<CNFSymbol> allLiterals = new ArrayList<CNFSymbol>();
		
		
		for(CNFExpression e : statements) {
			if(e.symbols.size()==0) {continue;}
			for(CNFSymbol s : e.symbols) {
				if(!allLiterals.contains(s))	{
					allLiterals.add(s);									
				}
			}
		}
		ArrayList<CNFSymbol> literalsToRemove = new ArrayList<CNFSymbol>();
		for(int i=0; i<allLiterals.size()-1; i++) {
			CNFSymbol s1 = allLiterals.get(i);
			for(int j=i+1; j<allLiterals.size(); j++)	{
				CNFSymbol s2 = allLiterals.get(j);
				if(CNFSymbol.IsCompliment(s1, s2)) {
					literalsToRemove.add(s1);
					literalsToRemove.add(s2);
				}
			}
		}
		allLiterals.removeAll(literalsToRemove);
		return allLiterals;
	}
	
	public static CNFExpression chooseLiteral(ArrayList<CNFExpression> statements)	{
		CNFExpression e = new CNFExpression();
		for(CNFExpression statement : statements) {
			if(statement.symbols.size()!=0) {
				e.symbols.add(statement.symbols.get(0));
				return e;
			}
		}
		return null;
	}
	
	
	//returns true if there is a possible solution
	//returns true if logically consistent
	public static boolean DPLL(ArrayList<CNFExpression> statements) {
		
		//System.out.println("");
		
		
		
		if(isConsistentSetOfLiterals(statements)) {
			return true;
		}
		if(containsEmptyClause(statements)) {return false;}
		//CNFExpression.printList(statements);
		ArrayList<CNFExpression> expressionsPropagated = new ArrayList<CNFExpression>();
		for(CNFExpression e : statements) {
			if(e.symbols.size()==1)	{
				if(e.symbols.get(0).name.equals("B_3_4"))	{
					//System.out.print(e);
					//CNFExpression.printList(statements);
				}
				expressionsPropagated.addAll(unitPropagate(e.symbols.get(0), statements));
				}			
			
		}
		Collections.sort(statements);
		
		Collections.sort(statements);
		statements.removeAll(expressionsPropagated);
		//Collections.sort(statements);
		ArrayList<CNFSymbol> pureLiterals = getPureLiterals(statements);
		pureLiteralAssign(pureLiterals, statements);
		
		Collections.sort(statements);
		ArrayList<CNFExpression> s1 = new ArrayList<CNFExpression>();
		ArrayList<CNFExpression> s2 = new ArrayList<CNFExpression>();
		

		if(statements.size()==0) {
			return true;
			}
		CNFExpression chosenLiteral = chooseLiteral(statements);
		CNFExpression inverseChosenLiteral = chooseLiteral(statements);
		inverseChosenLiteral.symbols.get(0).isNegated = !inverseChosenLiteral.symbols.get(0).isNegated;
		
		for(CNFExpression e : statements) {
			s1.add(CNFExpression.copyExpression(e));
			s2.add(CNFExpression.copyExpression(e));
		}
		
		s1.add(chosenLiteral);
		s2.add(inverseChosenLiteral);
		Collections.sort(s1);
		Collections.sort(s2);
		return DPLL(s1) || DPLL(s2);
		
		
	}
	
	public static boolean isLogicallyConsistent(ArrayList<CNFExpression> statements) {
	
		Collections.sort(statements);
		//CNFExpression.printList(statements);
		if(CNFExpression.listIsFalseTautology(statements)){
			return false;
		}
		ArrayList<CNFExpression> singulars = new ArrayList<CNFExpression>();
		for(CNFExpression e : statements) {
			if(e.symbols.size()==1) {
				singulars.add(e);
			}
		}
		
		//CNFExpression.printList(statements);
		
		for(int i=0; i<statements.size()-1; i++) {
			CNFExpression s1 = statements.get(i);
			for(int j=i+1; j<statements.size(); j++) {
				CNFExpression s2 = statements.get(j);
				CNFExpression s3 = s1.attemptCombine(s2);
				if(s3!=null && !statements.contains(s3) && !s3.isTrueTautology()) {

					
					//see if we can replace s1 or s2
					if(s1.symbols.size()>1 && s2.symbols.size()==1)	{
						statements.remove(s1);
					}	else	
					if(s2.symbols.size()>1 && s1.symbols.size()==1)	{
						statements.remove(s2);
					}
					//we extracted a single statement
					if(s3.symbols.size()==1) {
						if(s3.falseProjectionOnto(singulars)) 	{
							return false;
						}						
						singulars.add(s3);
						
					}	
					if(s3.symbols.size()<=s1.symbols.size()) {
						statements.add(i+1, s3);
					}	
					else	{
						for(int k=i; k<statements.size(); k++)	{
							if(statements.get(k).symbols.size()>=s3.symbols.size()) {
								statements.add(k, s3);
								break;
							}
						}						
					}
						
					
					//System.out.println("new statement: " + s3);

				}
			}
		}
		//System.out.println("singulars: ");
		CNFExpression.printList(singulars);
 		return true;
	}
	
	public Boolean falseProjectionOnto(ArrayList<CNFExpression> list) {
		if(this.symbols.size()!=1) {return null;}
		for(CNFExpression e : list) {
			if(e.symbols.size()!=1) {continue;}
			if(CNFSymbol.IsCompliment(this.symbols.get(0), e.symbols.get(0)))	{
				return true;
			}
		}
		return false;
	}
	
	public static CNFExpression copyExpression(CNFExpression e)	{
		CNFExpression returnExp = new CNFExpression();
		returnExp.symbols.addAll(e.symbols);
		return returnExp;
	}
	
	public static boolean statementsContradict(ArrayList<CNFExpression> statements, ArrayList<CNFExpression> KB)	{
		ArrayList<CNFExpression> newStatements = new ArrayList<CNFExpression>();
		
		for(CNFExpression e : KB)	{
			CNFExpression newE = CNFExpression.copyExpression(e);
			newStatements.add(newE);
		}
		for(CNFExpression e : statements)	{
			CNFExpression newE = CNFExpression.copyExpression(e);
			newStatements.add(newE);
		}
		
		
		
		filterOutTrueCNFExp(newStatements);
		Collections.sort(newStatements);
		
		//System.out.println("STARRT");
		//CNFExpression.printList(newStatements);
		
		return !DPLL(newStatements);
		//return !isLogicallyConsistent(statements);
		/*
		CNFExpression.filterOutTrueCNFExp(statements);
		ArrayList<CNFExpression> allInferred = new ArrayList<CNFExpression>();
		allInferred.addAll(statements);
		for(CNFExpression KBStatement : KB) {
			ArrayList<CNFExpression> inferredExpressions = new ArrayList<CNFExpression>();
			for(CNFExpression statement : statements) {
				CNFExpression inferredExpression = statement.attemptCombine(KBStatement);
				if(inferredExpression!=null && inferredExpression.symbols.size()==1 && inferredExpression.symbols.get(0).name.equals("P_1_3"))	{
					System.out.println("inferred -P_1_3");
				}
				if(inferredExpression!=null && inferredExpression.symbols.size()==0) {return true;}
				if(inferredExpression!=null && !allInferred.contains(inferredExpression) && !inferredExpressions.contains(inferredExpression)) {					
					inferredExpressions.add(inferredExpression);
				}
			}
			CNFExpression.filterOutTrueCNFExp(inferredExpressions);
			//statements.addAll(inferredExpressions);		
			allInferred.addAll(inferredExpressions);
			
			if(CNFExpression.listIsFalseTautology(allInferred))	{
				//temp:
				return CNFExpression.listIsFalseTautology(allInferred);
				//return true;
			}
			//System.out.println("Statements: ");
			//CNFExpression.printList(statements);
		}
		
		System.out.println("Final Statements: ");
		CNFExpression.printList(statements);
		
		System.out.println();
		System.out.println("All inferred:");
		CNFExpression.printList(allInferred);
		return false;*/
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
				if(s1.name.equals(s2.name) && CNFSymbol.IsCompliment(s1, s2)) {
					return true;
					}
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
		//System.out.println("combined: " + combined);
		combined.removeDuplicates();
		
		//System.out.println("combined: " + combined);
		
		CNFSymbol removed1, removed2;
		
		for(CNFSymbol s1 : this.symbols) {
			
			for(CNFSymbol s2 : e.symbols) {
				if(CNFSymbol.IsCompliment(s1, s2))	{
					removed1 = s1;
					removed2 = s2;
					combined.symbols.remove(removed1);
					combined.symbols.remove(removed2);
					if(combined.symbols.size()==0) {
						//System.out.println("found false taut");
					}
					if(combined.isTrueTautology()) {return null;}
					//an expression larger than the 2 combined doesn't help us:
					
					if(this.symbols.size()==1 || e.symbols.size()==1) {
						return combined;
					}					
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
	
	@Override
	public boolean equals(Object o) {
		CNFExpression e = (CNFExpression)o;
		if(this.symbols.size()!=e.symbols.size()) {return false;}
		Collections.sort(this.symbols);
		Collections.sort(e.symbols);
		
		for(int i=0; i<this.symbols.size(); i++) {
			if(!this.symbols.get(i).equals(e.symbols.get(i))) {return false;}
		}
		return true;
	}
	
	public static void printList(ArrayList<CNFExpression> list)	{
		System.out.println("\nstatements:");
		for(CNFExpression e : list) {
			System.out.println(e.toString());
		}
	}

	@Override
	public int compareTo(CNFExpression arg0) {
		return this.symbols.size()-arg0.symbols.size();
	}
	
}

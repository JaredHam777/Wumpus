import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Expression {
	public static Expression True;
	public static Expression False;
	
	public enum OpType	{
		Not("not"), If("if"), Iff("iff"), And("and"), Or("or"), Xor("xor");
		OpType(String value){
			this.value = value;
		}
		
		String value;
	}
	OpType operation;
	ArrayList<Expression> children;
	Symbol symbol;
	
	public Expression() {};
	public Expression(Token token) {
		this.symbol = new Symbol(token.value);
	};
	
	private Boolean Associative() {
		if(this.operation != OpType.And && this.operation != OpType.Or) {return false;}
		if(this.children == null) {return false;}
		//find children of this instance whose optype is the same as this instance's optype
		ArrayList<Expression> childrenToAdd = new ArrayList<Expression>();
		ArrayList<Expression> childrenToRemove = new ArrayList<Expression>();
		for(Expression child : this.children) {
			if(this.operation == child.operation)	{
				childrenToAdd.addAll(child.children);
				childrenToRemove.add(child);
			}
		}
		if(childrenToAdd.size()==0) {return false;}
		for(Expression child : childrenToRemove) {
			this.children.remove(child);
		}
		for(Expression child : childrenToAdd)	{
			this.children.add(child);
		}
		return true;
	}
	
	private Boolean NotExpression()	{
		if(this.operation != OpType.Not) {return false;}
		if(this.children.get(0).operation != OpType.And && this.children.get(0).operation != OpType.Or && this.children.get(0).operation != OpType.Not) {return false;}
		switch (this.children.get(0).operation) {
		case Not:
			Expression replacement = new Expression();
			replacement = this.children.get(0).children.get(0);
			this.operation = replacement.operation;
			this.children = replacement.children;
			this.symbol = replacement.symbol;
			return true;
		case And:
			return DeMorgan();
		case Or:
			return DeMorgan();
		default:
			return false;
		}	
	}
	
	private Boolean DeMorgan() {
		if(this.operation != OpType.Not) {return false;}
		if(this.children.get(0).operation != OpType.And && this.children.get(0).operation != OpType.Or)	{return false;}
		
		ArrayList<Expression> childrenToAdd = new ArrayList<Expression>();
		switch (this.children.get(0).operation)	{		
		case And:			
			childrenToAdd.addAll(this.children.get(0).children);
			this.children.remove(0);
			for(int i=0; i<childrenToAdd.size(); i++)	{
				Expression notExp = new Expression();
				notExp.children = new ArrayList<Expression>();
				this.operation = OpType.Or;
				notExp.children.add(childrenToAdd.get(i));
				notExp.operation = OpType.Not;
				this.children.add(notExp);
			}
			return true;
		case Or:			
			childrenToAdd.addAll(this.children.get(0).children);
			this.children.remove(0);
			for(int i=0; i<childrenToAdd.size(); i++)	{
				Expression notExp = new Expression();
				notExp.children = new ArrayList<Expression>();
				this.operation = OpType.And;
				notExp.children.add(childrenToAdd.get(i));
				notExp.operation = OpType.Not;
				this.children.add(notExp);
				
			}
			return true;
		default:
			return false;
		}
		
		
		
	}
	
	private Boolean OrExpression()	{
		if(this.operation != OpType.Or) {return false;}
		
		//find an And child expression and assign any other expression to DExp:
		Expression andExp = null;
		Expression DExp = null;			
		for(int i=0; i<this.children.size(); i++) {
			Expression child = this.children.get(i);
			if(child.operation == OpType.And) {					
				andExp = child; 
				if(i==0) {DExp = this.children.get(i+1);} else	{DExp = this.children.get(0);}
				break;
			}
		}
		
		//If we didn't find an And expression among the children, then this expression is already resolved
		if(andExp==null) {return false;}
		
		this.operation = OpType.And;
		ArrayList<Expression> newChildren = new ArrayList<Expression>();
		for(int i=0; i<andExp.children.size(); i++)	{
			Expression child = new Expression();
			child.operation = OpType.Or;
			child.children = new ArrayList<Expression>();
			child.children.add(DExp);
			child.children.add(andExp.children.get(i));
			newChildren.add(child);
		}
		
		this.children.remove(andExp);
		this.children.remove(DExp);			
		this.children.addAll(newChildren);	
		return true;
	}
	
	private Boolean AndExpression() {
		return false;
	}
	
	private Boolean IfExpression()	{
		if(this.operation != OpType.If) {return false;}
		this.operation = OpType.Or;
		Expression A = this.children.get(0);
		Expression B = this.children.get(1);
		Expression notExp = new Expression();
		notExp.children = new ArrayList<Expression>();
		notExp.operation = OpType.Not;
		notExp.children.add(A);
		this.children.remove(A);
		this.children.remove(B);
		
		this.children.add(notExp);
		this.children.add(B);
		return true;
		
	}
	
	private Boolean IffExpression()	{
		if(this.operation != OpType.Iff) {return false;}
		Expression orExp = new Expression();
		orExp.operation = OpType.Or;
		orExp.children = new ArrayList<Expression>();
		
		Expression and1Exp = new Expression();
		and1Exp.operation = OpType.And;
		and1Exp.children = new ArrayList<Expression>();
		
		Expression and2Exp = new Expression();
		and2Exp.operation = OpType.And;
		and2Exp.children = new ArrayList<Expression>();
		
		Expression not1Exp = new Expression();
		not1Exp.operation = OpType.Not;
		not1Exp.children = new ArrayList<Expression>();
		
		Expression not2Exp = new Expression();
		not2Exp.operation = OpType.Not;
		not2Exp.children = new ArrayList<Expression>();	
		
		and1Exp.children.add(this.children.get(0));
		and1Exp.children.add(this.children.get(1));
		
		not1Exp.children.add(this.children.get(0));
		not2Exp.children.add(this.children.get(1));
		
		and2Exp.children.add(not1Exp);
		and2Exp.children.add(not2Exp);
		
		orExp.children.add(and1Exp);
		orExp.children.add(and2Exp);
		
		this.children = orExp.children;
		this.operation = orExp.operation;
		
		return true;
		
	}
	
	private Boolean XorExpression()	{
		if(this.operation!=OpType.Xor) {return false;}
		
		ArrayList<Expression> andStatements = new ArrayList<Expression>();
		for(int i=0; i<this.children.size(); i++)	{
			Expression andStatement = new Expression();
			andStatement.operation = OpType.And;
			andStatement.children = new ArrayList<Expression>();
			Expression notExp = new Expression();
			notExp.children = new ArrayList<Expression>();
			notExp.operation = OpType.Not;
			notExp.children.add(this.children.get(i));
			for(int j=0; j<this.children.size(); j++)	{
				if(j==i) {
					andStatement.children.add(notExp);
					}	else	{
					andStatement.children.add(this.children.get(j));
				}
			}
			andStatements.add(andStatement);
		}
		this.operation = OpType.Or;
		this.children = new ArrayList<Expression>();
		this.children.addAll(andStatements);
		return true;
	}
	
	public Boolean resolve() {
		if(this.children==null) {return false;}
		
		Boolean resolveStepOccurred = 
		IfExpression() ||
		IffExpression() ||
		XorExpression() ||
		Associative() ||
		NotExpression() ||
		OrExpression() ||
		AndExpression();		
		if(this.children==null) {return resolveStepOccurred;}
		if(resolveStepOccurred) {	
			this.resolve();
			return true;
		}
		resolveStepOccurred = false;
		for(Expression child : this.children) {
			resolveStepOccurred = child.resolve() || resolveStepOccurred;
		}
		if(resolveStepOccurred)	{
			this.resolve();
			return true;
		}
		return false;
	}

	

	public static Expression buildExpression(ArrayList<Token> tokens) {
		//base case:
		if(tokens.get(0).type == Token.Type.Symbol)	{
			return new Expression(tokens.get(0));		
		}	else	{
			Expression e = new Expression();
			e.children = new ArrayList<Expression>();
			e.operation = OpType.valueOf(tokens.get(1).type.toString());
			for(int i = 2; i<tokens.size(); i++) {
				if(tokens.get(i).type == Token.Type.Symbol) {
					e.children.add(new Expression(tokens.get(i)));
					continue;
				}
				if(tokens.get(i).type == Token.Type.OpenParentheses) {
					ArrayList<Token> subList = Token.getTokensInParantheses(tokens.subList(i, tokens.size()));
					e.children.add(buildExpression(subList));
					i+=subList.size()-1;
				}
			}
			return e;
		}
	}
	
	private String expressionToText()	{
		if(this.symbol!=null) {return " " + this.symbol.name;}
		String returnStr = " (" + this.operation.value;
		for(Expression child : this.children) {
			returnStr += child.expressionToText();
		}
		returnStr += ")";
		return returnStr;
	}
	
	public String expressionText()	{
		String text = expressionToText();
		return text.substring(1, text.length());
	}

	
	
	public Boolean isFalseTautology()	{
		ArrayList<Symbol> trueSymbols = new ArrayList<Symbol>();
		ArrayList<Symbol> falseSymbols = new ArrayList<Symbol>();
		for(Expression exp : this.children) {
			if(exp.operation == OpType.And) {continue;}
			if(exp.operation == OpType.Not) {falseSymbols.add(exp.children.get(0).symbol);}
			if(exp.symbol != null)	{trueSymbols.add(exp.symbol);}			
		}
		ArrayList<Symbol> contradictors = new ArrayList<Symbol>(falseSymbols);
		contradictors.retainAll(trueSymbols);
		if(contradictors.size()>0) {
			return true;
		}	else	{
			return false;
		}
		
	}
	
	public String expressionEntails(Expression statement) throws Exception {
		Expression negatedStatement = new Expression();
		negatedStatement.operation = OpType.Not;
		negatedStatement.children = new ArrayList<Expression>();
		negatedStatement.children.add(statement);
		negatedStatement.resolve();
		
		ArrayList<CNFExpression> KBCNF = CNFExpression.expressionToCNFList(this);
		ArrayList<CNFExpression> statementCNF = CNFExpression.expressionToCNFList(statement);
		ArrayList<CNFExpression> negatedStatementCNF = CNFExpression.expressionToCNFList(negatedStatement);
		
		System.out.println("knowledge base: ");
		CNFExpression.printList(KBCNF);
		
		CNFExpression.fullyInferFrom(statementCNF, KBCNF);
		if(CNFExpression.listIsFalseTautology(statementCNF)) {return "definitely false";}
		
		CNFExpression.fullyInferFrom(negatedStatementCNF, KBCNF);
		if(CNFExpression.listIsFalseTautology(negatedStatementCNF)) {return "definitely true";}
		
		return "sometimes true, sometimes false";
	}
	

	public String entails(Expression statement)	{
		//if this expression AND -(statement) proves to be a false tautology, then return true
		Expression inverseStatement = new Expression();
		inverseStatement.operation = OpType.Not;
		inverseStatement.children = new ArrayList<Expression>();
		inverseStatement.children.add(statement);
		inverseStatement.resolve();
		//inverseStatement.firstOrderForm();
		//statement.firstOrderForm();
		


		for(int i=0; i<inverseStatement.children.size(); i++)	{
			Expression s = inverseStatement.children.get(i);			
			ArrayList<Expression> derivedExpressions = new ArrayList<Expression>();
			ArrayList<Expression> newDerivedExpressions = new ArrayList<Expression>();
			if(inverseStatement.isFalseTautology()) {
				return "definitely true";
			}
			
			derivedExpressions = s.projectStatement(this);
			
			//we only want new unique expressions, no duplicates
			for(Expression child : derivedExpressions)	{
				if(!Expression.listContains(inverseStatement.children, child)) {
					newDerivedExpressions.add(child);
				}	else	{
					System.out.println("is this unreachable?");
				}
			}
			
			if(newDerivedExpressions.size()>0) {
				inverseStatement.children.addAll(newDerivedExpressions);
				System.out.println("new statement: " + inverseStatement.expressionText());
				if(inverseStatement.isFalseTautology()) {
					return "definitely true";
				}
			}


		}
		
		for(int i=0; i<statement.children.size(); i++)	{
			Expression s = statement.children.get(i);
			ArrayList<Expression> derivedExpressions = new ArrayList<Expression>();
			ArrayList<Expression> newDerivedExpressions = new ArrayList<Expression>();
			if(statement.isFalseTautology()) {
				return "definitely false";
			}
			
			derivedExpressions = s.projectStatement(this);
			
			//we only want new unique expressions, no duplicates
			for(Expression child : derivedExpressions)	{
				if(!Expression.listContains(statement.children, child)) {
					newDerivedExpressions.add(child);
				}	else	{
					System.out.println("is this unreachable?");
				}
			}
			
			if(newDerivedExpressions.size()>0) {
				statement.children.addAll(newDerivedExpressions);
				System.out.println("new statement: " + inverseStatement.expressionText());
				if(statement.isFalseTautology()) {
					return "definitely false";
				}
			}


		}
		return "sometimes true, sometimes false";		
	}
	public Expression attemptCombine(Expression A)	{
		if(this.operation == OpType.Or && A.operation == OpType.Or) {
			ArrayList<Symbol> falseThisSymbols = new ArrayList<Symbol>();
			ArrayList<Symbol> trueThisSymbols = new ArrayList<Symbol>();
			ArrayList<Symbol> falseASymbols = new ArrayList<Symbol>();
			ArrayList<Symbol> trueASymbols = new ArrayList<Symbol>();
			
			for(Expression child : this.children) {
				if(child.operation==OpType.Not) {falseThisSymbols.add(child.children.get(0).symbol);}else{trueThisSymbols.add(child.symbol);}
			}
			for(Expression child : A.children) {
				if(child.operation==OpType.Not) {falseASymbols.add(child.children.get(0).symbol);}else{trueASymbols.add(child.symbol);}
			}
			
			ArrayList<Symbol> retained1 = new ArrayList<Symbol>(falseThisSymbols);
			retained1.retainAll(trueASymbols);
			ArrayList<Symbol> retained2 = new ArrayList<Symbol>(falseASymbols);
			retained2.retainAll(trueThisSymbols);
			
			Expression combinedExpression = new Expression();
			combinedExpression.children = new ArrayList<Expression>();
			Symbol commonSymbol=null;
			if(retained1.size()>0) {commonSymbol = retained1.get(0);}	else
			if(retained2.size()>0) {commonSymbol = retained2.get(0);}
			
			if(commonSymbol!=null) {
				ArrayList<Symbol> trues = new ArrayList<Symbol>();
				trues.addAll(trueThisSymbols);
				trues.addAll(trueASymbols);
				for(Symbol trueSym : trues) {
					if(trueSym!=commonSymbol) {
						Expression e = new Expression();
						e.symbol = trueSym;
						combinedExpression.children.add(e);
					}					
				}
				ArrayList<Symbol> falses = new ArrayList<Symbol>();
				falses.addAll(falseThisSymbols);
				falses.addAll(falseASymbols);
				for(Symbol falseSym : falses) {
					if(falseSym!=commonSymbol) {
						Expression e = new Expression();
						e.operation = OpType.Not;
						Expression child = new Expression();
						child.symbol = falseSym;
						e.children = new ArrayList<Expression>();
						e.children.add(child);						
						combinedExpression.children.add(e);
					}
				}
			}	else {return null;}
			combinedExpression.operation = OpType.Or;
			return combinedExpression;
			
		}
		if((this.operation == OpType.Or && A.operation != OpType.Or) || (this.operation != OpType.Or && A.operation == OpType.Or))	{
			Expression single;
			Expression orExp;
			if(this.operation == OpType.Or) {
				orExp = this;
				single = A;
			}	else	{
				single = this;
				orExp = A;
			}
			ArrayList<Symbol> falses = new ArrayList<Symbol>();
			ArrayList<Symbol> trues = new ArrayList<Symbol>();
			Symbol compareSymbol;
			for(Expression child : orExp.children) {
				if(child.operation == OpType.Not) {falses.add(child.children.get(0).symbol);}
				if(child.symbol!=null) {trues.add(child.symbol);}
			}
			ArrayList<Symbol> compareList;
			if(single.operation==OpType.Not) {compareList = trues; compareSymbol = single.children.get(0).symbol;}	else	{compareList = falses; compareSymbol = single.symbol;}
			
			if(!compareList.contains(compareSymbol)) {return null;}
			ArrayList<Expression> newChildren = new ArrayList<Expression>();
			newChildren.addAll(orExp.children);
			for(Expression child : orExp.children) {
				if(compareList==trues) {
					if(child.symbol==null) {continue;}
					if(child.symbol == compareSymbol)	{
						newChildren.remove(child);						
					}					
				}
				else if(compareList==falses) {
					if(child.symbol!=null) {continue;}
					if(child.children.get(0).symbol == compareSymbol)	{
						newChildren.remove(child);	
					}
				}
			}
			
			Expression returnExp = new Expression();			
			if(newChildren.size()==1) {
				if(newChildren.get(0).symbol!=null) {
					returnExp.symbol = newChildren.get(0).symbol;		
				}	else	{
					returnExp.symbol = newChildren.get(0).children.get(0).symbol;
				}
				
			}	else	{
				returnExp.children = newChildren;
				returnExp.operation = OpType.Or;				
			}
			return returnExp;
			
		}
		return null;
	}
	
	public ArrayList<Expression> projectStatement(Expression KB) {
		ArrayList<Expression> returnExp = new ArrayList<Expression>();
		for(Expression KBStatement : KB.children) {
			
			Expression combined = this.attemptCombine(KBStatement);
			if(combined!=null )	{
				if((combined.symbol==null && combined.operation != null && combined.children.size()>0) || combined.symbol!=null)	{
					returnExp.add(combined);
				}
			}
		}		
		return returnExp;		
	}
	
	public Boolean solve()	{
		if(this.symbol!=null) {return this.symbol.value;}
		switch (this.operation) {
		case Not:
			return (!this.children.get(0).solve());
		case If:	//0 -> 1
			return !this.children.get(0).solve() || this.children.get(1).solve();
		case Iff:
			return (this.children.get(0).solve() && this.children.get(1).solve()) || (!this.children.get(0).solve() && !this.children.get(1).solve());
		case Or:
			for(int i=0; i<children.size(); i++) {
				if(this.children.get(i).solve()) {return true;}
			}
			return false;
		case And:
			for(int i=0; i<children.size(); i++) {
				if(!this.children.get(i).solve()) {return false;}
			}
			return true;
		case Xor:
			int count = 0;
			for(int i=0; i<children.size(); i++) {
				if(this.children.get(i).solve()) {count++;}
			}
			return (count==1);
		default:
			return null;			
		}
	}
	
	public static boolean listContains(ArrayList<Expression> list, Expression e)	{
		for(Expression child : list) {
			if(child.isEqualTo(e)) {return true;}
		}
		return false;
	}
	
	public boolean isEqualTo(Object o) {
		Expression e = (Expression)o;
		boolean isEqual = true;
		if((e.symbol == null) ^ (this.symbol == null)) {return false;}
		if(e.symbol != null && this.symbol != null) {
			if(this.symbol.name == e.symbol.name) {return true;}	else	{
				return false;
			}
		}
		//both of these have operations:
		for(Expression child : e.children)	{
			isEqual &= Expression.listContains(e.children, child);
			if(!isEqual) {return false;}
		}
		for(Expression child : this.children)	{
			isEqual &= Expression.listContains(this.children, child);
			if(!isEqual) {return false;}
		}		
		return isEqual;
	}
	
}

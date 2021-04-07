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
		if(Main.symbols.containsKey(token.value))	
		{
			this.symbol = Main.symbols.get(token.value);
		}	
		else	{
			Main.symbols.put(token.value, new Symbol(token.value));
			this.symbol = Main.symbols.get(token.value);
		}
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
				childrenToAdd.get(i).operation = OpType.Or;
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
				childrenToAdd.get(i).operation = OpType.Or;
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
	
	public Boolean resolve() {
		if(this.children==null) {return false;}
		
		Boolean resolveStepOccurred = 
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
	
	
}

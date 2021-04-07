


import java.util.ArrayList;

import org.junit.Test;

import junit.framework.TestCase;

public class ExpressionTestCases extends TestCase {
	
	
	
	protected void setUp()	{
		
	}
	
	@Test
	public void testNegationLaw() {
		String sentence = "(not (not M_1_3))";
		ArrayList<Token> tokens = Token.parseInput(sentence);
		Expression exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
		System.out.println(exp.symbol.name + " should be equal to M_1_3");
		assert(exp.symbol.name.equals("M_1_3"));
		
		sentence = "(not (not (not (not M_1_3))))";
		tokens = Token.parseInput(sentence);
		exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
		System.out.println(exp.symbol.name + " should be equal to M_1_3");
		assert(exp.symbol.name.equals("M_1_3"));
		
		sentence = "(not (not (not M_1_3)))";
		tokens = Token.parseInput(sentence);
		exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
		//System.out.println(exp.symbol.name + " should be equal to M_1_3");
		assert(exp.children.get(0).symbol.name.equals("M_1_3") && exp.operation == Expression.OpType.Not);
		
	}
	
	@Test
	public void testAssociativeLaw()	{
		//and associative
		String sentence = "(and M_1_1 (and M_1_2 M_1_3))";
		ArrayList<Token> tokens = Token.parseInput(sentence);
		Expression exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
		assert(exp.children.size()==3 && exp.operation == Expression.OpType.And);
		for(Expression child : exp.children) {
			assert(child.symbol != null && child.operation == null);	//assert each child is a symbol
		}
		
		//or associative
		sentence = "(or M_1_1 (or M_1_2 M_1_3))";
		tokens = Token.parseInput(sentence);
		exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
		assert(exp.children.size()==3 && exp.operation == Expression.OpType.Or);
		for(Expression child : exp.children) {
			assert(child.symbol != null && child.operation == null);	//assert each child is a symbol
		}
	}
	
	@Test
	public void testDistributiveLaw()	{
		String sentence = "(or M_1_1 (and M_1_2 M_1_3))";
		ArrayList<Token> tokens = Token.parseInput(sentence);
		Expression exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
		
		assert(exp.operation == Expression.OpType.And);
		assert(exp.children.size()==2);
		for(Expression child : exp.children) {
			assert(child.operation == Expression.OpType.Or);
			assert(child.children.size()==2);
			for(Expression gChild : child.children) {
				assert(gChild.symbol!=null && gChild.operation==null);
			}
		}
	}
	
	@Test
	public testDeMorgan()	{
		String sentence = "(not (or M_1_1 M_1_2))";
		ArrayList<Token> tokens = Token.parseInput(sentence);
		Expression exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
	}
}

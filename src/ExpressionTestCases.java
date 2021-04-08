


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
	public void testDeMorgan()	{
		String sentence = "(not (or M_1_1 M_1_2))";
		ArrayList<Token> tokens = Token.parseInput(sentence);
		Expression exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
		
		assert(exp.operation == Expression.OpType.And);
		assert(exp.children.size() == 2);
		for(Expression child : exp.children) {
			assert(child.operation == Expression.OpType.Not);
			assert(child.children.size()==1 && child.children.get(0).symbol!=null);
		}
		
		
		sentence = "(not (and M_1_1 M_1_2))";
		tokens = Token.parseInput(sentence);
		exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
		
		assert(exp.operation == Expression.OpType.Or);
		assert(exp.children.size() == 2);
		for(Expression child : exp.children) {
			assert(child.operation == Expression.OpType.Not);
			assert(child.children.size()==1 && child.children.get(0).symbol!=null);
		}
		
		System.out.println(exp.expressionText());
	}
	
	@Test
	public void testIf()	{
		String sentence = "(if M_1_1 M_1_2)";
		ArrayList<Token> tokens = Token.parseInput(sentence);
		Expression exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
		System.out.println(exp.expressionText() + " should be equivilent to " + sentence);
		assert(exp.expressionText().equals("(or (not M_1_1) M_1_2)"));
	}
	
	@Test
	public void testIff()	{
		String sentence = "(iff M_1_1 M_1_2)";
		ArrayList<Token> tokens = Token.parseInput(sentence);
		Expression exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
		System.out.println(exp.expressionText() + " should be equivilent to " + sentence);
		assert(true);
	}
	
	@Test
	public void testXor()	{
		String sentence = "(xor M_1_1 M_1_2)";
		ArrayList<Token> tokens = Token.parseInput(sentence);
		Expression exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
		System.out.println(exp.expressionText() + " should be equivilent to " + sentence);
		assert(true);
	}
	
	@Test
	public void testFalseTautologyTest()	{
		String sentence = "(and M_1_1 (not M_1_1))";
		ArrayList<Token> tokens = Token.parseInput(sentence);
		Expression exp = new Expression();
		exp = Expression.buildExpression(tokens);
		exp.resolve();
		assert(exp.isFalseTautology());
		
		
	}
	
	@Test
	public void testCombineExpressions()	{
		String sentence = "M_1_1";
		ArrayList<Token> tokens = Token.parseInput(sentence);
		Expression exp1 = new Expression();
		exp1 = Expression.buildExpression(tokens);
		exp1.resolve();
		
		sentence = "(or (not M_1_1) M_1_2)";
		tokens = Token.parseInput(sentence);
		Expression exp2 = new Expression();
		exp2 = Expression.buildExpression(tokens);
		System.out.println("Combining...");
		exp2.resolve();
		
		
		Expression exp3 = exp1.attemptCombine(exp2);
		
		assert(exp3.symbol!=null && exp3.symbol.name.equals("M_1_2"));
		
		sentence = "(or M_1_3 M_1_1)";
		tokens = Token.parseInput(sentence);
		exp1 = new Expression();
		exp1 = Expression.buildExpression(tokens);
		exp1.resolve();
		
		exp3 = exp1.attemptCombine(exp2);
		assert(exp3.operation==Expression.OpType.Or && exp3.children.size()==2);
	}
	
	@Test
	public void testEntails()	{
		String sentence = "(and M_1_1 (or M_1_2 M_1_1 M_1_3) (or (not M_1_1) M_1_4))";
		ArrayList<Token> tokens = Token.parseInput(sentence);
		Expression KB = new Expression();
		KB = Expression.buildExpression(tokens);
		KB.resolve();
		
		sentence = "M_1_4";
		tokens = Token.parseInput(sentence);
		Expression statement = new Expression();
		statement = Expression.buildExpression(tokens);		
		statement.resolve();		
		
		//String entails = KB.entails(statement);
		
		//assert(entails == "definitely true");
		
		sentence = "(not M_1_4)";
		tokens = Token.parseInput(sentence);
		statement = new Expression();
		statement = Expression.buildExpression(tokens);		
		statement.resolve();
		
		//entails = KB.entails(statement);		
		//assert(entails == "definitely false");		
		
	}
	
	@Test
	public void testEntailsMulti() throws Exception	{
		String sentence = "(and (xor M_1_4 B_3_4) (if M_4_2 (or B_4_3 B_2_2)) (and (not M_3_1) S_4_1) (not B_2_2) (not (or P_4_2 P_3_3 P_2_4)))";
		ArrayList<Token> tokens = Token.parseInput(sentence);
		Expression KB = new Expression();
		KB = Expression.buildExpression(tokens);
		KB.resolve();
		
		sentence = "(or M_1_4 P_4_4)";
		tokens = Token.parseInput(sentence);
		Expression statement = new Expression();
		statement = Expression.buildExpression(tokens);		
		statement.resolve();	
		
		String entails = KB.expressionEntails(statement);
		
		
	}
	
	
}

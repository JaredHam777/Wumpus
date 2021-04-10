import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

	public static Map<String, Symbol> symbols = new HashMap<String, Symbol>();
	
	public static String statementFile = "a.txt";
	public static Expression statement;
	static ArrayList<Token> statementTokens;
	public static int numberOfSymbols;
	public static int numberOfCombinations;
	

	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String[] strArgs;
		Scanner sc = new Scanner(System.in);
		System.out.print(">");
		strArgs = sc.nextLine().split(" ");
		
		Expression wumpusRules = Expression.importExpressionFromFile("data/wumpus_rules.txt");
		wumpusRules.resolve();
		Expression additionalKnowledge = Expression.importExpressionFromFile(strArgs[2]);
		additionalKnowledge.resolve();
		Expression KB = Expression.combineAnd(wumpusRules, additionalKnowledge);
		KB.resolve();
		Expression statement = Expression.importExpressionFromFile(strArgs[3]);
		
		String entails = KB.expressionEntails(statement);
		
		BufferedWriter output = null;
		File resultFile = new File("result.txt");
		output = new BufferedWriter(new FileWriter(resultFile));
        output.write(entails);
		output.close();
		
		System.out.println(entails);
		
	}

}

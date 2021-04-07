import java.io.File;
import java.io.FileNotFoundException;
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
	
	public static void populateSymbols(int hashVal) {
		String binaryStr = "00000000000000000000000000000000" + Integer.toBinaryString(hashVal);
		binaryStr = binaryStr.substring(binaryStr.length()-numberOfSymbols, binaryStr.length());
		System.out.println("evaluating with truth column: " + binaryStr);
		
		int i=0;
		for(Symbol s : symbols.values()) {
			s.value = Integer.parseInt(binaryStr.substring(i,i+1)) != 0;
			System.out.println(s.name + ": " + s.value);
			i++;
		}
		
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		File file = new File("data/" + statementFile);
		Scanner sc = new Scanner(file);			
		String line;		
		
		
		
		while(sc.hasNextLine())	{
			line = sc.nextLine();
			statementTokens = Token.parseInput(line);			
		}
		statement = Expression.buildExpression(statementTokens);		
		
		numberOfSymbols = symbols.size(); 
		numberOfCombinations = (int) (Math.pow(numberOfSymbols, 2)-1);
		
	
		
		for(int i=0; i<numberOfCombinations; i++) {
			populateSymbols(i);
			System.out.println("statement is " + statement.solve());
		}
		
		statement.resolve();
		
	}

}

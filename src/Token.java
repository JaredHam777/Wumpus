import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Token {
	
	
	
	public enum Type	{
		Symbol("[MBPS]_[1-4]_[1-4]"), OpenParentheses("\\("), CloseParentheses("\\)"), 
		Not("not "), If("if "), Iff("iff "), And("and "), Xor("xor "), Or("or ");

		Type(String value){
			this.value = value;
		}
		
		String value;
	}
	
	public Token(Type type, String value) {
		this.type = type;
		this.value = value;
	}

	String value;
	Type type;

	public static ArrayList<Token> getTokensInParantheses(List<Token> tokens){
		int count = 0;
		for(Token t : tokens)	{
			if(t.type == Type.OpenParentheses)	{count--;}
			if(t.type == Type.CloseParentheses) {count++;}
			if(count==0) {return new ArrayList<Token>( tokens.subList(0, tokens.indexOf(t)));}
		}
		return null;
	}
	
	public static ArrayList<Token> parseInput(String line)	{
		ArrayList<Token> tokens = new ArrayList<Token>();
		int startIndex=0;
		for(int i=1; i<line.length()+1; i++) {
			
			for(Type t : Type.values())	{
				Pattern p = Pattern.compile(t.value);
				String stringToMatch = line.substring(startIndex, i);
				Matcher m = p.matcher(stringToMatch);
				if(m.find()) {
				//	System.out.println("match found: " + m.group());
					tokens.add(new Token(t, m.group()));
					startIndex=i;
					break;
				}
			}
		}
		
		
		return tokens;
	}



}

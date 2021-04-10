import java.util.ArrayList;

public class CNFSymbol extends Symbol implements Comparable<CNFSymbol> {

	public CNFSymbol(String name) {
		super(name);
	}
	
	public CNFSymbol(CNFSymbol s) {
		super(s.name);
		this.name = s.name;
		this.isNegated = s.isNegated;
	}
	
	public static boolean IsCompliment(CNFSymbol s1, CNFSymbol s2)	{
		return ((s1.isNegated ^ s2.isNegated) && (s1.name.equals(s2.name)));
	}
	
	
	
	public boolean isNegated = false;
	
	public boolean isEqual(CNFSymbol s1) {
		return ((s1.name.equals(this.name)) && !(CNFSymbol.IsCompliment(s1, this)));
	}
	
	public boolean containedIn(ArrayList<CNFSymbol> list) {
		for(CNFSymbol s : list) {
			if(s.isEqual(this))	{return true;}
		}
		return false;
	}

	@Override
	public int compareTo(CNFSymbol s1) {		
		return this.name.compareTo(s1.name);
	}
	
	/*
	@Override
	public boolean equals(Object o)	{
		CNFSymbol s1 = (CNFSymbol)o;
		return ((s1.name.equals(this.name)) && !(CNFSymbol.IsCompliment(s1, this)));
	}*/
	

}

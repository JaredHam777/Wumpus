import java.util.ArrayList;

public class helper {
	public static boolean fits(int a)	{
		return (a>0 && a<=4);
	}
	
	public static void main(String[] args)	{
		for(int i=1; i<5; i++) {
			
			for(int j=1; j<5; j++) {
				if(i>2 || j>2) {
					System.out.println(helper.getAdj("S", "M", i, j));
				}				
			}
		}
		
		System.out.println();
		
		for(int i=1; i<5; i++) {
			
			for(int j=1; j<5; j++) {
				if(i>2 || j>2) {
					System.out.println(helper.getAdj("B", "P", i, j));
				}				
			}
		}
		System.out.println();
		
		for(int i=1; i<5; i++) {
			
			for(int j=1; j<5; j++) {
				if(i>2 || j>2) {
					System.out.println(helper.getAdj("M", "S", i, j));
				}				
			}
		}
		System.out.println();
		
		for(int i=1; i<5; i++) {
			
			for(int j=1; j<5; j++) {
				if(i>2 || j>2) {
					System.out.println(helper.getAdj("P", "B", i, j));
				}				
			}
		}
	}
	
	public static String getAdj(String symbol, String ifSymbol, int x, int y) {
		ArrayList<String> args = new ArrayList<String>();
		
		int u = y+1;
		int d = y-1;
		int l = x-1;
		int r = x+1;
		
		if(helper.fits(u)) {
			String str = symbol + "_" + x + "_" + u;
			args.add(str);
		}
		if(helper.fits(d)) {
			String str = symbol + "_" + x + "_" + d;
			args.add(str);
		}
		if(helper.fits(l)) {
			String str = symbol + "_" + l + "_" + y;
			args.add(str);
		}
		if(helper.fits(r)) {
			String str = symbol + "_" + r + "_" + y;
			args.add(str);
		}
		
		String ifs = "(if ";
		ifs+=ifSymbol + "_" + x + "_" + y + " ";
		
		
		ifs += "(and";
		for(String str : args)	{
			ifs+=" " + str;
		}
		ifs+="))";
		
		return ifs;
	}
}

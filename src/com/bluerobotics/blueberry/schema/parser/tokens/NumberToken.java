/**
 * 
 */
package com.bluerobotics.blueberry.schema.parser.tokens;

/**
 * 
 */
public class NumberToken extends SingleWordToken {
	private final long m_value;
	private NumberToken(SingleWordToken swt, long v){
		super(swt.getStart(), swt.getEnd(), swt.getName());
		m_value = v;
	}
	private NumberToken(long v) {
		super(null, null, null);
		m_value = v;
	}
	
	public static NumberToken wrap(SingleWordToken swt) {
		
		NumberToken result = null;
		Long v = 0L;
		boolean success = false;
		try {
			v = parse(swt.getName());
			success = true;
		} catch(NumberFormatException e) {
		}
		if(success) {
			result = new NumberToken(swt, v);
		}
		return result;
	}
	public static NumberToken make(long v) {
		NumberToken nt = new NumberToken(v);
		return nt;
		
	}
	private static long parse(String s) {
		long result = 0;
		String s2 = s.toLowerCase();
		int n = s2.length();
		if(s2.startsWith("0x")) {
			result = Long.parseLong(s2, 2, n, 16);
		} else if(s2.startsWith("0d")) {
			result = Long.parseLong(s2, 2, n, 10);
		} else if(s2.startsWith("0b")) {
			result = Long.parseLong(s2, 2, n,  2);
		} else if(s2.length() > 1 && s2.startsWith("0")) {
			result = Long.parseLong(s2, 1, n, 8);
		} else {
			result = Long.parseLong(s2);
		}
		return result;
	}
	public long getNumber() {
		return m_value;
	}
}

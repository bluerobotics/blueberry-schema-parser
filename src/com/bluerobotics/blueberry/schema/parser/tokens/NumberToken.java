/**
 * 
 */
package com.bluerobotics.blueberry.schema.parser.tokens;

import java.math.BigDecimal;

/**
 * 
 */
public class NumberToken extends SingleWordToken {
	private final BigDecimal m_value;
	private NumberToken(SingleWordToken swt, BigDecimal v){
		super(swt.getStart(), swt.getEnd(), swt.getName());
		m_value = v;
	}
	private NumberToken(BigDecimal v) {
		super(null, null, null);
		m_value = v;
	}
	
	public static NumberToken wrap(SingleWordToken swt) {
		
		NumberToken result = null;
		BigDecimal v = null;
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
		NumberToken nt = new NumberToken(new BigDecimal(v));
		return nt;
		
	}
	private static BigDecimal parse(String s) {
		BigDecimal result = null;
		String s2 = s.toLowerCase();
		int n = s2.length();
		if(s2.startsWith("0x")) {
			result = new BigDecimal(Long.parseLong(s2, 2, n, 16));
		} else if(s2.startsWith("0d")) {
			result = new BigDecimal(Long.parseLong(s2, 2, n, 10));
		} else if(s2.startsWith("0b")) {
			result = new BigDecimal(Long.parseLong(s2, 2, n,  2));
		} else if(s2.length() > 1 && s2.startsWith("0")) {
			result = new BigDecimal(Long.parseLong(s2, 1, n, 8));
		} else {
			result = new BigDecimal(s2);
		}
		return result;
	}
	public BigDecimal getNumber() {
		return m_value;
	}
	public float getFloat() {
		return m_value.floatValue();
	}
	public float getInt() {
		return m_value.intValue();
	}
}

/**
 *
 */
package com.bluerobotics.blueberry.schema.parser.tokens;

import com.bluerobotics.blueberry.schema.parser.constants.Number;


/**
 *
 */
public class NumberToken extends SingleWordToken {
	private final Number m_value;
	private NumberToken(SingleWordToken swt, Number v){
		super(swt.getStart(), swt.getEnd(), swt.getName());
		m_value = v;
	}
	private NumberToken(Number v) {
		super(null, null, null);
		m_value = v;
	}

	public static NumberToken wrap(SingleWordToken swt) {

		NumberToken result = null;
		Number v = null;
		boolean success = false;
		try {
			v = Number.parse(swt.getName());
			success = true;
		} catch(NumberFormatException e) {
		}
		if(success) {
			result = new NumberToken(swt, v);
		}
		return result;
	}
	public static NumberToken make(Number v) {
		NumberToken nt = new NumberToken(v);
		return nt;

	}

	public Number getNumber() {
		return m_value;
	}

}

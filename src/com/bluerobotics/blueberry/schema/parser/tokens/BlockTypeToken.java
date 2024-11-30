/**
 * 
 */
package com.bluerobotics.blueberry.schema.parser.tokens;

import java.util.ArrayList;

/**
 * 
 */
public class BlockTypeToken extends SingleWordToken {
	private ArrayList<NameValueToken> m_nameValues = new ArrayList<NameValueToken>();
	public BlockTypeToken(SingleWordToken swt) {
		super(swt.getStart(), swt.getEnd(), swt.getName());
	}
	public void add(NameValueToken nvt) {
		//
		m_nameValues.add(nvt);
	}
}

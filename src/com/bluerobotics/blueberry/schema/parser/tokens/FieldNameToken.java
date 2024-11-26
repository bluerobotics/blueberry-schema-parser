/**
 * 
 */
package com.bluerobotics.blueberry.schema.parser.tokens;

/**
 * 
 */
public class FieldNameToken extends SingleWordToken {
	public FieldNameToken(SingleWordToken swt) {
		super(swt.getStart(), swt.getEnd(), swt.getName());
	}
}

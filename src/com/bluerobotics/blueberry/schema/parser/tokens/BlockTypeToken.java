/**
 * 
 */
package com.bluerobotics.blueberry.schema.parser.tokens;

/**
 * 
 */
public class BlockTypeToken extends SingleWordToken {
	public BlockTypeToken(SingleWordToken swt) {
		super(swt.getStart(), swt.getEnd(), swt.getName());
	}
}

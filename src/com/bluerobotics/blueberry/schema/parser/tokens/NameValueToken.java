/**
 * 
 */
package com.bluerobotics.blueberry.schema.parser.tokens;

/**
 * 
 */
public class NameValueToken extends Token {
	private final NumberToken value;
	private final SingleWordToken name;
	private final CommentToken comment;
	public NameValueToken(SingleWordToken n, NumberToken v, CommentToken c){
		super(n.getStart().getFirst(v != null ? v.getStart() : null).getFirst(c != null ? c.getStart() : null),
				n.getEnd().getLast(v != null ? v.getEnd() : null).getLast(c != null ? c.getEnd() : null));
		
		name = n;
		value = v;
		comment = c;
	}
	public String getName() {
		return name.getName();
	}
	public long getValue() {
		return value.getNumber();
	}
	public String[] getComment() {
		return comment.getComment();
	}
	public String toString() {
		String s = getClass().getSimpleName();
		s += "(";
		s += name.getName();
		s += " = ";
		s += value.getName();
		s += ")";
		return s;
	}
}

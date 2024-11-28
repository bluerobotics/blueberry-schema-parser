/**
 * 
 */
package com.bluerobotics.blueberry.schema.parser.tokens;

/**
 * 
 */
public class NameValueToken extends Token {
	NumberToken value;
	SingleWordToken name;
	CommentToken comment;
	public NameValueToken(SingleWordToken n, NumberToken v, CommentToken c){
		super(n.getStart().getFirst(v.getStart()).getFirst(c != null ? c.getStart() : null),
				n.getEnd().getLast(v.getEnd()).getLast(c != null ? c.getEnd() : null));
		
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

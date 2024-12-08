/**
 * 
 */
package com.bluerobotics.blueberry.schema.parser.tokens;

/**
 * 
 */
public class NameValueToken extends AbstractToken {
	private NumberToken value;
	private final SingleWordToken name;
	private final CommentToken comment;
	public NameValueToken(SingleWordToken n, NumberToken v, CommentToken c){
		super(n.getStart().getFirst(v != null ? v.getStart() : null).getFirst(c != null ? c.getStart() : null),
				n.getEnd().getLast(v != null ? v.getEnd() : null).getLast(c != null ? c.getEnd() : null));
		
		name = n;
		value = v;
		comment = c;
	}
	public NameValueToken(String n, long v) {
		super(null, null);
		name = new SingleWordToken(null, null, n);
		value = NumberToken.make(v);
		comment = null;
	}
	public String getName() {
		return name.getName();
	}
	public NumberToken getNumberToken() {
		return value;
	}
	public long getValue() {
		return value.getNumber();
	}
	public boolean isValue() {
		return value != null;
	}
	public CommentToken getComment() {
		return comment;
	}
	public String toString() {
		String s = getClass().getSimpleName();
		s += "(";
		s += name.getName();
		if(isValue()) {
			s += " = ";
			s += value.getName();
		}
		s += ")";
		return s;
	}
	public void setValue(long v) {
		value = NumberToken.make(v);
	}
}

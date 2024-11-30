/**
 * 
 */
package com.bluerobotics.blueberry.schema.parser.tokens;

/**
 * 
 */
public class SchemaParserException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private final Coord m_location;
	
	public SchemaParserException(String s, Coord c){
		super(addLineAndPointer(s, c));
		m_location = c;
	}
	public Coord getLocaation() {
		return m_location;
	}
	
	private static String addLineAndPointer(String s, Coord c) {
		s += "\n";
		s += c.getString() + "\n";
		s += " ".repeat(c.index);
		s += "^\n";
		return s;
	}

}

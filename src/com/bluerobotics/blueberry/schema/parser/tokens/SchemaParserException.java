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
		super(s);
		m_location = c;
	}
	public Coord getLocaation() {
		return m_location;
	}

}

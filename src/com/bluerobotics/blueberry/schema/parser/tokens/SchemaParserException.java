/**
 * 
 */
package com.bluerobotics.blueberry.schema.parser.tokens;

/**
 * 
 */
public class SchemaParserException extends RuntimeException {

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
		
		if(c != null) {
			s += " in \""+c.filePath+"\" ";
			s +=  " at line "+ (c.line + 1) + "\n";
			s += c.getString() + "\n";
			s += " ".repeat(c.index);
			s += "^\n";
		} else {
			s += "\n";
		}
		
		return s;
	}

}

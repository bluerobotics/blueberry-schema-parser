/*
Copyright (c) 2026  Blue Robotics

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.bluerobotics.blueberry.schema.parser.parsing;

import com.bluerobotics.blueberry.schema.parser.tokens.Coord;

/**
 * 
 */
public class ParserIssue {
	private final Coord[] m_locations;
	private final String m_description;
	public enum Type {
		ERROR,
		WARNING,
		NOTE,
		SKIPPED,
	}
	private final Type m_type;
	
	protected ParserIssue(String description, Type type, Coord... locations) {
		m_locations = locations;
		m_description = description;
		m_type = type;
	}
	public static ParserIssue error(String description, Coord... locations) {
		return new ParserIssue(description, Type.ERROR, locations);
	}
	public static ParserIssue warning(String description, Coord... locations) {
		return new ParserIssue(description, Type.WARNING, locations);
	}
	public static ParserIssue note(String description, Coord... locations) {
		return new ParserIssue(description, Type.NOTE, locations);
	}
	public static ParserIssue skipped(String description, Coord... locations) {
		return new ParserIssue("Unimplemented feature:\n"+description, Type.SKIPPED, locations);
	}
	public Type getType() {
		return m_type;
	}
	@Override
	public String toString() {
		String result =  ""+m_type+": "+m_description;
		boolean firstTime = true;
		for(Coord c : m_locations) {
			result += "\n";
			int loc = c.index;
			String ss = c.getString();
			int j = 0;
			for(int i = 0; i < loc; ++i) {
				if(ss.charAt(i) == '\t') {
					j += 4;
				}
			}
			loc += j;

			ss = ss.replace("\t", "    ");
			result += (!firstTime ? " and" : "") + " in \""+c.filePath+"\" ";
			result +=  " at line "+ (c.line + 1) + "\n";
			result += ss + "\n";
			result += " ".repeat(loc);
			firstTime = false;
		
		}
		result += "^\n";
		return result;
	}
	

}

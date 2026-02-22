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
	private final Coord m_location;
	private final String m_description;
	public enum Type {
		ERROR,
		WARNING,
		NOTE,
	}
	private final Type m_type;
	
	protected ParserIssue(String description, Coord location, Type type) {
		m_location = location;
		m_description = description;
		m_type = type;
	}
	public static ParserIssue error(String description, Coord location) {
		return new ParserIssue(description, location, Type.ERROR);
	}
	public static ParserIssue warning(String description, Coord location) {
		return new ParserIssue(description, location, Type.WARNING);
	}
	public static ParserIssue note(String description, Coord location) {
		return new ParserIssue(description, location, Type.NOTE);
	}
	@Override
	public String toString() {
		String result =  ""+m_type+": "+m_description;
		if(m_location != null) {
			int loc = m_location.index;
			String ss = m_location.getString();
			int j = 0;
			for(int i = 0; i < loc; ++i) {
				if(ss.charAt(i) == '\t') {
					j += 4;
				}
			}
			loc += j;

			ss = ss.replace("\t", "    ");
			result += " in \""+m_location.filePath+"\" ";
			result +=  " at line "+ (m_location.line + 1) + "\n";
			result += ss + "\n";
			result += " ".repeat(loc);
			result += "^\n";
		} else {
			result += "\n";
		}
		return result;
	}

}

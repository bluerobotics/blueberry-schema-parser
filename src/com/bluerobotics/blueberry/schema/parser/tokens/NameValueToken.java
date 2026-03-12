/*
Copyright (c) 2024  Blue Robotics North Inc.

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
package com.bluerobotics.blueberry.schema.parser.tokens;

import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;

/**
 * A token that represents a name assigned (with an equals sign) to a value, which could be of whatever type (e.g. Number, String, etc)
 */
public class NameValueToken<T> extends AbstractToken {
	private T m_value;
	private final SymbolName m_name;
	private final String m_comment;
	public NameValueToken(Coord start, Coord end, SymbolName name, T value, String comment) {
		super(start, end);

		m_name = name;
		m_value = value;
		m_comment = comment;
	
	}

	public T getValue() {
		return m_value;
	}
	public boolean isValue() {
		return m_value != null;
	}
	public SymbolName getSymbolName() {
		return m_name;
	}
	public String getComment() {
		return m_comment;
	}
	public String toString() {
		String s = getClass().getSimpleName();
		s += "(";
		s += m_name;
		if(isValue()) {
			s += " = ";
			s += m_value;
		}
		s += ")";
		return s;
	}
	

}

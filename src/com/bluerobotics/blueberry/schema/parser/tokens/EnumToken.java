/*
Copyright (c) 2024  Blue Robotics

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

import java.util.ArrayList;

import com.bluerobotics.blueberry.schema.parser.tokens.BaseTypeToken.BaseType;

/**
 * 
 */
public class EnumToken extends Token {
	private BaseType m_baseType = null;
	private class NameValue {
		SingleWordToken value;
		SingleWordToken name;
		CommentToken comment;
		NameValue(SingleWordToken n, SingleWordToken v, CommentToken c){
			name = n;
			value = v;
		}
	}
	private final ArrayList<NameValue> m_nameValues = new ArrayList<NameValue>();
	public EnumToken(Coord start, Coord end) {
		super(start, end);
	}
	public BaseType getBaseType() {
		return m_baseType;
	}
	public String toString() {
		String s = " " + ((m_baseType != null) ? m_baseType.name() : "") + " ";
		
		for(NameValue nv : m_nameValues) {
			s += nv.name.getName() + " ";
		}
		return getClass().getSimpleName()+"("+s+")";
	}
	public void setBaseType(BaseType bt) {
		m_baseType = bt;
	}
	public void addNameValue(SingleWordToken name, SingleWordToken value, CommentToken c) {
		
		m_nameValues.add(new NameValue(name, value, c));
		
	}
}

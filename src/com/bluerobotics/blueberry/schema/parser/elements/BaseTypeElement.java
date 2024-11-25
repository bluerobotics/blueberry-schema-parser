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
package com.bluerobotics.blueberry.schema.parser.elements;

/**
 * 
 */
public class BaseTypeElement extends ParserElement {
	public enum BaseType {
		BOOL("bool"),
		INT8("int8"),
		UINT8("uint8"),
		INT16("int16"),
		UINT16("uint16"),
		INT32("int32"),
		UINT32("uint32"),
		FLOAT32("float32"),
		;
		private String m_name;
		private BaseType(String s) {
			m_name = s;
		}
		String getName() {
			return m_name;
		}
		static BaseType lookup(String s) {
			BaseType result = null;
			for(BaseType bt : values()) {
				if(bt.getName().equals(s.trim())) {
					result = bt;
					break;
				}
			}
			return result;
		}
	}
	private final BaseType m_type;
	public BaseTypeElement(Coord start, Coord end, BaseType bt) {
		super(start, end);
		m_type = bt;
		
	}

	public static BaseTypeElement makeNew(Coord start, Coord end, String s) {
		BaseTypeElement result = null;
		BaseType bt = BaseType.lookup(s);
		if(bt != null) {
			result = new BaseTypeElement(start, end, bt);
		}
		return result;
	}
	public String toString() {
		return "BaseTypeElement("+m_type+")";
	}
}

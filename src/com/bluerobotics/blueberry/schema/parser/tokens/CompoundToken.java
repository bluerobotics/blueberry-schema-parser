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

/**
 * 
 */
public class CompoundToken extends SingleWordToken implements TypeToken, DefinedTypeToken {
	private DefineToken m_define = null;
	public CompoundToken(Coord start, Coord end, String s) {
		super(start, end, s);
	}

	@Override
	public DefineToken getDefineToken() {
		return m_define;
	}

	@Override
	public void setDefinedTypeName(DefineToken dt) {
		m_define = dt;
	}
	
	public String toString() {
		String s = getClass().getSimpleName();
		s += "(";
		s += m_define.getTypeName();
		s += ")";
		return s;
	}
}

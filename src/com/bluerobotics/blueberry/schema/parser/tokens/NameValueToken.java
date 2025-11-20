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

import com.bluerobotics.blueberry.schema.parser.constants.Number;
import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;

/**
 *
 */
public class NameValueToken extends GroupToken {
	private NumberToken value;
	private final SymbolNameToken name;
	private final CommentToken comment;
	public NameValueToken(CommentToken c, SymbolNameToken n, IdentifierToken e, NumberToken v){
		
		super(c, n, e, v);
		name = n;
		value = v;
		comment = c;
		
		
	
	}

	public Number getValue() {
		return value.getNumber();
	}
	public boolean isValue() {
		return value != null;
	}
	public SymbolName getSymbolName() {
		return name.getSymbolName();
	}
	public String getComment() {
		return comment == null ? "" : comment.combineLines();
	}
	public String toString() {
		String s = getClass().getSimpleName();
		s += "(";
		s += name;
		if(isValue()) {
			s += " = ";
			s += value;
		}
		s += ")";
		return s;
	}

	@Override
	public boolean isAfter(Token t) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	

}

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

import java.util.ArrayList;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.tokens.BaseTypeToken.BaseType;

/**
 * 
 */
public class EnumToken extends AbstractToken implements TypeToken, DefinedTypeToken {
	private BaseType m_baseType = null;
	private DefineToken m_define = null;
	private CommentToken m_comment = null;

	private final ArrayList<NameValueToken> m_nameValues = new ArrayList<NameValueToken>();
	public EnumToken(Coord start, Coord end) {
		super(start, end);
	}
	public BaseType getBaseType() {
		return m_baseType;
	}
	public String toString() {
		String s = getClass().getSimpleName()+"(";
		s += m_define.getTypeName() + " ";
		s += " " + ((m_baseType != null) ? m_baseType.name() : "") + " ";

		
		for(NameValueToken nv : m_nameValues) {
			s += nv.getName() + " ";
		}
		s += ")";
		return s;
	}
	public void setBaseType(BaseType bt) {
		m_baseType = bt;
	}
	public void addNameValue(NameValueToken nvt) {
		
		m_nameValues.add(nvt);
		
	}
	public List<NameValueToken> getNameValueTokens(){
		return m_nameValues;
	}
	@Override
	public String getName() {
		return "enum "+m_baseType.name();
	}
	@Override
	public DefineToken getDefineToken() {
		return m_define;
	}
	@Override
	public void setDefinedTypeName(DefineToken dt) {
		m_define = dt;
	}
//	@Override
//	public void add(FieldAllocationToken fat) {
//		//this doesn't need to to anything here
//	}
	@Override
	public void setComment(CommentToken t) {
		m_comment = t;
	}
	@Override
	public CommentToken getComment() {
		return m_comment;
	}
//	@Override
//	public List<FieldAllocationToken> getFields() {
//		// TODO Auto-generated method stub
//		return null;
//	}
}

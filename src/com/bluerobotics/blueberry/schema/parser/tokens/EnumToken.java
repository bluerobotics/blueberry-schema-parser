/*
Copyright (c) 2025  Blue Robotics

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
import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants.TokenIdentifier;

/**
 * 
 */
public class EnumToken extends GroupToken {
	private final CommentToken m_comment;
	private final SymbolNameToken m_name;
	private final BaseTypeToken m_type;
	private final BraceToken m_members;
	public EnumToken(CommentToken ct, IdentifierToken et, SymbolNameToken nt, BaseTypeToken tt, BraceToken ms) {
		super(ms.getChildren());
		m_comment = ct;
		m_name = nt;
		m_type = tt;
		m_members = ms;
		
	}
	public SymbolName getSymbolName() {
		return m_name.getSymbolName();
	}
	
	public TokenIdentifier getBaseType() {
		return m_type.getKeyword();
	}
	
	public String getComment() {
		return m_comment == null ? "" : m_comment.combineLines();
	}
//	@Override
//	public int getChildrenCount() {
//		return m_members.getChildrenCount();
//	}
//	@Override
//	public TokenList getChildren() {
//		return m_members.getChildren();
//	}
	
	
	
}

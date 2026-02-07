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

/**
 * 
 */
public class BlockToken extends AbstractToken implements DefinedTypeToken, FieldAllocationOwner {
	private DefineToken m_define = null;
	private ArrayList<FieldAllocationToken> m_nestedFields = new ArrayList<FieldAllocationToken>();
	private CommentToken m_comment = null;

	public BlockToken(Coord start, Coord end) {
		super(start, end);
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
		s += "(" + m_define.getTypeName()+")";
		return s;
	}
	@Override
	public void add(FieldAllocationToken fat) {
		m_nestedFields .add(fat);
	}
	@Override
	public void setComment(CommentToken t) {
		m_comment = t;
	}
	@Override
	public CommentToken getComment() {
		return m_comment;
	}
	@Override
	public List<FieldAllocationToken> getFields() {
		return m_nestedFields;
	}
	@Override
	public String getName() {
		return m_define.getTypeName();
	}
}

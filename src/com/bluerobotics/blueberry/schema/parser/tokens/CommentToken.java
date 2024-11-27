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

/**
 * 
 */
public class CommentToken extends Token {
	private final boolean m_blockNotLine;
	private final String[] m_comment;
	public CommentToken(Coord start, Coord end, String comment, boolean blockNotLine) {
		super(start, end);
		m_blockNotLine = blockNotLine;
		m_comment = comment.split("\\R");
//		System.out.println(this);
	}
	public String toString() {
		return getClass().getSimpleName()+"(\"" + getAbbreviatedComment() + "\")";
	}
	public String[] getComment() {
		return m_comment;
	}
	public String getAbbreviatedComment() {
		String result = "";
		for(String s : m_comment) {
			result += s.substring(0, 20)+"... ";
		}
		return result;
	}
	public CommentToken combine(CommentToken ct) {
		String cs = "";
		boolean firstTime = true;
		for(String s : m_comment) {
			if(!firstTime) {
				cs += "\n";
			}
			
			cs += s;
			firstTime = false;
		}

		for(String s : ct.m_comment) {
			
			cs += "\n";
			
			cs += s;
			firstTime = false;
		}
		
		CommentToken ctn = new CommentToken(getStart(), getEnd(), cs, true);
		return ctn;
	}
	
}

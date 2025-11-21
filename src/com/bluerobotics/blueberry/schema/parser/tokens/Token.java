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

import java.util.function.Consumer;

public interface Token {

	/**
	 * gets the coordinate in the source file of the first character of this token
	 * @return
	 */
	Coord getStart();
	/**
	 * gets the coordinate in the source file of the last character of this token
	 * @return
	 */
	Coord getEnd();
	
	/**
	 * gets the text of the source file that this token represents
	 * @return
	 */
	String getName();
	
	/**
	 * checks if the specified token comes before this token
	 * if they are not from the same source file then it should return false
	 * @param t
	 * @return
	 */
	boolean isAfter(Token t);
	
	
	static boolean inOrder(Token t1, Token t2) {
		boolean result = false;
		Coord e = t1.getEnd();
		Coord s = t2.getStart();
		if(e.isSameFile(s) && e.compareTo(s) < 0) {
			result = true;
		}
			
		return result;
	}
	/**
	 * Applies the specified consumer to this token
	 * 
	 * @param deep - if true will recurse into any group tokens
	 * @param c
	 */
	public void consume(boolean deep, Consumer<Token> c);
}
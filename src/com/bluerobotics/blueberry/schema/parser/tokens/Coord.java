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

public class Coord implements Comparable<Coord> {
	public final int line;
	public final int index;
	private String[] m_lines;
	public Coord(int l, int i, String[] lines){
		line = l;
		index = i;
		m_lines = lines;
	}
	public Coord(Coord c){
		line = c.line;
		index = c.index;
		m_lines = c.m_lines;
		
	}
	/**
	 * advances to the start of the next line
	 * @return the Coord of the next line
	 */
	public Coord nextLine() {
		Coord result = null;
		int i = line + 1;
		if(i < m_lines.length) {
			result = new Coord(i, 0, m_lines);
		}
		return result;
	}
	public Coord updateIndex(int i) {
		return new Coord(line, i, m_lines);
	}
	/**
	 * Increments the specified increment of characters.
	 * @param i
	 * @return
	 */
	public Coord incrementIndex(int i) {
		int j = index + i;
		Coord result = null;
		int n = getString().length();
		if(j > n) {
			j = n;
		
		}

		result = new Coord(line, j, m_lines);
		
		return result;
	}
	/**
	 * Increment the index by the length of the specified String.
	 * This is a convenience method
	 * @param s
	 * @return
	 */
	public Coord incrementIndex(String s) {
		return incrementIndex(s.length());
	}
	public String getString() {
		return m_lines[line];
	}
	public Coord getLine(int i) {
		return new Coord(i, index, m_lines);
	}
	/**
	 * advances to the next non-whitespace character
	 * will not advance to the next line
	 * @param c
	 * @return
	 */
	public Coord trim() {
		Coord result = this;
		boolean notDone = true;
		while(notDone || result == null) {
			if(result.index >= result.getString().length()) {
				notDone = false;
			} else {
				char ch = result.getString().charAt(result.index);
				if(ch == ' ' || ch == '\t') {
					result = result.incrementIndex(1);
				} else {
					notDone = false;
				}
			}
		}
		return result;
	}
	public Coord advanceToWhite() {
		Coord result = this;
		boolean notDone = true;
		while(notDone && result != null) {
			if(result.index >= result.getString().length()) {
				notDone = false;
			} else {
				char ch = result.getString().charAt(result.index);
				if(ch == ' ' || ch == '\t') {

					notDone = false;
				} else {
					result = result.incrementIndex(1);
				}
			}
		}
		return result;
	}
	/**
	 * this should advance from the current location to the next token
	 * This means if we're on a token identifier we should move off it
	 * If we're not on a token identifier we should move to the next one
	 * @param matches
	 * @return
	 */
	public Coord advanceToNext(char... matches) {
		
		Coord result = this;
		if(result.charMatches(matches)){
			result = result.incrementIndex(1);
		} else {
			boolean notDone = true;
			while(notDone && result != null) {
				
				if(result.index >= result.getString().length()) {
					notDone = false;
				} else {
					result = result.incrementIndex(1);
					if(result.isEol()) {
						notDone = false;
						break;
					}
					
					if(result.charMatches(matches)) {
						notDone = false;
						break;
					} else {
						
					}
				}
				
			}
		}
		return result;
	}
	public boolean charMatches(char... matches) {
		char ch = getString().charAt(index);
		boolean matched = false;
		for(char cht : matches) {
			if(cht == ch) {
				matched = true;
				break;
			}
		}
		return matched;
	}
	public boolean startsWith(String s) {
		return getString().substring(index).startsWith(s);
	}
	public Coord indexOf(String s) {
		return updateIndex(getString().indexOf(s, index));
	}
	public boolean contains(String s) {
		return remainingString().indexOf(s) > -1;
		
	}
	public String remainingString() {
		return getString().substring(index);
	}
	public String previousString() {
		return getString().substring(0, index);
	}
	public String fromThisToThatString(Coord that) {
		return getString().substring(index, that.index);
	}
	public boolean isEol() {
		return index == getString().length();
	}
	public Coord newLineIfEol() {
		Coord result = this;
		if(isEol()) {
			result = nextLine();
		}
		return result;
	}
	public Coord gotoEol() {
		return new Coord(this.line, getString().length(), this.m_lines);
	}
	public String toString() {
		Coord end = incrementIndex(5);
		String s = fromThisToThatString(end);
		s += "...";
		s = "Coord(\""+s+"\")";
		return s;
	}
	public int getLineIndex() {
		return line;
	}
	@Override
	public int compareTo(Coord o) {
		int result = 0;
		if(line > o.line) {
			result = 1;
		} else if(line < o.line) {
			result = -1;
		} else {
			if(index > o.index) {
				result = 1;
			} else if(index < o.index) {
				result = -1;
			}
		}
		return result;
	}
	public Coord getFirst(Coord c) {
		Coord result = this;
		if(c != null && result.compareTo(c) > 0) {
			result = c;
		}
		return result;
	}
	public Coord getLast(Coord c) {
		Coord result = this;
		if(c != null && result.compareTo(c) < 0) {
			result = c;
		}
		return result;
	}
	
}
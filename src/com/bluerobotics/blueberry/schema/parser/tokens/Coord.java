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

import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants.TokenIdentifier;

public class Coord implements Comparable<Coord> {
	public final String filePath;
	public final int line;
	public final int index;
	private String[] m_lines;
	public Coord(String path, int l, int i, String[] lines){
		filePath = path;
		line = l;
		index = i;
		m_lines = lines;
	}
	public Coord(Coord c){
		filePath = c.filePath;
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
			result = new Coord(filePath, i, 0, m_lines);
		}
		return result;
	}
	public Coord updateIndex(int i) {
		return new Coord(filePath, line, i, m_lines);
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

		} else if(j < 0) {
			j = 0;
		}

		result = new Coord(filePath, line, j, m_lines);

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
		return new Coord(filePath, i, index, m_lines);
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
			if(result.isAtEnd()) {
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
	/**
	 * retreats to be just after the last non-whitespace character
	 * will not retreat to the previous line
	 * @param c
	 * @return
	 */
	public Coord trimEnd() {
		Coord result = this;
		boolean notDone = true;
		boolean foundSpace = false;
		while(notDone || result == null) {
			if(!result.isAtStart() && result.isAtEnd()) {
				result = result.incrementIndex(-1);
				foundSpace = true;
			}
			if(result.isAtStart() && !foundSpace) {
				notDone = false;
			} else {
				char ch = result.getString().charAt(result.index);
				if(ch == ' ' || ch == '\t') {
					//go back one
					result = result.incrementIndex(-1);
					foundSpace = true;
				} else {
					notDone = false;
					//go ahead if we've processed a space
					if(foundSpace) {
						result = result.incrementIndex(1);
					}
				}
			}
		}
		return result;
	}
	public Coord advanceToWhite() {
		Coord result = this;
		boolean notDone = true;
		while(notDone && result != null) {
			if(result.isAtEnd()) {
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
	public Coord advanceToNext(TokenIdentifier... matches) {

		Coord result = this;
		//move off the current one if we're on one
		if(result.matches(matches)){
			result = result.incrementIndex(1);
		} else {
			boolean notDone = true;
			while(notDone && result != null) {

				if(result.isAtEnd()) {
					notDone = false;
				} else {
					result = result.incrementIndex(1);
					if(result.isEol()) {
						notDone = false;
						break;
					}

					if(result.matches(matches)) {
						notDone = false;
						break;
					} else {

					}
				}

			}
		}
		return result;
	}
	public boolean matches(TokenIdentifier... matches) {
		boolean matched = false;

		for(TokenIdentifier m : matches) {
			if(startsWith(m.id())) {
				matched = true;
			}

		}

		return matched;
	}
	/**
	 * finds the next occurrence of one of the specified strings
	 * ignores the starting position, even if it matches.
	 * If not found then returns end of string
	 * @param ss
	 * @return the location of the next occurrence
	 */
	public Coord findNext(String... ss) {
		Coord result = incrementIndex(1);
		boolean notDone = true;
		while(notDone) {

			if(result.startsWith(ss)) {
				notDone = false;
			} else if(result.isAtEnd()) {

				notDone = false;
			} else {
				result = result.incrementIndex(1);
				if(result.getString().length() <= result.index) {
					notDone = false;
					result = null;
				}
			}
		}
		return result;
	}
	/**
	 * checks if this specified location starts with any of the specified strings
	 * @param ss
	 * @return true if there's a match
	 */
	public boolean startsWith(String... ss) {
		boolean result = false;

		for(String s : ss) {
			result = getString().substring(index).startsWith(s);
			if(result) {
				break;
			}
		}
		return result;

	}
	public boolean isAtEnd() {
		return index >= getString().length();
	}
	public boolean isAtStart() {
		return index <= 0;
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
		return new Coord(this.filePath, this.line, getString().length(), this.m_lines);
	}
	public String toString() {
		Coord end = incrementIndex(15);
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
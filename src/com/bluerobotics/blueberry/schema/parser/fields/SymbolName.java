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
package com.bluerobotics.blueberry.schema.parser.fields;

import java.util.ArrayList;

/**
 * A class that wraps a hierarchical string name that can be easily expressed as various cases
 */
public class SymbolName {
	public static final SymbolName EMPTY = new SymbolName(new String[0]);
	private final String[] name;
	public SymbolName(String... ss) {
		name = ss;
	}
	public static SymbolName guess(String s) {
		SymbolName result;
		if(isUnderscore(s) || isAllUpperCase(s)) {
			result = fromSnake(s);
		} else {
			result = fromCamel(s);
		}
		return result;
	}
	public static SymbolName fromDot(String n) {
		return new SymbolName(breakUpDot(n));
	}
	public static SymbolName fromCamel(String n) {
		return new SymbolName(breakUpCamel(n));
	}
	public static SymbolName fromSnake(String n) {
		return new SymbolName(breakUpSnake(n));
	}
	private static String[] breakUpDot(String s) {
		return s.toLowerCase().split("\\.");
	}
	private static String[] breakUpSnake(String s) {
		return s.toLowerCase().split("_");
	}
	private static String[] breakUpCamel(String s){
		ArrayList<String> result = new ArrayList<String>();
		int j = 0;
		boolean inNumber = false;
		for(int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			boolean end = false;
			if(inNumber && Character.isAlphabetic(c)) {
				inNumber = false;
				end = true;
			} else if(!inNumber && Character.isDigit(c)) {
				inNumber = true;
				end = true;
			} else if(i > 0 && Character.isUpperCase(c)) {
				end = true;
			}
			if(end) {
				result.add(s.substring(j, i).toLowerCase());
				j = i;
			}
		}
		result.add(s.substring(j).toLowerCase());
		return result.toArray(new String[result.size()]);
	}

	private String toSnake(boolean upperNotLower) {
		String result = "";
		boolean firstTime = true;
		for(String w : name) {
			if(!firstTime) {
				result += "_";
			}
			firstTime = false;
			result += w;
		}
		if(upperNotLower) {
			result = result.toUpperCase();
		}
		return result;
	}
	private String toCamel(boolean upperNotLower) {
		String result = "";
		boolean firstTime = true;
		for(String s : name) {
			if((!upperNotLower) && firstTime) {
				result += s.substring(0, 1).toLowerCase();
			} else {
				result += s.substring(0, 1).toUpperCase();
			}
			firstTime = false;
			if(s.length() > 1) {
				result += s.substring(1).toLowerCase();
			} else {

			}
		}
		return result;
	}
	/**
	 * Make a new SymbolName by adding the specified array of Strings to the end of this SymbolName
	 * @param ss
	 * @return
	 */
	public SymbolName append(String... ss) {
		return append(new SymbolName(ss));
	}

	/**
	 * Make a new SymbolName by adding the specified SymbolName to the front of this SymbolName
	 * @param f
	 * @return
	 */
	public SymbolName append(SymbolName f) {
		int m = name.length;
		int n = f.name.length;
		int mm = m + n;
		String[] ss = new String[mm];
		for(int i = 0; i < mm; ++i) {
			if(i < m) {
				ss[i] = name[i];
			} else {
				ss[i] = f.name[i - m];
			}
		}
		return new SymbolName(ss);
	}
	/**
	 * Make a new SymbolName by adding the specified array of Strings to the front of this SymbolName
	 * @param ss
	 * @return
	 */
	public SymbolName prepend(String... ss) {
		return prepend(new SymbolName(ss));
	}
	/**
	 * Make a new SymbolName by adding the specified SymbolName to the front of this SymbolName
	 * @param f
	 * @return
	 */
	public SymbolName prepend(SymbolName f) {
		if(f == null) {
			return this;
		}
		int m = f.name.length;
		int n = name.length;
		int mm = m + n;
		String[] ss = new String[mm];
		for(int i = 0; i < mm; ++i) {
			if(i < m) {
				ss[i] = f.name[i];
			} else {
				ss[i] = name[i - m];
			}
		}
		return new SymbolName(ss);
	}
	public String toString() {
		return toCamel(false);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof SymbolName) {
			SymbolName fn = (SymbolName)obj;
			if(fn.toCamel(true).equals(toCamel(true))) {
				result = true;
			}
		}
		return result;
	}

	public String toLowerCamel() {
		return toCamel(false);
	}

	public String toUpperCamel() {
		return toCamel(true);
	}
	public String toLowerSnake() {
		return toSnake(false);
	}

	public String toUpperSnake() {
		return toSnake(true);
	}
	public String toLowerCase() {
		String result = "";
		for(String w : name) {
			result += w;
		}

		return result;
	}
	public String toDot() {
		String result = "";
		boolean firstTime = true;
		for(String w : name) {
			if(!firstTime) {
				result += ".";
			}
			firstTime = false;
			result += w;
		}

		return result;
	}
	public String toPath() {
		String result = "";
		for(String w : name) {


			result += w;
			result += "/";
		}

		return result;
	}
	/**
	 * Checks that none of the characters of this string are upper case
	 * @param s
	 * @return true if all characters are lower case or not cased characters
	 */
	private static boolean isAllLowerCase(String s) {
		boolean result = true;
		for(int i = 0; i < s.length(); ++i) {
			if(Character.isUpperCase(s.charAt(i))) {
				result = false;
				break;
			}
		}
		return result;
	}
	/**
	 * Checks that none of the characters of this string are lower case
	 * @param s
	 * @return true if all characters are upper case or not cased characters
	 */
	private static boolean isAllUpperCase(String s) {
		boolean result = true;
		for(int i = 0; i < s.length(); ++i) {
			if(Character.isLowerCase(s.charAt(i))) {
				result = false;
				break;
			}
		}
		return result;
	}
	
	private static boolean isUnderscore(String s) {
		return s.contains("_");
		
	}




}

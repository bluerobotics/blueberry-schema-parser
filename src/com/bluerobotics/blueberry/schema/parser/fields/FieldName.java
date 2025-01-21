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
 * 
 */
public class FieldName {
	private final String[] name;
	public FieldName(String... ss) {
		int n = ss.length;
		name = ss;
	}
	public static FieldName fromDot(String n) {
		return new FieldName(breakUpDot(n));
	}
	public static FieldName fromCamel(String n) {
		return new FieldName(breakUpCamel(n));
	}
	public static FieldName fromSnake(String n) {
		return new FieldName(breakUpSnake(n));
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
	public FieldName addSuffix(String... ss) {
		return addSuffix(new FieldName(ss));
	}
	public FieldName addPrefix(String... ss) {
		return addPrefix(new FieldName(ss));
	}
	public FieldName addSuffix(FieldName f) {
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
		return new FieldName(ss);
	}
	public FieldName addPrefix(FieldName f) {
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
		return new FieldName(ss);
	}
	public String toString() {
		return toCamel(false);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof FieldName) {
			FieldName fn = (FieldName)obj;
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

	
	

}

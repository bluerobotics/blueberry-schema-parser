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
package com.bluerobotics.blueberry.schema.parser.writers;

import java.util.ArrayList;

/**
 * some handy formatting and conversion tools
 */
public class WriterUtils {
	public static String formatAsHex(long v) {
		return "0x"+ Long.toHexString(v);
	}
	public static String camelToSnake(String s, boolean upperNotLower) {
		String result = "";
		boolean firstTime = true;
		for(String w : breakUpCamel(s)) {
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
	public static String snakeToCamel(String input, boolean upperNotLower) {
		String result = "";
		String ss[] = breakUpSnake(input);
		boolean firstTime = true;
		for(String s : ss) {
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
	public static void main(String... args) {
		System.out.println(camelToSnake("thisShouldNowBeInLowerSnakeCase001", false));
		System.out.println(camelToSnake("thisShouldNowBe002InUpperSnakeCase", true));
		System.out.println(snakeToCamel("this_should_now_be_in_lower_camel_case", false));
		System.out.println(snakeToCamel("this_should_now_be_in_lower_camel_case", true));

	}
	
}

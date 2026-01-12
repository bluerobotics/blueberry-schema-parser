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

/**
 * 
 */
public interface TokenConstants {
	public static final String COMMENT_BLOCK_START = "/*";
	public static final String COMMENT_BLOCK_END = "*/";
	public static final String COMMENT_BLOCK_MIDDLE = "*";
	public static final String LINE_COMMENT_START = "//";
	public static final String DEFINED_BLOCK_TOKEN = "define";
	public static final String FIELD_BLOCK_START = "{";
	public static final String FIELD_BLOCK_END = "}";
	public static final String COMPOUND_MODIFIER = "compound";
	public static final String ENUM_MODIFIER = "enum";
	public static final String BLOCK_MODIFIER = "block";
	public static final String ARRAY_MODIFIER = "array";
	public static final String COMPACT_ARRAY_MODIFIER = "compact";
	public static final String BRACKET_START = "(";
	public static final String BRACKET_END = ")";
	public static final String EQUALS = "=";
	public static final String KEY_FIELD_NAME = "key";
}

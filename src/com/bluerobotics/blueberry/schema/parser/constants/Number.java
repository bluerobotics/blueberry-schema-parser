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
package com.bluerobotics.blueberry.schema.parser.constants;

import java.math.BigDecimal;

/**
 *
 */
public class Number {
	public static final Number NAN = new Number();
	private final BigDecimal m_value;
	private final boolean m_integer;

	public Number(Long v) {
		m_value = new BigDecimal(v);
		m_integer = true;
	}
	private Number() {
		m_value = null;
		m_integer = false;
	}
	public Number(String s) {
		BigDecimal result = null;
		String s2 = s.toLowerCase();
		int n = s2.length();
		
		if(s2.startsWith("0x")) {
			result = new BigDecimal(Long.parseLong(s2, 2, n, 16));
			m_integer = true;
		} else if(s2.startsWith("0d")) {
			result = new BigDecimal(Long.parseLong(s2, 2, n, 10));
			m_integer = true;
		} else if(s2.startsWith("0b")) {
			result = new BigDecimal(Long.parseLong(s2, 2, n,  2));
			m_integer = true;
		} else if(s2.length() > 1 && s2.startsWith("0")) {
			result = new BigDecimal(Long.parseLong(s2, 1, n, 8));
			m_integer = true;
		} else {
			result = new BigDecimal(s2);
			if(s2.contains(".") || s2.contains("e")) {
				m_integer = false;
			} else {
				m_integer = true;
			}
		}
		m_value = result;
	}
	public static Number parse(String s) {
		return new Number(s);
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof Number) {
			Number n = (Number)obj;
			result = n.m_value.equals(m_value);
		}
		return result;
	}
	public boolean isInteger() {
		return m_integer;
	}

	public double asFloat() {
		double result = Double.NaN;
	
		if(m_value != null) {
			m_value.floatValue();
		}
		return result;
	}
	public int asInt() {
		int result = -1;
		if(m_value != null) {
			result  = m_value.intValue();
		}
		return result;
	}
	public long asLong() {
		long result = -1;
		if(m_value != null) {
			result  = m_value.longValue();
		}
		return result;
	}
	public String toString() {
		String result = "NAN";
		if(m_value != null) {
			result = m_value.toString();
		}
		return result;
	}
	public boolean isNan() {
		return m_value == null;
	}
}

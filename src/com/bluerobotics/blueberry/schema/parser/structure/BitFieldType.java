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
package com.bluerobotics.blueberry.schema.parser.structure;

import java.util.ArrayList;

/**
 * 
 */
public class BitFieldType extends ByteType implements BaseType {
	private final ArrayList<BoolType> m_bools = new ArrayList<BoolType>();
	@Override
	public Size getSize() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void add(BoolType t) {
		if(!isFull()) {
			m_bools.add(t);
		} else {
			throw new RuntimeException("Cannot add more than 8 bits to this byte!");
		}
	}
	public boolean isFull() {
		return m_bools.size() >= 8;
	}

}

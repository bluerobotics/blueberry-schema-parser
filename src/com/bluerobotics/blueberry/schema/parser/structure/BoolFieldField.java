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
import java.util.List;

/**
 * 
 */
public class BoolFieldField extends BaseField {
	private final ArrayList<BoolField> m_bools = new ArrayList<BoolField>();
	public BoolFieldField() {
		super(null, Type.BOOLFIELD, null);
	}
	
	
	public void add(BoolField t) {
		if(!isFull()) {
			m_bools.add(t);
		} else {
			throw new RuntimeException("Cannot add more than 8 bits to this byte!");
		}
	}
	public boolean isFull() {
		return m_bools.size() >= 8;
	}
	@Override
	Type checkType(Type t) throws RuntimeException {
		switch(t) {
		case BLOCK:
		case BOOL:
		case COMPOUND:
		case FLOAT32:
		case INT16:
		case INT32:
		case INT8:
		case UINT16:
		case UINT32:
		case UINT8:
			throw new RuntimeException("Field must only contain base types.");
		case BOOLFIELD:
			break;
		default:
			break;
				
		}
		return t;
	}
	public boolean hasRoom() {
		return m_bools.size() < 8;
	}
	public List<BoolField> getBoolFields(){
		return m_bools;
	}


	@Override
	public boolean equals(Object obj) {
		boolean result =  super.equals(obj);
		if(result) {
			BoolFieldField bff = (BoolFieldField)obj;
			for(int i = 0; i < m_bools.size(); ++i) {
				if(!m_bools.get(i).equals(bff.m_bools.get(i))) {
					result = false;
				}
			}
		}
		return result;
	}
	

}

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
public class CompoundField extends Field {
	private final ArrayList<Field> m_baseTypes = new ArrayList<Field>();


	public CompoundField(String name, String[] comment) {
		super(name, Type.COMPOUND, comment);
	}

	public void add(Field f) {
	
	
		if(f instanceof BoolField) {
			addBool((BoolField)f);
		} else {
			m_baseTypes.add(f);
		}
		if(getBitCount() > 32) {
			throw new RuntimeException("Bitcount cannot be greater than 32!");
		}
	}

	private void addBool(BoolField bf) {
		boolean done = false;
		for(Field f : m_baseTypes) {
			if(f instanceof BitFieldField) {
				BitFieldField bff = (BitFieldField)f;
				if(bff.hasRoom()) {
					bff.add(bf);
					done = true;
					break;
				}
			}
		}
		if(!done) {
			BitFieldField bff = new BitFieldField();
			bff.add(bf);
			add(bff);
			
		}
	}

	@Override
	Type checkType(Type t) throws RuntimeException {
		if(t != Type.COMPOUND) {
			throw new RuntimeException("Must of type Compound!");
		}
		return t;
	}
	
	public int getRoom() {
		return 32 - getBitCount();
	}
	
	

}

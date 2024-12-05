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
public class BlockField extends Field {
	private final ArrayList<Field> m_header = new ArrayList<Field>();
	private final ArrayList<Field> m_baseTypes = new ArrayList<Field>();
	private final ArrayList<BlockField> m_blockTypes = new ArrayList<BlockField>();
	private int m_bitCount = 0;

	protected BlockField(String name, Type type, String[] comment) {
		super(name, type, comment);
		
	}
	public BlockField(String name, String[] comment) {
		super(name, Type.BLOCK, comment);
	}
	
	public void add(Field f) {
		if(f == null) {
			return;
		}

		m_bitCount += f.getBitCount();
		m_baseTypes.add(f);
		
		if(f.getType() == Type.BOOL) {
			addBool(f);
		} else if(f.getBitCount() < 32) {
			addSubWord(f);
		} else if(f instanceof BlockField){
			m_blockTypes.add((BlockField)f);
		} else {
			m_baseTypes.add(f);
		}
	}

	private void addSubWord(Field f) {
		
	}
	private void addBool(Field f) {
		// TODO Auto-generated method stub
		
	}
	@Override
	int getBitCount() {
		return m_bitCount;
	}
	@Override
	Type checkType(Type t) throws RuntimeException {
		switch(t) {

		case BLOCK:
			break;
		default:
			throw new RuntimeException("Field must only contain block types.");
				
		}
		return t;
	}
	

}

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
package com.bluerobotics.blueberry.schema.parser.fields;

import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;

/**
 * This defines an array field
 * Note that in this case, the name of the array type is the field name. The type that this field is an array of is the type name
 */
public class ArrayField extends ParentField {
	private final int[] m_number;

	public ArrayField(SymbolName name, ScopeName typeName,  TypeId typeId, int[] number, String comment, Coord c) {
		super(name, typeName, TypeId.ARRAY, comment, c);
		m_number = number;
	}
	/**
	 * the size of this array, i.e. the number of elements
	 * @return
	 */
	public int[] getNumber() {
		return m_number;
	}


	
	@Override
	public int getBitCount() {

		int n = super.getBitCount();
		
		for(int i : getNumber()) {
			n *= i;
		}
		return n;
	}
	
	@Override
	public void add(Field f) {
		if(size() > 0) {
			throw new RuntimeException("Cannot add more than one child field to an array.");
		}
		super.add(f);
	}
	@Override
	public Field makeInstance(SymbolName name) {
		ArrayField f = new ArrayField(name, getTypeName(), getTypeId(), getNumber(), getComment(), getCoord());
		f.copyChildrenFrom(this);
		return f;
	}
	@Override
	public int getMinAlignment() {
		return getFirstChild().getMinAlignment();
	}
	@Override
	public int getPaddedByteCount() {
		return getFirstChild().getPaddedByteCount();
	}



}

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

import java.util.ArrayList;

import com.bluerobotics.blueberry.schema.parser.tokens.SchemaParserException;

/**
 * A packing algorithm for the Blueberry protocol
 * It will pack elements in order and appropriately byte-aligned but -
 * if a smaller word is packed it will be placed in the first available empty spot
 */
public class BlueberryFieldPacker {
	ArrayList<Boolean> pt = new ArrayList<>();
	public void pack(Field f) {
		if(f instanceof MessageField) {
			pack((MessageField)f);
		} else if(f instanceof StructField) {
			pack((StructField)f);
		} else if(f instanceof SequenceField) {
			pack((SequenceField)f);
		} else if(f instanceof BaseField) {
			pack((BaseField)f);
		} else if(f instanceof ArrayField) {
			pack((ArrayField)f);
		} else if(f instanceof EnumField) {
			pack((EnumField)f);
		} else {
			throw new SchemaParserException("Don't know how to pack a "+f.getClass().getSimpleName(), null);
		}
	}
	public void pack(MessageField f) {
		
	}
	public void pack(StructField f) {
		
	}
	public void pack(SequenceField f) {
		
	}
	public void pack(BaseField f) {
		
	}
	public void pack(ArrayField f) {
		
	}
	public void pack(EnumField f) {
		
	}
	private int findAndAssignSpot(int byteNum) {
		//TODO: check that bytenum is only either 1, 2, 4, 8
		
		int i = 0;
//		while(i < )
		return i;
	}
	
}

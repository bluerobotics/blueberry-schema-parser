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
import java.util.ListIterator;

import com.bluerobotics.blueberry.schema.parser.tokens.SchemaParserException;

/**
 * A packing algorithm for the Blueberry protocol
 * It will pack elements in order and appropriately byte-aligned but -
 * if a smaller word is packed it will be placed in the first available empty spot
 */
public class BlueberryFieldPacker {
	/**
	 * An array of booleans used to keep track of the message packing. Used bytes are true. Unused bytes are false.
	 */
	ArrayList<Boolean> m_bytes = new ArrayList<>();
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
		} else if(f instanceof BoolFieldField) {
			pack((BoolFieldField)f);
		} else if(f instanceof TypeDefField) {
			pack((TypeDefField)f);
		} else {
			throw new SchemaParserException("Don't know how to pack a "+f.getClass().getSimpleName(), null);
		}
	}
	public void pack(MessageField f) {
		//first make sure all bools are contained in bool field fields.
		organizeBools(f);
		//now go through all children and compute the indeces
		f.getChildren().forEach(ft -> pack(ft));
	}

	public void pack(StructField f) {
		f.getChildren().forEach(ft -> pack(ft));
	}
	public void pack(SequenceField f) {
		
	}
	public void pack(BoolFieldField f) {
		f.setIndex(findAndAssignSpot(1));
	}
	public void pack(BaseField f) {
		if(f.getBitCount() == 1) {
			throw new RuntimeException("All bit fields should have been moved out of the message by now and added to a bool field field.");
		} else {
			f.setIndex(findAndAssignSpot(f.getByteCount()));
		}
	}
	private void addBool(BaseField f) {
		//find first bool field field
		
	}
	private void pack(TypeDefField f) {
		f.setIndex(findAndAssignSpot(f.getByteCount()));	
	}
	public void pack(ArrayField f) {
		f.setIndex(findAndAssignSpot(f.getByteCount()));	
	}
	public void pack(EnumField f) {
		f.setIndex(findAndAssignSpot(f.getByteCount()));
	}
	private int findAndAssignSpot(int byteNum) {
		//TODO: check that bytenum is only either 1, 2, 4, 8
		
		int i = 0;
		while(i < m_bytes.size()) {
			if(isEmpty(i, byteNum)) {
				break;
			} else {
				i += byteNum;
			}
		}
		
		//add padding if necessary
		//e.g. if bytes size is 3 and i is 4, then we would need to add one
		while(m_bytes.size() < i) {
			m_bytes.add(false);
		}
		
		//fill in assigned bytes
		for(int j = i; j < byteNum + i; ++j) {
			if(j == m_bytes.size()) {
				m_bytes.add(true);
			} else if(j > m_bytes.size()) {
				throw new RuntimeException("Can put stuff beyond the end of the list.");
			} else {
				m_bytes.set(j, true);
			}
		}
		return i;
	}
	private boolean isEmpty(int i, int byteNum) {
		boolean result = true;
		for(int j = 0; j < byteNum; ++j) {
			if(m_bytes.get(j + i)){
				result = false;
				break;
			}
		}
		return result;
	}

	
	
	/**
	 * recurse through the specified parent and make sure all one bit fields are contained within bool field fields
	 * @param pf
	 */
	private void organizeBools(ParentField pf) {
		ListIterator<Field> li = pf.getChildren().getIterator();
		BoolFieldField bff = null;
		while(li.hasNext()) {
			Field f = li.next();
			if(f instanceof BoolFieldField) {
				bff = (BoolFieldField)f;
				if(bff.getChildren().size() >= 8) {
					bff = null;
				}
				
			} else if(f instanceof BaseField) {
				BaseField bf = (BaseField)f;
				if(bf.getBitCount() == 1) {
					if(bff != null) {
						bff.add(bf);
						li.remove();
					} else {
						bff = new BoolFieldField();
						bff.add(bf);
						li.set(bff);
					}
				}
			} else if(f instanceof ParentField) {
				ParentField pf2 = (ParentField)f;
				organizeBools(pf2);
			}
		}
	}
}

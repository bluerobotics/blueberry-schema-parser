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

import com.bluerobotics.blueberry.schema.parser.parsing.SchemaParserException;

/**
 * A packing algorithm for the Blueberry protocol
 * It will pack elements in order and appropriately byte-aligned but -
 * if a smaller word is packed it will be placed in the first available empty spot
 * Note that packing simply means assigning the index and next index of each field
 */
public class BlueberryFieldPacker {
	/**
	 * An array of booleans used to keep track of the message packing. Used bytes are true. Unused bytes are false.
	 */
	ArrayList<Boolean> m_bytes = new ArrayList<>();
	/**
	 * Recursively computes the index of the specified field and all of its children
	 * @param f
	 */
	private void pack(Field f) {
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
		} else if(f instanceof StringField) {
			pack((StringField)f);
		} else if(f instanceof DefinedTypeField) {
			pack((DefinedTypeField)f);
		} else {
			throw new SchemaParserException("Don't know how to pack a "+f.getClass().getSimpleName(), f.getCoord());
		}
	}

	private void pack(DefinedTypeField f) {		
		pack(f.getFirstChild());
		f.setIndex(f.getFirstChild().getIndex());
	}
	
	public static void pack(MessageField f) {
		BlueberryFieldPacker bfp = new BlueberryFieldPacker();
		//first make sure all bools are contained in boolfield fields.
		bfp.organizeBools(f);
		//now go through all children and compute the indeces
		f.getChildren().forEach(ft -> {
			bfp.pack(ft);
		});
	}

	private void pack(StructField f) {
		f.getChildren().forEach(ft -> {
			pack(ft);
		});
		f.setIndex(f.getFirstChild().getIndex());
	}
	private void pack(SequenceField f) {
		BlueberryFieldPacker bfp = new BlueberryFieldPacker();
		Field c = f.getFirstChild();
		bfp.pack(c);
		
		
		assignIndex(f);
		
	}
	private void pack(StringField f) {
		assignIndex(f);
	}
	private void pack(BoolFieldField f) {
		assignIndex(f);
	}
	private void pack(BaseField f) {
		if(f.getBitCount() == 1) {
			throw new RuntimeException("All bit fields should have been moved out of the message by now and added to a bool field field.");
		} else {
			assignIndex(f);
		}
	}

	private void pack(ArrayField f) {
		BlueberryFieldPacker bfp = new BlueberryFieldPacker();
		Field c = f.getFirstChild();
		bfp.pack(c);
		assignIndex(f);
		
	}
	private void pack(EnumField f) {
		assignIndex(f);
	}
	

	private void assignIndex(Field f) {
		int byteNum = f.getByteCount();
		int alignment = f.getMinAlignment();
		//TODO: check that bytenum is only either 1, 2, 4, 8
		if(alignment <= 0) {
			throw new RuntimeException("Field alginement cannot be zero or less ("+alignment+") "+f);
		}
		int i = 0;
		while(i < m_bytes.size()) {
			if(isEmpty(i, byteNum)) {
				break;
			} else {
				i += alignment;
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
		
		//now set the result as the index to the field in question
		f.setIndex(i);
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
						bff = new BoolFieldField(bf.getCoord());
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

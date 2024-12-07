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
public abstract class ParentField extends Field {
	protected ParentField(String name, Type type, String[] comment) {
		super(name, type, comment);
	}

	public abstract void add(Field f);

	protected void add(ArrayList<Field> fs, Field f) {
		if(f instanceof BoolField) {
			addBool(fs, (BoolField)f);
		} else if(f.getBitCount() < 32) {
			addSubWord(fs, f);
		} else if(f instanceof BlockField){
			fs.add((BlockField)f);
		} else {
			fs.add(f);
		}
	}

	public abstract List<Field> getBaseFields();

	protected void addSubWord(ArrayList<Field> fs, Field f) {
		//first scan for an existing compound word that has room
		CompoundField cf = null;
		for(Field ft : fs) {
			if(ft instanceof CompoundField) {
				CompoundField cft = (CompoundField)ft;
				if(cft.getName() == null && cft.getRoom() >= f.getBitCount()) {
					cf = cft;
					break;
				}
			}
		}
		if(cf == null) {
			//there is no existing cf with room
			cf = new CompoundField(null, null);
			fs.add(cf);
		}
		cf.add(f);
		
	}
	
	protected void addBool(ArrayList<Field> fs, BoolField f) {
		BitFieldField bff = null;
		for(Field ft : fs) {
			if(ft instanceof CompoundField && ft.getName() == null) {
				CompoundField cft = (CompoundField)ft;
			
				for(Field ft2 : cft.getBaseFields()) {
					if(ft2 instanceof BitFieldField) {
						BitFieldField bff2 = (BitFieldField)ft2;
						if(!bff2.isFull()){
							bff = bff2;
							break;
						}
					}
				}
			}
			if(bff != null) {
				break;
			}
		}
		if(bff == null) {
			bff = new BitFieldField();
			add(bff);
		}
		bff.add(f);
	}
	protected int getBitCount(ArrayList<Field> fs) {
		int result = 0;
		for(Field f : fs) {
			result += f.getBitCount();
		}
		return result;
	}
}

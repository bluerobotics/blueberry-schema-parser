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
package com.bluerobotics.blueberry.schema.parser.fields;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 */
public interface ParentField extends Field, DefinedField {

	public  void add(AbstractField f);
	public List<Field> getFields();

	default void add(ArrayList<Field> fs, BaseField f) {
		if(f instanceof BoolField) {
			addBool(fs, (BoolField)f);
		} else {
			fs.add(f);
			f.setParent(this);
		}
	}

	public void scanThroughFields(Consumer<Field> consumer);


	default void addSubWord(ArrayList<BaseField> fs, AbstractField f) {
//		//first scan for an existing compound word that has room
//		CompoundField cf = null;
//		for(AbstractField ft : fs) {
//			if(ft instanceof CompoundField) {
//				CompoundField cft = (CompoundField)ft;
//				if(cft.getName() == null && cft.getRoom() >= f.getBitCount()) {
//					cf = cft;
//					break;
//				}
//			}
//		}
//		if(cf == null) {
//			//there is no existing cf with room
//			cf = new CompoundField(null, null, null);
//			fs.add(cf);
//			cf.setParent(this);
//		}
//		cf.add(f);

	}


	/**
	 * adds a bool field.
	 * this currently does not put it in a bool field field
	 * maybe this is done here - if it's blueberry encoded
	 * @param fs
	 * @param f
	 */
	default void addBool(ArrayList<Field> fs, BoolField f) {
		add(f);


	}
	default int getBitCount(ArrayList<BaseField> fs) {
		int result = 0;
		for(AbstractField f : fs) {
			result += f.getBitCount();
		}
		return result;
	}


}

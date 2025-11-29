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
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;
/**
 *
 */
public class MessageField extends ParentField {
	public static final SymbolName MAX_ORDINAL_FIELD_NAME = SymbolName.fromCamel("maxOrdinal");
	public static final SymbolName LENGTH_FIELD_NAME = SymbolName.fromCamel("length");

	public MessageField(SymbolName name, ScopeName typeName, String comment, Coord c) {
		super(name, typeName, TypeId.MESSAGE, comment, c);
		
		//add default header fields
		BaseField lenF = new BaseField(LENGTH_FIELD_NAME, TypeId.UINT16, "The length of this message", Coord.NULL);
		BaseField fieldNumF = new BaseField(MAX_ORDINAL_FIELD_NAME, TypeId.UINT8, "The highest field ordinal in this message", Coord.NULL);
		FillerByteField fillerF = new FillerByteField();
		add(lenF);
		add(fieldNumF);
		add(fillerF);
	}

	@Override
	public Field makeInstance(SymbolName name) {
		MessageField result = new MessageField(name, getTypeName(), getComment(), getCoord());
		result.copyChildrenFrom(this);
		return result;
	}

	public List<Field> getFlatFields() {
		ArrayList<Field> result = new ArrayList<>();
		scanThroughDeepFields(f -> {
			if(f.isNamed() && f.getIndex() >= 0) {
				result.add(f);
			}
		});
		
		return result;
	}

	@Override
	public int getMinAlignment() {
		return 4;
	}
	@Override
	public int getPaddedByteCount() {
		int result = getByteCount();
		int m = result % getMinAlignment();
		result += m;
		return result;
	}
	/**
	 * looks for a serialization annotation and checks for a value of CDR
	 * @return
	 */
	public boolean useCdrNotBlueberry() {
		boolean result = false;
		Annotation a = getAnnotation(Annotation.SERIALIZATION_ANNOTATION);
		if(a != null) {
			Object s = a.getParameter(0, Object.class);
			if(s.toString().equals("CDR")) {
				result = true;
				
			}
		}
		return result;
	}
	/**
	 * looks for a topic annotation and returns the string value of the parameter
	 * @return
	 */
	public String getTopic() {
		String result = "";
		Annotation a = getAnnotation(Annotation.TOPIC_ANNOTATION);
		if(a != null) {
			result = a.getParameter(0, Object.class).toString();
		}
		return result;
	}

}

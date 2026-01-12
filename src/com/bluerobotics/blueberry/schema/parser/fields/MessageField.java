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

import com.bluerobotics.blueberry.schema.parser.parsing.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;
import com.bluerobotics.blueberry.schema.parser.constants.Number;
/**
 *
 */
public class MessageField extends ParentField {
	public static final SymbolName MODULE_MESSAGE_KEY_FIELD_NAME = SymbolName.fromCamel("moduleMessageKey");
	public static final SymbolName MAX_ORDINAL_FIELD_NAME = SymbolName.fromCamel("maxOrdinal");
	public static final SymbolName LENGTH_FIELD_NAME = SymbolName.fromCamel("length");

	public MessageField(SymbolName name, ScopeName typeName, String comment, Coord c) {
		super(name, typeName, TypeId.MESSAGE, comment, c);
		
		//add default header fields
		BaseField moduleMessageKeyF = new BaseField(MODULE_MESSAGE_KEY_FIELD_NAME, TypeId.UINT32, "The combination of the module unique key and the message unique key.", Coord.NULL);
		BaseField lenF = new BaseField(LENGTH_FIELD_NAME, TypeId.UINT16, "The length of this message", Coord.NULL);
		BaseField fieldNumF = new BaseField(MAX_ORDINAL_FIELD_NAME, TypeId.UINT8, "The highest field ordinal in this message", Coord.NULL);
		FillerByteField fillerF = new FillerByteField();
		add(moduleMessageKeyF);
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
	
	public int getModuleMessageKey() {
		Annotation modka = getAnnotation(Annotation.MODULE_KEY_ANNOTATION);
		Annotation meska = getAnnotation(Annotation.MESSAGE_KEY_ANNOTATION);
		
				
		if(modka == null) {
			throw new SchemaParserException("Message does not include a module key.", getCoord());
		} else if(meska == null) {
			throw new SchemaParserException("Message is not contained in a module that includes a module key.", getCoord());
		}
		Number modkn = modka.getParameter(0, Number.class);
		Number meskn = meska.getParameter(0, Number.class);
		
		if(modkn == null) {
			throw new SchemaParserException("Message module key is not a number.", getCoord());
		} else if(meskn == null) {
			throw new SchemaParserException("Module key of module that contains this message is not a number.", getCoord());
		}
		return modkn.asInt() << 16 | meskn.asInt();
	}
	/**
	 * returns the number of 4-byte words in this message
	 * This does not include any sequence blocks or string blocks
	 * @return
	 */
	public int getPaddedWordCount() {
		return getPaddedByteCount()/4;
	}

}

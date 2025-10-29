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
import java.util.ListIterator;

import com.bluerobotics.blueberry.schema.parser.types.TypeId;
import com.bluerobotics.blueberry.schema.parser.writers.WriterUtils;
import com.bluerobotics.blueberry.schema.parser.constants.Number;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField.NameValue;
/**
 * 
 */
public class EnumField extends AbstractField {
	public class NameValue {
		SymbolName name;
		Number value;
		boolean isValue;
		String comment;
		public NameValue(SymbolName n, Number v, String c) {
			name = n;
			value = v;
			comment = c;
		}
		public SymbolName getName() {
			return name;
		}
		public Number getValue() {
			return value;
		}
		public String getComment() {
			return comment;
		}

		public String toString() {
			return getClass().getSimpleName() + "(" + name.toUpperSnake() + " = " + value + ")";
		}
		public String getValueAsHex() {
			return WriterUtils.formatAsHex(getValue().asLong());
		}
	}

	private final ArrayList<NameValue> m_nameValues = new ArrayList<NameValue>();
	public EnumField(SymbolName name, ScopeName type, TypeId id, String comment) {
		super(name, type, id, comment);
	}
	
	public void addNameValue(SymbolName name, Number value, String comment) {
		m_nameValues.add(new NameValue(name,value, comment));
	}
	


	public List<NameValue> getNameValues(){
		return m_nameValues;
	}
	public String toString() {
		String result = getClass().getSimpleName();
		result += "(";
		result += getTypeName().toUpperCamel();
		result += ")";
		return result;
	}

	@Override
	public Field makeInstance(SymbolName name) {
		EnumField result = new EnumField(name, getTypeName(), getTypeId(), getComment());
		result.m_nameValues.addAll(m_nameValues);
		return result;
	}
	/**
	 * fills in missing enum values
	 * will try zero if it is not used for the first element
	 * will increment the value until it does not exist in the list

	 */
	public void fillInMissingValues() {
		long maxV = Long.MIN_VALUE;
		long i = 0;
		ListIterator<NameValue> nvs = m_nameValues.listIterator();
		while(nvs.hasNext()) {
			NameValue nv = nvs.next();
			if(nv.getValue().isNan()) {
				while(exists(i)){
					++i;
				}
				NameValue nv2 = new NameValue(nv.getName(), new Number(i), nv.getComment());
				nvs.set(nv2);
			} else {
				
				
				
				
			}
		}
	}

	
	private boolean exists(long v) {
		boolean result = false;
		for(NameValue nv : m_nameValues) {
			if(nv.getValue().asLong() == v) {
				result = true;
				break;
			}
		}
		return result;
	}
	private long getMin() {
		long v = Long.MAX_VALUE;
		for(NameValue nv : m_nameValues) {
			long n = nv.getValue().asLong();
			if(n < v) {
				v = n;
			}
		}
		if(v == Long.MAX_VALUE) {
			v = 0;
		}
		return v;
	}
	
	

}

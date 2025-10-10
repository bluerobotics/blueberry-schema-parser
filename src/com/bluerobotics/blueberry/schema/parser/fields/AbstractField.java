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

public abstract class AbstractField implements Field {
	private final FieldName m_name;
	private final Type m_type;
	private final String m_comment;
	private Field m_parent = null;
	private boolean m_inHeader = false;

	protected AbstractField(FieldName name, Type type, String comment) {
		m_name = name;
		m_type = checkType(type);
		m_comment = comment;
	}
	@Override
	public Type getType() {
		return m_type;
	}
	@Override
	public FieldName getName() {
		return m_name;
	}
	@Override
	public String getComment() {
		return m_comment;
	}
	@Override
	public int getBitCount() {
		return m_type.getBitCount();
	}
	/**
	 * checks the type to see if it's compatible with this field
	 * @param t the type to check
	 * @return the same type
	 * @throws RuntimeException if the type is not compatible
	 */
	abstract Type checkType(Type t) throws RuntimeException;

	public String toString() {
		return getClass().getSimpleName()+"("+m_name+")";
	}

	public boolean isInt() {
		boolean result = false;
		switch(getType()) {
		case ARRAY:
			break;
		case BLOCK:
			break;
		case BOOL:
			break;
		case BOOLFIELD:
			break;
		case COMPOUND:
			break;
		case FLOAT32:
			break;
		case INT16:
		case INT32:
		case INT8:
		case UINT16:
		case UINT32:
		case UINT8:
			result = true;
			break;

		}
		return result;
	}
	@Override
	public Field getParent() {
		return m_parent;
	}
	@Override
	public void setParent(Field p) {
		m_parent = p;
	}
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj != null && obj.getClass().equals(getClass())) {
			Field f = (Field)obj;
			if(f.getType() == getType()) {
				if(f.getParent() != null && f.getParent().equals(getParent())) {
					if(f.getName() != null && f.getName().equals(getName())){
						result = true;
					}
				}
			}
		}
		return result;
	}

	public FieldName getCorrectParentName() {
		FieldName result = null;
		Field p = getParent();
		StructField bf = null;
		if(p != null) {
			bf = (StructField)p;
		}


		result = bf.getName();

		return result;
	}


}

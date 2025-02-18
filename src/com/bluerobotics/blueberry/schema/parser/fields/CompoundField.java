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

/**
 * 
 */
public class CompoundField extends BaseField implements ParentField {
	private final ArrayList<BaseField> m_baseFields = new ArrayList<BaseField>();
	private boolean m_dontFill = false;
	private final FieldName m_typeName;


	public CompoundField(FieldName name, FieldName typeName, String comment) {
		super(name, Type.COMPOUND, comment);
		m_typeName = typeName;
	}

	@Override
	public void add(AbstractField f) {
		if(f.getBitCount() >= 32) {
			throw new RuntimeException("You can't add this to a compound word!");
		} else if(f instanceof BoolField) {
			if(!addBool(this, (BoolField)f)) {
				throw new RuntimeException("Could not add field "+f.getName());
			}
		} else {
			m_baseFields.add((BaseField)f);
			f.setParent(this);
			f.setInHeader(isInHeader());
		}
	}

	

	@Override
	Type checkType(Type t) throws RuntimeException {
		if(t != Type.COMPOUND) {
			throw new RuntimeException("Must of type Compound!");
		}
		return t;
	}
	
	public int getRoom() {
		int result = 32 - getBitCount(m_baseFields);
		if(m_dontFill) {
			result = 0;
		}
		
		return result;
	}
	@Override
	public List<BaseField> getBaseFields(){
		return m_baseFields;
	}

	@Override
	public int getBitCount() {
		return 32;
	}

	@Override
	public FieldName getTypeName() {
		return m_typeName;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		boolean result =  super.equals(obj);
		if(result) {
			CompoundField cf = (CompoundField)obj;
			for(int i = 0; i < m_baseFields.size(); ++i) {
				if(!m_baseFields.get(i).equals(cf.m_baseFields.get(i))) {
					result = false;
				}
			}
			if(!cf.getTypeName().equals(getTypeName())) {
				result = false;
			}
		}
		return result;
	}

//	@Override
//	public void setParent(Field p) {
//		super.setParent(p);
//		getBaseFields().forEach(bf -> bf.setParent(p));
//	}
	@Override
	public void setInHeader(boolean b) {
		super.setInHeader(b);
		m_baseFields.forEach(f -> f.setInHeader(b));
	}
	

}

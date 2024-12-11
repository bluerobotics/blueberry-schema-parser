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
public class CompoundField extends BaseField implements ParentField {
	private final ArrayList<BaseField> m_baseTypes = new ArrayList<BaseField>();
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
			if(!addBool((BoolField)f)) {
				throw new RuntimeException("Could not add field "+f.getName());
			}
		} else {
			m_baseTypes.add((BaseField)f);
		}
	}
	protected boolean addBool(BoolField f) {
		boolean result = false;
		BoolFieldField bff = null;
		
		for(AbstractField ft2 : getBaseFields()) {
			if(ft2 instanceof BoolFieldField) {
				BoolFieldField bff2 = (BoolFieldField)ft2;
				if(!bff2.isFull()){
					bff = bff2;
					break;
				}
			}
		}
		if(bff == null && getRoom() >= 8) {
			bff = new BoolFieldField();
			add(bff);
		}
		if(bff != null) {
			bff.add(f);
			result = true;
		}
		return result;
	}
	

	@Override
	Type checkType(Type t) throws RuntimeException {
		if(t != Type.COMPOUND) {
			throw new RuntimeException("Must of type Compound!");
		}
		return t;
	}
	
	public int getRoom() {
		int result = 32 - getBitCount(m_baseTypes);
		if(m_dontFill) {
			result = 0;
		}
		
		return result;
	}
	@Override
	public List<BaseField> getBaseFields(){
		return m_baseTypes;
	}

	@Override
	public int getBitCount() {
		return 32;
	}

	@Override
	public FieldName getTypeName() {
		return m_typeName;
	}
	
	
	
	

}

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

import com.bluerobotics.blueberry.schema.parser.types.TypeId;

/**
 * This defines an array field
 * Note that in this case, the name of the array type is the field name. The type that this field is an array of is the type name
 */
public class ArrayField extends ParentField implements DeferredField {
	private final int m_number;
	private int m_itemByteNum = -1;
	private final ArrayList<ScopeName> m_imports = new ArrayList<>();;

	public ArrayField(SymbolName name, ScopeName typeName,  List<ScopeName> imports, TypeId typeId, int number, String comment) {
		super(name, typeName, TypeId.ARRAY, comment);
		m_number = number;
		m_imports.addAll(imports);
	}
	public int getNumber() {
		return m_number;
	}
	public void setItemByteNum(int i) {
		m_itemByteNum = i;
	}
	public int getItemByteNum() {
		return m_itemByteNum;
	}
	
	
	
	@Override
	public void add(Field f) {
		if(size() > 0) {
			throw new RuntimeException("Cannot add more than one child field to an array.");
		}
		super.add(f);
	}
	@Override
	public Field makeInstance(SymbolName name) {
		return new ArrayField(name, getTypeName(), m_imports, getTypeId(), getNumber(), getComment());
	}


	@Override
	public List<ScopeName> getImports() {
		return m_imports;
	}
	
	@Override
	public void addImport(ScopeName s) {
		if(s == null) {
			return;
		}
		m_imports.add(s);
	}
	

}

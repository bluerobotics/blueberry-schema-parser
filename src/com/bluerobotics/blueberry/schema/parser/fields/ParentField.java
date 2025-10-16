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
import java.util.function.Consumer;

import com.bluerobotics.blueberry.schema.parser.types.BaseType;
import com.bluerobotics.blueberry.schema.parser.types.Type;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;

/**
 * An abstract field that adds the concept of child fields
 */
public abstract class ParentField extends AbstractField {
	private final ArrayList<Field> m_children = new ArrayList<>();
	protected ParentField(FieldName name, FieldName typeName, TypeId typeId, String comment) {
		super(name, typeName, typeId, comment);
	}
	public void add(Field f) {
		m_children.add(f);
	}

	public void scanThroughFields(Consumer<Field> c) {
		for(Field f : m_children) {
			c.accept(f);
		}
	}
	public String toString() {
		String result = getClass().getSimpleName();
		result += "(";
		result += getTypeName().toUpperCamel();
		result += ")";
		return result;
	}
	protected void copyChildrenFrom(ParentField pf) {
		pf.scanThroughFields(f -> {
			add(f.makeInstance(f.getName()));
		});
	}
	public ArrayList<Field> getChildren(){
		return m_children;
	}
}

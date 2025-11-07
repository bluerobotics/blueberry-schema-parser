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

import com.bluerobotics.blueberry.schema.parser.parsing.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.types.BaseType;
import com.bluerobotics.blueberry.schema.parser.types.Type;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;

/**
 * An abstract field that adds the concept of child fields
 */
public abstract class ParentField extends AbstractField {
	private final FieldList m_children = new FieldList();
	protected ParentField(SymbolName name, ScopeName typeName, TypeId typeId, String comment, Coord c) {
		super(name, typeName, typeId, comment, c);
	}
	public void add(Field f) {
		m_children.add(f);
	}

	/**
	 * Applies this specified consumer to all children of only the first level of this parental hierarchy
	 * @param c
	 */
	public void scanThroughFields(Consumer<Field> c) {
		m_children.forEach(c);
	}
	/**
	 * Applies the specified consumer to all children of this parent field, recursively.
	 * @param c
	 */
	public void scanThroughDeepFields(Consumer<Field> c) {
		m_children.forEach(f -> {
			if(f instanceof ParentField) {
				ParentField pf = (ParentField)f;
				pf.scanThroughDeepFields(c);
			}
			c.accept(f);
		});
	}
	public String toString() {
		String result = getClass().getSimpleName();
		result += "(";
		result += getTypeName() == null ? "???" : getTypeName().toString();
		result += ")";
		return result;
	}
	protected void copyChildrenFrom(ParentField pf) {
		pf.scanThroughFields(f -> {
			add(f.makeInstance(f.getName()));
		});
	}
	public FieldList getChildren(){
		return m_children;
	}
	public int size() {
		return m_children.size();
	}
	/**
	 * Gets the first assigned child field
	 * @return
	 * @throws SchemaParserException - if no children have been added yet
	 */
	public Field getFirstChild() throws SchemaParserException {
		checkSize();
		return m_children.getFirst();
	}
	/**
	 * Gets the last assigned child field
	 * @return
	 * @throws SchemaParserException - if no children have been added yet
	 */
	public Field getLastChild() throws SchemaParserException {
		checkSize();
		return m_children.getLast();
	}
	/**
	 * Checks to be sure there have been children-fields added
	 * @throws SchemaParserException - if there are no children
	 */
	protected void checkSize() throws SchemaParserException {
		if(m_children.size() <= 0) {
			throw new SchemaParserException(getClass().getSimpleName() + " has not had a child assigned.", getCoord());
		}
	}
	

	@Override
	public int getIndex() {
		return getFirstChild().getIndex();
	}

	@Override
	public int getNextIndex() {		
		return getLastChild().getNextIndex();
	}
}

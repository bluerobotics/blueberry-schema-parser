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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 
 */
public class FieldList {
	private final ArrayList<Field> m_fields = new ArrayList<>();
	public void add(Field f){
		m_fields.add(f);
	}
	/**
	 * Applies the specified consumer to all members of this list that are of the specified type
	 * @param <T>
	 * @param c
	 * @param deep - if true then will recurse through all parent fields in structure
	 * @param con
	 */
	public <T extends Field> void forEachOfType(Class<T> c, boolean deep, Consumer<T> con) {
		for(Field f : m_fields) {
			if(c.isInstance(f)) {
				T cf = c.cast(f);
				con.accept(cf);
			} else if(f instanceof ParentField && deep) {
				ParentField pf = (ParentField)f;
				pf.getChildren().forEachOfType(c, deep, con);
			}
		}
	}
	/**
	 * Applies the specified consumer to all members of this list that are of the specified type and whose type name is in the specified scope
	 * @param <T>
	 * @param c
	 * @param deep
	 * @param module
	 * @param con
	 */
	public <T extends Field> void forEachOfTypeInScope(Class<T> c, boolean deep, ScopeName module, Consumer<T> con) {
		for(Field f : m_fields) {
			if(c.isInstance(f) && f.getTypeName().removeLastLevel().equals(module)) {
				T cf = c.cast(f);
				con.accept(cf);
			} else if(f instanceof ParentField && deep) {
				ParentField pf = (ParentField)f;
				pf.getChildren().forEachOfTypeInScope(c, deep, module, con);
			}
		}
	}
	
	/**
	 * Applies the specified consumer to all members of this list that are of the specified type and whose type name is in the specified scope
	 * Also propagates a parent name down the recursive call chain by appending the same of the current field
	 * @param <T>
	 * @param c
	 * @param deep
	 * @param module
	 * @param 
	 * @param con
	 */
	public <T extends Field> void forEachOfTypeInScopePlusName(Class<T> c, boolean deep, ScopeName module, SymbolName parent, BiConsumer<SymbolName, T> con) {
		for(Field f : m_fields) {
			if(c.isInstance(f) && f.getTypeName().removeLastLevel().equals(module)) {
				T cf = c.cast(f);
				con.accept(parent, cf);
			} else if(f instanceof ParentField && deep) {
				ParentField pf = (ParentField)f;
				pf.getChildren().forEachOfTypeInScopePlusName(c, deep, module, parent.append(pf.getName()), con);
				
			}
		}
	}
	
	
	/**
	 * shallow scan through each element of list
	 * @param con
	 */
	public void forEach(Consumer<Field> con) {
		for(Field f : m_fields) {
			con.accept(f);
		}
	}
	/**
	 * shallow or deep scan through list elements
	 * @param con - consumer to apply to each element
	 * @param deep - when true, the a full recursive scan is done, down through each parent field
	 */
	public void forEach(Consumer<Field> con, boolean deep) {
		for(Field f : m_fields) {
			con.accept(f);
			if(f instanceof ParentField && deep) {
				ParentField pf = (ParentField)f;
				pf.getChildren().forEach(con, deep);
			}
		}
	}
	public void clear() {
		m_fields.clear();
	}
	public int size() {
		return m_fields.size();
	}
	public Field get(int i) {
		return m_fields.get(i);
	}
	public void set(int i, Field f) {
		m_fields.set(i, f);
	}
	public List<Field> getList(){
		return m_fields;
	}
	public ListIterator<Field> getIterator(){
		return m_fields.listIterator();
	}
	public Field getFirst() {
		return m_fields.getFirst();
	}
	public Field getLast() {
		return m_fields.getLast();
	}
	/**
	 * Looks for field with the specified name. This is not a deep lookkup
	 * @param n - the name to look up
	 * @return
	 */
	public Field getByName(SymbolName n) {
		Field result = null;
		for(Field f : m_fields) {
			if(f.getName() != null && f.getName().equals(n)) {
				result = f;
				break;
			}
		}
		return result;
	}
}

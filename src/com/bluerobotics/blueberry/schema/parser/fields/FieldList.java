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
import java.util.function.Consumer;

/**
 * 
 */
public class FieldList {
	private final ArrayList<Field> m_fields = new ArrayList<>();
	public void add(Field f){
		m_fields.add(f);
	}
	public <T extends Field> void forEachOfType(Class<T> c, boolean deep, Consumer<T> con) {
		for(Field f : m_fields) {
			if(f.getClass() == c) {
				T cf = c.cast(f);
				con.accept(cf);
			} else if(f instanceof ParentField) {
				ParentField pf = (ParentField)f;
				
			}
		}
	}
	public void forEach(Consumer<Field> con) {
		for(Field f : m_fields) {
			con.accept(f);
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
}

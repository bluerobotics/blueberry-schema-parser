/*
Copyright (c) 2024  Blue Robotics North Inc.

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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;

public abstract class AbstractField implements Field {








	private final SymbolName m_name;
	private final ScopeName m_typeName;
	private final String m_comment;
	private String m_fileName = null;
	private int m_index = -1;
	private int m_ordinal = -1;

	private final Coord m_coord;

	private final TypeId m_typeId;
	private ParentField m_parent = null;
	private final ArrayList<Annotation> m_annotations = new ArrayList<>();
	protected AbstractField(SymbolName name, ScopeName type, TypeId id, String comment, Coord c) {
		m_name = name;
		m_typeName = type;
		m_comment = comment;
		m_typeId = id;
		m_coord = c;
	}
	
	
	
	



//	@Override
//	public boolean isSimpleField() {
//		String tp = getType(this);
//
//		return tp != null && f.getTypeId() != TypeId.STRING && (MultipleField.getIndeces(f)).size() == 0 && !f.isFiller();
//	}







	@Override
	public boolean isNotFiller() {
		return getTypeId() != TypeId.FILLER;
	}







	@Override
	public int getOrdinal() {
		return m_ordinal;
	}







	@Override
	public void setOrdinal(int o) {
		m_ordinal = o;
	}







	@Override
	public final int getByteCount() {
		return getBitCount()/8;
	}







	@Override
	public ScopeName getTypeName() {
		return m_typeName;
	}
	@Override
	public TypeId getTypeId() {
		return m_typeId;
	}
	@Override
	public SymbolName getName() {
		return m_name;
	}
	@Override
	public String getComment() {
		return m_comment;
	}
	@Override
	public int getBitCount() {
		return getTypeId().getBitCount();
	}


	public String toString() {
		return getClass().getSimpleName()+"("+m_name+")";
	}


	@Override
	public ParentField getParent() {
		return m_parent;
	}
	@Override
	public void setParent(ParentField p) {
		m_parent = p;
	}
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(this == obj) {
			result = true;
		} else if(obj != null && obj.getClass().equals(getClass())) {
			Field f = (Field)obj;
			ScopeName ftn = f.getTypeName();
			ScopeName tn = getTypeName();
			
			if(f.getTypeName() == getTypeName() || (f.getTypeName() != null && f.getTypeName().equals(getTypeName()))) {
				if(f.getTypeId() == getTypeId() || (f.getTypeId() != null && f.getTypeId().equals(getTypeId()))) {
					if(f.getParent() == getParent() || (f.getParent() != null && f.getParent().equals(getParent()))) {
						if(f.getName() == getName() || (f.getName() != null && f.getName().equals(getName()))){
							result = true;
						}
					}
				}
			}
		}
		return result;
	}
	@Override
	public void addAnnotation(Annotation... as) {
		for(Annotation a : as) {
			int i = m_annotations.indexOf(a);
			if(i == -1) {
				m_annotations.add(a);
			} else {
				m_annotations.set(i, a);
			}
			
		}
	}
	@Override
	public void addAnnotation(List<Annotation> as) {
		for(Annotation a : as) {
			addAnnotation(a);
		}
	}

	@Override
	public Annotation getAnnotation(SymbolName name) {
		Annotation result = null;
		for(Annotation a : m_annotations) {
			if(a.getName().equals(name)){
				result = a;
				break;
			}
		}
		return result;
	}
	@Override
	public void setFileName(String name) {
		m_fileName = name;
	}
	@Override
	public String getFileName() {
		return m_fileName;
	}


	@Override
	public void scanAnnotations(Consumer<Annotation> c) {
		for(Annotation a : m_annotations) {
			c.accept(a);
		}
	}

	@Override
	public int getIndex() {
		return m_index;
	}

	@Override
	public void setIndex(int i) {
		m_index = i;
	}
	@Override
	public int getNextIndex() {
		return m_index + getByteCount();
	}


	@Override
	public Coord getCoord() {
		return m_coord;
	}



	@Override
	public final boolean inScope(ScopeName s) {
		return getTypeName().removeLastLevel().equals(s);
	}







	@Override
	public boolean isNamed() {
		boolean result = false;
		SymbolName name = getName();
		if(name != null && !name.isEmpty()) {
			result = true;
		}
		return result;
	}







	@Override
	public <T extends Field> T asType(Class<T> c) {
		T result = null;
		if(c.isInstance(this)) {
			result = c.cast(this);
		}
		return result;
	}
	
	
	public <T extends ParentField> T getAncestor(Class<T> c) {
		T result = null;
		Field f = this;
		while(f != null && result == null) {
			f = f.getParent();
			if(c.isInstance(f)) {
				result = c.cast(f);
			}
		}
		return result;
	}
	/**
	 * traverses upward through this fields parent hierarchy until a field of the specified class is found
	 * Resulting list is ordered from most distant ancestor in index 0 and this field in spot (n - 1) where n is the size of the list
	 */
	public <T extends ParentField> List<Field> getAncestors(Class<T> c){
		ArrayList<Field> result = new ArrayList<>();
		Field f = this;
		while(f != null) {
			result.add(0, f);
			if(c.isInstance(f)) {
				break;
			}
			f = f.getParent();
		}
		return result;
	}




}

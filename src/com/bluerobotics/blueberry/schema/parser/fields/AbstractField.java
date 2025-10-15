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

import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;

public abstract class AbstractField implements Field {
	private final FieldName m_name;
	private final FieldName m_typeName;
	private final String m_comment;
	private String m_fileName = null;
	private FieldName m_namespace = null;
	private final TypeId m_typeId;
	private ParentField m_parent = null;
	private final ArrayList<Annotation> m_annotations = new ArrayList<>();
	protected AbstractField(FieldName name, FieldName type, TypeId id, String comment) {
		m_name = name;
		m_typeName = type;
		m_comment = comment;
		m_typeId = id;
	}
	@Override
	public FieldName getTypeName() {
		return m_typeName;
	}
	@Override
	public TypeId getTypeId() {
		return m_typeId;
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
		return 0;
	}


	public String toString() {
		return getClass().getSimpleName()+"("+m_name+")";
	}


	@Override
	public Field getParent() {
		return m_parent;
	}
	@Override
	public void setParent(ParentField p) {
		m_parent = p;
	}
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj != null && obj.getClass().equals(getClass())) {
			Field f = (Field)obj;
			if(f.getTypeName().equals(getTypeName()) && f.getTypeId() == getTypeId()) {
				if(f.getParent() != null && f.getParent().equals(getParent())) {
					if(f.getName() != null && f.getName().equals(getName())){
						result = true;
					}
				}
			}
		}
		return result;
	}

	public void addAnnotation(Annotation... as) {
		for(Annotation a : as) {
			m_annotations.add(a);
		}
	}
	public void addAnnotation(List<Annotation> as) {
		for(Annotation a : as) {
			m_annotations.add(a);
		}
	}

	public Annotation getAnnotation(FieldName name) {
		Annotation result = null;
		for(Annotation a : m_annotations) {
			if(a.getName().equals(name)){
				result = a;
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
	public void setNamespace(FieldName name) {
		m_namespace = name;
	}
	@Override
	public FieldName getNamespace() {
		return m_namespace;
	}
	





}

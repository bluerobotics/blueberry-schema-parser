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
import java.util.function.Consumer;

import com.bluerobotics.blueberry.schema.parser.constants.Constant;
import com.bluerobotics.blueberry.schema.parser.parsing.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;

/**
 * 
 */
public class BlueModule implements AnnotationOwner {
	private final ScopeName m_name;
	public static final BlueModule ROOT = new BlueModule(ScopeName.ROOT, null);
	private final ArrayList<Annotation> m_annotations = new ArrayList<>();
	private ArrayList<Constant<?>> m_constants = new ArrayList<>();
	private FieldList m_defines = new FieldList();
	private FieldList m_messages = new FieldList();
	private final Coord m_coord;
	public BlueModule(ScopeName name, Coord c) {
		m_name = name;
		m_coord = c;
	}
	public Coord getCoord() {
		return m_coord;
	}
	
	public ScopeName getName() {
		return m_name;
	}
	@Override
	public void addAnnotation(Annotation... as) {
		for(Annotation a : as) {
			m_annotations.add(a);
		}
	}
	@Override
	public void addAnnotation(List<Annotation> as) {
		for(Annotation a : as) {
			m_annotations.add(a);
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
	public void scanAnnotations(Consumer<Annotation> c) {
		for(Annotation a : m_annotations) {
			c.accept(a);
		}
	}

	public ScopeName scope(SymbolName symbolName) {
		return getName().addLevelBelow(symbolName);
	}
	public BlueModule makeChild(SymbolName symbolName, Coord c) {
		return new BlueModule(m_name.addLevelBelow(symbolName), c);
	}
	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if(o instanceof BlueModule) {
			BlueModule m = (BlueModule)o;
			result = m.m_name.equals(m_name);
		}
		return result;
	}

	public void addConstant(Constant<?> c) {
		int i = m_constants.indexOf(c);
		if(i >= 0) {
			throw new SchemaParserException("Constant "+c.getName()+" already exists!", null);
		}
		m_constants.add(c);
	}
	public List<Constant<?>> getConstants(){
		return m_constants;
	}
	public Constant<?> getConstant(SymbolName n) {
		Constant<?> result = null;
		for(Constant<?> c : m_constants) {
			if(c.getName().equals(n)) {
				result = c;
				break;
			}
		}
		return result;
	}
	
	
	public FieldList getDefines() {
		return m_defines;
	}
	public FieldList getMessages() {
		return m_messages;
	}
	
	public String toString() {
		return getClass().getSimpleName()+"("+m_name.toLowerSnake("\\")+")";
	}

	public boolean isEmpty() {
		boolean result = false;
		result |= m_messages.size() == 0;
//		result |= m_constants.size() == 0;
		return result;
	}
	
	
	
}

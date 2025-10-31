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
 * An interface for a deferred field. That means the type must be looked up later
 */
public class DeferredField extends AbstractField {
	private final ArrayList<ScopeName> m_imports = new ArrayList<>();
	public DeferredField(SymbolName name, ScopeName type, String comment) {
		super(name, type, TypeId.DEFERRED, comment);
		// TODO Auto-generated constructor stub
	}

	public List<ScopeName> getImports(){
		return m_imports;
	}
	public void addImport(ScopeName s) {
		m_imports.add(s);
	}

	@Override
	public Field makeInstance(SymbolName name) {
		return new DeferredField(getName(), getTypeName(), getComment());
	}
	
	
}

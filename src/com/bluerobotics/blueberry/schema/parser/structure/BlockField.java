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
public class BlockField extends Field implements ParentField {
	private final ArrayList<BaseField> m_headerFields = new ArrayList<BaseField>();
	private final ArrayList<BaseField> m_baseFields = new ArrayList<BaseField>();
	private final ArrayList<BlockField> m_blockFields = new ArrayList<BlockField>();

	protected BlockField(String name, Type type, String[] comment) {
		super(name, type, comment);
		
	}
	public BlockField(String name, String[] comment) {
		super(name, Type.BLOCK, comment);
	}
	@Override
	public void add(Field f) {
		if(f == null) {
			return;
		}
		if(f instanceof BlockField) {
			m_blockFields.add((BlockField)f);
		} else if(f instanceof BaseField){
			add(m_baseFields, (BaseField)f);
		} else {
			throw new RuntimeException("Field is not block or base, it's "+f.getType());
		}
		
		
	}
	
	
	

	public void addToHeader(BaseField f) {
		if(f == null) {
			return;
		}
		add(m_headerFields, f);
		
		
		
	}
	public List<BaseField> getHeaderFields(){
		return m_headerFields;
	}
	public List<BlockField> getBlockFields(){
		return m_blockFields;
	}
	@Override
	public List<BaseField> getBaseFields(){
		return m_baseFields;
	}
	@Override
	public int getBitCount() {
		return getBitCount(m_headerFields) + getBitCount(m_baseFields);
	}
	@Override
	Type checkType(Type t) throws RuntimeException {
		switch(t) {

		case BLOCK:
			break;
		default:
			throw new RuntimeException("Field must only contain block types.");
				
		}
		return t;
	}
	
	
	

}

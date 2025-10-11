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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 */
public class StructField extends AbstractField implements ParentField {
	private final ArrayList<Field> m_fields = new ArrayList<>();
	private final FieldName m_typeName;

	protected StructField(FieldName name, FieldName typeName, String comment) {
		super(name, typeName, comment);
		m_typeName = typeName;

	}

	@Override
	public void add(AbstractField f) {
		if(f == null) {
			return;
		}
		m_fields.add(f);
	}
	public List<Field> getFields(){

		return m_fields;

	}


	@Override
	public int getBitCount() {
		//TODO: figure this out!
		return 0;
	}
	@Override
	BaseType checkType(BaseType t) throws RuntimeException {
		switch(t) {

		case BLOCK:
			break;
		default:
			throw new RuntimeException("Field must only contain block types.");

		}
		return t;
	}
	@Override
	public FieldName getTypeName() {
		return m_typeName;
	}
	public void scanThroughFields(Consumer<Field> consumer) {
		for(Field bf : getFields()) {
			if(bf instanceof ParentField) {
				((ParentField)bf).scanThroughFields(consumer);
			} else {
				consumer.accept(bf);
			}
		}


	}


	public static StructField getBlockParent(Field ft) {

		Field f = ft;
		while(!(f instanceof StructField) && f != null) {
			f = f.getParent();
		}
		StructField result = null;
		if(f != null) {
			result = (StructField)f;
		}
		return result;

	}


	public int getBaseWordCount() {
		return 0;
	}



}

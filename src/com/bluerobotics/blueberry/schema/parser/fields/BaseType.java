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

/**
 *
 */
public class BaseType implements Type {
	public enum TypeDef {
		BOOL           (1),
		INT8           (8),
		UINT8          (8),
		BOOLFIELD      (8),
		INT16          (16),
		UINT16         (16),
		INT32          (32),
		UINT32         (32),
		FLOAT32        (32),
		INT64		   (64),
		UINT64         (64),
		FLOAT64         (64),
		;
	
	
	
	
	
	
	
	
	
		private int bitNum;
		private FieldName name;
		TypeDef(int bn){
			bitNum = bn;
			name = FieldName.fromSnake(name());
		}
		public int getBitCount() {
			return bitNum;
		}
		public boolean isBaseType() {
			return bitNum <= 32;
		}
		public FieldName getName() {
			return name;
		}
	}
	private final TypeDef m_type;
	
	public BaseType(TypeDef t) {
		m_type = t;
	}
	@Override
	public FieldName getTypeName() {
		return m_type.getName();
	}
	



}

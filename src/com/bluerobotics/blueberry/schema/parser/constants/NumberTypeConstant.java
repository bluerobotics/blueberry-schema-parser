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
package com.bluerobotics.blueberry.schema.parser.constants;

import java.math.BigDecimal;

import com.bluerobotics.blueberry.schema.parser.fields.FieldName;
import com.bluerobotics.blueberry.schema.parser.types.BaseType;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;


/**
 *
 */
public class NumberTypeConstant extends AbstractConstant<BigDecimal> {
	private final BigDecimal m_value;
	public NumberTypeConstant(TypeId id, FieldName name, BigDecimal value, String comment) {
		super(BaseType.getBaseType(id), name, comment);
		m_value = value;
	}
	public long getLong() {
		return m_value.longValue();
	}
	public int getInt() {
		return m_value.intValue();
	}
	public double getDouble() {
		return m_value.doubleValue();
	}
	@Override
	public BigDecimal getValue() {
		return m_value;
	}




}

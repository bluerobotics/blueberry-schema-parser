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
 * This represents a field whose contents are determined by a defined type.
 * Defined types are defined with one of the following commands: typedef, struct, sequence, message
 * As usual the name is the name of the field and the type name is the name of the defined type (that must be looked up to figure out what it is)
 * When the defined type is looked up it can be added to this field as a child.
 * This sort of field can be added to a struct, a message, or used in an array
 * Note that if this turns out to be referencing an array type then probably this field will get replaced later by an array field
 */
public class DefinedTypeField extends ParentField {
	public DefinedTypeField(SymbolName name, ScopeName type, String comment) {
		super(name, type, TypeId.DEFERRED, comment);
		
	}

	@Override
	public Field makeInstance(SymbolName name) {
		return new DefinedTypeField(name, getTypeName(), getComment());
	}
	


}

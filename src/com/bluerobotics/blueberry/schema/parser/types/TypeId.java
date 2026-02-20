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
package com.bluerobotics.blueberry.schema.parser.types;

import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;

/**
 *
 */
public enum TypeId {
	BOOL           (1),
	CHAR           (8),
	INT8           (8),
	UINT8          (8),
	BOOLFIELD      (8),
	FILLER         (8),//this indicates a 1 byte field with no assigned purpose other than being a placeholder
	INT16          (16),
	UINT16         (16),
	INT32          (32),
	UINT32         (32),
	FLOAT32        (32),
	INT64		   (64),
	UINT64         (64),
	FLOAT64        (64),
	STRING         (32),
	SEQUENCE       (32),
	STRUCT         (0),
	MESSAGE        (0), 
	ARRAY          (0),
	DEFERRED       (0), //this means the type is not currently known but should be looked up later 
	DEFINED        (0),
	;









	private int bitNum;
	private SymbolName name;
	TypeId(int bn){
		bitNum = bn;
		name = SymbolName.fromSnake(name());
	}
	public int getBitCount() {
		return bitNum;
	}
	public boolean isBaseType() {
		return bitNum <= 32;
	}

	public SymbolName getTypeName() {
		return name;
	}
}

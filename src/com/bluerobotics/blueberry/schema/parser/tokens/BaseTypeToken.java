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
package com.bluerobotics.blueberry.schema.parser.tokens;

import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants.TokenIdentifier;

/**
 * A token that represents a base type
 */
public class BaseTypeToken extends IdentifierToken implements TypeToken {

	private static final TokenIdentifier[] BASE_TYPES = {
		TokenIdentifier.BOOLEAN,
		TokenIdentifier.BYTE,
		TokenIdentifier.DOUBLE,
		TokenIdentifier.FLOAT,
		TokenIdentifier.INT,
		TokenIdentifier.INT16,
		TokenIdentifier.INT32,
		TokenIdentifier.INT64,
		TokenIdentifier.INT8,
		TokenIdentifier.UINT16,
		TokenIdentifier.UINT32,
		TokenIdentifier.UINT64,
		TokenIdentifier.UINT8,
		TokenIdentifier.LONG,
		TokenIdentifier.SHORT,
		TokenIdentifier.UNSIGNED,
	};
	private BaseTypeToken(IdentifierToken it) {
		super(it.getStart(), it.getEnd(), it.getKeyword());
	}

	public static BaseTypeToken makeNew(IdentifierToken it) {
		BaseTypeToken result = null;
		for(TokenIdentifier ti : BASE_TYPES) {
			if(it.getKeyword() == ti) {
				result = new BaseTypeToken(it);
				break;
			}
		}

		return result;
	}




	@Override
	public String getName() {
		return getKeyword().id();
	}
}

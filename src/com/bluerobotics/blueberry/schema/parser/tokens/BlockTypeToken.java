/*
Copyright (c) 2024  Blue Robotics North Inc.

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

import java.util.ArrayList;
import java.util.List;

/**
 * This token indicates a type that must have been defined. We haven't checked yet. 
 * It is a single word token that has not yet been identified as something else and it starts a line 
 */
public class BlockTypeToken extends SingleWordToken implements TypeToken {
	private ArrayList<NameValueToken> m_nameValues = new ArrayList<NameValueToken>();
	public BlockTypeToken(SingleWordToken swt) {
		super(swt.getStart(), swt.getEnd(), swt.getName());
	}
	public void add(NameValueToken nvt) {
		//
		m_nameValues.add(nvt);
	}
	public NameValueToken getValue(String name) {
		NameValueToken result = null;
		for(NameValueToken nvt : m_nameValues) {
			if(nvt.getName().equals(name)) {
				result = nvt;
				break;
			}
		}
		return result;
	}
	public List<NameValueToken> getNameValues(){
		return m_nameValues;
	}
	
	
	
}

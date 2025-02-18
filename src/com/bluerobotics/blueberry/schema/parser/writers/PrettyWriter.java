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
package com.bluerobotics.blueberry.schema.parser.writers;

import java.io.File;

import com.bluerobotics.blueberry.schema.parser.fields.BlockField;

/**
 * A SourceWriter to generate a pretty version of the schema
 * This will include all enum values and key values assigned
 * As well as consistent formatting for all blocks
 */
public class PrettyWriter extends SourceWriter {

	public PrettyWriter(File dir) {
		super(dir);
	}

	@Override
	public void write(BlockField bf, String... headers) {
		startFile(headers);
		
		writeDefinedTypes(bf);
		
		writeBlockField(bf);
		
		
		writeToFile(bf.getName().addSuffix("pretty").toLowerCamel(),".txt");
	}
	/**
	 * scan through field hierarchy finding new defined types
	 * @param bf
	 */
	private void writeBlockField(BlockField bf) {
		// TODO Auto-generated method stub
		
	}

	private void writeDefinedTypes(BlockField bf) {
		
	}

}

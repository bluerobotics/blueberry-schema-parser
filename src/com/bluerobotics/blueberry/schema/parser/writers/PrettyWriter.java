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
import java.util.ArrayList;

import com.bluerobotics.blueberry.schema.parser.fields.BaseField;
import com.bluerobotics.blueberry.schema.parser.fields.StructField;
import com.bluerobotics.blueberry.schema.parser.fields.FieldName;
import com.bluerobotics.blueberry.schema.parser.fields.FixedIntField;

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
	public void write(StructField bf, String... headers) {
		startFile(headers);

		writeDefinedTypes(bf);

		writeBlockField(bf);


		writeToFile(bf.getName().addSuffix("pretty").toLowerCamel(),".txt");
	}
	/**
	 * write block field and children
	 * @param bf
	 */
	private void writeBlockField(StructField bf) {
//		addBlockComment(bf.getComment());
//		FieldName tn = bf.getTypeName();
//		FieldName bn = bf.getName();
//		String assigns = "";
//		boolean started = false;
//		for(BaseField f : bf.getHeaderFields()) {
//			if(f instanceof FixedIntField) {
//				FixedIntField fif = (FixedIntField)f;
//				if(!started) {
//					assigns = "(";
//					started = true;
//				}
//				assigns += " " +fif.getName().toLowerCamel() + " = " + WriterUtils.formatAsHex(fif.getValue());
//			}
//		}
//		if(started) {
//			assigns += " )";
//		}
//
//
//		addLine(tn.toUpperCamel() + " " + bn.toLowerCamel() + assigns + " {");
//		indent();
//		for(BaseField f : bf.getAllBaseFields()) {
//			writeBaseField(f);
//		}
//		for(StructField bf2 : bf.getAllBlockFields()) {
//			writeBlockField(bf2);
//		}
//		closeBrace();
//
//

	}

	private void writeBaseField(BaseField f) {
		// TODO Auto-generated method stub

	}

	private void writeDefinedTypes(StructField bf) {
		// TODO Auto-generated method stub

	}

}

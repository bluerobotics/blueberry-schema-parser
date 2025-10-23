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
import java.io.StringWriter;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.constants.Constant;
import com.bluerobotics.blueberry.schema.parser.fields.AbstractField;
import com.bluerobotics.blueberry.schema.parser.fields.BaseField;
import com.bluerobotics.blueberry.schema.parser.fields.FieldList;
import com.bluerobotics.blueberry.schema.parser.fields.StructField;
import com.bluerobotics.blueberry.schema.parser.parsing.BlueberrySchemaParser;

/**
 * A class to test writing something useful from the computed schema packet format
 */
public class TestWriter extends SourceWriter {
	public TestWriter(File dir, BlueberrySchemaParser parser, String header) {
		super(dir, parser, header);
	}
	StringWriter m_writer = new StringWriter();
	public String getOutput() {
		return m_writer.toString();
	}
	@Override
	public void write() {
//		write(0, bf);
	}
	private void start(AbstractField f) {
		w("(");

		if(f instanceof BaseField) {
			BaseField bf = (BaseField)f;
//			w(""+bf.getIndex()+" ");
		}
//		if(f instanceof EnumType) {
//			w("enum ");
//		}
//		w(f.getType().name());
		if(f.getName() != null) {
			w(" ");
			w(f.getName().toUpperCamel());
		}
//		w(" ");
	}
	private void end() {
		w(")");
	}
	private void write(int i, StructField f) {

//		start(f);
//		++i;
//		eol(i);
//		write(i, f.getHeaderFields());
//		if(f.getBaseFields().size() > 0) {
//			eol(i);
//		}
//		write(i, f.getBaseFields());
//		for(StructField bf2 : f.getBlockFields()) {
//			eol(i);
//			write(i, bf2);
//		}
//
//		end();
//		eol(--i);

	}
	private void write(int i, AbstractField f) {
//		if(f instanceof CompoundField) {
//			write(i, (CompoundField)f);
////		} else if(f instanceof EnumField) {
////			write(i, (EnumField)f);
//		} else if(f instanceof FixedIntField) {
//			write(i, (FixedIntField)f);
//		} else if(f instanceof BoolFieldField) {
//			write(i, (BoolFieldField)f);
//		} else if(f instanceof BaseField) {
//			write(i, (BaseField)f);
//		} else if(f instanceof StructField) {
//			write(i, (StructField)f);
//		}
	}

	private void write(int i, List<BaseField> fs) {
		boolean firstTime = true;
		for(AbstractField f : fs) {
			if(!firstTime) {
				eol(i);
			}
			firstTime = false;
//			write(i, f);
		}
	}
//	private void write(int i, FixedIntField f) {
//		start(f);
//		w(" = " + WriterUtils.formatAsHex(f.getValue()));
//		end();
//	}
	private void write(int i, BaseField f) {
		start(f);
		end();
//		eol(i);
	}
//	private void write(int i, BoolFieldField f) {
//		start(f);
//		++i;
//		for(BoolField bf : f.getBoolFields()) {
//			eol(i);
//			start(bf);
//			end();
//		}
//
//		end();
//	}

	private void w(String s) {
		m_writer.write(s);
	}
	private void eol(int i) {
		w("\n");
		w("    ".repeat(i));
	}

}

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.structure.BaseField;
import com.bluerobotics.blueberry.schema.parser.structure.BlockField;
import com.bluerobotics.blueberry.schema.parser.structure.FieldName;
import com.bluerobotics.blueberry.schema.parser.structure.FieldUtils;

public class CWriter extends SourceWriter {
	private static final int INDENT_SPACE_NUM = 4;
	private final FieldUtils m_fu = new FieldUtils(); 

	public CWriter(File dir) {
		super(dir);
	}

	@Override
	public void write(BlockField bf, String... headers) {
		
		try {
			test(bf, headers);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void test(BlockField top, String... h) throws IOException {
		List<BlockField> bfs = m_fu.getAllBlockFields(top);
		BufferedWriter w = makeFileWriter("test", "h");
		int i = 0;
		addBlockComment(w, i, h);
		for(BlockField bf : bfs) {
			for(BaseField f : bf.getAllBaseFields()) {
				if(f.getName() != null) {
					writeDefine(w, i, f.getName().addPrefix(top.getName()).addSuffix("field"), ""+f.getIndex());
				}
			}
		}
		w.close();
	}
	
	
	private void newLine(BufferedWriter w, int indent) throws IOException {
		w.append("\n");
		indent(w, indent);
	}
	private void indent(BufferedWriter w, int indent) throws IOException {
		w.append(" ".repeat(indent*INDENT_SPACE_NUM));

	}
	private void writeDefine(BufferedWriter w, int i, FieldName name, String value) throws IOException {
		indent(w, i);
		w.append("#define ");
		w.append(name.toSnake(true));
		w.append(" (" + value + ")");
		newLine(w, i);
		
	}
	private void addBlockComment(BufferedWriter w, int indent, String... cs) throws IOException {
		for(String c : cs) {
			String[] ss = c.split("\\R");
			int n = ss.length - 1;
			
			for(int i = 0; i <= n; ++i) {
				if(i == 0) {
					indent(w, indent);
					w.append("/*");
					
				}
				newLine(w, indent);
				w.append(" * "+ss[i]);
				if(i == n) {
					newLine(w, indent);
					w.append(" */");
					newLine(w, indent);
				}
			}
		}
	}
	

}

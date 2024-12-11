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
	private final FieldUtils m_fu = new FieldUtils(); 

	public CWriter(File dir) {
		super(dir);
	}

	@Override
	public void write(BlockField bf, String... headers) {
		
		
			test(bf, headers);
		
		
	}
	private void test(BlockField top, String... h) {
//		List<BlockField> bfs = top.getBlockFields();
		clear();
		int i = 0;
		addBlockComment(i, h);
		writeDefines(i, top);
		writeToFile("test","h");
		
	}
	
	private void writeDefines(int i, BlockField top) {
		List<BlockField> bfs = top.getBlockFields();
		

		for(BlockField bf : bfs) {
		
			writeDefines(i, (BlockField)bf);
		}
		for(BaseField f : top.getAllBaseFields()) {
			if(f.getName() != null) {
				boolean multiLine = f.getComment().split("\\R").length > 1 ;
				String c = "";
				if(multiLine) {
					addBlockComment(i, f.getComment());
				} else {
					String s = f.getComment();
					if(!s.isBlank()) {
						c = "    //"+f.getComment();
					}
				}
				
				writeDefine(i, f.getName().addPrefix(top.getName()).addSuffix("field"), ""+f.getIndex(),c);
			}
				
			
		}
		
	}
	
	

	private void writeDefine(int i, FieldName name, String value, String comment) {
		indent(i);
		append("#define ");
		append(name.toSnake(true));
		append(" (" + value + ")");
		if(!comment.isEmpty()) {
			append(comment);
		}
		newLine(i);
		
	}
//	private void writeEnums(Bu)
	private void addBlockComment(int indent, String... cs) {
		for(String c : cs) {
			String[] ss = c.split("\\R");
			int n = ss.length - 1;
			
			for(int i = 0; i <= n; ++i) {
				if(i == 0) {
					indent(indent);
					append("/*");
					
				}
				newLine(indent);
				append(" * "+ss[i]);
				if(i == n) {
					newLine(indent);
					append(" */");
					newLine(indent);
				}
			}
		}
	}
	

}

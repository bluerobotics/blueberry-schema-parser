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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.bluerobotics.blueberry.schema.parser.structure.BaseField;
import com.bluerobotics.blueberry.schema.parser.structure.BlockField;
import com.bluerobotics.blueberry.schema.parser.structure.EnumField;
import com.bluerobotics.blueberry.schema.parser.structure.EnumField.NameValue;
import com.bluerobotics.blueberry.schema.parser.structure.Field;
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
		addBlockComment(h);
		writeDefines(top);
		writeEnums(top);
		
		
		
		writeToFile("test","h");
		
	}
	
	private void writeEnums(BlockField top) {
		//first make a list of all unique enums
		ArrayList<EnumField> es = new ArrayList<EnumField>();
		scanThroughBaseFields(top, f -> {
			if(f instanceof EnumField) {
				EnumField e = (EnumField)f;
				boolean found = false;
				//check that this new one isn't the same as an existing one
				for(EnumField ef : es) {
					if(ef.getTypeName().equals(e)) {
						found = true;
						break;
					}
				}
				if(!found) {
					es.add(e);
				}
			}
		});	
		for(EnumField ef : es) {
			addDocComment(ef.getComment());
			append("typedef enum {");
			
			indent();
			for(NameValue nv : ef.getNameValues()) {
				appendNewLine();
				append(nv.getName().toSnake(true));
				append(" = " + nv.getValue());
				String c = nv.getComment();
				if(c != null && !c.isBlank()) {
					append("// "+c);
				}
				
				
			}
			outdent();
			appendNewLine();
			append("} "+ef.getTypeName().toCamel(true));
			appendNewLine();
			appendNewLine();

		}
	}

	private void writeDefines(BlockField top) {
		scanThroughBaseFields(top, f -> {
			if(f.getName() != null) {
				boolean multiLine = f.getComment().split("\\R").length > 1 ;
				String c = "";
				if(multiLine) {
					addBlockComment(false, f.getComment());
				} else {
					String s = f.getComment();
					if(!s.isBlank()) {
						c = "    //"+f.getComment();
					}
				}
				
				writeDefine(f.getName().addPrefix(top.getName()).addSuffix("field"), ""+f.getIndex(),c);
			}
		});	
	}

	
	

	private void writeDefine(FieldName name, String value, String comment) {
		appendIndent();
		append("#define ");
		append(name.toSnake(true));
		append(" (" + value + ")");
		if(!comment.isEmpty()) {
			append(comment);
		}
		appendNewLine();
		
	}
	private void addDocComment(String... cs) {
		addBlockComment(true, cs);
	}
	private void addBlockComment(String... cs) {
		addBlockComment(false, cs);
	}
	/**
	 * Adds some block comments
	 * @param docNotBlock
	 * @param cs
	 */
	private void addBlockComment(boolean docNotBlock, String... cs) {
		if(cs.length > 0) {
			for(String c : cs) {
				String[] ss = c.split("\\R");
				int n = ss.length - 1;
				String startToken = docNotBlock ? "/**" : "/*";
				if(!c.isBlank()){
					for(int i = 0; i <= n; ++i) {
						if(i == 0) {
							appendIndent();
							append(startToken);
							
						}
						appendNewLine();
						append(" * "+ss[i]);
						if(i == n) {
							appendNewLine();
							append(" */");
							appendNewLine();
						}
					}
				}
			}
		}
	}
	

}

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.bluerobotics.blueberry.schema.parser.structure.BaseField;
import com.bluerobotics.blueberry.schema.parser.structure.BlockField;
import com.bluerobotics.blueberry.schema.parser.structure.AbstractField;
import com.bluerobotics.blueberry.schema.parser.structure.ParentField;

/**
 * 
 */
public abstract class SourceWriter {
	protected final File m_directory;
	private static final int INDENT_SPACE_NUM = 4;
	

	protected StringBuffer m_buffer = new StringBuffer();
	protected int m_indent = 0;
	public SourceWriter(File dir) {
		if(!dir.isDirectory()) {
			throw new RuntimeException("Specified file location is not a directory!");
		}
		m_directory = dir;
	}
	public abstract void write(BlockField bf, String... headers);
	
	protected void indent() {
		++m_indent;
	}
	protected void outdent() {
		--m_indent;
	}
	protected void writeToFile(String name, String extension) {
		File f = new File(m_directory, name+"."+extension);
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new FileWriter(f));
			w.append(getBuffer());
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}
	protected void add(String s) {
		m_buffer.append(s);
	}
	protected void clear() {
		m_buffer = new StringBuffer();
	}
	protected void addNewLine(){
		add("\n");
		addIndent();
	}
	protected void addIndent(){
		add(" ".repeat(m_indent*INDENT_SPACE_NUM));

	}
	protected StringBuffer getBuffer() {
		return m_buffer;
	}
	protected void addLine(String s) {
		addIndent();
		add(s);
		addNewLine();
	}
	protected void addLineComment(String c) {
		addLine("//"+c);
	}
	
	protected void addDocComment(String cs) {
		addBlockComment(true, cs);
	}
	protected void addBlockComment(String cs) {
		addBlockComment(false, cs);
	}
	/**
	 * Adds some block comments
	 * @param docNotBlock
	 * @param c
	 */
	private void addBlockComment(boolean docNotBlock, String c) {
		
			
		String[] ss = c.split("\\R");
		int n = ss.length - 1;
		String startToken = docNotBlock ? "/**" : "/*";
		if(!c.isBlank()){
			for(int i = 0; i <= n; ++i) {
				if(i == 0) {
					addIndent();
					add(startToken);
					
				}
				addNewLine();
				add(" * "+ss[i]);
				if(i == n) {
					addNewLine();
					add(" */");
					addNewLine();
				}
			}
		}
	}
	
	protected void addSectionDivider(String title) {
		String[] ss = title.split("\\R");
		addNewLine();
		addLineComment("*************************************************************************************");
		if(!title.isBlank()) {
			for(String s : ss) {
				addLineComment(s);
			}
			addLineComment("*************************************************************************************");
		}
		addNewLine();
	}

}

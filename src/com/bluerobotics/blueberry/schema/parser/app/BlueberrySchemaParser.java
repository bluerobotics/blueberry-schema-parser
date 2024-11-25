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
package com.bluerobotics.blueberry.schema.parser.app;

import java.util.ArrayList;

import com.bluerobotics.blueberry.schema.parser.elements.BlockEndElement;
import com.bluerobotics.blueberry.schema.parser.elements.BlockStartElement;
import com.bluerobotics.blueberry.schema.parser.elements.BraceEndElement;
import com.bluerobotics.blueberry.schema.parser.elements.BraceStartElement;
import com.bluerobotics.blueberry.schema.parser.elements.CommentElement;
import com.bluerobotics.blueberry.schema.parser.elements.Coord;
import com.bluerobotics.blueberry.schema.parser.elements.EolElement;
import com.bluerobotics.blueberry.schema.parser.elements.EqualsElement;
import com.bluerobotics.blueberry.schema.parser.elements.TokenElement;
import com.bluerobotics.blueberry.schema.parser.elements.ParserElement;
import com.starfishmedical.utils.ResourceTools;

/**
 * 
 */
public class BlueberrySchemaParser implements Constants {
	private static final String COMMENT_BLOCK_START = "/*";
	private static final String COMMENT_BLOCK_END = "*/";
	private static final String COMMENT_BLOCK_MIDDLE = "*";
	private static final String LINE_COMMENT_START = "//";
	private static final String DEFINED_BLOCK_TOKEN = "define";
	private static final String FIELD_BLOCK_START = "{";
	private static final String FIELD_BLOCK_END = "}";
	private static final String COMPOUND_MODIFIER = "compound";
	private static final String BRACE_START = "(";
	private static final String BRACE_END = ")";
	private static final String EQUALS = "=";
	private String m_token = "";


	private final ArrayList<ParserElement> m_elements = new ArrayList<ParserElement>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("BlueberrySchemaParser args: "+ args);
		String sf = ResourceTools.loadText(RESOURCE_PATH + "testSchema.txt");
		
		BlueberrySchemaParser p = new BlueberrySchemaParser(sf);
		
		
		
		
	}
	public BlueberrySchemaParser(String schema) {
		//split into lines
		
		Coord c = new Coord(0,0, schema.split("\\R"));
		do {
			c = c.trim();
			
			c = processBlockComment(c);
			c = processLineComment(c);
			c = processNextToken(c);
			c = processEol(c);
			
			
		}
		while(c != null);
		
		
	}

	
	private Coord processEol(Coord c) {
		Coord result = c;
		if(result.remainingString().isBlank()) {
			m_elements.add(new EolElement(result));
			result = result.nextLine();
		}
		return result;
	}
	private Coord processNextToken(Coord c) {
		if(c.isEol()) {
			return c;
		}
		Coord start = c.trim();
		//now advance to the next whitespace
		Coord result = start.advanceToNext(' ','\t','{','}','(',')','=');
		Coord end = result;
		String s = start.fromThisToThatString(end);
	
		if(!s.isEmpty()) {
			addToken(start, end, s);
		}
		return result;
	}
	private void addToken(Coord start, Coord end, String s) {
		switch(s) {
		case FIELD_BLOCK_START:
			m_elements.add(new BlockStartElement(start));
			break;
		case FIELD_BLOCK_END:
			m_elements.add(new BlockEndElement(start));
			break;
		case BRACE_START:
			m_elements.add(new BraceStartElement(start));
			break;
		case BRACE_END:
			m_elements.add(new BraceEndElement(start));
			break;
		case EQUALS:
			m_elements.add(new EqualsElement(start));
			break;
			
		default:
			m_elements.add(new TokenElement(start, end, s));
			break;
		}
	
		
	}
	private Coord moveToNextToken(Coord c) {
		Coord result = c.advanceToNext(' ','\t','{','}','(',')','=');
		
		return result;
	}
	private Coord processLineComment(Coord c) {
		if(c == null) {
			return null;
		}
		Coord result = c.trim();
		if(result.startsWith(LINE_COMMENT_START)) {
			Coord start = result;
			Coord end = result.nextLine();
			result = result.incrementIndex(LINE_COMMENT_START);
			String comment = result.remainingString();
			result = result.gotoEol();
			m_elements.add(new CommentElement(start, end, comment, false));
		}
		
		return result;
	}
	
	/**
	 * Checks if the next element of the file is a block comment
	 * @return the next Coord after a comment if there is one. null if incomplete comment, c if no comment
	 */
	private Coord processBlockComment(Coord c){
		if(c == null) {
			return null;
		}
		Coord result = c;
		String line = "";
	
		//first check for comment block
		if(result.startsWith(COMMENT_BLOCK_START)) {
			
			result = result.indexOf(COMMENT_BLOCK_START);
			result = result.incrementIndex(COMMENT_BLOCK_START);
			String comment = "";
			
			Coord start = result;
			
			boolean keepGoing = true;
			boolean firstTime = true;
			while(keepGoing) {
				result = result.trim();//remove whitespace if any
				
				if(result.contains(COMMENT_BLOCK_END)) {
					Coord end = result.indexOf(COMMENT_BLOCK_END);
					String ns = result.fromThisToThatString(end);
					if(!ns.isBlank()) {
						comment += ns;
					}
					m_elements.add(new CommentElement(start, end, comment, true));
					keepGoing = false;
					result = end.incrementIndex(COMMENT_BLOCK_END).newLineIfEol();
				} else {
					if(result.startsWith(COMMENT_BLOCK_MIDDLE)) {
						result = result.incrementIndex(COMMENT_BLOCK_MIDDLE);
					}
					result = result.trim();
					if(!firstTime) {
						comment += "\n";
					} 
					String s = result.remainingString();
					if(!s.isBlank()) {
						comment += s;
						firstTime = false;
					} else {
						
					}
					result = result.nextLine();
					if(result == null) {
						keepGoing = false; 
						result = null;
						throw new RuntimeException("Block comment missing end!");
					}
					
				}
				
			}
			
		}
		return result;
	}
	

	
	
	

}

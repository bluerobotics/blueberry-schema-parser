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

import com.bluerobotics.blueberry.schema.parser.tokens.BaseTypeToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BlockEndToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BlockStartToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BlockToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BracketEndToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BracketStartToken;
import com.bluerobotics.blueberry.schema.parser.tokens.CommentToken;
import com.bluerobotics.blueberry.schema.parser.tokens.CompoundToken;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.tokens.DefineToken;
import com.bluerobotics.blueberry.schema.parser.tokens.EnumToken;
import com.bluerobotics.blueberry.schema.parser.tokens.EolToken;
import com.bluerobotics.blueberry.schema.parser.tokens.EqualsToken;
import com.bluerobotics.blueberry.schema.parser.tokens.FieldNameToken;
import com.bluerobotics.blueberry.schema.parser.tokens.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.tokens.SingleWordToken;
import com.bluerobotics.blueberry.schema.parser.tokens.Token;
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
	private static final String ENUM_MODIFIER = "enum";
	private static final String BLOCK_MODIFIER = "block";
	private static final String BRACKET_START = "(";
	private static final String BRACKET_END = ")";
	private static final String EQUALS = "=";

	
	private String m_token = "";


	private final ArrayList<Token> m_elements = new ArrayList<Token>();
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
		
		try {
			while(c != null) {
				c = c.trim();
				
				c = processBlockComment(c);
				c = processLineComment(c);
				c = processNextToken(c);
				c = processEol(c);
				
				
			}
			
			
			collapseDefines();
			collapseEnums();
//			identifyFieldNames();
//			identifyBlockTypes();
		} catch (SchemaParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("************* Output ************");
		for(Token pe : m_elements) {
			System.out.println(pe.toString());
		}
	}
	/**
	 * now any single word token at the end of a line is a fieldname or before a block start
	 * @throws SchemaParserException 
	 */
	private void identifyFieldNames() throws SchemaParserException {
		boolean notDone = true;
		int i = 0;
		int n = m_elements.size();
		for(i = 0; i < m_elements.size() - 1; ++i){
			
			Token ti = m_elements.get(i);
			Token tj = null;
			//find last element of this line
			int j;
			for(j = i; j < n - 1; ++j) {
				Token t = m_elements.get(j+1);
				if(t.getStart().getLineIndex() > ti.getStart().getLineIndex()) {
					break;
				} else {
					tj = t;
				}
			}
			//tj should be the last element of this line
			//backtrack if a block start
			if(tj instanceof BlockStartToken) {
				--j;
				tj = m_elements.get(j);
			}
			if(tj instanceof SingleWordToken) {
				SingleWordToken swt = (SingleWordToken)tj;
				FieldNameToken fnt = new FieldNameToken(swt);
				m_elements.set(i, fnt);
			}
		
			
			//first find next define element
			
			
		
			
			n = m_elements.size();
		}
		
	}
	/**
	 * any single word token at the start of a line or after a 
	 */
	private void identifyBlockTypes() {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Collapse all the define tokens and the following single word tokens together
	 * @throws SchemaParserException 
	 */
	private void collapseDefines() throws SchemaParserException {
		boolean notDone = true;
		int i = 0;
		
		while(i < m_elements.size()){
			//first find next define element
			DefineToken dt = null;
			while(dt == null) {
				Token t = m_elements.get(i);
				if(t instanceof DefineToken) {
					dt = (DefineToken)t;
				} else {
					++i;
				
					if(i >= m_elements.size()) {
						break;
					}
				}
			}
			
			if(dt != null) {
				//now get next token
				++i;
				Token t = i < m_elements.size() ? m_elements.get(i) : null;
				if(t instanceof SingleWordToken) {
					SingleWordToken swt = (SingleWordToken)t;
					dt.setTypeName(swt.getName());
					m_elements.remove(i);
				} else {
					throw new SchemaParserException("Define keyword must be followed by a type name!", t.getStart());
				}
			}
			
		
			
			
			notDone = false;
		}
		
	}
	/**
	 * Collapse all the enum tokens and the following base type together
	 * @throws SchemaParserException 
	 */
	private void collapseEnums() throws SchemaParserException {
		boolean notDone = true;
		int i = 0;
		
		while(i < m_elements.size()){
			//first find next define element
			EnumToken dt = null;
			while(dt == null) {
				Token t = m_elements.get(i);
				if(t instanceof EnumToken) {
					dt = (EnumToken)t;
				} else {
					++i;
				
					if(i >= m_elements.size()) {
						break;
					}
				}
			}
			
			if(dt != null) {
				//now get next token
				++i;
				Token t = i < m_elements.size() ? m_elements.get(i) : null;
				if(t instanceof BaseTypeToken) {
					BaseTypeToken swt = (BaseTypeToken)t;
					dt.setBaseType(swt.getBaseType());
					m_elements.remove(i);
				} else {
					throw new SchemaParserException("Enum keyword must be followed by a base type name!", t.getStart());
				}
			}
			
		
			
			
			notDone = false;
		}
		
	}
	private Coord processEol(Coord c) {
		Coord result = c;
		if(result.remainingString().isBlank()) {
			m_elements.add(new EolToken(result));
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
			m_elements.add(new BlockStartToken(start));
			break;
		case FIELD_BLOCK_END:
			m_elements.add(new BlockEndToken(start));
			break;
		case BRACKET_START:
			m_elements.add(new BracketStartToken(start));
			break;
		case BRACKET_END:
			m_elements.add(new BracketEndToken(start));
			break;
		case EQUALS:
			m_elements.add(new EqualsToken(start));
			break;
		case COMPOUND_MODIFIER:
			m_elements.add(new CompoundToken(start, end, s));
			break;
		case ENUM_MODIFIER:
			m_elements.add(new EnumToken(start, end));
			break;
		case BLOCK_MODIFIER:
			m_elements.add(new BlockToken(start, end));
			break;
		case DEFINED_BLOCK_TOKEN:
			m_elements.add(new DefineToken(start, end));
			break;
		default:
			BaseTypeToken bte = BaseTypeToken.makeNew(start, end, s);
			if(bte != null) {
				m_elements.add(bte);
			} else {
				m_elements.add(new SingleWordToken(start, end, s));
			}
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
			//find the index of the first element of the line that this comment occurred on.
			//place this commment before that element
			
			m_elements.add(getFirstIndexBeforeLine(start.line), new CommentToken(start, end, comment, false));
		}
		
		return result;
	}
	
	private int getFirstIndexBeforeLine(int line) {
		int i = m_elements.size();
		if(i > 0) {
			--i;
			boolean done = false;
			while(!done) {
				int lt = m_elements.get(i).getStart().line;
				if(lt < line) {
					++i;
					done = true;
				} else {
					if(i <= 0) {
						break;
					} else {
						--i;
					}
				}
			}
			
		}
		return i;
	}
	/**
	 * Checks if the next element of the file is a block comment
	 * @return the next Coord after a comment if there is one. null if incomplete comment, c if no comment
	 * @throws SchemaParserException 
	 */
	private Coord processBlockComment(Coord c) throws SchemaParserException{
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
					m_elements.add(new CommentToken(start, end, comment, true));
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
						throw new SchemaParserException("Block comment missing end!", c);
					}
					
				}
				
			}
			
		}
		return result;
	}
	

	
	
	

}

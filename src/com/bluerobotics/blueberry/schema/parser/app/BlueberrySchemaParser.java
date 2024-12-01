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

import javax.swing.plaf.synth.SynthCheckBoxMenuItemUI;

import com.bluerobotics.blueberry.schema.parser.tokens.BaseTypeToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BraceEndToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BraceStartToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BlockToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BlockTypeToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BracketEndToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BracketStartToken;
import com.bluerobotics.blueberry.schema.parser.tokens.CommentToken;
import com.bluerobotics.blueberry.schema.parser.tokens.CompoundToken;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.tokens.DefineToken;
import com.bluerobotics.blueberry.schema.parser.tokens.DefinedTypeToken;
import com.bluerobotics.blueberry.schema.parser.tokens.EnumToken;
import com.bluerobotics.blueberry.schema.parser.tokens.EolToken;
import com.bluerobotics.blueberry.schema.parser.tokens.EqualsToken;
import com.bluerobotics.blueberry.schema.parser.tokens.FieldAllocationToken;
import com.bluerobotics.blueberry.schema.parser.tokens.FieldNameToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NameValueToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NestedFieldAllocationToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NumberToken;
import com.bluerobotics.blueberry.schema.parser.tokens.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.tokens.SingleWordToken;
import com.bluerobotics.blueberry.schema.parser.tokens.Token;
import com.bluerobotics.blueberry.schema.parser.tokens.AbstractToken;
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


	private final ArrayList<AbstractToken> m_tokens = new ArrayList<AbstractToken>();
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
			collapseComments();
			collapseDefines();
			collapseNumbers();
			collapseNameValues();
			collapseEnums();
			collapseEnumValues();
			identifyFieldNames();
			identifyBlockTypeInstances();
			collapseBlockTokenNameValues();
			collapseBaseTypeAllocations();
			collapseNestedFieldAllocations();
			collapseDefinedTypes();			
			collapseEols();
			collapseDefinedTypeFields(0);
			
			
			
			
		} catch (SchemaParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("************* Output ************");
		for(Token pe : m_tokens) {
			System.out.println(pe.toString());
		}
	}
	/**
	 * scan a range of tokens to resolve field allocaations in defined types
	 * this will recurse and close all braces from deepest level outwards
	 * @param start - the first index to scan
	 * @param end - the end of the list of elements to scan, not including this element
	 * @return the index of an end token
	 * @throws SchemaParserException 
	 */
	private int collapseDefinedTypeFields(int start) throws SchemaParserException {
		int result = -1;
		int i = start;
		while(i < m_tokens.size()){
			//find the next brace
			i = findToken(i, true, BraceStartToken.class, BraceEndToken.class);
			
			if(i > 0 && i < m_tokens.size() - 1) {
				Token ti = m_tokens.get(i);
				if(ti instanceof BraceStartToken) {
					int j = collapseDefinedTypeFields(i + 1);
					if(j < 0) {
						throw new SchemaParserException("Could not find closing brace!", ti.getStart());
					}
					//if the previous token is a DefinedTypeToken
					Token prevT = m_tokens.get(i - 1);
					if(prevT instanceof DefinedTypeToken) {
						DefinedTypeToken dtt = (DefinedTypeToken)prevT;
						//so now i to j should be the braces to remove
						for(int k = j - 1; k > i; --k) {
							Token tk = m_tokens.get(k);
							if(tk instanceof FieldAllocationToken) {
								dtt.add((FieldAllocationToken)tk);
								m_tokens.remove(k);
							}
						}
						i += 2;//point past the block end
					} else {
						i = j + 1;
					}
					
				} else if(ti instanceof BraceEndToken) {
					//This will unroll this level of recursion
					result = i;
					break;
					
				}
			} else {
				break;
			}
		}
		return result;
			
	}
	private void collapseDefinedTypes() throws SchemaParserException {
		int i = 0;
		while(i < m_tokens.size()){
			//first find next define element
			i = findToken(i, true, DefineToken.class);
			
			if(i > 0 && i < m_tokens.size() - 1) {//note the limits here!
				DefineToken dt = (DefineToken)m_tokens.get(i);
				Token nextT = m_tokens.get(i + 1);
				if(nextT instanceof DefinedTypeToken) {
					DefinedTypeToken dtt = (DefinedTypeToken)nextT;
					dtt.setDefinedTypeName(dt);
					m_tokens.remove(i);
				} else {
					throw new SchemaParserException("Expected either block, enum or compound here!", nextT.getStart());
					
				}
			} else {
				break;
			}
		}
	}
	private void collapseNestedFieldAllocations() throws SchemaParserException {
		int i = 0;
		while(i < m_tokens.size()){
			//first find next define element
			i = findToken(i, true, BlockTypeToken.class);
			
			if(i > 0 && i < m_tokens.size() - 1) {//note the limits here!

				BlockTypeToken et = (BlockTypeToken)m_tokens.get(i);
				CommentToken ct = null;
				FieldNameToken fnt = null;
				if(i > 0) {
					Token tPrev = m_tokens.get(i - 1);
					if(tPrev instanceof CommentToken) {
						ct = (CommentToken)tPrev;
					}
				}
				Token tNext = m_tokens.get(i + 1);
				if(tNext instanceof FieldNameToken) {
					fnt = (FieldNameToken)tNext;
					FieldAllocationToken fat = new NestedFieldAllocationToken(fnt, et, ct);
					if(ct != null) {
						m_tokens.set(i - 1, fat);
						m_tokens.remove(i + 1);
						m_tokens.remove(i);
					} else {
						m_tokens.set(i,  fat);
						m_tokens.remove(i + 1);
					}
				} else {
					throw new SchemaParserException("Expected field name token here.", tNext.getStart());
				}
			} else {
				break;
			}
		}
	}
	private void collapseEnumAllocations() throws SchemaParserException {
		int i = 0;
		while(i < m_tokens.size()){
			//first find next define element
			i = findToken(i, true, EnumToken.class);
			
			if(i > 0 && i < m_tokens.size() - 1) {//note the limits here!

				EnumToken et = (EnumToken)m_tokens.get(i);
				CommentToken ct = null;
				FieldNameToken fnt = null;
				if(i > 0) {
					Token tPrev = m_tokens.get(i - 1);
					if(tPrev instanceof CommentToken) {
						ct = (CommentToken)tPrev;
					}
				}
				Token tNext = m_tokens.get(i + 1);
				if(tNext instanceof FieldNameToken) {
					fnt = (FieldNameToken)tNext;
					FieldAllocationToken fat = new FieldAllocationToken(fnt, et, ct);
					if(ct != null) {
						m_tokens.set(i - 1, fat);
						m_tokens.remove(i + 1);
						m_tokens.remove(i);
					} else {
						m_tokens.set(i,  fat);
						m_tokens.remove(i + 1);
					}
				} else {
					throw new SchemaParserException("Expected field name token here.", tNext.getStart());
				}
			} else {
				break;
			}
		}
	}
	
	private void collapseBaseTypeAllocations() throws SchemaParserException {
		int i = 0;
		while(i < m_tokens.size()){
			//first find next define element
			i = findToken(i, true, BaseTypeToken.class);
			
			if(i > 0 && i < m_tokens.size() - 1) {//note the limits here!

				BaseTypeToken swt = (BaseTypeToken)m_tokens.get(i);
				CommentToken ct = null;
				FieldNameToken fnt = null;
				if(i > 0) {
					Token tPrev = m_tokens.get(i - 1);
					if(tPrev instanceof CommentToken) {
						ct = (CommentToken)tPrev;
					}
				}
				Token tNext = m_tokens.get(i + 1);
				if(tNext instanceof FieldNameToken) {
					fnt = (FieldNameToken)tNext;
					FieldAllocationToken fat = new FieldAllocationToken(fnt, swt, ct);
					if(ct != null) {
						m_tokens.set(i - 1, fat);
						m_tokens.remove(i + 1);
						m_tokens.remove(i);
					} else {
						m_tokens.set(i,  fat);
						m_tokens.remove(i + 1);
					}
				} else {
					throw new SchemaParserException("Expected field name token here.", tNext.getStart());
				}
			} else {
				break;
			}
		}
	}
	/**
	 * grab any name values from within brackets that occur on a block type allocation
	 * @throws SchemaParserException 
	 */
	private void collapseBlockTokenNameValues() throws SchemaParserException {
		int i = 0;
		while(i < m_tokens.size()){
			//first find next define element
			i = findToken(i, true, BlockTypeToken.class);
			
			
			if(i > 0 && i < m_tokens.size()) {//note the limits here!
				BlockTypeToken btt = (BlockTypeToken)m_tokens.get(i);
				int bs = findToken(i, true, BracketStartToken.class);
				int be = findToken(i, true, BracketEndToken.class);
				BracketStartToken bst = null;
				BracketEndToken bet = null;
				
				
				if(bs >= 0) {
					bst = (BracketStartToken)m_tokens.get(bs);
					//throw this out if it's not on the same line
					if(bst.getStart().getLineIndex() != btt.getStart().getLineIndex()) {
						bst = null;
					}
				}
				if(be >= 0) {
					bet = (BracketEndToken)m_tokens.get(be);
				}
				
				if(bst != null) {
					if(bet == null) {
						throw new SchemaParserException("No closing brackets for opening brackets.", bst.getStart());
					}
				
					int j = bs+1;
					while(m_tokens.get(j) != bet) {
						Token t = m_tokens.get(j);
						if(t instanceof NameValueToken) {
							btt.add((NameValueToken)t);
						} else {
							if(t instanceof EolToken) {
							} else {
								throw new SchemaParserException("Unexpected token!", t.getStart());
							}
						}
						++j;
					}
					for(int k = be; k >= bs; --k) {
						m_tokens.remove(k);
					}
					
				}
			} else {
				break;
			}
			++i;
		
		}
	}
	/**
	 * This looks for single word tokens at the start of a line that are followed by a field name
	 * these are then converted to block type tokens
	 */
	private void identifyBlockTypeInstances() {
		int i = 0;
		while(i < m_tokens.size()){
			//first find next define element
			i = findToken(i, true, SingleWordToken.class);
			
			if(i > 1 && i < m_tokens.size() - 1) {//note the limits here!
				Token t = m_tokens.get(i);

				SingleWordToken swt = (SingleWordToken)t;
				//if this is followed by a field name and starts a line
				//then it is a block type instance
				
				Token nextT = m_tokens.get(i + 1);
				Token prevT = m_tokens.get(i - 1);
				
				if(nextT instanceof FieldNameToken && (prevT instanceof EolToken || prevT instanceof CommentToken)) {
					//yup, this is a BlockTypeInstance
					BlockTypeToken btt = new BlockTypeToken(swt);
					m_tokens.set(i, btt);
				}
				++i;
			} else {
				break;
			}
			
		}
		
	}
	private void collapseNameValues() throws SchemaParserException {
		int i = 0;
		while(i < m_tokens.size()){
			//first find next define element
			i = findToken(i, true, EqualsToken.class);
			
			if(i > 0 && i < m_tokens.size()) {
				int ni = i - 1;//this should point to a name
				int vi = i + 1;//this should point to a number
				int cti = ni -1;//this should point to a comment, maybe
				Token equalsT = m_tokens.get(i);//equals
				Token numberT = m_tokens.get(vi);//number
				Token nameT = m_tokens.get(ni);//name
				Token commentT= null;//comment
				CommentToken ct = null;
				if(cti >= 0) {
					Token t = m_tokens.get(cti);
					if(t instanceof CommentToken) {
						commentT = (CommentToken)t;
					}
				}
				

				if(numberT instanceof NumberToken && nameT instanceof SingleWordToken) {
					NumberToken nt = (NumberToken)numberT;
					SingleWordToken swt = (SingleWordToken)nameT;
					NameValueToken nvt = new NameValueToken(swt, nt, ct);
					if(commentT != null) {
						m_tokens.set(cti, nvt);//put the new thing in the place of the comment
						m_tokens.remove(numberT);//remove the number
						m_tokens.remove(equalsT);//remove the equals
						m_tokens.remove(nameT);//remove the name
						
					} else {
						m_tokens.set(ni, nvt);//put the new thing in the place of the name
						m_tokens.remove(numberT);//remove the number
						m_tokens.remove(equalsT);//remove the equals
					}
					++i;
					
					
				} else {
					throw new SchemaParserException("Incorrect tokens around equals.", numberT.getStart());
				}
			} else {
				break;
			}
		}
	}
	private void collapseNumbers() throws SchemaParserException {
		int i = 0;
		while(i < m_tokens.size()){
			//first find next define element
			i = findToken(i, true, EqualsToken.class);
			
			if(i > 0 && i < m_tokens.size()) {
				++i;
				Token t = (Token)m_tokens.get(i);
				if(t instanceof SingleWordToken) {
					SingleWordToken swt = (SingleWordToken)t;
					NumberToken nt = NumberToken.wrap(swt);
					if(nt != null) {
						m_tokens.set(i, nt);
					} else {
						throw new SchemaParserException("Cannot parse \"" + swt.getName()+ "\"as number.", t.getStart());
					}
				} else {
					throw new SchemaParserException("Incorrect token after equals.", t.getStart());
				}
			} else {
				break;
			}
		}
	}
	private void collapseEols() {
		int i = 0;
		while(i < m_tokens.size()){
			//first find next define element
			i = findToken(i, true, EolToken.class);
				
			int k = i - 1;
			int j = i + 1;
			Token tj = null;
			Token tk = null;
			if(k >= 0) {
				
				tk = m_tokens.get(k);
			}
			
			
			
			if(j < m_tokens.size()) {
				tj = m_tokens.get(j);
			}
			if(tk instanceof BraceStartToken ||
				tk instanceof CommentToken ||
				tk instanceof FieldAllocationToken ||
				tk instanceof NameValueToken ||
				tk instanceof BraceEndToken ||
				tj instanceof EolToken ||
				tj instanceof FieldAllocationToken ||
				tj instanceof BraceStartToken ||
				tj instanceof BraceEndToken ||
				tj instanceof NameValueToken) {
				m_tokens.remove(i);
			} else {
				i = j;
			}
			
			
		}
				
		
	}
	/**
	 * Combine comment tokens if there are consecutive ones
	 * Block comments will not be collapsed. Consecutive line comments will collapse into blocks.
	 * Line comments following block comments will collapse into the block. 
	 */
	private void collapseComments() {
		int i = 0;
		while(i < m_tokens.size()){
			//first find next define element
			i = findToken(i, true, CommentToken.class);
				
			
			if(i >= 0) {
				CommentToken cti = (CommentToken)m_tokens.get(i);
				boolean notDone = true;
				int j = i+1; 
				while(notDone) {
					if(j < m_tokens.size()) {
						Token tj = m_tokens.get(j);
						if(tj instanceof EolToken) {
							++j;
						} else if(tj instanceof CommentToken) {
							CommentToken ctj = (CommentToken)tj;
							if(cti.isLineComnent() || ctj.isLineComnent()) {
								CommentToken ctn = cti.combine(ctj);
								m_tokens.set(i, ctn);
								for(int k = j; k > i; --k) {
									m_tokens.remove(k);
								}
							} else {
								++i;
							}
							notDone = false;
						} else {
							//end becuase we've seen a token that is not an eol or comment
							notDone = false;
							//trigger looking for the next one
							++i;
					
						}
					} else {
						notDone = false;
						i = Integer.MAX_VALUE;
					}
				}
			} else {
				i = Integer.MAX_VALUE;
			}
		}

	}
	/**
	 * processes the name value pairs defined for an eenum
	 * @throws SchemaParserException 
	 */
	private void collapseEnumValues() throws SchemaParserException {
		int i = 0;
		
		while(i < m_tokens.size()){
			//first find next define element
			int j = findToken(i, true, EnumToken.class);

			if(j >= 0) {
				//this should be safe because of how j was determined
				EnumToken et = (EnumToken)(m_tokens.get(j));
				
				int k = findToken(j, true, BraceStartToken.class);
				int m = findToken(k, true, BraceEndToken.class);
				
				if(k < 0) {
					throw new SchemaParserException("Could not find a block start after enum keyword", et.getStart());
				}
				
				if(m < 0) {
					throw new SchemaParserException("Could not find a matching block end after enum block start",m_tokens.get(k).getStart());
				}
				
				Token bs = m_tokens.get(k);
				Token be = m_tokens.get(m);
				i = k;
				
				//everything inside the block should be part of the enum 
				
				CommentToken ct = null;
				for(int x = k+1; x < m; ++x) {
					Token t = m_tokens.get(x);
					
					if(t instanceof NameValueToken) {
						NameValueToken nvt = (NameValueToken)t;
						et.addNameValue(nvt);
					} else if(t instanceof EolToken) {
						//don't do anything here. It will all be fine!
					} else if(t instanceof SingleWordToken) {
						//this is likely an enum element that has not been assigned a value
						//should probably check that next and previous token are EOLs
						SingleWordToken swt = (SingleWordToken)t;
						Token nextT = m_tokens.get(x + 1);
						Token prevT = m_tokens.get(x - 1);
						if(nextT instanceof EolToken) {
							if(prevT instanceof EolToken) {
								NameValueToken nvt = new NameValueToken(swt, null, null);
								et.addNameValue(nvt);
							} else {
								throw new SchemaParserException("Unexpected input while parsing enum elements", prevT.getStart());
							}
						
							
						} else {
							throw new SchemaParserException("Unexpected input while parsing enum elements", nextT.getStart());

						}
					} else {
						throw new SchemaParserException("Did not expect "+t.toString() + "in enum block!", t.getStart());
					}
				}
				
				//now remove the stuff in the block
				for(int x = m; x >= k; --x) {
					m_tokens.remove(x);
				}
				
			
				
			} else {
				break;
			}
		}
		
	}
	
	private int findToken(int i, boolean forwardNotReverse, Class<?>... cs) {
		if(i < 0) {
			return -1;
		}
		int result = -1;
		boolean notDone = true;
		int n = m_tokens.size();
		int j = i;
		while(notDone) {
			Token t = m_tokens.get(j);
			for(Class<?> c : cs) {
				if(t.getClass() == c) {
					result = j;
					break;
				}
			}
			if(result >= 0) {
				break;
			} else if(forwardNotReverse) {
				++j;
				if(j >= n) {
					notDone = false;
				}
			} else {
				--j;
				if(j < 0) {
					notDone = false;
				}
			}
		}
		return result;
	}
	/**
	 * now any single word token at the end of a line is a fieldname or before a block start
	 * @throws SchemaParserException 
	 */
	private void identifyFieldNames() throws SchemaParserException {
		boolean notDone = true;
		int i = 0;
		int n = m_tokens.size();
		while(i < n) {
			int eol = findToken(i, true, EolToken.class);
			int fnti = eol;
			if(eol >= 0) {
				//rewind to the previous open bracket
				int bracket = findToken(eol, false, BracketStartToken.class);
				if(bracket > i) {//only valid if it occurs after the point that we started looking
					fnti = bracket;
				}
				//rewind to the first single word token. This should be a field
				fnti = findToken(fnti, false, SingleWordToken.class);
				if(fnti > i) {
					
					FieldNameToken fnt = new FieldNameToken((SingleWordToken)m_tokens.get(fnti));
					m_tokens.set(fnti, fnt);//this will replace the swt
				}
				i = eol+1;
			} else {
				break;
			}
		}
		
	}
	/**
	 * any single word token at the start of a line or after a 
	 */
	private void identifyBlockTypes() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * identifies that last token of the line that the specified index is on
	 * @param i
	 * @return
	 */
	private int advanceToEndOfLine(int i) {
		Token ti = m_tokens.get(i);
		int j = i;
		int n = m_tokens.size();
		int result = i;
		while(j < n) {
			Token tj = m_tokens.get(j);
			if(tj.getStart().getLineIndex() > ti.getStart().getLineIndex()) {
				break;
			}
			result = j;
		}
		return j;
	}
	/**
	 * Collapse all the define tokens and the following single word tokens together
	 * @throws SchemaParserException 
	 */
	private void collapseDefines() throws SchemaParserException {
		int i = 0;
		
		while(i < m_tokens.size()){
			//first find next define element
			int j = findToken(i, true, DefineToken.class);

			if(j >= 0) {
				//this should be safe because of how j was determined
				DefineToken dt = (DefineToken)(m_tokens.get(j));
				//now get next token
				int k = findToken(j, true, SingleWordToken.class);
				if(k == j + 1) {//it better be the very next token
					SingleWordToken swt = (SingleWordToken)(m_tokens.get(k));
					dt.setTypeName(swt.getName());
					m_tokens.remove(k);
				} else {
					throw new SchemaParserException("Define keyword must be followed by a type name!", dt.getStart());
				}
				i = k;
			} else {
				break;
			}
		}
	}
	/**
	 * Collapse all the enum tokens and the following base type together
	 * @throws SchemaParserException 
	 */
	private void collapseEnums() throws SchemaParserException {
		int i = 0;
		
		while(i < m_tokens.size()){
			//first find next define element
			int j = findToken(i, true, EnumToken.class);

			if(j >= 0) {
				//this should be safe because of how j was determined
				EnumToken et = (EnumToken)(m_tokens.get(j));
				//now get next token
				int k = findToken(j, true, BaseTypeToken.class);
				if(k == j + 1) {//it better be the very next token
					BaseTypeToken swt = (BaseTypeToken)(m_tokens.get(k));
					et.setBaseType(swt.getBaseType());
					m_tokens.remove(k);
				} else {
					throw new SchemaParserException("Enum keyword must be followed by a base type name!", et.getStart());
				}
				i = k;
			} else {
				break;
			}
		}
	}
	private Coord processEol(Coord c) {
		Coord result = c;
		if(result.remainingString().isBlank()) {
			m_tokens.add(new EolToken(result));
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
			m_tokens.add(new BraceStartToken(start));
			break;
		case FIELD_BLOCK_END:
			m_tokens.add(new BraceEndToken(start));
			break;
		case BRACKET_START:
			m_tokens.add(new BracketStartToken(start));
			break;
		case BRACKET_END:
			m_tokens.add(new BracketEndToken(start));
			break;
		case EQUALS:
			m_tokens.add(new EqualsToken(start));
			break;
		case COMPOUND_MODIFIER:
			m_tokens.add(new CompoundToken(start, end, s));
			break;
		case ENUM_MODIFIER:
			m_tokens.add(new EnumToken(start, end));
			break;
		case BLOCK_MODIFIER:
			m_tokens.add(new BlockToken(start, end));
			break;
		case DEFINED_BLOCK_TOKEN:
			m_tokens.add(new DefineToken(start, end));
			break;
		default:
			BaseTypeToken bte = BaseTypeToken.makeNew(start, end, s);
			if(bte != null) {
				m_tokens.add(bte);
			} else {
				m_tokens.add(new SingleWordToken(start, end, s));
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
			
			m_tokens.add(getFirstIndexBeforeLine(start.line), new CommentToken(start, end, comment, false));
		}
		
		return result;
	}
	
	private int getFirstIndexBeforeLine(int line) {
		int i = m_tokens.size();
		if(i > 0) {
			--i;
			boolean done = false;
			while(!done) {
				int lt = m_tokens.get(i).getStart().line;
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
					m_tokens.add(new CommentToken(start, end, comment, true));
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

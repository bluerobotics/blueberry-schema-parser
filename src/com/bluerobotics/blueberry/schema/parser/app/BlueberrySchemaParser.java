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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.structure.ArrayField;
import com.bluerobotics.blueberry.schema.parser.structure.BaseField;
import com.bluerobotics.blueberry.schema.parser.structure.BlockField;
import com.bluerobotics.blueberry.schema.parser.structure.BoolField;
import com.bluerobotics.blueberry.schema.parser.structure.BoolFieldField;
import com.bluerobotics.blueberry.schema.parser.structure.CompoundField;
import com.bluerobotics.blueberry.schema.parser.structure.EnumField;
import com.bluerobotics.blueberry.schema.parser.structure.AbstractField;
import com.bluerobotics.blueberry.schema.parser.structure.FieldName;
import com.bluerobotics.blueberry.schema.parser.structure.FieldUtils;
import com.bluerobotics.blueberry.schema.parser.structure.FixedIntField;
import com.bluerobotics.blueberry.schema.parser.structure.ParentField;
import com.bluerobotics.blueberry.schema.parser.structure.Type;
import com.bluerobotics.blueberry.schema.parser.tokens.ArrayToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BaseTypeToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BaseTypeToken.BaseType;
import com.bluerobotics.blueberry.schema.parser.tokens.BlockToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BlockTypeToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BraceEndToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BraceStartToken;
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
import com.bluerobotics.blueberry.schema.parser.tokens.FieldAllocationOwner;
import com.bluerobotics.blueberry.schema.parser.tokens.FieldAllocationToken;
import com.bluerobotics.blueberry.schema.parser.tokens.FieldNameToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NameValueToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NestedFieldAllocationToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NumberToken;
import com.bluerobotics.blueberry.schema.parser.tokens.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.tokens.SingleWordToken;
import com.bluerobotics.blueberry.schema.parser.tokens.Token;
import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants;
import com.bluerobotics.blueberry.schema.parser.tokens.TypeToken;
import com.bluerobotics.blueberry.schema.parser.writers.TestWriter;
import com.starfishmedical.utils.ResourceTools;

/**
 * 
 */
public class BlueberrySchemaParser implements Constants, TokenConstants {
	



	private final ArrayList<Token> m_tokens = new ArrayList<Token>();
	private final ArrayList<DefinedTypeToken> m_defines = new ArrayList<DefinedTypeToken>();
	private BlockField m_topLevelField = null;
	private ArrayList<CommentToken> m_topLevelComments = new ArrayList<CommentToken>();
	private NestedFieldAllocationToken m_topLevelToken = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sargs = "[";
		boolean firstTime = true;
		for(String s : args) {
			sargs = sargs + (firstTime ? "" : " ") + s;
			firstTime = false;
		}
		sargs += "]";
		System.out.println("BlueberrySchemaParser args:"+ sargs);
		String sf = ResourceTools.loadText(RESOURCE_PATH + "testSchema.txt");
		BlueberrySchemaParser p = new BlueberrySchemaParser();
		try {
			p.parse(sf.split("\\R"));
		} catch (SchemaParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TestWriter tw = new TestWriter(new File(""));
		tw.write(p.getTopLevelField(), p.getHeader());
		System.out.println(tw.getOutput());
		
		
		
		
		
	}
	public String[] getHeader() {
		String[] result = new String[m_topLevelComments.size()];
		for(int i = 0; i < m_topLevelComments.size(); ++i) {
			CommentToken ct = m_topLevelComments.get(i);
			result[i] = ct.combineLines();
			
		}
		return result;
	}
	/**
	 * this method performs the parsing of the schema and builds a structure of fields to represent the packet
	 * @param schema - a string containing the schema to parse
	 * @throws SchemaParserException
	 */
	public void parse(String[] schemaLines) throws SchemaParserException {
		//split into lines
		
		Coord c = new Coord(0,0, schemaLines);
		
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
			collapseBlockTypeTokenNameValues();
			collapseBaseTypeAllocations();
			collapseNestedFieldAllocations();
			collapseDefinedTypes();			
			collapseEols();
			collapseDefinedTypeComments();
			collapseDefinedTypeFields(0, null);
			removeTopLevelComments();
			removeTopLevelField();
			
			fillInMissingEnumValues();
			checkForDuplicateEnumValues();
			fillInMissingKeyValues(m_topLevelToken);
			checkForDuplicateKeyValues(m_topLevelToken);
			checkKeyValuesForSize(m_topLevelToken);
			
			if(m_tokens.size() > 0) {
				throw new SchemaParserException("Tokens left over after parsing.", m_tokens.get(0).getStart());
			}
			
			
			//now build model of packets
			
			buildPackets(m_topLevelToken, null);
			FieldUtils fu = new FieldUtils();
			fu.padExtraSpaceInCompoundFields(m_topLevelField);
			fu.computeIndeces(m_topLevelField, 0);
			
			
			
		} catch (SchemaParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("************* Defines ***************");
		for(Token pe : m_defines) {
			System.out.println(pe.toString());
		}
		
		
		if(m_tokens.size() > 0) {
			throw new SchemaParserException("Unexpected token left after parsing.", m_tokens.get(0).getStart());
		}
		
	}

	/**
	 * this is supposed to scan for constant fields and check to be sure they fit in the allocated number of bits
	 * @param nfat
	 */
	private void checkKeyValuesForSize(NestedFieldAllocationToken nfat) {
//		BlockTypeToken btt = (BlockTypeToken)nfat.getType();//this should always be of this type for an nfat - I think
//
//		DefinedTypeToken dtt = lookupType(btt.getName());
//		if(dtt instanceof BlockToken) {//only do this for block tokens (and array tokens)
//			for(NameValueToken nvt : btt.getNameValues()) {
//				long v = nvt.getValue();
//				
//				//now check for size
//				double d = (double)v;
//				d = Math.log(d);
//				d /= Math.log(2);
//				//d is now number of bits
//				System.out.println("blah blah blah "+d);
//				for(FieldAllocationToken fat : dtt.)
//				
//			}
//			
//		}
//		for(FieldAllocationToken fat : nfat.getFields()) {
//			if(fat instanceof NestedFieldAllocationToken) {
//				//recurse
//				checkKeyValuesForSize((NestedFieldAllocationToken)fat);
//				//now check this one
//				
//			}
//		}
	}

	/**
	 * scan all block fields to check for any with duplicate key values
	 * @param nfat
	 * @throws SchemaParserException
	 */
	private void checkForDuplicateKeyValues(NestedFieldAllocationToken nfat) throws SchemaParserException {
		for(FieldAllocationToken fat : nfat.getFields()) {
			if(fat instanceof NestedFieldAllocationToken) {
				//recurse
				fillInMissingKeyValues((NestedFieldAllocationToken)fat);
				//now check this one
				BlockTypeToken btt = (BlockTypeToken)fat.getType();//this should always be of this type for an nfat - I think
				DefinedTypeToken dtt = lookupType(btt.getName());
				if(dtt instanceof BlockToken) {//only do this for block tokens (and array tokens)
					NameValueToken nvt = btt.getValue(KEY_FIELD_NAME);
					long v = nvt.getValue();
					//now check all other fields 
	
					for(FieldAllocationToken fat2 : nfat.getFields()) {
						if(fat instanceof NestedFieldAllocationToken) {
							BlockTypeToken btt2 = (BlockTypeToken)fat2.getType();//this should always be of this type for an nfat - I think
							NameValueToken nvt2 = btt2.getValue(KEY_FIELD_NAME);

							if(nvt2 != nvt) {
								if(nvt2.getValue() == v) {
									throw new SchemaParserException("Two block allocations have the same key value!", nvt.getNumberToken().getStart());
								}
								
							}
						}
					}
				}
			}
		}
	
	}
	/**
	 * traverse the hierarchy and check every defined type to be sure they have a key value set
	 * @param m_topLevelToken2
	 */
	private void fillInMissingKeyValues(NestedFieldAllocationToken nfat) {
		for(FieldAllocationToken fat : nfat.getFields()) {
			if(fat instanceof NestedFieldAllocationToken) {
				//recurse
				fillInMissingKeyValues((NestedFieldAllocationToken)fat);
				//now check this one
				BlockTypeToken btt = (BlockTypeToken)fat.getType();//this should always be of this type for an nfat - I think
				DefinedTypeToken dtt = lookupType(btt.getName());
				if(dtt instanceof BlockToken) {//only do this for block tokens (and array tokens)
					NameValueToken nvt = btt.getValue(KEY_FIELD_NAME);
					if(nvt == null) {
						//this doesn't have one.
						//cycle through its siblings to determine a good value
						long v = 0;
						for(FieldAllocationToken fat2 : nfat.getFields()) {
							if(fat instanceof NestedFieldAllocationToken) {
								BlockTypeToken btt2 = (BlockTypeToken)fat2.getType();//this should always be of this type for an nfat - I think
								NameValueToken nvt2 = btt2.getValue(KEY_FIELD_NAME);
	
								if(nvt2 != null) {
									if(nvt2.getValue() == v) {
										++v;
									}
									
								}
							}
						}
						//v should now be a good value
						btt.add(new NameValueToken(KEY_FIELD_NAME, v));
					}
					
				}
			}
		}
	}



	
	/**
	 * Scans enum tokens and checks for duplicate values
	 * @throws SchemaParserException 
	 */
	private void checkForDuplicateEnumValues() throws SchemaParserException {
		for(Token t : m_defines) {
			if(t instanceof EnumToken) {
				EnumToken et = (EnumToken)t;
				for(NameValueToken nvt : et.getNameValueTokens()) {
					if(!nvt.isValue()) {
						throw new SchemaParserException("Weird! There is no value set for this enum value.", nvt.getStart());
					}
					for(NameValueToken nvt2 : et.getNameValueTokens()) {
						if(nvt2 == nvt) {
							//don't worry, these are the same element
						} else if(nvt.getName().equals(nvt2.getName())) {
							//found two names that are the same
							throw new SchemaParserException("Two enum element names are the same!", nvt2.getStart());
						} else if(nvt.getValue() == nvt2.getValue()) {
							throw new SchemaParserException("Two enum elements have the same values!", nvt2.getStart());
						}
					}
				}
			}
		}
	}
	/**
	 * Scans for enum tokens that are missing values for their elements.
	 * Fills them in with the smallest, unused, positive, integer value.
	 */
	private void fillInMissingEnumValues() {
		for(Token t : m_defines) {
			if(t instanceof EnumToken) {
				EnumToken et = (EnumToken)t;
				long nextValue = 0;
				for(NameValueToken nvt : et.getNameValueTokens()) {
					if(nvt.isValue()) {
						if(nvt.getValue() == nextValue) {
							++nextValue;
						}
					} else {
						nvt.setValue(nextValue);
					}
				}
			}
		}
	}
	/**
	 * constructs a hierarchy of fields to represent all the packets
	 * This applies all defined types 
	 * @param t
	 * @param parentField
	 * @throws SchemaParserException
	 */
	private void buildPackets(FieldAllocationToken t, ParentField parentField) throws SchemaParserException {
		TypeToken tt = t.getType();
		AbstractField f = null;
		if(tt instanceof BlockTypeToken) {
			BlockTypeToken btt = (BlockTypeToken)tt;
			DefinedTypeToken type = lookupType(tt.getName());
			if(type != null) {
				f = makeField(t);
//				f = makeField(type, fieldName, ct == null ? type.getComment() : ct.combine(type.getComment()));
				
				if(f instanceof BlockField) {
					//if this is a block type then add header stuff
					BlockField bf = (BlockField)f;
					if(type instanceof FieldAllocationOwner) {
						FieldAllocationOwner fao = (FieldAllocationOwner)type;
						for(FieldAllocationToken fat : fao.getFields()) {
							BaseField f2 = (BaseField)makeField(fat);
							if(f2.isInt()) {
								NameValueToken nvt = btt.getValue(fat.getFieldName());
								if(nvt != null) {
									f2 = new FixedIntField(f2, nvt.getValue());
								}
							}
							bf.addToHeader(f2);
						}
					}
					
				} else if(f instanceof CompoundField) {
					CompoundField cf = (CompoundField)f;
					if(type instanceof FieldAllocationOwner) {
						FieldAllocationOwner fao = (FieldAllocationOwner)type;
						for(FieldAllocationToken fat : fao.getFields()) {
							if(fat instanceof NestedFieldAllocationToken) {
								buildPackets(fat, cf);
							} else {
								cf.add(makeField(fat));
							}
							
						}
					}
					
				}
		
			} else {
				throw new SchemaParserException("Could not find defined type: \"" + tt.getName() + "\"", tt.getStart());
			}
		} else {
			//it's not a clock type token, which means it's not a defined type
			f = makeField(t);
			
		}
		if(t instanceof NestedFieldAllocationToken) {
			NestedFieldAllocationToken nfat = (NestedFieldAllocationToken)t;
			if(f instanceof ParentField) {
				//add child fields
				ParentField pf = (ParentField)f;
				

				List<FieldAllocationToken> list = nfat.getFields();
				for(FieldAllocationToken fat : list) {
					buildPackets(fat, pf);//recurse into nested token
					
				}
				
			
//			} else {
//				//it should always be that if its a nested token it should be a parent field
//				throw new RuntimeException("This should never have happened - I think.");
			}
		}
	
		if(f != null) {
			if(parentField == null) {
				m_topLevelField = (BlockField)f;
			} else {
				parentField.add(f);
			}
		} else {
			throw new SchemaParserException("Couldn't make a field for some reason", null);
		}
	}
	
	/**
	 * creates a field from a token
	 * @param fat
	 * @return
	 */
	private AbstractField makeField(FieldAllocationToken fat) {
//		f = makeField(type, fieldName, ct == null ? type.getComment() : ct.combine(type.getComment()));
		
		TypeToken t = fat.getType();
		String fieldName = fat.getFieldName();
		CommentToken comment = fat.getComment();
		TypeToken tt = t;//keep track of the original type
		//if it's a block type token (i.e. it was a defined type) then replace with the actual type
		if(t instanceof BlockTypeToken) {
			DefinedTypeToken dtt = lookupType(t.getName()); 
			
			t = dtt;
			comment = comment == null ? dtt.getComment() : comment.combine(dtt.getComment());
		}
		
		
		AbstractField result = null;
		
		String c = getComment(comment);
		
		
		if(t instanceof ArrayToken) {
//			ArrayToken at = (ArrayToken)t;
			result = new ArrayField(FieldName.fromCamel(fieldName), FieldName.fromCamel(tt.getName()), c);
		} else if(t instanceof BlockToken) {
			result = new BlockField(FieldName.fromCamel(fieldName), FieldName.fromCamel(tt.getName()), c);
		} else if(t instanceof EnumToken) {
			EnumToken et = (EnumToken)t;
			
			
			EnumField ef = new EnumField(FieldName.fromCamel(fieldName), FieldName.fromCamel(tt.getName()), lookupBaseType(et.getBaseType()), getComment(et.getComment()));
			for(NameValueToken nvt : et.getNameValueTokens()) {
				
				ef.addNameValue(FieldName.fromSnake(nvt.getName()), nvt.getValue(), getComment(nvt.getComment()));
			
			}
			result = ef;
		} else if(t instanceof CompoundToken) {
			CompoundField cf = new CompoundField(FieldName.fromCamel(fieldName), FieldName.fromCamel(tt.getName()), c);
			result = cf;
		
			
		} else if(t instanceof BaseTypeToken) {
			BaseTypeToken btt = (BaseTypeToken)t;
			Type tp = lookupBaseType(btt.getBaseType());
			switch(tp) {
			
			case BOOL:
				result = new BoolField(FieldName.fromCamel(fieldName), c);
				break;
			case INT16:
			case INT32:
			case INT8:
			case UINT16:
			case UINT32:
			case UINT8:
			case FLOAT32:
				result = new BaseField(FieldName.fromCamel(fieldName), tp, c);
				break;
			
			default:
				break;
			
			
			}
			
		}
		if(result == null) {
			throw new RuntimeException("Result is null!");
		}
		return result;
	}
	/**
	 * converts from a token base type to a field type
	 * @param bt
	 * @return
	 */
	Type lookupBaseType(BaseType bt) {
		Type result = null;
		switch(bt) {
		case BOOL:
			result = Type.BOOL;
			break;
		case FLOAT32:
			result = Type.FLOAT32;
			break;
		case INT16:
			result = Type.INT16;
			break;
		case INT32:
			result = Type.INT32;
			break;
		case INT8:
			result = Type.INT8;
			break;
		case UINT16:
			result = Type.UINT16;
			break;
		case UINT32:
			result = Type.UINT32;
			break;
		case UINT8:
			result = Type.UINT8;
			break;
		
		
		}
		return result;
		
	}
	/**
	 * helper method to trap nulls
	 * @param ct
	 * @return
	 */
	private String getComment(CommentToken ct) {
		return ct == null ? "" : ct.combineLines();
	}
	/**
	 *  looks up a defined type given a type name
	 * @param typeName
	 * @return
	 */
	private DefinedTypeToken lookupType(String typeName) {
		DefinedTypeToken result = null;
		for(DefinedTypeToken t : m_defines) {
			if(t.getDefineToken() == null) {
				System.out.println("blah");
			}
			if(t.getDefineToken().getTypeName().equals(typeName)) {
				result = t;
			}
		}
		return result;
	}
	/**
	 * There probably is only one nested field allocation token left
	 * Remove it and set is as the top level token
	 */
	private void removeTopLevelField() {
		for(Token t : m_tokens) {
			if(t instanceof NestedFieldAllocationToken) {
				m_topLevelToken  = (NestedFieldAllocationToken)t;
				m_tokens.remove(t);
				break;
			}
		}		
	}
	/**
	 * Any comments left must be file-level comments
	 * This includes any file header comments, light license headers, etc.
	 */
	private void removeTopLevelComments() {
		ArrayList<Token> ts = new ArrayList<Token>();
		ts.addAll(m_tokens);
		for(Token t : ts) {
			if(t instanceof CommentToken) {
				m_topLevelComments.add((CommentToken)t);
				m_tokens.remove(t);
				
			}
		}
	}


	/**
	 * This grabs any comments that precede a defined type token and assigns to that token
	 */
	private void collapseDefinedTypeComments() {
		int i = 0;
		while(i < m_tokens.size()){
			//find the next brace
			i = findToken(i, true, BlockToken.class, CompoundToken.class, EnumToken.class);
			
			if(i > 0 && i < m_tokens.size()) {
				DefinedTypeToken dtt = (DefinedTypeToken)m_tokens.get(i);
				Token prevT = m_tokens.get(i - 1);
				if(prevT instanceof CommentToken) {
					dtt.setComment((CommentToken)prevT);
					m_tokens.remove(i - 1);
				}
				++i;
			} else {
				break;
			}
		}
	}
	/**
	 * scan a range of tokens to resolve field allocaations in defined types
	 * this will recurse and close all braces from deepest level outwards
	 * @param start - the first index to scan
	 * @param owner - the owner of the block of field allocations
	 * @return the index of a closing brace token
	 * @throws SchemaParserException 
	 */
	private int collapseDefinedTypeFields(int start, FieldAllocationOwner owner) throws SchemaParserException {
		int result = -1;
		int i = start;
		int bsi = -1;

		while(i < m_tokens.size()){
			//find the next brace
//			i = findToken(i, true, BraceStartToken.class, BraceEndToken.class);
			
			Token ti = m_tokens.get(i);
			if(bsi < 0 && owner != null) {
				if(ti instanceof CommentToken) {
					
				} else if(ti instanceof BraceStartToken) {
					bsi = i;
				} else {
					result = start;
					break;
				}
				++i;
				
			} else if(ti instanceof FieldAllocationOwner) {
				
				//we've found another owner so recurse
				int m = collapseDefinedTypeFields(i + 1, (FieldAllocationOwner)ti);
				if(m < 0) {
					break;
				} else if(ti instanceof DefinedTypeToken) {
					m_defines.add((DefinedTypeToken)ti);
					m_tokens.remove(i);
				} else if(owner != null && ti instanceof FieldAllocationToken) {
					owner.add((FieldAllocationToken)ti);
					m_tokens.remove(i);
				}
//				i = m;
			} else if(ti instanceof EnumToken) {
				m_defines.add((EnumToken)ti);
				m_tokens.remove(i);
			} else if(ti instanceof BraceEndToken) {
				//can probably delete everything since the brace start, which was bsi
				if(bsi < 0) {
					throw new SchemaParserException("That's weird, we should have had an open brace before this, line "+ti.getStart().getLineIndex(), ti.getStart());
				}
				for(int k = i; k >= bsi; --k) {
					m_tokens.remove(k);
				}
				result = start;
				break;//bail out of this level of recursion
			} else if(ti instanceof FieldAllocationToken) {
				if(owner != null) {
					owner.add((FieldAllocationToken)ti);
				}
				++i;
			
			} else  {
				++i;
			}
			if(i >= m_tokens.size()) {
				if(bsi >= 0) {
					throw new SchemaParserException("No closing brace!", m_tokens.get(bsi).getStart());
				}
			}
		
		}
		return result;
			
	}
	/**
	 * Any sequence of a define token and a defined type token gets collapsed into a single defined type token
	 * @throws SchemaParserException
	 */
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
	/**
	 * A sequence of Comment, BlockType and FieldName form the allocation of a blockType
	 * These get collapsed into a nested field allocation
	 * @throws SchemaParserException
	 */
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
	
	/**
	 * Any sequence of a Comment, BaseType, FieldName is the allocation of a base type field
	 * so collapse them to a single field allocation token
	 * @throws SchemaParserException
	 */
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
	 * These will define constant values in the block header
	 * @throws SchemaParserException 
	 */
	private void collapseBlockTypeTokenNameValues() throws SchemaParserException {
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
	/**
	 * now collapse any token sequences of form CommentToken, SingleValueToken, EqualsToken, NumberToken to a NameValueToken 
	 * @throws SchemaParserException
	 */
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
	/**
	 * any single word token after an equals sign must be a number so replace it with a number token
	 * @throws SchemaParserException
	 */
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
	/**
	 * remove all end of line tokens that are preceded or followed by a token that no longer needs an EOL token
	 * This likely now removes all EOL tokens
	 */
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
	 * processes the name value pairs defined for an enum
	 * They are identified as being contained in a following block of braces
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
			
				i = k;
				
				//everything inside the block should be part of the enum 
				
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
	 * The base type becomes the type of the enum
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
		case ARRAY_MODIFIER:
			m_tokens.add(new ArrayToken(start, end));
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
	
		//first check for comment block
		while(result.startsWith(COMMENT_BLOCK_START)) {
			
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
	

	
	
	public BlockField getTopLevelField() {
		return m_topLevelField;
	}

}

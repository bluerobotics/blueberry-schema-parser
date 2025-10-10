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
package com.bluerobotics.blueberry.schema.parser.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.fields.AbstractField;
import com.bluerobotics.blueberry.schema.parser.fields.ArrayField;
import com.bluerobotics.blueberry.schema.parser.fields.BaseField;
import com.bluerobotics.blueberry.schema.parser.fields.BoolField;
import com.bluerobotics.blueberry.schema.parser.fields.DefinedField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField;
import com.bluerobotics.blueberry.schema.parser.fields.Field;
import com.bluerobotics.blueberry.schema.parser.fields.FieldName;
import com.bluerobotics.blueberry.schema.parser.fields.ParentField;
import com.bluerobotics.blueberry.schema.parser.fields.StructField;
import com.bluerobotics.blueberry.schema.parser.fields.Type;
import com.bluerobotics.blueberry.schema.parser.tokens.AnnotationToken;
import com.bluerobotics.blueberry.schema.parser.tokens.ArrayToken;
import com.bluerobotics.blueberry.schema.parser.tokens.BaseTypeToken;
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
import com.bluerobotics.blueberry.schema.parser.tokens.FieldAllocationOwner;
import com.bluerobotics.blueberry.schema.parser.tokens.FieldAllocationToken;
import com.bluerobotics.blueberry.schema.parser.tokens.FilePathToken;
import com.bluerobotics.blueberry.schema.parser.tokens.IdentifierToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NameValueToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NestedFieldAllocationToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NumberToken;
import com.bluerobotics.blueberry.schema.parser.tokens.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.tokens.SingleWordToken;
import com.bluerobotics.blueberry.schema.parser.tokens.SquareBracketStartToken;
import com.bluerobotics.blueberry.schema.parser.tokens.StringToken;
import com.bluerobotics.blueberry.schema.parser.tokens.Token;
import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants;
import com.bluerobotics.blueberry.schema.parser.tokens.TokenList;
import com.bluerobotics.blueberry.schema.parser.tokens.TypeToken;
import com.bluerobotics.blueberry.schema.parser.writers.WriterUtils;

/**
 * This class implements the token parsing algorithm
 * This reads the schema file, tokenizes and then builds a data structure of fields to represent the schema
 */
public class BlueberrySchemaParser implements Constants, TokenConstants {




	private final TokenList m_tokens = new TokenList();
	private final ArrayList<DefinedField> m_defines = new ArrayList<>();
	private StructField m_topLevelField = null;
	private ArrayList<CommentToken> m_topLevelComments = new ArrayList<CommentToken>();
	private NestedFieldAllocationToken m_topLevelToken = null;

	public String[] getHeader() {
		String[] result = new String[m_topLevelComments.size()];
		for(int i = 0; i < m_topLevelComments.size(); ++i) {
			CommentToken ct = m_topLevelComments.get(i);
			result[i] = ct.combineLines();

		}
		return result;
	}
	public void clear() {
		m_tokens.clear();
		m_defines.clear();

	}
	/**
	 * this method performs the parsing of the schema and builds a structure of fields to represent the packet
	 * @param filePath - a string of the file path of this file
	 * @param schema - a string containing the schema to parse
	 * @throws SchemaParserException
	 */
	public void append(String filePath, String[] schemaLines) throws SchemaParserException {
		//split into lines

		Coord c = new Coord(filePath, 0,0, schemaLines);	/**
		 * scan a range of tokens to resolve field allocaations in defined types
		 * this will recurse and close all braces from deepest level outwards
		 * @param start - the first index to scan
		 * @param owner - the owner of the block of field allocations
		 * @return the index of a closing brace token
		 * @throws SchemaParserException
		 */


		m_tokens.add(new FilePathToken(c, filePath));

		try {
			while(c != null) {
				c = c.trim();
				c = processBlockComment(c);
				c = processLineComment(c);
				c = processStrings(c);
				c = processNextToken(c);
				c = processEol(c);

			}
		} catch (SchemaParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void parse() throws SchemaParserException {
		try {
			collapseComments();
//			collapseDefines();
			collapseNumbers();
			collapseBaseTypes();
			collapseAnnotations();
			collapseNameValues();
			collapseEols();
			coolapseSemicolons();
			checkBrackets();
//			collapseEnums();
//			collapseEnumValues();
//			identifyFieldNames();
//			identifyBlockTypeInstances();
//			collapseBlockTypeTokenNameValues();
//			collapseBaseTypeAllocations();
//			collapseNestedFieldAllocations();
//			collapseDefinedTypes();
//
//			collapseDefinedTypeComments();
//			collapseDefinedTypeFields(0, null);
//			removeTopLevelComments();
//			removeTopLevelField();
//
//			fillInMissingEnumValues();
//			checkForDuplicateEnumValues();
//			List<NestedFieldAllocationToken> blocks = listAllBlocks(m_topLevelToken, null);
//			fillInMissingKeyValues(blocks);
//			checkForDuplicateKeyValues(blocks);
//			checkKeyValuesForSize(m_topLevelToken);
//
//			if(m_tokens.size() > 0) {
//				throw new SchemaParserException("Tokens left over after parsing.", m_tokens.get(0).getStart());
//			}
//
//
//			//now build model of packets
//
//			buildPackets(m_topLevelToken, null);
//			FieldUtils fu = new FieldUtils();
//			fu.padExtraSpaceInCompoundFields(m_topLevelField);
//			fu.computeIndeces(m_topLevelField, 0);
////			fu.computeParents(m_topLevelField);
////			fu.removeDuplicates(m_topLevelField, null);
			assembleFields();
//			extractEnums();
//			extractTypedefs();
//			extractStructs();

		} catch (SchemaParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("************* Defines ***************");
		for(DefinedField pe : m_defines) {
			System.out.println(pe.toString());
		}




	}


	private void assembleFields() {
		FieldName module = null;
		ParentField parent = null;

		m_tokens.resetIndex();

		while(m_tokens.isMore()) {
			if(parent == null) {//we're not currently doing anything
				Token t = m_tokens.getCurrent();
				IdentifierToken it = m_tokens.relative(0,IdentifierToken.class);
				if(it != null) {
					switch(it.getKeyword()) {
					case CONST:
						break;
					case ENUM:
						break;
					case MESSAGE:
						break;
					case MODULE:
						break;
					case SEQUENCE:
						break;
					case STRUC:
						break;
					case TYPEDEF:
						break;
					}
				} else if(t instanceof AnnotationToken) {

				}

			}
		}
	}
	/**
	 * Checks braces. Makes sure they are properly opened and closed and relate to known keywords
	 * @throws SchemaParserException
	 */
	private void checkBrackets() throws SchemaParserException {

		Token result = m_tokens.get(0);
		while(result != null) {
			result = m_tokens.matchBrackets(result);
			result = m_tokens.next(result);
		}
	}

	private void extractStructs() {
		// TODO Auto-generated method stub

	}
	private void extractTypedefs() {
		// TODO Auto-generated method stub

	}
	/**
	 * Scans for enum definitions
	 * Should be of the form <comment?><enum><typeName><colon?><baseType?><openBrace>
	 * Note this doesn't check for
	 * @throws SchemaParserException
	 */
	private void extractEnums() throws SchemaParserException {
		m_tokens.resetIndex();
		while(m_tokens.isAtEnd()) {
			IdentifierToken et = m_tokens.gotoNextId(TokenIdentifier.ENUM);
			if(et != null) {
				CommentToken enumComment = m_tokens.relative(-1, CommentToken.class);
				SingleWordToken enumName = m_tokens.relative(1, SingleWordToken.class);
				IdentifierToken colon = m_tokens.relativeId(2, TokenIdentifier.COLON);
				BaseTypeToken enumType = m_tokens.relative(3, BaseTypeToken.class);
				IdentifierToken braceStart = m_tokens.relativeId(colon == null ? 2 : 4, TokenIdentifier.COLON);
				IdentifierToken braceEnd = m_tokens.matchBrackets(braceStart);
				if(enumName != null && braceStart != null && braceEnd != null) {
//					EnumField ef = new EnumField(enumName.getFieldName(), );
//					m_defines.add(ef);
					Token t = braceStart;
					while(m_tokens.inOrder(t, braceEnd)) {
						SingleWordToken swt = m_tokens.findNext(t, SingleWordToken.class);


					}
				}


			}
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
	private void checkForDuplicateKeyValues(List<NestedFieldAllocationToken> blocks) throws SchemaParserException {
		for(NestedFieldAllocationToken nfat : blocks) {
			BlockTypeToken btt = (BlockTypeToken)nfat.getType();
			DefinedTypeToken dtt = lookupType(btt.getName());
			NameValueToken nvt = btt.getValue(KEY_FIELD_NAME);
			long v = nvt.getValue();
			//now check all other fields

			for(NestedFieldAllocationToken nfat2 : blocks) {
				if(nfat2 != nfat) {//don't check against itself
					BlockTypeToken btt2 = (BlockTypeToken)nfat2.getType();
					NameValueToken nvt2 = btt2.getValue(KEY_FIELD_NAME);
					long v2 = nvt2.getValue();
					if(v2 == v) {
						throw new SchemaParserException("Two block allocations have the same key value!", nvt.getNumberToken().getStart());
					}
				}
			}
		}

	}
	private List<NestedFieldAllocationToken> listAllBlocks(NestedFieldAllocationToken nfat, List<NestedFieldAllocationToken> blocks) {
		if(blocks == null) {
			blocks = new ArrayList<NestedFieldAllocationToken>();
		}
		for(FieldAllocationToken fat : nfat.getFields()) {
			if(fat instanceof NestedFieldAllocationToken) {
				//recurse
				listAllBlocks((NestedFieldAllocationToken)fat, blocks);
				//now check this one
				BlockTypeToken btt = (BlockTypeToken)fat.getType();//this should always be of this type for an nfat - I think
				DefinedTypeToken dtt = lookupType(btt.getName());
				if(dtt instanceof BlockToken) {//only do this for block tokens (and array tokens)
					blocks.add((NestedFieldAllocationToken)fat);
				}
			}
		}
		Collections.sort(blocks, (n1, n2) -> {
			BlockTypeToken b1 = (BlockTypeToken)n1.getType();
			BlockTypeToken b2 = (BlockTypeToken)n2.getType();
			NameValueToken nvt1 = b1.getValue(KEY_FIELD_NAME);
			NameValueToken nvt2 = b2.getValue(KEY_FIELD_NAME);
			int result = -1;
			if(nvt1 != null && nvt2 != null) {
				result = (int)(nvt1.getValue() - nvt2.getValue());
			}
			return result;
		});
		return blocks;
	}
	/**
	 * traverse the hierarchy and check every defined type to be sure they have a key value set
	 * @param m_topLevelToken2
	 */
	private void fillInMissingKeyValues(List<NestedFieldAllocationToken> blocks) {
		ArrayList<Long> values = new ArrayList<Long>();

		//first scan for existing values
		for(NestedFieldAllocationToken nfat : blocks) {
			BlockTypeToken btt = (BlockTypeToken)nfat.getType();
			DefinedTypeToken dtt = lookupType(btt.getName());
			NameValueToken nvt = btt.getValue(KEY_FIELD_NAME);
			if(nvt != null) {
				long vt = nvt.getValue();
				values.add(vt);
			}

		}
		long v = 0;
		Collections.sort(values);
		//now scan for non-set ones
		for(NestedFieldAllocationToken nfat : blocks) {
			BlockTypeToken btt = (BlockTypeToken)nfat.getType();
			DefinedTypeToken dtt = lookupType(btt.getName());
			NameValueToken nvt = btt.getValue(KEY_FIELD_NAME);
			if(nvt == null) {
				//get next value
				for(Long vn : values) {
					if(vn == v) {
						++v;
					} else {
						break;
					}
				}
				btt.add(new NameValueToken(KEY_FIELD_NAME, v));
				System.out.println("BlueberrySchemaParser.fillInMissingKeyValues found block with no key set. Assigned a value of *** " + nfat.getFieldName()+ "(key = " + WriterUtils.formatAsHex(v, 4) + ") ***");
				++v;
			}

		}



	}




	/**
	 * Scans enum tokens and checks for duplicate values
	 * @throws SchemaParserException
	 */
	private void checkForDuplicateEnumValues() throws SchemaParserException {
		for(DefinedField t : m_defines) {
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
		for(DefinedField t : m_defines) {
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
//		TypeToken tt = t.getType();
//		AbstractField f = null;
//		if(tt instanceof BlockTypeToken) {
//			BlockTypeToken btt = (BlockTypeToken)tt;
//			DefinedTypeToken type = lookupType(tt.getName());
//			if(type != null) {
//				f = makeField(t);
////				f = makeField(type, fieldName, ct == null ? type.getComment() : ct.combine(type.getComment()));
//
//				if(f instanceof StructField) {
//					//if this is a block type then add header stuff
//					StructField bf = (StructField)f;
//					if(type instanceof FieldAllocationOwner) {
//						FieldAllocationOwner fao = (FieldAllocationOwner)type;
//						for(FieldAllocationToken fat : fao.getFields()) {
//							BaseField f2 = (BaseField)makeField(fat);
//							if(f2.isInt()) {
//								NameValueToken nvt = btt.getValue(fat.getFieldName());
//								if(nvt != null) {
//									f2 = new FixedIntField(f2, nvt.getValue());
//								}
//							}
//							bf.addToHeader(f2);
//						}
//					}
//
//				} else if(f instanceof CompoundField) {
//					CompoundField cf = (CompoundField)f;
//					if(type instanceof FieldAllocationOwner) {
//						FieldAllocationOwner fao = (FieldAllocationOwner)type;
//						for(FieldAllocationToken fat : fao.getFields()) {
//							if(fat instanceof NestedFieldAllocationToken) {
//								buildPackets(fat, cf);
//							} else {
//								cf.add(makeField(fat));
//							}
//
//						}
//					}
//
//				}
//
//			} else {
//				throw new SchemaParserException("Could not find defined type: \"" + tt.getName() + "\"", tt.getStart());
//			}
//		} else {
//			//it's not a clock type token, which means it's not a defined type
//			f = makeField(t);
//
//		}
//		if(t instanceof NestedFieldAllocationToken) {
//			NestedFieldAllocationToken nfat = (NestedFieldAllocationToken)t;
//			if(f instanceof ParentField) {
//				//add child fields
//				ParentField pf = (ParentField)f;
//
//
//				List<FieldAllocationToken> list = nfat.getFields();
//				for(FieldAllocationToken fat : list) {
//					buildPackets(fat, pf);//recurse into nested token
//
//				}
//
//
////			} else {
////				//it should always be that if its a nested token it should be a parent field
////				throw new RuntimeException("This should never have happened - I think.");
//			}
//		}
//
//		if(f != null) {
//			if(parentField == null) {
//				m_topLevelField = (StructField)f;
//			} else {
//				parentField.add(f);
//			}
//		} else {
//			throw new SchemaParserException("Couldn't make a field for some reason", null);
//		}
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
			result = new StructField(FieldName.fromCamel(fieldName), FieldName.fromCamel(tt.getName()), c);
		} else if(t instanceof EnumToken) {
			EnumToken et = (EnumToken)t;


			EnumField ef = new EnumField(FieldName.fromCamel(fieldName), FieldName.fromCamel(tt.getName()), lookupBaseType(et.getKeyword()), getComment(et.getComment()));
			for(NameValueToken nvt : et.getNameValueTokens()) {

				ef.addNameValue(FieldName.fromSnake(nvt.getName()), nvt.getValue(), getComment(nvt.getComment()));

			}
			result = ef;

		} else if(t instanceof BaseTypeToken) {
			BaseTypeToken btt = (BaseTypeToken)t;
			Type tp = lookupBaseType(btt.getKeyword());
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
	Type lookupBaseType(TokenIdentifier bt) {
		Type result = null;
		switch(bt) {
		case BOOLEAN:
			result = Type.BOOL;
			break;
		case FLOAT:
			result = Type.FLOAT32;
			break;
		case DOUBLE:
			result = Type.FLOAT64;
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
		case UINT64:
			result = Type.UINT64;
			break;
		case INT64:
			result = Type.INT64;
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
		for(DefinedField t : m_defines) {

		}
		return result;
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
					m_tokens.remove(prevT);
				}
				++i;
			} else {
				break;
			}
		}
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
//		while(i < m_tokens.size()){
//			//first find next define element
//			i = findToken(i, true, BlockTypeToken.class);
//
//			if(i > 0 && i < m_tokens.size() - 1) {//note the limits here!
//
//				BlockTypeToken et = (BlockTypeToken)m_tokens.get(i);
//				CommentToken ct = null;
//				FieldNameToken fnt = null;
//				Token tPrev = null;
//				if(i > 0) {
//					tPrev = m_tokens.get(i - 1);
//					if(tPrev instanceof CommentToken) {
//						ct = (CommentToken)tPrev;
//					}
//				}
//				Token tNext = m_tokens.get(i + 1);
//				if(tNext instanceof FieldNameToken) {
//					fnt = (FieldNameToken)tNext;
//					FieldAllocationToken fat = new NestedFieldAllocationToken(fnt, et, ct);
//					if(ct != null) {
//						m_tokens.remove(i);
//						m_tokens.set(tPrev, fat);
//						m_tokens.remove(i + 1);
//						m_tokens.remove(i);
//					} else {
//						m_tokens.set(i,  fat);
//						m_tokens.remove(i + 1);
//					}
//				} else {
//					throw new SchemaParserException("Expected field name token here.", tNext.getStart());
//				}
//			} else {
//				break;
//			}
//		}
	}

	/**
	 * Any sequence of a Comment, BaseType, FieldName is the allocation of a base type field
	 * so collapse them to a single field allocation token
	 * @throws SchemaParserException
	 */
	private void collapseBaseTypeAllocations() throws SchemaParserException {
		int i = 0;
		while(i < m_tokens.size()){
//			//first find next define element
//			i = findToken(i, true, BaseTypeToken.class);
//
//			if(i > 0 && i < m_tokens.size() - 1) {//note the limits here!
//
//				BaseTypeToken swt = (BaseTypeToken)m_tokens.get(i);
//				CommentToken ct = null;
//				FieldNameToken fnt = null;
//				if(i > 0) {
//					Token tPrev = m_tokens.get(i - 1);
//					if(tPrev instanceof CommentToken) {
//						ct = (CommentToken)tPrev;
//					}
//				}
//				Token tNext = m_tokens.get(i + 1);
//				if(tNext instanceof FieldNameToken) {
//					fnt = (FieldNameToken)tNext;
//					FieldAllocationToken fat = new FieldAllocationToken(fnt, swt, ct);
//					if(ct != null) {
//						m_tokens.remove(ct);
//					}
//
//				} else {
//					throw new SchemaParserException("Expected field name token here.", tNext.getStart());
//				}
//			} else {
//				break;
//			}
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
//			//first find next define element
//			i = findToken(i, true, BlockTypeToken.class);
//
//
//			if(i > 0 && i < m_tokens.size()) {//note the limits here!
//				BlockTypeToken btt = (BlockTypeToken)m_tokens.get(i);
//				int bs = findToken(i, true, BracketStartToken.class);
//				int be = findToken(i, true, BracketEndToken.class);
//				BracketStartToken bst = null;
//				BracketEndToken bet = null;
//
//
//				if(bs >= 0) {
//					bst = (BracketStartToken)m_tokens.get(bs);
//					//throw this out if it's not on the same line
//					if(bst.getStart().getLineIndex() != btt.getStart().getLineIndex()) {
//						bst = null;
//					}
//				}
//				if(be >= 0) {
//					bet = (BracketEndToken)m_tokens.get(be);
//				}
//
//				if(bst != null) {
//					if(bet == null) {
//						throw new SchemaParserException("No closing brackets for opening brackets.", bst.getStart());
//					}
//
//					int j = bs+1;
//					while(m_tokens.get(j) != bet) {
//						Token t = m_tokens.get(j);
//						if(t instanceof NameValueToken) {
//							btt.add((NameValueToken)t);
//						} else {
//							if(t instanceof EolToken) {
//							} else {
//								throw new SchemaParserException("Unexpected token!", t.getStart());
//							}
//						}
//						++j;
//					}
//					for(int k = be; k >= bs; --k) {
//						m_tokens.remove(k);
//					}
//
//				}
//			} else {
//				break;
//			}
//			++i;

		}
	}

	/**
	 * now collapse any token sequences of form CommentToken, SingleWordToken, IdentifierToken (equals sign), NumberToken to a NameValueToken
	 * @throws SchemaParserException
	 */
	private void collapseNameValues() throws SchemaParserException {

		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			Token equalsT = m_tokens.gotoNextId(TokenIdentifier.EQUALS);

			if(equalsT != null) {
				CommentToken commentT = m_tokens.relative(-2, CommentToken.class);
				SingleWordToken nameT = m_tokens.relative(-1, SingleWordToken.class);
				NumberToken numberT = m_tokens.relative(+1, NumberToken.class);

				if(nameT != null && numberT != null) {
					NameValueToken nvt = new NameValueToken(nameT, numberT, commentT);
					m_tokens.replace(equalsT, nvt);
					m_tokens.remove(numberT);
					m_tokens.remove(nameT);
					m_tokens.remove(commentT);

				} else {
					throw new SchemaParserException("Incorrect tokens around equals.", numberT.getStart());
				}
			} else {
				break;
			}
		}

	}
	/**
	 * Tests all single word tokens and replaces with a Number token if it's formatted like a number
	 * @throws SchemaParserException
	 */
	private void collapseNumbers() throws SchemaParserException {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			SingleWordToken swt = m_tokens.gotoNext(SingleWordToken.class);
			if(swt == null) {
				break;
			} else {
				NumberToken nt = NumberToken.wrap(swt);
				if(nt == null) {
					m_tokens.next();
				} else {
					m_tokens.replace(swt, nt);
				}
			}

		}
	}
	/**
	 * scans for annotation starting tokens and makes annotation tokens of the next single word token
	 */
	private void collapseAnnotations() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			//first find next define element
			IdentifierToken aT = m_tokens.gotoNextId(TokenIdentifier.ANNOTATION_START);
			if(aT != null) {
				SingleWordToken swt = m_tokens.relative(1, SingleWordToken.class);
				if(swt != null) {
					AnnotationToken at = new AnnotationToken(swt);
					m_tokens.replace(aT, at);
					m_tokens.remove(swt);
				}
			}
		}
	}
	/**
	 * remove all end of line tokens that are preceded or followed by a token that no longer needs an EOL token
	 * This likely now removes all EOL tokens
	 */
	private void collapseEols() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			EolToken et = m_tokens.gotoNext(EolToken.class);
			Token t = m_tokens.relative(-1);
			if(t instanceof EolToken || t instanceof CommentToken) {
				m_tokens.remove(et);
			} else {
				IdentifierToken it = m_tokens.relative(-1, IdentifierToken.class);

				if(it != null) {
					if(it.check(TokenIdentifier.BRACE_END,
							TokenIdentifier.BRACE_START,
							TokenIdentifier.BRACKET_END,
							TokenIdentifier.BRACKET_START,
							TokenIdentifier.COLON,
							TokenIdentifier.COMMA,
							TokenIdentifier.SEMICOLON,
							TokenIdentifier.SQUARE_BRACKET_END,
							TokenIdentifier.SQUARE_BRACKET_START
					)) {
						m_tokens.remove(et);
					}
				}
			}
		}
	}
	private void coolapseSemicolons() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			IdentifierToken et = m_tokens.gotoNextId(TokenIdentifier.SEMICOLON);
			Token t = m_tokens.relative(-1);
			IdentifierToken it = m_tokens.relative(-1, IdentifierToken.class);
			if(t instanceof NameValueToken || t instanceof CommentToken || t instanceof SingleWordToken
					|| (it != null && it.check(TokenIdentifier.BRACE_END, TokenIdentifier.BRACKET_END, TokenIdentifier.SQUARE_BRACKET_END))) {
				m_tokens.remove(et);
			}
		}
	}
	/**
	 * Combine comment tokens if there are consecutive ones
	 * Block comments will not be collapsed. Consecutive line comments will collapse and become blocks.
	 * Comments separated by emptylines will not be collapsed
	 */
	private void collapseComments() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			CommentToken ct = m_tokens.gotoNext(CommentToken.class);


			if(ct == null) {
				break;
			} else if(!ct.isLineComment()) {
					//don't do anything here. We don't collapse block comments
				m_tokens.next();
			} else {
				CommentToken ct2 = m_tokens.relative(1, CommentToken.class);
				if(ct2 != null) {
					if(ct2.isLineComment()) {
						CommentToken ctn = ct.combine(ct2);
						m_tokens.replace(ct, ctn);
						m_tokens.remove(ct2);
					}
				} else {
					//check for eol
					EolToken et = m_tokens.relative(1, EolToken.class);
					if(et != null) {
						m_tokens.remove(et);
					} else {
						m_tokens.next();
					}
				}
			}
		}

	}
	/**
	 * processes the name value pairs defined for an enum
	 * They are identified as being contained in a following block of braces
	 * @throws SchemaParserException
	 */
	private void collapseEnumValues() throws SchemaParserException {
		m_tokens.resetIndex();
		while(true){
//			//first find next define element
//			EnumToken et = m_tokens.gotoNext(EnumToken.class);
//
//			if(et != null) {
//				//this should be safe because of how j was determined
//
//				int k = findToken(j, true, BraceStartToken.class);
//				int m = findToken(k, true, BraceEndToken.class);
//
//				if(k < 0) {
//					throw new SchemaParserException("Could not find a block start after enum keyword", et.getStart());
//				}
//
//				if(m < 0) {
//					throw new SchemaParserException("Could not find a matching block end after enum block start",m_tokens.get(k).getStart());
//				}
//
//				i = k;
//
//				//everything inside the block should be part of the enum
//
//				for(int x = k+1; x < m; ++x) {
//					Token t = m_tokens.get(x);
//
//					if(t instanceof NameValueToken) {
//						NameValueToken nvt = (NameValueToken)t;
//						et.addNameValue(nvt);
//					} else if(t instanceof EolToken) {
//						//don't do anything here. It will all be fine!
//					} else if(t instanceof SingleWordToken) {
//						//this is likely an enum element that has not been assigned a value
//						//should probably check that next and previous token are EOLs
//						SingleWordToken swt = (SingleWordToken)t;
//						Token nextT = m_tokens.get(x + 1);
//						Token prevT = m_tokens.get(x - 1);
//						if(nextT instanceof EolToken) {
//							if(prevT instanceof EolToken) {
//								NameValueToken nvt = new NameValueToken(swt, null, null);
//								et.addNameValue(nvt);
//							} else {
//								throw new SchemaParserException("Unexpected input while parsing enum elements", prevT.getStart());
//							}
//
//
//						} else {
//							throw new SchemaParserException("Unexpected input while parsing enum elements", nextT.getStart());
//
//						}
//					} else {
//						throw new SchemaParserException("Did not expect "+t.toString() + "in enum block!", t.getStart());
//					}
//				}
//
//				//now remove the stuff in the block
//				for(int x = m; x >= k; --x) {
//					m_tokens.remove(x);
//				}
//
//
//
//			} else {
//				break;
//			}
		}

	}

	/**
	 * Finds the next token of the specified type
	 * @param i - starting index
	 * @param forwardNotReverse
	 * @param cs
	 * @return
	 */
	private int findToken(int i, boolean forwardNotReverse, Class<?>... cs) {
		if(i < 0 || i >= m_tokens.size()) {
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
//			int eol = findToken(i, true, EolToken.class);
//			int fnti = eol;
//			if(eol >= 0) {
//				//rewind to the previous open bracket
//				int bracket = findToken(eol, false, BracketStartToken.class);
//				if(bracket > i) {//only valid if it occurs after the point that we started looking
//					fnti = bracket;
//				}
//				//rewind to the first single word token. This should be a field
//				fnti = findToken(fnti, false, SingleWordToken.class);
//				if(fnti > i) {
//
//					FieldNameToken fnt = new FieldNameToken((SingleWordToken)m_tokens.get(fnti));
//					m_tokens.set(fnti, fnt);//this will replace the swt
//				}
//				i = eol+1;
//			} else {
//				break;
//			}
		}

	}



	/**
	 * Collapse all the define tokens and the following single word tokens together
	 * @throws SchemaParserException
	 */
	private void collapseDefines() throws SchemaParserException {
		int i = 0;

		while(i < m_tokens.size()){
//			//first find next define element
//			int j = findToken(i, true, DefineToken.class);
//
//			if(j >= 0) {
//				//this should be safe because of how j was determined
//				DefineToken dt = (DefineToken)(m_tokens.get(j));
//				//now get next token
//				int k = findToken(j, true, SingleWordToken.class);
//				if(k == j + 1) {//it better be the very next token
//					SingleWordToken swt = (SingleWordToken)(m_tokens.get(k));
//					dt.setTypeName(swt.getName());
//					m_tokens.remove(k);
//				} else {
//					throw new SchemaParserException("Define keyword must be followed by a type name!", dt.getStart());
//				}
//				i = k;
//			} else {
//				break;
//			}
		}
	}
	/**
	 * Scan through all
	 */
	private void collapseBaseTypes() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			IdentifierToken it = m_tokens.gotoNext(IdentifierToken.class);
			if(it != null) {

				BaseTypeToken btt = BaseTypeToken.makeNew(it);
				if(btt == null) {
					m_tokens.next();
				} else {
					m_tokens.replace(it, btt);
				}

				m_tokens.next();
			}
		}
	}
	/**
	 * Collapse all the enum tokens and the following base type together
	 * The base type becomes the type of the enum
	 * @throws SchemaParserException
	 */
	private void collapseEnums() throws SchemaParserException {

//		m_tokens.resetIndex();
//
//		while(m_tokens.isMore()){
//			//first find next define element
//			IdentifierToken it = m_tokens.gotoNextId(TokenIdentifier.ENUM);
//
//			if(it != null) {
//
//				//replace with enum token
//				EnumToken et = new EnumToken(it);
//				m_tokens.replace(it,  et);
//				et.setBaseType(TokenIdentifier.UINT32);//this is the default
//
//
//
//				IdentifierToken it2 = m_tokens.relative(1, IdentifierToken.class);
//				BaseTypeToken btt = m_tokens.relative(2, BaseTypeToken.class);
//
//				if(it2 != null && btt != null && it2.getKeyword() == TokenIdentifier.COLON) {
//					et.setBaseType(btt.getKeyword());
//				}
//
//
//				CommentToken ct = m_tokens.relative(-1, CommentToken.class);
//
//
//				if(ct != null) {
//					m_tokens.setIndex(ct);
//					FilePathToken fpt = m_tokens.relative(-1, FilePathToken.class);
//					if(fpt == null) {
//						//we're ok to use the comment
//
//					}
//				}
//
//				FilePathToken t = m_tokens.relative(, FilePathToken.class, CommentToken.class);
//				if(t instanceof FilePathToken) {
//
//				}
//				if(tm1 instanceof CommentToken) {
//					//if this is the first comment of the file then it's probably not the comment for this enum
//					int h = findToken(j - 1, false, FilePathToken.class);
//					h = findToken(h, true, CommentToken.class);
//					if(h < j - 1) {
//						et.setComment((CommentToken)tm1);
//					} else {
//						tm1 = null;
//					}
//				}
//
//
//				//now check for a type definition
//				int k = findToken(j, true, BaseTypeToken.class);
//				if(k == j + 3) {
//					//so far so good
//					//now check for colon and base type
//					Token t1 = m_tokens.get(j + 1);//should be the name
//					Token t2 = m_tokens.get(j + 2);//should be a colon
//					Token t3 = m_tokens.get(j + 3);//should be a base type
//					if(t1 instanceof SingleWordToken && IdentifierToken.check(t2, TokenIdentifier.COLON)) {
//						et.setBaseType(((BaseTypeToken)t3).getKeyword());
//						m_tokens.remove(j + 3);
//						m_tokens.remove(j + 2);
//					}
//
//
//
//				}
//				if(tm1 != null) {
//					m_tokens.remove(j - 1);
//					--j;
//				}
//
//
//				i = j + 1;
//			} else {
//				break;
//			}
//		}
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
		//now advance to the next interesting token
		Coord result = start.advanceToNext(
				TokenIdentifier.SPACE,
				TokenIdentifier.TAB,
				TokenIdentifier.BRACE_START,
				TokenIdentifier.BRACE_END,
				TokenIdentifier.BRACKET_START,
				TokenIdentifier.BRACKET_END,
				TokenIdentifier.SQUARE_BRACKET_START,
				TokenIdentifier.SQUARE_BRACKET_END,
				TokenIdentifier.BICOLON,
				TokenIdentifier.COLON,
				TokenIdentifier.EQUALS,
				TokenIdentifier.ANNOTATION_START,
				TokenIdentifier.SEMICOLON,
				TokenIdentifier.COMMA
				);
				//' ','\t','{','}','(',')','=','@',':',';',',');
		Coord end = result;
		end = end.trimEnd();
		String s = start.fromThisToThatString(end);

		if(!s.isEmpty()) {
			addToken(start, end, s);
		}
		return result;
	}
	private void addToken(Coord start, Coord end, String s) {
		TokenIdentifier tif = null;
		for(TokenIdentifier ti : TokenIdentifier.values()) {
			if(ti.id().equals(s)) {
				tif = ti;
				break;
			}
		}
		if(tif != null) {

			m_tokens.add(new IdentifierToken(start, end, tif));
		} else {
			m_tokens.add(new SingleWordToken(start, end, s));
		}




	}
	/**
	 * find all line comments and insert the token at the start of the line they are on
	 * @param c
	 * @return
	 */
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
	/**
	 * if the next coord is a double quotation mark then process a string until the closing quote
	 * if no valid closing quote is found then consume the remainder of the whole input
	 * @param c
	 * @return
	 */
	private Coord processStrings(Coord c) {
		if(c == null) {
			return null;
		}

		Coord result = c.trim();

		if(result.startsWith(STRING_DELIMITER)) {
			Coord start = result.incrementIndex(1);//we don't want to point to the quotation mark
			Coord end = start;
			boolean notDone = true;
			while(notDone) {
				Coord r = result.findNext(STRING_DELIMITER, STRING_ESCAPE_DELIMITER);
				if(r == null) {
					System.out.println("BlueberrySchemaParser.processStrings");
				}
				result = r;
				if(result.isAtEnd()) {
					notDone = false;
					end = result;
				} else if(result.startsWith(STRING_DELIMITER)) {
					end = result;
					notDone = false;
					result = result.incrementIndex(1);

				} else {
					//this must be a escape character then the next character might be a quotation so skip it
					result = result.incrementIndex(1);
				}

			}
			m_tokens.add(new StringToken(start, end));
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




	public StructField getTopLevelField() {
		return m_topLevelField;
	}

}

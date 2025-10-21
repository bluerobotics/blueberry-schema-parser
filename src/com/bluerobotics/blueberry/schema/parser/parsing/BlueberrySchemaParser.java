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
import java.util.ListIterator;

import com.bluerobotics.blueberry.schema.parser.constants.Constant;
import com.bluerobotics.blueberry.schema.parser.constants.Number;
import com.bluerobotics.blueberry.schema.parser.constants.NumberConstant;
import com.bluerobotics.blueberry.schema.parser.constants.StringConstant;
import com.bluerobotics.blueberry.schema.parser.fields.ArrayField;
import com.bluerobotics.blueberry.schema.parser.fields.BaseField;
import com.bluerobotics.blueberry.schema.parser.fields.BlueberryFieldPacker;
import com.bluerobotics.blueberry.schema.parser.fields.DeferredField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField.NameValue;
import com.bluerobotics.blueberry.schema.parser.fields.Field;
import com.bluerobotics.blueberry.schema.parser.fields.FieldList;
import com.bluerobotics.blueberry.schema.parser.fields.MessageField;
import com.bluerobotics.blueberry.schema.parser.fields.ParentField;
import com.bluerobotics.blueberry.schema.parser.fields.SequenceField;
import com.bluerobotics.blueberry.schema.parser.fields.StructField;
import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;
import com.bluerobotics.blueberry.schema.parser.fields.TypeDefField;
import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;
import com.bluerobotics.blueberry.schema.parser.tokens.BaseTypeToken;
import com.bluerobotics.blueberry.schema.parser.tokens.CommentToken;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.tokens.EolToken;
import com.bluerobotics.blueberry.schema.parser.tokens.FilePathToken;
import com.bluerobotics.blueberry.schema.parser.tokens.IdentifierToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NameValueToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NumberToken;
import com.bluerobotics.blueberry.schema.parser.tokens.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.tokens.SingleWordToken;
import com.bluerobotics.blueberry.schema.parser.tokens.StringToken;
import com.bluerobotics.blueberry.schema.parser.tokens.SymbolNameToken;
import com.bluerobotics.blueberry.schema.parser.tokens.Token;
import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants;
import com.bluerobotics.blueberry.schema.parser.tokens.TokenList;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;

/**
 * This class implements the token parsing algorithm
 * This reads the schema file, tokenizes and then builds a data structure of fields to represent the schema
 */
public class BlueberrySchemaParser implements Constants, TokenConstants {




	private final TokenList m_tokens = new TokenList();
	private final FieldList m_defines = new FieldList();
	private final ArrayList<Constant<?>> m_constants = new ArrayList<>();
	private final FieldList m_messages = new FieldList();
	private final ArrayList<SymbolName> m_imports = new ArrayList<>();
	private final ArrayList<Annotation> m_annotations = new ArrayList<>();

	
	public void clear() {
		m_tokens.clear();
		m_defines.clear();
		m_messages.clear();
		m_imports.clear();
		m_annotations.clear();
		m_constants.clear();
		m_fileName = null;
		m_lastComment = null;
		m_module = null;
		m_moduleEnd = null;

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
//				c = c.trim();
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
			
			collapseNumbers();
			collapseSymbolNames();
			collapseBaseTypes();
			collapseScope();
			collapseWhiteSpace();
			collapseUnsigned();
			collapseComments();
			

			collapseTypedefs();
			collapseEols();
			collapseNameValues();
			
			collapseSemicolons();
			
			m_tokens.matchBrackets(null);



//
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
			fillInMissingEnumValues();
			checkForDuplicateEnumValues();
			fillInMissingMessageKeyValues();

			
			processDeferredFields(m_defines, m_defines);	
			processDeferredFields(m_messages, m_defines);
			
			applyDeferredParameters(m_defines);
			applyDeferredParameters(m_messages);
			
			computeIndeces();

//			extractEnums();
//			extractTypedefs();
//			extractStructs();

		} catch (SchemaParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("************* Defines ***************");
		m_defines.forEach(f -> {
			
			System.out.println(f.toString());
		});


		System.out.println("BlueberrySchemaParser.parse done.");

	}
	/**
	 * Calculates the field indeces for messages, based on the word packing rules
	 */
	private void computeIndeces() {
		
		m_messages.forEachOfType(MessageField.class, false, mf -> {
			
			boolean doCdr = false;
			Annotation a = mf.getAnnotation(Annotation.SERIALIZATION_ANNOTATION);
			if(a != null) {
				Object s = a.getParameter(0, Object.class);
				if(s.toString().equals("CDR")) {
					doCdr = true;
					throw new SchemaParserException("CDR serialization not supported yet", null);
				}
			}
				
			BlueberryFieldPacker p = new BlueberryFieldPacker();
			p.pack(mf);
		});
		
	}
	
	private void fillInMissingMessageKeyValues() {
		m_messages.forEachOfType(MessageField.class, false, mf -> {
			Annotation a = mf.getAnnotation(Annotation.MESSAGE_KEY_ANNOTATION);
			if(a == null) {
				 a = new Annotation(Annotation.MESSAGE_KEY_ANNOTATION);
				a.addParameter(new Number(getNextMessageKey()));
				mf.addAnnotation(a);
				
			}
		});
	}
	/**
	 * picks the next available message
	 * @return
	 */
	private long getNextMessageKey() {
		final ArrayList<Integer> keys = new ArrayList<>();
		m_messages.forEachOfType(MessageField.class, false, mf -> {
			Annotation a = mf.getAnnotation(Annotation.MESSAGE_KEY_ANNOTATION);
			if(a != null) {
				Number an = a.getParameter(0, Number.class);
				if(an != null) {
					keys.add(an.asInt());
				}
			}
			
		});
		Collections.sort(keys);
		int i = 0;
		if(keys.size() > 0) {
			i = keys.get(0);
		}
		//find the first value that does not exist
		boolean done = false;
		while(!done) {
			done = true;
			for(Integer k : keys) {
				if(i == k) {
					++i;
					done = false;
					break;
				}
			}
		}
		
		return i;
	}
	private void collapseSymbolNames() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			SingleWordToken swt = m_tokens.relative(0, SingleWordToken.class);
			if(swt != null) {
				SymbolName sn = SymbolName.guess(swt.getName());
				SymbolNameToken snt = new SymbolNameToken(swt.getStart(), swt.getEnd(), sn);
				m_tokens.replace(swt, snt);
			}
			m_tokens.next();
		}
	}
	/**
	 * remove all white space tokens except if they precede or follow a scope separator
	 */
	private void collapseWhiteSpace() {
		//remove all spaces followed by tokens that should not be a problem
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			IdentifierToken w = m_tokens.relativeId(0,TokenIdentifier.SPACE);
			if(w != null) {
				IdentifierToken tNext = m_tokens.relativeId(1, TokenIdentifier.SCOPE_SEPARATOR);
				IdentifierToken tPrev = m_tokens.relativeId(-1, TokenIdentifier.SCOPE_SEPARATOR);
				if(tNext != null || tPrev != null) {
					m_tokens.next();
				} else {
					m_tokens.remove(w);
				}
			} else {
				m_tokens.next();
			}
		}
	}
	/**
	 * scan through tokens looking for a scope separator beside a single word token
	 * @throws SchemaParserException 
	 */
	private void collapseScope() throws SchemaParserException {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			IdentifierToken s = m_tokens.relativeId(0,TokenIdentifier.SCOPE_SEPARATOR);
			if(s != null) {
				SymbolNameToken tName = m_tokens.relative(1, SymbolNameToken.class);
				SymbolNameToken tScope = m_tokens.relative(-1, SymbolNameToken.class);
				if(tScope != null && tName != null) {
					//this should be a scoped name
					SymbolNameToken swt = new SymbolNameToken(tScope.getStart(), tName.getEnd(), tScope.getSymbolName().append(TokenIdentifier.SCOPE_SEPARATOR.id()).append(tName.getSymbolName()));
					m_tokens.replace(tScope, swt);
					m_tokens.remove(s);
					m_tokens.remove(tName);
				} else if(tScope == null && tName != null){
					SymbolNameToken swt = new SymbolNameToken(s.getStart(), tName.getEnd(), SymbolName.guess(TokenIdentifier.SCOPE_SEPARATOR.id()).append(tName.getSymbolName()));
					m_tokens.replace(s, swt);
					m_tokens.remove(tName);

				} else if(tName == null) {
					throw new SchemaParserException("There does not seem to be a name with this Scope separator.", s.getEnd());
				}
			} else {
				m_tokens.next();
			}
		}
	}
	/**
	 * check all annotations of all 
	 */
	private void applyDeferredParameters(FieldList fs) {
		fs.forEach(f -> {
			f.scanAnnotations(a -> {
				a.replaceDeferredParameters(fn -> {
					Object result = null;
					for(Constant<?> c : m_constants) {
						if(c.getName().equals(fn)) {
							result = c.getValue();
							break;
						}
					}
					return result;
				});
			});
		});
	}

	/**
	 * method that returns a replacement field for a deferred field
	 * @param m_defines2
	 * @param m_defines3
	 * @throws SchemaParserException 
	 */
	private <T extends Field> void processDeferredFields(FieldList fs, FieldList defines) throws SchemaParserException {
		for(int i = 0; i < fs.size(); ++i) {
			Field f = fs.get(i);
			if(f instanceof DeferredField) {
				List<SymbolName> imports = ((DeferredField) f).getImports();
				SymbolName typeName = f.getTypeName();
				Field dft = null;
				
			
				for(Field df : defines.getList()) {
					if(df.getTypeName().isMatchWithScope(TokenIdentifier.SCOPE_SEPARATOR.id(), imports, typeName)) {
						if(dft != null) {
							throw new SchemaParserException("Ambiguous field type: "+typeName.toUpperCamel(), null);
						} else {
							dft = df;
						}	
					}
				}
				if(dft == null) {
					throw new SchemaParserException("Could not find a type definition for "+typeName.toUpperCamel(), null);
				}
				fs.set(i, dft.makeInstance(f.getName()));
			} else if(f instanceof ParentField) {
				processDeferredFields(((ParentField)f).getChildren(), defines);
			}
		}
		
	}
	/**
	 * typedef keyword is not required before enum, sequence and struct keywords so remove it
	 */
	private void collapseTypedefs() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			IdentifierToken typedef = m_tokens.gotoNextId(TokenIdentifier.TYPEDEF);
			if(typedef != null) {
				IdentifierToken id = m_tokens.relative(1, IdentifierToken.class);
				if(id != null) {
					switch(id.getKeyword()) {
					case ENUM:
					case STRUCT:
					case SEQUENCE:
						m_tokens.next();
						m_tokens.remove(typedef);
						break;
					default:
						m_tokens.next();
						break;
					}
				} else {
					m_tokens.next();
				}
			}

		}
	}
	/**
	 * replace integer types preceded with the unsigned keyword with the appropriate unsigned type
	 * @throws SchemaParserException
	 */
	private void collapseUnsigned() throws SchemaParserException {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			BaseTypeToken it = m_tokens.gotoNext(BaseTypeToken.class);
			if(it != null && it.getKeyword() == TokenIdentifier.UNSIGNED) {
				BaseTypeToken btt = m_tokens.relative(1, BaseTypeToken.class);
				TokenIdentifier newTi = null;
				if(btt == null) {
					throw new SchemaParserException("Unsigned keyword must be followed by a base type.", it.getEnd());
				} else {
					switch(btt.getKeyword()) {
					case BYTE:
					case INT8:
						newTi = TokenIdentifier.UINT8;
						break;
					case SHORT:
					case INT16:
						newTi = TokenIdentifier.UINT16;
						break;
					case LONG:
					case INT:
					case INT32:
						newTi = TokenIdentifier.UINT32;
						break;
					case INT64:
						newTi = TokenIdentifier.UINT64;
						break;
					default:
						throw new SchemaParserException("Unsigned keyword makes no sense combined with "+btt.getName(), btt.getStart());
					}
					BaseTypeToken newBtt = new BaseTypeToken(it.getStart(), btt.getEnd(), newTi);
					m_tokens.replace(it, newBtt);
					m_tokens.remove(btt);
				}
			}
			m_tokens.next();
		}

	}
	private SymbolName m_module = null;
	private Token m_moduleEnd = null;
	private String m_fileName = null;//indicates the filename that the present tokens are from
	private String m_lastComment = null;

	private void assembleFields() throws SchemaParserException {
		m_module = null;
		m_moduleEnd = null;
		m_lastComment = null;
		m_tokens.resetIndex();

		while(m_tokens.isMore()) {
			Token t = m_tokens.getCurrent();
			//check if we've hit the closing brace of the current module - if there is one
			if(!m_tokens.isCurrentBefore(m_moduleEnd)) {
				m_module = null;
				m_moduleEnd = null;
			}
			IdentifierToken it = m_tokens.relative(0,IdentifierToken.class);
			if(it != null) {
				switch(it.getKeyword()) {
				case CONST:
					processConst(it);
					break;
				case ENUM:
					processEnum(it);
					break;
				case IMPORT:
					processImport(it);
					break;
				case MESSAGE:
					processMessage(it);
					break;
				case MODULE:
					processModule(it);
					break;
				case SEQUENCE:
					processSequence(it);
					break;
				case STRING:
					processString(it);
					break;
				case STRUCT:
					processStructs(it);
					break;
				case TYPEDEF:
					processTypedef(it);
					break;
				case ANNOTATION_START:
					processAnnotation(it);
					break;
				default:
					System.out.println("Did not process "+it);
					break;
				}


			} else if(t instanceof FilePathToken) {
				FilePathToken fpt = (FilePathToken)t;
				m_fileName = fpt.getString();
				m_imports.clear();
			} else if(t instanceof CommentToken) {
				CommentToken ct = (CommentToken)t;
				m_lastComment = ct.combineLines();
			}
			m_tokens.next();

		}
	}
	
	private void processImport(IdentifierToken it) {
		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
		if(nameToken == null) {
			throw new SchemaParserException("Import statement does not have a name specified", null);
		}
		m_imports.add(SymbolName.guess(nameToken.getName()));
	}
	private void processMessage(IdentifierToken it) throws SchemaParserException {
		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
		IdentifierToken braceStart = m_tokens.relativeId(2, TokenIdentifier.BRACE_START);
		IdentifierToken braceEnd = m_tokens.matchBrackets(braceStart);
		if(nameToken == null) {
			throw new SchemaParserException("Message has no specified name.", it.getEnd());
		} else if(braceStart == null) {
			throw new SchemaParserException("Message stament has no opening brace.", it.getEnd());
		}
		SymbolName name = nameToken.getSymbolName().addScope(TokenIdentifier.SCOPE_SEPARATOR.id(), m_module);


		MessageField m = new MessageField(SymbolName.EMPTY, name, m_lastComment);
		m.setFileName(m_fileName);
		m.setNamespace(m_module);
		m_lastComment = null;

		m_messages.add(m);
		m.addAnnotation(m_annotations);
		m_annotations.clear();
		m_tokens.setIndex(braceStart);
		m_tokens.next();
		while(m_tokens.isCurrentBefore(braceEnd)) {
			if(m_tokens.getCurrent() instanceof CommentToken) {
				m_tokens.next();
			}
			if(m_tokens.getCurrent() instanceof EolToken) {
				m_tokens.next();
			} else {
			
				//reuse a bunch of fields from above for the sub-field
				CommentToken ct = m_tokens.relative(-1, CommentToken.class);
				String comment = ct != null ? ct.combineLines() : null;
				SymbolNameToken typeNameToken = m_tokens.relative(0, SymbolNameToken.class);//or this
				BaseTypeToken btt = m_tokens.relative(0, BaseTypeToken.class);//or this
				nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
				if(nameToken == null) {
					throw new SchemaParserException("No name specified for field", m_tokens.getCurrent().getEnd());
				}
				if(btt != null) {
					//add a base type field
					TypeId tid = lookupBaseType(btt.getKeyword());
					m.add(new BaseField(nameToken.getSymbolName(), tid, comment));
	
				} else if(typeNameToken != null) {
					DeferredField df = new DeferredField(nameToken.getSymbolName(), typeNameToken.getSymbolName(), m_imports, comment);
					df.addImport(m_module);
					m.add(df);
					
				}
				m_tokens.setIndex(nameToken);
				m_tokens.next();
			}
		}
	}
	/**
	 * of the form <comment?><sequence><angle bracket start><constituentTypeName><comma?><number?><angle bracket end><sequenceTypeName>
	 * @param it
	 * @throws SchemaParserException
	 */
	private void processSequence(IdentifierToken it) throws SchemaParserException {
		IdentifierToken angleBracketStart = m_tokens.relativeId(1, TokenIdentifier.BRACE_START);
		if(angleBracketStart == null) {
			throw new SchemaParserException("Sequence keyword should be followed by an open angle bracket.", it.getEnd());
		}
		BaseTypeToken btt = m_tokens.relative(2, BaseTypeToken.class);
		SymbolNameToken snt = m_tokens.relative(2, SymbolNameToken.class);
		Field cf = null;
		if(btt != null) {
			TypeId tid = lookupBaseType(btt.getKeyword());
			cf = new BaseField(null, tid, null);
		} else if(snt != null) {
			cf = new DeferredField(null, snt.getSymbolName(), m_imports, null);
		} else {
			throw new SchemaParserException("Sequence must be defined with a type for its elements.", it.getEnd());
		}
		IdentifierToken commaT = m_tokens.relativeId(3, TokenIdentifier.COMMA);
		NumberToken nt = m_tokens.relative(4, NumberToken.class);
		int n = -1;
		if(commaT != null && nt != null) {
			n = nt.getNumber().asInt();
		}
		IdentifierToken angleBracketEnd = m_tokens.matchBrackets(angleBracketStart);
		if(angleBracketEnd == null) {
			throw new SchemaParserException("Starting angle brackets must have closing bracket too.", angleBracketStart.getEnd());
		}
		
		m_tokens.setIndex(angleBracketEnd);
		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);
		
		SequenceField sf = new SequenceField(null, nameToken.getSymbolName(), m_lastComment);
		sf.setFileName(m_fileName);
		sf.setNamespace(m_module);
		sf.add(cf);
		m_lastComment = null;
		
	}
	
	private void processString(IdentifierToken it) {
		// TODO Auto-generated method stub
		
	}

	private void processStructs(IdentifierToken it) throws SchemaParserException {

		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
		IdentifierToken braceStart = m_tokens.relativeId(2, TokenIdentifier.BRACE_START);
		IdentifierToken braceEnd = m_tokens.matchBrackets(braceStart);
		if(nameToken == null) {
			throw new SchemaParserException("Struct has no specified name.", it.getEnd());
		} else if(braceStart == null) {
			throw new SchemaParserException("Struct has no opening brace.", it.getEnd());
		}
		SymbolName name = nameToken.getSymbolName().addScope(TokenIdentifier.SCOPE_SEPARATOR.id(), m_module);


		StructField m = new StructField(SymbolName.EMPTY, name, m_lastComment);
		m.setFileName(m_fileName);
		m.setNamespace(m_module);
		m_lastComment = null;

		m_defines.add(m);
		m.addAnnotation(m_annotations);
		m_annotations.clear();
		m_tokens.setIndex(braceStart);

		while(m_tokens.isCurrentBefore(braceEnd)) {
			Token t = m_tokens.gotoNextOfThese(braceEnd, SymbolNameToken.class, BaseTypeToken.class);
			if(t == null || t == braceEnd) {
				break;
			}
			
			//reuse a bunch of fields from above for the sub-field
			CommentToken ct = m_tokens.relative(-1, CommentToken.class);
			String comment = ct != null ? ct.combineLines() : null;
			SymbolNameToken typeNameToken = m_tokens.relative(0, SymbolNameToken.class);//or this
			BaseTypeToken btt = m_tokens.relative(0, BaseTypeToken.class);//or this
			nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
			if(nameToken == null) {
				throw new SchemaParserException("No name specified for field", m_tokens.getCurrent().getEnd());
			}
			name = nameToken.getSymbolName();
			if(btt != null) {
				//add a base type field
				TypeId tid = lookupBaseType(btt.getKeyword());
				m.add(new BaseField(name, tid, comment));

			} else if(typeNameToken != null) {
				DeferredField df = new DeferredField(name, typeNameToken.getSymbolName(), m_imports,comment);
				df.addImport(m_module);
				m.add(df);
			}
			m_tokens.setIndex(nameToken);
			m_tokens.next();

		}
	}
	/**
	 * of the form <comment?><typedef><constituentTypeName><typeName><square bracket start?><number?><square bracket end>
	 * @param it
	 * @throws SchemaParserException
	 */
	private void processTypedef(IdentifierToken it) throws SchemaParserException {

		SymbolNameToken name = m_tokens.relative(1, SymbolNameToken.class);//or this
		BaseTypeToken btt = m_tokens.relative(2, BaseTypeToken.class);//either this
		SymbolNameToken typeName = m_tokens.relative(2, SymbolNameToken.class);//name of new type
		IdentifierToken squareBracketStart = m_tokens.relativeId(3, TokenIdentifier.SQUARE_BRACKET_START);
		
		
		if(name == null) {
			throw new SchemaParserException("No type name specified for this typedef.",it.getEnd());
		} else if(btt == null && typeName == null) {
			throw new SchemaParserException("No type specified for this typedef.",it.getEnd());
		}
		SymbolName fn = name.getSymbolName().addScope(TokenIdentifier.SCOPE_SEPARATOR.id(), m_module);
		
		TypeId id = (btt != null) ? lookupBaseType(btt.getKeyword()) : TypeId.DEFERRED;

		if(squareBracketStart == null) {
		
			//this is a normal base type

			TypeDefField tdf = new TypeDefField(SymbolName.EMPTY, fn, id, m_lastComment);
			tdf.setFileName(m_fileName);
			tdf.setNamespace(m_module);
			m_defines.add(tdf);
			m_tokens.setIndex(btt != null ? btt : typeName );


		} else {
			//this is an array type
			NumberToken arraySize = m_tokens.relative(4, NumberToken.class);
			SymbolNameToken arraySizeConst = m_tokens.relative(4,SymbolNameToken.class);
			IdentifierToken squareBracketEnd = m_tokens.relativeId(5, TokenIdentifier.SQUARE_BRACKET_END);
			//this is an array
			if(squareBracketEnd == null) {
				throw new SchemaParserException("No closing square-bracket found.",squareBracketStart.getEnd());
			} else if(arraySize == null && arraySizeConst == null) {
				throw new SchemaParserException("No valid array size specified.",squareBracketStart.getEnd());
			}
			//FieldName name, FieldName typeName, TypeId typeId, int number, String comment

			int n = arraySize != null ? arraySize.getNumber().asInt() : lookupConstInt(SymbolName.fromSnake(arraySizeConst.getName()));
			ArrayField af = new ArrayField(SymbolName.EMPTY, fn, id, n, m_lastComment);
			af.setFileName(m_fileName);
			af.setNamespace(m_module);
			m_defines.add(af);
			m_tokens.setIndex(squareBracketEnd);


		}

		m_lastComment = null;


	}
	private int lookupConstInt(SymbolName name) {
		NumberConstant result = null;
		for(Constant<?> c : m_constants) {
			if(c instanceof NumberConstant) {
				NumberConstant nc = (NumberConstant)c;
				if(nc.getName().equals(name)) {
					result = nc;
					break;
				}
			}
		}
		return result != null ? result.getValue().asInt() : -1;
	}
	private void processEnum(IdentifierToken enumT) throws SchemaParserException {


		SymbolNameToken name = m_tokens.relative(1, SymbolNameToken.class);
		IdentifierToken colon = m_tokens.relativeId(2, TokenIdentifier.COLON);
		BaseTypeToken btt = m_tokens.relative(3, BaseTypeToken.class);
		IdentifierToken braceStart = m_tokens.relativeId(colon == null ? 2 : 4, TokenIdentifier.BRACE_START);
		IdentifierToken braceEnd = m_tokens.matchBrackets(braceStart);
		if(name == null) {
			throw new SchemaParserException("Enum name not specified.", enumT.getEnd());
		} else if(braceStart == null){
			throw new SchemaParserException("Enum needs a body with members.", enumT.getEnd());
		} else if(braceEnd == null) {
			throw new SchemaParserException("Enum body has no closing brace.", enumT.getEnd());
		}

		TypeId bt = (btt != null) ? lookupBaseType(btt.getKeyword()) : TypeId.UINT32;
		SymbolName fn = name.getSymbolName().addScope(TokenIdentifier.SCOPE_SEPARATOR.id(), m_module);
		EnumField et = new EnumField(SymbolName.EMPTY, fn, bt, m_lastComment);
		et.setFileName(m_fileName);
		et.setNamespace(m_module);
		m_lastComment = null;
		m_defines.add(et);

		m_tokens.setIndex(braceStart);

		while(m_tokens.isCurrentBefore(braceEnd)) {
			Token t = m_tokens.gotoNextOfThese(braceEnd, SymbolNameToken.class, NameValueToken.class, braceEnd.getClass());
			if(t == null || t == braceEnd) {
				break;
			}
			//reuse a bunch of fields from above for the sub-field
			CommentToken ct = m_tokens.relative(-1, CommentToken.class);
			String comment = ct != null ? ct.combineLines() : null;
			SymbolNameToken nameToken = m_tokens.relative(0, SymbolNameToken.class);
			NameValueToken nameValueToken = m_tokens.relative(0, NameValueToken.class);
			if(nameToken != null) {
				et.addNameValue(nameToken.getSymbolName(), Number.NAN, comment);
			} else if(nameValueToken != null) {
				try {
					et.addNameValue(nameValueToken.getSymbolName(), nameValueToken.getValue(), comment);
				} catch(RuntimeException e) {
					System.out.println("BlueberrySchemaParser.processEnum ");
				}
				
			}



			m_tokens.setIndex(m_tokens.relative(1));

		}

	}
	/**
	 *
	 * @param it
	 * @throws SchemaParserException
	 */
	private void processConst(IdentifierToken it) throws SchemaParserException {
		CommentToken ct = m_tokens.relative(-1, CommentToken.class);
		String comment = ct != null ? ct.combineLines() : null;

		BaseTypeToken btt = m_tokens.relative(1, BaseTypeToken.class);
		IdentifierToken stringType = m_tokens.relativeId(1,  TokenIdentifier.STRING);
		NameValueToken nvt = m_tokens.relative(2, NameValueToken.class);
		SymbolNameToken name = m_tokens.relative(2, SymbolNameToken.class);
		IdentifierToken equals = m_tokens.relativeId(3, TokenIdentifier.EQUALS);
		StringToken string = m_tokens.relative(4, StringToken.class);


		if(btt != null) {

			if(nvt == null) {
				throw new SchemaParserException("Const must include a name and value.", it.getEnd());
			}

			TypeId typeId = lookupBaseType(btt.getKeyword());

			SymbolName fn = nvt.getSymbolName().addScope(TokenIdentifier.SCOPE_SEPARATOR.id(), m_module);

			NumberConstant c = new NumberConstant(typeId, fn, nvt.getValue(), comment);
			c.setFileName(m_fileName);
			c.setNamespace(m_module);
			m_constants.add(c);
			m_tokens.setIndex(nvt);
		} else if(stringType != null) {
			if(equals == null) {
				throw new SchemaParserException("Const must include an equals symbol", it.getEnd());
			}
			SymbolName fn = SymbolName.guess(name.getName()).prepend(m_module);
			StringConstant c = new StringConstant(fn, string.getString(), comment);
			c.setFileName(m_fileName);
			c.setNamespace(m_module);
			m_constants.add(c);
			m_tokens.setIndex(string);
		} else {
			throw new SchemaParserException("Only base types and Strings can be declared const.", it.getEnd());

		}




	}
	private void processModule(IdentifierToken it) throws SchemaParserException {
		SymbolNameToken moduleName = m_tokens.relative(1, SymbolNameToken.class);
		IdentifierToken braceStart = m_tokens.relativeId(2, TokenIdentifier.BRACE_START);
		if(braceStart == null) {
			throw new SchemaParserException("Module should start with opening brace", it.getEnd());
		} else if(moduleName == null){
			throw new SchemaParserException("Module name is ill-formed.",it.getEnd());
		} else {
			IdentifierToken braceEnd = m_tokens.matchBrackets(braceStart);//this should never be null I think
			m_module = SymbolName.fromCamel(moduleName.getName());

			//TODO: what is the best way to separate the module name from the rest?
			m_moduleEnd = braceEnd;
			m_tokens.setIndex(braceStart);

		}

	}









	/**
	 * Scans enum tokens and checks for duplicate values
	 * @throws SchemaParserException
	 */
	private void checkForDuplicateEnumValues() throws SchemaParserException {
		m_defines.forEachOfType(EnumField.class, true, ef -> {
			for(NameValue nv1 : ef.getNameValues()) {
				for(NameValue nv2 : ef.getNameValues()) {
					if(nv1 == nv2) {
						//don't do anything, they're the same item
					} else if(nv1.getName().equals(nv2.getName())){
						//they have the same name
						throw new SchemaParserException("Duplicate enum item names: "+nv1.getName().toUpperSnake(), null);
					} else if(nv1.getValue().equals(nv2.getValue())) {
						//they have the same value
						throw new SchemaParserException("Duplicate enum item values: "+nv1.getName().toUpperSnake()+" = "+nv1.getValue(), null);
					}
				}
			}
		});
	}
	/**
	 * Scans for enum tokens that are missing values for their elements.
	 * Fills them in with the smallest, unused, positive, integer value.
	 */
	private void fillInMissingEnumValues() {
		m_defines.forEachOfType(EnumField.class, true, ef -> {
			ef.fillInMissingValues();
		});
	}
	/**
	 * constructs a hierarchy of fields to represent all the packets
	 * This applies all defined types
	 * @param t
	 * @param parentField
	 * @throws SchemaParserException
	 */
//	private void buildPackets(FieldAllocationToken t, ParentField parentField) throws SchemaParserException {
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
//	}



	/**
	 * converts from a token base type to a field type
	 * @param bt
	 * @return
	 */
	TypeId lookupBaseType(TokenIdentifier bt) {
		TypeId result = null;
		switch(bt) {
		case BOOLEAN:
			result = TypeId.BOOL;
			break;
		case FLOAT:
			result = TypeId.FLOAT32;
			break;
		case DOUBLE:
			result = TypeId.FLOAT64;
			break;
		case INT16:
			result = TypeId.INT16;
			break;
		case INT32:
			result = TypeId.INT32;
			break;
		case INT8:
			result = TypeId.INT8;
			break;
		case UINT16:
			result = TypeId.UINT16;
			break;
		case UINT32:
			result = TypeId.UINT32;
			break;
		case UINT8:
			result = TypeId.UINT8;
			break;
		case UINT64:
			result = TypeId.UINT64;
			break;
		case INT64:
			result = TypeId.INT64;
			break;
		default:
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
	 * now collapse any token sequences of form CommentToken, SymbolNameToken, IdentifierToken (equals sign), NumberToken to a NameValueToken
	 * @throws SchemaParserException
	 */
	private void collapseNameValues() throws SchemaParserException {

		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			Token equalsT = m_tokens.gotoNextId(TokenIdentifier.EQUALS);

			if(equalsT != null) {
				CommentToken commentT = m_tokens.relative(-2, CommentToken.class);
				SymbolNameToken nameT = m_tokens.relative(-1, SymbolNameToken.class);
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
	 * @throws SchemaParserException
	 */
	private void processAnnotation(IdentifierToken at) throws SchemaParserException {

		if(at != null) {
			SymbolNameToken swt = m_tokens.relative(1, SymbolNameToken.class);
			IdentifierToken bracketStart = m_tokens.relativeId(2, TokenIdentifier.BRACKET_START);
			IdentifierToken bracketEnd = m_tokens.matchBrackets(bracketStart);

			if(swt == null) {
				throw new SchemaParserException("Annotation symbol should be followed by a name", at.getEnd());
			} else if(bracketStart == null) {
				throw new SchemaParserException("Annotation should be followed by an opening bracket.", swt.getEnd());
			} else if(bracketEnd == null) {
				//this should never happen because the match bracket will throw its own exception
			}
			
			Annotation a = new Annotation(swt.getSymbolName());
			
			m_tokens.setIndex(bracketStart);
			m_tokens.next();

			while(m_tokens.isCurrentBefore( bracketEnd)) {
				Token t = m_tokens.getCurrent();
				if(t instanceof StringToken) {
					
					a.addParameter(t.getName());
				} else if(t instanceof SymbolNameToken) {
					//this is likely a constant
					a.addDeferredParameter(((SymbolNameToken)t).getSymbolName(), m_imports.toArray(new SymbolName[m_imports.size()]));
				} else if(t instanceof NumberToken) {
					NumberToken nt = (NumberToken)t;
					a.addParameter(nt.getNumber());
				} else if(t instanceof CommentToken) {
					//ignore comments
				} else if(t instanceof IdentifierToken) {
					IdentifierToken comma = (IdentifierToken)t;
					if(comma.getKeyword() != TokenIdentifier.COMMA){
						throw new SchemaParserException("Parameters should e comma separated "+t, t.getStart());
					}
				} else {
					throw new SchemaParserException("This doesn't seem to be a valid annotation parameter "+t, t.getStart());
				}
				m_tokens.next();

			}
			if(a.getName().equals(Annotation.FILE_PATH_ANNOTATION)) {
				String fn = a.getParameter(0, String.class);
				if(fn == null) {
					throw new SchemaParserException("File path annotation should have a string parameter", null);
				} else {
					m_fileName = fn;
				}

			} else {
				m_annotations.add(a);
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
							TokenIdentifier.COMMA,
							TokenIdentifier.SQUARE_BRACKET_END,
							TokenIdentifier.SQUARE_BRACKET_START
					)) {
						m_tokens.remove(et);
					} else {
						m_tokens.next();
					}
				} else {
					m_tokens.next();
				}
			}
		}
	}
	private void collapseSemicolons() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			IdentifierToken et = m_tokens.gotoNextId(TokenIdentifier.SEMICOLON);
			Token tPrev = m_tokens.relative(-1);
			IdentifierToken itPrev = m_tokens.relative(-1, IdentifierToken.class);
			Token tNext = m_tokens.relative(1);
			IdentifierToken itNext = m_tokens.relative(1, IdentifierToken.class);
			if(tPrev instanceof NameValueToken || tPrev instanceof CommentToken || tPrev instanceof FilePathToken || tPrev instanceof EolToken
					|| (itPrev != null && itPrev.check(TokenIdentifier.BRACE_END, TokenIdentifier.BRACKET_END, TokenIdentifier.SQUARE_BRACKET_END))) {
				m_tokens.remove(et);
			} else if(tNext instanceof NameValueToken || tNext instanceof CommentToken || tNext instanceof FilePathToken || tNext instanceof EolToken
					|| (itNext != null && itNext.check(TokenIdentifier.BRACE_END, TokenIdentifier.BRACKET_END, TokenIdentifier.SQUARE_BRACKET_END))) {
				m_tokens.remove(et);
			} else {
				m_tokens.next();
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
		Coord start = c;//c.trim();
		//now advance to the next interesting token
		Coord result = start.advanceToNext( //the following elements are sensitive to order. For example the scope separator must be tested before the colon
				TokenIdentifier.SPACE,
				TokenIdentifier.TAB,
				TokenIdentifier.BRACE_START,
				TokenIdentifier.BRACE_END,
				TokenIdentifier.BRACKET_START,
				TokenIdentifier.BRACKET_END,
				TokenIdentifier.SQUARE_BRACKET_START,
				TokenIdentifier.SQUARE_BRACKET_END,
				TokenIdentifier.SCOPE_SEPARATOR,
				TokenIdentifier.COLON,
				TokenIdentifier.EQUALS,
				TokenIdentifier.ANNOTATION_START,
				TokenIdentifier.SEMICOLON,
				TokenIdentifier.COMMA
				);
		Coord end = result;
		
		
		String s = start.fromThisToThatString(end);
		if(s.isBlank()) {
			//this should allow whitespace to be tokenized
		} else {
			end = end.trimEnd();
			s = start.fromThisToThatString(end);
		}
		
		
		addToken(start, end, s);
		
		return result;
	}
	private void addToken(Coord start, Coord end, String s) {
		TokenIdentifier tif = null;
		if(s.isEmpty()) {
			return;
		} else if(s.isBlank()) {
			m_tokens.add(new IdentifierToken(start, end, TokenIdentifier.SPACE));
		} else {
			for(TokenIdentifier ti : TokenIdentifier.values()) {
				if(ti.id().equals(s)) {
					tif = ti;
					break;
				}
			}
			if(tif != null) {
	
				m_tokens.add(new IdentifierToken(start, end, tif));
			} else {
				m_tokens.add(new SingleWordToken(start, end));
			}
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
		Coord result = c;//c.trim();
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

		Coord result = c;

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
		if(result.startsWith(COMMENT_BLOCK_START)) {

//			result = result.indexOf(COMMENT_BLOCK_START);
			result = result.incrementIndex(COMMENT_BLOCK_START);//move to the end of the block comment
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

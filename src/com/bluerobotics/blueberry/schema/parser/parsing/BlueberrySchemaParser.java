/*
Copyright (c) 2024  Blue Robotics North Inc.

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
import java.util.Iterator;
import java.util.ListIterator;

import com.bluerobotics.blueberry.schema.parser.constants.BooleanConstant;
import com.bluerobotics.blueberry.schema.parser.constants.Constant;
import com.bluerobotics.blueberry.schema.parser.constants.Number;
import com.bluerobotics.blueberry.schema.parser.constants.NumberConstant;
import com.bluerobotics.blueberry.schema.parser.constants.StringConstant;
import com.bluerobotics.blueberry.schema.parser.fields.ArrayField;
import com.bluerobotics.blueberry.schema.parser.fields.BaseField;
import com.bluerobotics.blueberry.schema.parser.fields.BlueModule;
import com.bluerobotics.blueberry.schema.parser.fields.BlueberryFieldPacker;
import com.bluerobotics.blueberry.schema.parser.fields.BoolFieldField;
import com.bluerobotics.blueberry.schema.parser.fields.DeferredField;
import com.bluerobotics.blueberry.schema.parser.fields.DefinedTypeField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField.NameValue;
import com.bluerobotics.blueberry.schema.parser.fields.Field;
import com.bluerobotics.blueberry.schema.parser.fields.FieldList;
import com.bluerobotics.blueberry.schema.parser.fields.MessageField;
import com.bluerobotics.blueberry.schema.parser.fields.ParentField;
import com.bluerobotics.blueberry.schema.parser.fields.ScopeName;
import com.bluerobotics.blueberry.schema.parser.fields.SequenceField;
import com.bluerobotics.blueberry.schema.parser.fields.StringField;
import com.bluerobotics.blueberry.schema.parser.fields.StructField;
import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;
import com.bluerobotics.blueberry.schema.parser.gui.BlueberrySchemaParserGui.TextOutput;
import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;
import com.bluerobotics.blueberry.schema.parser.tokens.BaseTypeToken;
import com.bluerobotics.blueberry.schema.parser.tokens.CharToken;
import com.bluerobotics.blueberry.schema.parser.tokens.CommentToken;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.tokens.EolToken;
import com.bluerobotics.blueberry.schema.parser.tokens.FilePathToken;
import com.bluerobotics.blueberry.schema.parser.tokens.IdentifierToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NameValueToken;
import com.bluerobotics.blueberry.schema.parser.tokens.NumberToken;
import com.bluerobotics.blueberry.schema.parser.tokens.ScopeNameToken;
import com.bluerobotics.blueberry.schema.parser.tokens.SingleWordToken;
import com.bluerobotics.blueberry.schema.parser.tokens.StringToken;
import com.bluerobotics.blueberry.schema.parser.tokens.SymbolNameToken;
import com.bluerobotics.blueberry.schema.parser.tokens.Token;
import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants;
import com.bluerobotics.blueberry.schema.parser.tokens.TokenList;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;
import com.bluerobotics.blueberry.schema.parser.writers.WriterUtils;

/**
 * This class implements the token parsing algorithm
 * This reads the schema file, tokenizes and then builds a data structure of fields to represent the schema
 */
public class BlueberrySchemaParser implements Constants, TokenConstants {


//	private static final String SEP = TokenIdentifier.SCOPE_SEPARATOR.id();

	private final TokenList m_tokens = new TokenList();//this is where all tokens get assembled while parsing
	private final FieldList m_defines = new FieldList();//all defines end up here
//	private final ArrayList<Constant<?>> m_constants = new ArrayList<>();//all parsed constants will be stored here
	private final FieldList m_messages = new FieldList();//all parsed messages will be stored here
	
	private final ArrayList<ScopeName> m_imports = new ArrayList<>();//temporary storage of imported module names
	private final ArrayList<Annotation> m_annotations = new ArrayList<>();//temporary storage of annotations
	private final ArrayList<BlueModule> m_moduleStack = new ArrayList<>();//keeps track of the current module that the token being currently processed is within.
	private final ArrayList<BlueModule> m_modules = new ArrayList<>();
	private final ArrayList<IdentifierToken> m_moduleEnd = new ArrayList<>();
	private final ArrayList<ParserIssue> m_issues = new ArrayList<>();
	private String m_fileName = null;//indicates the filename that the present tokens are from
	private String m_lastComment = null;//temporary storage for the last processed comment
	private final TextOutput m_output;
	
	public BlueberrySchemaParser(TextOutput ti) {
		m_output = ti;
	}
	
	/**
	 * Clear this parser's state in preparation for a new parsing session
	 */
	public void clear() {
		m_tokens.clear();
		m_defines.clear();
		m_issues.clear();

		m_messages.clear();
		
		m_imports.clear();
		m_annotations.clear();
		
		m_moduleStack.clear();
		m_moduleStack.add(BlueModule.ROOT);
		m_modules.add(BlueModule.ROOT);
		m_moduleEnd.clear();
		m_fileName = null;
		m_lastComment = null;
		

	}
	/**
	 * this reads through the contents of a file and adds tokens to the current token list
	 * @param filePath - a string of the file path of this file
	 * @param schema - a string containing the schema to parse
	 * @throws SchemaParserException
	 */
	public void append(String filePath, String[] schemaLines) throws SchemaParserException {
		//split into lines

		Coord c = new Coord(filePath, 0,0, schemaLines);	


		m_tokens.add(new FilePathToken(c, filePath));


		while(c != null) {
//				c = c.trim();
			c = processBlockComment(c);
			c = processLineComment(c);
			c = processStrings(c);
			c = processChars(c);
			c = processNextToken(c);
			c = processEol(c);

		}
	
	}
	/**
	 * scans through the token list and generates a full set of constants, defines and message fields from it.
	 * @throws SchemaParserException
	 */
	public void parse() throws SchemaParserException {
		try {
			collapseIdentifiers();
			collapseNumbers();
			collapseSymbolNames();
			collapseBaseTypes();
			collapseLongLong();
			collapseScope();
			collapseWhiteSpace();
			collapseUnsigned();
			collapseComments();
			

			collapseTypedefs();
			collapseEols();
			
			collapseNameValues();
//			collapseSemicolons();
			
			//check all brackets of all kinds to be sure they all match
			m_tokens.matchBrackets(null);

			assembleFields();
			fillInMissingEnumValues();
			checkForDuplicateEnumValues();
			fillInMissingMessageKeyValues();
			fillInMissingModuleKeyValues();
			assignModuleAnnotations();
			checkForDuplicateMessageKeys();
			
			processDeferredFields(m_defines.getIterator(), m_defines);	
			processDeferredFields(m_messages.getIterator(), m_defines);
			
			applyDeferredParameters(m_defines);
			applyDeferredParameters(m_messages);
			
			computeOrder(m_messages);
		
			organizeBools(m_messages);

			
			computeParents(m_defines, null);
			computeParents(m_messages, null);
			
			
			computeIndeces();
			

		} catch (SchemaParserException e) {
			e.printStackTrace();
		}
		System.out.println("************* Messages ***************");
		m_messages.forEach(f -> {
			
			System.out.println(f.toString());
		});


		System.out.println("BlueberrySchemaParser.parse done.");

	}
	/**
	 * any instance of two longs in a row should be collapsed to a long long
	 */
	private void collapseLongLong() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			IdentifierToken long1 = m_tokens.gotoNextId(TokenIdentifier.LONG);
			IdentifierToken long2 = m_tokens.relativeId(1, TokenIdentifier.LONG);
			if(long1 != null && long2 != null) {
				m_tokens.next();
				m_tokens.remove(long2);
				IdentifierToken longLong = new IdentifierToken(long1.getStart(), long2.getEnd(), TokenIdentifier.LONG_LONG);
				m_tokens.replace(long1, longLong);
				
			}

		}
		
	}
	/**
	 * put a reference to all module annotations into every message member of that module
	 */
	private void assignModuleAnnotations() {
		ArrayList<Annotation> as = new ArrayList<>();
		m_modules.forEach(m -> {
			as.clear();
			m.scanAnnotations(a -> {
				as.add(a);
			});
			Annotation[] aa = as.toArray(new Annotation[as.size()]);
//			m.getDefines().forEach(f -> {
//				f.addAnnotation(aa);
//			});
			m.getMessages().forEach(mess -> {
				mess.addAnnotation(aa);
			});
		});
		
	}
	/**
	 * scan through all messages and compute the field order
	 * any children of message fields will be assigned the same value as their parent
	 * @param ms - the field list to scan through
	 */
	private void computeOrder(FieldList ms) {
		ms.forEachOfType(MessageField.class, false, m -> {
			Iterator<Field> fs = m.getChildren().getIterator();
			int order = 0;
			while(fs.hasNext()) {
				Field f = fs.next();
				if(f.isNotFiller()) {//don't include any filler because it was not in the original definition
					f.setOrdinal(order);
					if(f instanceof ParentField) {
						ParentField pf = (ParentField)f;
						int o = order;
						pf.getChildren().forEach(true, ft -> {
							ft.setOrdinal(o);
						});
					}
					++order;
				}
			}
		});
		
	}
	private void organizeBools(FieldList ms) {
		ms.forEachOfType(MessageField.class, false, mf -> {
			organizeBools(mf);
		});
	}
	
	/**
	 * recurse through the specified parent and make sure all one bit fields are contained within bool field fields
	 * @param pf
	 */
	private void organizeBools(ParentField pf) {
		ListIterator<Field> li = pf.getChildren().getIterator();
		BoolFieldField bff = null;
		while(li.hasNext()) {
			Field f = li.next();
			if(f instanceof BoolFieldField) {
				bff = (BoolFieldField)f;
				if(bff.getChildren().size() >= 8) {
					bff = null;
				}
				
			} else if(f instanceof BaseField) {
				BaseField bf = (BaseField)f;
				if(bf.getBitCount() == 1) {
					if(bff != null) {
						bff.add(bf);
						li.remove();
					} else {
						bff = new BoolFieldField(bf.getCoord());
						bff.add(bf);
						li.set(bff);
					}
				}
			} else if(f instanceof ParentField) {
				ParentField pf2 = (ParentField)f;
				organizeBools(pf2);
			}
		}
	}
	/**
	 * Traverse the higherarchy and set parents.
	 * @param fl
	 * @param pf
	 */
	private void computeParents(FieldList fl, ParentField pf) {
		fl.forEach(f -> {
			f.setParent(pf);
			if(f instanceof ParentField) {
				ParentField pf2 = (ParentField)f;
				computeParents(pf2.getChildren(), pf2);
			}
		});
	}
	/**
	 * scans all messages and checks for any duplicate message keys
	 * this should be called after fillInMessageKeyValues()
	 */
	private void checkForDuplicateMessageKeys() {
		final ArrayList<Integer> keys = new ArrayList<>();
		m_modules.forEach(mod -> {
			mod.getMessages().forEachOfType(MessageField.class, false, msg -> {
				int i = msg.getModuleMessageKey();
				
				if(keys.contains(i)) {
					issueError("Duplicate message key detected ("+WriterUtils.formatAsHex(i)+")in "+msg.getName(), null);
				}
				keys.add(i);
			});
		});

	}
	/**
	 * Calculates the field indeces for messages, based on the word packing rules
	 */
	private void computeIndeces() {
		
		m_messages.forEachOfType(MessageField.class, false, mf -> {
			
			
			
			if(mf.useCdrNotBlueberry()) {
				issueError("CDR serialization not supported yet", mf.getCoord());
				//TODO: add a field packer for CDR packing
			} else {
				BlueberryFieldPacker.pack(mf);
			}
		});
		
	}
	
	/**
	 * scan through all messages and assign any message keys that have not been assigned.
	 */
	private void fillInMissingModuleKeyValues() {
		m_modules.forEach(m -> {
			Annotation a = m.getAnnotation(Annotation.MODULE_KEY_ANNOTATION);
			if(a == null || a.getParameter(0, Number.class) == null) {
				 a = new Annotation(Annotation.MODULE_KEY_ANNOTATION);
				 long n = getNextModuleKey();
				a.addParameter(new Number(n));
				m.addAnnotation(a);
				issueNote("Adding module_key: "+n+" for module "+m.getName().toLowerSnake("\\"), m.getCoord());
				
			}
		});
	}
	/**
	 * scan through all messages and assign any message keys that have not been assigned.
	 */
	private void fillInMissingMessageKeyValues() {
		for(BlueModule m : m_modules) {
			m.getMessages().forEachOfType(MessageField.class, false, mf -> {
				Annotation a = mf.getAnnotation(Annotation.MESSAGE_KEY_ANNOTATION);
				if(a == null || a.getParameter(0, Number.class) == null) {
					 a = new Annotation(Annotation.MESSAGE_KEY_ANNOTATION);
					 long i = getNextMessageKey(m);
					a.addParameter(new Number(i));
					mf.addAnnotation(a);
					issueNote("Adding message_key for "+mf.getTypeName()+" message  -> "+WriterUtils.formatAsHex(i), mf.getCoord());
				}
					
			});
		}
		
	}
	/**
	 * picks the next available message key value
	 * @return
	 */
	private long getNextMessageKey(BlueModule m) {
		final ArrayList<Integer> keys = new ArrayList<>();
		m.getMessages().forEachOfType(MessageField.class, false, mf -> {
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
		//this assumes that the lowest value module should be the lowest possible value
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
				} else if(k > i){
					done = true;
				}
			}
		}
		
		return i;
	}
	/**
	 * picks the next available message key value
	 * @return
	 */
	private long getNextModuleKey() {
		final ArrayList<Integer> keys = new ArrayList<>();
		m_modules.forEach(m -> {
			Annotation a = m.getAnnotation(Annotation.MODULE_KEY_ANNOTATION);
			if(a != null) {
				Number an = a.getParameter(0, Number.class);
				if(an != null) {
					keys.add(an.asInt());
				}
			}
			
		});
		Collections.sort(keys);
		int i = 0;
		//this assumes that the lowest value module should be the lowest possible value
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
				} else if(k > i){
					done = true;
				}
			}
		}
		
		return i;
	}
	/**
	 * scans through all SingleWordTokens and converts them to SymbolNameTokens
	 * Note that this should be called after scanning for numbers
	 */
	private void collapseSymbolNames() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()) {
			SingleWordToken swt = m_tokens.gotoNext(SingleWordToken.class);
			if(swt != null) {
				SymbolName sn = SymbolName.guess(swt.getName());
				SymbolNameToken snt = new SymbolNameToken(swt.getStart(), swt.getEnd(), sn);
				m_tokens.replace(swt, snt);
			}
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
			IdentifierToken s = m_tokens.gotoNextId(TokenIdentifier.SCOPE_SEPARATOR);
			if(s != null) {
				SymbolNameToken tName = m_tokens.relative(1, SymbolNameToken.class);
				SymbolNameToken tScope = m_tokens.relative(-1, SymbolNameToken.class);
				if(tScope == null) {
					tScope = m_tokens.relative(-1, ScopeNameToken.class);
				}
				if(tScope != null && tName != null) {
					//this should be a scoped name
					ScopeName sn = ScopeName.combine(tScope.getSymbolName(), tName.getSymbolName());
					ScopeNameToken swt = new ScopeNameToken(tScope.getStart(), tName.getEnd(), sn);
					m_tokens.replace(tScope, swt);
					m_tokens.remove(s);
					m_tokens.remove(tName);
				} else if(tScope == null && tName != null){
					ScopeName sn = ScopeName.ROOT.addLevelBelow(tName.getSymbolName());
					ScopeNameToken swt = new ScopeNameToken(s.getStart(), tName.getEnd(), sn);

					m_tokens.replace(s, swt);
					m_tokens.remove(tName);

				} else if(tName == null) {
					issueError("There does not seem to be a name with this Scope separator.", s.getEnd());
				}
			}
		}
	}
	/**
	 * check all annotations in the specified field list and replace any constants
	 */
	private void applyDeferredParameters(FieldList fs) {
		fs.forEach(f -> {
			f.scanAnnotations(a -> {
				a.replaceDeferredParameters(dp -> {
					Constant<?> c = lookupConstant(dp.name, dp.imports);
					Object result = null;
					if(c != null) {
						result = c.getValue();
					}
					return result;
				});
			});
		});
	}

	/**
	 * method that returns a replacement field for a deferred field
	 * @param fs - this list to check for deferred fields
	 * @param defines - the defines that hopefully contains the definition for those fields
	 * @throws SchemaParserException 
	 */
	private <T extends Field> void processDeferredFields(ListIterator<Field> fi, FieldList defines) throws SchemaParserException {
		
		while(fi.hasNext()) {
			Field f = fi.next();
			if(f instanceof DeferredField) {
				ScopeName[] imports = ((DeferredField) f).getImports();
				ScopeName typeName = f.getTypeName();
				Field dft = null;
				if(typeName == null && f.getTypeId() == null) {
					issueError("BlueberrySchemaParser.processDeferredFields type is somehow not defined.", f.getCoord());
				} else if(typeName != null) {
				
				
					for(Field df : defines.getList()) {
						if(df.getTypeName() == null) {
							issueError("Don't think this should ever be null", df.getCoord());
						}
						
						if(df.getTypeName().isMatch(imports, typeName)) {
							if(dft != null) {
								issueWarning("Type shadowing: "+typeName.toUpperCamelString(), f.getCoord());//TODO: add this as a warning
							} else {
								dft = df;
							}	
						}
					}
					if(dft == null) {
						issueError("Could not find a type definition for "+typeName, f.getCoord());
					}
					fi.set(dft.makeInstance(f.getName()));
				}
			} else if(f instanceof ParentField) {
				processDeferredFields(((ParentField)f).getChildren().getIterator(), defines);
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
					case SEQUENCE:
						m_tokens.next();
						m_tokens.remove(typedef);
						IdentifierToken id2 = new IdentifierToken(typedef.getStart(), id.getEnd(), TokenIdentifier.TYPEDEF_SEQUENCE);
						m_tokens.replace(id, id2);
						break;
					case ENUM:
					case STRUCT:
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
					issueError("Unsigned keyword must be followed by a base type.", it.getEnd());
					m_tokens.gotoSemi();
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
						issueError("Unsigned keyword makes no sense combined with "+btt.getName(), btt.getStart());
					}
					BaseTypeToken newBtt = new BaseTypeToken(it.getStart(), btt.getEnd(), newTi);
					m_tokens.replace(it, newBtt);
					m_tokens.remove(btt);
				}
			}
			m_tokens.next();
		}

	}

	/**
	 * scans through the whole token list and creates all the fields
	 * @throws SchemaParserException
	 */
	private void assembleFields() throws SchemaParserException {
		m_moduleStack.clear();
		m_moduleEnd.clear();
		m_modules.clear();
		m_lastComment = null;
		m_tokens.resetIndex();

		while(m_tokens.isMore()) {
			Token t = m_tokens.getCurrent();
			//check if we've hit the closing brace of the current module - if there is one
			if(!m_moduleEnd.isEmpty() && !m_tokens.isCurrentBefore(m_moduleEnd.getLast())) {
				m_moduleStack.removeLast();
				m_moduleEnd.removeLast();
				m_tokens.next();//skip past last brace
			}
			IdentifierToken it = m_tokens.relative(0,IdentifierToken.class);
			if(it != null) {
				switch(it.getKeyword()) {
				case CONST:
					assembleConst(it);
					break;
				case ENUM:
					assembleEnum(it);
					break;
				case IMPORT:
					assembleImport(it);
					break;
				case MESSAGE:
					assembleMessage(it);
					break;
				case MODULE:
					assembleModule(it);
					break;
				case TYPEDEF_SEQUENCE:
					assembleSequence(it);
					break;
				case STRUCT:
					assembleStructs(it);
					break;
				case TYPEDEF:
					assembleTypedef(it);
					break;
				case ANNOTATION_START:
					assembleAnnotation(it);
					break;
				case SEMICOLON:
					//nothing to do
					break;
				default:
					issueNote("Did not process "+it, it.getStart());
					break;
				}


			} else if(t instanceof FilePathToken) {
				FilePathToken fpt = (FilePathToken)t;
				m_fileName = fpt.getString();
				m_imports.clear();
				m_moduleStack.clear();
				m_moduleStack.add(BlueModule.ROOT);
				m_moduleEnd.clear();
			} else if(t instanceof CommentToken) {
				CommentToken ct = (CommentToken)t;
				m_lastComment = ct.combineLines();
			}
			m_tokens.next();

		}
	}

	private void assembleImport(IdentifierToken it) throws SchemaParserException {
		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
		if(nameToken == null) {
			issueError("Import statement does not have a name specified", it.getStart());
			m_tokens.gotoSemi();
		}
		ScopeName scope = ScopeName.wrap(nameToken.getSymbolName());
		if(!scope.isAbsolute()) {
			scope = scope.addRoot();
		}
		m_imports.add(scope);
		m_tokens.setIndex(nameToken);
	}
	/**
	 * process a struct statement and all the fields in the following braces
	 * The fields are actually processed by a helper method
	 * @param it
	 * @throws SchemaParserException
	 */
	private void assembleStructs(IdentifierToken it) throws SchemaParserException {

		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
		IdentifierToken braceStart = m_tokens.relativeId(2, TokenIdentifier.BRACE_START);
		IdentifierToken braceEnd = m_tokens.matchBrackets(braceStart);
		if(nameToken == null) {
			throw new SchemaParserException("Struct has no specified name.", it.getEnd());
		} else if(braceStart == null) {
			throw new SchemaParserException("Struct has no opening brace.", it.getEnd());
		}
		ScopeName name = m_moduleStack.getLast().scope(nameToken.getSymbolName());


		StructField m = new StructField(SymbolName.EMPTY, name, m_lastComment, it.getStart());
		m.setFileName(m_fileName);
		m_lastComment = null;

		m_defines.add(m);
		m_moduleStack.getLast().getDefines().add(m);
		m.addAnnotation(m_annotations);
		m_annotations.clear();
		processMessageOrStructFields(m, braceStart, braceEnd);

		
	}
	/**
	 * process a message statement and all the fields in the following braces
	 * The fields are actually processed by a helper method
	 * A message is like a strut whose purpose is to be sent over the network
	 * @param it
	 * @throws SchemaParserException
	 */
	private void assembleMessage(IdentifierToken it) throws SchemaParserException {
		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
		IdentifierToken braceStart = m_tokens.relativeId(2, TokenIdentifier.BRACE_START);
		IdentifierToken braceEnd = m_tokens.matchBrackets(braceStart);
		if(nameToken == null) {
			throw new SchemaParserException("Message has no specified name.", it.getEnd());
		} else if(braceStart == null) {
			throw new SchemaParserException("Message statement has no opening brace.", it.getEnd());
		} else if(braceEnd == null) {
			throw new SchemaParserException("Message statement has no closing brace.", braceStart.getEnd());
		}
		ScopeName name = m_moduleStack.getLast().scope(nameToken.getSymbolName());


		MessageField m = new MessageField(SymbolName.EMPTY, name, m_lastComment, it.getEnd());
		m.setFileName(m_fileName);
		m_lastComment = null;

		m_messages.add(m);
		m_moduleStack.getLast().getMessages().add(m);
		m.addAnnotation(m_annotations);
		m_annotations.clear();
		processMessageOrStructFields(m, braceStart, braceEnd);
	}
	private void processMessageOrStructFields(ParentField m, IdentifierToken braceStart, IdentifierToken braceEnd) {
		m_tokens.setIndex(braceStart);
		m_tokens.next();
		while(m_tokens.isCurrentBefore(braceEnd)) {
			if(IdentifierToken.check(m_tokens.getCurrent(), TokenIdentifier.ANNOTATION_START)) {
				assembleAnnotation((IdentifierToken)m_tokens.getCurrent());
				 
			} else if(m_tokens.getCurrent() instanceof CommentToken) {
				
			} else if(m_tokens.getCurrent() instanceof EolToken) {
			} else if(IdentifierToken.check(m_tokens.getCurrent(), TokenIdentifier.SEMICOLON)){
			
			} else {
			
				//reuse a bunch of fields from above for the sub-field
				CommentToken ct = m_tokens.relative(-1, CommentToken.class);
				String comment = ct != null ? ct.combineLines() : null;
				SymbolNameToken typeNameToken = m_tokens.relative(0, SymbolNameToken.class);//or this
//				if(typeNameToken == null) {
//					typeNameToken = m_tokens.relative(0, ScopeNameToken.class);
//				}
				BaseTypeToken btt = m_tokens.relative(0, BaseTypeToken.class);//or this
				if(btt == null && typeNameToken == null) {
					throw new SchemaParserException("Expecting a type name.", m_tokens.getCurrent().getStart());
				} else if(btt != null && btt.getKeyword() == TokenIdentifier.STRING) {
					m.add(processString(btt));
				} else {
					SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);//or this
					if(nameToken == null) {
						throw new SchemaParserException("No name specified for field", m_tokens.getCurrent().getEnd());
					}
					
					if(btt != null) {
						//there's a base type identifier so we're either adding a string or a numerical base type
						
					
						
						//add a base type field
						m.add(new BaseField(nameToken.getSymbolName(), lookupBaseType(btt.getKeyword()), comment, nameToken.getEnd()));
						m_tokens.setIndex(nameToken);
						
					} else {//must be a defined type field
						//we have to defer looking this up for now
						DeferredField df = new DeferredField(nameToken.getSymbolName(), ScopeName.wrap(typeNameToken.getSymbolName()), getImports(true), comment, nameToken.getStart());
						m.add(df);
						m_tokens.setIndex(nameToken);	
					}
				}
				
				
			}
			m_tokens.next();
		}
		
	}
	/**
	 * of the form <comment?><typdef><sequence><angle bracket start><constituentTypeName><comma?><number?><angle bracket end><sequenceTypeName>
	 * except this method assumes that the <typedef> has already been removed
	 * @param it - the sequence token
	 * @throws SchemaParserException
	 */
	private void assembleSequence(IdentifierToken it) throws SchemaParserException {

		IdentifierToken angleBracketStart = m_tokens.relativeId(1, TokenIdentifier.ANGLE_BRACKET_START);
		if(angleBracketStart == null) {
			throw new SchemaParserException("Sequence keyword should be followed by an open angle bracket.", it.getEnd());
		}
		BaseTypeToken btt = m_tokens.relative(2, BaseTypeToken.class);
		SymbolNameToken snt = m_tokens.relative(2, SymbolNameToken.class);
		Field cf = null;
		if(btt != null) {
			TypeId tid = lookupBaseType(btt.getKeyword());
			if(btt.getKeyword() == TokenIdentifier.STRING) {
				cf = processString(btt);
				
			} else {
				cf = new BaseField(null, tid, null, btt.getEnd());
			}
		} else if(snt != null) {
			cf = new DeferredField(null, ScopeName.wrap(snt.getSymbolName()), getImports(true), null, snt.getStart());
		} else {
			
			issueError("Sequence must be defined with a type for its elements.", it.getEnd());
			
		}
		if(cf != null) {
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
			if(nameToken == null) {
				throw new SchemaParserException("Sequence needs a type name specified.", angleBracketEnd.getEnd());
			}
			m_tokens.setIndex(angleBracketEnd);
			ScopeName name = m_moduleStack.getLast().scope(nameToken.getSymbolName());
			SequenceField sf = new SequenceField(null, name, m_lastComment, it.getEnd());
			sf.setFileName(m_fileName);
			sf.add(cf);
			sf.setLimit(n);
			m_lastComment = null;
			m_defines.add(sf);
			m_moduleStack.getLast().getDefines().add(sf);
			
		
		}
		m_tokens.gotoSemi();
	}


	/**
	 * processes a string token at the current token list position
	 * @param it
	 * @return
	 */
	private StringField processString(IdentifierToken it)  throws SchemaParserException {
		m_tokens.setIndex(it);
		CommentToken ct = m_tokens.relative(-1, CommentToken.class);
		IdentifierToken angleBracketStart = m_tokens.relativeId(1, TokenIdentifier.ANGLE_BRACKET_START);
		int maxSize = 65536;
		if(angleBracketStart != null) {
			//todo trap number token
			NumberToken nt = m_tokens.relative(2,NumberToken.class);
			
			if(nt == null) {
				throw new SchemaParserException("Angle brackets in a string definition should contain a number.", angleBracketStart.getEnd());
			}
			maxSize = nt.getNumber().asInt();
			IdentifierToken angleBracketEnd = m_tokens.matchBrackets(angleBracketStart);
			if(angleBracketEnd == null) {
				throw new SchemaParserException("Starting angle brackets must have closing bracket too.", angleBracketStart.getEnd());
			}
			m_tokens.setIndex(angleBracketEnd);

		}
		
		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);
		
		if(nameToken == null) {
			throw new SchemaParserException("String field needs a name specified.", it.getEnd());
		}
		m_tokens.setIndex(nameToken);
//		SymbolName name = m_module.getLast().addLevel(nameToken.getSymbolName());
		SymbolName name = nameToken.getSymbolName();
		StringField sf = new StringField(name, maxSize, ct == null ? "" : ct.combineLines(), it.getEnd());
		sf.setFileName(m_fileName);
		return sf;
	}


	/**
	 * of the form <comment?><typedef><constituentTypeName><typeName><square bracket start?><number?><square bracket end>
	 * @param it
	 * @throws SchemaParserException
	 */
	private void assembleTypedef(IdentifierToken it) throws SchemaParserException {
		IdentifierToken st = m_tokens.relativeId(1,  TokenIdentifier.SEQUENCE);
//		if(st != null) {
//			m_tokens.remove(it);
//			m_tokens.setIndex(st);
//			assembleSequence(st);
//			return;
//		}
		SymbolNameToken typeName = m_tokens.relative(1, SymbolNameToken.class);//this is the original type that this typedef is based on 
		BaseTypeToken btt = m_tokens.relative(1, BaseTypeToken.class);//this could also be the original type depending on whether it's a base type or not
		SymbolNameToken name = m_tokens.relative(2, SymbolNameToken.class);//name of new type
		
		//check for valid name and type
		if(name == null) {
			throw new SchemaParserException("No type name specified for this typedef.",it.getEnd());
		} else if(btt == null && typeName == null) {
			throw new SchemaParserException("No type specified for this typedef.",it.getEnd());
		}
		ScopeName scopedName = m_moduleStack.getLast().scope(name.getSymbolName());
		
		
		//check for arrays and keep track of the dimensions
		ArrayList<Integer> dims = new ArrayList<>();
		boolean keepGoing = true;
		
		IdentifierToken squareBracketStart  = null;	
		NumberToken arraySize = null;
		SymbolNameToken arraySizeConst = null;	
		IdentifierToken squareBracketEnd = null;
		Token lastValidEnd = name;
		
		int i = 3;
		
		while(keepGoing) {
			squareBracketStart = m_tokens.relativeId(i, TokenIdentifier.SQUARE_BRACKET_START);
			arraySize = m_tokens.relative(i + 1, NumberToken.class);
			arraySizeConst = m_tokens.relative(i + 1,SymbolNameToken.class);
			squareBracketEnd = m_tokens.relativeId(i + 2, TokenIdentifier.SQUARE_BRACKET_END);
			i += 3;
			
			
			if(squareBracketStart != null) {
				if(squareBracketEnd == null) {
					throw new SchemaParserException("Starting square bracket does not have a closing bracket.", squareBracketStart.getEnd());
				} else if(arraySize == null && arraySizeConst == null) {
					throw new SchemaParserException("Array definition needs a size specified.", squareBracketStart.getEnd());
				}
				
				
				int n = arraySize != null ? arraySize.getNumber().asInt() : lookupConstInt(arraySizeConst.getSymbolName(), getImports(true));
				dims.add(n);
				lastValidEnd = squareBracketEnd;
				
			} else {
				keepGoing = false;
			}
		}
		
		
		
		if(btt != null && btt.getKeyword() == TokenIdentifier.STRING) {
			throw new SchemaParserException("Typedef doesn't work with Strings yet.", btt.getStart());
		}
		
		TypeId id = (btt != null) ? lookupBaseType(btt.getKeyword()) : TypeId.DEFERRED;
		
		ParentField pf;
		if(dims.size() == 0) {
		
			//this is a normal base type
			pf = new DefinedTypeField(null, scopedName, m_lastComment, it.getEnd());
//			TypeDefField tdf = new TypeDefField(SymbolName.EMPTY, scopedName, id, m_lastComment);
		
			pf.setFileName(m_fileName);
			
			


		} else {
			
			//this is an array type

		
			int[] ds = new int[dims.size()];
			for(int j = 0; j < dims.size(); ++j) {
				ds[j] = dims.get(j);
			}
			pf = new ArrayField(null, scopedName, id, ds, m_lastComment, name.getEnd());
			pf.setFileName(m_fileName);
			


		}
		m_defines.add(pf);
		m_moduleStack.getLast().getDefines().add(pf);
		//now add a field to contain the target type of this define
		if(btt != null) {
			pf.add(new BaseField(null, lookupBaseType(btt.getKeyword()), null, btt.getEnd()));

		} else {
			//we must defer this
			pf.add(new DeferredField(null, ScopeName.wrap(typeName.getSymbolName()), getImports(true), m_lastComment, typeName.getEnd()));
		}

		m_lastComment = null;
		m_tokens.setIndex(lastValidEnd);


	}
	/**
	 * Looks up a symbol name in the constant list and returns its integer value if it exists
	 * TODO: make it look through the imports
	 * @param name
	 * @return
	 */
	private int lookupConstInt(SymbolName name, ScopeName[] imports) {
		NumberConstant result = null;
		Constant<?> c = lookupConstant(name, imports);
		if(c instanceof NumberConstant) {
			result = (NumberConstant)c;
		}
		return result != null ? result.getValue().asInt() : -1;
	}
	
	/**
	 * scans through all known modules to find a constant with the specified name, given the list of imports
	 * @param n
	 * @param imports
	 * @return
	 */
	private Constant<?> lookupConstant(SymbolName n, ScopeName[] imports){
		Constant<?> result = null;
		for(ScopeName sn : imports) {
			BlueModule found = null;
			for(int i = 0; i < m_modules.size(); ++i) {
				BlueModule m = m_modules.get(i);
				if(m.getName().equals(sn)) {
					found = m;
					break;
				}
			}
			if(found != null) {
				result = found.getConstant(n);
				break;
			}
		}
		if(result == null) {
			throw new SchemaParserException("Cannot find constant of name: \""+n.toLowerSnakeString()+"\"", null);
		}
		return result;
	}
	private void assembleEnum(IdentifierToken enumT) throws SchemaParserException {


		SymbolNameToken nameToken = m_tokens.relative(1, SymbolNameToken.class);
		IdentifierToken colon = m_tokens.relativeId(2, TokenIdentifier.COLON);
		BaseTypeToken btt = m_tokens.relative(3, BaseTypeToken.class);
		IdentifierToken braceStart = m_tokens.relativeId(colon == null ? 2 : 4, TokenIdentifier.BRACE_START);
		IdentifierToken braceEnd = m_tokens.matchBrackets(braceStart);
		if(nameToken == null) {
			throw new SchemaParserException("Enum name not specified.", enumT.getEnd());
		} else if(braceStart == null){
			throw new SchemaParserException("Enum needs a body with members.", enumT.getEnd());
		} else if(braceEnd == null) {
			throw new SchemaParserException("Enum body has no closing brace.", enumT.getEnd());
		}

		TypeId bt = (btt != null) ? lookupBaseType(btt.getKeyword()) : TypeId.UINT32;
		ScopeName name = m_moduleStack.getLast().scope(nameToken.getSymbolName());
		EnumField et = new EnumField(SymbolName.EMPTY, name, bt, m_lastComment, enumT.getEnd());
		et.setFileName(m_fileName);
		m_lastComment = null;
		m_defines.add(et);
		m_moduleStack.getLast().getDefines().add(et);

		m_tokens.setIndex(braceStart);

		while(m_tokens.isCurrentBefore(braceEnd)) {
			Token t = m_tokens.gotoNextOfThese(braceEnd, SymbolNameToken.class, NameValueToken.class, braceEnd.getClass());
			if(t == null || t == braceEnd) {
				break;
			}
			//reuse a bunch of fields from above for the sub-field
			CommentToken ct = m_tokens.relative(-1, CommentToken.class);
			String comment = ct != null ? ct.combineLines() : null;
			nameToken = m_tokens.relative(0, SymbolNameToken.class);
			NameValueToken<?> nvt = m_tokens.relative(0, NameValueToken.class);
			Number n = null;
			if(nvt != null && nvt.getValue() instanceof Number) {
				n = (Number)nvt.getValue();
			}
			if(nameToken != null) {
				et.addNameValue(nameToken.getSymbolName(), Number.NAN, comment);
			} else if(n != null) {
				try {
					et.addNameValue(nvt.getSymbolName(), n, comment);
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
	private void assembleConst(IdentifierToken it) throws SchemaParserException {
//		CommentToken ct = m_tokens.relative(-1, CommentToken.class);
//		String comment = ct != null ? ct.combineLines() : null;

		BaseTypeToken btt = m_tokens.relative(1, BaseTypeToken.class);
		boolean nvtGood = m_tokens.relative(2, NameValueToken.class) != null;
		SymbolName name = nvtGood ? m_tokens.relative(2, NameValueToken.class).getSymbolName() : null;
		Object val = nvtGood ? m_tokens.relative(2, NameValueToken.class).getValue() : null;
		Number nVal = val instanceof Number ? (Number)val : null;
		String sVal = val instanceof String ? (String)val : null;
		TokenIdentifier idVal = val instanceof TokenIdentifier ? (TokenIdentifier)val : null;
		
		TokenIdentifier ti = btt != null ? btt.getKeyword() : null;
		if(btt == null) {
			issueWarning("Only base types and Strings can be declared const so far.", it.getStart());
			m_tokens.gotoSemi();
		} else if(ti == TokenIdentifier.STRING || ti == TokenIdentifier.CHAR) {
			if(sVal == null) {
				issueError("Const string must include a string value", it.getStart());
				m_tokens.gotoSemi();
			} else {
				StringConstant c = new StringConstant(name, sVal, m_lastComment);
				c.setFileName(m_fileName);
				m_lastComment = null;
				m_moduleStack.getLast().addConstant(c);
			
			}
			
			


		
		} else if(btt.getKeyword() == TokenIdentifier.BOOLEAN) {
			//check for boolean
			Boolean b  = null;
			
			if(idVal != null){
				if(idVal == TokenIdentifier.TRUE) {
					b = Boolean.TRUE;
				} else if(idVal == TokenIdentifier.FALSE) {
					b = Boolean.FALSE;
				} else {
					issueError("Valid values are either true or false, not "+idVal, btt.getStart());
					
				}
			} else if(nVal != null){
				if(nVal.asFloat() > 0.0) {
					b = Boolean.TRUE; 
				} else if(nVal.asFloat() < 0.0) {
					issueError("Negative value is inappropriate for boolean type.", btt.getStart());
					
				}
			} else {
				issueError("Const must include a boolean value", btt.getStart());
				
			}
			
			if(b != null) {
			
				BooleanConstant c = new BooleanConstant(name, b, m_lastComment);
				c.setFileName(m_fileName);
				m_lastComment = null;
				
				m_moduleStack.getLast().addConstant(c);
			}
			
		} else {
				
			if(nVal == null) {
				issueError("Const must include a name and value.", it.getEnd());
				
			} else {

				TypeId typeId = lookupBaseType(btt.getKeyword());
				if(typeId == null) {
					issueError("Something wrong with base type \""+btt.getKeyword()+"\"", btt.getStart());
				} else {
				
			
			
				
					NumberConstant c = new NumberConstant(typeId, name, nVal, m_lastComment);
					c.setFileName(m_fileName);
					m_lastComment = null;
				
			
					m_moduleStack.getLast().addConstant(c);
				}
			}
		
	
		}

		m_tokens.gotoSemi();


	}
	private void assembleModule(IdentifierToken it) throws SchemaParserException {
		SymbolNameToken moduleName = m_tokens.relative(1, SymbolNameToken.class);
		if(moduleName == null) {
			moduleName = m_tokens.relative(1, ScopeNameToken.class);
		}
		IdentifierToken braceStart = m_tokens.relativeId(2, TokenIdentifier.BRACE_START);
		if(braceStart == null) {
			issueError("Module should start with opening brace", it.getEnd());
			m_tokens.gotoSemi();
		} else if(moduleName == null){
			issueError("Module name is ill-formed.",it.getEnd());
			m_tokens.gotoSemi();
		} else {
			IdentifierToken braceEnd = m_tokens.matchBrackets(braceStart);//this should never be null I think
			if(braceEnd == null) {
				issueError("Module statement open brace is never closed.", braceStart.getEnd());
				m_tokens.setIndex(m_tokens.getLast());
			}
			BlueModule m = m_moduleStack.getLast().makeChild(moduleName.getSymbolName(), moduleName.getStart());
			int i = m_modules.indexOf(m);
			if(i >= 0) {
				m = m_modules.get(i);
			} else {
				m_modules.add(m);
			}
			m.addAnnotation(m_annotations);
			m_annotations.clear();
			m_moduleStack.add(m);
			m_moduleEnd.add(braceEnd);
			
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
						throw new SchemaParserException("Duplicate enum item names: "+nv1.getName().toUpperSnakeString(), ef.getCoord());
					} else if(nv1.getValue().equals(nv2.getValue())) {
						//they have the same value
						throw new SchemaParserException("Duplicate enum item values: "+nv1.getName().toUpperSnakeString()+" = "+nv1.getValue(), ef.getCoord());
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
		case LONG:
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
		case LONG_LONG:
			result = TypeId.UINT64;
			break;
		case INT64:
			result = TypeId.INT64;
			break;
		case CHAR:
			result = TypeId.CHAR;
			break;
		default:
			break;

		}
		return result;

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
				StringToken stringT = m_tokens.relative(+1, StringToken.class);
				CharToken charT = m_tokens.relative(+1, CharToken.class);
				SymbolNameToken constT = m_tokens.relative(+1, SymbolNameToken.class);
				ScopeNameToken scopedConstT = m_tokens.relative(+1, ScopeNameToken.class);
				IdentifierToken iConstT = m_tokens.relative(+1, IdentifierToken.class);
				String comment = commentT != null ? commentT.combineLines() : null;
				if(nameT != null && numberT != null) {
					//this should be for a number
					NameValueToken<Number> nvt = new NameValueToken<Number>(Coord.findStart(nameT, numberT), Coord.findEnd(nameT, numberT, commentT), nameT.getSymbolName(), numberT.getNumber(), comment);
					m_tokens.replace(equalsT, nvt);
					m_tokens.remove(numberT);
					m_tokens.remove(nameT);
					m_tokens.remove(commentT);

				} else if(nameT != null && stringT != null){
					//this should be for a String
					NameValueToken<String> nvt = new NameValueToken<String>(Coord.findStart(nameT, numberT), Coord.findEnd(nameT, numberT, commentT), nameT.getSymbolName(), stringT.getString(), comment);

					m_tokens.remove(nameT);
					m_tokens.remove(commentT);
					m_tokens.remove(stringT);
					m_tokens.replace(equalsT, nvt);
				} else if(nameT != null && scopedConstT != null) {
					NameValueToken<ScopeName> nvt = new NameValueToken<ScopeName>(Coord.findStart(nameT, numberT), Coord.findEnd(nameT, numberT, commentT), nameT.getSymbolName(), scopedConstT.getScopeName(), comment);

					m_tokens.remove(nameT);
					m_tokens.remove(commentT);
					m_tokens.remove(scopedConstT);
					m_tokens.replace(equalsT, nvt);
				} else if(nameT != null && constT != null) {
					NameValueToken<SymbolName> nvt = new NameValueToken<SymbolName>(Coord.findStart(nameT, numberT), Coord.findEnd(nameT, numberT, commentT), nameT.getSymbolName(), constT.getSymbolName(), comment);
					m_tokens.remove(nameT);
					m_tokens.remove(commentT);
					m_tokens.remove(constT);
					m_tokens.replace(equalsT, nvt);
				} else if(nameT != null && iConstT != null) {
					NameValueToken<TokenIdentifier> nvt = new NameValueToken<TokenIdentifier>(Coord.findStart(nameT, numberT), Coord.findEnd(nameT, numberT, commentT), nameT.getSymbolName(), iConstT.getKeyword(), comment);
					m_tokens.remove(nameT);
					m_tokens.remove(commentT);
					m_tokens.remove(iConstT);
					m_tokens.replace(equalsT, nvt);
				} else {
					throw new SchemaParserException("Incorrect tokens around equals.", equalsT.getStart());
				}
			} else {
				break;
			}
		}

	}
	/**
	 * replace any single word tokens that match token identifiers with identifier tokens
	 */
	private void collapseIdentifiers() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			SingleWordToken swt = m_tokens.gotoNext(SingleWordToken.class);
			if(swt != null) {
				for(TokenIdentifier ti : TokenIdentifier.values()) {
					if(ti.id().toLowerCase().equals(swt.getName().toLowerCase())) {
						IdentifierToken it = new IdentifierToken(swt.getStart(), swt.getEnd(), ti);
						m_tokens.replace(swt, it);
						
						break;
					}
					
				}
				m_tokens.next();
				
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
	private void assembleAnnotation(IdentifierToken at) throws SchemaParserException {

		if(at != null) {
			ScopeNameToken snt = m_tokens.relative(1, ScopeNameToken.class);
			SymbolNameToken swt = m_tokens.relative(1, SymbolNameToken.class);
			IdentifierToken bracketStart = m_tokens.relativeId(2, TokenIdentifier.BRACKET_START);
			IdentifierToken bracketEnd = m_tokens.matchBrackets(bracketStart);
			boolean noBrackets = false;

			if(swt == null && snt == null) {
				throw new SchemaParserException("Annotation symbol should be followed by a name", at.getEnd());
			} else if(bracketStart == null) {
//				throw new SchemaParserException("Annotation should be followed by an opening bracket.", swt.getEnd());
				noBrackets = true;
			} else if(bracketEnd == null) {
				//this should never happen because the match bracket will throw its own exception
			}
			
			ScopeName sn = snt == null ? ScopeName.wrap(swt.getSymbolName()) : snt.getScopeName();
			Annotation a = new Annotation(sn);
			
			

			if(noBrackets) {
				m_tokens.setIndex(swt);
				m_tokens.next();
			} else {
				m_tokens.setIndex(bracketStart);
				m_tokens.next();
				while(m_tokens.isCurrentBefore( bracketEnd)) {
					Token t = m_tokens.getCurrent();
					if(t instanceof StringToken) {
						
						a.addParameter(t.getName());
					} else if(t instanceof SymbolNameToken) {
						//this is likely a constant
						a.addDeferredParameter(((SymbolNameToken)t).getSymbolName(), getImports(true));
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
					} else if(t instanceof NameValueToken) {
						NameValueToken<?> nvt = (NameValueToken<?>)t;
						a.addParameter(nvt.getValue());
					} else {
						throw new SchemaParserException("This doesn't seem to be a valid annotation parameter "+t, t.getStart());
					}
					m_tokens.next();
	
				}
//				if(m_tokens.getCurrent() == bracketEnd) {
//					m_tokens.next();
//				}
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
	 * builds an array of all names of modules, from any import statements and if specified, from the current module too
	 * @param includeModule
	 * @return
	 */
	private ScopeName[] getImports(boolean includeModule) {
		ArrayList<ScopeName> result = new ArrayList<ScopeName>();
		result.addAll(m_imports);
		if(includeModule) {
			result.add(m_moduleStack.getLast().getName());
		}
		result.add(0, m_moduleStack.getFirst().getName());
		return result.toArray(new ScopeName[result.size()]);
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
							TokenIdentifier.SQUARE_BRACKET_START,
							TokenIdentifier.SEMICOLON
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
	/**
	 * removes any semicolon tokens that occur either right right after  the following tokens:
	 * NameValueToken, CommmentToken, FilePathToken, closing brace, closing bracket, closing square bracket
	 * Also removes if the token right after the semicolon is:
	 * NameValueToken, CommentToken, FilePathToken, BraceEnd, BracketEnd, SquareBracketEnd
	 */
	private void collapseSemicolons() {
		m_tokens.resetIndex();
		while(m_tokens.isMore()){
			//first find next define element
			IdentifierToken et = m_tokens.gotoNextId(TokenIdentifier.SEMICOLON);
			Token tPrev = m_tokens.relative(-1);
			IdentifierToken itPrev = m_tokens.relative(-1, IdentifierToken.class);
			Token tNext = m_tokens.relative(1);
			IdentifierToken itNext = m_tokens.relative(1, IdentifierToken.class);
			if(tPrev instanceof NameValueToken || tPrev instanceof CommentToken || tPrev instanceof FilePathToken
					|| (itPrev != null && itPrev.check(TokenIdentifier.BRACE_END, TokenIdentifier.BRACKET_END, TokenIdentifier.SQUARE_BRACKET_END))) {
				m_tokens.remove(et);
			} else if(tNext instanceof NameValueToken || tNext instanceof CommentToken || tNext instanceof FilePathToken 
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
				TokenIdentifier.ANGLE_BRACKET_START,
				TokenIdentifier.ANGLE_BRACKET_END,
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
				
		if(s.isEmpty()) {
		} else if(s.isBlank()) {
			m_tokens.add(new IdentifierToken(start, end, TokenIdentifier.SPACE));
		} else {
			m_tokens.add(new SingleWordToken(start, end));
			
		}
		
		return result;
	}

	/**
	 * find all line comments and insert the token at the start of the line they are on in the token list
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

			m_tokens.add(m_tokens.getFirstIndexBeforeLine(start.line), new CommentToken(start, end, comment, false));
		}

		return result;
	}
	/**
	 * if the next coord is a single quotation mark then process as a string until the closing quote
	 * if no valid closing quote is found then consume the remainder of the whole input
	 * So this really is not different that processsing single quotes
	 * @param c
	 * @return
	 */
	private Coord processChars(Coord c) {
		if(c == null) {
			return null;
		}

		Coord result = c;

		if(result.startsWith(CHAR_DELIMITER)) {
			Coord start = result.incrementIndex(1);//we don't want to point to the quotation mark
			Coord end = start;
			boolean notDone = true;
			while(notDone) {
				Coord r = result.findNext(CHAR_DELIMITER, STRING_ESCAPE_DELIMITER);
				if(r == null) {
					System.out.println("BlueberrySchemaParser.processStrings");
				}
				result = r;
				if(result.isAtEnd()) {
					notDone = false;
					end = result;
				} else if(result.startsWith(CHAR_DELIMITER)) {
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


	public FieldList getMessages() {
		return m_messages;
	}
	public FieldList getDefines() {
		return m_defines;
	}
	public ArrayList<BlueModule> getModules(){
		return m_modules;
	}

	private void issueError(String desc, Coord loc) {
		logIssue(ParserIssue.error(desc, loc));
	}
	private void issueWarning(String desc, Coord loc) {
		logIssue(ParserIssue.warning(desc, loc));
	}
	private void issueNote(String desc, Coord loc) {
		logIssue(ParserIssue.note(desc, loc));
	}
	private void logIssue(ParserIssue pi) {
		m_issues.add(pi);
		m_output.add(pi.toString(), pi.getType());
	}



}

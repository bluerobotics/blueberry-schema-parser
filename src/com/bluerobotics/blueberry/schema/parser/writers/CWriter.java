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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.constants.Number;
import com.bluerobotics.blueberry.schema.parser.fields.ArrayField;
import com.bluerobotics.blueberry.schema.parser.fields.BaseField;
import com.bluerobotics.blueberry.schema.parser.fields.BoolFieldField;
import com.bluerobotics.blueberry.schema.parser.fields.DefinedTypeField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField.NameValue;
import com.bluerobotics.blueberry.schema.parser.fields.Field;
import com.bluerobotics.blueberry.schema.parser.fields.FieldList;
import com.bluerobotics.blueberry.schema.parser.fields.MessageField;
import com.bluerobotics.blueberry.schema.parser.fields.ParentField;
import com.bluerobotics.blueberry.schema.parser.fields.ScopeName;
import com.bluerobotics.blueberry.schema.parser.fields.SequenceField;
import com.bluerobotics.blueberry.schema.parser.fields.StructField;
import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;
import com.bluerobotics.blueberry.schema.parser.parsing.BlueberrySchemaParser;
import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;

/**
 * This class implements the autogeneration of C code based on a parsed field structure
 */
public class CWriter extends SourceWriter {

	public CWriter(File dir, BlueberrySchemaParser parser, String header) {
		super(dir, parser, header);
	}

	@Override
	public void write() {
			FieldList messages = getParser().getMessages();
			ArrayList<ScopeName> modules = new ArrayList<>();
			
			messages.forEachOfType(MessageField.class, false, mf -> {
				ScopeName module = mf.getTypeName().removeLastLevel();
				if(!modules.contains(module)) {
					modules.add(module);
				}
				
			});
			FieldList defines = getParser().getDefines();
			
			defines.forEachOfType(EnumField.class, false, ef -> {
				ScopeName module = ef.getTypeName().removeLastLevel();
				if(!modules.contains(module)) {
					modules.add(module);
				}
			});

			//now modules contains a list of all unique modules.
//			//mow make a header and source files for each message
			
			modules.forEach(mod -> {
				makeHeaderFile(mod);
				makeSourceFile(mod);
			});
			



	}
	private void makeHeaderFile(ScopeName module) {
		String moduleFileRoot = module.deScope().toLowerCamel();
		
		startFile(getHeader());



		addSectionDivider("Includes");
		addLine("#include <stdbool.h>");
		addLine("#include <stdint.h>");
		addLine("#include <blueberry-transcoder.h>");

		addSectionDivider("Defines");
		
		addLineComment("Add message keys");
		m_parser.getMessages().forEachOfTypeInScope(MessageField.class, false, module, mf -> {
			addMessageKey(mf);	
		});

		addSectionDivider("Types");

//		addFirstBlockDefine();
//
		m_parser.getDefines().forEachOfTypeInScope(EnumField.class, false, module, ef -> {
			writeEnum(ef);
		});
//
		addSectionDivider("Function Prototypes");
		m_parser.getMessages().forEachOfTypeInScope(MessageField.class, false, module, mf -> {
			makeMessageAdderSignature(mf, true);
		});
		m_parser.getMessages().forEachOfTypeInScope(MessageField.class, false, module, mf -> {
			mf.getChildren().forEach(f -> {
				
				makeMessageGetterSetterSignatures(f, true, true);
				makeMessageGetterSetterSignatures(f, false, true);
			}, true);
			
		});
		
		
//		addBytesPerRepeatGetter(top, true);
//
//		addBaseFieldGetters(top, true);
//
//		addPacketStartFinish(top, true);
//
//		addBlockFunctionGetters(top, true);
//
//		addBlockAdders(top, true);
//
//		addArrayAdders(top, true);
//
//		addCompactArrayAdders(top, true);
//
//
//


		writeToFile("inc/"+moduleFileRoot,"h");

	}

	

	private void addMessageKey(MessageField mf) {
		Number n = mf.getAnnotation(Annotation.MESSAGE_KEY_ANNOTATION).getParameter(0, Number.class);
		addLine("#define "+mf.getTypeName().deScope().append("key").toUpperSnake() + "("+n.asInt()+")");
	}
	private String m_paramList = "";
	private void makeMessageAdderSignature(MessageField mf, boolean semiNotBrace) {
		ArrayList<String> comments = new ArrayList<>();
		comments.add("A function to add a "+mf.getTypeName().deScope().toTitle());
		comments.add(mf.getComment());
		comments.add("@param message - the message buffer to add the message to");
		m_paramList = "Bb * message";
		ArrayList<Field> fs = new ArrayList<>();
		//first make a list of all top-level fields that are not strings or parent fields
		//but also add contents of boolfieldfields
		mf.getChildren().forEach(f -> {
			
			String tp = getType(f);
			
			
		
			if(tp != null && f.getTypeId() != TypeId.STRING && getArraysOrSequences(f).size() == 0) {
				fs.add(f);
				
			} 
		}, true);
		
		for(Field f : fs) {
			String stuff = "";
			ParentField pf = f.getParent();
			if(m_paramList.length() > 0) {
				m_paramList += ", ";
			}
			
			String paramName = makeName(f, false).toLowerCamel();
			String type = getType(f);
			if(f instanceof EnumField) {
				type = f.getTypeName().deScope().toUpperCamel();
			}
			m_paramList += type+" " + paramName+stuff;
			
			comments.add("@param " + paramName + prependHyphen( getFieldComment(f)));
		}
	
		
	
		
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		addLine("void "+mf.getTypeName().deScope().prepend("add").toLowerCamel()+"("+m_paramList+")" + (semiNotBrace ? ";" : "{"));
	}
	/**
	 * make getters and settings for all base types except strings
	 * @param f
	 * @param getNotSet
	 * @param protoNotDef
	 */
	private void makeMessageGetterSetterSignatures(Field f, boolean getNotSet, boolean protoNotDef) {
		String tf = getType(f);
		if(tf == null || f.getTypeId() == TypeId.STRING) {
			return;
		}
		List<ParentField> ps = getArraysOrSequences(f);
		HashMap<ParentField, String> indeces = new HashMap<>();
		//don't need a setter if it's a simple field not in an array
		if((!getNotSet) && ps.size() == 0) {
			return;
		}
		ArrayList<String> comments = new ArrayList<>();
		SymbolName fn = f.getName();
		if(fn == null) {
			fn = f.getParent().getName();
		}
		comments.add("A "+(getNotSet ? "g" : "s") + "etter for the "+fn+" field");

		if(f.getComment() != null) {
			comments.add(f.getComment());
		}
		
		
//		List<Field> fs = f.getAncestors(MessageField.class);
		String paramList = "Bb * message";
		int j = 0;
		for(ParentField pf : ps) {
			ArrayField af = pf.asType(ArrayField.class);
			SequenceField sf = pf.asType(SequenceField.class);
			String type = af != null ? "array" : "sequence";
			int[] ns = af != null ? af.getNumber() : new int[] {-1};
			for(int i = 0; i < ns.length; ++i) {
				int n = ns[i];
				String index = "i"+j;
				indeces.put(pf, index);
				
				if(n >= 0) {
					comments.add("@param "+index+" - index "+i+" of "+ pf.getName().toLowerCamel()+" "+type+". Valid values: 0 to "+(n - 1));
				} else {
					comments.add("@param "+index+" - index "+i+" of "+ pf.getName().toLowerCamel()+" "+type+".");
				}
				paramList += ", int i"+j;
				++j;
				
			}
		}
		if(!getNotSet) {
			paramList += ", "+ tf + " "+fn;
			
			comments.add("@param "+fn+prependHyphen(f.getComment()));
			
		}
		

	
		ScopeName name = makeScopeName(f);
		
		
		addDocComment(comments);
		String line = (getNotSet ? tf + " get" : "void set")+name.toSymbolName().toUpperCamel()+"("+paramList+")"+(protoNotDef ? ";" : "{");
		addLine(line);
		if(protoNotDef) {
			return;
		}
		indent();
		addLineComment("autogenerated content not done yet");
		outdent();
		addLine("}");
		return;
	}
	

	private String prependHyphen(String s) {
		String result = "";
		if(s != null && !s.isBlank()) {
			result = " - "+s;
		}
		return result;
	}
	
	/**
	 * Scans upward from the specified field to a message field and note any array fields along the way
	 * @param f
	 * @return
	 */
	private List<ParentField> getArraysOrSequences(Field f) {
		ArrayList<ParentField> result = new ArrayList<>();
		Field pf = f;
		while(pf != null) {
			if(pf instanceof ArrayField || pf instanceof SequenceField) {
				result.add((ParentField)pf);
			}
			pf = pf.getParent();
		}
		return result;
	}

	/**
	 * Traverse the parent hierarchy of this field until a message field is reached
	 * Construts a scope name from all the names up to the message and prepends the message type
	 * the rightmost scope level should be the field name
	 * the leftmost scope level should be the message name
	 * @param f
	 * @return
	 */
	private ScopeName makeScopeName(Field f) {
		ScopeName result = ScopeName.wrap(SymbolName.EMPTY);
		MessageField mf = null;
		Field ft = f;
		while(ft != null && mf == null) {
			SymbolName n = ft.getName();
			result = result.addLevelAbove(n);
			ft = ft.getParent();
			mf = ft.asType(MessageField.class);
			
		}
		
		if(mf == null) {
			throw new RuntimeException("Could not determine the message that this field is part of "+f);
		}
		result = result.addLevelAbove(mf.getTypeName().deScope());
		return result;
	}
	

	
	private String getFieldComment(Field f) {
		String result = "";
		if(f.getComment() != null) {
			result = f.getComment();
		}
		ParentField pf = f.getParent();
		while(pf != null && (!(pf instanceof MessageField))) {
			if(pf.getComment() != null) {
				result = pf.getComment() + " " + result;
			}
			pf = pf.getParent();
		}
		return result;
	}

	private String getType(Field f) {
		String result = null;
		if(true) {
		
		
			switch(f.getTypeId()) {
			
			case BOOL:
				result = "bool";
				break;
			case FLOAT32:
				result = "float";
				break;
			case FLOAT64:
				result = "double";
				break;
			case INT16:
				result = "int16_t";
				break;
			case INT32:
				result = "int32_t";
				break;
			case INT64:
				result = "int64_t";
				break;
			case INT8:
				result = "int8_t";
				break;
			case STRING:
				result = "char*";
				break;
			case UINT16:
				result = "uint16_t";
				break;
			case UINT32:
				result = "uint32_t";
				break;
			case UINT64:
				result = "uint64";
				break;
			case UINT8:
				result = "uint8_t";
				break;
			case DEFINED:
				//check if it's a defined type of a base type
				Field f2 = f;
				while(f2 instanceof DefinedTypeField) {
					f2 = ((DefinedTypeField)f2).getFirstChild();
				}
				result = getType(f2);
				break;
			case ARRAY:
			case BOOLFIELD:
			case DEFERRED:
			case MESSAGE:
			case SEQUENCE:
			case STRUCT:
				result = null;
				break;
			
			}
		}
		return result;
	}
	/**
	 * Constructs a name for this field.
	 * Basefields that are children of the message should just be named by their name
	 * BaseFields that are children of structs should have the struct name prepended
	 * Types that are in an Array type should have the array name prepended
	 * Same with types that are in a sequence
	 * 
	 * 
	 * 
	 * @param f - the field to name
	 * @param includeMessage - if true will include the message name it the final name
	 * @return
	 */
	private SymbolName makeName(Field f, boolean includeMessage) {
		SymbolName result = f.getName();
		if(result == null) {
			result = f.getParent().getName();
			
		}
		ParentField pf = f.getParent();
		while((pf != null) && (includeMessage || !(pf instanceof MessageField))) {
			SymbolName pn = null;
			pn = pf.getName();
			
			
			result = result.prepend(pn);
			pf = pf.getParent();
		}
		return result;
	}
	boolean m_bools = false;
	private void makeSourceFile(ScopeName module) {
		String moduleFileRoot = module.deScope().toLowerCamel();

		startFile(getHeader());



		addSectionDivider("Includes");
		addLine("#include <"+moduleFileRoot+".h>");

		addSectionDivider("Defines");
		addLineComment("Add message field indeces");
		//add defines for field indeces
		//also keep track of any boolfieldfields
		m_bools = false;
		m_parser.getMessages().forEachOfTypeInScope(MessageField.class, false, module, mf -> {
			mf.getChildren().forEach(f -> {
				if(f.getIndex() >= 0) {
					if(!(f instanceof BoolFieldField)) {
						if(f.getBitCount() == 1) {
							addLine("#define " + makeName(f, true).append("index").toUpperSnake() + " ("+f.getParent().getIndex()+")");
							m_bools = true;
						} else {
							addLine("#define " + makeName(f, true).append("index").toUpperSnake() + " ("+f.getIndex()+")");
						}
					}
					
				}
			}, true);
		});
		
		if(m_bools) {
			addLine();
			addLineComment("Add message boolean field masks");

			//now add defines for bit field indeces and bit masks
			m_parser.getMessages().forEachOfTypeInScope(MessageField.class, false, module, mf -> {
				mf.getChildren().forEach(f -> {
					if(f.getIndex() >= 0) {
						if(f instanceof BaseField && f.getBitCount() == 1) {
						
							addLine("#define " + makeName(f, true).append("mask").toUpperSnake() + " (1 << "+f.getIndex()+")");
	
						}
						
						
					}
				}, true);
			});
		}


		addSectionDivider("Types");



		addSectionDivider("Function Prototypes");
//		addBlockFunctionAdder(top, true);


		addSectionDivider("Source");
		
		m_parser.getMessages().forEachOfTypeInScope(MessageField.class, false, module, mf -> {
			makeMessageAdderSignature(mf, false);
			addLine("}");
		});
		
		m_parser.getMessages().forEachOfTypeInScope(MessageField.class, false, module, mf -> {
			mf.getChildren().forEach(f -> {
				
				makeMessageGetterSetterSignatures(f, true, false);
				makeMessageGetterSetterSignatures(f, false, false);
			}, true);
			
		});
		

//		addHeaderFieldGetters(top,false);
//
//		addBytesPerRepeatGetter(top, false);
//
//
//		addBaseFieldGetters(top, false);
//
//		addPacketStartFinish(top, false);
//
//		addBlockFunctionGetters(top, false);
////		addBlockFunctionAdder(top, false);
//
//		addBlockAdders(top, false);
//
//		addArrayAdders(top, false);
//		addCompactArrayAdders(top, false);
//
////		addArrayGetters(top, false);
////		addArrayElementAdders(top, true);



		writeToFile("src/"+moduleFileRoot,"c");

	}




	private void addGetterSetterGuts(Field f, boolean getNotSet) {
		
		
	}

	private void addBytesPerRepeatGetter(StructField top, boolean proto) {
//		//grab the first array field found
//		ArrayField af = getArrayFields(top).get(0);
//
//		int hl = af.getHeaderWordCount();
//		FieldName tn = af.getTypeName();
//
//		addDocComment("get the number of bytes per array element.\nThis is needed for array value getters.");
//		addLine("uint32_t getBb"+tn.toUpperCamel()+"BytesPerRepeat(Bb* buf, BbBlock currentBlock)"+(proto ? ";" : "{"));
//		if(!proto) {
//			indent();
//
//			addLine("uint32_t length = getBb"+tn.toUpperCamel()+"Length(buf, currentBlock);");
//			addLine("uint32_t repeats = getBb"+tn.toUpperCamel()+"Repeats(buf, currentBlock);");
//			addLine("repeats = repeats == 0 ? 1 : repeats; //don't allow zero or it will trigger a divide by zero hard fault below.");
//			addLine("return ((length * 4) - " + (hl * 4) + ") / repeats;");
//			closeBrace();
//		}

	}

//	private String makeBlockValueDefine(FixedIntField fif) {
//		FieldName parent = fif.getParent().getName();
//
//		return parent.addSuffix(fif.getName()).addSuffix("VALUE").toUpperSnake();
//	}


	private void writeBlockValueDefine(StructField top) {

//		top.scanThroughHeaderFields(f -> {
//			if(f instanceof FixedIntField) {
//				FixedIntField fif = (FixedIntField)f;
//
//				addBlockComment(fif.getComment());
//				addLine("#define "+makeBlockValueDefine(fif)+" ("+WriterUtils.formatAsHex(fif.getValue())+")");
//			}
//
//		}, true);

	}

	private void writeEnum(EnumField ef) {

		addDocComment(ef.getComment());
		addLine("typedef enum {");

		indent();
		for(NameValue nv : ef.getNameValues()) {


			String c = nv.getComment();
			if(c != null && !c.isBlank()) {
				c = "// "+c;
			} else {
				c = "";
			}
			addLine(makeEnumName(ef, nv) + " = " + WriterUtils.formatAsHex(nv.getValue().asLong())+", " + c);


		}
		outdent();

		addLine("} "+ef.getTypeName().deScope().toUpperCamel()+";");

		addLine();
	
	}

	private String makeEnumName(EnumField ef, NameValue nv) {
		return nv.getName().prepend(ef.getTypeName().deScope()).toUpperSnake();
	}

	private void writeBaseFieldDefines(StructField top) {
//		top.scanThroughBaseFields((f) -> {
//			writeDefine(f);
//		}, false);
	}
	private void writeHeaderDefines(StructField top) {
//
//		//first scan through header fields and get all unique ones
//		ArrayList<BaseField> hfs = new ArrayList<BaseField>();
//		top.scanThroughHeaderFields(bf -> {
//			if(bf.getName() != null) {
//				boolean found = false;
//				for(BaseField f : hfs) {
//					if(f.getName().equals(bf.getName())) {
//						if(f.getCorrectParentName().equals(bf.getCorrectParentName())){
//							found = true;
//							break;
//						}
//					}
//				}
//				if(!found) {
//					hfs.add(bf);
//				}
//			}
//		}, true);
//
//		//now write defines
//		hfs.forEach(bf -> writeDefine(bf));

	}




	private void writeDefine(BaseField bf) {
//		if(bf.getName() == null || bf.getParent() == null) {
//			return;
//		}
//		if(bf instanceof BoolFieldField) {
//			//don't do anything
//		} else if(bf instanceof CompoundField) {
//			//probably also don't do anything
//		} else if(bf instanceof BoolField) {
//			//do the byte index and the bit index
//			writeDefine(makeBaseFieldNameRoot(bf).addSuffix("INDEX").toUpperSnake(), ""+((BoolFieldField)(bf.getParent())).getIndex(),bf);
//			writeDefine(makeBaseFieldNameRoot(bf).addSuffix("BIT").toUpperSnake(), ""+bf.getIndex(),bf);
//			writeDefine(makeBaseFieldNameRoot(bf).addSuffix("MASK").toUpperSnake(), "1 << "+bf.getIndex(),bf);
//
//		} else {
//			writeDefine(makeBaseFieldNameRoot(bf).addSuffix("INDEX").toUpperSnake(), ""+bf.getIndex(),bf);
//		}
//
	}

	private void writeDefine(String name, String value, BaseField commentField) {
		String comment = commentField != null ? commentField.getComment() : "";
		if(comment == null) {
			comment = "";
		}
		boolean multiLine = comment.split("\\R").length > 1 ;
		String c = "";
		if(multiLine) {
			addBlockComment(comment);
		} else {
			String s = comment;
			if(!s.isBlank()) {
				c = "    //"+comment;
			}
		}
		addIndent();
		add("#define ");
		add(name);
		add(" (" + value + ")");
		if(!c.isEmpty()) {
			add(c);
		}
		addLine();

	}

	private void addHeaderFieldGetters(StructField top, boolean protoNotDeclaration) {
//		ArrayList<StructField> bfs = new ArrayList<StructField>();
//		//first find all blockfields with unique types
//		top.scanThroughBlockFields((bf) -> {
//			boolean found = false;
//			for(StructField bft : bfs) {
//				if(bft.getTypeName().equals(bf.getTypeName())) {
//					found = true;
//					break;
//				}
//			}
//			if(!found) {
//				bfs.add(bf);
//			}
//		});
//
//		//now do the stuff
//		for(StructField bf : bfs) {
//			bf.scanThroughHeaderFields(f -> {
//				if(f.getName() != null) {
//					addBaseGetter(f, protoNotDeclaration, false);
//					if(!protoNotDeclaration) {
//						if(f.getName() != null) {
//							indent();
//							String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//							addBaseGetterGuts(f, dn, "return ", false);
//							closeBrace();
//						}
//					}
//				}
//			}, false);
//		}

	}

	private void addBaseFieldGetters(StructField top, boolean protoNotDeclaration) {
//		ArrayList<StructField> bfs = new ArrayList<StructField>();
//		top.scanThroughBlockFields(bf -> {
////			if(!(bf instanceof ArrayField)) {
//				bfs.add(bf);
////			}
//		});
//		for(StructField bf : bfs) {
//			for(BaseField f : bf.getNamedBaseFields()) {
//				if(f instanceof BoolField) {
//					addBoolGetter((BoolField)f, protoNotDeclaration);
//
//				} else if(f instanceof CompoundField) {
//					addCompoundGetterPrototype((CompoundField)f, top, protoNotDeclaration);
//				} else {
//					addBaseGetter(f, protoNotDeclaration, true);
//					if(!protoNotDeclaration) {
//						if(f.getName() != null) {
//							indent();
//							String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//							addBaseGetterGuts(f, dn, "return ", true);
//							outdent();
//							addLine("}");
//
//						}
//					}
//				}
//			}
//		}

	}


//	private void addBoolGetter(BoolField b, boolean protoNotDeclaration) {
//
//		addBaseGetter(b, protoNotDeclaration, true);
//		if(!protoNotDeclaration) {
//			indent();
//			String dn = makeBaseFieldNameRoot(b).addSuffix("INDEX").toUpperSnake();
//			addBoolGetterGuts(b, dn, "return ");
//			outdent();
//			addLine("}");
//		}
//
//
//
//
//	}

//	private void addBaseGetter(BaseField f, boolean protoNotDeclaration, boolean addBytesPerRepeat) {
//		if(f.getName() != null) {
//
//			String gs = "get";
//			String c = f.getName().toLowerCamel()+" field of the " + f.getCorrectParentName().toLowerCamel() + " " + f.getCorrectParentName().toUpperCamel()+ "\n"+f.getComment();
//			String rt = getBaseType(f);
//			String function = makeBaseFieldNameRoot(f).toUpperCamel();
//
//			boolean array = false;
//
//			String arrayComment = "";
//			if(addBytesPerRepeat && (f.getParent()) instanceof ArrayField) {
//				array = true;
//				arrayComment = "@param i - index of array item to get\n"+
//								"@param bytesPerRepeat - number of bytes in each array repeated element";
//
//			}
//
//			addDocComment( gs + "s the " + c + "\n"+
//					"@param buf - the buffer containing the packet\n" +
//					"@param currentBlock - the index of the block we're interested in\n"+
//					arrayComment
//					);
//
//
//
//			String s = rt;
//			String arrayParam = array ? ", uint32_t i, uint32_t bytesPerRepeat" : "";
//			s += " " + gs + "Bb" + function + "(Bb* buf, BbBlock currentBlock"+arrayParam+")";
//
//			s += protoNotDeclaration ? ";" : "{";
//			addLine(s);
//
//
//		}
//	}
//	private boolean isInArray(BaseField f) {
//		StructField bf = (StructField)f.getParent();//this must be true I think
//		return (bf instanceof ArrayField);
//	}
//	private void addBaseSetterGuts(BaseField f) {
//		String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//		addBaseSetterGuts(f, dn);
//	}
//	private void addBaseSetterGuts(BaseField f, String index, String value) {
//		String rt = getBaseType(f);
//		String paramName = f.getCorrectParentName().toLowerCamel();
//
//		String functionName = FieldName.fromSnake(f.getType().name()).addPrefix("bb").addPrefix("set").toLowerCamel();
//		addLine(functionName + "(buf, currentBlock, " + index + ", " + value + ");");
//	}
//	private void addBaseGetterGuts(BaseField f) {
//		String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//		addBaseGetterGuts(f, dn);
//	}
//	private void addBaseGetterGuts(BaseField f, String index, String start, boolean doArrayStuff) {
//		String rt = getBaseType(f);
//		String paramName = f.getCorrectParentName().toLowerCamel();
//
//		ArrayField af = null;
//
//		String arrayParms = "";
//		String arrayComment = "";
//		Field p = f.getParent();
//		if(p instanceof ArrayField && doArrayStuff) {
//			af = (ArrayField)p;
//
//			arrayParms = " + (i * bytesPerRepeat)";
//
//
//			arrayComment = " //magic number represents the number of bytes in each array rep";
//		}
//
//
//
//
//		String functionName = FieldName.fromSnake(f.getType().name()).addPrefix("bb").addPrefix("get").toLowerCamel();
//		if(f instanceof EnumType) {
//			functionName = "("+rt+")" + functionName;
//		}
//		addLine(start + functionName + "(buf, currentBlock , " + index +  arrayParms +");");
//
//
//
//	}
//	private void addBoolSetterGuts(BaseField f) {
//		String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//
//		addBoolSetterGuts(f, dn);
//	}
//	private void addBoolSetterGuts(BaseField f, String index, String value) {
//		String paramName = f.getCorrectParentName().toLowerCamel();
//		String functionName = FieldName.fromSnake(f.getType().name()).addPrefix("set","bb").toLowerCamel();
//		String dbn = makeBaseFieldNameRoot(f).addSuffix("MASK").toUpperSnake();
//		addLine(functionName + "(buf, currentBlock, " + index + ", " + dbn + ", " + value + ");");
//	}
//	private void addBoolGetterGuts(BaseField f) {
//		String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//		addBoolGetterGuts(f, dn);
//	}
//	private void addBoolGetterGuts(BaseField f, String index, String start) {
//		String paramName = f.getCorrectParentName().toLowerCamel();
//		String dbn = makeBaseFieldNameRoot(f).addSuffix("MASK").toUpperSnake();
//		String functionName = FieldName.fromSnake(f.getType().name()).addPrefix("get", "bb").toLowerCamel();
//		addLine(start + functionName + "(buf, currentBlock , " + index + ", " + dbn + ");");
//
//	}

	private void addBlockFunctionAdder(StructField top, boolean protoNotDeclaration) {
//		ArrayList<StructField> fields = new ArrayList<StructField>();
//		//first scan for all blockfields with unique type names
//		top.scanThroughBlockFields(bf -> {
//			if(bf != top) {
//				boolean found = false;
//				for(StructField f : fields) {
//
//					if(f.getTypeName().equals(bf.getTypeName())) {
//						found = true;
//						break;
//					}
//				}
//				if(!found) {
//					fields.add(bf);
//				}
//			}
//		});
//
//		//now build the functions using the first block.
//		if(fields.size() > 0) {
//			StructField bf = fields.get(0);
//			FieldName tn = bf.getTypeName();
//			FieldName functionName = tn.addPrefix("next").addPrefix("add");
//			String f = "BbBlock " + functionName.toLowerCamel()+"(Bb* buf, BbBlock block)";
//			addDocComment("computes the index of the next new block given the previous one.");
//			BaseField lf = null; //length field
//			for(BaseField bft : bf.getHeaderFields()) {
//				if(bft instanceof CompoundField) {
//					CompoundField cf = (CompoundField)bft;
//					for(BaseField bf2 : cf.getBaseFields()) {
//						if(bf2.getName() != null && bf2.getName().toLowerCamel().equals("length")) {
//							lf = bf2;
//							break;
//						}
//					}
//				}
//				if(lf != null) {
//					break;
//				} else if(bft.getName() != null && bft.getName().toLowerCamel().equals("length")) {
//					lf = bft;
//					break;
//				}
//			}
//
//			if(lf == null || lf.getName() == null) {
//				throw new RuntimeException("No length field found!");
//			}
//
//			if(protoNotDeclaration) {
//				addLine(f + ";");
//			} else {
//				addLine(f + "{");
//				indent();
////				addLine();
//				//build the function name
////				BbBlock addPayload(Bb* buf, BbBlock prevPayload){
//
//				//Packet packet = buf->start;
//				//uint16_t pLen = getUint16(buf, PACKET_LENGTH_INDEX);
//				//Payload
//
//				addLine(top.getTypeName().toUpperCamel()+" p = buff->start;");
//				String lenType = getBaseType(lf);
////				addLine(lenType + " pLen = "+new FieldName("get",lenType).toLowerCamel()+"(buf, ");
//
//
//
//				String lgn = tn.addPrefix("get").addSuffix(lf.getName()).toLowerCamel();
////				String ldn =
//				addLine(getBaseType(lf) + " len = " + lgn + "(buf,  block);");//this gets the block length
//
//				addLine("return block + len;");
//				outdent();
//				addLine("}");
//			}
//
//
//
//
//
//		}
	}

	private void addBlockFunctionGetters(StructField top, boolean protoNotDeclaration) {
//		ArrayList<StructField> fields = new ArrayList<StructField>();
//		//first scan for all blockfields with unique type names
//		top.scanThroughBlockFields(bf -> {
//			if(bf != top) {
//				boolean found = false;
//				for(StructField f : fields) {
//
//					if(f.getTypeName().equals(bf.getTypeName())) {
//						found = true;
//						break;
//					}
//				}
//				if(!found) {
//					fields.add(bf);
//				}
//			}
//		});
//
//		//now build the functions using the first block.
//		if(fields.size() > 0) {
//			StructField bf = fields.get(0);
//			FieldName tn = bf.getTypeName();
//			FieldName getterName = tn.addPrefix("get", "Bb", "next");
//			FieldName setterName = tn.addPrefix("set","Bb", "next");
//			String f = "BbBlock " + getterName.toLowerCamel()+"(Bb* buf, BbBlock block)";
//			addDocComment("computes the index of the next block given the previous one.");
//			BaseField lf = bf.getHeaderField("length");
//
//
//
//			if(lf == null || lf.getName() == null) {
//				throw new RuntimeException("No length field found!");
//			}
//
//			if(protoNotDeclaration) {
//				addLine(f + ";");
//			} else {
//				addLine(f + "{");
//				indent();
////				addLine();
//				//build the function name
//				String lgn = tn.addPrefix("get","bb").addSuffix(lf.getName()).toLowerCamel();
////				String ldn =
//				addLine("uint32_t" + " len = ((uint32_t)" + lgn + "(buf,  block)) * 4; //get length in words and convert to length in bytes");//this gets the block length
//				addLine("return block + len;");
//				outdent();
//				addLine("}");
//			}
//
//
//
//
//
//		}
	}


//	private String getBaseType(BaseField f) {
//		String rt = "";
//
//		BaseType t = f.getType();
//		if(f instanceof EnumType) {
//			rt = getEnumTypeName((EnumType)f);
//		} else {
//
//			switch(t) {
//			case COMPOUND:
//			case ARRAY:
//			case BLOCK:
//				break;
//			case BOOL:
//				rt = "bool";
//				break;
//			case BOOLFIELD:
//				rt = "uint8_t";
//				break;
//			case FLOAT32:
//				rt = "float";
//				break;
//			case INT16:
//				rt = "int16_t";
//				break;
//			case INT32:
//				rt = "int32_t";
//				break;
//			case INT8:
//				rt = "int8_t";
//				break;
//			case UINT16:
//				rt = "uint16_t";
//				break;
//			case UINT32:
//				rt = "uint32_t";
//				break;
//			case UINT8:
//				rt = "uint8_t";
//				break;
//			default:
//				break;
//
//			}
//		}
//		return rt;
//	}


	
//	private void addBlockKeyDefines(StructField top) {
//		List<FixedIntField> keys = getBlockKeys(top);
//
//		for(FixedIntField key : keys) {
//			String name = makeBaseFieldNameRoot(key).toUpperSnake();
//			writeDefine(name, ""+key.getValue(), key);
//		}
//	}

//	private void addBlockKeyEnum(StructField top) {
//		List<FixedIntField> keys = getBlockKeys(top);
//		addLine("typedef enum {");
//		indent();
//		for(FixedIntField key : keys) {
////			String name = makeBaseFieldNameRoot(key).toUpperSnake();
//			String name = makeKeyName(key);
//			addLine(name + " = "+WriterUtils.formatAsHex(key.getValue())+",");
//		}
//		outdent();
//		addLine("} BlockKeys;");
//		addLine();
//	}
//	private void addBlockAdders(StructField top, boolean protoNotDeclaration) {
////		//first get all blocks that we want to make adders for
////		List<StructField> bfs = top.getAllBlockFields();
////
////		for(StructField bf : bfs) {
////			addBlockAdder(bf, true, protoNotDeclaration);
////			addBlockAdder(bf, false, protoNotDeclaration);
////		}
//	}

	private void addArrayGetters(StructField top, boolean protoNotDeclaration) {
//		List<ArrayField> afs = top.getAllArrayFields();
//		for(ArrayField af : afs) {
//			addArrayGetter(af, protoNotDeclaration);
//		}
	}
//	private void addArrayGetter(ArrayField bf, boolean protoNotDeclaration) {
//		String blockName = bf.getName().toUpperCamel();
//		String comment = "Adds a new "+blockName+" to the specified packet.\n"+bf.getComment();
//		String functionName = "getBb"+blockName;
//		List<BaseField> fs = bf.getNamedBaseFields();
//		String paramList = "";
//
//
//		for(BaseField f : fs) {
//			paramList += ", "+getBaseType(f)+"* "+f.getName().toLowerCamel();
//		}
//
//		addDocComment(comment);
//		addLine("void "+functionName+"(Bb* buf, BbBlock currentBlock, uint32_t n"+paramList+")"+(protoNotDeclaration ? ";" : "{"));
//		if(!protoNotDeclaration) {
//			indent();
//
//			//now fill in the details
//			BaseField lf = bf.getHeaderField("length");
//			BaseField keyField = bf.getHeaderField("key");
//
//			FieldName tn = bf.getTypeName();
//
//			int blockLen = bf.getHeaderWordCount() + bf.getBaseWordCount();
//
//			if(lf == null) {
//				throw new RuntimeException("No length field found!");
//			}
//			if(keyField == null) {
//				throw new RuntimeException("No key field found!");
//			}
//
//			String keyValue = makeBaseFieldNameRoot(keyField).toUpperSnake();
//			String keyIndex = makeBaseFieldNameRoot(keyField).addSuffix("INDEX").toUpperSnake();
//			String keyFuncName = FieldName.fromCamel("setBb").addSuffix(keyField.getType().name()).toLowerCamel();
//
//			String lenValue = ""+blockLen;
//			String lenIndex = makeBaseFieldNameRoot(lf).addSuffix("INDEX").toUpperSnake();
//			String lenFuncName = FieldName.fromCamel("setBb").addSuffix(lf.getType().name()).toLowerCamel();
//
//
//
//
//
//
//
//
//
//
//
//			//then do the params
//
//			addLineComment("Add base fields");
//			addLine("for(uint32_t i = 0; i < n; ++i){");
//			indent();
//			for(BaseField f : fs) {
//				String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//				String value = f.getName().toLowerCamel()+"[i] = ";
//
//				if(fs.size() == 1) {
//					dn += " + i ";
//				} else {
//					dn += " + (i * "+fs.size()+")";
//				}
//				if(f instanceof BoolField) {
//
//
//					addBoolGetterGuts(f, dn, value);
//				} else {
//					addBaseGetterGuts(f, dn, value, true);
//				}
//			}
//			outdent();
//			addLine("}");
//
//
//
//
//
//			//update the block length
//
//
//			//return the new block index
////			addLine("return result;");
//
//
//			outdent();
//			addLine("}");
//		}
//	}
	private void addPacketStartFinish(StructField top, boolean protoNotDeclaration) {
//		String blockName = top.getName().toUpperCamel();
//		List<BaseField> fs = top.getHeaderFields();
//		String topLineEnd = protoNotDeclaration ? ";" : "{";
//		int headerLength = top.getHeaderWordCount();
//
//		addDocComment("Add packet header.\nThis only adds a placeholder - make sure you finish the packet."+top.getComment());
//		addLine("BbBlock start"+blockName+"(Bb* buf)"+topLineEnd);
//		if(!protoNotDeclaration) {
//			indent();
//			addLine("return "+headerLength*4+";");
//
//
//			closeBrace();
//		}
//		addDocComment("Add packet header.\nThis only adds a placeholder - make sure you finish the packet."+top.getComment());
//		addLine("void finish"+blockName+"(Bb* buf, BbBlock bb)"+topLineEnd);
//		if(!protoNotDeclaration) {
//			indent();
//
//			FixedIntField preamble = (FixedIntField)top.getHeaderField("preamble");
//			BaseField length = top.getHeaderField("length");
//			BaseField crc = top.getHeaderField("crc");
//
//			String preambleIndex = makeBaseFieldNameRoot(preamble).addSuffix("INDEX").toUpperSnake();
//			String lengthIndex = makeBaseFieldNameRoot(length).addSuffix("INDEX").toUpperSnake();
//			String crcIndex = makeBaseFieldNameRoot(crc).addSuffix("INDEX").toUpperSnake();
//
//			String preambleSetter = FieldName.fromCamel("setBb").addSuffix(preamble.getType().name()).toLowerCamel();
//			String lengthSetter = FieldName.fromCamel("setBb").addSuffix(length.getType().name()).toLowerCamel();
//			String crcSetter = FieldName.fromCamel("setBb").addSuffix(crc.getType().name()).toLowerCamel();
//
//			String preambleVal = makeBlockValueDefine(preamble);
//			String lengthVal = "bb>>2";//(buf->start - bb)>>2";
//			String start = top.getName().addSuffix("first","block","index").toUpperSnake();
//			String crcVal = "computeCrc(buf, "+start+", bb)";
//
//			addLine(preambleSetter+"(buf, 0, "+preambleIndex+", "+preambleVal+");");
//			addLine(lengthSetter+"(buf, 0, "+lengthIndex+", "+lengthVal+");");
//			addLine(crcSetter+"(buf, 0, "+crcIndex+", "+crcVal+");");
//
//
//			closeBrace();
//		}
	}
	private void addBlockAdder(StructField bf, boolean withParamsNotWithout, boolean protoNotDeclaration) {
//		String blockName = bf.getName().toUpperCamel();
//		String comment = "Adds a new "+blockName+" to the specified packet.\n"+bf.getComment();
//		String functionName = "add"+(withParamsNotWithout ? "" : "Empty")+"Bb"+blockName;
//		List<BaseField> fs = bf.getNamedBaseFields();
//		String paramList = "";
//		if(withParamsNotWithout && fs.size() == 0) {
//			//don't do anything if this block does not have parameters but we're doing the version with params
//			return;
//		}
//		if(withParamsNotWithout) {
//			for(BaseField f : fs) {
//				paramList += ", "+getBaseType(f)+" "+f.getName().toLowerCamel();
//			}
//		}
//		addDocComment(comment);
//		addLine("BbBlock "+functionName+"(Bb* buf, BbBlock currentBlock"+paramList+")"+(protoNotDeclaration ? ";" : "{"));
//		if(!protoNotDeclaration) {
//			indent();
//
//			//now fill in the details
//			BaseField lf = bf.getHeaderField("length");
//			FixedIntField keyField = (FixedIntField)bf.getHeaderField("key");
//
//			FieldName tn = bf.getTypeName();
//
//			int blockLen = bf.getHeaderWordCount() + (withParamsNotWithout ? bf.getBaseWordCount() : 0);
//
//			if(lf == null) {
//				throw new RuntimeException("No length field found!");
//			}
//			if(keyField == null) {
//				throw new RuntimeException("No key field found!");
//			}
//
//			String keyValue = makeBlockValueDefine(keyField);
//			String keyIndex = makeBaseFieldNameRoot(keyField).addSuffix("INDEX").toUpperSnake();
//			String keyFuncName = FieldName.fromCamel("setBb").addSuffix(keyField.getType().name()).toLowerCamel();
//
//			String lenIndex = makeBaseFieldNameRoot(lf).addSuffix("INDEX").toUpperSnake();
//			String lenFuncName = FieldName.fromCamel("setBb").addSuffix(lf.getType().name()).toLowerCamel();
//
//
//
//
////			//first setup index
////			addLineComment("Compute index of new block");
//////			addLine(bf.getTypeName().toUpperCamel()+" p = buff->start;");
////			String lenType = getBaseType(lf);
////
////
////
////			String lgn = tn.addPrefix("get","bb").addSuffix(lf.getName()).toLowerCamel();
////			addLine(getBaseType(lf) + " len = " + lgn + "(buf,  currentBlock);");//this gets the block length
////			addLine("BbBlock result = bbWrap(buf, currentBlock + len);");
//
//
//
//
//
//			//first do the header stuff
//			addLine();
//			addLine("uint32_t blockOffset = "+blockLen + ";//sorry about the magic number. This is computed by the schema parser");
//			addLineComment("Add header fields");
//			//write the key
//			addLine(keyFuncName+"(buf, currentBlock, "+keyIndex+", "+keyValue+");");
//			//write the length
//			addLine(lenFuncName+"(buf, currentBlock, "+lenIndex+", blockOffset);");
//
//
//			//then do the params if we're doing params
//			if(withParamsNotWithout) {
//				addLine();
//				addLineComment("Add base fields");
//
//				for(BaseField f : fs) {
////					String value = f.getName().toLowerCamel();
////					String index = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
////					String funcName = FieldName.fromCamel("setBb").addSuffix(keyField.getType().name()).toLowerCamel();
////					addLine(funcName+"(buf, result, "+index+", "+value+");");
//					String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//					String value = f.getName().toLowerCamel();
//
//					if(f instanceof BoolField) {
//						addBoolSetterGuts(f, dn, value);
//					} else {
//						addBaseSetterGuts(f, dn, value);
//					}
//				}
//
//
//			}
//
//
//
//			//update the block length
//
//
//			//return the new block index
//			addLine("return currentBlock + (blockOffset * 4);");
//
//
//			outdent();
//			addLine("}");
//		}
	}


	private void addFirstBlockDefine(StructField top) {
//		String n = top.getName().addSuffix("first","block","index").toUpperSnake();
//		addBlockComment("This defines the starting position of the first block after the packet header");
//		addLine("#define "+n+" ("+top.getHeaderWordCount()*4+")");
	}
	private void addArrayAdders(StructField top, boolean protoNotDeclaration) {
//		List<ArrayField> afs = top.getAllArrayFields();
//		for(ArrayField af : afs) {
//			addArrayAdder(af, protoNotDeclaration);
//			addArrayElementAdder(af, protoNotDeclaration);
//		}
	}
	private void addCompactArrayAdders(StructField top, boolean protoNotDeclaration) {
//		List<CompactArrayField> afs = top.getAllCompactArrayFields();
//		for(CompactArrayField af : afs) {
//			addCompactArrayAdder(af, protoNotDeclaration);
//			addCompactArrayElementAdder(af, protoNotDeclaration);
//		}
	}

//	private void addArrayAdder(ArrayField bf, boolean protoNotDeclaration) {
//		String blockName =  bf.getName().toUpperCamel();
//		String comment = "Adds a new "+blockName+" to the specified packet.\n"+bf.getComment();
//		String functionName = "addBb"+blockName;
//		List<BaseField> fs = bf.getNamedBaseFields();
//		String paramList = "";
//
//
//
//
//		addDocComment(comment);
//		addLine("BbBlock "+functionName+"(Bb* buf, BbBlock currentBlock, uint32_t n)"+(protoNotDeclaration ? ";" : "{"));
//		if(!protoNotDeclaration) {
//			indent();
//
//			//now fill in the details
//			BaseField lf = bf.getHeaderField("length");
//			FixedIntField keyField = (FixedIntField)bf.getHeaderField("key");
//			BaseField rf = bf.getHeaderField("repeats");
//
//			FieldName tn = bf.getTypeName();
//
//			int blockLen = bf.getHeaderWordCount() + bf.getBaseWordCount();
//
//			if(lf == null) {
//				throw new RuntimeException("No length field found!");
//			}
//			if(keyField == null) {
//				throw new RuntimeException("No key field found!");
//			}
//
//			String keyValue = makeBlockValueDefine(keyField);
//			String keyIndex = makeBaseFieldNameRoot(keyField).addSuffix("INDEX").toUpperSnake();
//			String keyFuncName = FieldName.fromCamel("setBb").addSuffix(keyField.getType().name()).toLowerCamel();
//
//			String lenValue = ""+blockLen;
//			String lenIndex = makeBaseFieldNameRoot(lf).addSuffix("INDEX").toUpperSnake();
//			String lenFuncName = FieldName.fromCamel("setBb").addSuffix(lf.getType().name()).toLowerCamel();
//
//			String repeatValue = "n";
//			String repeatIndex = makeBaseFieldNameRoot(rf).addSuffix("INDEX").toUpperSnake();
//			String repeatFuncName = FieldName.fromCamel("setBb").addSuffix(lf.getType().name()).toLowerCamel();
//
//
//			//first setup index
//			addLineComment("Compute index of new block");
////			addLine(bf.getTypeName().toUpperCamel()+" p = buff->start;");
//			String lenType = getBaseType(lf);
//
//
//
//			String lgn = tn.addPrefix("get","bb").addSuffix(lf.getName()).toLowerCamel();
////			addLine(getBaseType(lf) + " len = " + lgn + "(buf,  currentBlock);");//this gets the block length
////			addLine("BbBlock result = bbWrap(buf, currentBlock + len);");
//
//
//
//
//
//			//first do the header stuff
//			addLine();
//
//			addLine("uint32_t blockOffset = "+bf.getHeaderWordCount()+" + (n * "+bf.getBaseWordCount()+");//a couple magic numbers: header length plus n * base fields length, generated by schema parser");
//
//
//
//			addLineComment("Add header fields");
//			//write the key
//			addLine(keyFuncName+"(buf, currentBlock, "+keyIndex+", "+keyValue+");");
//			//write the length
//			addLine(lenFuncName+"(buf, currentBlock, "+lenIndex+", blockOffset);");
//			//write the repeats field
//			addLine(lenFuncName+"(buf, currentBlock, "+repeatIndex+", "+repeatValue+");");
//
//
//
//
//
//
//			//return the new block index
//			addLine("return currentBlock + (blockOffset * 4);");
//
//
//			outdent();
//			addLine("}");
//		}
//	}

//	private void addArrayElementAdder(ArrayField bf, boolean protoNotDeclaration) {
//
//		List<BaseField> fs = bf.getNamedBaseFields();
//
//		//do this once for each base field
//		for(BaseField f : fs) {
//			String blockName =  bf.getName().toUpperCamel();
//			String comment = "sets an element into the specified "+blockName+" in the specified packet.\n"+bf.getComment();
//			String functionName = bf.getName().addPrefix("set","bb").addSuffix(f.getName()).toLowerCamel();
//			String paramName = f.getName().toLowerCamel();
//			String paramType = getBaseType(f);
//
//			addDocComment(comment);
//			addLine("void "+functionName+"(Bb* buf, BbBlock currentBlock, uint32_t i, "+paramType+" "+paramName+")"+(protoNotDeclaration ? ";" : "{"));
//			if(!protoNotDeclaration) {
//				indent();
//
//
//
//				//then do the params
//
//
//				String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//
//				dn += " + (i * "+fs.size()*4+")";
//
//
//				String value = paramName;
//
//				if(f instanceof BoolField) {
//					addBoolSetterGuts(f, dn, value);
//				} else {
//					addBaseSetterGuts(f, dn, value);
//				}
//
//				//update the block length
//
//
//				//return the new block index
//
//
//				closeBrace();
//			}
//		}

//	}

}

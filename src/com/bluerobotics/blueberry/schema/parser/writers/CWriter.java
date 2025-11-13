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
import java.util.List;
import java.util.function.Function;

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
import com.bluerobotics.blueberry.schema.parser.fields.StringField;
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
			makeMessageAdder(mf, true);
		});
		m_parser.getMessages().forEachOfTypeInScope(MessageField.class, false, module, mf -> {
			mf.getChildren().forEach(f -> {
				
				makeMessageGetterSetter(f, mf, true, true);
				makeMessageGetterSetter(f, mf, false, true);
			}, true);
			
			mf.getChildren().forEachOfType(StringField.class, true, sf -> {
				makeStringCopier(sf, mf, true, true);
				makeStringCopier(sf, mf, false, true);
				makeStringLengthGetter(sf, mf, true);
			});
			
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
				if(getType(f) != null) {
					if(!(f instanceof BoolFieldField)) {
						if(f.getBitCount() == 1) {
							addLine("#define " + makeFieldIndexName(f) + " ("+f.getParent().getIndex()+")");
							m_bools = true;
						} else {
							addLine("#define " + makeFieldIndexName(f) + " ("+f.getIndex()+")");
						}
					}
					
				} else {
					if(f instanceof ArrayField) {
						m_arrays = true;
					} else if(f instanceof SequenceField) {
						m_sequences = true;
						addLine("#define " + makeFieldIndexName(f) + " ("+f.getIndex()+")");
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
						
							addLine("#define " + makeBooleanMaskName(f) + " (1 << "+f.getIndex()+")");
	
						}
						
						
					}
				}, true);
			});
		}
		if(m_arrays) {
			addLine();
			addLineComment("Add array sizes and element byte count");
			m_parser.getMessages().forEachOfTypeInScope(MessageField.class, false, module, mf -> {
				mf.getChildren().forEachOfType(ArrayField.class, true, af -> {
					int[] is = af.getNumber();
					if(is.length == 1) {
						addLine("#define " + makeArraySizeName(af, -1) + " ("+is[0]+")");
					} else {
						for(int i = 0; i < is.length; ++i) {
							addLine("#define " + makeArraySizeName(af, i) + " ("+is[i]+")");
						}
					}
					addLine("#define " + makeArrayElementByteCountName(af) + " ("+af.getPaddedByteCount()+")");
				});
			});
		}
		if(m_sequences) {
			addLine();
			addLineComment("Add sequence element byte count");
			m_parser.getMessages().forEachOfTypeInScope(MessageField.class, false, module, mf -> {
				mf.getChildren().forEachOfType(SequenceField.class, true, sf -> {
					if(sf.getPaddedByteCount() == 0) {
						addLine("blahblah");
					}
					addLine("#define " + makeArrayElementByteCountName(sf) + " ("+sf.getPaddedByteCount()+")");

				});
			});
			

		}
		addLine();
		addLineComment("Add message lengths");
		m_parser.getMessages().forEachOfTypeInScope(MessageField.class, false, module, mf -> {
			addLine("#define " + makeMessageLengthName(mf) + " (" + mf.getPaddedByteCount()+")");
		});


		addSectionDivider("Types");



		addSectionDivider("Function Prototypes");
//		addBlockFunctionAdder(top, true);


		addSectionDivider("Source");
		
	
		
		m_parser.getMessages().forEachOfTypeInScope(MessageField.class, false, module, mf -> {
			makeMessageAdder(mf, false);
			mf.getChildren().forEach(f -> {
				
				makeMessageGetterSetter(f, mf, true, false);
				makeMessageGetterSetter(f, mf, false, false);
			}, true);
			
			mf.getChildren().forEachOfType(StringField.class, true, sf -> {
				makeStringCopier(sf, mf, true,  false);
				makeStringCopier(sf, mf, false, false);
				makeStringLengthGetter(sf, mf, false);
			});
			
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


	



	private String makeFieldIndexName(Field f) {
		return makeName(f, true).append("index").toUpperSnake();
	}

	private String makeBooleanMaskName(Field f) {
		if(f.getBitCount() != 1) {
			throw new RuntimeException("This should only be used for boolean fields, not this one: "+f);
		}
		return makeName(f, true).append("mask").toUpperSnake();
	}

	private String makeArraySizeName(ArrayField af, int i) {
		SymbolName result = makeName(af, true).append("size");
		if(i >= 0) {
			result = result.append("" + i);
		}
		return  result.toUpperSnake();
	}
	private String makeArrayElementByteCountName(ParentField af) {
		SymbolName result = makeName(af, true).append("element", "byte", "count");
	
		return  result.toUpperSnake();
	}

	private void addMessageKey(MessageField mf) {
		Number n = mf.getAnnotation(Annotation.MESSAGE_KEY_ANNOTATION).getParameter(0, Number.class);
		addLine("#define "+makeMessageKeyName(mf) + "("+n.asInt()+")");
	}
	private String makeMessageKeyName(MessageField mf) {
		return mf.getTypeName().deScope().append("key").toUpperSnake();
	}
	private String makeMessageLengthName(MessageField mf) {
		return mf.getTypeName().deScope().append("length").toUpperSnake();
	}
	private String m_paramList = "";
	
	private void makeMessageAdder(MessageField mf, boolean protoNotDef) {
		ArrayList<String> comments = new ArrayList<>();
		comments.add("A function to add a "+mf.getTypeName().deScope().toTitle());
		comments.add(mf.getComment());
		
		comments.add("@param buf - the message buffer to add the message to");
		comments.add("@param msg - the index of the start of the message");
		m_paramList = "Bb * buf, BbBlock msg";
		ArrayList<Field> fs = new ArrayList<>();
		ArrayList<Field> ss = new ArrayList<>();
		//first make a list of all top-level fields that are not strings or parent fields
		//but also add contents of boolfieldfields
		mf.getChildren().forEach(f -> {
			
			String tp = getType(f);
			
			
		
			if(tp != null && f.getTypeId() != TypeId.STRING && getIndeces(f).size() == 0) {
				fs.add(f);
				
			} else if(f.getTypeId() == TypeId.STRING || f.getTypeId() == TypeId.SEQUENCE) {
				//build a list of all sequences and strings that are not in sequences
				boolean notInSequence = true;
				for(Index i : getIndeces(f)) {
					if(!i.arrayNotSequence) {
						notInSequence = false;
						break;
					}
				}
					
					
				if(notInSequence) {
					ss.add(f);
				}
			}
		}, true);
		
		for(Field f : fs) {
			String stuff = "";
			
			m_paramList += ", ";
			
			
			String paramName = makeName(f, false).toLowerCamel();
			String type = getType(f);
			if(f instanceof EnumField) {
				type = f.getTypeName().deScope().toUpperCamel();
			}
			m_paramList += type+" " + paramName+stuff;
			
			comments.add("@param " + paramName + prependHyphen( getFieldComment(f)));
		}
	
		
		comments.add("@returns - the index of the next byte after this message.");
		
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		addLine("BbBlock "+mf.getTypeName().deScope().prepend("add").toLowerCamel()+"("+m_paramList+")" + (protoNotDef ? ";" : "{"));
		if(protoNotDef) {
			return;
		}
		//now do contents of function
		indent();
		
		for(Field f : fs) {
				
			String paramName = makeName(f, false).toLowerCamel();

			

			
			
			addLine(lookupBbGetSet(f, false)+"(buf, msg, "+makeFieldIndexName(f)+", "+paramName+");");
			
			
		}
		for(Field f : ss) {
			List<Index> pis = getIndeces(f);
			if(pis.size() == 0) {
				//zero the index field of each string and sequence
				String s = (f.getTypeId() == TypeId.SEQUENCE) ? "sequence" : "string";
				addLine("setUint16(buf, msg, "+makeFieldIndexName(f)+", 0);//clear "+s+" header");
			} else {
				//TODO: cycle through all the permutations of indeces and zero all the sequence headers
				//TODO: this is not right yet
				int[] ii = new int[pis.size()];
				int n = 1;
				for(Index pi : pis) {
					n *= pi.n;
				}
				int i = 0;
				int m = 1;
				while(i < n) {
					boolean carry = true;
					int offset = 0;
					for(int j = pis.size() - 1; j >= 0; --j) {
						offset += ii[j] * m;
						m *= pis.get(j).n;
					}				
					String s = (f.getTypeId() == TypeId.SEQUENCE) ? "sequence" : "string";
					addLine("setUint16(buf, msg, "+makeFieldIndexName(f)+" + "+offset+", 0);//clear "+s+" header. Note magic number. Sorry.");
					for(int j = pis.size() - 1; j >= 0; --j) {
						if(carry) {
							++ii[j];
							if(ii[j] >= pis.get(j).n) {
								ii[j] = 0;
								carry = true;
							} else {
								carry = false;
							}
						}
					}
					
					++i;
				}
				
				
			}
			
		}
		addLine("buf->length = msg + "+makeMessageLengthName(mf));
		addLine("return buf->length;");
		
		outdent();
		addLine("}");
	}
	/**
	 * make getter or setter for all base types except strings
	 * @param f
	 * @param getNotSet
	 * @param protoNotDef
	 */
	private void makeMessageGetterSetter(Field f, MessageField mf, boolean getNotSet, boolean protoNotDef) {
		String tf = getType(f);
		if(tf == null || f.getTypeId() == TypeId.STRING) {
			return;
		}
		List<Index> pis = getIndeces(f);
		
		//don't need a setter if it's a simple field not in an array
		if((!getNotSet) && pis.size() == 0) {
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
		String paramList = "Bb * buf, BbBlock msg ";
		for(Index pi : pis) {
			
			SymbolName pName = pi.p.getName(); 
			if(pName == null) {
				pName = pi.p.getParent().getName();
			}
			
			if(pi.p instanceof ArrayField && pi.p.asType(ArrayField.class).getNumber().length > 1) {
				comments.add("@param "+pi.name+" - index "+pi.i+" of "+ pName.toLowerCamel()+" "+pi.type+". Valid values: 0 to "+(pi.n - 1));
			} else {
				comments.add("@param "+pi.name+" - index of "+ pName.toLowerCamel()+" "+pi.type+"." + (pi.n >= 0 ? " Valid values: 0 to "+(pi.n - 1) : ""));
			}
			paramList += ", int "+pi.name;
	
				
			
		}
		
		String val = "";
		
	
		if(!getNotSet) {
			val =", "+ tf + " "+fn;
			paramList += val;
			
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
		
		if(pis.size() == 0) {
			
			addLine((getNotSet ? "return " : "")+lookupBbGetSet(f, getNotSet)+"(buf, msg, "+ makeFieldIndexName(f) + ");");
			
		} else {
		
			addLine("uint32_t i = "+makeFieldIndexName(f) + ";" );
			for(Index pi : pis) {
				if(pi.p instanceof ArrayField) {
					addLine("i += "+makeArraySizeName((ArrayField)pi.p, pi.i) + " * " + pi.name + ";");
				} else if(pi.p instanceof SequenceField) {
					
					addLine("i = getBbSequenceElementIndex(buf, msg, i, "+pi.name+");");
					
				}
				
			}
			
			addLine((getNotSet ? "return " : "")+lookupBbGetSet(f, getNotSet)+"(buf, msg, i"+ val + ");");
		}
		
		outdent();
		addLine("}");
		return;
	}
	
	
	private void makeStringCopier(StringField f, MessageField mf, boolean toNotFrom, boolean protoNotDef) {
		List<Index> pis = getIndeces(f);
		ArrayList<String> comments = new ArrayList<>();
		SymbolName fn = f.getName();
		if(fn == null) {
			fn = f.getParent().getName();
		}
		comments.add("A function to copy a string "+(toNotFrom ? "to" : "from") + " a message.");
		

		if(f.getComment() != null) {
			comments.add(f.getComment());
		}
		
		
		String paramList = "Bb * buf, BbBlock msg";
		comments.add("@param buf - the buffer that the message is being read/written from/to");
		comments.add("@param msg - the index to the start of the message in the buffer.");
		
		for(Index pi : pis) {
			
			SymbolName pName = pi.p.getName(); 
			if(pName == null) {
				pName = pi.p.getParent().getName();
			}
			
			if(pi.p instanceof ArrayField && pi.p.asType(ArrayField.class).getNumber().length > 1) {
				comments.add("@param "+pi.name+" - index "+pi.i+" of "+ pName.toLowerCamel()+" "+pi.type+". Valid values: 0 to "+(pi.n - 1));
			} else {
				comments.add("@param "+pi.name+" - index of "+ pName.toLowerCamel()+" "+pi.type+"." + (pi.n >= 0 ? " Valid values: 0 to "+(pi.n - 1) : ""));
			}
			paramList += ", int "+pi.name;
	
				
			
		}

		paramList += ", char * string";
		
	
		
			
		comments.add("@param string - the string to copy "+(toNotFrom ? "to" : "from"));
			
	
		

	
		ScopeName name = makeScopeName(f);
		
		
		addDocComment(comments);
		
		addLine("void copy"+(toNotFrom ? "To" : "From")+name.toSymbolName().toUpperCamel()+"("+paramList+")"+(protoNotDef ? ";" : "{"));
		
		if(protoNotDef) {
			return;
		}
		
		indent();
		addLine("uint32_t i = " + makeFieldIndexName(f) + ";");
		for(Index pi : pis) {
			if(pi.p instanceof ArrayField) {
				addLine("i += "+makeArraySizeName((ArrayField)pi.p, pi.i) + " * " + pi.name + ";");
			} else if(pi.p instanceof SequenceField) {
				
				addLine("i = getBbSequenceElementIndex(buf, msg, i, "+pi.name+");");
				
			}
			
		}
		

		
		if(toNotFrom) {
			//we're copying to the message
			addLine("uint32_t lenW = buf->length;//the current end of the message which will now be the length word of the string");
			addLine("uint32_t si = lenW + 4;//this will be the start of the string data");
			addLine("uint32_t j = 0;//this will be the string length by the time we're done");
			addLine("for(; j < "+makeStringMaxLengthName(f)+"; ++j){");
			indent();
			addLine("char c = string[j]");
			addLine("if(c == 0){");
			indent();
			addLine("break;");
			
			closeBrace();
			addLine("setUint8(buf, msg, si, c);");
			addLine("++si;");
			closeBrace();
			
			
			
			addLineComment("Update the string length and the buffer length");
			addLine("setUint32(buf, msg, lenW, j);");
			addLine("buf->length = si;");
			
		} else {
			//we're copying from the message
			addLine("uint32_t len = getUint16(buf, msg, i);");
			addLine("for(uint32_t j; j < len; ++j){");
			indent();
			addLine("string[j] = getUint8(buf, msg, si);");
			addLine("++si;");
			closeBrace();
		}
		

		closeBrace();
		
	}
	
	private void makeStringLengthGetter(StringField f, MessageField mf, boolean protoNotDef) {
		List<Index> pis = getIndeces(f);
		ArrayList<String> comments = new ArrayList<>();
		SymbolName fn = f.getName();
		if(fn == null) {
			fn = f.getParent().getName();
		}
		comments.add("A function to retrieve the length of a string in a message.");
		

		if(f.getComment() != null) {
			comments.add(f.getComment());
		}
		
		
		String paramList = "Bb * buf, BbBlock msg";
		comments.add("@param buf - the buffer that the message is being read/written from/to");
		comments.add("@param msg - the index to the start of the message in the buffer.");
		
		for(Index pi : pis) {
			
			SymbolName pName = pi.p.getName(); 
			if(pName == null) {
				pName = pi.p.getParent().getName();
			}
			
			if(pi.p instanceof ArrayField && pi.p.asType(ArrayField.class).getNumber().length > 1) {
				comments.add("@param "+pi.name+" - index "+pi.i+" of "+ pName.toLowerCamel()+" "+pi.type+". Valid values: 0 to "+(pi.n - 1));
			} else {
				comments.add("@param "+pi.name+" - index of "+ pName.toLowerCamel()+" "+pi.type+"." + (pi.n >= 0 ? " Valid values: 0 to "+(pi.n - 1) : ""));
			}
			paramList += ", int "+pi.name;
	
				
			
		}

		
	
		

	
		ScopeName name = makeScopeName(f);
		
		
		addDocComment(comments);
		
		addLine("uint32_t getStringLength"+name.toSymbolName().toUpperCamel()+"("+paramList+")"+(protoNotDef ? ";" : "{"));
		
		if(protoNotDef) {
			return;
		}
		
		indent();
		addLine("uint32_t i = " + makeFieldIndexName(f) + ";");
		for(Index pi : pis) {
			if(pi.p instanceof ArrayField) {
				addLine("i += "+makeArraySizeName((ArrayField)pi.p, pi.i) + " * " + pi.name + ";");
			} else if(pi.p instanceof SequenceField) {
				
				addLine("i = getBbSequenceElementIndex(buf, msg, i, "+pi.name+");");
				
			}
			
		}
	
			

		//we're copying from the message
		addLine("return (uint32_t)getUint16(buf, msg, i);");
		
		
		

		closeBrace();
		
	}
	
	
	private String makeStringMaxLengthName(StringField f) {
		// TODO Auto-generated method stub
		return null;
	}



	/**
	 * looks up the name of the bb transcoder function 
	 * @param f
	 * @return
	 */
	private String lookupBbGetSet(Field f, boolean getNotSet) {
		String result = getNotSet ? "getBb" : "setBb";
		switch(f.getTypeId()) {
		
		case BOOL:
			result += "Bool";
			break;
		case FLOAT32:
			result += "Float32";
			break;
		case FLOAT64:
			result += "Float64";
			break;
		case INT16:
			result += "Int16";
			break;
		case INT32:
			result += "Int32";
			break;
		case INT64:
			result += "Int64";
			break;
		case INT8:
			result += "Int8";
			break;
		case UINT16:
			result += "Uint16";
			break;
		case UINT32:
			result += "Uint32";
			break;
		case UINT64:
			result += "Uint64";
			break;
		case UINT8:
			result += "Uint8";
			break;
			
		case BOOLFIELD:
			break;
		case STRUCT:
			break;
		case ARRAY:
			break;
		case MESSAGE:
			break;
		case SEQUENCE:
			break;
		case STRING:
			break;
		case DEFERRED:
			break;
		case DEFINED:
			break;
		}
		return result;
	}

	private String prependHyphen(String s) {
		String result = "";
		if(s != null && !s.isBlank()) {
			result = " - "+s;
		}
		return result;
	}
	
	private class Index {
		final ParentField p;
		final int i;
		final int n;
		final String type;
		final boolean arrayNotSequence;
		final String name;
		int mult = -1;
		Index(ParentField pf, int j, int num, String nm){
			p = pf;
			i = j;
			n = num;
			arrayNotSequence =  pf instanceof ArrayField;
			type = arrayNotSequence ? "array" : "sequence";
			name = nm;
		}
		
	}
	
	
	private List<Index> getIndeces(Field f){
		return getIndeces(f, ft -> (ft.getTypeId() != TypeId.MESSAGE));
	}
	
	/**
	 * Scans upward from the specified field to the specified ParentField and note any array fields along the way
	 * @param f
	 * @param keepGoing - a function that checks the current field and returns true if it should keep scanning upwards, false if it should stop
	 * @return
	 */
	private List<Index> getIndeces(Field f, Function<Field, Boolean> keepGoing) {
		
		
		
		ArrayList<Index> result = new ArrayList<>();
		ParentField pf = f.getParent();
		int k = 0;
		List<ParentField> pfs = new ArrayList<>();
		while(pf != null && keepGoing.apply(pf)) {
			
			pfs.add((ParentField)pf);
			
			pf = pf.getParent();
		}
		
		//now re-order the fields so the go top to bottom instead of bottom-up
		pfs = pfs.reversed();
		
		for(ParentField pft : pfs) {
		
			if(pft instanceof ArrayField) {
				ArrayField af = (ArrayField)pft;
				for(int i = 0; i < af.getNumber().length; ++i) {
					result.add(new Index(af, i, af.getNumber()[i], "i"+k));
					++k;
				}
			} else if(pft instanceof SequenceField) {
				result.add(new Index((ParentField)pft, 0, -1, "i"+k));
				++k;
			}
			
			
		}
		
		
		//now compute multipliers
		int mult = 1;
		Field last = null;
		for(int j = result.size() - 1; j >= 0; --j) {
			Index i = result.get(j);
			if(last != i.p) {
				mult = 1;
				last = i.p;
			} 
			i.mult = mult;
			mult *= i.n;
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
				result = "char *";
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
		while((pf != null) && !(pf instanceof MessageField)) {
			SymbolName pn = null;
			pn = pf.getName();
			
			
			result = result.prepend(pn);
			pf = pf.getParent();
		}
		MessageField mf = f.getAncestor(MessageField.class);
		if(mf != null) {
			result = result.prepend(mf.getTypeName().deScope());
		}
		return result;
	}
	boolean m_bools = false;
	boolean m_arrays = false;
	boolean m_sequences = false;



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




}

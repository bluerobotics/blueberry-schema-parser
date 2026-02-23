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
package com.bluerobotics.blueberry.schema.parser.writers;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.constants.BooleanConstant;
import com.bluerobotics.blueberry.schema.parser.constants.NumberConstant;
import com.bluerobotics.blueberry.schema.parser.constants.StringConstant;
import com.bluerobotics.blueberry.schema.parser.fields.ArrayField;
import com.bluerobotics.blueberry.schema.parser.fields.BaseField;
import com.bluerobotics.blueberry.schema.parser.fields.BlueModule;
import com.bluerobotics.blueberry.schema.parser.fields.BoolFieldField;
import com.bluerobotics.blueberry.schema.parser.fields.DefinedTypeField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField.NameValue;
import com.bluerobotics.blueberry.schema.parser.fields.Field;
import com.bluerobotics.blueberry.schema.parser.fields.FieldList;
import com.bluerobotics.blueberry.schema.parser.fields.MessageField;
import com.bluerobotics.blueberry.schema.parser.fields.MultipleField;
import com.bluerobotics.blueberry.schema.parser.fields.MultipleField.Index;
import com.bluerobotics.blueberry.schema.parser.fields.NameMaker;
import com.bluerobotics.blueberry.schema.parser.fields.ParentField;
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
	
	
	private static final String TOPIC_NID_CHAR_STRING = "\\x80";
	private static final String TOPIC_DEVICE_TYPE_CHAR_STRING = "\\x81";
	
	public CWriter(File dir, BlueberrySchemaParser parser, String header) {
		super(dir, parser, header);
	}

	
	@Override
	public void write() {
			ArrayList<BlueModule> modules = getParser().getModules();
			

			//now modules contains a list of all unique modules.
//			//mow make a header and source files for each message
			
			modules.forEach(mod -> {
				makeHeaderFile(mod);
				makeSourceFile(mod);
			});
			



	}
	/**
	 * writes the header file for the specified module
	 * @param module
	 */
	private void makeHeaderFile(BlueModule module) {
		
		startFile(module, getHeader());

//		#ifndef BLUEBERRY_TRANSCODE_FIRMWARE_INC_BLUEBERRY_PACKET_H_
//		#define BLUEBERRY_TRANSCODE_FIRMWARE_INC_BLUEBERRY_PACKET_H_
//		#endif /* BLUEBERRY_TRANSCODE_FIRMWARE_INC_BLUEBERRY_PACKET_H_ */
		
		addLine("#ifndef "+module.getName().toUpperSnake("_") + "_MODULE_");
		addLine("#define "+module.getName().toUpperSnake("_") + "_MODULE_");
		addLine();
		
		addSectionDivider("Includes");
		addLine("#include <stdbool.h>");
		addLine("#include <stdint.h>");
		addLine("#include <blueberry-transcoder.h>");

		addSectionDivider("Defines");
		
		addLineComment("Message keys");
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			addMessageKey(mf);	
		});
		
		addLineComment("Numerical & Boolean Constants");
		module.getConstants().forEach(c -> {
			if(c instanceof StringConstant) {
//				StringConstant sc = (StringConstant)c;
//				addLine("const char "+sc.getName()+"[] = \"" + sc.getValue()+"\";");
			} else if(c instanceof BooleanConstant) {
				BooleanConstant bc = (BooleanConstant)c;
				String val = bc.getValue() ? "true" : "false";
				addLine("#define "+bc.getName().toUpperSnakeString()+" ("+val+")");
			} else if(c instanceof NumberConstant) {
				NumberConstant nc = (NumberConstant)c;
				TypeId tid = nc.getType().getTypeId();
				String type = getType(tid);
				String val = "";
				switch(tid) {
				
				case BOOL:
					val = "false";//TODO: don't have these yet
					break;
				case FLOAT32:
				case FLOAT64:
					val = nc.getValue().toString();
					break;
				case INT16:
				case INT32:
				case INT64:
				case INT8:
				case UINT16:
				case UINT32:
				case UINT64:
				case UINT8:
					val = nc.getValue().toString();
					break;
				case STRING:
				case BOOLFIELD:
				case DEFERRED:
				case DEFINED:
				case FILLER:

				case ARRAY:
				case STRUCT:
				case SEQUENCE:
				case MESSAGE:
					val = "";
					break;
				
				}
				addLine("#define "+nc.getName().toUpperSnakeString()+" ("+val+")");
			}
		});

		addSectionDivider("Types");

//		addFirstBlockDefine();
//
		module.getDefines().forEachOfType(EnumField.class, false, ef -> {
			writeEnum(ef);
		});
		addSectionDivider("Variables");
		module.getConstants().forEach(c -> {
			if(c instanceof StringConstant) {
				StringConstant sc = (StringConstant)c;
				addLine("extern const char "+NameMaker.makeConstantName(sc)+"[];");
			}
		});
		
		
		
		addSectionDivider("Topic String Constants");
		module.getMessages().forEachOfType(MessageField.class, false, msg -> {
			Annotation a = msg.getAnnotation(Annotation.TOPIC_ANNOTATION);
			String t = a.getParameter(0, String.class);
			addLine("extern const char "+NameMaker.makeTopicSymbol(msg)+"[];");
		});
		addLine();
			
		

		addSectionDivider("Function Prototypes");
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			makeMessageAdder(mf, true);
		});
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			makeMessageEmptyandFullTester(mf, true, true);
			makeMessageEmptyandFullTester(mf, true, false);

			mf.getUsefulChildren(true).forEach(true, f -> {
				makeMessageGetterSetter(f, true, true);
				makeMessageGetterSetter(f, false, true);
				makeMessagePresenceTester(f, true);
				
			});
			
			mf.getChildren().forEachOfType(StringField.class, true, sf -> {
				makeStringCopier(sf, true, true);
				makeStringCopier(sf, false, true);
				makeStringLengthGetter(sf, mf, true);
			});
			mf.getChildren().forEachOfType(SequenceField.class, true, sf -> {
				makeSequenceInit(sf, true);
				makeSequenceLengthGetter(sf, true);
			});
			
		});
		
		addLine();
		addLine("#endif /* "+module.getName().toUpperSnake("_") + "_MODULE_ */");
	
		writeToFile("inc/"+NameMaker.makeCModuleFileName(module, true));

	}
	

	/**
	 * creates and writes to disk the C source file for the specified module
	 * @param module
	 */
	private void makeSourceFile(BlueModule module) {
		

		startFile(module, getHeader());



		addSectionDivider("Includes");
		addLine("#include <"+NameMaker.makeCModuleFileName(module, true)+">");
		addLine("#include <blueberry-message.h>");

		addSectionDivider("Defines");
		addLineComment("Add message field indeces");
		//add defines for field indeces
		//also keep track of any boolfieldfields
		m_bools = false;
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			
			
			mf.getChildren().forEach(true, f -> {
				if(getType(f) != null) {
					if(!(f instanceof BoolFieldField)) {
						if(f.getBitCount() == 1) {
							addLine("#define " + NameMaker.makeFieldIndexName(f) + " ("+f.getParent().getIndex()+")");
							m_bools = true;
						} else {
							addLine("#define " + NameMaker.makeFieldIndexName(f) + " ("+f.getIndex()+")");
						}
					}
					if(f.getTypeId() == TypeId.STRING) {
						m_strings = true;
					}
					
				} else {
					if(f instanceof ArrayField) {
						m_arrays = true;
						addLine("#define " + NameMaker.makeFieldIndexName(f) + " ("+f.getIndex()+")");
					} else if(f instanceof SequenceField) {
						m_sequences = true;
						addLine("#define " + NameMaker.makeFieldIndexName(f) + " ("+f.getIndex()+")");
					}
				}
			});
		});
		addLine();
		addLineComment("Add message field ordinals");
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			
			
			mf.getChildren().forEach(true, f -> {
				
				if(f.getName() != null && f.isNotFiller()) {
					addLine("#define " + NameMaker.makeFieldOrdinalName(f) + " ("+f.getOrdinal()+")");
				}
						
					
				
					
				
			});
		});

		
		addLine();
		addLineComment("Add message max ordinals - the number of fields in the message and the ordinal of the last field of the message");
		
		//add a line for the max ordinal
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {

			addLine("#define "+NameMaker.makeMessageMaxOrdinalName(mf) + " ("+mf.getMaxOrdinal()+")");
			addLine("#define "+NameMaker.makeMessageModuleMessageConstant(mf) + " ("+WriterUtils.formatAsHex(mf.getModuleMessageKey())+")");
			
		});
		
		if(m_bools) {
			addLine();
			addLineComment("Add message boolean field masks");

			//now add defines for bit field indeces and bit masks
			module.getMessages().forEachOfType(MessageField.class, false, mf -> {
				mf.getChildren().forEach(true, f -> {
					if(f.getIndex() >= 0) {
						if(f instanceof BaseField && f.getBitCount() == 1) {
						
							addLine("#define " + NameMaker.makeBooleanMaskName(f) + " (1 << "+f.getIndex()+")");
	
						}
						
						
					}
				});
			});
		}
		if(m_arrays) {
			addLine();
			addLineComment("Add array sizes and element byte count");
			module.getMessages().forEachOfType(MessageField.class, false, mf -> {
				mf.getChildren().forEachOfType(ArrayField.class, true, af -> {
					List<Index> is = af.getIndeces();
					int n = is.size();
					if(n == 1) {
						Index pi = is.get(0);
						addLine("#define " + NameMaker.makeArraySizeName(pi) + " ("+pi.n+")");
						addLine("#define " + NameMaker.makeMultipleFieldElementByteCountName(pi) + " ("+pi.bytesPerElement+")");
					} else {
						for(Index pi : is) { 
							addLine("#define " + NameMaker.makeArraySizeName(pi) + " ("+pi.n+")");
							addLine("#define " + NameMaker.makeMultipleFieldElementByteCountName(pi) + " ("+pi.bytesPerElement+")");
						}
					}
					
				});
			});
		}
		if(m_sequences) {
			addLine();
			addLineComment("Add sequence element byte count");
			module.getMessages().forEachOfType(MessageField.class, false, mf -> {
				mf.getChildren().forEachOfType(SequenceField.class, true, sf -> {
					
					addLine("#define " + NameMaker.makeMultipleFieldElementByteCountName(sf.getIndeces().getFirst()) + " ("+sf.getPaddedByteCount()+")");

				});
			});
			

		}
		if(m_strings) {
			addLine();
			addLineComment("Add string max length constants");
			module.getMessages().forEachOfType(MessageField.class, false, mf -> {
				mf.getChildren().forEachOfType(StringField.class, true, sf -> {
					
					addLine("#define " + NameMaker.makeStringMaxLengthName(sf) + " ("+sf.getMaxSize()+")");

				});
			});
		}
		
		
		
		addLine();
		addLineComment("Add message lengths - measured in bytes");
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			addLine("#define " + NameMaker.makeMessageLengthName(mf) + " (" + mf.getPaddedByteCount()+")");
		});


		addSectionDivider("Types");

		addSectionDivider("Variables");
		module.getConstants().forEach(c -> {
			if(c instanceof StringConstant) {
				StringConstant sc = (StringConstant)c;
				addLine("const char "+NameMaker.makeConstantName(sc)+"[] = \"" + sc.getValue()+"\";");
			}
		});
		addSectionDivider("Topic String Constants");
		module.getMessages().forEachOfType(MessageField.class, false, msg -> {
			Annotation a = msg.getAnnotation(Annotation.TOPIC_ANNOTATION);
			String t = a.getParameter(0, String.class);
			t = t.replace(Annotation.TOPIC_NID_STRING, TOPIC_NID_CHAR_STRING);
			t = t.replace(Annotation.TOPIC_DEVICE_TYPE_STRING, TOPIC_DEVICE_TYPE_CHAR_STRING);

			addLine("const char "+NameMaker.makeTopicSymbol(msg)+"[] = \""+t+"\";");
		});

		addSectionDivider("Function Prototypes");
//		addBlockFunctionAdder(top, true);


		addSectionDivider("Source");
		
	
		
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			makeMessageAdder(mf, false);
			makeMessageEmptyandFullTester(mf, false, true);
			makeMessageEmptyandFullTester(mf, false, false);

			mf.getUsefulChildren(true).forEach(false, f -> {
				
				makeMessageGetterSetter(f, true, false);
				makeMessageGetterSetter(f, false, false);
				makeMessagePresenceTester(f, false);
				

			});
		
			mf.getChildren().forEachOfType(StringField.class, true, sf -> {
				makeStringCopier(sf, true,  false);
				makeStringCopier(sf, false, false);
				makeStringLengthGetter(sf, mf, false);
			});
			mf.getChildren().forEachOfType(SequenceField.class, true, sf -> {
				makeSequenceInit(sf, false);
				makeSequenceLengthGetter(sf, false);
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



		writeToFile("src/"+NameMaker.makeCModuleFileName(module, false));

	}


	



	

	

	


	private void makeSequenceLengthGetter(SequenceField sf, boolean protoNotDef) {
		ArrayList<String> comments = new ArrayList<>();
		comments.add("Gets the defined length of a sequence "+sf.getTypeName().deScope().toTitle());
		comments.add(sf.getComment());
		
		List<Index> pis = MultipleField.getIndeces(sf);
		
		
		comments.add("@param buf - the message buffer to add the message to");
		comments.add("@param msg - the index of the start of the message");

		String paramList = "Bb * buf, BbBlock msg";
				
		
		addIndecesComments(pis, comments);
		
		paramList += makeIndecesParamList(pis);
		
		comments.add("@return - the number of elements in the sequence");
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		
		
	
		
		addLine("uint32_t get"+NameMaker.makeSequenceLengthGetterName(sf, true)+ "("+paramList+")" + (protoNotDef ? ";" : "{"));
		if(protoNotDef) {
			return;
		}
		//now do contents of function
		indent();

		
		
		
		
		
		addLinesForFieldIndexCalc(pis, sf);
		

		addLine("if(isBbBlockInvalid(i)){");
		indent();
		addLine("return 0;//bail because a sequence was not initialized");
		closeBrace();
		addLineComment("i is now the index of this sequence field header");
		
		addLine("return getBbSequenceLength(buf, msg, i);");
		
		
		
		
		
		outdent();
		addLine("}");
	}

	/**
	 * adds details of the index parameters to the specified comments list
	 * @param pis
	 * @param comments
	 */
	private void addIndecesComments(List<Index> pis, ArrayList<String> comments) {
		for(Index pi : pis) {
			
			SymbolName pName = pi.p.getName(); 
			if(pName == null) {
				pName = pi.p.getParent().getName();
			}
			
			
			if(pi.p instanceof ArrayField && pi.p.asType(ArrayField.class).getNumber().length > 1) {
				comments.add("@param "+pi.paramName+" - index "+pi.i+" of "+ pName.toLowerCamel()+" "+pi.type+". Valid values: 0 to "+(pi.n - 1));
			} else {
				comments.add("@param "+pi.paramName+" - index of "+ pName.toLowerCamel()+" "+pi.type+"." + (pi.n >= 0 ? " Valid values: 0 to "+(pi.n - 1) : ""));
			}

		}
	}

	/**
	 * makes string list of index parameters to append to the paramater list of a function declaration
	 * for fields that must be accessed by specfying these indeces
	 * @param pis
	 * @return
	 */
	private String makeIndecesParamList(List<Index> pis) {
		String result = "";
		for(Index pi : pis) {
			result += ", uint32_t " + pi.paramName;
		}
		
		return result;
	}

	/**
	 * makes the sequence initialize function
	 * this allocates bytes in the buffer for the contents of the sequence
	 * this must be called on all sequences before any of the contained fields can be assigned values
	 * TODO: init any child sequence placeholders to 0xffff
	 * @param sf
	 * @param protoNotDef
	 */
	private void makeSequenceInit(SequenceField sf, boolean protoNotDef) {
		ArrayList<String> comments = new ArrayList<>();
		comments.add("A function to initialize a "+sf.getTypeName().deScope().toTitle());
		comments.add(sf.getComment());
		
		List<Index> pis = MultipleField.getIndeces(sf);
		
		
		comments.add("@param buf - the message buffer to add the message to");
		comments.add("@param msg - the index of the start of the message");
		comments.add("@param n - the number of elements of this sequence");
		String paramList = "Bb * buf, BbBlock msg";
				
		addIndecesComments(pis, comments);
		paramList += makeIndecesParamList(pis);
		
		paramList += ", uint32_t n";
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		
		
		
		addLine("void "+NameMaker.makeSequenceInitName(sf, true)+"("+paramList+")" + (protoNotDef ? ";" : "{"));
		if(protoNotDef) {
			return;
		}
		//now do contents of function
		indent();
		addLinesForFieldIndexCalc(pis, sf);
		

		addLine("if(isBbBlockInvalid(i)){");
		indent();
		addLine("return;//bail because a sequence was not initialized");
		closeBrace();
		
		
		addLineComment("i is now the index of this sequence field header");
		
		
		
		
		addLine("uint32_t bs = "+NameMaker.makeMultipleFieldElementByteCountName(sf.getIndeces().getFirst())+"; //the 4 is to account for the length field that precedes the sequence data");
		addLine("initBbSequence(buf, msg, i, bs, n);");
		
		outdent();
		addLine("}");
	}

	private void addMessageKey(MessageField mf) {
		String mk = makeFullMessageKey(mf);
		addLine("#define "+NameMaker.makeMessageKeyName(mf) + " ("+mk+")");
	}
	
	
	



	/**
	 * writes a function to add a message to a packet
	 * TODO: make sure all sequence and string placeholders are initialized to 0xffff!
	 * @param mf
	 * @param protoNotDef
	 */
	private void makeMessageAdder(MessageField mf, boolean protoNotDef) {
		ArrayList<String> comments = new ArrayList<>();
		comments.add("Adds a "+mf.getTypeName().deScope().toTitle()+" to the end of the current buffer");
		comments.add(mf.getComment());
		
		comments.add("@param buf - the message buffer to add the message to");
		
		String paramList = "Bb * buf";
		ArrayList<Field> fs = new ArrayList<>();
		ArrayList<Field> ss = new ArrayList<>();
		//first make a list of all top-level fields that are not strings or parent fields
		//but also add contents of boolfieldfields
		mf.getUsefulChildren(false).forEach(true, f -> {
			
			
			String tp = getType(f);
			
			
			
			if(tp != null && f.getTypeId() != TypeId.STRING && (MultipleField.getIndeces(f)).size() == 0) {
				fs.add(f);
				
			} else if(f.getTypeId() == TypeId.STRING || f.getTypeId() == TypeId.SEQUENCE) {
				//build a list of all sequences and strings that are not in sequences
				boolean notInSequence = true;
				for(Index i : MultipleField.getIndeces(f)) {
					if(!i.arrayNotSequence) {
						notInSequence = false;
						break;
					}
				}
					
					
				if(notInSequence) {
					ss.add(f);
				}
			}
			
		});
		
		for(Field f : fs) {
			String stuff = "";
			
			paramList += ", ";
			
			
			SymbolName paramName = NameMaker.makeParamName(f);
			String type = getType(f);
			if(f instanceof EnumField) {
				type = f.getTypeName().deScope().toUpperCamelString();
			}
			paramList += type+" " + paramName+stuff;
			
			comments.add("@param " + paramName + prependHyphen( getFieldComment(f)));
		}
	
		
		comments.add("@returns - the index of the new message.");
		
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		addLine("BbBlock "+mf.getTypeName().deScope().prepend("add").toLowerCamel()+"("+paramList+")" + (protoNotDef ? ";" : "{"));
		if(protoNotDef) {
			return;
		}
		//now do contents of function
		indent();
		
		//now compute the location of the new message
		addLine("BbBlock msg = buf->length;");
		addLineComment("Extend buffer to include the main message body before writing it");
		addLine("buf->length = msg + "+NameMaker.makeMessageLengthName(mf)+";");
		
		
		Field ft = null;
		ft = mf.getChildren().getByName(MessageField.MODULE_MESSAGE_KEY_FIELD_NAME);
		addLine(lookupBbGetSet(ft, false)+"(buf, msg, "+NameMaker.makeFieldIndexName(ft)+", "+NameMaker.makeMessageModuleMessageConstant(mf)+");");
		
		ft = mf.getChildren().getByName(MessageField.LENGTH_FIELD_NAME);
		addLine(lookupBbGetSet(ft, false)+"(buf, msg, "+NameMaker.makeFieldIndexName(ft)+", "+NameMaker.makeMessageLengthName(mf)+"/4);//length field is measured in 4-byte words");
		
		ft = mf.getChildren().getByName(MessageField.MAX_ORDINAL_FIELD_NAME);
		addLine(lookupBbGetSet(ft, false)+"(buf, msg, "+NameMaker.makeFieldIndexName(ft)+", "+NameMaker.makeMessageMaxOrdinalName(mf)+");");

		
		for(Field f : fs) {
			
			
			
				
		

			
				SymbolName paramName = NameMaker.makeParamName(f);
			
				String boolStuff = "";
				if(f.getBitCount() == 1) {
					//this is a bool so it needs another field for the bit num
					boolStuff = ", " + NameMaker.makeBooleanMaskName(f);
				}
				addLine(lookupBbGetSet(f, false)+"(buf, msg, "+NameMaker.makeFieldIndexName(f)+boolStuff+", "+paramName+");");
			
			
			
		}
		
		
		
		
		for(Field f : ss) {
			List<Index> pis = MultipleField.getIndeces(f);
			if(pis.size() == 0) {
				//zero the index field of each string and sequence
				String s = (f.getTypeId() == TypeId.SEQUENCE) ? "sequence" : "string";
				addLine("setBbUint16(buf, msg, "+NameMaker.makeFieldIndexName(f)+", BB_INVALID_BLOCK);//clear "+s+" header");
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
						Index pi = pis.get(j);
						offset += ii[j] * pi.bytesPerElement;
				
					}				
					String s = (f.getTypeId() == TypeId.SEQUENCE) ? "sequence" : "string";
					addLine("setBbUint16(buf, msg, "+NameMaker.makeFieldIndexName(f)+" + "+offset+", BB_INVALID_BLOCK);//clear "+s+" header. Note magic number. Sorry.");
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
		
	
		
		
		//finally return the index of the new message
		addLine("return msg;");
		
		outdent();
		addLine("}");
	}
	/**
	 * makes a function to test if a message contains no fields or if it contains all fields defined in this version of the schema
	 * This is computed using the max ordinal field
	 * @param mf
	 * @param protoNotDef - makes a function prototype if true and a function definition if false
	 * @param emptyNotFull - makes an empty test if true and a full tester if false
	 */
	private void makeMessageEmptyandFullTester(MessageField mf, boolean protoNotDef, boolean emptyNotFull) {
		ArrayList<String> comments = new ArrayList<>();
		comments.add("Tests if the current message has"+(emptyNotFull ? " no fields present." : " all defined fields present."));

		if(mf.getComment() != null) {
			comments.add(mf.getComment());
		}
		
		SymbolName functionName = mf.getTypeName().toSymbolName().prepend("is").append(emptyNotFull ? "empty" : "full");
		
		addDocComment(comments);
		addLine("bool "+functionName.toLowerCamel()+"(Bb * buf, BbBlock msg)"+(protoNotDef ? ";" : "{"));
		
		if(!protoNotDef) {
			indent();

			if(emptyNotFull) {
				addLine("return getBbMessageMaxOrdinal(buf, msg) <= 2;//will always be length and ordinal fields");
			} else {
				addLine("return getBbMessageMaxOrdinal(buf, msg) >= "+NameMaker.makeMessageMaxOrdinalName(mf)+";");
			}
			
			
			closeBrace();
		}
	}
	/**
	 * makes a function to test if a message has the specified field or not
	 * this uses the field number field to compare against the field's ordinal
	 * @param f
	 * @param b
	 */
	private void makeMessagePresenceTester(Field f, boolean protoNotDef) {
		String tf = getType(f);
		if(tf == null || f.getTypeId() == TypeId.STRING) {
			return;
		}
		List<Index> pis = MultipleField.getIndeces(f);
		if(pis.size() > 0) {//only need this for top level message fields
			return;
		}
		
		
		ArrayList<String> comments = new ArrayList<>();
		SymbolName fn = f.getName();
		if(fn == null) {
			fn = f.getParent().getName();
		}
		comments.add("Tests if the current message containts the "+fn.toLowerCamel()+" field");

		if(f.getComment() != null) {
			comments.add(f.getComment());
		}
		
		
//		List<Field> fs = f.getAncestors(MessageField.class);
		String paramList = "Bb * buf, BbBlock msg ";
		
		
		String val = "";
		
	
		
	
		
		
		addDocComment(comments);
		String line = ("bool ")+NameMaker.makeFieldPresenceTesterName(f, true)+"("+paramList+")"+(protoNotDef ? ";" : "{");
		addLine(line);
		if(protoNotDef) {
			return;
		}
		indent();
		
	
			
			addLine("return "+ NameMaker.makeFieldOrdinalName(f) + " <= (getBbMessageMaxOrdinal(buf, msg));");
			
				
		outdent();
		addLine("}");
		return;		
	}


	/**
	 * make getter or setter for all base types except strings
	 * Note that this won't make a setter for a simple field in a message. Those are set as part of the message adder
	 * @param f
	 * @param getNotSet
	 * @param protoNotDef
	 */
	private void makeMessageGetterSetter(Field f, boolean getNotSet, boolean protoNotDef) {
		String tf = getType(f);
		if(tf == null || f.getTypeId() == TypeId.STRING) {
			return;
		}
		List<Index> pis = MultipleField.getIndeces(f);
		
		//don't need a setter if it's a simple field not in an array
		if((!getNotSet) && pis.size() == 0) {
			return;
		}
		ArrayList<String> comments = new ArrayList<>();
		
		SymbolName fn = f.getName();
		if(fn == null) {
			fn = f.getParent().getName();
		}
		comments.add("A "+(getNotSet ? "g" : "s") + "etter for the "+fn.toLowerCamel()+" field");

		if(f.getComment() != null) {
			comments.add(f.getComment());
		}
		
		comments.add("@param buf - the message buffer to add the message to");
		comments.add("@param msg - the index of the start of the message");
		String paramList = "Bb * buf, BbBlock msg ";
		addIndecesComments(pis, comments);
		paramList += makeIndecesParamList(pis);
		
		String val = "";
		
	
		if(!getNotSet) {
			val =", "+ tf + " "+fn.toLowerCamel();
			paramList += val;
			
			comments.add("@param "+fn.toLowerCamel()+prependHyphen(f.getComment()));
			
		}
		

	
		
		
		addDocComment(comments);
		
	
		
		String line;
		if(getNotSet) {
			line =  tf + " " + NameMaker.makeFieldGetterName(f, true);
		} else {
			line =  "void " + NameMaker.makeFieldSetterName(f, true);
		}
		line += "("+paramList+")"+(protoNotDef ? ";" : "{");
		addLine(line);
		if(protoNotDef) {
			return;
		}
		indent();
		
		addLinesForFieldIndexCalc(pis, f);
		
		if(!getNotSet) {
			addLine("if(isBbBlockInvalid(i)){");
			indent();
			addLine("return;//bail because a sequence was not initialized");
			closeBrace();
		}
		//TODO: what to do if the index is invalid for a getter?
		
		String boolStuff = "";
		if(f.getBitCount() == 1) {
			//this is a bool so it needs another field for the bit num
			boolStuff = ", " + NameMaker.makeBooleanMaskName(f);
		}
		
		addLine((getNotSet ? "return " : "")+lookupBbGetSet(f, getNotSet)+"(buf, msg, i" + boolStuff + (getNotSet ? "" : ", "+ fn.toLowerCamel()) + ");");
		
		outdent();
		addLine("}");
	}
	
	/**
	 * adds lines to the output file for looking up the index of the specified field
	 * This takes into account the various array and sequence indeces required
	 * @param pis
	 * @param f
	 */
	private void addLinesForFieldIndexCalc(List<Index> pis, Field f) {
		boolean bail = false;
		addLine("uint16_t i = 0;");
		if(pis.size() == 0) {
			
			
		} else {
			
			
			for(Index pi : pis) {
				addLine("i += "+NameMaker.makeFieldIndexName(pis.getFirst().p)+";" );
				if(pi.p instanceof ArrayField) {
//					addLine("i += "+NameMaker.makeMultipleFieldElementByteCountName(pi) + " * " + name + ";");
					addLine("i = getBbArrayElementIndex(buf, msg, i, "+pi.paramName+", "+pi.bytesPerElement+");");
				} else if(pi.p instanceof SequenceField) {
					
					addLine("i = getBbSequenceElementIndex(buf, msg, i, "+pi.paramName+");");

				
				}
				
			}
			
		}
		addLine("i += "+NameMaker.makeFieldIndexName(f)+";");
		
	}
	

	private void makeStringCopier(StringField f, boolean toNotFrom, boolean protoNotDef) {
		List<Index> pis = MultipleField.getIndeces(f);
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
		
		addIndecesComments(pis, comments);
		paramList += makeIndecesParamList(pis);

		paramList += ", char * string, uint32_t n";
		
	
		
			
		comments.add("@param string - the string to copy "+(toNotFrom ? "from" : "to"));//note that when we're copying to the message, we copy from the string parameter
			
	
		

	
		
		
		addDocComment(comments);
		
		addLine("void "+NameMaker.makeStringCopierName(f, toNotFrom, true)+"("+paramList+")"+(protoNotDef ? ";" : "{"));
		
		if(protoNotDef) {
			return;
		}
		
		indent();
		addLinesForFieldIndexCalc(pis, f);
		

		addLine("if(isBbBlockInvalid(i)){");
		indent();
		addLine("return;//bail because a sequence was not initialized");
		closeBrace();
		addLineComment("i is now the index of this string field header");
		

		
		
		//we're copying to the message
		addLine("copyBbString"+(toNotFrom ? "To" : "From")+"Message(buf, msg, i, string, n);");
			
			
			
	
		

		closeBrace();
		
	}
	
	private void makeStringLengthGetter(StringField f, MessageField mf, boolean protoNotDef) {
		List<Index> pis = MultipleField.getIndeces(f);
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
		
		addIndecesComments(pis, comments);
		paramList += makeIndecesParamList(pis);

		
	
		

	
		
		
		addDocComment(comments);
		
		addLine("uint32_t "+NameMaker.makeStringLengthGetterName(f, true)+"("+paramList+")"+(protoNotDef ? ";" : "{"));
		
		if(protoNotDef) {
			return;
		}
		
		indent();
		addLinesForFieldIndexCalc(pis, f);
		

		addLine("if(isBbBlockInvalid(i)){");
		indent();
		addLine("return 0;//bail because a sequence was not initialized");
		closeBrace();
		addLineComment("i is now the index of this string field header");
	
			

		//we're copying from the message
		addLine("return getBbStringLength(buf, msg, i);");
		
		
		

		closeBrace();
		
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
		case FILLER:
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



	
	



	

	private String getType(TypeId tid) {
		String result = "";
		switch(tid) {
		
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
		
			break;
		case ARRAY:
		case BOOLFIELD:
		case FILLER:
		case DEFERRED:
		case MESSAGE:
		case SEQUENCE:
		case STRUCT:
			result = null;
			break;
		
		}
		return result;
	}

	private String getType(Field f) {
		String result = null;
		if(f.getTypeId() == TypeId.DEFINED) {
			Field f2 = f;
			while(f2 instanceof DefinedTypeField) {
				f2 = ((DefinedTypeField)f2).getFirstChild();
			}
			result = getType(f2);
		} else {
		
			result = getType(f.getTypeId());
		}
		return result;
	}

	boolean m_bools = false;
	boolean m_arrays = false;
	boolean m_sequences = false;
	boolean m_strings = false;



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
			addLine(NameMaker.makeEnumItemName(ef, nv) + " = " + WriterUtils.formatAsHex(nv.getValue().asLong())+", " + c);


		}
		outdent();

		addLine("} "+NameMaker.makeEnumName(ef)+";");

		addLine();
	
	}

	




}

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

import com.bluerobotics.blueberry.schema.parser.constants.Number;
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
import com.bluerobotics.blueberry.schema.parser.fields.ScopeName;
import com.bluerobotics.blueberry.schema.parser.fields.SequenceField;
import com.bluerobotics.blueberry.schema.parser.fields.StringField;
import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;
import com.bluerobotics.blueberry.schema.parser.parsing.BlueberrySchemaParser;
import com.bluerobotics.blueberry.schema.parser.parsing.SchemaParserException;
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

		addSectionDivider("Types");

//		addFirstBlockDefine();
//
		module.getDefines().forEachOfType(EnumField.class, false, ef -> {
			writeEnum(ef);
		});
//
		addSectionDivider("Function Prototypes");
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			makeMessageAdder(mf, true);
		});
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			makeMessageEmptyandFullTester(mf, true, true);
			makeMessageEmptyandFullTester(mf, true, false);

			mf.getChildren().forEach(f -> {
				
				makeMessageGetterSetter(f, true, true);
				makeMessageGetterSetter(f, false, true);
				makeMessagePresenceTester(f, true);
			}, true);
			
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

		addSectionDivider("Defines");
		addLineComment("Add message field indeces");
		//add defines for field indeces
		//also keep track of any boolfieldfields
		m_bools = false;
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			
			
			mf.getChildren().forEach(f -> {
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
			}, true);
		});
		addLine();
		addLineComment("Add message field ordinals");
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			
			
			mf.getChildren().forEach(f -> {
				
				if(f.getName() != null && f.isNotFiller()) {
					addLine("#define " + NameMaker.makeFieldOrdinalName(f) + " ("+f.getOrdinal()+")");
				}
						
					
				
					
				
			}, true);
		});

		
		addLine();
		addLineComment("Add message max ordinals - the number of fields in the message and the ordinal of the last field of the message");
		
		//add a line for the max ordinal
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {

			addLine("#define "+NameMaker.makeMessageMaxOrdinalName(mf) + " ("+mf.getLastChild().getOrdinal()+")");
			addLine("#define "+NameMaker.makeMessageModuleMessageConstant(mf) + " ("+WriterUtils.formatAsHex(mf.getModuleMessageKey())+")");
			
		});
		
		if(m_bools) {
			addLine();
			addLineComment("Add message boolean field masks");

			//now add defines for bit field indeces and bit masks
			module.getMessages().forEachOfType(MessageField.class, false, mf -> {
				mf.getChildren().forEach(f -> {
					if(f.getIndex() >= 0) {
						if(f instanceof BaseField && f.getBitCount() == 1) {
						
							addLine("#define " + NameMaker.makeBooleanMaskName(f) + " (1 << "+f.getIndex()+")");
	
						}
						
						
					}
				}, true);
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
						for(int i = 0; i < n; ++i) { 
							Index pi = is.get(i);
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
		addLineComment("Add message lengths");
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			addLine("#define " + NameMaker.makeMessageLengthName(mf) + " (" + mf.getPaddedByteCount()+")");
		});


		addSectionDivider("Types");



		addSectionDivider("Function Prototypes");
//		addBlockFunctionAdder(top, true);


		addSectionDivider("Source");
		
	
		
		module.getMessages().forEachOfType(MessageField.class, false, mf -> {
			makeMessageAdder(mf, false);
			makeMessageEmptyandFullTester(mf, false, true);
			makeMessageEmptyandFullTester(mf, false, false);

			mf.getChildren().forEach(f -> {
				
				makeMessageGetterSetter(f, true, false);
				makeMessageGetterSetter(f, false, false);
				makeMessagePresenceTester(f, false);

			}, true);
			
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
				
		for(Index pi : pis) {
			
			SymbolName pName = pi.p.getName(); 
			if(pName == null) {
				pName = pi.p.getParent().getName();
			}
			
			String name = NameMaker.makeIndexName(pi);
			if(pi.p instanceof ArrayField && pi.p.asType(ArrayField.class).getNumber().length > 1) {
				comments.add("@param "+name+" - index "+pi.i+" of "+ pName.toLowerCamel()+" "+pi.type+". Valid values: 0 to "+(pi.n - 1));
			} else {
				comments.add("@param "+name+" - index of "+ pName.toLowerCamel()+" "+pi.type+"." + (pi.n >= 0 ? " Valid values: 0 to "+(pi.n - 1) : ""));
			}
			paramList += ", uint32_t "+name;
	
				
			
		}
		
		comments.add("@return - the number of elements in the sequence");
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		
		
	
		
		addLine("uint32_t get"+NameMaker.makeSequenceLengthGetterName(sf)+ "("+paramList+")" + (protoNotDef ? ";" : "{"));
		if(protoNotDef) {
			return;
		}
		//now do contents of function
		indent();

		
		
		
		
		
		addLine("uint32_t i = "+NameMaker.makeFieldIndexName(sf) + ";" );
		for(Index pi : pis) {
			String name = NameMaker.makeIndexName(pi);
			if(pi.p instanceof ArrayField) {
				addLine("i += "+NameMaker.makeMultipleFieldElementByteCountName(pi) + " * " + name + ";");
			} else if(pi.p instanceof SequenceField) {
				
				addLine("i = getBbSequenceElementIndex(buf, msg, i, "+name+");");
				addLine("if(i == 0){");
				indent();
				addLine("return 0;//bail because an upstream sequence was not initialized");
				closeBrace();
				
				
				
			}
			
		}
		addLineComment("i is now the index of this sequence field header");
		
		addLine("return getUint16(buf, msg, i);//get the length field of the sequence");
		
		
		
		
		outdent();
		addLine("}");
	}

	/**
	 * makes the sequence initialize function
	 * this allocates bytes in the buffer for the contents of the sequence
	 * this must be called on all sequences before any of the contained fields can be assigned values
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
				
		for(Index pi : pis) {
			String name = NameMaker.makeIndexName(pi);
			SymbolName pName = pi.p.getName(); 
			if(pName == null) {
				pName = pi.p.getParent().getName();
			}
			
			if(pi.p instanceof ArrayField && pi.p.asType(ArrayField.class).getNumber().length > 1) {
				comments.add("@param "+name+" - index "+pi.i+" of "+ pName.toLowerCamel()+" "+pi.type+". Valid values: 0 to "+(pi.n - 1));
			} else {
				comments.add("@param "+name+" - index of "+ pName.toLowerCamel()+" "+pi.type+"." + (pi.n >= 0 ? " Valid values: 0 to "+(pi.n - 1) : ""));
			}
			paramList += ", uint32_t "+name;
	
				
			
		}
		
		paramList += ", uint32_t n";
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		
		
	
		
		addLine("void "+NameMaker.makeSequenceInitName(sf)+"("+paramList+")" + (protoNotDef ? ";" : "{"));
		if(protoNotDef) {
			return;
		}
		//now do contents of function
		indent();

		addLine("uint32_t is = buf->length;//this is the next free byte of the message");
		addLine("uint32_t dis = is % 4;");
		addLine("is += dis == 0 ? 0 : 4 - (is % 4);//advance to the next4 byte alignment");
		
		addLine("setUint32(buf, msg, is, n);//set the length field of the header sequence");
		addLine("++is;");
		
		
		
		
		
		addLine("uint32_t i = "+NameMaker.makeFieldIndexName(sf) + ";" );
		for(Index pi : pis) {
			String name = NameMaker.makeIndexName(pi);
			if(pi.p instanceof ArrayField) {
				addLine("i += "+NameMaker.makeMultipleFieldElementByteCountName(pi) + " * " + name + ";");
			} else if(pi.p instanceof SequenceField) {
				
				addLine("i = getBbSequenceElementIndex(buf, msg, i, "+name+");");
				addLine("if(i == 0){");
				indent();
				addLine("return;//bail because an upstream sequence was not initialized");
				closeBrace();
				
				
				
			}
			
		}
		addLineComment("i is now the index of this sequence field header");
		
		addLine("setUint16(buf, msg, i, is);//set the header to the location of the sequence data");
		
		addLine("uint32_t size = ("+NameMaker.makeMultipleFieldElementByteCountName(sf.getIndeces().getFirst())+" * n) + 4; //the 4 is to account for the length field that precedes the sequence data");
		
		addLine("buf->length += size;");
		addLineComment("Finially update the length field of the message");
		
		MessageField mf = sf.getAncestor(MessageField.class);
		Field ft = mf.getChildren().getByName(MessageField.LENGTH_FIELD_NAME);
		addLine("uint32_t len = "+lookupBbGetSet(ft, true)+"(buf, msg, "+NameMaker.makeFieldIndexName(ft)+");");
		addLine(lookupBbGetSet(ft, false)+"(buf, msg, "+NameMaker.makeFieldIndexName(ft)+", len + size);");
		
		outdent();
		addLine("}");
	}

	private void addMessageKey(MessageField mf) {
		String mk = makeFullMessageKey(mf);
		addLine("#define "+NameMaker.makeMessageKeyName(mf) + " ("+mk+")");
	}
	
	
	



	
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
		mf.getChildren().forEach(f -> {
			//don't process the length, maxOrdinal or moduleMessageKey fields. These fields will be set to constants
			if(f.getName() != null && f.getName().equals(MessageField.MODULE_MESSAGE_KEY_FIELD_NAME)) {
			} else if(f.getName() != null && f.getName().equals(MessageField.LENGTH_FIELD_NAME)) {
			} else if(f.getName() != null && f.getName().equals(MessageField.MAX_ORDINAL_FIELD_NAME)) {
			} else {
			
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
			}
		}, true);
		
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
		
		
		Field ft = null;
		ft = mf.getChildren().getByName(MessageField.MODULE_MESSAGE_KEY_FIELD_NAME);
		addLine(lookupBbGetSet(ft, false)+"(buf, msg, "+NameMaker.makeFieldIndexName(ft)+", "+NameMaker.makeMessageModuleMessageConstant(mf)+");");
		
		ft = mf.getChildren().getByName(MessageField.LENGTH_FIELD_NAME);
		addLine(lookupBbGetSet(ft, false)+"(buf, msg, "+NameMaker.makeFieldIndexName(ft)+", "+NameMaker.makeMessageLengthName(mf)+");");
		
		ft = mf.getChildren().getByName(MessageField.MAX_ORDINAL_FIELD_NAME);
		addLine(lookupBbGetSet(ft, false)+"(buf, msg, "+NameMaker.makeFieldIndexName(ft)+", "+NameMaker.makeMessageMaxOrdinalName(mf)+");");

		
		for(Field f : fs) {
			
			
			
				
		

			
				SymbolName paramName = NameMaker.makeParamName(f);
			
			
				addLine(lookupBbGetSet(f, false)+"(buf, msg, "+NameMaker.makeFieldIndexName(f)+", "+paramName+");");
			
			
			
		}
		
		
		
		
		for(Field f : ss) {
			List<Index> pis = MultipleField.getIndeces(f);
			if(pis.size() == 0) {
				//zero the index field of each string and sequence
				String s = (f.getTypeId() == TypeId.SEQUENCE) ? "sequence" : "string";
				addLine("setUint16(buf, msg, "+NameMaker.makeFieldIndexName(f)+", 0);//clear "+s+" header");
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
					addLine("setUint16(buf, msg, "+NameMaker.makeFieldIndexName(f)+" + "+offset+", 0);//clear "+s+" header. Note magic number. Sorry.");
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
		addLine("buf->length = msg + "+NameMaker.makeMessageLengthName(mf)+";");
	
		
		
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
				addLine("return (" + NameMaker.makeFieldGetterName(getMaxOrdinalField(mf))+"(buf, msg) <= 2);//will always be length and ordinal fields");
			} else {
				addLine("return (" + NameMaker.makeFieldGetterName(getMaxOrdinalField(mf))+"(buf, msg) >= "+NameMaker.makeMessageMaxOrdinalName(mf)+");");
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
		String line = ("bool ")+NameMaker.makeFieldPresenceTesterName(f)+"("+paramList+")"+(protoNotDef ? ";" : "{");
		addLine(line);
		if(protoNotDef) {
			return;
		}
		indent();
		
	
			Field of = getMaxOrdinalField(f);
			addLine("return "+ NameMaker.makeFieldOrdinalName(f) + " <= "+NameMaker.makeFieldGetterName(of)+"(buf, msg);");
			
				
		outdent();
		addLine("}");
		return;		
	}


	/**
	 * make getter or setter for all base types except strings
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
		
		
//		List<Field> fs = f.getAncestors(MessageField.class);
		String paramList = "Bb * buf, BbBlock msg ";
		for(Index pi : pis) {
			String name = NameMaker.makeIndexName(pi);
			SymbolName pName = pi.p.getName(); 
			if(pName == null) {
				pName = pi.p.getParent().getName();
			}
			
			if(pi.p instanceof ArrayField && pi.p.asType(ArrayField.class).getNumber().length > 1) {
				comments.add("@param "+name+" - index "+pi.i+" of "+ pName.toLowerCamel()+" "+pi.type+". Valid values: 0 to "+(pi.n - 1));
			} else {
				comments.add("@param "+name+" - index of "+ pName.toLowerCamel()+" "+pi.type+"." + (pi.n >= 0 ? " Valid values: 0 to "+(pi.n - 1) : ""));
			}
			paramList += ", uin32_t "+name;
	
				
			
		}
		
		String val = "";
		
	
		if(!getNotSet) {
			val =", "+ tf + " "+fn.toLowerCamel();
			paramList += val;
			
			comments.add("@param "+fn.toLowerCamel()+prependHyphen(f.getComment()));
			
		}
		

	
		
		
		addDocComment(comments);
		
		String line;
		if(getNotSet) {
			line =  tf + " " + NameMaker.makeFieldGetterName(f);
		} else {
			line =  "void " + NameMaker.makeFieldSetterName(f);
		}
		line += "("+paramList+")"+(protoNotDef ? ";" : "{");
		addLine(line);
		if(protoNotDef) {
			return;
		}
		indent();
		
		if(pis.size() == 0) {
			
			addLine((getNotSet ? "return " : "")+lookupBbGetSet(f, getNotSet)+"(buf, msg, "+ NameMaker.makeFieldIndexName(f) + ");");
			
		} else {
		
			addLine("uint32_t i = "+NameMaker.makeFieldIndexName(f) + ";" );
			for(Index pi : pis) {
				String name = NameMaker.makeIndexName(pi);
				if(pi.p instanceof ArrayField) {
					addLine("i += "+NameMaker.makeMultipleFieldElementByteCountName(pi) + " * " + name + ";");
				} else if(pi.p instanceof SequenceField) {
					
					addLine("i = getBbSequenceElementIndex(buf, msg, i, "+name+");");
					if(!getNotSet) {
						addLine("if(i == 0){");
						indent();
						addLine("return;//bail because a sequence was not initialized");
						closeBrace();
					}
					
				}
				
			}
			
			addLine((getNotSet ? "return " : "")+lookupBbGetSet(f, getNotSet)+"(buf, msg, i"+ val + ");");
		}
		
		outdent();
		addLine("}");
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
		
		for(Index pi : pis) {
			String name = NameMaker.makeIndexName(pi);
			
			SymbolName pName = pi.p.getName(); 
			if(pName == null) {
				pName = pi.p.getParent().getName();
			}
			
			if(pi.p instanceof ArrayField && pi.p.asType(ArrayField.class).getNumber().length > 1) {
				comments.add("@param "+name+" - index "+pi.i+" of "+ pName.toLowerCamel()+" "+pi.type+". Valid values: 0 to "+(pi.n - 1));
			} else {
				comments.add("@param "+name+" - index of "+ pName.toLowerCamel()+" "+pi.type+"." + (pi.n >= 0 ? " Valid values: 0 to "+(pi.n - 1) : ""));
			}
			paramList += ", uint32_t "+name;
	
				
			
		}

		paramList += ", char * string";
		
	
		
			
		comments.add("@param string - the string to copy "+(toNotFrom ? "from" : "to"));//note that when we're copying to the message, we copy from the string parameter
			
	
		

	
		
		
		addDocComment(comments);
		
		addLine("void "+NameMaker.makeStringCopierName(f, toNotFrom)+"("+paramList+")"+(protoNotDef ? ";" : "{"));
		
		if(protoNotDef) {
			return;
		}
		
		indent();
		addLine("uint32_t i = " + NameMaker.makeFieldIndexName(f) + ";");
		for(Index pi : pis) {
			String name = NameMaker.makeIndexName(pi);
			if(pi.p instanceof ArrayField) {
				addLine("i += "+NameMaker.makeMultipleFieldElementByteCountName(pi) + " * " + name + ";");
			} else if(pi.p instanceof SequenceField) {
				
				addLine("i = getBbSequenceElementIndex(buf, msg, i, "+name+");");
				
			}
			
		}
		

		
		if(toNotFrom) {
			//we're copying to the message
			addLine("uint32_t lenW = buf->length;//the current end of the message which will now be the length word of the string");
			addLine("uint32_t si = lenW + 4;//this will be the start of the string data");
			addLine("uint32_t j = 0;//this will be the string length by the time we're done");
			
			addLine("for(; j < "+NameMaker.makeStringMaxLengthName(f)+"; ++j){");
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
		
		for(Index pi : pis) {
			String name = NameMaker.makeIndexName(pi);
			SymbolName pName = pi.p.getName(); 
			if(pName == null) {
				pName = pi.p.getParent().getName();
			}
			
			if(pi.p instanceof ArrayField && pi.p.asType(ArrayField.class).getNumber().length > 1) {
				comments.add("@param "+name+" - index "+pi.i+" of "+ pName.toLowerCamel()+" "+pi.type+". Valid values: 0 to "+(pi.n - 1));
			} else {
				comments.add("@param "+name+" - index of "+ pName.toLowerCamel()+" "+pi.type+"." + (pi.n >= 0 ? " Valid values: 0 to "+(pi.n - 1) : ""));
			}
			paramList += ", uint32_t "+name;
	
				
			
		}

		
	
		

	
		
		
		addDocComment(comments);
		
		addLine("uint32_t "+NameMaker.makeStringLengthGetterName(f)+"("+paramList+")"+(protoNotDef ? ";" : "{"));
		
		if(protoNotDef) {
			return;
		}
		
		indent();
		addLine("uint32_t i = " + NameMaker.makeFieldIndexName(f) + ";");
		for(Index pi : pis) {
			String name = NameMaker.makeIndexName(pi);
			if(pi.p instanceof ArrayField) {
				addLine("i += "+NameMaker.makeArraySizeName(pi) + " * " + name + ";");
			} else if(pi.p instanceof SequenceField) {
				
				addLine("i = getBbSequenceElementIndex(buf, msg, i, "+name+");");
				
			}
			
		}
	
			

		//we're copying from the message
		addLine("return (uint32_t)getUint16(buf, msg, i);");
		
		
		

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
			case FILLER:
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

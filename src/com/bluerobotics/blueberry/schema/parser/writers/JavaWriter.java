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
import java.util.ArrayList;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.fields.ArrayField;
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
import com.bluerobotics.blueberry.schema.parser.fields.SequenceField;
import com.bluerobotics.blueberry.schema.parser.fields.StringField;
import com.bluerobotics.blueberry.schema.parser.fields.StructField;
import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;
import com.bluerobotics.blueberry.schema.parser.parsing.BlueberrySchemaParser;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;


/**
 * Autogenerates Java code stubs based on a parsed field structure
 */
public class JavaWriter extends SourceWriter {

//	private SymbolName m_packageName;
//	private String m_constantsName;
//	private String m_bitIndexEnumName;
//	private String m_fieldIndexEnumName;
//	private String m_packetBuilderName;
//	private String m_consumerInterfaceName;
//	private String m_keyEnumName;
//	private String m_consumerManagerName;
//	private String m_packetRecieverName;

	public JavaWriter(File dir, BlueberrySchemaParser parser, String header) {
		super(dir, parser, header);
	}

	@Override
	public void write() {
		ArrayList<BlueModule> modules = getParser().getModules();
//		m_constantsName = top.getName().append("constants").toUpperCamel();
//		m_bitIndexEnumName = top.getName().append("bit","index").toUpperCamel();
//		m_fieldIndexEnumName = top.getName().append("field","index").toUpperCamel();
//		m_packetBuilderName = top.getName().append("builder").toUpperCamel();
//		m_keyEnumName = top.getName().append("block","keys").toUpperCamel();
//		m_consumerInterfaceName = top.getName().append("consumer").toUpperCamel();
//		m_consumerManagerName = top.getName().append("consumer","manager").toUpperCamel();
//		m_packetRecieverName = top.getName().append("Receiver").toUpperCamel();
//
		modules.forEach(m -> {
			writeConstantsFile(m);
			m.getMessages().forEachOfType(MessageField.class, false, msg -> {
				writeMessageFile(m, msg);
				
			});
			
		});
		writeMessageLookup(modules);	
		
//		writeBlockParsers(top, headers);
//		writeParserInterface(top, headers);
//		writeConsumerManager(top, headers);
//		writePacketReceiver(top, headers);


	}
	/**
	 * makes a class that maps module/message keys to message constructors
	 * @param modules
	 */
	private void writeMessageLookup(ArrayList<BlueModule> modules) {
		// TODO Auto-generated method stub
		
	}


	private void writeInterfaceMethod(StructField bf) {
		String className = bf.getName().append("parser").toUpperCamelString();
		addDocComment("consume the "+className+" block.\n"+bf.getComment());
		addLine("public void consume("+className+" p);");
	}




	private void writeConstantsFile(BlueModule m) {
		startFile(m, getHeader());
		addLine();
		addLine("import com.bluerobotics.blueberry.transcoder.java.BitIndex;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.FieldIndex;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.EnumLookup;");
		addLine();

		addLine("public interface "+NameMaker.makeJavaConstantInterface(m)+" {");
		indent();
		
		writeConstants(m);
		
		
//		writeFieldIndexEnum(top);
//		writeBitIndexEnum(top);
		writeOtherEnums(m);
		closeBrace();
		writeToFile(NameMaker.makePackageName(m).toLowerSnake("/")+"/"+NameMaker.makeJavaConstantInterface(m)+".java");



	}

	private void writeOtherEnums(BlueModule m) {
		m.getDefines().forEachOfType(EnumField.class, false, f -> {
			writeEnum(f);

		});
	
	}

	private void writeEnum(EnumField f) {
		List<NameValue> nvs = f.getNameValues();

		String comment = f.getComment();
		String name = NameMaker.makeEnumName(f);
		String type = lookupTypeForJavaType(f);

		addDocComment(comment);
		addLine("public enum "+name+" {");
		indent();
		for(NameValue nv : nvs) {
			String c = nv.getComment();
			if(c != null && !c.isBlank()) {
				c = "//"+c;
			} else {
				c = "";
			}
			addLine(nv.getName().toUpperSnakeString()+"(("+type+")"+nv.getValueAsHex()+"),"+c);
		}
		addLine(";");
		addLine("private static EnumLookup<"+name+"> m_lookup = new EnumLookup<"+name+">();");
		addLine("private int value;");
		addLine("private "+name+"(int v){");
		indent();
		addLine("value = v;");
		closeBrace();
		addLine("public int getValue(){");
		indent();
		addLine("return value;");
		closeBrace();
		addLine("public static "+name+" lookup(int i){");
		indent();
		addLine("if(m_lookup.size() == 0) {");
		indent();
		addLine("for("+name+" e : values()) {");
		indent();
		addLine("m_lookup.add(e.getValue(), e);");
		closeBrace();
		closeBrace();
		addLine("return m_lookup.lookup(i);");
		closeBrace();
		closeBrace();

	}

//	private String lookupTypeForFuncName(BaseField f) {
//		String result = "";
//		switch(f.getType()) {
//		case ARRAY:
//			break;
//		case BLOCK:
//			break;
//		case BOOL:
//			result = "bool";
//			break;
//		case BOOLFIELD:
//			result = "byte";
//			break;
//		case COMPOUND:
//			result = "int";
//			break;
//		case FLOAT32:
//			result = "float";
//			break;
//		case INT16:
//			result = "short";
//			break;
//		case INT32:
//			result = "int";
//			break;
//		case INT8:
//			result = "byte";
//			break;
//		case UINT16:m_bitIndexEnumName
//			result = "short";
//			break;
//		case UINT32:
//			result = "int";
//			break;
//		case UINT8:
//			result = "byte";
//			break;
//
//		}
//		return result;
//	}
	/**
	 * looks up an object type to represent the type of the specified base field
	 * This will not return a primitive type, instead it looks up the equivalent class type
	 * @param f
	 * @return
	 */
	private String lookupObjectTypeForJavaVars(Field f){
		String result = "";
		if(f instanceof EnumField) {
			result = NameMaker.makeEnumName((EnumField)f);
		} else {
			switch(f.getTypeId()) {
			case ARRAY:
				break;

			case BOOL:
				result = "Boolean";
				break;
			case FLOAT32:
			case FLOAT64:

				result = "Double";
				break;
			case BOOLFIELD:
			case INT16:
			case INT32:
			case INT8:
			case UINT16:
			case UINT32:
			case UINT8:
				result = "Integer";
				break;
			case DEFERRED:
				break;
			case DEFINED:
				break;
			case FILLER:
				break;
			case MESSAGE:
				break;
			case SEQUENCE:
				break;
			case STRING:
				result = "String";
				break;
			case STRUCT:
				break;
			case UINT64:
			case INT64:
				result = "Long";
				break;
		

			}
		}
		return result;
	}
	private String lookupTypeForJavaVars(Field f) {
		String result = "";
		if(f instanceof EnumField) {
			result = NameMaker.makeEnumName((EnumField)f);
		} else {
			switch(f.getTypeId()) {
			case ARRAY:
				break;
			
			case BOOL:
				result = "boolean";
				break;
			case BOOLFIELD:
				result = "int";
				break;
			
			case FLOAT32:
				result = "double";
				break;
			case INT16:
				result = "int";
				break;
			case INT32:
				result = "int";
				break;
			case INT8:
				result = "int";
				break;
			case UINT16:
				result = "int";
				break;
			case UINT32:
				result = "int";
				break;
			case UINT8:
				result = "int";
				break;
			case DEFERRED:
				break;
			case DEFINED:
				break;
			case FILLER:
				break;
			case FLOAT64:
				result = "double";
				break;
			case INT64:
				result = "long";
				break;
			case MESSAGE:
				break;
			case SEQUENCE:
				break;
			case STRING:
				break;
			case STRUCT:
				break;
			case UINT64:
				result = "long";//TODO:this is not strictly true
				break;
			

			}
		}
		return result;
	}
	private String lookupTypeForJavaType(EnumField f) {
		String result = "";

		switch(f.getTypeId()) {
		case ARRAY:
			break;
		case BOOL:
			result = "boolean";
			break;
		case BOOLFIELD:
			result = "int";
			break;
		case FLOAT32:
			result = "double";
			break;
		case INT16:
			result = "short";
			break;
		case INT32:
			result = "int";
			break;
		case INT8:
			result = "byte";
			break;
		case UINT16:
			result = "short";
			break;
		case UINT32:
			result = "int";
			break;
		case UINT8:
			result = "byte";
			break;
		case DEFERRED:
			break;
		case DEFINED:
			break;
		case FILLER:
			break;
		case FLOAT64:
			result = "double";
			break;
		case INT64:
			result = "long";
			break;
		case MESSAGE:
			break;
		case SEQUENCE:
			break;
		case STRING:
			result = "String";
			break;
		case STRUCT:
			break;
		case UINT64:
			result = "long";//TODO:this is not strictly true
			break;
		default:
			break;


		}
		return result;
	}


	private void writeConstants(BlueModule m) {

		m.getMessages().forEachOfType(MessageField.class, false, mf -> {
			

			
			addDocComment(mf.getComment());
			addLine("public static final int "+ NameMaker.makeMessageKeyName(mf)+" = "+makeFullMessageKey(mf)+";");
		
		});
	}

	@Override
	protected void startFile(BlueModule m, String... hs) {
		super.startFile(m, hs);
		addLine("package " + NameMaker.makePackageName(m).toLowerSnake(".")+";");


	}

	private void writeMessageFile(BlueModule m, MessageField msg) {
		String messageName = NameMaker.makeJavaMessageClass(msg).toString();
		startFile(m, getHeader());
		addLine();
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryMessage;");

		addLine();
		addBlockComment("A class to read and write a "+messageName);
		addLine("public class "+messageName+" extends BlueberryMessage {");
		indent();
		
		//add field indeces
		
		
	
		


		addLineComment("This is the unique key to identify this type of message");
		addLine("private static final int "+NameMaker.makeMessageModuleMessageConstant(msg)+" = "+WriterUtils.formatAsHex(msg.getModuleMessageKey())+";");
		addLine();
		addLine("private static final int "+NameMaker.makeMessageMaxOrdinalName(msg)+" = "+msg.getMaxOrdinal()+";");
		addLine();
		addLine("private static final int "+NameMaker.makeMessageLengthName(msg)+" = "+msg.getPaddedByteCount()+";");
		addLine();
		
		addLineComment("These values are used to index into this message to access the various fields.");
		
		FieldList fs = msg.getUsefulChildren();
		fs.forEach(f -> {
			if(getType(f) != null || f instanceof ArrayField || f instanceof SequenceField) {
				int fi = f.getIndex();
				if(f.getBitCount() == 1) {
					fi = f.getParent().getIndex();
				}
				addLine("private static final " + NameMaker.makeFieldIndexName(f) + " = "+fi+";");
					
			} else {
				System.out.println("JavaWriter.writeMessageFile not sure how to do index constant for --> "+f);
			}
		});
		
		addLine();
		addLineComment("The following values represent the ordinals of the fields of this message.");
		addLineComment("This corresponds to the order that they were defined in the schema");
		fs.forEach(f -> {
			int fi = f.getOrdinal();
			if(f.getBitCount() == 1) {
				fi = f.getParent().getOrdinal();
			}
			addLine("private static final " + NameMaker.makeFieldOrdinalName(f) + " = "+fi+";");
		});
		//sequence stuff
		if(fs.isChildrenOfType(SequenceField.class, false)) {
			addLine();
			addLineComment("Sequence Element Byte Counts");
			fs.forEachOfType(SequenceField.class, false, sf -> {
				addLine("private static final " + NameMaker.makeMultipleFieldElementByteCountName(sf.getIndeces().getFirst()) + " = "+sf.getPaddedByteCount()+";");
			});
		}
		//string stuff
		if(fs.isChildrenOfType(StringField.class, false)) {
			addLine();
			addLineComment("String max length constants");
			fs.forEachOfType(StringField.class, false, sf -> {
				addLine("private static final " + NameMaker.makeStringMaxLengthName(sf) + " = "+sf.getMaxSize()+";");
			});
		}
		//array stuff
		if(fs.isChildrenOfType(ArrayField.class, false)) {
			addLine();
			addLineComment("Add array sizes and element byte count");
			fs.forEachOfType(ArrayField.class, false, af -> {
				List<Index> is = af.getIndeces();
				int n = is.size();
				if(n == 1) {
					Index pi = is.get(0);
					addLine("private static final  " + NameMaker.makeArraySizeName(pi) + " = "+pi.n+";");
					addLine("private static final " + NameMaker.makeMultipleFieldElementByteCountName(pi) + " = "+pi.bytesPerElement+";");
				} else {
					for(Index pi : is) { 
						addLine("private static final " + NameMaker.makeArraySizeName(pi) + " ("+pi.n+")");
						addLine("private static final " + NameMaker.makeMultipleFieldElementByteCountName(pi) + " = "+pi.bytesPerElement+";");
					}
				}
			});
		}
		
		
		
		addMessageConstructor(msg);
		addTxMessageMaker(msg, true);
		addTxMessageMaker(msg, false);
		addRxMessageWrapper(msg);

		
		
		closeBrace();
		writeToFile(NameMaker.makePackageName(m).toLowerSnake("/")+"/"+messageName+".java");
		
	}
	
	/**
	 * makes a protected message constructor, either for tx or rx
	 * doesn't set up header
	 * @param mf
	 * @param txNotRx
	 */
	private void addMessageConstructor(MessageField mf) {
		String messageName = NameMaker.makeJavaMessageClass(mf).toString();
		ArrayList<String> comments = new ArrayList<>();
		comments.add("A constructor to create a "+messageName+".");
		comments.add("This does the bare minimum: it just wraps a buffer in a message.");
		comments.add("This is private so it should never be called outside of this class.");
		comments.add("The intent is that the static factory methods would be used instead.");
		comments.add(mf.getComment());
		
		comments.add("@param buf - the message buffer to add the message to");
		String paramList = "BlueberryBuffer buf";
		ArrayList<Field> fs = new ArrayList<>();
		ArrayList<Field> ss = new ArrayList<>();
		//first make a list of all top-level fields that are not strings or parent fields
		//but also add contents of boolfieldfields
		
		
		
		
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		addLine("private "+messageName+"("+paramList+") {");
		
		//now do contents of function
		indent();
	
		addLine("super(buf);");
		
		
		outdent();
		addLine("}");
	}
	/**
	 * makes a protected message constructor, either for tx or rx
	 * doesn't set up header
	 * @param mf
	 * @param txNotRx
	 */
	private void addRxMessageWrapper(MessageField mf) {
		String messageName = NameMaker.makeJavaMessageClass(mf).toString();
		ArrayList<String> comments = new ArrayList<>();
		comments.add("A method to wrap a buffer of received data in a "+messageName+".");
		comments.add(mf.getComment());
		
		comments.add("@param buf - the message buffer to add the message to");
		String paramList = "BlueberryBuffer buf";
		ArrayList<Field> fs = new ArrayList<>();
		ArrayList<Field> ss = new ArrayList<>();
		//first make a list of all top-level fields that are not strings or parent fields
		//but also add contents of boolfieldfields
		
		
		
		
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		addLine("public static "+messageName +" wrap"+"("+paramList+") {");
		
		//now do contents of function
		indent();
	
		addLine(messageName + " msg = new "+messageName+"(buf);");
		//TODO: add stuff to check the module/message key and stuff
		
		addLine("return msg;");
		outdent();
		addLine("}");
	}
	private void addTxMessageMaker(MessageField mf, boolean params) {
		String messageName = NameMaker.makeJavaMessageClass(mf).toString();
		ArrayList<String> comments = new ArrayList<>();
		comments.add("A constructor to create " + (params ? "a " : "an empty ") +messageName+" for transmission.");
		comments.add(mf.getComment());
		
		comments.add("@param buf - the message buffer to add the message to");
		String paramList = "BlueberryBuffer buf";
		ArrayList<Field> fs = new ArrayList<>();
		ArrayList<Field> ss = new ArrayList<>();
		//first make a list of all top-level fields that are not strings or parent fields
		//but also add contents of boolfieldfields
		if(params) {
			mf.getUsefulChildren().forEach(true, f -> {
				
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
		}
		
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
	
		
		
		
		
		
		addDocComment(comments.toArray(new String[comments.size()]));
		String prefix = params ? "" : "Empty";
		addLine("public static " + messageName + " make"+prefix+"("+paramList+") {");
		
		//now do contents of function
		indent();
		String maxOrd = params ? NameMaker.makeMessageMaxOrdinalName(mf) : "MIN_MAX_ORDINAL";
		String mLen = params ? NameMaker.makeMessageLengthName(mf) : "MIN_MESSAGE_LENGTH";
		addLine(messageName + " msg = new "+messageName+"(buf);");

		addLine("msg.makeHeader( "+NameMaker.makeMessageKeyName(mf)+", "+maxOrd+", "+mLen+");");
		
		for(Field f : fs) {
				
			SymbolName paramName = NameMaker.makeParamName(f);
	
			
	
			
			
			addLine("msg.m_buf."+makeBbGetSet(f, false)+"("+NameMaker.makeFieldIndexName(f)+", "+paramName+");");
			
			
		}
		for(Field f : ss) {
			List<Index> pis = MultipleField.getIndeces(f);
			if(pis.size() == 0) {
				//zero the index field of each string and sequence
				String s = (f.getTypeId() == TypeId.SEQUENCE) ? "sequence" : "string";
				addLine("msg.m_buf.setUint16(buf, msg, "+NameMaker.makeFieldIndexName(f)+", INVALID_BLOCK);//clear "+s+" header");
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
					addLine("msg.m_buf.setUint16(buf, msg, "+NameMaker.makeFieldIndexName(f)+" + "+offset+", INVALID_BLOCK);//clear "+s+" header.");
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
		
		addLine("return msg;");
		outdent();
		addLine("}");
	}
	private String makeBbGetSet(Field f, boolean b) {
		SymbolName result = SymbolName.fromCamel(b ? "get" : "set");
		
		switch(f.getTypeId()) {
		default:
		case ARRAY:
		case BOOLFIELD:
		case DEFERRED:
		case DEFINED:
		case FILLER:
		case MESSAGE:
		case SEQUENCE:
		case STRING:
		case STRUCT:
			throw new RuntimeException("Should never have done this!");
		case BOOL:
			result.append("bit");
			break;
		case FLOAT32:
			result.append("float32");
			break;
		case FLOAT64:
			result.append("flaot64");
			break;
		case INT16:
			result.append("int16");
			break;
		case INT32:
			result.append("int32");
			break;
		case INT64:
			result.append("int64");
			break;
		case INT8:
			result.append("int8");
			break;
		case UINT16:
			result.append("uint16");
			break;
		case UINT32:
			result.append("uint32");
			break;
		case UINT64:
			result.append("uint64");
			break;
		case UINT8:
			result.append("uint8");
			break;
		
		}
		
		return result.toLowerCamelString();
	}

	private String getType(Field f) {
		String result = null;
		if(true) {
		
		
			switch(f.getTypeId()) {
			
			case BOOL:
				result = "boolean";
				break;
			case FLOAT32:
				result = "double";
				break;
			case FLOAT64:
				result = "double";
				break;
			case INT16:
				result = "int";
				break;
			case INT32:
				result = "int";
				break;
			case INT64:
				result = "long";
				break;
			case INT8:
				result = "int";
				break;
			case STRING:
				result = "String";
				break;
			case UINT16:
				result = "int";
				break;
			case UINT32:
				result = "long";
				break;
			case UINT64:
				result = "long";
				break;
			case UINT8:
				result = "int";
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
	

}

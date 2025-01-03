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

import com.bluerobotics.blueberry.schema.parser.structure.ArrayField;
import com.bluerobotics.blueberry.schema.parser.structure.BaseField;
import com.bluerobotics.blueberry.schema.parser.structure.BlockField;
import com.bluerobotics.blueberry.schema.parser.structure.BoolField;
import com.bluerobotics.blueberry.schema.parser.structure.BoolFieldField;
import com.bluerobotics.blueberry.schema.parser.structure.CompoundField;
import com.bluerobotics.blueberry.schema.parser.structure.FieldName;
import com.bluerobotics.blueberry.schema.parser.structure.FixedIntField;

/**
 * 
 */
public class JavaWriter extends SourceWriter {
	private FieldName m_packageName;
	private String m_constantsName;
	private String m_bitIndexEnumName;
	private String m_fieldIndexEnumName;
	private String m_packetClassName;
	private String m_keyEnumName;

	public JavaWriter(File dir) {
		super(dir);
	}

	@Override
	public void write(BlockField top, String... headers) {
		m_packageName = FieldName.fromDot("com.bluerobotics.blueberry").addSuffix(top.getName().toLowerCase());
		m_constantsName = top.getName().addSuffix("constants").toUpperCamel();
		m_bitIndexEnumName = top.getName().addSuffix("bit","index").toUpperCamel();
		m_fieldIndexEnumName = top.getName().addSuffix("field","index").toUpperCamel();
		m_packetClassName = top.getName().toUpperCamel();
		m_keyEnumName = top.getName().addSuffix("block","keys").toUpperCamel();



		writeConstantsFile(top, headers);
		writePacketClass(top, headers);
		
		
	}
	
	

	private void writePacketClass(BlockField top, String[] headers) {
		startFile(headers);
		addLine();
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacket;");
		addLine();


		
		addLine("public class "+m_packetClassName+" extends BlueberryPacket implements "+m_constantsName+"{");
		indent();
		
		addLine("public "+m_packetClassName + "(int size){");
		indent();
		addLine("super(size);");
		outdent();
		addLine("}");
		
		
		//first get length, preamble and crc fields
		BaseField preamble = top.getHeaderField("preamble");
		BaseField length = top.getHeaderField("length");
		BaseField crc = top.getHeaderField("crc");
		String preambleConstantName = makeBaseFieldNameRoot(preamble).addSuffix("VALUE").toUpperSnake();
		
		addLine();
		addLine("@Override");
		addLine("public int getPublishedCrc(){");
		indent();
		addLine("return "+FieldName.fromCamel("get").addSuffix(lookupType(crc)).toLowerCamel()+"("+m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(crc).toUpperSnake()+");");

		outdent();
		addLine("}");
		
		addLine();
		addLine("@Override");
		addLine("public void setPublishedCrc(int crc){");
		indent();
		addLine(FieldName.fromCamel("put").addSuffix(lookupType(crc)).toLowerCamel()+"("+m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(crc).toUpperSnake()+", ("+lookupType(crc)+")crc);");

		outdent();
		addLine("}");
	
		
		addLine();
		addLine("@Override");
		addLine("public int getStartWord(){");
		indent();
		addLine("return "+preambleConstantName+";");
		outdent();
		addLine("}");
		
		addLine();
		addLine("@Override");
		addLine("public int getPublishedWordLength(){");
		indent();
		
		addLine("return "+FieldName.fromCamel("get").addSuffix(lookupType(length)).toLowerCamel()+"("+m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(length).toUpperSnake()+");");
		outdent();
		addLine("}");
		
		addLine();
		addLine("@Override");
		addLine("public void setPublishedWordLength(int length){");
		indent();
		
		addLine(FieldName.fromCamel("put").addSuffix(lookupType(length)).toLowerCamel()+"("+m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(length).toUpperSnake()+", ("+lookupType(length)+")length);");
		outdent();
		addLine("}");
		
		
		
		addBlockMethods(top);
		
		addArrayMethods(top);
		

		outdent();
		addLine("}");
		writeToFile(m_packageName.toPath() + m_packetClassName,"java");	
	}

	private void addArrayMethods(BlockField top) {
		List<ArrayField> afs = top.getAllArrayFields();
		for(ArrayField af : afs) {
			addArrayAdder(af);
		}
	}

	

	private void addBlockMethods(BlockField top) {
		List<BlockField> bfs = top.getAllBlockFields();
		
		for(BlockField bf : bfs) {
			if(bf != top) {//ignore top level
				addBlockAdder(bf, true);
				addBlockAdder(bf, false);
			}
		}
	}
	
	private void addArrayAdder(ArrayField af) {
		String blockName = af.getName().toUpperCamel();
		String comment = "Adds a new "+blockName+" to the specified packet.\n"+af.getComment();
		String functionName = "add"+blockName;
		List<BaseField> fs = af.getNamedBaseFields();
		FixedIntField keyF = (FixedIntField)af.getHeaderField("key");
		BaseField lengthF = af.getHeaderField("length");
		BaseField repeatsF = af.getHeaderField("repeats");
		String paramList = "";
		
		
		boolean firstTime = true;
		for(BaseField f : fs) {
			paramList += (firstTime ? "" : ", ")+lookupType(f)+" "+f.getName().toLowerCamel();
			firstTime = false;
		}
		
		addDocComment(comment);
		addLine("void "+functionName+"("+paramList+"){");
		indent();
		
		//add method to set key
		String keyType = lookupType(keyF);
		String keyIndex = makeBaseFieldNameRoot(keyF).toUpperSnake();
		String keyValue = makeKeyName(keyF);
		String keyFuncName = FieldName.fromCamel("put").addSuffix(keyType).toLowerCamel();
	
		addLine(keyFuncName+"("+m_fieldIndexEnumName+"."+keyIndex+", "+m_keyEnumName+"."+keyValue+".getValue());");
		

		//add method to set length
		String lengthType = lookupType(lengthF);
		int lengthValue = fs.size() + 1;
		String lengthIndex = makeBaseFieldNameRoot(lengthF).toUpperSnake();
		String lengthFuncName = FieldName.fromCamel("put").addSuffix(lengthType).toLowerCamel();
		//add key
		addLine(lengthFuncName+"("+m_fieldIndexEnumName+"."+lengthIndex+", "+lengthValue+");");
		
		for(BaseField f : fs) {
			boolean bit = f instanceof BoolField;
			
			String fType = bit ? "bool" : lookupType(f);
			String fValue = f.getName().toLowerCamel();
			String fIndex = makeBaseFieldNameRoot(f).toUpperSnake();
			String fFuncName = FieldName.fromCamel("put").addSuffix(fType).toLowerCamel();
			String enumName = bit ? m_bitIndexEnumName : m_fieldIndexEnumName;
			addLine(fFuncName+"("+enumName+"."+fIndex+", "+fValue+");");
		}
		
		
		addLine("addWords("+lengthValue+");");
		
		
		
		outdent();
		addLine("}");
	}
	private void addBlockAdder(BlockField bf, boolean withParamsNotWithout) {
		String blockName = bf.getName().toUpperCamel();
		String comment = "Adds a new "+blockName+" to the specified packet.\n"+bf.getComment();
		String functionName = "add"+blockName;
		List<BaseField> fs = bf.getNamedBaseFields();
		FixedIntField keyF = (FixedIntField)bf.getHeaderField("key");
		BaseField lengthF = bf.getHeaderField("length");
		BaseField repeatsF = bf.getHeaderField("repeats");
		String paramList = "";
		if(withParamsNotWithout && fs.size() == 0) {
			//don't do anything if this block does not have parameters but we're doing the version with params
			return;
		}
		if(withParamsNotWithout) {
			boolean firstTime = true;
			for(BaseField f : fs) {
				paramList += (firstTime ? "" : ", ")+lookupType(f)+" "+f.getName().toLowerCamel();
				firstTime = false;
			}
		}
		addDocComment(comment);
		addLine("void "+functionName+"("+paramList+"){");
		indent();
		
		//add method to set key
		String keyType = lookupType(keyF);
		String keyIndex = makeBaseFieldNameRoot(keyF).toUpperSnake();
		String keyValue = makeKeyName(keyF);
		String keyFuncName = FieldName.fromCamel("put").addSuffix(keyType).toLowerCamel();
	
		addLine(keyFuncName+"("+m_fieldIndexEnumName+"."+keyIndex+", "+m_keyEnumName+"."+keyValue+".getValue());");
		

		//add method to set length
		String lengthType = lookupType(lengthF);
		int lengthValue = withParamsNotWithout ? fs.size() + 1 : 1;
		String lengthIndex = makeBaseFieldNameRoot(lengthF).toUpperSnake();
		String lengthFuncName = FieldName.fromCamel("put").addSuffix(lengthType).toLowerCamel();
		//add key
		addLine(lengthFuncName+"("+m_fieldIndexEnumName+"."+lengthIndex+", "+lengthValue+");");
		if(withParamsNotWithout) {
			for(BaseField f : fs) {
				boolean bit = f instanceof BoolField;
				
				String fType = bit ? "bool" : lookupType(f);
				String fValue = f.getName().toLowerCamel();
				String fIndex = makeBaseFieldNameRoot(f).toUpperSnake();
				String fFuncName = FieldName.fromCamel("put").addSuffix(fType).toLowerCamel();
				String enumName = bit ? m_bitIndexEnumName : m_fieldIndexEnumName;
				addLine(fFuncName+"("+enumName+"."+fIndex+", "+fValue+");");
			}
		}
		
		addLine("addWords("+lengthValue+");");
		
		
		
		outdent();
		addLine("}");
		
		
		
	}

	private void writeConstantsFile(BlockField top, String[] headers) {
		startFile(headers);
		addLine();
		addLine("import com.bluerobotics.blueberry.transcoder.java.BitIndex;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.FieldIndex;");
		addLine();
		
		addLine("public interface "+m_constantsName+" {");
		indent();
		
		writeConsants(top);
		writeKeyEnum(top);
		writeFieldIndexEnum(top);
		writeBitIndexEnum(top);
		outdent();
		addLine("}");
		writeToFile(m_packageName.toPath() + m_constantsName,"java");	
		
		

	}

	private String lookupType(BaseField f) {
		String result = "";
		switch(f.getType()) {
		case ARRAY:
			break;
		case BLOCK:
			break;
		case BOOL:
			result = "boolean";
			break;
		case BOOLFIELD:
			result = "byte";
			break;
		case COMPOUND:
			result = "int";
			break;
		case FLOAT32:
			result = "float";
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
	
		}
		return result;
	}
	private void writeConsants(BlockField top) {
		ArrayList<FixedIntField> fifs = new ArrayList<FixedIntField>();
		
		top.scanThroughHeaderFields(f -> {
			if(f instanceof FixedIntField && !f.getName().toLowerCamel().equals("key")) {
				fifs.add((FixedIntField)f);
			}
		}, true);
		for(FixedIntField fif : fifs) {
			
			String name = makeBaseFieldNameRoot(fif).addSuffix("VALUE").toUpperSnake();
			addDocComment(fif.getComment());
			addLine("public static final int "+name+" = "+WriterUtils.formatAsHex(fif.getValue())+";");
		}
	}

	@Override
	protected void startFile(String... hs) {
		// TODO Auto-generated method stub
		super.startFile(hs);
		addLine("package " + m_packageName.toDot()+";");
		

	}

	private void writeBitIndexEnum(BlockField top) {
		
		

		
		//make a list of all the fields
		List<BoolField> fs = new ArrayList<BoolField>();
		//first header fields
		top.scanThroughBaseFields(f -> {
			if(f instanceof BoolField) {
				fs.add((BoolField)f);
			}
		}, true);
		
		//now generate the enum details
		addDocComment("An enum of all the field indeces for the "+top.getName()+" schema.");
		addLine("public enum "+m_bitIndexEnumName+" implements BitIndex {");
		indent();
		for(BoolField f : fs) {
			String name = makeBaseFieldNameRoot(f).toUpperSnake();
			int i;
			
			BoolFieldField bff = (BoolFieldField)f.getParent();
			i = bff.getIndex();
			
			addLine(name + "("+i+", "+ f.getIndex()+"),");
		}
		addLine(";");
		addLine("private int index;");
		addLine("private int bitIndex;");
		addLine("private "+m_bitIndexEnumName+"(int i, int bi){");
		indent();
		addLine("index = i;");
		addLine("bitIndex = bi;");
		outdent();
		addLine("}");
		addLine("@Override");
		addLine("public int getIndex(){");
		indent();
		addLine("return index;");
		outdent();
		addLine("}");
		addLine("@Override");
		addLine("public int getBitIndex(){");
		indent();
		addLine("return bitIndex;");
		outdent();
		addLine("}");
		addLine("@Override");
		addLine("public int getBits(){");
		indent();
		addLine("return 1;");
		outdent();
		addLine("}");
		outdent();
		addLine("};");
		
		addLine();
	}

	private void writeFieldIndexEnum(BlockField top) {
		//make a list of all the fields
		List<BaseField> fs = new ArrayList<BaseField>();
		//first header fields
		top.scanThroughHeaderFields(bf -> {
			if(bf.getName() != null && !(bf instanceof CompoundField) && !(bf instanceof BoolField)) {
				boolean found = false;
				for(BaseField f : fs) {
					if(f.getName().equals(bf.getName())) {
						if(f.getCorrectParentName().equals(bf.getCorrectParentName())){
							found = true;
						}
					}
				}
				if(!found) {
					fs.add(bf);
				}
			}
		}, true);
		
		
		//now base fields
		top.scanThroughBaseFields((bf) -> {
			if(bf.getName() != null && !(bf instanceof CompoundField) && !(bf instanceof BoolField)) {
				fs.add(bf);
			}
		}, false);	
		
		
		//now generate the enum details
		addDocComment("An enum of all the field indeces for the "+top.getName()+" schema.");
		addLine("public enum "+m_fieldIndexEnumName+" implements FieldIndex {");
		indent();
		for(BaseField f : fs) {
			String name = makeBaseFieldNameRoot(f).toUpperSnake();
			int i;
			if(f instanceof BoolField) {
				BoolFieldField bff = (BoolFieldField)f.getParent();
				i = bff.getIndex();
			} else {
				i = f.getIndex();
			}
			addLine(name + "("+i+", " +f.getBitCount()+"),");
		}
		addLine(";");
		addLine("private int index;");
		addLine("private int bits;");
		addLine("private "+m_fieldIndexEnumName+"(int i, int b){");
		indent();
		addLine("index = i;");
		addLine("bits = b;");
		outdent();
		addLine("}");
		addLine("@Override");
		addLine("public int getIndex(){");
		indent();
		addLine("return index;");
		outdent();
		addLine("}");
		addLine("@Override");
		addLine("public int getBits(){");
		indent();
		addLine("return bits;");
		outdent();
		addLine("}");
		outdent();
		addLine("};");
		
		addLine();
		
	}

	private void writeKeyEnum(BlockField top) {
		List<FixedIntField> keys = getBlockKeys(top);
		addDocComment("An enum of all the block keys for the "+top.getName()+" schema.");
		addLine("public enum "+m_keyEnumName+" {");
		indent();
		for(FixedIntField key : keys) {
			String name = makeKeyName(key);
			addDocComment(key.getComment());
			addLine(name + "("+WriterUtils.formatAsHex(key.getValue())+"),");
		}
		addLine(";");
		addLine("private int value;");
		addLine("private "+m_keyEnumName+"(int i){");
		indent();
		addLine("value = i;");
		outdent();
		addLine("}");
	
		addLine("public int getValue(){");
		indent();
		addLine("return value;");
		outdent();
		addLine("}");
		outdent();
		addLine("};");
		addLine();
		
	}

}

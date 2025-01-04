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
	private String m_packetBuilderName;
	private String m_packetDecoderName;
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
		m_packetBuilderName = top.getName().addSuffix("builder").toUpperCamel();
		m_packetDecoderName = top.getName().addSuffix("decoder").toUpperCamel();
		m_keyEnumName = top.getName().addSuffix("block","keys").toUpperCamel();



		writeConstantsFile(top, headers);
		writePacketBuilder(top, headers);
		
		
	}
	
	

	private void writePacketBuilder(BlockField top, String[] headers) {
		startFile(headers);
		addLine();
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacketBuilder;");
		addLine();


		
		addLine("public class "+m_packetBuilderName+" extends BlueberryPacketBuilder implements "+m_constantsName+"{");
		indent();
		
		addLine("public "+m_packetBuilderName + "(int size){");
		indent();
		addLine("super(size);");
		outdent();
		addLine("}");
		
		
		//first get length, preamble and crc fields
		FixedIntField preamble = (FixedIntField)top.getHeaderField("preamble");
		BaseField length = top.getHeaderField("length");
		BaseField crc = top.getHeaderField("crc");
	
		
	
	
	
		addLine("public void finish(){");
		indent();
		
		String preambleConstantName = makeBaseFieldNameRoot(preamble).addSuffix("VALUE").toUpperSnake();
		String preambleIndexName = makeBaseFieldNameRoot(preamble).toUpperSnake();
		String preambleSetter = FieldName.fromCamel("write").addSuffix(lookupTypeForFuncName(preamble)).toLowerCamel();
		
		
		addLine("getTopLevelBlock()."+preambleSetter+"("+m_fieldIndexEnumName+"."+preambleIndexName+", 0, "+preambleConstantName+");");
		
//		String lengthConstantName = makeBaseFieldNameRoot(length).addSuffix("VALUE").toUpperSnake();
		String lengthIndexName = makeBaseFieldNameRoot(length).toUpperSnake();
		String lengthSetter = FieldName.fromCamel("write").addSuffix(lookupTypeForFuncName(length)).toLowerCamel();
		String lengthVal = "getCurrentBlock().getCurrentByteIndex()/4";
		
		addLine("getTopLevelBlock()."+lengthSetter+"("+m_fieldIndexEnumName+"."+lengthIndexName+", 0, "+lengthVal+");");
		
//		String crcConstantName = makeBaseFieldNameRoot(crc).addSuffix("VALUE").toUpperSnake();
		String crcIndexName = makeBaseFieldNameRoot(crc).toUpperSnake();
		String crcSetter = FieldName.fromCamel("write").addSuffix(lookupTypeForFuncName(crc)).toLowerCamel();
		String crcVal = "getPacket().computeCrc("+top.getHeaderWordCount()+")";
		
		addLine("getTopLevelBlock()."+crcSetter+"("+m_fieldIndexEnumName+"."+crcIndexName+", 0, "+crcVal+");");
		
		closeBrace();
		
		
		
		addBlockMethods(top);
		
		addArrayMethods(top);
		

		outdent();
		addLine("}");
		writeToFile(m_packageName.toPath() + m_packetBuilderName,"java");	
	}

	private void writePacketClass(BlockField top, String[] headers) {
		startFile(headers);
		addLine();
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacket;");
		addLine();


		
		addLine("public class "+m_packetDecoderName+" extends BlueberryPacket implements "+m_constantsName+"{");
		indent();
		
		addLine("public "+m_packetDecoderName + "(int size){");
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
		addLine("return "+FieldName.fromCamel("get").addSuffix(lookupTypeForFuncName(crc)).toLowerCamel()+"("+m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(crc).toUpperSnake()+");");

		outdent();
		addLine("}");
		
		addLine();
		addLine("@Override");
		addLine("public void setPublishedCrc(int crc){");
		indent();
		addLine(FieldName.fromCamel("put").addSuffix(lookupTypeForFuncName(crc)).toLowerCamel()+"("+m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(crc).toUpperSnake()+", ("+lookupTypeForFuncName(crc)+")crc);");

		outdent();
		addLine("}");
	
		
		addLine();
		addLine("@Override");
		addLine("public int getStartWord(){");
		indent();
		addLine("return "+preambleConstantName+";");
		closeBrace();
		
		addLine();
		addLine("@Override");
		addLine("public int getPublishedWordLength(){");
		indent();
		
		addLine("return "+FieldName.fromCamel("get").addSuffix(lookupTypeForFuncName(length)).toLowerCamel()+"("+m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(length).toUpperSnake()+");");
		closeBrace();
		
		addLine();
		addLine("@Override");
		addLine("public void setPublishedWordLength(int length){");
		indent();
		
		addLine(FieldName.fromCamel("put").addSuffix(lookupTypeForFuncName(length)).toLowerCamel()+"("+m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(length).toUpperSnake()+", ("+lookupTypeForFuncName(length)+")length);");
		closeBrace();
		
		
		
		addBlockMethods(top);
		
		addArrayMethods(top);
		

		closeBrace();
		writeToFile(m_packageName.toPath() + m_packetDecoderName,"java");	
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
		String functionName = "add"+af.getParent().getName().toUpperCamel() +blockName;
		List<BaseField> fs = af.getNamedBaseFields();
		FixedIntField keyF = (FixedIntField)af.getHeaderField("key");
		BaseField lengthF = af.getHeaderField("length");
		BaseField repeatsF = af.getHeaderField("repeats");
		String paramList = "";
		
		
		boolean firstTime = true;
		for(BaseField f : fs) {
			paramList += (firstTime ? "" : ", ")+lookupTypeForJavaVars(f)+"[] "+f.getName().toLowerCamel();
			firstTime = false;
		}
		
		addDocComment(comment);
		addLine("void "+functionName+"("+paramList+"){");
		indent();
		
		addLine("int n = "+fs.get(0).getName().toLowerCamel()+".length;");
		
		
		//add method to set key
		String keyType = lookupTypeForFuncName(keyF);
		String keyIndex = makeBaseFieldNameRoot(keyF).toUpperSnake();
		String keyValue = makeKeyName(keyF);
		String keyFuncName = FieldName.fromCamel("write").addSuffix(keyType).toLowerCamel();
	
		addLine("getCurrentBlock()."+keyFuncName+"("+m_fieldIndexEnumName+"."+keyIndex+", 0, "+m_keyEnumName+"."+keyValue+".getValue());");
		

		//add method to set length
		String lengthType = lookupTypeForFuncName(lengthF);
		int lengthValue = fs.size();
		String lengthIndex = makeBaseFieldNameRoot(lengthF).toUpperSnake();
		String lengthFuncName = FieldName.fromCamel("write").addSuffix(lengthType).toLowerCamel();
		//add key
		addLine("getCurrentBlock()."+lengthFuncName+"("+m_fieldIndexEnumName+"."+lengthIndex+", 0, 1 + ("+lengthValue+" * n));");
		
		//add method to set repeat
		String repeatsType = lookupTypeForFuncName(repeatsF);
		String repeatsValue ="n";
		String repeatsIndex = makeBaseFieldNameRoot(repeatsF).toUpperSnake();
		String repeatsFuncName = FieldName.fromCamel("write").addSuffix(repeatsType).toLowerCamel();
		//add key
		addLine("getCurrentBlock()."+repeatsFuncName+"("+m_fieldIndexEnumName+"."+repeatsIndex+", 0, "+repeatsValue+");");
		addLine("for(int i = 0; i < n; ++i){");
		indent();
		for(BaseField f : fs) {
			boolean bit = f instanceof BoolField;
			
			String fType = bit ? "bool" : lookupTypeForFuncName(f);
			String fValue = f.getName().toLowerCamel();
			String fIndex = makeBaseFieldNameRoot(f).toUpperSnake();
			String fFuncName = FieldName.fromCamel("write").addSuffix(fType).toLowerCamel();
			String enumName = bit ? m_bitIndexEnumName : m_fieldIndexEnumName;
			addLine("getCurrentBlock()."+fFuncName+"("+enumName+"."+fIndex+", "+fs.size()+" * i, "+fValue+"[i]);");
		}
		closeBrace();
		addLine("advanceBlock("+lengthValue+");");
		
		
		
		closeBrace();
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
				paramList += (firstTime ? "" : ", ")+lookupTypeForJavaVars(f)+" "+f.getName().toLowerCamel();
				firstTime = false;
			}
		}
		addDocComment(comment);
		addLine("void "+functionName+"("+paramList+"){");
		indent();
	
		
		//add method to set key
		String keyType = lookupTypeForFuncName(keyF);
		String keyIndex = makeBaseFieldNameRoot(keyF).toUpperSnake();
		String keyValue = makeKeyName(keyF);
		String keyFuncName = FieldName.fromCamel("write").addSuffix(keyType).toLowerCamel();
	
		addLine("getCurrentBlock()."+keyFuncName+"("+m_fieldIndexEnumName+"."+keyIndex+", 0, "+m_keyEnumName+"."+keyValue+".getValue());");
		

		//add method to set length
		String lengthType = lookupTypeForFuncName(lengthF);
		int lengthValue = withParamsNotWithout ? fs.size() + 1 : 1;
		String lengthIndex = makeBaseFieldNameRoot(lengthF).toUpperSnake();
		String lengthFuncName = FieldName.fromCamel("write").addSuffix(lengthType).toLowerCamel();
		//add key
		addLine("getCurrentBlock()."+lengthFuncName+"("+m_fieldIndexEnumName+"."+lengthIndex+", 0, "+lengthValue+");");
		
	
		
		
		
		if(withParamsNotWithout) {
			for(BaseField f : fs) {
				boolean bit = f instanceof BoolField;
				
				String fType = bit ? "bool" : lookupTypeForFuncName(f);
				String fValue = f.getName().toLowerCamel();
				String fIndex = makeBaseFieldNameRoot(f).toUpperSnake();
				String fFuncName = FieldName.fromCamel("write").addSuffix(fType).toLowerCamel();
				String enumName = bit ? m_bitIndexEnumName : m_fieldIndexEnumName;
				addLine("getCurrentBlock()."+fFuncName+"("+enumName+"."+fIndex+", 0, "+fValue+");");
			}
		}
		
		addLine("advanceBlock("+lengthValue+");");
		
		
		
		closeBrace();
		
		
		
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
		closeBrace();
		writeToFile(m_packageName.toPath() + m_constantsName,"java");	
		
		

	}

	private String lookupTypeForFuncName(BaseField f) {
		String result = "";
		switch(f.getType()) {
		case ARRAY:
			break;
		case BLOCK:
			break;
		case BOOL:
			result = "bool";
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
	private String lookupTypeForJavaVars(BaseField f) {
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
			result = "int";
			break;
		case COMPOUND:
			result = "int";
			break;
		case FLOAT32:
			result = "float";
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
		closeBrace();
		
		addLine("@Override");
		addLine("public int getIndex(){");
		indent();
		addLine("return index;");
		closeBrace();
		
		addLine("@Override");
		addLine("public int getBitIndex(){");
		indent();
		addLine("return bitIndex;");
		closeBrace();
		
		addLine("@Override");
		addLine("public int getBits(){");
		indent();
		addLine("return 1;");
		closeBrace();
		closeBrace();
		
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
		closeBrace();
		
		addLine("@Override");
		addLine("public int getIndex(){");
		indent();
		addLine("return index;");
		closeBrace();
		
		addLine("@Override");
		addLine("public int getBits(){");
		indent();
		addLine("return bits;");
		closeBrace();
		closeBrace();
		
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
		closeBrace();
	
		addLine("public int getValue(){");
		indent();
		addLine("return value;");
		closeBrace();
		closeBrace();
		addLine();
		
	}

}

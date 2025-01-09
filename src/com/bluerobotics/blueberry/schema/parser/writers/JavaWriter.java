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
	private String m_consumerInterfaceName;
	private String m_keyEnumName;
	private String m_consumerManagerName;
	private String m_packetRecieverName;

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
		m_consumerInterfaceName = top.getName().addSuffix("consumer").toUpperCamel();
		m_consumerManagerName = top.getName().addSuffix("consumer","manager").toUpperCamel();
		m_packetRecieverName = top.getName().addSuffix("Receiver").toUpperCamel();


		writeConstantsFile(top, headers);
		writePacketBuilder(top, headers);
		writeBlockParsers(top, headers);
		writeParserInterface(top, headers);
		writeConsumerManager(top, headers);
		writePacketReceiver(top, headers);
		
		
	}
	
	private void writePacketReceiver(BlockField top, String[] headers) {
		startFile(headers);
		addLine();
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacketBuilder;");
//		addLine();

		
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryReceiver;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacket;");


		addDocComment("A class to receive packets, byte by byte and pass them on when they've passed the header, length and crc checks of the "+top.getName()+" schema.");
		addLine("public class "+m_packetRecieverName+" extends BlueberryReceiver implements "+m_constantsName+" {");
		indent();

	

		
		
		BlockField bf = getListOfAllBlocksAndArrays(top).get(0);
		FixedIntField key = (FixedIntField)bf.getHeaderField("key");
		BaseField length = bf.getHeaderField("length");
		String keyIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(key).toUpperSnake();
		String keyGetterName = "bb."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(key)).toLowerCamel();
		String keyGetter = keyGetterName+"("+keyIndexName+", 0)";
		String lengthIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(length).toUpperSnake();
		String lengthGetterName = "((BlueberryPacket)getPacket()).getTopLevelBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(length)).toLowerCamel();
		String lengthGetter = lengthGetterName+"("+lengthIndexName+", 0)";
		
		BaseField crc = top.getHeaderField("crc");
		String crcIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(crc).toUpperSnake();
		String crcGetterName = "((BlueberryPacket)getPacket()).getTopLevelBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(crc)).toLowerCamel();
		String crcGetter = crcGetterName+"("+crcIndexName+", 0)";
		
		FixedIntField preamble = (FixedIntField)top.getHeaderField("preamble");
		String preambleIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(preamble).toUpperSnake();
		String preambleGetterName = "((BlueberryPacket)getPacket()).getTopLevelBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(preamble)).toLowerCamel();
		String preambleGetter = preambleGetterName+"("+preambleIndexName+", 0)";
		String preambleConst = makeBaseFieldNameRoot(preamble).addSuffix("VALUE").toUpperSnake();
		
		
		
		addLine("@Override");
		addLine("protected boolean checkCrc(){");
		indent();
		addLine("return "+crcGetter+" == getPacket().computeCrc("+crcIndexName+".getIndex());");	
		closeBrace();
		
		addLine("@Override");
		addLine("protected boolean checkStartWord(int i){");
		indent();
		addLine("return checkStartWord(i, "+preambleGetter+", "+preambleConst+");");	
		closeBrace();
		
		
		int packetHeaderLength = top.getHeaderWordCount();
		
		addLine("@Override");
		addLine("protected boolean noBytesNeeded(int i){");
		indent();
		addLine("return "+lengthGetter+" == ((BlueberryPacket)getPacket()).getWordLength();");	
		closeBrace();
	
		
		
		closeBrace();
		writeToFile(m_packageName.toPath() + m_packetRecieverName,"java");	
	}

	private List<BlockField> getListOfAllBlocksAndArrays(BlockField top){
		ArrayList<BlockField> result = new ArrayList<BlockField>();
		

		List<BlockField> bfs = top.getAllBlockFields();
		
		for(BlockField bf : bfs) {
			result.add(bf);
		}
		List<ArrayField> afs = top.getAllArrayFields();
		for(ArrayField af : afs) {
			result.add(af);
		}
		
		return result;
	}

	private void writeConsumerManager(BlockField top, String[] headers) {
		startFile(headers);
		addLine();
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacketBuilder;");
//		addLine();

		
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacketConsumerManager;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryBlock;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacket;");


		addDocComment("A class to digest packets and pass blocks to appropriate consumers for the "+top.getName()+" schema.");
		addLine("public class "+m_consumerManagerName+" extends BlueberryPacketConsumerManager<"+m_consumerInterfaceName+"> implements "+m_constantsName+" {");
		indent();
//		addLine("private "+m_consumerInterfaceName+" m_consumer = null;");

		addLine("public "+m_consumerManagerName+"("+m_consumerInterfaceName+" ci){");
		indent();
		addLine("super(ci);");
		
		for(BlockField bf : getListOfAllBlocksAndArrays(top)) {
			String className = bf.getName().addSuffix("parser").toUpperCamel();
			FixedIntField key = (FixedIntField)bf.getHeaderField("key");
			String keyEnumName = m_keyEnumName+"."+makeKeyName(key)+".getValue()";
			String keyIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(key).toUpperSnake();
			String keyGetterName = "bb."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(key)).toLowerCamel();
			String keyGetter = keyGetterName+"("+keyIndexName+", 0)";
			addLine("add("+keyEnumName+", bb -> {");
			indent();
			addLine(className+" c = new "+className+"(bb);");
			addLine("getParserConsumer().consume(c);");
			
			outdent();
			addLine("});");
			
		}
		closeBrace();

		
		
		BlockField bf = getListOfAllBlocksAndArrays(top).get(0);
		FixedIntField key = (FixedIntField)bf.getHeaderField("key");
		BaseField length = bf.getHeaderField("length");
		String keyIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(key).toUpperSnake();
		String keyGetterName = "bb."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(key)).toLowerCamel();
		String keyGetter = keyGetterName+"("+keyIndexName+", 0)";
		String lengthIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(length).toUpperSnake();
		String lengthGetterName = "bb."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(length)).toLowerCamel();
		String lengthGetter = lengthGetterName+"("+lengthIndexName+", 0)";
		
		
		addDocComment("A method for reading the key value from a block.");
		addLine("@Override");
		addLine("protected int getBlockKey(BlueberryBlock bb){");
		indent();
		addLine("return "+keyGetter+";");	
		closeBrace();
		
		addDocComment("A method for reading the length value from a block.");
		addLine("@Override");
		addLine("protected int getBlockLength(BlueberryBlock bb){");
		indent();
		addLine("return "+lengthGetter+";");	
		closeBrace();
		
		
		int packetHeaderLength = top.getHeaderWordCount();
		
		addDocComment("A method to get the first block of the packet, after the packet header.");
		addLine("@Override");
		addLine("protected BlueberryBlock getFirstBlock(BlueberryPacket p){");
		indent();
		addLine("return p.getTopLevelBlock().getNextBlock("+packetHeaderLength+"); //this is the packet header length");	
		closeBrace();
		
//		addDocComment("A method to get the consumer interface that is used to process parsed blocks.");
//		addLine("protected "+m_consumerInterfaceName+" getConsumer(){");
//		indent();
//		addLine("return m_consumer;");	
//		closeBrace();
//		
//		addDocComment("A method to set the consumer interface that is used to process parsed blocks.");
//		addLine("protected void setConsumer("+m_consumerInterfaceName+" c){");
//		indent();
//		addLine("m_consumer = c;");	
//		closeBrace();
		
		
		closeBrace();
		writeToFile(m_packageName.toPath() + m_consumerManagerName,"java");	
	}

	private void writeParserInterface(BlockField top, String[] headers) {
		startFile(headers);
		addLine();
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacketBuilder;");
//		addLine();


		addDocComment("An interface for consuming all the types of parsed blocks in the "+top.getName()+" schema.");
		addLine("public interface "+m_consumerInterfaceName+" {");
		indent();

		for(BlockField bf : getListOfAllBlocksAndArrays(top)) {
			 writeInterfaceMethod(bf);
		}
		
		closeBrace();
		writeToFile(m_packageName.toPath() + m_consumerInterfaceName,"java");			
	}

	private void writeInterfaceMethod(BlockField bf) {
		String className = bf.getName().addSuffix("parser").toUpperCamel();
		addDocComment("consume the "+className+" block.\n"+bf.getComment());
		addLine("public void consume("+className+" p);");
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
		closeBrace();
		
		
		//first get length, preamble and crc fields
		FixedIntField preamble = (FixedIntField)top.getHeaderField("preamble");
		BaseField length = top.getHeaderField("length");
		BaseField crc = top.getHeaderField("crc");
	
		addLine("@Override");
		addLine("public void reset(){");
		indent();
		addLine("super.reset();");
		addLine("advanceBlock("+top.getHeaderWordCount()+");");
		closeBrace();
	
		addLine("@Override");	
		addLine("protected void finish(){");
		indent();
		
		String preambleConstantName = makeBaseFieldNameRoot(preamble).addSuffix("VALUE").toUpperSnake();
		String preambleIndexName = makeBaseFieldNameRoot(preamble).toUpperSnake();
		String preambleSetter = FieldName.fromCamel("write").addSuffix(lookupTypeForFuncName(preamble)).toLowerCamel();
		
		
		addLine("getTopLevelBlock()."+preambleSetter+"("+m_fieldIndexEnumName+"."+preambleIndexName+", 0, "+preambleConstantName+");");
		
//		String lengthConstantName = makeBaseFieldNameRoot(length).addSuffix("VALUE").toUpperSnake();
		String lengthIndexName = makeBaseFieldNameRoot(length).toUpperSnake();
		String lengthSetter = FieldName.fromCamel("write").addSuffix(lookupTypeForFuncName(length)).toLowerCamel();
		String lengthVal = "getCurrentBlock().getCurrentWordIndex()";
		
		addLine("getTopLevelBlock()."+lengthSetter+"("+m_fieldIndexEnumName+"."+lengthIndexName+", 0, "+lengthVal+");");
		
//		String crcConstantName = makeBaseFieldNameRoot(crc).addSuffix("VALUE").toUpperSnake();
		String crcIndexName = makeBaseFieldNameRoot(crc).toUpperSnake();
		String crcSetter = FieldName.fromCamel("write").addSuffix(lookupTypeForFuncName(crc)).toLowerCamel();
		String crcVal = "getPacket().computeCrc("+top.getHeaderWordCount()+")";
		
		addLine("getTopLevelBlock()."+crcSetter+"("+m_fieldIndexEnumName+"."+crcIndexName+", 0, "+crcVal+");");
		
		closeBrace();
		
		
		
		addBlockMethods(top);
		
		addArrayMethods(top);
		

		closeBrace();
		writeToFile(m_packageName.toPath() + m_packetBuilderName,"java");	
	}

	private void writeBlockParsers(BlockField top, String[] headers) {
		List<ArrayField> afs = top.getAllArrayFields();
		for(ArrayField af : afs) {
			addArrayParser(af, headers);
		}
		
		List<BlockField> bfs = top.getAllBlockFields();
		for(BlockField bf : bfs) {
			if(bf != top) {//ignore top level
				addBlockParser(bf, headers);
			}
		}
	}
	
	private void addArrayParser(ArrayField af, String[] headers) {
		//first get length, preamble and crc fields
		FixedIntField key = (FixedIntField)af.getHeaderField("key");
		BaseField repeats = af.getHeaderField("repeats");
		BaseField length = af.getHeaderField("length");
		String className = af.getName().addSuffix("parser").toUpperCamel();
		String keyEnumName = m_keyEnumName+"."+makeKeyName(key)+".getValue()";
		String keyIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(key).toUpperSnake();
		String keyGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(key)).toLowerCamel();
		String keyGetter = keyGetterName+"("+keyIndexName+", 0)";
		String repeatsGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(repeats)).toLowerCamel();
		String repeatsIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(repeats).toUpperSnake();
		String repeatsGetter = repeatsGetterName+"("+repeatsIndexName+", 0)";
		String lengthGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(length)).toLowerCamel();
		String lengthIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(length).toUpperSnake();
		String lengthGetter = lengthGetterName+"("+lengthIndexName+", 0)";
	
		int headerLength = af.getHeaderWordCount();

		
		
		startFile(headers);
		addLine();
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryBlockParser;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryBlock;");

		addLine();


		
		addLine("public class "+className+" extends BlueberryBlockParser implements "+m_constantsName+"{");
		indent();
		addLine();
		addLine("private final int repeats;");
		addLine("private final int length;");
		addLine("private final int unit;");
		addLine("private static final int HEADER_LENGTH = "+headerLength+";");

		addLine();

		//write constructor
		addLine("public "+className + "(BlueberryBlock bb){");
		indent();
		addLine("super(bb);");
		addLine("repeats = "+repeatsGetter+";");
		addLine("length = "+lengthGetter+";");
		addLine("unit = (length - HEADER_LENGTH)/repeats;");

		closeBrace();

		addLine();
		//write checkKey method
		addLine("@Override");
		addLine("protected boolean checkKey(){");
		indent();
		addLine("return "+keyGetter+" == "+keyEnumName+";");
		closeBrace();
		addLine();
		
		//write getLength method
		addLine("@Override");
		addLine("public int getLength(){");
		indent();
		addLine("return length;");
		closeBrace();
		addLine();
		
		
		//now add methods to read fields
		for(BaseField f : af.getNamedBaseFields()) {
			boolean bool = f instanceof BoolField;
			
			String fieldFuncName = makeBaseFieldNameRoot(f).addPrefix("get").toLowerCamel();
			String fieldFuncReturnType = lookupTypeForJavaVars(f);
			String fieldGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(f)).toLowerCamel();
			String fieldIndexEnum = bool ? m_bitIndexEnumName : m_fieldIndexEnumName;
			String fieldIndexName = fieldIndexEnum+"."+makeBaseFieldNameRoot(f).toUpperSnake(); 
			String fieldGetter = fieldGetterName + "("+fieldIndexName+", 0)";
			addDocComment(f.getComment());
			addLine("public "+fieldFuncReturnType+"[] "+fieldFuncName+"(){");
			indent();
			addLine(fieldFuncReturnType + "[] result = new "+fieldFuncReturnType+"[repeats];");
			addLine("for(int i = 0; i < repeats; ++i){");
			indent();
			addLine("result[i] = "+fieldGetterName+"("+fieldIndexName+", i * unit);");
			closeBrace();
			
			addLine("return result;");
			closeBrace();

			addLine();
		}

		


		closeBrace();
		writeToFile(m_packageName.toPath() + className,"java");	
	}
	private void addBlockParser(BlockField bf, String[] headers) {
		//first get length, preamble and crc fields
		FixedIntField key = (FixedIntField)bf.getHeaderField("key");
		BaseField length = bf.getHeaderField("length");
		String className = bf.getName().addSuffix("parser").toUpperCamel();
		String keyEnumName = m_keyEnumName+"."+makeKeyName(key)+".getValue()";
		String keyIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(key).toUpperSnake();
		String keyGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(key)).toLowerCamel();
		String keyGetter = keyGetterName+"("+keyIndexName+", 0)";
		String lengthGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(length)).toLowerCamel();
		String lengthIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(length).toUpperSnake();
		String lengthGetter = lengthGetterName+"("+lengthIndexName+", 0)";
		
		
		startFile(headers);
		addLine();
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryBlockParser;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryBlock;");

		addLine();


		
		addLine("public class "+className+" extends BlueberryBlockParser implements "+m_constantsName+"{");
		indent();
		addLine();
		addLine("private final int length;");

		
		//write constructor
		addLine("public "+className + "(BlueberryBlock bb){");
		indent();
		addLine("super(bb);");
		addLine("length = "+lengthGetter+";");
		closeBrace();
		addLine();
		//write checkKey method
		addLine("@Override");
		addLine("protected boolean checkKey(){");
		indent();
		addLine("return "+keyGetter+" == "+keyEnumName+";");
		closeBrace();
		addLine();
		
		//write getLength method
		addLine("@Override");
		addLine("public int getLength(){");
		indent();
		addLine("return length;");
		closeBrace();
		addLine();
		
		
		//now add methods to read fields
		for(BaseField f : bf.getNamedBaseFields()) {
			boolean bool = f instanceof BoolField;
			
			String fieldFuncName = makeBaseFieldNameRoot(f).addPrefix("get").toLowerCamel();
			String fieldFuncReturnType = lookupTypeForJavaVars(f);
			String fieldGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(f)).toLowerCamel();
			String fieldIndexEnum = bool ? m_bitIndexEnumName : m_fieldIndexEnumName;
			String fieldIndexName = fieldIndexEnum+"."+makeBaseFieldNameRoot(f).toUpperSnake(); 
			String fieldGetter = fieldGetterName + "("+fieldIndexName+", 0)";
			addDocComment(f.getComment());

			addLine("public "+fieldFuncReturnType+" "+fieldFuncName+"(){");
			indent();
			addLine("return "+fieldGetter+";");
			closeBrace();
			addLine();
		}

		


		closeBrace();
		writeToFile(m_packageName.toPath() + className,"java");	
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
		String comment = "Adds a new "+blockName+" to the packet under construction.\n"+af.getComment();
		String functionName = "add"+blockName;
		List<BaseField> fs = af.getNamedBaseFields();
		FixedIntField keyF = (FixedIntField)af.getHeaderField("key");
		BaseField lengthF = af.getHeaderField("length");
		BaseField repeatsF = af.getHeaderField("repeats");
		String paramList = "";
		
		int headerLength = af.getHeaderWordCount();
		
		
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
		addLine("getCurrentBlock()."+lengthFuncName+"("+m_fieldIndexEnumName+"."+lengthIndex+", 0, "+headerLength+" + ("+lengthValue+" * n));");
		
		//add method to set repeat
		String repeatsType = lookupTypeForFuncName(repeatsF);
		String repeatsValue ="n";
		String repeatsIndex = makeBaseFieldNameRoot(repeatsF).toUpperSnake();
		String repeatsFuncName = FieldName.fromCamel("write").addSuffix(repeatsType).toLowerCamel();
		//add key
		addLine("getCurrentBlock()."+repeatsFuncName+"("+m_fieldIndexEnumName+"."+repeatsIndex+", 0, "+repeatsValue+");");
		addLine("for(int i = 0; i < n; ++i){");
		indent();
		addLine("int arrayOffsetForThisCycle = "+headerLength+" + ("+fs.size()+" * i);");
		for(BaseField f : fs) {
			boolean bit = f instanceof BoolField;
			
			String fType = bit ? "bool" : lookupTypeForFuncName(f);
			String fValue = f.getName().toLowerCamel();
			String fIndex = makeBaseFieldNameRoot(f).toUpperSnake();
			String fFuncName = FieldName.fromCamel("write").addSuffix(fType).toLowerCamel();
			String enumName = bit ? m_bitIndexEnumName : m_fieldIndexEnumName;
			addLine("getCurrentBlock()."+fFuncName+"("+enumName+"."+fIndex+", arrayOffsetForThisCycle, "+fValue+"[i]);");
		}
		closeBrace();
		addLine("advanceBlock("+headerLength + " + ("+ lengthValue+" * n));");
		
		
		
		closeBrace();
	}
	private void addBlockAdder(BlockField bf, boolean withParamsNotWithout) {
		String blockName = bf.getName().toUpperCamel();
		String comment = "Adds a new "+blockName+" to the packet under construction.\n"+bf.getComment();
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

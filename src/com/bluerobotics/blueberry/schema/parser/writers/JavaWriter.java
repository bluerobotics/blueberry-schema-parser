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

import com.bluerobotics.blueberry.schema.parser.fields.ArrayField;
import com.bluerobotics.blueberry.schema.parser.fields.BaseField;
import com.bluerobotics.blueberry.schema.parser.fields.BoolField;
import com.bluerobotics.blueberry.schema.parser.fields.BoolFieldField;

import com.bluerobotics.blueberry.schema.parser.fields.EnumField;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField.NameValue;
import com.bluerobotics.blueberry.schema.parser.fields.FieldName;
import com.bluerobotics.blueberry.schema.parser.fields.FixedIntField;
import com.bluerobotics.blueberry.schema.parser.fields.StructField;

/**
 * Autogenerates Java code stubs based on a parsed field structure
 */
public class JavaWriter extends SourceWriter {
	private FieldName m_packageName;
	private String m_constantsName;
	private String m_bitIndexEnumName;
	private String m_fieldIndexEnumName;
	private String m_packetBuilderName;
//	private String m_packetDecoderName;
	private String m_consumerInterfaceName;
	private String m_keyEnumName;
	private String m_consumerManagerName;
	private String m_packetRecieverName;

	public JavaWriter(File dir) {
		super(dir);
	}

	@Override
	public void write(StructField top, String... headers) {
		m_packageName = FieldName.fromDot("com.bluerobotics.blueberry").addSuffix(top.getName().toLowerCase());
		m_constantsName = top.getName().addSuffix("constants").toUpperCamel();
		m_bitIndexEnumName = top.getName().addSuffix("bit","index").toUpperCamel();
		m_fieldIndexEnumName = top.getName().addSuffix("field","index").toUpperCamel();
		m_packetBuilderName = top.getName().addSuffix("builder").toUpperCamel();
//		m_packetDecoderName = top.getName().addSuffix("decoder").toUpperCamel();
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

	private void writePacketReceiver(StructField top, String[] headers) {
//		startFile(headers);
//		addLine();
////		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacketBuilder;");
////		addLine();
//
//
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryReceiver;");
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacket;");
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryBlock;");
//
//
//		addDocComment("A class to receive packets, byte by byte and pass them on when they've passed the header, length and crc checks of the "+top.getName()+" schema.");
//		addLine("public class "+m_packetRecieverName+" extends BlueberryReceiver implements "+m_constantsName+" {");
//		indent();
//
//
//
//
//
////		BlockField bf = getListOfAllBlocksAndArrays(top).get(0);
////		FixedIntField key = (FixedIntField)top.getHeaderField("key");
//		BaseField length = top.getHeaderField("length");
////		String keyIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(key).toUpperSnake();
////		String keyGetterName = "bb."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(key)).toLowerCamel();
////		String keyGetter = keyGetterName+"("+keyIndexName+", 0)";
//		String lengthIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(length).toUpperSnake();
//		String lengthGetterName = "b."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(length)).toLowerCamel();
//		String lengthGetter = lengthGetterName+"("+lengthIndexName+", 0)";
//
//		BaseField crc = top.getHeaderField("crc");
//		String crcIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(crc).toUpperSnake();
//		String crcGetterName = "b."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(crc)).toLowerCamel();
//		String crcGetter = crcGetterName+"("+crcIndexName+", 0)";
//
//		FixedIntField preamble = (FixedIntField)top.getHeaderField("preamble");
//		String preambleIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(preamble).toUpperSnake();
//		String preambleGetterName = "b."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(preamble)).toLowerCamel();
//		String preambleGetter = preambleGetterName+"("+preambleIndexName+", 0)";
//		String preambleConst = makeBaseFieldNameRoot(preamble).addSuffix("VALUE").toUpperSnake();
//		int packetHeaderLength = top.getHeaderWordCount();
//
//		addLine("@Override");
//		addLine("protected boolean checkCrc(){");
//		indent();
//		addLine("BlueberryPacket p = (BlueberryPacket)getPacket();");
//		addLine("BlueberryBlock b = p.getTopLevelBlock();");
//		addLine("int published = 0xffff & "+crcGetter+";");
//		addLine("int computed = p.computeCrc("+packetHeaderLength+");");
//		addLine("return published == computed;");
//		closeBrace();
//
//		addLine("@Override");
//		addLine("protected boolean checkStartWord(int i){");
//		indent();
//		addLine("BlueberryPacket p = (BlueberryPacket)getPacket();");
//		addLine("BlueberryBlock b = p.getTopLevelBlock();");
//		addLine("int published = "+preambleGetter+";");
//		addLine("int constant = "+preambleConst+";");
//		addLine("return checkStartWord(i, published, constant);");
//		closeBrace();
//
//
//
//
//		addLine("@Override");
//		addLine("protected boolean isNoBytesNeeded(int i){");
//		indent();
//		addLine("BlueberryPacket p = (BlueberryPacket)getPacket();");
//		addLine("BlueberryBlock b = p.getTopLevelBlock();");
//		addLine("int published = "+lengthGetter+" * 4;");
//		addLine("int actual = i;");
//		addLine("return published != 0 && published == actual;");
//		closeBrace();
//
//
//
//		closeBrace();
//		writeToFile(m_packageName.toPath() + m_packetRecieverName,"java");
	}



	private void writeConsumerManager(StructField top, String[] headers) {
//		startFile(headers);
//		addLine();
//
//
//
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacketConsumerManager;");
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryBlock;");
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacket;");
//
//
//		addDocComment("A class to digest packets and pass blocks to appropriate consumers for the "+top.getName()+" schema.");
//		addLine("public class "+m_consumerManagerName+" extends BlueberryPacketConsumerManager<"+m_consumerInterfaceName+"> implements "+m_constantsName+" {");
//		indent();
////		addLine("private "+m_consumerInterfaceName+" m_consumer = null;");
//
//		addLine("public "+m_consumerManagerName+"("+m_consumerInterfaceName+" ci){");
//		indent();
//		addLine("super(ci);");
//
//		for(StructField bf : getListOfAllBlocksAndArrays(top)) {
//			String className = bf.getName().addSuffix("parser").toUpperCamel();
//			FixedIntField key = (FixedIntField)bf.getHeaderField("key");
//			String keyEnumName = m_keyEnumName+"."+makeKeyName(key)+".getValue()";
//			String keyIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(key).toUpperSnake();
//			String keyGetterName = "bb."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(key)).toLowerCamel();
//			String keyGetter = keyGetterName+"("+keyIndexName+", 0)";
//			addLine("add("+keyEnumName+", bb -> {");
//			indent();
//			addLine(className+" c = new "+className+"(bb);");
//			addLine("getParserConsumer().consume(c);");
//
//			outdent();
//			addLine("});");
//
//		}
//		closeBrace();
//
//
//
//		StructField bf = getListOfAllBlocksAndArrays(top).get(0);
//		FixedIntField key = (FixedIntField)bf.getHeaderField("key");
//		BaseField length = bf.getHeaderField("length");
//		String keyIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(key).toUpperSnake();
//		String keyGetterName = "bb."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(key)).toLowerCamel();
//		String keyGetter = keyGetterName+"("+keyIndexName+", 0)";
//		String lengthIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(length).toUpperSnake();
//		String lengthGetterName = "bb."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(length)).toLowerCamel();
//		String lengthGetter = lengthGetterName+"("+lengthIndexName+", 0)";
//
//
//		addDocComment("A method for reading the key value from a block.");
//		addLine("@Override");
//		addLine("protected int getBlockKey(BlueberryBlock bb){");
//		indent();
//		addLine("return "+keyGetter+";");
//		closeBrace();
//
//		addDocComment("A method for reading the length value from a block.");
//		addLine("@Override");
//		addLine("protected int getBlockLength(BlueberryBlock bb){");
//		indent();
//		addLine("return "+lengthGetter+";");
//		closeBrace();
//
//
//		int packetHeaderLength = top.getHeaderWordCount();
//
//		addDocComment("A method to get the first block of the packet, after the packet header.");
//		addLine("@Override");
//		addLine("protected BlueberryBlock getFirstBlock(BlueberryPacket p){");
//		indent();
//		addLine("return p.getTopLevelBlock().getNextBlock("+packetHeaderLength+"); //this is the packet header length");
//		closeBrace();
//
////		addDocComment("A method to get the consumer interface that is used to process parsed blocks.");
////		addLine("protected "+m_consumerInterfaceName+" getConsumer(){");
////		indent();
////		addLine("return m_consumer;");
////		closeBrace();
////
////		addDocComment("A method to set the consumer interface that is used to process parsed blocks.");
////		addLine("protected void setConsumer("+m_consumerInterfaceName+" c){");
////		indent();
////		addLine("m_consumer = c;");
////		closeBrace();
//
//
//		closeBrace();
//		writeToFile(m_packageName.toPath() + m_consumerManagerName,"java");
	}

	private void writeParserInterface(StructField top, String[] headers) {
//		startFile(headers);
//		addLine();
////		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacketBuilder;");
////		addLine();
//
//
//		addDocComment("An interface for consuming all the types of parsed blocks in the "+top.getName()+" schema.");
//		addLine("public interface "+m_consumerInterfaceName+" {");
//		indent();
//
//		for(StructField bf : getListOfAllBlocksAndArrays(top)) {
//			 writeInterfaceMethod(bf);
//		}
//
//		closeBrace();
//		writeToFile(m_packageName.toPath() + m_consumerInterfaceName,"java");
	}

	private void writeInterfaceMethod(StructField bf) {
		String className = bf.getName().addSuffix("parser").toUpperCamel();
		addDocComment("consume the "+className+" block.\n"+bf.getComment());
		addLine("public void consume("+className+" p);");
	}

	private void writePacketBuilder(StructField top, String[] headers) {
//		startFile(headers);
//		addLine();
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryPacketBuilder;");
//		addLine("import java.util.function.Function;");
//		addLine();
//
//
//
//		addLine("public class "+m_packetBuilderName+" extends BlueberryPacketBuilder implements "+m_constantsName+"{");
//		indent();
//
//		addLine("public "+m_packetBuilderName + "(int size){");
//		indent();
//		addLine("super(size);");
//		closeBrace();
//
//
//		//first get length, preamble and crc fields
//		FixedIntField preamble = (FixedIntField)top.getHeaderField("preamble");
//		BaseField length = top.getHeaderField("length");
//		BaseField crc = top.getHeaderField("crc");
//
//		addLine("@Override");
//		addLine("public void reset(){");
//		indent();
//		addLine("super.reset();");
//		addLine("advanceBlock("+top.getHeaderWordCount()+");");
//		closeBrace();
//
//		addLine("@Override");
//		addLine("public void finish(boolean computeCrc){");
//		indent();
//
//		String preambleConstantName = makeBaseFieldNameRoot(preamble).addSuffix("VALUE").toUpperSnake();
//		String preambleIndexName = makeBaseFieldNameRoot(preamble).toUpperSnake();
//		String preambleSetter = FieldName.fromCamel("write").addSuffix(lookupTypeForFuncName(preamble)).toLowerCamel();
//
//
//		addLine("getTopLevelBlock()."+preambleSetter+"("+m_fieldIndexEnumName+"."+preambleIndexName+", 0, "+preambleConstantName+");");
//
////		String lengthConstantName = makeBaseFieldNameRoot(length).addSuffix("VALUE").toUpperSnake();
//		String lengthIndexName = makeBaseFieldNameRoot(length).toUpperSnake();
//		String lengthSetter = FieldName.fromCamel("write").addSuffix(lookupTypeForFuncName(length)).toLowerCamel();
//		String lengthVal = "getPacket().getWordLength()";//"getCurrentBlock().getCurrentWordIndex()";
//
//		addLine("getTopLevelBlock()."+lengthSetter+"("+m_fieldIndexEnumName+"."+lengthIndexName+", 0, "+lengthVal+");");
//
////		String crcConstantName = makeBaseFieldNameRoot(crc).addSuffix("VALUE").toUpperSnake();
//		String crcIndexName = makeBaseFieldNameRoot(crc).toUpperSnake();
//		String crcSetter = FieldName.fromCamel("write").addSuffix(lookupTypeForFuncName(crc)).toLowerCamel();
//		String crcVal = "computeCrc ? getPacket().computeCrc("+top.getHeaderWordCount()+") : -1";
//
//		addLine("getTopLevelBlock()."+crcSetter+"("+m_fieldIndexEnumName+"."+crcIndexName+", 0, "+crcVal+");");
//
//		closeBrace();
//
//
//
//		addBlockMethods(top);
//
//		addArrayMethods(top);
//
//		addCompactArrayMethods(top);
//
//
//		closeBrace();
//		writeToFile(m_packageName.toPath() + m_packetBuilderName,"java");
	}

	private void writeBlockParsers(StructField top, String[] headers) {
//		List<ArrayField> afs = top.getAllArrayFields();
//		for(ArrayField af : afs) {
//			addArrayParser(af, headers);
//		}
//
//		List<StructField> bfs = top.getAllBlockFields();
//		for(StructField bf : bfs) {
//			if(bf != top) {//ignore top level
//				addBlockParser(bf, headers);
//			}
//		}
//		List<CompactArrayField> cafs = top.getAllCompactArrayFields();
//		for(CompactArrayField caf : cafs) {
//			if(caf != top) {//ignore top level
//				addCompactArrayParser(caf, headers);
//			}
//		}
	}



	private void addArrayParser(ArrayField af, String[] headers) {
//		//first get length, preamble and crc fields
//		FixedIntField key = (FixedIntField)af.getHeaderField("key");
//		BaseField repeats = af.getHeaderField("repeats");
//		BaseField length = af.getHeaderField("length");
//		String className = af.getName().addSuffix("parser").toUpperCamel();
//		String keyEnumName = m_keyEnumName+"."+makeKeyName(key)+".getValue()";
//		String keyIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(key).toUpperSnake();
//		String keyGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(key)).toLowerCamel();
//		String keyGetter = keyGetterName+"("+keyIndexName+", 0)";
//		String repeatsGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(repeats)).toLowerCamel();
//		String repeatsIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(repeats).toUpperSnake();
//		String repeatsGetter = repeatsGetterName+"("+repeatsIndexName+", 0)";
//		String lengthGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(length)).toLowerCamel();
//		String lengthIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(length).toUpperSnake();
//		String lengthGetter = lengthGetterName+"("+lengthIndexName+", 0)";
//
//		int headerLength = af.getHeaderWordCount();
//
//
//
//		startFile(headers);
//		addLine();
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryBlockParser;");
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryBlock;");
//
//		addLine();
//
//
//
//		addLine("public class "+className+" extends BlueberryBlockParser implements "+m_constantsName+"{");
//		indent();
//		addLine();
//		addLine("private final int repeats;");
//		addLine("private final int length;");
//		addLine("private final int unit;");
//		addLine("private static final int HEADER_LENGTH = "+headerLength+";");
//
//		addLine();
//
//		//write constructor
//		addLine("public "+className + "(BlueberryBlock bb){");
//		indent();
//		addLine("super(bb);");
//		addLine("repeats = "+repeatsGetter+";");
//		addLine("length = "+lengthGetter+";");
//		addLine("unit = repeats == 0 ? 0 : (length - HEADER_LENGTH)*4/repeats;");
//
//		closeBrace();
//
//		addLine();
//		//write checkKey method
//		addLine("@Override");
//		addLine("protected boolean checkKey(){");
//		indent();
//		addLine("return "+keyGetter+" == "+keyEnumName+";");
//		closeBrace();
//		addLine();
//
//		//write isEmpty method
//		addLine("@Override");
//		addLine("public boolean isEmpty(){");
//		indent();
//		addLine("return length <= "+headerLength+";");
//		closeBrace();
//		addLine();
//
//		addDocComment("Gets the number of elements in the array field.");
//		addLine("public int getRepeats(){");
//		indent();
//		addLine("return repeats;");
//		closeBrace();
//		addLine();
//
//		//now add methods to read fields
//		for(BaseField f : af.getNamedBaseFields()) {
//			boolean bool = f instanceof BoolField;
//
//			String fieldFuncName = f.getName().addPrefix("get").toLowerCamel();
//			String fieldFuncReturnType = lookupTypeForJavaVars(f);
//			String fieldGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(f)).toLowerCamel();
//			String fieldIndexEnum = bool ? m_bitIndexEnumName : m_fieldIndexEnumName;
//			String fieldIndexName = fieldIndexEnum+"."+makeBaseFieldNameRoot(f).toUpperSnake();
//			String fieldGetter = fieldGetterName + "("+fieldIndexName+", 0)";
//			if(f instanceof EnumField) {
//				fieldGetter = fieldFuncReturnType+".lookup("+fieldGetter+")";
//			}
//			addDocComment("Gets the "+makeBaseFieldNameRoot(f)+" field of the "+af.getName().toLowerCamel()+" Block",f.getComment(), "@param i - the index of the desired element. This must be less than the repeats value of this block.");
//			addLine("public "+fieldFuncReturnType+" "+fieldFuncName+"(int i){");
//			indent();
//			addLine("if(i >= repeats || i < 0) {");
//			indent();
//			addLine("throw new RuntimeException(\"Index out of bounds!\");");
//			closeBrace();
//			addLine("return "+fieldGetterName+"("+fieldIndexName+", i * unit);");
//			closeBrace();
//			addLine();
//		}
//
//
//
//
//		closeBrace();
//		writeToFile(m_packageName.toPath() + className,"java");
	}
	private void addBlockParser(StructField bf, String[] headers) {
//		//first get length, preamble and crc fields
//		FixedIntField key = (FixedIntField)bf.getHeaderField("key");
//		BaseField length = bf.getHeaderField("length");
//		String className = bf.getName().addSuffix("parser").toUpperCamel();
//		String keyEnumName = m_keyEnumName+"."+makeKeyName(key)+".getValue()";
//		String keyIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(key).toUpperSnake();
//		String keyGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(key)).toLowerCamel();
//		String keyGetter = keyGetterName+"("+keyIndexName+", 0)";
//		String lengthGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(length)).toLowerCamel();
//		String lengthIndexName = m_fieldIndexEnumName+"."+makeBaseFieldNameRoot(length).toUpperSnake();
//		String lengthGetter = lengthGetterName+"("+lengthIndexName+", 0)";
//		int headerLength = bf.getHeaderWordCount();
//
//
//
//		startFile(headers);
//		addLine();
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryBlockParser;");
//		addLine("import com.bluerobotics.blueberry.transcoder.java.BlueberryBlock;");
//
//		addLine();
//
//
//
//		addLine("public class "+className+" extends BlueberryBlockParser implements "+m_constantsName+"{");
//		indent();
//		addLine();
//		addLine("private final int length;");
//
//
//		//write constructor
//		addLine("public "+className + "(BlueberryBlock bb){");
//		indent();
//		addLine("super(bb);");
//		addLine("length = "+lengthGetter+";");
//		closeBrace();
//		addLine();
//		//write checkKey method
//		addLine("@Override");
//		addLine("protected boolean checkKey(){");
//		indent();
//		addLine("return "+keyGetter+" == "+keyEnumName+";");
//		closeBrace();
//		addLine();
//
//		//write isEmpty method
//		addLine("@Override");
//		addLine("public boolean isEmpty(){");
//		indent();
//		addLine("return length <= "+headerLength+";");
//		closeBrace();
//		addLine();
//
//
//		//now add methods to read fields
//		for(BaseField f : bf.getNamedBaseFields()) {
//			boolean bool = f instanceof BoolField;
//
//			String fieldFuncName = f.getName().addPrefix("get").toLowerCamel();
//			String fieldFuncReturnType = lookupTypeForJavaVars(f);
//			String fieldGetterName = "getBlock()."+FieldName.fromCamel("read").addSuffix(lookupTypeForFuncName(f)).toLowerCamel();
//			String fieldIndexEnum = bool ? m_bitIndexEnumName : m_fieldIndexEnumName;
//			String fieldIndexName = fieldIndexEnum+"."+makeBaseFieldNameRoot(f).toUpperSnake();
//			String fieldGetter = fieldGetterName + "("+fieldIndexName+", 0)";
//			if(f instanceof EnumField) {
//				fieldGetter = fieldFuncReturnType+".lookup("+fieldGetter+")";
//			}
//			addDocComment(f.getComment());
//
//			addLine("public "+fieldFuncReturnType+" "+fieldFuncName+"(){");
//			indent();
//			addLine("return "+fieldGetter+";");
//			closeBrace();
//			addLine();
//		}
//
//
//
//
//		closeBrace();
//		writeToFile(m_packageName.toPath() + className,"java");
	}

	private void addArrayMethods(StructField top) {
//		List<ArrayField> afs = top.getAllArrayFields();
//		for(ArrayField af : afs) {
//			addArrayAdder(af, true);
//			addArrayAdder(af, false);
//
//		}
	}



	private void addBlockMethods(StructField top) {
//		List<StructField> bfs = top.getAllBlockFields();
//
//		for(StructField bf : bfs) {
//			if(bf != top) {//ignore top level
//				addBlockAdder(bf, true);
//				addBlockAdder(bf, false);
//			}
//		}
	}
	private void addCompactArrayMethods(StructField top) {
//		List<CompactArrayField> cafs = top.getAllCompactArrayFields();
//		for(CompactArrayField caf : cafs) {
//			addCompactArrayAdder(caf, true);
//			addCompactArrayAdder(caf, false);
//		}
	}


	private void addArrayAdder(ArrayField af, boolean withParametersNotEmpty) {
//		String blockName = af.getName().toUpperCamel();
//		String comment = "Adds a new "+blockName+" to the packet under construction.\n"+af.getComment();
//		String functionName = "add"+(withParametersNotEmpty ? "" : "Empty")+ blockName;
//		List<BaseField> fs = af.getNamedBaseFields();
//		FixedIntField keyF = (FixedIntField)af.getHeaderField("key");
//		BaseField lengthF = af.getHeaderField("length");
//		BaseField repeatsF = af.getHeaderField("repeats");
//		String paramList = withParametersNotEmpty ? "int n" : "";
//
//		int headerLength = af.getHeaderWordCount();
//		String paramComments = "";
//		if(withParametersNotEmpty) {
//			paramComments += "@param n - the number of elements\n";
//			for(BaseField f : fs) {
//				String fName = f.getName().addSuffix("Function").toLowerCamel();
//				paramList += ", "+"Function<Integer, "+lookupObjectTypeForJavaVars(f)+"> "+fName;
//				paramComments += "@param "+fName+" - a function that returns the array element given an integer index\n";
//			}
//		}
//
//		addDocComment(comment, "This assumes parameters defined with a functional interface ", paramComments);
//		addLine("public void "+functionName+"("+paramList+"){");
//		indent();
//		String firstArray = fs.get(0).getName().toLowerCamel();
////		addLine("int n = "+firstArray+" == null ? 0 : "+firstArray+".length;");
//
//
//		//add method to set key
//		String keyType = lookupTypeForFuncName(keyF);
//		String keyIndex = makeBaseFieldNameRoot(keyF).toUpperSnake();
//		String keyValue = makeKeyName(keyF);
//		String keyFuncName = FieldName.fromCamel("write").addSuffix(keyType).toLowerCamel();
//
//		addLine("getCurrentBlock()."+keyFuncName+"("+m_fieldIndexEnumName+"."+keyIndex+", 0, "+m_keyEnumName+"."+keyValue+".getValue());");
//
//
//		//add method to set length
//		String lengthType = lookupTypeForFuncName(lengthF);
//		int lengthValue = withParametersNotEmpty ? fs.size() : 0;
//		String lengthIndex = makeBaseFieldNameRoot(lengthF).toUpperSnake();
//		String lengthFuncName = FieldName.fromCamel("write").addSuffix(lengthType).toLowerCamel();
//		String offset = withParametersNotEmpty ? " + (" + lengthValue+" * n)" : "";
//		//add key
//		addLine("getCurrentBlock()."+lengthFuncName+"("+m_fieldIndexEnumName+"."+lengthIndex+", 0, "+headerLength+offset+");");
//
//
//		//add method to set repeat
//		String repeatsType = lookupTypeForFuncName(repeatsF);
//		String repeatsValue = withParametersNotEmpty ? "n" : "0";
//		String repeatsIndex = makeBaseFieldNameRoot(repeatsF).toUpperSnake();
//		String repeatsFuncName = FieldName.fromCamel("write").addSuffix(repeatsType).toLowerCamel();
//		//add key
//		addLine("getCurrentBlock()."+repeatsFuncName+"("+m_fieldIndexEnumName+"."+repeatsIndex+", 0, "+repeatsValue+");");
//		if(withParametersNotEmpty) {
//			addLine("for(int i = 0; i < n; ++i){");
//			indent();
//			addLine("int arrayOffsetForThisCycle = ("+fs.size()+" * i * 4);//this is in bytes, not words");
//			for(BaseField f : fs) {
//				boolean bit = f instanceof BoolField;
//
//				String fType = bit ? "bool" : lookupTypeForFuncName(f);
//				String fValue = f.getName().addSuffix("Function").toLowerCamel();
//				if(f instanceof EnumField) {
//					fValue += ".getValue()";
//				}
//				String fIndex = makeBaseFieldNameRoot(f).toUpperSnake();
//				String fFuncName = FieldName.fromCamel("write").addSuffix(fType).toLowerCamel();
//				String enumName = bit ? m_bitIndexEnumName : m_fieldIndexEnumName;
//				addLine("getCurrentBlock()."+fFuncName+"("+enumName+"."+fIndex+", arrayOffsetForThisCycle, "+fValue+".apply(i));");
//			}
//			closeBrace();
//		}
//
//		addLine("advanceBlock("+headerLength + offset + ");");
//
//
//
//		closeBrace();
	}
	private void addBlockAdder(StructField bf, boolean withParamsNotWithout) {
//		String blockName = bf.getName().toUpperCamel();
//		String comment = "Adds a new "+blockName+" to the packet under construction.\n"+bf.getComment();
//		String functionName = "add"+blockName;
//		List<BaseField> fs = bf.getNamedBaseFields();
//		FixedIntField keyF = (FixedIntField)bf.getHeaderField("key");
//		BaseField lengthF = bf.getHeaderField("length");
//		BaseField repeatsF = bf.getHeaderField("repeats");
//		String paramList = "";
//		if(withParamsNotWithout && fs.size() == 0) {
//			//don't do anything if this block does not have parameters but we're doing the version with params
//			return;
//		}
//		String paramComment = "";
//		if(withParamsNotWithout) {
//			boolean firstTime = true;
//			for(BaseField f : fs) {
//				String fName = f.getName().toLowerCamel();
//				paramList += (firstTime ? "" : ", ")+lookupTypeForJavaVars(f)+" "+fName;
//				firstTime = false;
//				paramComment += "@param "+fName+" - "+f.getComment()+"\n";
//			}
//		}
//		addDocComment(comment, paramComment);
//		addLine("public void "+functionName+"("+paramList+"){");
//		indent();
//
//
//		//add method to set key
//		String keyType = lookupTypeForFuncName(keyF);
//		String keyIndex = makeBaseFieldNameRoot(keyF).toUpperSnake();
//		String keyValue = makeKeyName(keyF);
//		String keyFuncName = FieldName.fromCamel("write").addSuffix(keyType).toLowerCamel();
//
//		addLine("getCurrentBlock()."+keyFuncName+"("+m_fieldIndexEnumName+"."+keyIndex+", 0, "+m_keyEnumName+"."+keyValue+".getValue());");
//
//
//		//add method to set length
//		String lengthType = lookupTypeForFuncName(lengthF);
//		int lengthValue = withParamsNotWithout ? fs.size() + 1 : 1;
//		String lengthIndex = makeBaseFieldNameRoot(lengthF).toUpperSnake();
//		String lengthFuncName = FieldName.fromCamel("write").addSuffix(lengthType).toLowerCamel();
//		//add key
//		addLine("getCurrentBlock()."+lengthFuncName+"("+m_fieldIndexEnumName+"."+lengthIndex+", 0, "+lengthValue+");");
//
//
//
//
//
//		if(withParamsNotWithout) {
//			for(BaseField f : fs) {
//				boolean bit = f instanceof BoolField;
//
//				String fType = bit ? "bool" : lookupTypeForFuncName(f);
//				String fValue = f.getName().toLowerCamel();
//				if(f instanceof EnumField) {
//					fValue += ".getValue()";
//				}
//				String fIndex = makeBaseFieldNameRoot(f).toUpperSnake();
//				String fFuncName = FieldName.fromCamel("write").addSuffix(fType).toLowerCamel();
//				String enumName = bit ? m_bitIndexEnumName : m_fieldIndexEnumName;
//				addLine("getCurrentBlock()."+fFuncName+"("+enumName+"."+fIndex+", 0, "+fValue+");");
//			}
//		}
//
//		addLine("advanceBlock("+lengthValue+");");
//
//
//
//		closeBrace();
//
//
//
	}

	private void writeConstantsFile(StructField top, String[] headers) {
		startFile(headers);
		addLine();
		addLine("import com.bluerobotics.blueberry.transcoder.java.BitIndex;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.FieldIndex;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.EnumLookup;");
		addLine();

		addLine("public interface "+m_constantsName+" {");
		indent();

		writeConsants(top);
		writeKeyEnum(top);
		writeFieldIndexEnum(top);
		writeBitIndexEnum(top);
		writeOtherEnums(top);
		closeBrace();
		writeToFile(m_packageName.toPath() + m_constantsName,"java");



	}

	private void writeOtherEnums(StructField top) {
//		List<EnumField> enums = new ArrayList<EnumField>();
//		top.scanThroughBaseFields(f -> {
//			if(f instanceof EnumField && f.getName() != null) {
//				boolean found = false;
//				EnumField ef = (EnumField)f;
//				for(EnumField eft : enums) {
//					if(eft.getTypeName().equals(ef.getTypeName())){
//						found = true;
//						break;
//					}
//				}
//				if(!found) {
//					enums.add(ef);
//				}
//			}
//		}, true);
//		enums.forEach(f -> {
//			writeEnum(f);
//		});
	}

	private void writeEnum(EnumField f) {
		List<NameValue> nvs = f.getNameValues();

		String comment = f.getComment();
		String name = makeEnumTypeName(f);
		String type = lookupTypeForJavaType(f);

		addDocComment(comment);
		addLine("public enum "+name+" {");
		indent();
		for(NameValue nv : nvs) {
			String c = nv.getComment();
			if(!c.isBlank()) {
				c = "//"+c;
			} else {
				c = "";
			}
			addLine(nv.getName().toUpperSnake()+"(("+type+")"+nv.getValueAsHex()+"),"+c);
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
	/**
	 * looks up an object type to represent the type of the specified base field
	 * This will not return a primitive type, instead it looks up the equivalent class type
	 * @param f
	 * @return
	 */
	private String lookupObjectTypeForJavaVars(BaseField f){
		String result = "";
		if(f instanceof EnumField) {
			result = makeEnumTypeName((EnumField)f);
		} else {
			switch(f.getType()) {
			case ARRAY:
				break;
			case BLOCK:
				break;
			case BOOL:
				result = "Boolean";
				break;
			case FLOAT32:
				result = "Double";
				break;
			case BOOLFIELD:
			case COMPOUND:
			case INT16:
			case INT32:
			case INT8:
			case UINT16:
			case UINT32:
			case UINT8:
				result = "Integer";
				break;

			}
		}
		return result;
	}
	private String lookupTypeForJavaVars(BaseField f) {
		String result = "";
		if(f instanceof EnumField) {
			result = makeEnumTypeName((EnumField)f);
		} else {
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
		}
		return result;
	}
	private String lookupTypeForJavaType(EnumField f) {
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
	private String makeEnumTypeName(EnumField f) {
		return f.getTypeName().addSuffix("enum").toUpperCamel();
	}

	private void writeConsants(StructField top) {
//		ArrayList<FixedIntField> fifs = new ArrayList<FixedIntField>();
//
//		top.scanThroughHeaderFields(f -> {
//			if(f instanceof FixedIntField && !f.getName().toLowerCamel().equals("key")) {
//				fifs.add((FixedIntField)f);
//			}
//		}, true);
//		for(FixedIntField fif : fifs) {
//
//			String name = makeBaseFieldNameRoot(fif).addSuffix("VALUE").toUpperSnake();
//			addDocComment(fif.getComment());
//			addLine("public static final int "+name+" = "+WriterUtils.formatAsHex(fif.getValue())+";");
//		}
	}

	@Override
	protected void startFile(String... hs) {
		// TODO Auto-generated method stub
		super.startFile(hs);
		addLine("package " + m_packageName.toDot()+";");


	}

	private void writeBitIndexEnum(StructField top) {
//
//
//
//
//		//make a list of all the fields
//		List<BoolField> fs = new ArrayList<BoolField>();
//		//first header fields
//		top.scanThroughBaseFields(f -> {
//			if(f instanceof BoolField) {
//				fs.add((BoolField)f);
//			}
//		}, true);
//
//		//now generate the enum details
//		addDocComment("An enum of all the field indeces for the "+top.getName()+" schema.");
//		addLine("public enum "+m_bitIndexEnumName+" implements BitIndex {");
//		indent();
//		for(BoolField f : fs) {
//			String name = makeBaseFieldNameRoot(f).toUpperSnake();
//			int i;
//
//			BoolFieldField bff = (BoolFieldField)f.getParent();
//			i = bff.getIndex();
//
//			addLine(name + "("+i+", "+ f.getIndex()+"),");
//		}
//		addLine(";");
//		addLine("private int index;");
//		addLine("private int bitIndex;");
//		addLine("private "+m_bitIndexEnumName+"(int i, int bi){");
//		indent();
//		addLine("index = i;");
//		addLine("bitIndex = bi;");
//		closeBrace();
//
//		addLine("@Override");
//		addLine("public int getIndex(){");
//		indent();
//		addLine("return index;");
//		closeBrace();
//
//		addLine("@Override");
//		addLine("public int getBitIndex(){");
//		indent();
//		addLine("return bitIndex;");
//		closeBrace();
//
//		addLine("@Override");
//		addLine("public int getBits(){");
//		indent();
//		addLine("return 1;");
//		closeBrace();
//		closeBrace();
//
//		addLine();
	}

	private void writeFieldIndexEnum(StructField top) {
//		//make a list of all the fields
//		List<BaseField> fs = new ArrayList<BaseField>();
//		//first header fields
//		top.scanThroughHeaderFields(bf -> {
//			if(bf.getName() != null && !(bf instanceof CompoundField) && !(bf instanceof BoolField)) {
//				boolean found = false;
//				for(BaseField f : fs) {
//					if(f.getName().equals(bf.getName())) {
//						if(f.getCorrectParentName().equals(bf.getCorrectParentName())){
//							found = true;
//						}
//					}
//				}
//				if(!found) {
//					fs.add(bf);
//				}
//			}
//		}, true);
//
//
//		//now base fields
//		top.scanThroughBaseFields((bf) -> {
//			if(bf.getName() != null && !(bf instanceof CompoundField) && !(bf instanceof BoolField)) {
//				fs.add(bf);
//			}
//		}, false);
//
//
//		//now generate the enum details
//		addDocComment("An enum of all the field indeces for the "+top.getName()+" schema.");
//		addLine("public enum "+m_fieldIndexEnumName+" implements FieldIndex {");
//		indent();
//		for(BaseField f : fs) {
//			String name = makeBaseFieldNameRoot(f).toUpperSnake();
//			int i;
//			if(f instanceof BoolField) {
//				BoolFieldField bff = (BoolFieldField)f.getParent();
//				i = bff.getIndex();
//			} else {
//				i = f.getIndex();
//			}
//			addLine(name + "("+i+", " +f.getBitCount()+"),");
//		}
//		addLine(";");
//		addLine("private int index;");
//		addLine("private int bits;");
//		addLine("private "+m_fieldIndexEnumName+"(int i, int b){");
//		indent();
//		addLine("index = i;");
//		addLine("bits = b;");
//		closeBrace();
//
//		addLine("@Override");
//		addLine("public int getIndex(){");
//		indent();
//		addLine("return index;");
//		closeBrace();
//
//		addLine("@Override");
//		addLine("public int getBits(){");
//		indent();
//		addLine("return bits;");
//		closeBrace();
//		closeBrace();
//
//		addLine();

	}

	private void writeKeyEnum(StructField top) {
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

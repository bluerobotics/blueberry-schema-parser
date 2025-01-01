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

	public JavaWriter(File dir) {
		super(dir);
	}

	@Override
	public void write(BlockField top, String... headers) {
		m_packageName = FieldName.fromDot("com.bluerobotics.blueberry").addSuffix(top.getName().toLowerCase());
		String interfaceName = top.getName().addSuffix("constants").toUpperCamel();
		startFile(headers);
		addLine("public interface "+interfaceName+" {");
		indent();
		
		writeConsants(top);
		writeKeyEnum(top);
		writeFieldIndexEnum(top);
		writeBitIndexEnum(top);
		outdent();
		addLine("}");
		writeToFile(m_packageName.toPath() + interfaceName,"java");	
		
		
		String className = top.getName().toUpperCamel();
		startFile(headers);
		addLine("public class "+className+" extends Packet implements "+interfaceName+"{");
		indent();
		
		writePacketMethods(top);

		outdent();
		addLine("}");
		writeToFile(m_packageName.toPath() + className,"java");	
		
	}
	
	

	private void writePacketMethods(BlockField top) {
		//first get length, preamble and crc fields
		BaseField preamble = top.getHeaderField("preamble");
		BaseField length = top.getHeaderField("length");
		BaseField crc = top.getHeaderField("crc");
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
		
		addLine();
		addLine("import com.bluerobotics.blueberry.transcoder.java.BitIndex;");
		addLine("import com.bluerobotics.blueberry.transcoder.java.FieldIndex;");
		addLine();
	}

	private void writeBitIndexEnum(BlockField top) {
		String className = top.getName().addSuffix("bit","index").toUpperCamel();
		

		
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
		addLine("public enum "+className+" implements BitIndex {");
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
		addLine("private "+className+"(int i, int bi){");
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
		String className = top.getName().addSuffix("field","index").toUpperCamel();
	
		
		

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
		addLine("public enum "+className+" implements FieldIndex {");
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
		addLine("private "+className+"(int i, int b){");
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
		String className = top.getName().addSuffix("block","keys").toUpperCamel();
	

		
		List<FixedIntField> keys = getBlockKeys(top);
		addDocComment("An enum of all the block keys for the "+top.getName()+" schema.");
		addLine("public enum "+className+" {");
		indent();
		for(FixedIntField key : keys) {
			String name = makeKeyName(key);
			addDocComment(key.getComment());
			addLine(name + "("+WriterUtils.formatAsHex(key.getValue())+"),");
		}
		addLine(";");
		addLine("private int value;");
		addLine("private "+className+"(int i){");
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

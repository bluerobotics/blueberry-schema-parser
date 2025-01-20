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
import com.bluerobotics.blueberry.schema.parser.structure.EnumField;
import com.bluerobotics.blueberry.schema.parser.structure.EnumField.NameValue;
import com.bluerobotics.blueberry.schema.parser.structure.Field;
import com.bluerobotics.blueberry.schema.parser.structure.FieldName;
import com.bluerobotics.blueberry.schema.parser.structure.FixedIntField;
import com.bluerobotics.blueberry.schema.parser.structure.Type;

public class CWriter extends SourceWriter {

	public CWriter(File dir) {
		super(dir);
	}

	@Override
	public void write(BlockField bf, String... headers) {
		
		
			makeHeaderFile(bf, headers);
			makeSourceFile(bf, headers);
		
		
	}
	private void makeHeaderFile(BlockField top, String... hs) {
		startFile(hs);
	
		
		
		addSectionDivider("Includes");
		addLine("#include <stdbool.h>");
		addLine("#include <stdint.h>");
		addLine("#include <blueberry-transcoder.h>");
	
		addSectionDivider("Defines");
		writeBlockValueDefine(top);
//		addBlockKeyDefines(top);
		
		
		addSectionDivider("Types");
		addBlockKeyEnum(top);
		
		addFirstBlockDefine(top);
		
		writeEnums(top);
//		addLine("typedef BlueberryBlock Bb;");
//		writeCompounds(top);
		
		addSectionDivider("Function Prototypes");
		addHeaderFieldGetters(top,true);
		
		addUnitsPerRepeatGetter(top, true);

		addBaseFieldGetters(top, true);
		
		addPacketStartFinish(top, true);
		
		addBlockFunctionGetters(top, true);
		
		addBlockAdders(top, true);
		
		addArrayAdders(top, true);
//		addArrayGetters(top, true);
		
//		addArrayElementAdders(top, true);
		
		
		
		
		writeToFile("inc/"+top.getName().toLowerCamel(),"h");
	
	}



	private void makeSourceFile(BlockField top, String... hs) {
		startFile(hs);
	
		
		
		addSectionDivider("Includes");
		addLine("#include <"+top.getName().toLowerCamel()+".h>");
	
		addSectionDivider("Defines");
		
		writeHeaderDefines(top);
		addLine();
		addLine();
		writeBaseFieldDefines(top);
		
		addSectionDivider("Types");
		

		
		addSectionDivider("Function Prototypes");
//		addBlockFunctionAdder(top, true);
		
	
		addSectionDivider("Source");
		
		addHeaderFieldGetters(top,false);
		
		addUnitsPerRepeatGetter(top, false);

		
		addBaseFieldGetters(top, false);
		
		addPacketStartFinish(top, false);
		
		addBlockFunctionGetters(top, false);
//		addBlockFunctionAdder(top, false);
		
		addBlockAdders(top, false);
		
		addArrayAdders(top, false);
//		addArrayGetters(top, false);
//		addArrayElementAdders(top, true);

		
		
		writeToFile("src/"+top.getName().toLowerCamel(),"c");
	
	}
	

	

	private void addUnitsPerRepeatGetter(BlockField top, boolean proto) {
		//grab the first array field found
		ArrayField af = getArrayFields(top).get(0);
		
		int hl = af.getHeaderWordCount();
		FieldName tn = af.getTypeName();
		
		addDocComment("get the number of bytes per array element.\nThis is needed for array value getters.");
		addLine("uint32_t getBb"+tn.toUpperCamel()+"BytesPerRepeat(Bb* buf, BbBlock currentBlock)"+(proto ? ";" : "{"));
		if(!proto) {
			indent();
		
//			BaseField lField = af.getHeaderField("length");
			addLine("uint32_t length = getBb"+tn.toUpperCamel()+"Length(buf, currentBlock);");
			addLine("uint32_t repeats = getBb"+tn.toUpperCamel()+"Repeats(buf, currentBlock);");
			addLine("return (length - " + (hl * 4) + ") / repeats;");
			closeBrace();
		}
		
	}

	private String makeBlockValueDefine(FixedIntField fif) {
		FieldName parent = fif.getContainingWord().getParent().getName();
		
		return parent.addSuffix(fif.getName()).addSuffix("VALUE").toUpperSnake();
	}
	

	private void writeBlockValueDefine(BlockField top) {
		
		top.scanThroughHeaderFields(f -> {
			if(f instanceof FixedIntField) {
				FixedIntField fif = (FixedIntField)f;

				addBlockComment(fif.getComment());
				addLine("#define "+makeBlockValueDefine(fif)+" ("+WriterUtils.formatAsHex(fif.getValue())+")");
			}
			
		}, true);
		
	}

	private void writeEnums(BlockField top) {
		//first make a list of all unique enums
		ArrayList<EnumField> es = new ArrayList<EnumField>();
		top.scanThroughBaseFields((f) -> {
			if(f instanceof EnumField) {
				EnumField e = (EnumField)f;
				boolean found = false;
				//check that this new one isn't the same as an existing one
				for(EnumField ef : es) {
					if(ef.getTypeName().equals(e)) {
						found = true;
						break;
					}
				}
				if(!found) {
					es.add(e);
				}
			}
		}, true);	
		for(EnumField ef : es) {
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
				addLine(makeEnumName(ef, nv) + " = " + WriterUtils.formatAsHex(nv.getValue())+", " + c);
				
				
			}
			outdent();
			
			addLine("} "+ef.getTypeName().toUpperCamel()+";");
			
			addLine();

		}
	}
	
	private String makeEnumName(EnumField ef, NameValue nv) {
		return nv.getName().addSuffix(ef.getTypeName()).toUpperSnake();
	}

	private void writeBaseFieldDefines(BlockField top) {
		top.scanThroughBaseFields((f) -> {
			writeDefine(f);
		}, false);	
//		top.getBaseFields().forEach(f -> writeDefine(f));
	}
	private void writeHeaderDefines(BlockField top) {
	
		//first scan through header fields and get all unique ones
		ArrayList<BaseField> hfs = new ArrayList<BaseField>();
		top.scanThroughHeaderFields(bf -> {
			if(bf.getName() != null) {
				boolean found = false;
				for(BaseField f : hfs) {
					if(f.getName().equals(bf.getName())) {
						if(f.getCorrectParentName().equals(bf.getCorrectParentName())){
							found = true;
							break;
						}
					}
				}
				if(!found) {
					hfs.add(bf);
				}
			}
		}, true);
		
		//now write defines
		hfs.forEach(bf -> writeDefine(bf));
		
	}
	

	

	private void writeDefine(BaseField bf) {
		if(bf.getName() == null || bf.getParent() == null) {
			return;
		}
		if(bf instanceof BoolFieldField) {
			//don't do anything
		} else if(bf instanceof CompoundField) {
			//probably also don't do anything
		} else if(bf instanceof BoolField) {
			//do the byte index and the bit index
			writeDefine(makeBaseFieldNameRoot(bf).addSuffix("INDEX").toUpperSnake(), ""+((BoolFieldField)(bf.getParent())).getIndex(),bf);
			writeDefine(makeBaseFieldNameRoot(bf).addSuffix("BIT").toUpperSnake(), ""+bf.getIndex(),bf);
			writeDefine(makeBaseFieldNameRoot(bf).addSuffix("MASK").toUpperSnake(), "1 << "+bf.getIndex(),bf);

		} else {
			writeDefine(makeBaseFieldNameRoot(bf).addSuffix("INDEX").toUpperSnake(), ""+bf.getIndex(),bf);
		}
		
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
	
	private void addHeaderFieldGetters(BlockField top, boolean protoNotDeclaration) {
		ArrayList<BlockField> bfs = new ArrayList<BlockField>();
		//first find all blockfields with unique types
		top.scanThroughBlockFields((bf) -> {
			boolean found = false;
			for(BlockField bft : bfs) {
				if(bft.getTypeName().equals(bf.getTypeName())) {
					found = true;
					break;
				}
			}
			if(!found) {
				bfs.add(bf);
			}
		});
		
		//now do the stuff
		for(BlockField bf : bfs) {
			bf.scanThroughHeaderFields(f -> {
				if(f.getName() != null) {
					addBaseGetter(f, protoNotDeclaration, false);
					if(!protoNotDeclaration) {
						if(f.getName() != null) {
							indent();
							String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
							addBaseGetterGuts(f, dn, "return ", false);
							closeBrace();
						}
					}
				}
			}, false);
		}
		
		
//		top.scanThroughHeaderFields(f -> {
//			if(f instanceof BoolField) {
//				addBoolGetter((BoolField)f, protoNotDeclaration);
//			
//			} else if(f instanceof CompoundField) {
//				addCompoundGetterPrototype((CompoundField)f, top, protoNotDeclaration);
//			} else {
//				addBaseGetter(f, protoNotDeclaration);
//				if(!protoNotDeclaration) {
//					if(f.getName() != null) {
//						indent();
//						String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//						addBaseGetterGuts(f, dn, "return ");
//						closeBrace();
//					}
//				}
//			}
//		}, false);
	}

	private void addBaseFieldGetters(BlockField top, boolean protoNotDeclaration) {
		ArrayList<BlockField> bfs = new ArrayList<BlockField>();
		top.scanThroughBlockFields(bf -> {
//			if(!(bf instanceof ArrayField)) {
				bfs.add(bf);
//			}
		});
		for(BlockField bf : bfs) {
			for(BaseField f : bf.getNamedBaseFields()) {
				if(f instanceof BoolField) {
					addBoolGetter((BoolField)f, protoNotDeclaration);
				
				} else if(f instanceof CompoundField) {
					addCompoundGetterPrototype((CompoundField)f, top, protoNotDeclaration);
				} else {
					addBaseGetter(f, protoNotDeclaration, true);
					if(!protoNotDeclaration) {
						if(f.getName() != null) {
							indent();
							String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
							addBaseGetterGuts(f, dn, "return ", true);
							outdent();
							addLine("}");

						}
					}
				}
			}
		}
	
	}
	private void addCompoundGetterPrototype(CompoundField f, BlockField top, boolean protoNotDeclaration) {
		// TODO Auto-generated method stub
		
	}

	private void addBoolGetter(BoolField b, boolean protoNotDeclaration) {
		
		addBaseGetter(b, protoNotDeclaration, true);
		if(!protoNotDeclaration) {
			indent();
			String dn = makeBaseFieldNameRoot(b).addSuffix("INDEX").toUpperSnake();
			addBoolGetterGuts(b, dn, "return ");
			outdent();
			addLine("}");
		}
	
		
		
		
	}

	private void addBaseGetter(BaseField f, boolean protoNotDeclaration, boolean addBytesPerRepeat) {
		if(f.getName() != null) {

			String gs = "get";
			String c = f.getName().toLowerCamel()+" field of the " + f.getCorrectParentName().toLowerCamel() + " " + f.getCorrectParentName().toUpperCamel()+ "\n"+f.getComment();
			String rt = getBaseType(f);
			String function = makeBaseFieldNameRoot(f).toUpperCamel();
			
			boolean array = false;
			
			String arrayComment = "";
			if(addBytesPerRepeat && (f.getContainingWord().getParent()) instanceof ArrayField) {
				array = true;
				arrayComment = "@param i - index of array item to get\n"+
								"@param bytesPerRepeat - number of bytes in each array repeated element";
				
			}
			
			addDocComment( gs + "s the " + c + "\n"+
					"@param buf - the buffer containing the packet\n" +
					"@param currentBlock - the index of the block we're interested in\n"+
					arrayComment
					);
			
			
			
			String s = rt;
			String arrayParam = array ? ", uint32_t i, uint32_t bytesPerRepeat" : "";
			s += " " + gs + "Bb" + function + "(Bb* buf, BbBlock currentBlock"+arrayParam+")";
	
			s += protoNotDeclaration ? ";" : "{";
			addLine(s);

			
		}
	}
	private boolean isInArray(BaseField f) {
		Field cw = f.getContainingWord();
		BlockField bf = (BlockField)cw.getParent();//this must be true I think
		return (bf instanceof ArrayField);
	}
//	private void addBaseSetterGuts(BaseField f) {
//		String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//		addBaseSetterGuts(f, dn);
//	}
	private void addBaseSetterGuts(BaseField f, String index, String value) {
		String rt = getBaseType(f);
		String paramName = f.getCorrectParentName().toLowerCamel();

		String functionName = FieldName.fromSnake(f.getType().name()).addPrefix("bb").addPrefix("set").toLowerCamel();
		addLine(functionName + "(buf, currentBlock, " + index + ", " + value + ");");
	}
//	private void addBaseGetterGuts(BaseField f) {
//		String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//		addBaseGetterGuts(f, dn);
//	}
	private void addBaseGetterGuts(BaseField f, String index, String start, boolean doArrayStuff) {
		String rt = getBaseType(f);
		String paramName = f.getCorrectParentName().toLowerCamel();

		ArrayField af = null;

		String arrayParms = "";
		String arrayComment = "";
		Field p = f.getContainingWord().getParent();
		if(p instanceof ArrayField & doArrayStuff) {
			af = (ArrayField)p;
		
			arrayParms = " + (i * bytesPerRepeat)";
		
			
			arrayComment = " //magic number represents the number of bytes in each array rep";
		}

	
	
			
		String functionName = FieldName.fromSnake(f.getType().name()).addPrefix("bb").addPrefix("get").toLowerCamel();
		if(f instanceof EnumField) {
			functionName = "("+rt+")" + functionName;
		}
		addLine(start + functionName + "(buf, currentBlock , " + index +  arrayParms +");");
		
		
		
	}
//	private void addBoolSetterGuts(BaseField f) {
//		String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//
//		addBoolSetterGuts(f, dn);
//	}
	private void addBoolSetterGuts(BaseField f, String index, String value) {
		String paramName = f.getCorrectParentName().toLowerCamel();
		String functionName = FieldName.fromSnake(f.getType().name()).addPrefix("set","bb").toLowerCamel();
		String dbn = makeBaseFieldNameRoot(f).addSuffix("MASK").toUpperSnake();
		addLine(functionName + "(buf, currentBlock, " + index + ", " + dbn + ", " + value + ");");
	}
//	private void addBoolGetterGuts(BaseField f) {
//		String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//		addBoolGetterGuts(f, dn);
//	}
	private void addBoolGetterGuts(BaseField f, String index, String start) {
		String paramName = f.getCorrectParentName().toLowerCamel();
		String dbn = makeBaseFieldNameRoot(f).addSuffix("MASK").toUpperSnake();	
		String functionName = FieldName.fromSnake(f.getType().name()).addPrefix("get", "bb").toLowerCamel();
		addLine(start + functionName + "(buf, currentBlock , " + index + ", " + dbn + ");");

	}
	
	private void addBlockFunctionAdder(BlockField top, boolean protoNotDeclaration) {
		ArrayList<BlockField> fields = new ArrayList<BlockField>();
		//first scan for all blockfields with unique type names
		top.scanThroughBlockFields(bf -> {
			if(bf != top) {
				boolean found = false;
				for(BlockField f : fields) {
					
					if(f.getTypeName().equals(bf.getTypeName())) {
						found = true;
						break;
					}
				}
				if(!found) {
					fields.add(bf);
				}
			}
		});
		
		//now build the functions using the first block.
		if(fields.size() > 0) {
			BlockField bf = fields.get(0);
			FieldName tn = bf.getTypeName();
			FieldName functionName = tn.addPrefix("next").addPrefix("add");
			String f = "BbBlock " + functionName.toLowerCamel()+"(Bb* buf, BbBlock block)";
			addDocComment("computes the index of the next new block given the previous one.");
			BaseField lf = null; //length field
			for(BaseField bft : bf.getHeaderFields()) {
				if(bft instanceof CompoundField) {
					CompoundField cf = (CompoundField)bft;
					for(BaseField bf2 : cf.getBaseFields()) {
						if(bf2.getName() != null && bf2.getName().toLowerCamel().equals("length")) {
							lf = bf2;
							break;
						}
					}
				}
				if(lf != null) {
					break;
				} else if(bft.getName() != null && bft.getName().toLowerCamel().equals("length")) {
					lf = bft;
					break;
				}
			}
			
			if(lf == null || lf.getName() == null) {
				throw new RuntimeException("No length field found!");
			}

			if(protoNotDeclaration) {
				addLine(f + ";");
			} else {
				addLine(f + "{");
				indent();
//				addLine();
				//build the function name
//				BbBlock addPayload(Bb* buf, BbBlock prevPayload){
				
				//Packet packet = buf->start;
				//uint16_t pLen = getUint16(buf, PACKET_LENGTH_INDEX); 
				//Payload 
				
				addLine(top.getTypeName().toUpperCamel()+" p = buff->start;");
				String lenType = getBaseType(lf);
//				addLine(lenType + " pLen = "+new FieldName("get",lenType).toLowerCamel()+"(buf, ");

				
				
				String lgn = tn.addPrefix("get").addSuffix(lf.getName()).toLowerCamel();
//				String ldn = 
				addLine(getBaseType(lf) + " len = " + lgn + "(buf,  block);");//this gets the block length
				
				addLine("return block + len;");
				outdent();
				addLine("}");
			}
			
			
			
			
			
		}
	}
	
	private void addBlockFunctionGetters(BlockField top, boolean protoNotDeclaration) {
		ArrayList<BlockField> fields = new ArrayList<BlockField>();
		//first scan for all blockfields with unique type names
		top.scanThroughBlockFields(bf -> {
			if(bf != top) {
				boolean found = false;
				for(BlockField f : fields) {
					
					if(f.getTypeName().equals(bf.getTypeName())) {
						found = true;
						break;
					}
				}
				if(!found) {
					fields.add(bf);
				}
			}
		});
		
		//now build the functions using the first block.
		if(fields.size() > 0) {
			BlockField bf = fields.get(0);
			FieldName tn = bf.getTypeName();
			FieldName getterName = tn.addPrefix("get", "Bb", "next");
			FieldName setterName = tn.addPrefix("set","Bb", "next");
			String f = "BbBlock " + getterName.toLowerCamel()+"(Bb* buf, BbBlock block)";
			addDocComment("computes the index of the next block given the previous one.");
			BaseField lf = bf.getHeaderField("length");
			
							
			
			if(lf == null || lf.getName() == null) {
				throw new RuntimeException("No length field found!");
			}

			if(protoNotDeclaration) {
				addLine(f + ";");
			} else {
				addLine(f + "{");
				indent();
//				addLine();
				//build the function name
				String lgn = tn.addPrefix("get","bb").addSuffix(lf.getName()).toLowerCamel();
//				String ldn = 
				addLine(getBaseType(lf) + " len = " + lgn + "(buf,  block) * 4; //get length in words and convert to length in bytes");//this gets the block length
				addLine("return block + len;");
				outdent();
				addLine("}");
			}
			
			
			
			
			
		}
	}
	
	
	private String getBaseType(BaseField f) {
		String rt = "";
	
		Type t = f.getType();
		if(f instanceof EnumField) {
			rt = getEnumTypeName(f);
		} else {
	
			switch(t) {
			case COMPOUND:
			case ARRAY:
			case BLOCK:
				break;
			case BOOL:
				rt = "bool";
				break;
			case BOOLFIELD:
				rt = "uint8_t";
				break;
			case FLOAT32:
				rt = "float";
				break;
			case INT16:
				rt = "int16_t";
				break;
			case INT32:
				rt = "int32_t";
				break;
			case INT8:
				rt = "int8_t";
				break;
			case UINT16:
				rt = "uint16_t";
				break;
			case UINT32:
				rt = "uint32_t";
				break;
			case UINT8:
				rt = "uint8_t";
				break;
			default:
				break;
			
			}
		}
		return rt;
	}
	
	
	private String getEnumTypeName(BaseField f) {
		return f.getName().toUpperCamel();
	}

	private void addBlockKeyDefines(BlockField top) {
		List<FixedIntField> keys = getBlockKeys(top);
		
		for(FixedIntField key : keys) {
			String name = makeBaseFieldNameRoot(key).toUpperSnake();
			writeDefine(name, ""+key.getValue(), key);
		}
	}
	
	private void addBlockKeyEnum(BlockField top) {
		List<FixedIntField> keys = getBlockKeys(top);
		addLine("typedef enum {");
		indent();
		for(FixedIntField key : keys) {
//			String name = makeBaseFieldNameRoot(key).toUpperSnake();
			String name = makeKeyName(key);
			addLine(name + " = "+WriterUtils.formatAsHex(key.getValue())+",");
		}
		outdent();
		addLine("} BlockKeys;");
		addLine();
	}
	private void addBlockAdders(BlockField top, boolean protoNotDeclaration) {
		//first get all blocks that we want to make adders for
		List<BlockField> bfs = top.getAllBlockFields();
		
		for(BlockField bf : bfs) {
			addBlockAdder(bf, true, protoNotDeclaration);
			addBlockAdder(bf, false, protoNotDeclaration);
		}
	}

	private void addArrayGetters(BlockField top, boolean protoNotDeclaration) {
		List<ArrayField> afs = top.getAllArrayFields();
		for(ArrayField af : afs) {
			addArrayGetter(af, protoNotDeclaration);
		}
	}
	private void addArrayGetter(ArrayField bf, boolean protoNotDeclaration) {
		String blockName = bf.getName().toUpperCamel();
		String comment = "Adds a new "+blockName+" to the specified packet.\n"+bf.getComment();
		String functionName = "getBb"+blockName;
		List<BaseField> fs = bf.getNamedBaseFields();
		String paramList = "";
		
		
		for(BaseField f : fs) {
			paramList += ", "+getBaseType(f)+"* "+f.getName().toLowerCamel();
		}
		
		addDocComment(comment);
		addLine("void "+functionName+"(Bb* buf, BbBlock currentBlock, uint32_t n"+paramList+")"+(protoNotDeclaration ? ";" : "{"));
		if(!protoNotDeclaration) {
			indent();
			
			//now fill in the details
			BaseField lf = bf.getHeaderField("length");
			BaseField keyField = bf.getHeaderField("key");
			
			FieldName tn = bf.getTypeName();
			
			int blockLen = bf.getHeaderWordCount() + bf.getBaseWordCount();

			if(lf == null) {
				throw new RuntimeException("No length field found!");
			}
			if(keyField == null) {
				throw new RuntimeException("No key field found!");
			}
			
			String keyValue = makeBaseFieldNameRoot(keyField).toUpperSnake();
			String keyIndex = makeBaseFieldNameRoot(keyField).addSuffix("INDEX").toUpperSnake();
			String keyFuncName = FieldName.fromCamel("setBb").addSuffix(keyField.getType().name()).toLowerCamel();
			
			String lenValue = ""+blockLen;
			String lenIndex = makeBaseFieldNameRoot(lf).addSuffix("INDEX").toUpperSnake();
			String lenFuncName = FieldName.fromCamel("setBb").addSuffix(lf.getType().name()).toLowerCamel();
			
			
			
			
			
			
		
			
			
		
			
			//then do the params 
		
			addLineComment("Add base fields");
			addLine("for(uint32_t i = 0; i < n; ++i){");
			indent();
			for(BaseField f : fs) {
				String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
				String value = f.getName().toLowerCamel()+"[i] = ";

				if(fs.size() == 1) {
					dn += " + i ";
				} else {
					dn += " + (i * "+fs.size()+")";
				}
				if(f instanceof BoolField) {
					

					addBoolGetterGuts(f, dn, value);
				} else {
					addBaseGetterGuts(f, dn, value, true);
				}
			}
			outdent();
			addLine("}");
				
				
			
			
			
			//update the block length
			
			
			//return the new block index
//			addLine("return result;");
			
			
			outdent();
			addLine("}");
		}
	}
	private void addPacketStartFinish(BlockField top, boolean protoNotDeclaration) {
		String blockName = top.getName().toUpperCamel();
		List<BaseField> fs = top.getHeaderFields();
		String topLineEnd = protoNotDeclaration ? ";" : "{";
		int headerLength = top.getHeaderWordCount();
		
		addDocComment("Add packet header.\nThis only adds a placeholder - make sure you finish the packet."+top.getComment());
		addLine("BbBlock start"+blockName+"(Bb* buf)"+topLineEnd);
		if(!protoNotDeclaration) {
			indent();
			addLine("return "+headerLength*4+";");
			
			
			closeBrace();
		}
		addDocComment("Add packet header.\nThis only adds a placeholder - make sure you finish the packet."+top.getComment());
		addLine("void finish"+blockName+"(Bb* buf, BbBlock bb)"+topLineEnd);
		if(!protoNotDeclaration) {
			indent();
			
			FixedIntField preamble = (FixedIntField)top.getHeaderField("preamble");
			BaseField length = top.getHeaderField("length");
			BaseField crc = top.getHeaderField("crc");
			
			String preambleIndex = makeBaseFieldNameRoot(preamble).addSuffix("INDEX").toUpperSnake();
			String lengthIndex = makeBaseFieldNameRoot(length).addSuffix("INDEX").toUpperSnake();
			String crcIndex = makeBaseFieldNameRoot(crc).addSuffix("INDEX").toUpperSnake();
			
			String preambleSetter = FieldName.fromCamel("setBb").addSuffix(preamble.getType().name()).toLowerCamel();
			String lengthSetter = FieldName.fromCamel("setBb").addSuffix(length.getType().name()).toLowerCamel();
			String crcSetter = FieldName.fromCamel("setBb").addSuffix(crc.getType().name()).toLowerCamel();

			String preambleVal = makeBlockValueDefine(preamble);
			String lengthVal = "bb>>2";//(buf->start - bb)>>2";
			String start = top.getName().addSuffix("first","block","index").toUpperSnake();
			String crcVal = "computeCrc(buf, "+start+", bb)";
			
			addLine(preambleSetter+"(buf, 0, "+preambleIndex+", "+preambleVal+");");
			addLine(lengthSetter+"(buf, 0, "+lengthIndex+", "+lengthVal+");");
			addLine(crcSetter+"(buf, 0, "+crcIndex+", "+crcVal+");");
			
			
			closeBrace();
		}
	}
	private void addBlockAdder(BlockField bf, boolean withParamsNotWithout, boolean protoNotDeclaration) {
		String blockName = bf.getName().toUpperCamel();
		String comment = "Adds a new "+blockName+" to the specified packet.\n"+bf.getComment();
		String functionName = "add"+(withParamsNotWithout ? "" : "Empty")+"Bb"+blockName;
		List<BaseField> fs = bf.getNamedBaseFields();
		String paramList = "";
		if(withParamsNotWithout && fs.size() == 0) {
			//don't do anything if this block does not have parameters but we're doing the version with params
			return;
		}
		if(withParamsNotWithout) {
			for(BaseField f : fs) {
				paramList += ", "+getBaseType(f)+" "+f.getName().toLowerCamel();
			}
		}
		addDocComment(comment);
		addLine("BbBlock "+functionName+"(Bb* buf, BbBlock currentBlock"+paramList+")"+(protoNotDeclaration ? ";" : "{"));
		if(!protoNotDeclaration) {
			indent();
			
			//now fill in the details
			BaseField lf = bf.getHeaderField("length");
			FixedIntField keyField = (FixedIntField)bf.getHeaderField("key");
			
			FieldName tn = bf.getTypeName();
			
			int blockLen = bf.getHeaderWordCount() + (withParamsNotWithout ? bf.getBaseWordCount() : 0);

			if(lf == null) {
				throw new RuntimeException("No length field found!");
			}
			if(keyField == null) {
				throw new RuntimeException("No key field found!");
			}
			
			String keyValue = makeBlockValueDefine(keyField);
			String keyIndex = makeBaseFieldNameRoot(keyField).addSuffix("INDEX").toUpperSnake();
			String keyFuncName = FieldName.fromCamel("setBb").addSuffix(keyField.getType().name()).toLowerCamel();
			
			String lenIndex = makeBaseFieldNameRoot(lf).addSuffix("INDEX").toUpperSnake();
			String lenFuncName = FieldName.fromCamel("setBb").addSuffix(lf.getType().name()).toLowerCamel();
			
			
			
//			//first setup index
//			addLineComment("Compute index of new block");
////			addLine(bf.getTypeName().toUpperCamel()+" p = buff->start;");
//			String lenType = getBaseType(lf);
//
//			
//			
//			String lgn = tn.addPrefix("get","bb").addSuffix(lf.getName()).toLowerCamel();
//			addLine(getBaseType(lf) + " len = " + lgn + "(buf,  currentBlock);");//this gets the block length
//			addLine("BbBlock result = bbWrap(buf, currentBlock + len);");
			
			
			
			
			
			//first do the header stuff
			addLine();
			addLine("BbBlock nextBlock = currentBlock + "+blockLen*4 + ";//sorry about the magic number");
			addLineComment("Add header fields");
			//write the key
			addLine(keyFuncName+"(buf, currentBlock, "+keyIndex+", "+keyValue+");");
			//write the length
			addLine(lenFuncName+"(buf, currentBlock, "+lenIndex+", nextBlock >> 2); //length field is in words, not bytes");
			
			
			//then do the params if we're doing params
			if(withParamsNotWithout) {
				addLine();
				addLineComment("Add base fields");
				
				for(BaseField f : fs) {
//					String value = f.getName().toLowerCamel();
//					String index = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
//					String funcName = FieldName.fromCamel("setBb").addSuffix(keyField.getType().name()).toLowerCamel();
//					addLine(funcName+"(buf, result, "+index+", "+value+");");
					String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
					String value = f.getName().toLowerCamel();

					if(f instanceof BoolField) {
						addBoolSetterGuts(f, dn, value);
					} else {
						addBaseSetterGuts(f, dn, value);
					}
				}
				
				
			}
			
			
			
			//update the block length
			
			
			//return the new block index
			addLine("return nextBlock;");
			
			
			outdent();
			addLine("}");
		}
	}

	
	private void addFirstBlockDefine(BlockField top) {
		String n = top.getName().addSuffix("first","block","index").toUpperSnake();
		addBlockComment("This defines the starting position of the first block after the packet header");
		addLine("#define "+n+" ("+top.getHeaderWordCount()*4+")");
	}
	private void addArrayAdders(BlockField top, boolean protoNotDeclaration) {
		List<ArrayField> afs = top.getAllArrayFields();
		for(ArrayField af : afs) {
			addArrayAdder(af, protoNotDeclaration);
			addArrayElementAdder(af, protoNotDeclaration);
		}
	}
	
	
	
	private void addArrayAdder(ArrayField bf, boolean protoNotDeclaration) {
		String blockName =  bf.getName().toUpperCamel();
		String comment = "Adds a new "+blockName+" to the specified packet.\n"+bf.getComment();
		String functionName = "addBb"+blockName;
		List<BaseField> fs = bf.getNamedBaseFields();
		String paramList = "";
		
		
	
		
		addDocComment(comment);
		addLine("BbBlock "+functionName+"(Bb* buf, BbBlock currentBlock, uint32_t n)"+(protoNotDeclaration ? ";" : "{"));
		if(!protoNotDeclaration) {
			indent();
			
			//now fill in the details
			BaseField lf = bf.getHeaderField("length");
			FixedIntField keyField = (FixedIntField)bf.getHeaderField("key");
			BaseField rf = bf.getHeaderField("repeats");
			
			FieldName tn = bf.getTypeName();
			
			int blockLen = bf.getHeaderWordCount() + bf.getBaseWordCount();

			if(lf == null) {
				throw new RuntimeException("No length field found!");
			}
			if(keyField == null) {
				throw new RuntimeException("No key field found!");
			}
			
			String keyValue = makeBlockValueDefine(keyField);
			String keyIndex = makeBaseFieldNameRoot(keyField).addSuffix("INDEX").toUpperSnake();
			String keyFuncName = FieldName.fromCamel("setBb").addSuffix(keyField.getType().name()).toLowerCamel();
			
			String lenValue = ""+blockLen;
			String lenIndex = makeBaseFieldNameRoot(lf).addSuffix("INDEX").toUpperSnake();
			String lenFuncName = FieldName.fromCamel("setBb").addSuffix(lf.getType().name()).toLowerCamel();
			
			String repeatValue = "n";
			String repeatIndex = makeBaseFieldNameRoot(rf).addSuffix("INDEX").toUpperSnake();
			String repeatFuncName = FieldName.fromCamel("setBb").addSuffix(lf.getType().name()).toLowerCamel();
			
			
			//first setup index
			addLineComment("Compute index of new block");
//			addLine(bf.getTypeName().toUpperCamel()+" p = buff->start;");
			String lenType = getBaseType(lf);

			
			
			String lgn = tn.addPrefix("get","bb").addSuffix(lf.getName()).toLowerCamel();
//			addLine(getBaseType(lf) + " len = " + lgn + "(buf,  currentBlock);");//this gets the block length
//			addLine("BbBlock result = bbWrap(buf, currentBlock + len);");
			
			
		
			
			
			//first do the header stuff
			addLine();
			
			addLine("BbBlock nextBlock =  currentBlock + "+bf.getHeaderWordCount()*4+" + (n * "+bf.getBaseWordCount()*4+");//a couple magic numbers: header length plus n * base fields length");
			addLineComment("Add header fields");
			//write the key
			addLine(keyFuncName+"(buf, currentBlock, "+keyIndex+", "+keyValue+");");
			//write the length
			addLine(lenFuncName+"(buf, currentBlock, "+lenIndex+", nextBlock >> 2);//length is in words not bytes");
			//write the repeats field
			addLine(lenFuncName+"(buf, currentBlock, "+repeatIndex+", "+repeatValue+");");
			
			
			

			
			
			//return the new block index
			addLine("return nextBlock;");
			
			
			outdent();
			addLine("}");
		}
	}
	
	private void addArrayElementAdder(ArrayField bf, boolean protoNotDeclaration) {
	
		List<BaseField> fs = bf.getNamedBaseFields();
		
		//do this once for each base field
		for(BaseField f : fs) {
			String blockName =  bf.getName().toUpperCamel();
			String comment = "sets an element into the specified "+blockName+" in the specified packet.\n"+bf.getComment();
			String functionName = bf.getName().addPrefix("set","bb").addSuffix(f.getName()).toLowerCamel();
			String paramName = f.getName().toLowerCamel();
			String paramType = getBaseType(f);
		
			addDocComment(comment);
			addLine("void "+functionName+"(Bb* buf, BbBlock currentBlock, uint32_t i, "+paramType+" "+paramName+")"+(protoNotDeclaration ? ";" : "{"));
			if(!protoNotDeclaration) {
				indent();
				
			
				
				//then do the params 
			
			
				String dn = makeBaseFieldNameRoot(f).addSuffix("INDEX").toUpperSnake();
				if(fs.size() == 1) {
					dn += " + i ";
				} else {
					dn += " + (i * "+fs.size()*4+")";
				}
				
				String value = paramName;

				if(f instanceof BoolField) {
					addBoolSetterGuts(f, dn, value);
				} else {
					addBaseSetterGuts(f, dn, value);
				}

				//update the block length
				
				
				//return the new block index
				
				
				closeBrace();
			}
		}

	}

}

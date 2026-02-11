/*
Copyright (c) 2025  Blue Robotics

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
package com.bluerobotics.blueberry.schema.parser.fields;

import com.bluerobotics.blueberry.schema.parser.constants.StringConstant;
import com.bluerobotics.blueberry.schema.parser.fields.EnumField.NameValue;
import com.bluerobotics.blueberry.schema.parser.fields.MultipleField.Index;
import com.bluerobotics.blueberry.schema.parser.fields.SymbolName.Case;

/**
 * A class to make names for various constants, variables, etc used in the source writers
 */
public class NameMaker {
	public static SymbolName makeMessageKeyName(MessageField mf) {

	
		
		SymbolName result = mf.getTypeName().toSymbolName().append("key").toUpperSnake();
		return result;
	}
	public static String makeMessageLengthName(MessageField mf) {
		return mf.getTypeName().deScope().append("length").toUpperSnakeString();
	}
	public static String makeBooleanMaskName(Field f) {
		if(f.getBitCount() != 1) {
			throw new RuntimeException("This should only be used for boolean fields, not this one: "+f);
		}
		return makeName(f, true).append("mask").toUpperSnakeString();
	}
	public static String makeBooleanBitNumName(Field f) {
		if(f.getBitCount() != 1) {
			throw new RuntimeException("This should only be used for boolean fields, not this one: "+f);
		}
		return makeName(f, true).append("bit", "num").toUpperSnakeString();
	}
	public static String makeArraySizeName(Index pi) {
		SymbolName result = makeName(pi.p, true).append("size");
		if(pi.ofN >= 0) {
			result = result.append("" + pi.i);
		}
		return  result.toUpperSnakeString();
	}
	public static String makeMultipleFieldElementByteCountName(Index pi) {
		
		
		SymbolName result = makeName(pi.p, true).append((pi.arrayNotSequence ? "array" : "sequence"), "element", "byte", "count");
		if(pi.ofN > 1) {
			result = result.append("" + pi.i);
		}
	
		return  result.toUpperSnakeString();
	}
	public static String makeFieldIndexName(Field f) {
		return makeName(f, true).append("index").toUpperSnakeString();
	}
	public static String makeFieldOrdinalName(Field f) {
		return makeName(f, true).append("ordinal").toUpperSnakeString();

	}
	public static String makeMessageMaxOrdinalName(MessageField mf) {
		
		return mf.getTypeName().deScope().append("max","ordinal").toUpperSnakeString();
	}

	/**
	 * Traverse the parent hierarchy of this field until a message field is reached
	 * Construts a scope name from all the names up to the message and prepends the message type
	 * the rightmost scope level should be the field name
	 * the leftmost scope level should be the message name
	 * @param f
	 * @return
	 */
	protected static ScopeName makeScopeName(Field f, boolean includeMessageName) {
		ScopeName result = ScopeName.wrap(SymbolName.EMPTY);
		MessageField mf = null;
		Field ft = f;
		while(ft != null && mf == null) {
			SymbolName n = ft.getName();
			result = result.addLevelAbove(n);
			ft = ft.getParent();
			if(ft != null) {
				mf = ft.asType(MessageField.class);
			}
			
		}
		
		if(mf == null) {
			throw new RuntimeException("Could not determine the message that this field is part of "+f);
		}
		if(includeMessageName) {
			result = result.addLevelAbove(mf.getTypeName().deScope());
		}
		return result;
	}
	public static String makeEnumName(EnumField ef) {
		return ef.getTypeName().deScope().toUpperCamelString();
	}
	public static String makeEnumItemName(EnumField ef, NameValue nv) {
		return nv.getName().prepend(ef.getTypeName().deScope()).toUpperSnakeString();
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
	private static SymbolName makeName(Field f, boolean includeMessage) {
		SymbolName result = SymbolName.EMPTY;
		
		Field pf = f;
		while(pf != null) {
			 SymbolName n = pf.getName();
			 
			
			if(n != null) {
				result = result.prepend(n);
			}
			pf = pf.getParent();
			if(pf instanceof MessageField) {
				if(includeMessage) {
					MessageField mf = (MessageField)pf;
					result = result.prepend(mf.getTypeName().deScope());
				}
				break;
			}
		}
		if(f instanceof SequenceField) {
			result = result.append("placeholder");
		} else if(f instanceof ArrayField) {
			result = result.append("array");
		}
		
		return result;
	}
	
	public static SymbolName makeParamName(Field f) {
		return makeName(f, false).toLowerCamel();
	}

	
	/**
	 * creates the name for the index for an array or sequence
	 * @param f - the array or sequence
	 * @param i - the number of the index. -1 causes the index to be left off the name
	 * @return
	 */
	public static String makeIndexName(Index idx) {
		MultipleField f = idx.p;
		int i = idx.ofN == 1 ? -1 : idx.i;
		SymbolName name = f.getName();
		if(name == null) {
			name = f.getParent().getName();
		}
		name = name.append("Index");
		if(i >= 0) {
			name = name.append(""+i);
		}
		return name.toLowerCamel().toString();
	}
	
	public static String  makeFieldGetterName(Field f, boolean includeMessage) {
		return makeScopeName(f, includeMessage).toSymbolName().prepend("get").toLowerCamel().toString();
	}
	public static String  makeFieldSetterName(Field f, boolean includeMessage) {
		return makeScopeName(f, includeMessage).toSymbolName().prepend("set").toLowerCamel().toString();
	}
	public static String  makeJavaFieldPresenceTesterName(Field f, boolean includeMessage) {
		return makeScopeName(f, includeMessage).deScope().prepend("is").append("present").toLowerCamel().toString();
	}
	public static String  makeFieldPresenceTesterName(Field f,boolean includeMessage) {
		return makeScopeName(f, includeMessage).toSymbolName().prepend("is").append("present").toLowerCamel().toString();
	}
	public static String makeStringCopierName(StringField f, boolean toNotFrom, boolean includeMessage) {
		return "copy"+(toNotFrom ? "To" : "From")+makeScopeName(f, includeMessage).toSymbolName().toUpperCamelString();
	}
	public static String makeStringLengthGetterName(StringField f, boolean includeMessage) {
		ScopeName name = NameMaker.makeScopeName(f, includeMessage);

		return "get"+name.toSymbolName().toUpperCamelString()+"StringLength";
	}
	public static String makeSequenceLengthGetterName(SequenceField sf, boolean includeMessage) {
		return makeScopeName(sf, includeMessage).toSymbolName().toUpperCamelString()+"SequenceLength";
	}
	
	public static String makeSequenceInitName(SequenceField sf, boolean includeMessage) {
		return "init"+NameMaker.makeScopeName(sf, includeMessage).toSymbolName().toUpperCamelString();
	}
	public static String makeCModuleFileName(BlueModule m, boolean headerNotSource) {
		String result = m.getName().deScope().toLowerCamel().toString();
		result += headerNotSource ? ".h" : ".c";
		return result;
	}
	public static String makeJavaConstantInterface(BlueModule m) {
		String result = m.getName().deScope().append("constants").toUpperCamelString();
		return result;
	}
	public static SymbolName makePacketBuilderName(BlueModule m) {
		return m.getName().deScope().append("builder").toUpperCamel();
	}
	public static ScopeName makePackageName(BlueModule m, ScopeName prefix) {
		return prefix.addLevelBelow(m.getName().makeRelative());
	}
	public static SymbolName makeJavaMessageClass(MessageField msg) {
		return msg.getTypeName().deScope().toUpperCamel();
	}

	public static SymbolName makeMessageModuleMessageConstant(MessageField mf) {
		return mf.getTypeName().deScope().append("module","message", "key").toUpperSnake();
	}
	public static SymbolName makeStringMaxLengthName(StringField f) {
		return makeName(f, true).append("max", "length").toUpperSnake();
	}
//	public static String makeStringMaxLengthName(StringField f) {
//		return makeName(f, true).append("max", "length").toUpperSnakeString();
//	}
	public static SymbolName makeCModuleFileRoot(BlueModule module) {
		return module.getName().deScope().toLowerCamel();
	}
	public static String makeConstantName(StringConstant sc) {
		return sc.getName().toUpperSnakeString();
	}
}

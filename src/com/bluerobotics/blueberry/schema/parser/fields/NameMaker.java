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

import com.bluerobotics.blueberry.schema.parser.fields.EnumField.NameValue;
import com.bluerobotics.blueberry.schema.parser.fields.MultipleField.Index;

/**
 * A class to make names for various constants, variables, etc used in the source writers
 */
public class NameMaker {
	public static String makeMessageKeyName(MessageField mf) {
		return mf.getTypeName().deScope().append("key").toUpperSnake();
	}
	public static String makeMessageLengthName(MessageField mf) {
		return mf.getTypeName().deScope().append("length").toUpperSnake();
	}
	public static String makeBooleanMaskName(Field f) {
		if(f.getBitCount() != 1) {
			throw new RuntimeException("This should only be used for boolean fields, not this one: "+f);
		}
		return makeName(f, true).append("mask").toUpperSnake();
	}
	public static String makeArraySizeName(Index pi) {
		SymbolName result = makeName(pi.p, true).append("size");
		if(pi.ofN >= 0) {
			result = result.append("" + pi.i);
		}
		return  result.toUpperSnake();
	}
	public static String makeMultipleFieldElementByteCountName(Index pi) {
		
		
		SymbolName result = makeName(pi.p, true).append("element", "byte", "count");
		if(pi.ofN > 1) {
			result = result.append("" + pi.i);
		}
	
		return  result.toUpperSnake();
	}
	public static String makeFieldIndexName(Field f) {
		return makeName(f, true).append("index").toUpperSnake();
	}
	public static String makeFieldOrdinalName(Field f) {
		return makeName(f, true).append("ordinal").toUpperSnake();

	}
	/**
	 * Traverse the parent hierarchy of this field until a message field is reached
	 * Construts a scope name from all the names up to the message and prepends the message type
	 * the rightmost scope level should be the field name
	 * the leftmost scope level should be the message name
	 * @param f
	 * @return
	 */
	public static ScopeName makeScopeName(Field f) {
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
		result = result.addLevelAbove(mf.getTypeName().deScope());
		return result;
	}
	public static String makeEnumName(EnumField ef, NameValue nv) {
		return nv.getName().prepend(ef.getTypeName().deScope()).toUpperSnake();
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
		SymbolName result = f.getName();
		if(result == null) {
			result = f.getParent().getName();
			
		}
		ParentField pf = f.getParent();
		while((pf != null) && !(pf instanceof MessageField)) {
			SymbolName pn = null;
			pn = pf.getName();
			
			
			result = result.prepend(pn);
			pf = pf.getParent();
		}
		MessageField mf = f.getAncestor(MessageField.class);
		if(mf != null) {
			result = result.prepend(mf.getTypeName().deScope());
		}
		return result;
	}
	
	public static String makeParamName(Field f) {
		return makeName(f, false).toLowerCamel();
	}
	public static String makeStringMaxLengthName(StringField f) {
		return makeName(f, true).append("max", "length").toUpperSnake();
	}
	/**
	 * creates the name for the index for an array or sequence
	 * @param f - the array or sequence
	 * @param i - the number of the index. -1 causes the index to be left off the name
	 * @return
	 */
	public static String makeMultipleFieldIndexParamName(MultipleField f, int i) {
		SymbolName name = f.getName();
		if(name == null) {
			name = f.getParent().getName();
		}
		name = name.append("Index");
		if(i >= 0) {
			name = name.append(""+i);
		}
		return name.toLowerCamel();
	}


}

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
package com.bluerobotics.blueberry.schema.parser.fields;

/**
 *
 */
public class FieldUtils {

	/**
	 * traverse header and base fields of blocks to number them
	 * @param bf
	 */
	public void computeIndeces(AbstractField f, int i) {
//		if(f instanceof CompactArrayField) {
//			StructField bf = (StructField)f;
//			i = 0;
//			for(BaseField fb : bf.getHeaderFields()) {
//				computeIndeces(fb, i);
//				i += 4;
//			}
//			for(BaseField fb : bf.getBaseFields()) {
//				computeIndeces(fb,i);
//				i += fb.getBitCount()/8;
//			}
//		} else if(f instanceof StructField) {
//			StructField bf = (StructField)f;
//			i = 0;
//			for(BaseField fb : bf.getHeaderFields()) {
//				computeIndeces(fb, i);
//				i += 4;
//			}
//			for(BaseField fb : bf.getBaseFields()) {
//				computeIndeces(fb,i);
//				i += 4;
//			}
//			for(StructField bf2 : bf.getBlockFields()) {
//				computeIndeces(bf2,0);
//			}
//		} else if(f instanceof CompoundField) {
//			CompoundField cf = (CompoundField)f;
//			cf.setIndex(i);
//			for(BaseField fb : cf.getBaseFields()) {
//				computeIndeces(fb,i);
//				i += fb.getBitCount()/8;
//			}
//		} else if(f instanceof BoolFieldField) {
//			BoolFieldField bf = (BoolFieldField)f;
//			bf.setIndex(i);
//			int j = 0;
//			for(BoolField bool : bf.getBoolFields()) {
//				bool.setIndex(j);
//				++j;
//			}
//		} else if(f instanceof BaseField) {
//			((BaseField) f).setIndex(i);
//		}

	}


}


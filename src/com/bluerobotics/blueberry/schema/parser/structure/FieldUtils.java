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
package com.bluerobotics.blueberry.schema.parser.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class FieldUtils {
	public void padExtraSpaceInCompoundFields(BlockField bf) {
		bf.scanThroughBaseFields((f,p) -> {
			if(f instanceof CompoundField) {
				CompoundField cf = (CompoundField)f;
				int r = cf.getRoom();
				if(r == 8) {
					cf.add(new BaseField(null, Type.UINT8, null));
				} else if(r == 16) {
					cf.add(new BaseField(null, Type.UINT16, null));
				} else if(r == 24) {
					cf.add(new BaseField(null, Type.UINT8, null));
					cf.add(new BaseField(null, Type.UINT16, null));
		
				}
			}
		}, true);
	}
	/**
	 * traverse header and base fields of blocks to number them
	 * @param bf
	 */
	public void computeIndeces(AbstractField f, int i) {
		if(f instanceof BlockField) {
			BlockField bf = (BlockField)f;
			i = 0;
			for(BaseField fb : bf.getHeaderFields()) {
				computeIndeces(fb, i);
				i += 4;
			}
			for(BaseField fb : bf.getBaseFields()) {
				computeIndeces(fb,i);
				i += 4;
			}
			for(BlockField bf2 : bf.getBlockFields()) {
				computeIndeces(bf2,0);
			}
		} else if(f instanceof CompoundField) {
			CompoundField cf = (CompoundField)f;
			cf.setIndex(i);
			for(BaseField fb : cf.getBaseFields()) {
				computeIndeces(fb,i);
				i += fb.getBitCount()/8;
			}
		} else if(f instanceof BoolFieldField) {
			BoolFieldField bf = (BoolFieldField)f;
			bf.setIndex(i);
			int j = 0;
			for(BoolField bool : bf.getBoolFields()) {
				bool.setIndex(j);
				++j;
			}
		} else if(f instanceof BaseField) {
			((BaseField) f).setIndex(i);
		}
		
	}
	public List<BlockField> getAllBlockFields(BlockField top){
		ArrayList<BlockField> result = new ArrayList<BlockField>();
		result.add(top);
		for(BlockField bf : top.getBlockFields()) {
			result.addAll(getAllBlockFields(bf));
		}
		return result;
		
	}
	/**
	 * traverses the hierarchy and fills in the parents of all the fields
	 * @param top
	 */
	public void computeParents(BlockField top) {
		//recurse
		for(BlockField bf : top.getBlockFields()) {
			computeParents(bf);
		}
		//do header fields
		setParents(top.getTypeName(), top.getHeaderFields());
		//now base fields
		setParents(top.getName(), top.getBaseFields());
	}
	private void setParents(FieldName p, List<BaseField> fs) {
		for(BaseField f : fs) {
			if(f instanceof CompoundField) {
				CompoundField cf = (CompoundField)f;
				cf.setParentName(p);
				setParents(p, cf.getBaseFields());
			} else if(f instanceof BoolFieldField) {
				BoolFieldField bff = (BoolFieldField)f;
				bff.setParentName(p);
				for(BoolField b : bff.getBoolFields()) {
					b.setParentName(p);
				}
			} else {
				f.setParentName(p);
			}
		}
	}
	/**
	 * replaces duplicate header fields with the first instance of it
	 * @param top
	 * @param uniques
	 */
	public void removeDuplicates(BlockField top, List<BaseField> uniques) {
		if(uniques == null) {
			uniques = new ArrayList<BaseField>();
		}
		for(BlockField bf : top.getBlockFields()) {
			removeDuplicates(bf, uniques);
		}
		checkAndReplaceField(top.getHeaderFields(), uniques);
		
	}
	private void checkAndReplaceField(List<BaseField> fs, List<BaseField> uniques) {
		for(int i = 0; i < fs.size(); ++i) {
			BaseField f = fs.get(i);
			BaseField found = null;
			if(f instanceof CompoundField) {
				CompoundField cf = (CompoundField)f;
				
				checkAndReplaceField(cf.getBaseFields(), uniques);
			} else if(f instanceof BoolFieldField) {
				BoolFieldField bff = (BoolFieldField)f;
				ArrayList<BaseField> bfs = new ArrayList<BaseField>();
				for(BoolField bf : bff.getBoolFields()) {
					bfs.add(bf);
				}
				checkAndReplaceField(bfs, uniques);
			}
			
			for(BaseField bft : uniques) {
				if(bft.equals(f)) {
					found = bft;
				}
			}
			if(found != null) {
				fs.set(i, found);
			} else {
				uniques.add(f);
			}
		}
	}
	
}


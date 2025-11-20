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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * 
 */
public class ScopeName extends SymbolName {
	private static final String SEPARATOR = "%^&";//probably doesn't matter what this is so long as it's unique and unlikely to occur in a string.
	public static final ScopeName ROOT = new ScopeName(Case.LOWER_SNAKE, SEPARATOR); 
	private ScopeName(Case c, String... ss) {
		super(c, ss);
		
		
	}
	private ScopeName(Case c, List<String> ss) {
		this(c, ss.toArray(new String[ss.size()]));
	}
	
	private ScopeName makeFromSymbols(List<SymbolName> sns, boolean absolute) {
		
		ArrayList<String> result = new ArrayList<>();

		for(SymbolName sn : sns) {
			if(result.size() != 0 || absolute) {
				result.add(SEPARATOR);
			}
			result.addAll(Arrays.asList(sn.m_name));
		}
		return new ScopeName(getCase(), result);
	}
	public static ScopeName wrap(SymbolName sn) {
		if(sn instanceof ScopeName) {
			return (ScopeName)sn;
		} else if(sn == null) {
			return null;
		}
		return new ScopeName(sn.getCase(), sn.m_name);
	}
	/**
	 * Splits this name by the specified scope separator
	 * and create a list of symbol names for each chunk
	 * @param separator
	 * @return
	 */
	private ArrayList<SymbolName> splitScope() {
		ArrayList<SymbolName> result = new ArrayList<>();
		ArrayList<String> ss = new ArrayList<>();
		ListIterator<String> li = Arrays.asList(m_name).listIterator();
		while(li.hasNext()) {
			String s = li.next();
			if(!s.equals(SEPARATOR)) {
				ss.add(s);
			} else if(!li.hasNext()) {
				if(!ss.isEmpty()) {
					result.add(new SymbolName(getCase(), ss.toArray(new String[ss.size()])));
					ss.clear();
				}
			}
		
		}
		return result;
		
	}
	/**
	 * removes the rightmost scoping term, i.e. the rightmost parts of the name before the first scoping separator
	 * @return
	 */
	public ScopeName removeLastLevel() {
		List<SymbolName> ss = splitScope();
		ss.removeLast();
		ScopeName result = makeFromSymbols(ss, isAbsolute());
		return result;
	}
	
	/**
	 * adds a new level of scope to the end (right) of this ScopeName
	 * @param scope
	 * @return
	 */
	public ScopeName addLevelBelow(SymbolName scope) {
		ScopeName result = this;
		ScopeName s = ScopeName.wrap(scope);
		if(scope == null || scope.isEmpty()) {
		} else if(isEmpty()) {
			result = s;
		} else {
			List<SymbolName> nms = splitScope();
			nms.addAll(s.splitScope());
		
			result = makeFromSymbols(nms, isAbsolute());
		}
		System.out.println("ScopeName.addLevelBelow "+result);
		return result;
	}
	/**
	 * adds a new level of scope to the start (left) of this ScopeName
	 * @param scope
	 * @return
	 */
	public ScopeName addLevelAbove(SymbolName scope) {
		ScopeName result = this;
		ScopeName s = ScopeName.wrap(scope);
		if(s != null) {
			result = s.addLevelBelow(this);	
		}
		
		
		return result;
	}
	public boolean isAbsolute() {
		boolean result = false;
		if(m_name.length > 0) {
			if(m_name[0].equals(SEPARATOR)) {
				result = true;
			}
		}
		return result;
	}
	public ScopeName makeAbsolute() {
		if(isAbsolute()) {
			return this;
		}
		return this.addLevelAbove(ROOT);
	}
	
	
	/**
	 * checks to see if this symbol name is a match for the specified name and list of imported scope.
	 * This assumes that ScopeName is absolute in scope (starts with separator)
	 * @param separator - the character sequence that is used to separate the scope from the name
	 * @param imports - an array of scope names, any one of which might match the scope of this symbol name
	 * @param name - the local name that is being compared to this symbol name. This can be absolute or relative
	 * @return
	 */
	public boolean isMatch(ScopeName[] imports, SymbolName name) {
		if(!isAbsolute()) {
			throw new RuntimeException("This scope name must be absolute!");
		}
		boolean result = false;
		ScopeName sn = ScopeName.wrap(name);
		if(sn.isAbsolute()) {
			result = equals(name);
		} else {
			for(ScopeName n : imports) {
				ScopeName nn = n.addLevelBelow(name);
				if(nn.equals(this)) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	public ScopeName addRoot() {
		ScopeName result = this;
	
		if(!isAbsolute()) {
			result = wrap(prepend(SEPARATOR));
		}
		return result;
	}
	/**
	 * Removes any elements that occur to the left of the last separator. It also removes the separator.
	 * @param separator - the character sequence that is used to separate the scope from the name
	 * @return
	 */
	public ScopeName deScope( ) {
		ArrayList<String> result = new ArrayList<>();
		
		for(int i = m_name.length - 1; i >= 0; --i) {
			String s = m_name[i];
			if(s.equals(SEPARATOR)) {
				break;
			} else {
				result.add(0, s);
			}
		}
		return wrap(make(result));
	}
	/**
	 * Combines the two symbol names into a multilevel scope name
	 * @param sn1
	 * @param sn2
	 * @return
	 */
	public static ScopeName combine(SymbolName sn1, SymbolName sn2) {
		return ScopeName.wrap(sn1).addLevelBelow(sn2);
	}
	public String toString() {
		List<SymbolName> sns = splitScope();
		String result = "";
		
		boolean firstTime = !isAbsolute();
		for(SymbolName sn : sns) {
			
			if(!firstTime) {
				result += "/";
			}
			firstTime = false;
	
			result += sn.toLowerSnake();
		
		}

		return result;
	}
	/**
	 * Strips out the separators and makes a symbol name of all other terms
	 * @return
	 */
	public SymbolName toSymbolName() {
		ArrayList<String> result = new ArrayList<>();
		for(String s : m_name) {
			if(!s.equals(SEPARATOR)) {
				result.add(s);
			}
		}
		return SymbolName.make(result);
	}


}

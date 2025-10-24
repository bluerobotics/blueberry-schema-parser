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

/**
 * 
 */
public class ScopeName extends SymbolName {
	private final String m_separator;
	private ScopeName(Case c, String separator, String... ss) {
		super(c, ss);
		m_separator = separator;
		
	}
	private ScopeName(Case c, String separator, List<String> ss) {
		this(c, separator, ss.toArray(new String[ss.size()]));
	}
	
	private static ScopeName make(Case c, SymbolName[] names, String separator) {
		ArrayList<String> result = new ArrayList<>();
		for(SymbolName sn : names) {
			result.add(separator);
			result.addAll(Arrays.asList(sn.m_name));
		}
		return new ScopeName(c, separator, result);
	}
	private static ScopeName wrap(SymbolName sn, String separator) {
		if(sn instanceof ScopeName) {
			return (ScopeName)sn;
		}
		return new ScopeName(sn.getCase(), separator, sn.m_name);
	}
	/**
	 * Splits this name by the specified scope separator
	 * @param separator
	 * @return
	 */
	private List<SymbolName> splitScope() {
		ArrayList<SymbolName> result = new ArrayList<>();
		ArrayList<String> ss = new ArrayList<>();
		for(int i = 0; i < m_name.length; ++i) {
			String s = m_name[i];
			if(s.equals(m_separator) || (i == m_name.length - 1)) {
				if(ss.size() > 0) {
					result.add(new SymbolName(getCase(), ss.toArray(new String[ss.size()])));
					ss.clear();
				}
			} else {
				ss.add(s);
			}
		}
		return result;
		
	}
	/**
	 * removes the rightmost scoping term, i.e. the rightmost parts of the name before the first scoping separator
	 * @return
	 */
	public SymbolName removeLastScope() {
		List<SymbolName> ss = splitScope();
		ss.removeLast();
		return new SymbolName(getCase(), m_separator, ss);
		
	}
	public ScopeName getLastScope() {
		
	}
	/**
	 * adds a new level of scope to the end (right) of this ScopeName
	 * @param scope
	 * @return
	 */
	public ScopeName addScopeLevel(SymbolName scope) {
		ScopeName result = this;
		if(scope != null) {
			result = wrap(result.append(m_separator).append(scope), m_separator);
		}
		return result;
	}
	/**
	 * checks to see if this symbol name is a match for the specified name and list of imported scope
	 * @param separator - the character sequence that is used to separate the scope from the name
	 * @param imports - an array of scope names, any one of which might match the scope of this symbol name
	 * @param name - the local name that is being compared to this symbol name.
	 * @return
	 */
	public boolean isMatchWithScope(String separator, List<SymbolName> imports, SymbolName name) {
		boolean result = false;
		SymbolName thisScope = getScope();
		SymbolName thisName = deScope();
		SymbolName thatScope = name.getScope();
		SymbolName thatName = name.deScope();
		
		
		if(thisScope == EMPTY && thatScope == EMPTY) {
			if(thisName.equals(thatName)) {
				//an unscoped match
				result = true;
			}
		} else if(thisScope.equals(thatScope)) {
			if(thisName.equals(thatName)) {
			//	a perfect match
				result = true;
			}
		} else if(thatScope == EMPTY) {
			for(SymbolName i : imports) {
				if(i.equals(thisScope) && thisName.equals(thatName)){
					result = true;
					break;
				}
			}
		}
		return result;
	}
	/**
	 * Removes any elements that occur to the left of the specified separator. It also removes the separator.
	 * @param separator - the character sequence that is used to separate the scope from the name
	 * @return
	 */
	public ScopeName deScope( ) {
		ArrayList<String> result = new ArrayList<>();
		
		for(int i = m_name.length - 1; i >= 0; --i) {
			String s = m_name[i];
			if(s.equals(m_separator)) {
				break;
			} else {
				result.add(0, s);
			}
		}
		return wrap(make(result), m_separator);
	}


}

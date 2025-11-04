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
package com.bluerobotics.blueberry.schema.parser.fields;

import java.util.List;
import java.util.function.Consumer;

import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.types.TypeId;


/**
 * A class to represent both a data type and an instance of that type
 */
public interface Field extends ThingFromFile {
	/**
	 * This is the name of the type that this field consists
	 * @return
	 */
	ScopeName getTypeName();
	/**
	 * This is the type of field this is
	 * @return
	 */
	TypeId getTypeId();
	
	/**
	 * gets the number of bits taken up by this field
	 * @return
	 */
	int getBitCount();
	/**
	 * gets the number of bytes taken up by this field
	 * @return
	 */
	int getByteCount();
	/**
	 * gets the parent of this field. Don't know if this is necessary
	 * @return
	 */
	ParentField getParent();
	/**
	 * sets the parent of this field. Don't know if this is necessary
	 * @param p
	 */
	void setParent(ParentField p);
	/**
	 * replicates this field for use in another struct, message, etc.
	 * @param name
	 * @return
	 */
	Field makeInstance(SymbolName name);
	/**
	 * adds an annotation to this field.
	 * Only messages currently use this so far
	 * @param as
	 */
	void addAnnotation(List<Annotation> as);
	/**
	 * gets the annotation value for the specified name
	 * @param name
	 * @return
	 */
	Annotation getAnnotation(SymbolName name);
	
	/**
	 * scans through the annotations and applies the specified consumer to each
	 * @param c
	 */
	void scanAnnotations(Consumer<Annotation> c);
	/**
	 * Gets the byte index of this element within the message bytes
	 * If this is a single bit then it could represent the bit index within it's containing byte
	 * @return
	 */
	public int getIndex();
	/**
	 * Sets the byte index of this element within the message bytes
	 * If this is a single bit then it could represent the bit index within it's containing byte
	 * @param i
	 */
	public void setIndex(int i);
	/**
	 * gets the coordinate (essentially a reference to the location in the schema file that this field was defined 
	 * @return
	 */
	public Coord getCoord();
	
	/**
	 * gets the index for the location after this field
	 * @return
	 */
	public int getNextIndex();
	
	public boolean inScope(ScopeName s);


}
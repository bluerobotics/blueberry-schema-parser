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
package com.bluerobotics.blueberry.schema.parser.tokens;

/**
 *
 */
public class AnnotationToken extends SingleWordToken {
	private enum KnownAnnotation {
		 FILE_PATH("file_path"),
		 TOPIC("topic"),
		 MESSAGE_KEY("message_key"),
		 NAMESPACE("namespace"),
		 SERIALIZATION("serialization"),
		 REVISION("revision"),
		 ;
		private String name;
		private KnownAnnotation(String n) {
			name = n;
		}
		public String getName() {
			return name;
		}
	}
	public static final String FILE_PATH_ANNOTATION = "file_path";
	public static final String TOPIC_ANNOTATION = "topic";
	public static final String MESSAGE_KEY_ANNOTATION      = "message_key";
	public static final String NAMESPACE_ANNOTATION      = "namespace";
	public static final String SERIALIZATION_ANNOTATION      = "serialization";
	private final KnownAnnotation m_known;
	public AnnotationToken(SingleWordToken swt) {
		this(swt.getStart(), swt.getEnd(), swt.getName());

	}
	public AnnotationToken(Coord start, Coord end, String name) {
		super(start, end, name);
		KnownAnnotation ka = null;
		for(KnownAnnotation kaf : KnownAnnotation.values()) {
			if(kaf.getName().equals(getName())) {
				ka = kaf;
				break;
			}
		}
		m_known = ka;
	}
	public boolean isKnown() {
		return m_known != null;
	}
	public String toString() {
		if(isKnown()) {
			return getClass().getSimpleName()+"("+m_known+")";
		} else {
			return super.toString();
		}
	}
	public static AnnotationToken makeFilePathAnnotationToken(Coord c) {
		return new AnnotationToken(c, c, KnownAnnotation.FILE_PATH.getName());
	}

}

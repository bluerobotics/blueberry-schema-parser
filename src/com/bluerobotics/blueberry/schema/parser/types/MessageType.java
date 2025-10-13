package com.bluerobotics.blueberry.schema.parser.types;

import com.bluerobotics.blueberry.schema.parser.fields.FieldName;

public class MessageType extends ParentType {

	public MessageType(FieldName typeName, String comment) {
		super(TypeId.MESSAGE, typeName, comment);
	}

}

package com.bluerobotics.blueberry.schema.parser.types;

import com.bluerobotics.blueberry.schema.parser.fields.SymbolName;

public class MessageType extends ParentType {

	public MessageType(SymbolName typeName, String comment) {
		super(TypeId.MESSAGE, typeName, comment);
	}

}

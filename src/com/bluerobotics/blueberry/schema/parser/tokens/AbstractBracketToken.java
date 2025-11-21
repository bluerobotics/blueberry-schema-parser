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

import com.bluerobotics.blueberry.schema.parser.parsing.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.tokens.TokenConstants.TokenIdentifier;

/**
 * 
 */
public abstract class AbstractBracketToken extends GroupToken {
	public AbstractBracketToken(TokenList ts) {
		super(ts);
		if(!getOpenBracket().matches(ts.getFirst())){
			throw new SchemaParserException("Somehow the opening bracket was not found or the wrong type. Expected "+getOpenBracket()+" but got "+ts.getFirst(), ts.getFirst().getStart());
			
		} else if(!getCloseBracket().matches(ts.getLast())) {
			throw new SchemaParserException("Somehow the closing bracket was not found or the wrong type. Expected "+getCloseBracket()+" but got "+ts.getLast(), ts.getLast().getStart());
		}
		//now get rid of the brackets
		getChildren().remove(getChildren().getFirst());
		getChildren().remove(getChildren().getLast());

	}
	
	abstract protected TokenIdentifier getOpenBracket();
	abstract protected TokenIdentifier getCloseBracket();

	@Override
	public void addChild(Token ts) {
		int n = getChildrenCount();
		super.addChild(n - 1, ts);//add this child before the closing bracket
	}
	
	
	public static AbstractBracketToken makeBracket(TokenList ts) {
		Token t = ts.getFirst();
		
		if(t == null || !(t instanceof IdentifierToken)) {
			return null;
		}
		TokenIdentifier ti = ((IdentifierToken)t).getKeyword();
		AbstractBracketToken result = null;
		switch(ti) {

		case ANGLE_BRACKET_START:
			result = new AngleBracketToken(ts);
			break;

		case BRACE_START:
			result = new BraceToken(ts);
			break;

		case BRACKET_START:
			result = new RoundBracketToken(ts);
			break;

		case SQUARE_BRACKET_START:
			result = new SquareBracketToken(ts);
			break;

		default:
			break;
		
		}
		return result;
	}
	
	public static boolean isOpen(Token t) {
		if(t == null || !(t instanceof IdentifierToken)) {
			return false;
		}
		TokenIdentifier ti = ((IdentifierToken)t).getKeyword();
		boolean result = false;
		switch(ti) {

		case ANGLE_BRACKET_START:
		case BRACE_START:
		case BRACKET_START:
		case SQUARE_BRACKET_START:
			result = true;
			break;

		default:
			break;
		
		}
		return result;
	}
	/**
	 * given two tokens, returns true if the first is some sort of opening bracket and the other is the matching close bracket
	 * Does not check if the close token is the actual matching bracket, just that it is the right kind of closing bracket
	 * @param open - the open bracket candidate
	 * @param close - the close bracket candidate
	 * @return
	 */
	public static boolean isClose(Token open, Token close) {
		if(!isOpen(open)) {
			return false;
		}
		if(close == null || !(close instanceof IdentifierToken)){
			return false;
		}
		IdentifierToken tClose = (IdentifierToken)close;
		TokenIdentifier iOpen = ((IdentifierToken)open).getKeyword();
	
		TokenIdentifier iClose = null;
		switch(iOpen) {

		case ANGLE_BRACKET_START:
			iClose = TokenIdentifier.ANGLE_BRACKET_END;
			break;
		case BRACE_START:
			iClose = TokenIdentifier.BRACE_END;
			break;
		case BRACKET_START:
			iClose = TokenIdentifier.BRACKET_END;
			break;
		case SQUARE_BRACKET_START:
			iClose = TokenIdentifier.SQUARE_BRACKET_END;
			break;

		default:
			break;
		
		}
		return iClose == tClose.getKeyword();
	}


	
	
	
	
	
		
}

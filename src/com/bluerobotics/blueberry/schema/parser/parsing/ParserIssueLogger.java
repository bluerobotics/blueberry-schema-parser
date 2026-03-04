/**
 * 
 */
package com.bluerobotics.blueberry.schema.parser.parsing;

import java.util.ArrayList;

import com.bluerobotics.blueberry.schema.parser.gui.BlueberrySchemaParserGui.TextOutput;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;

/**
 * 
 */
public class ParserIssueLogger {
	private final TextOutput m_output;
	private boolean m_errorDetected = false;
	private final ArrayList<ParserIssue> m_issues = new ArrayList<>();

	
	public void clear() {
		m_issues.clear();
		m_errorDetected = false;

	}

	public ParserIssueLogger(TextOutput out) {
		m_output = out;
	}
	
	
	public void issueSkipped(String desc, Coord... locs) {
		logIssue(ParserIssue.skipped(desc, locs));
	}
	public void issueError(String desc, Coord... locs) {
		logIssue(ParserIssue.error(desc, locs));
	}
	public void issueWarning(String desc, Coord... locs) {
		logIssue(ParserIssue.warning(desc, locs));
	}
	public void issueNote(String desc, Coord... locs) {
		logIssue(ParserIssue.note(desc, locs));
	}
	
	
	private void logIssue(ParserIssue pi) {
		if(pi.getType() == ParserIssue.Type.ERROR) {
			m_errorDetected = true;
		}
		m_issues.add(pi);
		m_output.add(pi.toString(), pi.getType());
	}
	
	public boolean isError() {
		return m_errorDetected;
	}
}
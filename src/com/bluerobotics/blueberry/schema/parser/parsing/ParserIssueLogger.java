/**
 * 
 */
package com.bluerobotics.blueberry.schema.parser.parsing;

import java.util.ArrayList;

import com.bluerobotics.blueberry.schema.parser.gui.BlueberrySchemaParserGui.TextOutput;
import com.bluerobotics.blueberry.schema.parser.parsing.ParserIssue.Type;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;

/**
 * 
 */
public class ParserIssueLogger {
	private final TextOutput m_output;
	private boolean m_errorDetected = false;
	private int[] m_counts = new int[Type.values().length];

	private final ArrayList<ParserIssue> m_issues = new ArrayList<>();

	
	public void clear() {
		m_issues.clear();
		m_errorDetected = false;
		for(int i = 0; i < Type.values().length; ++i) {
			m_counts[i] = 0;
		}

	}

	public ParserIssueLogger(TextOutput out) {
		m_output = out;
	}
	
	
	public void issueSkipped(String desc, Coord... locs) {
		++m_counts[Type.SKIPPED.ordinal()];
		logIssue(ParserIssue.skipped(desc, locs));
	}
	public void issueError(String desc, Coord... locs) {
		++m_counts[Type.SKIPPED.ordinal()];
		logIssue(ParserIssue.error(desc, locs));
	}
	public void issueWarning(String desc, Coord... locs) {
		++m_counts[Type.SKIPPED.ordinal()];
		logIssue(ParserIssue.warning(desc, locs));
	}
	public void issueNote(String desc, Coord... locs) {
		++m_counts[Type.SKIPPED.ordinal()];
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
	public int getIssueCountOfType(Type t) {
		return m_counts[t.ordinal()];
	}
}
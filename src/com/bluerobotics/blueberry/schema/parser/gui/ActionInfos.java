/*
Copyright (c) 2024  Blue Robotics North Inc.

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
package com.bluerobotics.blueberry.schema.parser.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;

import com.bluerobotics.blueberry.schema.parser.parsing.Constants;
import com.starfishmedical.sfdq.actions.ActionInfo;




public enum ActionInfos implements ActionInfo, Constants {
	PARSE_SCHEMA("Parse", ICON_FONT, "\ue86c", "Parse the chosen schema file.", KeyEvent.VK_UNDEFINED, "ctrl alt P"),
	GENERATE_C("Generete C", UI_FONT, "C", "Generate C code in the specified location", KeyEvent.VK_UNDEFINED, "ctrl alt C"),
	GENERATE_JAVA("Generete Java", UI_FONT, "J", "Generate Java code in the specified location", KeyEvent.VK_UNDEFINED, "ctrl alt J"),
	CLEAN_SCHEMA("Clean Schema", ICON_FONT, "\uf1b2", "Generate a cleaned up version of the schema.", KeyEvent.VK_UNDEFINED, ""),
	HELP("Help", ICON_FONT, "\ue887", "Show application help.", KeyEvent.VK_UNDEFINED, "pressed F1"),//"ctrl H"),
	EXIT("Exit", ICON_FONT, "\ue5cd", "Exit the application", KeyEvent.VK_X, "ctrl shift X"),
	MINIMIZE("Minimize", ICON_FONT, "\ue931", "Hide the app window.", KeyEvent.VK_UNDEFINED, ""),
	NORMALIZE("Normalize", ICON_FONT, "\ue15b", "Make the app window not too small and not too big.", KeyEvent.VK_UNDEFINED, "shift alt N"),
	MAXIMIZE("Maximize",ICON_FONT, "\ue930", "Full-screen the app window.", KeyEvent.VK_UNDEFINED, "shift alt X"),
	NULL("",null, "An action was referenced that does not exist in the action list.",KeyEvent.VK_N),
	
	SETTINGS_LOAD("Load settings", "loadSettings.png", "Load previously saved settings.", KeyEvent.VK_L),
	SETTINGS_SAVE("Save settings", "saveSettings.png", "Save current application settings.", KeyEvent.VK_E),
	SETTINGS_SHOW("Show settings", "settings.png", "Show the settings dialog.", KeyEvent.VK_UNDEFINED),

	;
		
	


	private final String m_description;
	private final String m_hotKeyName;
	private final String m_iconName;
	private final String m_offIconName;
	private final int m_mnemonic;
	
	private final String m_name;
	private final Font m_iconFont;
	private final Color m_iconColor;
	
	private ActionInfos(String name, Font iconFont, String iconName, String description, int mnemonic, String hotKey){
		m_name = name;
		m_description = description;
		m_mnemonic = mnemonic;
		m_iconName = iconName;
		m_offIconName = null;
		m_hotKeyName = hotKey;
		m_iconFont = iconFont;
		m_iconColor = null;
//		setMnemonicIdeces(mnemonic);
	}
	//	
	private ActionInfos(String name, String iconName, String description, int mnemonic){
		m_name = name;
		m_description = description;
		m_mnemonic = mnemonic;
		m_iconName = iconName;
		m_offIconName = null;
		m_iconFont = null;
		m_iconColor = null;
		m_hotKeyName = null;
//		setMnemonicIdeces(mnemonic);
	}
	private ActionInfos(String name, String iconName, String description, int mnemonic, boolean  defaultState){
		m_name = name;
		m_description = description;
		m_mnemonic = mnemonic;
		m_offIconName = null;
		m_iconName = iconName;
		m_iconFont = null;
		m_iconColor = null;
		m_hotKeyName = null;

	}
	
	private ActionInfos(String name, String iconName, String description, int mnemonic, String hotKey){
		m_name = name;
		m_description = description;
		m_mnemonic = mnemonic;
		m_iconName = iconName;
		m_offIconName = null;
		m_hotKeyName = hotKey;
		m_iconFont = null;
		m_iconColor = null;
//		setMnemonicIdeces(mnemonic);
	}
	private ActionInfos(String name, String iconName, String offIconName, String description, int mnemonic, String hotKey){
		m_name = name;
		m_description = description;
		m_mnemonic = mnemonic;
		m_iconName = iconName;
		m_offIconName = offIconName;
		m_hotKeyName = hotKey;
		m_iconFont = null;
		m_iconColor = null;

//		setMnemonicIdeces(mnemonic);
	}

//	private void setMnemonicIdeces(int mnemonic){
//		String key = KeyEvent.getKeyText(mnemonic).toLowerCase();
//		m_selectedMnemonicIndex = m_name.toLowerCase().indexOf(key);
//		
//	}
	@Override
	public String getName(){		
		return m_name;
	}
	



	@Override
	public String getDescription() {
		return m_description;
	}
	@Override
	public String getHotKey() {
		return m_hotKeyName;
	}
	@Override
	public String getIconName() {
		return m_iconName;
	}
	@Override
	public String getOffIconName() {
		return m_offIconName;
	}
	@Override
	public int getMnemonic() {
		return m_mnemonic;
	}
	@Override
	public Font getIconFont() {
		// TODO Auto-generated method stub
		return m_iconFont;
	}
	@Override
	public Color getIconColor() {
		// TODO Auto-generated method stub
		return m_iconColor;
	}


	

}

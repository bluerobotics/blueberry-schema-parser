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

import javax.swing.Icon;

import com.bluerobotics.blueberry.schema.parser.parsing.Constants;
import com.starfishmedical.sfdq.actions.ActionInfo;
import com.starfishmedical.utils.UtilMethods;




public enum ActionInfos implements ActionInfo, Constants {
	


	PARSE_SCHEMA("Parse",          ICON_FONT, COLOR_LOGO_BLUE, "\ue86c", "Parse the chosen schema file.", KeyEvent.VK_UNDEFINED, "ctrl alt P"),
	GENERATE_C("Generete C",       UI_FONT,   COLOR_LOGO_BLUE, "C",      "Generate C code in the specified location", KeyEvent.VK_UNDEFINED, "ctrl alt C"),
	GENERATE_JAVA("Generete Java", UI_FONT,   COLOR_LOGO_BLUE, "J",      "Generate Java code in the specified location", KeyEvent.VK_UNDEFINED, "ctrl alt J"),
	CLEAN_SCHEMA("Clean Schema",   ICON_FONT, COLOR_LOGO_BLUE, "\uf1b2", "Generate a cleaned up version of the schema.", KeyEvent.VK_UNDEFINED, ""),
	HELP("Help",                   ICON_FONT, COLOR_LOGO_BLUE, "\ue887", "Show application help.", KeyEvent.VK_UNDEFINED, "pressed F1"),//"ctrl H"),
	EXIT("Exit",                   ICON_FONT, COLOR_LOGO_BLUE, "\ue5cd", "Exit the application", KeyEvent.VK_X, "ctrl shift X"),
	MINIMIZE("Minimize",           ICON_FONT, COLOR_LOGO_BLUE, "\ue931", "Hide the app window.", KeyEvent.VK_UNDEFINED, ""),
	NORMALIZE("Normalize",         ICON_FONT, COLOR_LOGO_BLUE, "\ue15b", "Make the app window not too small and not too big.", KeyEvent.VK_UNDEFINED, "shift alt N"),
	MAXIMIZE("Maximize",           ICON_FONT, COLOR_LOGO_BLUE, "\ue930", "Full-screen the app window.", KeyEvent.VK_UNDEFINED, "shift alt X"),
	COPY_ISSUES("Copy Issues",     ICON_FONT, COLOR_LOGO_BLUE, "\ue14d", "Copy issues to clipboard.", KeyEvent.VK_C, ""),
	
	SETTINGS_LOAD("Load settings", "loadSettings.png", "Load previously saved settings.", KeyEvent.VK_L),
	SETTINGS_SAVE("Save settings", "saveSettings.png", "Save current application settings.", KeyEvent.VK_E),
	SETTINGS_SHOW("Show settings", ICON_FONT, COLOR_LOGO_BLUE, "\ue930", "Show the settings dialog.", KeyEvent.VK_UNDEFINED, ""),
	
	NULL("",null, "An action was referenced that does not exist in the action list.",KeyEvent.VK_N),
	
	TOOLS_RESET_KEYS("Reset Keys", ICON_FONT, COLOR_LOGO_BLUE, "\ueb84", "Reset all message and module keys.", KeyEvent.VK_UNDEFINED, ""),
	;
		

	private final String m_description;
	private final String m_hotKeyName;
	private final String m_iconName;
	private final String m_offIconName;
	private final Font m_iconFont;
	private final int m_mnemonic;
	private final String m_name;
	private final Color m_iconColor;
	
	private ActionInfos(String name, Font iconFont, Color iconColor, String iconText, String description, int mnemonic, String hotKey){
		m_name = name;
		m_description = description;
		m_mnemonic = mnemonic;
		m_iconName = iconText;
		m_iconFont = iconFont;
		m_offIconName = null;
		m_hotKeyName = hotKey;
		m_iconColor = iconColor;
//		setMnemonicIdeces(mnemonic);
	}
	
	private ActionInfos(String name, String iconName, String description, int mnemonic){
		m_name = name;
		m_description = description;
		m_mnemonic = mnemonic;
		m_iconName = iconName;
		m_iconFont = null;
		m_iconColor = null;
		m_offIconName = null;
		m_hotKeyName = "";

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
		m_hotKeyName = "";
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
	public Font getIconFont() {
		return m_iconFont;
	}
	@Override
	public Color getIconColor() {
		return m_iconColor;
	}
	@Override
	public String getOffIconName() {
		return m_offIconName;
	}
	@Override
	public int getMnemonic() {
		return m_mnemonic;
	}


	

}

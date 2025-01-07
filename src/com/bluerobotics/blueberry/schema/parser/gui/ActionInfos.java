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
package com.bluerobotics.blueberry.schema.parser.gui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import com.starfishmedical.sfdq.actions.ActionInfo;




public enum ActionInfos implements ActionInfo {
	PARSE_SCHEMA("Parse", "parse.png", "Parse the chosen schema file.", KeyEvent.VK_UNDEFINED, "ctrl alt P"),
	GENERATE_C("Generete C", "generateC.png", "Generate C code in the specified location", KeyEvent.VK_UNDEFINED, "ctrl alt C"),
	GENERATE_JAVA("Generete Java", "generateJ.png", "Generate Java code in the specified location", KeyEvent.VK_UNDEFINED, "ctrl alt J"),
	CLEAN_SCHEMA("Clean Schema", "", "Generate a cleaned up version of the schema.", KeyEvent.VK_UNDEFINED, ""),
	HELP("Help", "help.png", "Show application help.", KeyEvent.VK_UNDEFINED, "pressed F1"),//"ctrl H"),
	EXIT("Exit", "exit.png", "Exit the application", KeyEvent.VK_X, "ctrl shift X"),
	MINIMIZE("Minimize", "minimizeWindow.png", "Hide the app window.", KeyEvent.VK_UNDEFINED, ""),
	NORMALIZE("Normalize", "normalizeWindow.png", "Make the app window not too small and not too big.", KeyEvent.VK_UNDEFINED, "shift alt N"),
	MAXIMIZE("Maximize", "maximizeWindow.png", "Full-screen the app window.", KeyEvent.VK_UNDEFINED, "shift alt X"),
	NULL("",null, "An action was referenced that does not exist in the action list.",KeyEvent.VK_N),
	
	SETTINGS_LOAD("Load settings", "loadSettings.png", "Load previously saved settings.", KeyEvent.VK_L),
	SETTINGS_SAVE("Save settings", "saveSettings.png", "Save current application settings.", KeyEvent.VK_E),
	SETTINGS_SHOW("Show settings", "settings.png", "Show the settings dialog.", KeyEvent.VK_UNDEFINED),

	;
		

	private String m_description;
	private String m_hotKeyName;
	private String m_iconName;
	private String m_offIconName;
	private int m_mnemonic;
	
	private String m_name;
	//	
	private ActionInfos(String name, String iconName, String description, int mnemonic){
		m_name = name;
		m_description = description;
		m_mnemonic = mnemonic;
		m_iconName = iconName;
		m_offIconName = null;
//		setMnemonicIdeces(mnemonic);
	}
	private ActionInfos(String name, String iconName, String description, int mnemonic, boolean  defaultState){
		m_name = name;
		m_description = description;
		m_mnemonic = mnemonic;
		m_offIconName = null;
		m_iconName = iconName;
	}
	
	private ActionInfos(String name, String iconName, String description, int mnemonic, String hotKey){
		m_name = name;
		m_description = description;
		m_mnemonic = mnemonic;
		m_iconName = iconName;
		m_offIconName = null;
		m_hotKeyName = hotKey;
//		setMnemonicIdeces(mnemonic);
	}
	private ActionInfos(String name, String iconName, String offIconName, String description, int mnemonic, String hotKey){
		m_name = name;
		m_description = description;
		m_mnemonic = mnemonic;
		m_iconName = iconName;
		m_offIconName = offIconName;
		m_hotKeyName = hotKey;

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


	

}

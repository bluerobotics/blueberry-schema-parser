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
package com.bluerobotics.blueberry.schema.parser.parsing;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.starfishmedical.utils.ResourceTools;
import com.starfishmedical.utils.UtilMethods;

public interface Constants {
	public static final String APP_NAME = "Blueberry Schema Parser";
	public static final String VERSION = "0.0.0";// Don't forget to change this in the POM too!

	public static final File HOME_DIR = new File(System.getProperty("user.dir"));
	public static final String RESOURCE_PATH = "com/bluerobotics/blueberry/schema/parser/resources/";
	public static final BufferedImage BLUEBERRY_LOGO = ResourceTools.loadImage(RESOURCE_PATH + "Project Blueberry Logo.png");
	public static final Icon BLUEBERRY_ICON = new ImageIcon(ResourceTools.loadImage(RESOURCE_PATH + "Project Blueberry Logo66x48.png"));
	public static final Font ICON_FONT = ResourceTools.loadFont(RESOURCE_PATH + "MaterialSymbolsRounded-VariableFont_FILL,GRAD,opsz,wght.ttf", 18.0f, Font.BOLD);
	public static final Font UI_FONT = ResourceTools.loadFont(RESOURCE_PATH + "Roboto-Regular.ttf", 18.0f);
	public static final Color COLOR_LOGO_DARK_GREEN = new Color(0x43994f);
	public static final Color COLOR_LOGO_LIGHT_GREEN = new Color(0x4eb277);
	public static final Color COLOR_LOGO_BLUE = new Color(0x4a99d3);
	public static final Color COLOR_LOGO_LILAC = new Color(0x4e68b2);
	public static final Icon EXIT_ICON =          UtilMethods.makeIconFromFont(ICON_FONT, "\ue5cd", COLOR_LOGO_BLUE, -1, 32); 
	public static final Icon TO_JAVA_ICON =       UtilMethods.makeIconFromFont(ICON_FONT, "\ue5cd", COLOR_LOGO_BLUE, -1, 32); 
	public static final Icon TO_C_ICON =          UtilMethods.makeIconFromFont(ICON_FONT, "\ue5cd", COLOR_LOGO_BLUE, -1, 32); 
	public static final Icon MINIMIZE_ICON =      UtilMethods.makeIconFromFont(ICON_FONT, "\ue931", COLOR_LOGO_BLUE, -1, 32);
	public static final Icon NORMALIZE_ICON =     UtilMethods.makeIconFromFont(ICON_FONT, "\ue15b", COLOR_LOGO_BLUE, -1, 32); 
	public static final Icon MAXIMIZE_ICON =      UtilMethods.makeIconFromFont(ICON_FONT, "\ue930", COLOR_LOGO_BLUE, -1, 32); 
	public static final Icon GENERATE_C_ICON =    UtilMethods.makeIconFromFont(UI_FONT,        "C", COLOR_LOGO_BLUE, 32, 32);
	public static final Icon GENERATE_JAVA_ICON = UtilMethods.makeIconFromFont(UI_FONT,        "J", COLOR_LOGO_BLUE, 32, 32);
	public static final Icon WASHING_MACHINE_ICON = UtilMethods.makeIconFromFont(ICON_FONT,"\ue54a", COLOR_LOGO_BLUE, -1, 32);
	public static final Icon SOAP_ICON =            UtilMethods.makeIconFromFont(ICON_FONT, "\uf1b2", COLOR_LOGO_BLUE, -1, 32);	
	public static final Icon CHECK_ICON =           UtilMethods.makeIconFromFont(ICON_FONT, "\ue86c", COLOR_LOGO_BLUE, -1, 32);
	
}

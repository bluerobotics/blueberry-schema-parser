package com.bluerobotics.blueberry.schema.parser.app;

import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.starfishmedical.settings.Settings;
import com.starfishmedical.sfdq.settings.Key;
import com.starfishmedical.utils.ResourceTools;

public class BlueberrySchemaParserGui implements Constants {
	private final Settings m_settings;
	private final JFrame m_frame;
	public BlueberrySchemaParserGui(Settings s) {
		m_settings = s;
		JFrame f = new JFrame();
		f.setIconImage(BLUEBERRY_LOGO);
		m_frame = f;
		Container cp = f.getContentPane();
		JPanel p = new JPanel();
		
		int x = s.getInt(Key.APP_POS_X);
		int y = s.getInt(Key.APP_POS_Y);
		int w = s.getInt(Key.APP_WIDTH);
		int h = s.getInt(Key.APP_HEIGHT);
		f.setLocation(x, y);
		f.setSize(w, h);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		f.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				exit();
			}
		});
		f.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int w = e.getComponent().getWidth();
				int h = e.getComponent().getHeight();
				w = w < 200 ? 200 : w;
				h  = h < 200 ? 200 : h;
				s.set(Key.APP_WIDTH, w);
				s.set(Key.APP_HEIGHT, h);
			}
			@Override
			public void componentMoved(ComponentEvent e) {
				int x = e.getComponent().getX();
				int y = e.getComponent().getY();
				s.set(Key.APP_POS_X, x);
				s.set(Key.APP_POS_Y, y);
			}
		});

	}

	public static void main(String...args){
		Settings settings = new Settings(BlueberrySchemaParserGui.class);
		ResourceTools.setFonts();
		
		BlueberrySchemaParserGui gui = new BlueberrySchemaParserGui(settings);
	
	
	}	
}

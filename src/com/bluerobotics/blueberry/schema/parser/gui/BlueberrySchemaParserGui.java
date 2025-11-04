package com.bluerobotics.blueberry.schema.parser.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.bluerobotics.blueberry.schema.parser.parsing.BlueberrySchemaParser;
import com.bluerobotics.blueberry.schema.parser.parsing.Constants;
import com.bluerobotics.blueberry.schema.parser.parsing.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.writers.CWriter;
import com.bluerobotics.blueberry.schema.parser.writers.JavaWriter;
import com.bluerobotics.blueberry.schema.parser.writers.PrettyWriter;
import com.starfishmedical.settings.Settings;
import com.starfishmedical.settings.SettingsDialog;
import com.starfishmedical.settings.SettingsTableCellEditor;
import com.starfishmedical.settings.SettingsTableCellRenderer;
import com.starfishmedical.settings.SettingsTableModel;
import com.starfishmedical.sfdq.actions.ActionManager;
import com.starfishmedical.sfdq.gui.FrameResizer;
import com.starfishmedical.sfdq.gui.ToolBar;
import com.starfishmedical.utils.ResourceTools;
import com.starfishmedical.utils.UtilMethods;

/**
 * This is a main GUI class. It starts up a simple dialog for running the parser
 */
public class BlueberrySchemaParserGui implements Constants {
	private final Settings m_settings;
	private final JFrame m_frame;
//	private MenuBar m_menuBar;
	private BlueberrySchemaParser m_parser = new BlueberrySchemaParser();

	private final ActionManager m_actions = new ActionManager(RESOURCE_PATH);
	private final JTextArea m_text = new JTextArea();
	
	/**
	 * 
	 * Constructs the GUI, maps actions
	 * @param s - a settings object for persistence
	 */
	public BlueberrySchemaParserGui(Settings s) {
		m_settings = s;
		JFrame f = new JFrame();
		f.setIconImage(BLUEBERRY_LOGO);
		m_frame = f;
		f.setUndecorated(true);

		Container cp = f.getContentPane();
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createLineBorder(COLOR_LOGO_LILAC, 3));
//		p.setBorder(BorderFactory.createRaisedBevelBorder());
		FrameResizer resizer = new FrameResizer(p, m_frame);
		cp.add(p);
		cp = p;

		cp.setLayout(new BorderLayout());
		
		

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
		registerActions();


		ToolBar toolbar = new ToolBar() {
			private static final long serialVersionUID = 1L;

			//add initializer to abstract class
			{
				JButton comp = new JButton("<html><p>"+ APP_NAME + "</p>"+VERSION+"</p></html>");
//				comp.setIcon(new ImageIcon(BLUEBERRY_LOGO.getScaledInstance(-1, 49, Image.SCALE_SMOOTH)));
				comp.setIcon(UtilMethods.makeIcon(BLUEBERRY_LOGO, -1, 48));
				comp.setAlignmentY(0.5f);
				comp.setVerticalAlignment(SwingConstants.CENTER);
//				comp.setIcon(BLUEBERRY_ICON);
//				comp.setIcon(UtilMethods.makeIcon(BLUEBERRY_LOGO, 48));
				comp.setRequestFocusEnabled(false);//this stops buttons from taking focus
				comp.setBackground(Color.BLUE);//don't know why this works, but it keeps the rollover from showing a weird gradient
				comp.setBorder(new EmptyBorder(5,5,5,5));
				comp.setOpaque(false);
				Dimension d = new Dimension(300,50);
//				comp.setPreferredSize(d);
				comp.setMaximumSize(d);
				comp.setFocusable(false);
				add(comp);
				setFloatable(false);

				addItem(m_actions, ActionInfos.PARSE_SCHEMA);
				addItem(m_actions, ActionInfos.GENERATE_C);
				addItem(m_actions, ActionInfos.GENERATE_JAVA);
				addItem(m_actions, ActionInfos.CLEAN_SCHEMA);

				addSeparator(new Dimension(20,20));
//				addItem( m_actions, ActionInfos.HELP);

				addGlue();
				addItem(m_actions, ActionInfos.MINIMIZE);
				addItem(m_actions, ActionInfos.NORMALIZE);
				addItem(m_actions, ActionInfos.MAXIMIZE);
				addItem(m_actions, ActionInfos.EXIT);

			}
		};

		resizer.addMoveComponent(toolbar);
		cp.add(toolbar, BorderLayout.NORTH);
		Key[] keys = new Key[] {Key.SCHEMA_DIRECTORY,
				Key.JAVA_DIRECTORY,
				Key.JAVA_PACKAGE_NAME,
				Key.C_DIRECTORY,
				Key.CODE_HEADER_FILE_PATH,
				Key.IDL_HEADER_FILE_PATH,
		};

		JTable setTable = new JTable(new SettingsTableModel(m_settings, keys));

		setTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setTable.setRowHeight((int)(setTable.getFont().getSize()*1.5));
		setTable.getColumnModel().getColumn(1).setCellEditor(new SettingsTableCellEditor(m_settings));
		setTable.getColumnModel().getColumn(1).setCellRenderer(new SettingsTableCellRenderer(m_settings));
//		settings.setDefaultEditor(File.class, new SettingsTableFileCellEditor());
//		settings.setDefaultEditor(SerialPort.class, new SettingsTableSerialPortCellEditor());
		Box b = Box.createVerticalBox();
		b.add(new JScrollPane(setTable));
		m_text.setPreferredSize(new Dimension(500,500));
		b.add(new JScrollPane(m_text));
		cp.add(b);

		f.setVisible(true);

	}
	private Settings getSettings() {
		return m_settings;
	}
	public JFrame getFrame(){
		return m_frame;
	}

	private void registerActions() {
		m_actions.registerActions(ActionInfos.values());
		m_actions.addKeyBindings(m_frame.getRootPane());

		m_actions.addListener(ActionInfos.HELP, e -> {});
		m_actions.addListener(ActionInfos.EXIT, e -> exit());
		m_actions.addListener(ActionInfos.MINIMIZE, e -> m_frame.setExtendedState(Frame.ICONIFIED));
		m_actions.addListener(ActionInfos.NORMALIZE, e -> m_frame.setExtendedState(Frame.NORMAL));
		m_actions.addListener(ActionInfos.MAXIMIZE, e -> m_frame.setExtendedState(Frame.MAXIMIZED_BOTH));

		m_actions.addListener(ActionInfos.SETTINGS_LOAD, e -> getSettings().loadSettings(getFrame(), Key.DEFAULT_FILE_PATH));
		m_actions.addListener(ActionInfos.SETTINGS_SAVE, e -> getSettings().saveSettings(getFrame(), Key.DEFAULT_FILE_PATH));
		m_actions.addListener(ActionInfos.SETTINGS_SHOW, e -> SettingsDialog.showSettingsDialog(getFrame(), getSettings(), Key.values()));
		m_actions.addListener(ActionInfos.PARSE_SCHEMA, e -> parse());
		m_actions.addListener(ActionInfos.GENERATE_C, e -> generateC());
		m_actions.addListener(ActionInfos.GENERATE_JAVA, e -> generateJava());
		m_actions.addListener(ActionInfos.CLEAN_SCHEMA, e -> generatePretty());



		m_actions.getAction(ActionInfos.EXIT).setIcon(EXIT_ICON);
		m_actions.getAction(ActionInfos.MINIMIZE).setIcon(MINIMIZE_ICON);
		m_actions.getAction(ActionInfos.NORMALIZE).setIcon(NORMALIZE_ICON);

		m_actions.getAction(ActionInfos.MAXIMIZE).setIcon(MAXIMIZE_ICON);
		m_actions.getAction(ActionInfos.GENERATE_C).setIcon(GENERATE_C_ICON);
		m_actions.getAction(ActionInfos.GENERATE_JAVA).setIcon(GENERATE_JAVA_ICON);
		m_actions.getAction(ActionInfos.CLEAN_SCHEMA).setIcon(SOAP_ICON);
		m_actions.getAction(ActionInfos.PARSE_SCHEMA).setIcon(CHECK_ICON);


	}

	private void generateJava() {
		File dir = m_settings.getFile(Key.JAVA_DIRECTORY);
		m_text.append("Generating Java code in \"" + dir+"\"\n");

		if(m_parser.getMessages().size() == 0) {
			parse();
		}

		String h = readHeader(m_settings.getUri(Key.CODE_HEADER_FILE_PATH));
		JavaWriter w = new JavaWriter(dir, m_parser, h);
		w.write();
		m_text.append("Done");
	}

	private void generateC() {
		File dir = m_settings.getFile(Key.C_DIRECTORY);
		m_text.append("Generating C code in \"" + dir+"\"\n");

		if(m_parser.getMessages().size() == 0) {
			parse();
		}

		String h = readHeader(m_settings.getUri(Key.CODE_HEADER_FILE_PATH));
		CWriter w = new CWriter(dir, m_parser, h);
		w.write();
		m_text.append("Done");
	}
	private String readHeader(URI uri) {
		String header = "";
		
		Path p = Path.of(uri);
		if(!Files.isRegularFile(p)) {
			System.out.println("Code header is not a file: "+p);
			
		} else {
			try {
				header = Files.readString(p);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return header;
			
	}
	private void generatePretty() {
		File dir = m_settings.getFile(Key.SCHEMA_DIRECTORY);
		m_text.append("Generating Beautified Schema in \"" + dir+"\"\n");

//		if(m_parser.getTopLevelField() == null) {
//			parse();
//		}

		String h = readHeader(m_settings.getUri(Key.IDL_HEADER_FILE_PATH));
		PrettyWriter pw = new PrettyWriter(dir, m_parser, h);
//		pw.write(m_parser.getTopLevelField(), m_parser.getHeader());
		m_text.append("Done");
	}
	private void parse() {

		File dir = m_settings.getFile(Key.SCHEMA_DIRECTORY);
		try {
			m_parser.clear();
			loadFiles(dir, dir);
		
			m_parser.parse();
		} catch(SchemaParserException e) {
			m_text.append(e.toString());
		}
	}
	private void loadFiles(File root, File f) throws SchemaParserException {
		m_text.append("Parsing \""+f+"\"\n");
		if(f.isDirectory()) {
			File[] fs = f.listFiles();
			for(File cf : fs) {
				loadFiles(root, cf);
			}
		} else if(f.isFile()) {
			BufferedReader br;
			String[] ss = null;
			try {
				br = new BufferedReader(new FileReader(f));
				Stream<String> lines = br.lines();
				ss = lines.toArray(String[]::new);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				m_text.append(e.toString());
			}

			if(ss != null) {
				
				Path p = root.toPath().relativize(f.toPath());
				m_parser.append(p.toString(), ss);
				
			}
			m_text.append("Done\n");
		}
	}

	public static void main(String...args){
		Settings settings = new Settings(BlueberrySchemaParserGui.class);
		ResourceTools.setFonts();

		BlueberrySchemaParserGui gui = new BlueberrySchemaParserGui(settings);


	}
	public void exit() {

		System.exit(0);


	}
}

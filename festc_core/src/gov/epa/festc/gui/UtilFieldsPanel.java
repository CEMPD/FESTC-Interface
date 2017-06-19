package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.SpringLayoutGenerator;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class UtilFieldsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5055392954069778413L;
	protected JFormattedTextField rows;
	protected JFormattedTextField cols;
	protected JFormattedTextField xmin;
	protected JFormattedTextField ymin;
	protected JFormattedTextField xSize;
	protected JFormattedTextField ySize;
	protected JTextField proj4proj;
	protected JTextField gridName;
	protected JTextField scenarioDir;
	protected JPanel scenarioDirP = new JPanel();;
	protected JComboBox fertYearSel;
	protected DomainFields domain;
	
	protected FestcApplication app;
	protected JTextArea runMessages;

	protected String outMessages = "Job messages: \n";
	protected String ls = "\n";
	
	public UtilFieldsPanel() {
		//no op
	}
	
	public UtilFieldsPanel(SpringLayout springLayout) {
		super(springLayout);
	}

	protected void init(){
//		JPanel panel = new JPanel();
//		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		proj4proj = new JTextField(32);
		//proj4proj.setToolTipText("Use full description, ie. +proj=lcc +a=6370000.0 +b=6370000.0 +lat_1=33 +lat_2=45 +lat_0=40 +lon_0=-97");
		
		scenarioDir = new JTextField(40);
		scenarioDirP.add(scenarioDir);
		rows = new JFormattedTextField(NumberFormat.getNumberInstance());
		rows.setColumns(3);
		//rows.setValue(299);
		cols = new JFormattedTextField(NumberFormat.getNumberInstance());
		cols.setColumns(3);
		//cols.setValue(459);
		
		xmin = new JFormattedTextField(NumberFormat.getNumberInstance());
		xmin.setColumns(9);
		//xmin.setValue(-2556000.000);
		ymin = new JFormattedTextField(NumberFormat.getNumberInstance());
		ymin.setColumns(9);
		//ymin.setValue(-1728000.000);
		
		
		//JPanel cellSizePanel = new JPanel(new SpringLayout());
		//SpringLayoutGenerator cellSizeLayout = new SpringLayoutGenerator();
		xSize = new JFormattedTextField(NumberFormat.getNumberInstance());
		xSize.setColumns(8);
		//xSize.setValue(12000);
		ySize = new JFormattedTextField(NumberFormat.getNumberInstance());
		ySize.setColumns(8);
		//ySize.setValue(12000);
		
		//JPanel projPanel = new JPanel(new SpringLayout());
		//SpringLayoutGenerator projLayout = new SpringLayoutGenerator();
		//sgridName = new JTextField(30);
		
	}
	
	protected JPanel messageBox(){
		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		this.runMessages = new JTextArea("", 10, 60);
		runMessages.setLineWrap(true);
		JScrollPane messageScroll = new JScrollPane(runMessages, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		layout.addLabelWidgetPair("Message Box: ", messageScroll, panel);
		layout.makeCompactGrid(panel, 1, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading
		return panel;
	}	
	
	protected JComponent getGridDescPanel(boolean isNew) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JPanel gridNamePanel = new JPanel(new SpringLayout());
		SpringLayoutGenerator gridNameLayout = new SpringLayoutGenerator();
		gridName = new JTextField(32);
		//gridName.setToolTipText("Grid Name");		 
		gridNameLayout.addLabelWidgetPair("Grid Name:", gridName, gridNamePanel);
		gridNameLayout.makeCompactGrid(gridNamePanel, 1, 2, 5, 5, 5, 5);
		
		JPanel colRowPanel = new JPanel(new SpringLayout());
		SpringLayoutGenerator colRowLayout = new SpringLayoutGenerator();
		
		colRowLayout.addLabelWidgetPair("Rows:", rows, colRowPanel);
		colRowLayout.addLabelWidgetPair("Columns:", cols, colRowPanel);
		colRowLayout.makeCompactGrid(colRowPanel, 2, 2, 5, 5, 5, 5);
		
		JPanel xyMinPanel = new JPanel(new SpringLayout());
		SpringLayoutGenerator xyMinLayout = new SpringLayoutGenerator();
		
		xyMinLayout.addLabelWidgetPair("XMin:", xmin, xyMinPanel);
		xyMinLayout.addLabelWidgetPair("YMin:", ymin, xyMinPanel);
		xyMinLayout.makeCompactGrid(xyMinPanel, 2, 2, 5, 5, 5, 5);
		
		JPanel cellSizePanel = new JPanel(new SpringLayout());
		SpringLayoutGenerator cellSizeLayout = new SpringLayoutGenerator();
		
		cellSizeLayout.addLabelWidgetPair("XCellSize:", xSize, cellSizePanel);
		cellSizeLayout.addLabelWidgetPair("YCellSize:", ySize, cellSizePanel);
		cellSizeLayout.makeCompactGrid(cellSizePanel, 2, 2, 5, 5, 5, 5);
		
		panel.add(colRowPanel);
		panel.add(cellSizePanel);
		panel.add(xyMinPanel);
		
		JPanel projPanel = new JPanel(new SpringLayout());
		SpringLayoutGenerator projLayout = new SpringLayoutGenerator();
		
		proj4proj.setToolTipText("Use full description, ie. +proj=lcc +a=6370000.0 +b=6370000.0 +lat_1=33 +lat_2=45 +lat_0=40 +lon_0=-97");
		if (isNew )
			proj4proj.setText("+proj=lcc +a=6370000.0 +b=6370000.0 +lat_1=33 +lat_2=45 +lat_0=40 +lon_0=-97");
		projLayout.addLabelWidgetPair("Proj4Projection:", proj4proj, projPanel);
		
		projLayout.makeCompactGrid(projPanel, 1, 2, 5, 5, 5, 5);
		
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.add(panel);
		container.add(projPanel);
		container.add(gridNamePanel);
		
		return container;
	}
	
	protected JPanel scenPanel() {
	 
		
        JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		
		
		this.scenarioDir = new JTextField(40);
		 

		layout.addLabelWidgetPair(Constants.LABEL_EPIC_SCENARIO, scenarioDir, panel);
		
		layout.makeCompactGrid(panel, 1, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading
		return panel;
	}
	
	protected JPanel fertYearPanel() {
	 
		
        JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		
		JPanel bPanel = new JPanel(); 
		 
		
	    fertYearSel = new JComboBox(Constants.FERTYEARS);
	    fertYearSel.setSelectedIndex(1);
	    bPanel.add(fertYearSel);
//	    bPanel.add(new JLabel("               "));
//	    bPanel.add(runTiledrain);    
		
		layout.addLabelWidgetPair("Fertilizer Year: ", bPanel, panel);
		
		layout.makeCompactGrid(panel, 1, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading
		return panel;
	}
	
	protected void validateScen(String scenDir) throws Exception { 
		if ( scenDir == null || scenDir.isEmpty()) 
			throw new Exception("Scenario dir is empty!");
		if (scenDir.trim().contains(" ") )
			throw new Exception("Scenario dir has space in between.");
	}
	
	protected void validateGrids() throws Exception  {
		String gridN = gridName.getText();
		if (gridN == null || gridN.trim().isEmpty())
			throw new Exception("Grid name field is empty.");
		if (gridN.trim().length() > 16)
			throw new Exception(" New grid name is too long (larger than 16 chars).");
		if (gridN.trim().contains(" ") )
			throw new Exception(" New grid name has space in between.");
		
		String proj = proj4proj.getText();
		if (proj == null || proj.trim().isEmpty())
			throw new Exception("Projection field is empty.");
		
		String rowValue = rows.getValue() == null ? "": rows.getValue()+"";
		if (rowValue.trim().isEmpty() || rowValue.trim().charAt(0) == '0')
			throw new Exception("Rows value is invalid.");
		
		String colValue = cols.getValue() == null ? "": cols.getValue()+"";
		if (colValue.trim().isEmpty() || colValue.trim().charAt(0) == '0')
			throw new Exception("Cols value is invalid.");
		
		
		String xminValue = xmin.getValue() == null ? "": xmin.getValue()+"";
		if (xminValue.trim().isEmpty() )
			throw new Exception("XMin value is invalid.");
		
		String yminValue = ymin.getValue() == null ? "": ymin.getValue()+"";
		if (yminValue.trim().isEmpty() )
			throw new Exception("YMin value is invalid.");
		
		String xSizeValue = xSize.getValue() == null ? "": xSize.getValue()+"";
		if (xSizeValue.trim().isEmpty() || xSizeValue.trim().charAt(0) == '0')
			throw new Exception("XCellSize value is invalid.");
		
		String ySizeValue = ySize.getValue() == null ? "": ySize.getValue()+"";
		if (ySizeValue.trim().isEmpty() || ySizeValue.trim().charAt(0) == '0')
			throw new Exception("YCellSize value is invalid.");
	}

//	protected  void setFalseEditable(){
//		rows.setEditable(false);
//		cols.setEditable(false);;
//		xmin.setEditable(false);;
//		ymin.setEditable(false);;
//		xSize.setEditable(false);;
//		ySize.setEditable(false);;
//		//proj4proj.setEditable(false);;
//		gridName.setEditable(false);;
//		 
//		scenarioDir.setEditable(false);;
//		runMessages.setEditable(false);;
//		
//	}

//	protected Action browseDirAction(final String name, final JTextField text) {
//		return new AbstractAction("Browse...") {
//			private static final long serialVersionUID = 482845697751457179L;
//
//			public void actionPerformed(ActionEvent e) {
//				JFileChooser chooser;
//				File file = new File(text.getText());
//
//				if (file != null && file.isFile()) {
//					chooser = new JFileChooser(file.getParentFile());
//				} else if (file != null && file.isDirectory()) {
//					chooser = new JFileChooser(file);
//				} else
//					chooser = new JFileChooser(app.getCurrentDir());
//
//				chooser.setDialogTitle("Please select the " + name);
//				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//
//				int option = chooser.showDialog(UtilFieldsPanel.this,
//						"Select");
//				if (option == JFileChooser.APPROVE_OPTION) {
//					File selected = chooser.getSelectedFile();
//					text.setText("" + selected);
//					app.setCurrentDir(selected);
//				}
//			}
//		};
//	}
}



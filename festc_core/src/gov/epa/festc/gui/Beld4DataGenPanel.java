package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.Beld4DataGenFields;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.Epic2CMAQFields;
import gov.epa.festc.core.proj.EpicYearlyAverage2CMAQFields;
import gov.epa.festc.core.proj.ManageSpinupFields;
import gov.epa.festc.core.proj.Mcip2EpicFields;
import gov.epa.festc.core.proj.SiteFilesFields;
import gov.epa.festc.core.proj.SiteInfoGenFields;
import gov.epa.festc.util.BrowseAction;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.FileRunner;
import gov.epa.festc.util.SpringLayoutGenerator;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import simphony.util.messages.MessageCenter;

public class Beld4DataGenPanel extends UtilFieldsPanel implements PlotEventListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1403169880186710184L;

	private MessageCenter msg;
	private FestcApplication app;
	private Beld4DataGenFields fields;
	private JCheckBox nlcdBox; 
	private JCheckBox modisBox;
	private JTextField nlcdYear;
	private JTextField inputDir;
	private JButton inputDirBrowser;
	
	public Beld4DataGenPanel(FestcApplication festcApp) {
		app = festcApp;
		msg = app.getMessageCenter();
		fields = new Beld4DataGenFields();
		app.getProject().addPage(fields);
		app.addPlotListener(this);
		add(createPanel());
	}
	
	private JPanel createPanel() {
		JPanel main = new JPanel();
		try{
			init();
			main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
			main.add(getNorthPanel());
			main.add(getCenterPanel());
			main.add(getSouthPanel());
			main.add(messageBox());
			 
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return main;

	}
	
	private JPanel getNorthPanel() {
		JPanel panel = new JPanel();
		JLabel title = new JLabel(Constants.BELD4_GEN, SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

		return panel;
	}
	
	private JPanel getCenterPanel() {
		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		
		JPanel dataPanel = new JPanel();
		this.nlcdBox = new JCheckBox("NLCD ", true); 
		this.modisBox = new JCheckBox("MODIS ", true); 
		dataPanel.add(this.nlcdBox);
		dataPanel.add(this.modisBox);
		
		JPanel yearPanel = new JPanel( );
		nlcdYear = new JTextField(40);
		nlcdYear.setEditable(false);
		yearPanel.add(nlcdYear);
		
		JPanel inputDirPanel = new JPanel();
		inputDir = new JTextField(40);
		inputDir.setToolTipText("I.E. ../data/nlcd_modis_files_2006.txt");
		inputDirBrowser = new JButton(BrowseAction.browseAction(this, app.getCurrentDir(), "input file", inputDir));
		inputDirPanel.add(inputDir);
		inputDirPanel.add(inputDirBrowser);	

		layout.addLabelWidgetPair("Grid Description: ", getGridDescPanel(false), panel);
		layout.addLabelWidgetPair(Constants.LABEL_EPIC_SCENARIO, scenarioDir, panel);
		layout.addLabelWidgetPair("NLCD/MODIS Data Year:", yearPanel, panel);
		layout.addLabelWidgetPair("NLCD/MODIS List File:", inputDirPanel, panel);
		layout.addLabelWidgetPair("Data selection:", dataPanel, panel);
		//layout.addLabelWidgetPair("   ", new JLabel("   "), panel);

		layout.makeCompactGrid(panel, 5, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading

		return panel;
	}
	

	
	private JPanel getSouthPanel() {
		JPanel panel = new JPanel();
		JButton display = new JButton(runAction());
		panel.add(display);

		panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

		return panel;
	}
	
	private Action runAction() {
		return new AbstractAction("Run") {
			private static final long serialVersionUID = 8573618661168062193L;

			public void actionPerformed(ActionEvent e) {
				
				try {		
					generateRunScript();
				} catch (Exception exc) {
					//msg.error("ERROR", exc);
					app.showMessage("Run script", exc.getMessage());
				}
			}
		};
	}
	
	private void generateRunScript() throws Exception {
		String baseDir = Constants.getProperty(Constants.EPIC_HOME, msg);
		if (baseDir == null || baseDir.isEmpty()) 
			throw new Exception("Base dir is empty, please specify it in the configuration file!");
		
		String scenarioDir = this.scenarioDir.getText();
		if (scenarioDir == null || scenarioDir.isEmpty()) 
			throw new Exception("Scenario dir is empty!");
//		if (scenarioDir.trim().contains(" ") )
//			throw new Exception(" New scenario name has space in between.");
//			 
		validateGrids();
		 
	    
	    String dYear = this.nlcdYear.getText();
		if ( dYear.trim().isEmpty() )
			throw new Exception("NLCD/MODIS data year is empty!");	 
		
		String inputFile = this.inputDir.getText();
		if (inputFile == null || inputFile.isEmpty()) 
			throw new Exception("Please select input file!");		 
		
        String sahome = Constants.getProperty(Constants.SA_HOME, msg);
        if (sahome == null || sahome.trim().isEmpty() || !(new File(sahome).exists()))
        	throw new Exception("SA dir is empty, please specify it in the configuration file!");
        
        outMessages += ls + "Epic base: " + baseDir + ls;
		outMessages += "SA home: " + sahome + ls;
		
		final String file = writeRunScriptScript(baseDir, scenarioDir, sahome, dYear.trim());		
		Thread populateThread = new Thread(new Runnable() {
			public void run() {
				runScript(file);
			}
		});
		populateThread.start();
	}
	
	protected String writeRunScriptScript( 
			String baseDir, 
			String scenarioDir, 
			String sahome, String dYear ) throws Exception {

		Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		String file = scenarioDir.trim() + "/scripts/";
		file = file.trim() + "generateBeld4Data_"+ gridName.getText().trim()+ "_" + timeStamp +".csh";
		
		StringBuilder sb = new StringBuilder();
		
		//String netcdfout = (netcdfFile.getText() == null || netcdfFile.getText().trim().isEmpty()) ? "NONE" : netcdfFile.getText().trim();
		sb.append(getScriptHeader() + ls);
		sb.append("#" + ls + "# Set up runtime environment" + ls + "#" + ls);
		sb.append("source " + sahome.trim() + Constants.SA_SETUP_FILE + ls + ls);
		
		sb.append(ls + "#" + ls);
		sb.append("# Define environment variables" + ls);
		sb.append("#" + ls + ls);
		sb.append("setenv    EPIC_DIR " + baseDir + ls);
		sb.append("setenv    SCEN_DIR " + scenarioDir + ls);
		sb.append("setenv    SA_HOME " + sahome +ls);
		sb.append("setenv    COMM_DIR $EPIC_DIR/common_data " + ls);
		sb.append("cd        $SCEN_DIR/scripts " + ls);
		
		sb.append("#" + ls + "# Define domain grids" + ls + "#" + ls);
		//sb.append("setenv GRID_PROJ \"+proj=lcc +a=6370000.0 +b=6370000.0 +lat_1=33 +lat_2=45 +lat_0=40 +lon_0=-97\"" + ls + ls);
		sb.append("setenv GRID_PROJ     \"" + proj4proj.getText().trim() + "\"" + ls);
		sb.append("setenv GRID_ROWS      " + ((Number)rows.getValue()).intValue() + ls);
		sb.append("setenv GRID_COLUMNS   " + ((Number)cols.getValue()).intValue() + ls + ls);
		sb.append("setenv GRID_XMIN      " + ((Number)xmin.getValue()).doubleValue() + ls);
		sb.append("setenv GRID_YMIN      " + ((Number)ymin.getValue()).doubleValue() + ls + ls);
		sb.append("setenv GRID_XCELLSIZE " + ((Number)xSize.getValue()).doubleValue() + ls);
		sb.append("setenv GRID_YCELLSIZE " + ((Number)ySize.getValue()).doubleValue() + ls + ls);
		String gridN = gridName.getText() == null ? "" : gridName.getText().trim();
		sb.append("setenv GRID_NAME  \"" + gridN + "\"" + ls + ls);
		
		sb.append("# Define input file which contains NLCD and MODIS land cover files " + ls + ls );
		sb.append("setenv INPUT_FILE_LIST  " + inputDir.getText() + ls + ls);
		
		String nlcdYN = nlcdBox.isSelected()? "YES" : "NO";
		String modisYN = modisBox.isSelected()? "YES" : "NO";
		sb.append("# INCLUDE data selection  " + ls );
		sb.append("setenv INCLUDE_NLCD    " + nlcdYN + ls );
		sb.append("setenv INCLUDE_MODIS   " + modisYN + ls + ls);
	 
		sb.append("# Define county shapefile " + ls );
		sb.append("setenv COUNTY_SHAPEFILE     $SA_HOME/data/county_pophu02_48st.shp" + ls + ls);		
		sb.append("setenv COUNTY_FIPS_ATTR     CNTYID" + ls + ls );
		sb.append("# BELD3 FIA tree fractions at county level for 1990s" + ls );
		sb.append("setenv US_COUNTY_FIA_FILE    $SA_HOME/data/beld3-fia.dat" + ls + ls);
		
		sb.append("# new NASS crop/pasture fractions at county level for 2001 or 2006" + ls);
		sb.append("setenv US_COUNTY_NASS_FILE   $SA_HOME/data/nass" + dYear +"_beld4_ag.dat" + ls + ls);
		 
		sb.append("# CAN crop division or census division shapefile " + ls);
		sb.append("# Only divisions have crop data are incldued " + ls);
		sb.append("setenv CAN_COUNTY_SHAPEFILE      $SA_HOME/data/can" + dYear +"_cd_sel.shp" + ls);
		sb.append("setenv CAN_COUNTY_FIPS_ATTR      AGUID  " + ls + ls );
		
		sb.append("# CAN crop fraction table at crop divisions for 2001 or 2006" + ls);
		sb.append("setenv CAN_CROP_FILE  $SA_HOME/data/can" + dYear +"_beld4_ag.dat" + ls +ls);
		
		sb.append("#table contains class names for land cover data and canopy FIA trees" + ls + ls);
		sb.append("setenv BELD4_CLASS_NAMES     $SA_HOME/data/beld4_class_names_40classes.txt" + ls + ls);
		
		sb.append("# Output files" + ls);
		sb.append("setenv OUTPUT_LANDUSE_TEXT_FILE      $SCEN_DIR/share_data/beld4_" + gridN +"_"+ dYear +".txt" + ls);
		sb.append("setenv OUTPUT_LANDUSE_NETCDF_FILE    $SCEN_DIR/share_data/beld4_" + gridN +"_"+ dYear + ".nc" + ls + ls);
		sb.append("$SA_HOME/bin/64bits/computeGridLandUse_beld4.exe" + ls);
		
		sb.append("   if ( $status == 0 ) then " + ls);
		sb.append("      echo  ==== Finished Beld4 data generation. " + ls);
		sb.append("   else " + ls);
		sb.append("      echo  ==== Error in Beld4 data generation." + ls + ls);
		sb.append("      echo " + ls );
		sb.append("   endif " + ls);
		sb.append("#===================================================================" + ls);
        
		String mesg = "";
		try {
			File script = new File(file);
			Runtime.getRuntime().exec("chmod 755 " + script.getAbsolutePath());
	        BufferedWriter out = new BufferedWriter(new FileWriter(script));
	        out.write(sb.toString());
	        out.close();
	        mesg = "Created a script file: " + file + ls;
	        boolean ok = script.setExecutable(true, false);
	        mesg += "Set the script file to be executable: ";
	        mesg += ok ? "ok." : "failed.";
	    } catch (IOException e) {
	    	throw new Exception(e.getMessage());
	    }
	    app.showMessage("Write script", mesg);
		return file;
	}
	
	private String getScriptHeader() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  generate landuse information for a given modeling " + ls );
		sb.append("#           domain grids from:" + ls); 

		sb.append("#     1. USGS NLCD 30m Landuse Files " + ls); 
		sb.append("#     2. USGS NLCD 30m Urban Imperviousness files " + ls); 
		sb.append("#     3. USGS NLCD 30m Tree Canopy Files " + ls); 
		sb.append("#     4. MODIS Landcove files " + ls + ls );
		sb.append("#     NLCD data can be downloaded from: " + ls);
		sb.append("#         http://www.mrlc.gov/nlcd2001.php " + ls); 
		sb.append("#     MODIS land cover tiles (e.g. MCD12Q1) can be downloaded from:" + ls); 
		sb.append("#         http://ladsweb.nascom.nasa.gov/data/search.html" + ls + ls); 
		
		sb.append("#     Crop and FIA data used for more crop and tree classes" + ls); 
		sb.append("#     1. US FIA tree species fractions at county (from census data) " + ls);
		sb.append("#     2. US NASS crop fractions at county" + ls);
		sb.append("#     3. CAN crop fractions at crop divisions" + ls);
		sb.append("#     4. Class names for all crops and trees " + ls +ls);
		
		sb.append("#     Shapefiles used:" + ls); 
		sb.append("#     1. US county shapefile and ID attribute" + ls);
		sb.append("#     2. CAN census division shapefile and ID attribute " + ls);
		
		sb.append("# Written by:   L. R., 2012-2013 updated " + ls);
		sb.append("#" + ls);
		sb.append("# Call program: computeGridLandUse.exe" + ls);
		sb.append("#               Needed environment variables included in the script file to run." + ls); 
		sb.append("#" + ls);        
		sb.append("#***************************************************************************************" + ls + ls);
 
		return sb.toString();

	}
	
	private void runScript(final String file) {
		String log = file + ".log";
		 
		outMessages += "Script file: " + file + ls;
		outMessages += "Log file: " + log + ls;
		runMessages.setText(outMessages);
		runMessages.validate();
		FileRunner.runScript(file, log, msg);
	}
	
	@Override
	public void newProjectCreated() {
		DomainFields domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		rows.setValue(domain.getRows());
		cols.setValue(domain.getCols());
		xmin.setValue(domain.getXmin());
		ymin.setValue(domain.getYmin());
		xSize.setValue(domain.getXcellSize());
		ySize.setValue(domain.getYcellSize());
		proj4proj.setText(domain.getProj());
		gridName.setText(domain.getGridName());
		scenarioDir.setText(domain.getScenarioDir());
		String nlcdY = domain.getNlcdYear();
		nlcdYear.setText(nlcdY);
		String sahome = Constants.getProperty(Constants.SA_HOME, msg);	
		inputDir.setText(sahome.trim() + "/data/nlcd_modis_files_" + nlcdY + ".txt");
		
		runMessages.setText("");
		if ( fields == null ) {
			fields = new Beld4DataGenFields();
			app.getProject().addPage(fields);
		}
	}

	@Override
	public void projectLoaded() {
		fields = (Beld4DataGenFields) app.getProject().getPage(fields.getName());
		if( fields != null ) {
			this.scenarioDir.setText(fields.getScenarioDir());
			 
			this.runMessages.setText(fields.getMessage());
			rows.setValue(fields.getRows());
			cols.setValue(fields.getCols());
			xmin.setValue(fields.getXmin());
			ymin.setValue(fields.getYmin());
			xSize.setValue(fields.getXcellSize());
			ySize.setValue(fields.getYcellSize());
			proj4proj.setText(fields.getProj());
			gridName.setText(fields.getGridName());	
			nlcdYear.setText(fields.getNLCDyear());
			inputDir.setText(fields.getNLCDfile());
			nlcdBox.setSelected(fields.isNlcdDataSelected());
			modisBox.setSelected(fields.isModisDataSelected());
		}else {		
			newProjectCreated();
		}	
		//setFalseEditable();
	}

	@Override
	public void saveProjectRequested() {
		if ( scenarioDir != null ) fields.setScenarioDir(scenarioDir.getText());
		if ( rows != null ) fields.setRows(Integer.parseInt(rows.getText() == null? "0" : rows.getValue()+""));
		 
		if ( cols != null ) fields.setCols(Integer.parseInt(cols.getText() == null? "0" : cols.getValue()+""));
		if ( xSize != null ) fields.setXcellSize(Float.parseFloat(xSize.getText() == null? "0" : xSize.getValue()+""));
		if ( ySize != null ) fields.setYcellSize(Float.parseFloat(ySize.getText() == null? "0" : ySize.getValue()+""));
		if ( xmin != null ) fields.setXmin(Float.parseFloat(xmin.getText() == null? "0" : xmin.getValue()+""));
		if ( ymin != null ) fields.setYmin(Float.parseFloat(ymin.getText() == null? "0" : ymin.getValue()+""));
		if ( proj4proj != null ) fields.setProj(proj4proj.getText() == null? "" : proj4proj.getText());
		if ( gridName != null ) fields.setGridName(gridName.getText()== null? "" : gridName.getText());
		if ( runMessages != null ) fields.setMessage(runMessages.getText().trim());
		if ( nlcdBox != null ) fields.setNlcdDataSelected(nlcdBox.isSelected());
		if ( modisBox != null ) fields.setModisDataSelected(modisBox.isSelected());
		  
		if ( nlcdYear != null ) {
			String dYear = this.nlcdYear.getText();
			fields.setNLCDyear(dYear);
		}
		if ( inputDir != null ) fields.setNLCDfile(inputDir.getText().trim());
		
	}
	
}



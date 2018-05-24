package gov.epa.festc.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.EpicAppFields;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.FileRunner;
import gov.epa.festc.util.SpringLayoutGenerator;
import simphony.util.messages.MessageCenter;

public class EpicRunAppPanel extends UtilFieldsPanel implements PlotEventListener {
 
	private static final long serialVersionUID = -7198422813335101891L;
	private static final String TITLE = Constants.EPIC_APP;

	private FestcApplication app;

	private CropSelectionPanel cropSelectionPanel;

	//private JComboBox comboCrops;
	private JTextField simYear;

	private MessageCenter msg;
	
	String baseDir = null;

	private EpicAppFields fields;
	//private DomainFields domain;
	
	private JComboBox nDepSel;
	private JComboBox runTiledrain;
	private JTextField co2Factor;

	public EpicRunAppPanel(FestcApplication application) {
		app = application;
		fields = new EpicAppFields();
		app.getProject().addPage(fields);
		msg = app.getMessageCenter();
		baseDir = Constants.getProperty(Constants.EPIC_HOME, msg);
		app.addPlotListener(this);
		add(createPanel());
	}
	
	public void setMessageCenter(MessageCenter msgCtr) {
		msg = msgCtr;
	}

	private JPanel createPanel() {
		init();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(getNorthPanel());
		mainPanel.add(getCenterPanel());
		 
		mainPanel.add(cropsPanel());
		mainPanel.add(messageBox());
		return mainPanel;
	}

	private JPanel getNorthPanel() {
		JPanel panel = new JPanel();
		JLabel title = new JLabel(TITLE, SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		return panel;
	}

	private JPanel getCenterPanel() {
		//JPanel centerPanel = new JPanel();
		
		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		
		this.scenarioDir = new JTextField(40);
		
		nDepSel = new JComboBox(Constants.NDEPS);
		nDepSel.setSelectedIndex(2);
		nDepSel.setToolTipText("RFN0: get NDep value from EPICCONT.DAT. ");
		//JPanel co2FacPanel = new JPanel();
		co2Factor = new JTextField(20);
		//co2Factor.setToolTipText("Default value is 413.00");
		//co2FacPanel.add(co2Factor);
		
		runTiledrain = new JComboBox(new String[] {"YES", "NO"});
		runTiledrain.setSelectedIndex(1);

		this.simYear = new JTextField(20);
		layout.addLabelWidgetPair(Constants.LABEL_EPIC_SCENARIO, scenarioDir, panel);
		layout.addLabelWidgetPair("Simulation Year: ", simYear, panel);
		layout.addLabelWidgetPair("CO2 Level (ppm):",co2Factor, panel);
		layout.addLabelWidgetPair("Daily Average N Deposition: ", nDepSel, panel);
		layout.addLabelWidgetPair("Run Tiledrain : ", runTiledrain, panel);
		 
		layout.makeCompactGrid(panel, 5,2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading

		return panel;
	}
	
	
	private JPanel cropsPanel(){
		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		JPanel buttonPanel = new JPanel();
		JButton btn = new JButton(runAction());
		btn.setPreferredSize(new Dimension(100,50));
		buttonPanel.add(btn);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(70, 30, 70, 30));
		 
		this.cropSelectionPanel = new CropSelectionPanel(app);
		layout.addWidgetPair(cropSelectionPanel, buttonPanel, panel);
		layout.makeCompactGrid(panel, 1, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading		
		return panel;
	}

	private Action runAction() {
		return new AbstractAction("Run") {
			private static final long serialVersionUID = 5558465823154735475L;

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
		
		String baseDir = this.baseDir;
		if ( baseDir == null || baseDir.isEmpty()) 
			throw new Exception( "Base dir is empty, please specify in the configuration file!");
			 
		String scenarioDir = this.scenarioDir.getText();
		if ( scenarioDir == null || scenarioDir.isEmpty()) 
			throw new Exception("Please select scenario dir first!");		 
 
		String co2Fac = co2Factor.getText();
		if (co2Fac == null || co2Fac.isEmpty()) 
			throw new Exception("co2 Level is not specified!");
		
		try {
			Float.parseFloat(co2Fac);
		}catch(NumberFormatException e) {
			throw new Exception("CO2 Level is not a number!");
		}
		
		String simY = this.simYear.getText();
		if ( simY == null || simY.isEmpty()) 
			throw new Exception("Please select simulation year. ");
		
		try {
			Integer.parseInt(simY);
		}catch(NumberFormatException e) {
			throw new Exception( "Simulation year is not an integer!");
		}
		
		String ndepValue = (String) this.nDepSel.getSelectedItem();
		if ( ndepValue == null || ndepValue.isEmpty()) 
			throw new Exception( "Deposition dir is empty, please specify it!");
		
		String seCropsString = cropSelectionPanel.selectedItemTostring();
		String[] seCrops = cropSelectionPanel.getSelectedCrops();
		if ( seCrops == null || seCrops.length == 0) 
			throw new Exception( "Please select crop(s) first!");
			 

		String crop = null;
		String cropIDs = "(";
		for (int i=0; i<seCrops.length; i++) {
			crop = seCrops[i];
			Integer cropID = Constants.CROPS.get(crop);
			if ( cropID == null || cropID <= 0 )
				throw new Exception("crop id is null for crop " + crop);
			 
			Integer cropIrID = cropID +1;
			cropIDs += " " + cropID;
			cropIDs += " " + cropIrID;
		}
		cropIDs += ")";
		
		outMessages += "Epic base: " + baseDir + ls;
		outMessages += "Scen directory: " + scenarioDir + ls;
		
		final String file = writeRunScript(baseDir, scenarioDir, seCropsString, cropIDs, 
				simY, ndepValue);
		Thread populateThread = new Thread(new Runnable() {
			public void run() {
				runScript(file);
			}
		});
		populateThread.start();
	}

	protected String writeRunScript( String baseDir, String scenarioDir, 
			String cropNames, String cropIDs, String simY, String ndepValue) throws Exception {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		
		String file = scenarioDir.trim() + "/scripts";
		if ( !file.endsWith(System.getProperty("file.separator"))) 
				file += System.getProperty("file.separator");
		file += "runEpicApp_" + timeStamp + ".csh";
		
		StringBuilder sb = new StringBuilder();
		sb.append(getScirptHeader());
		sb.append(getEnvironmentDef(baseDir, scenarioDir, simY,ndepValue));
		sb.append(getRunDef(cropNames, cropIDs));		 
		
		String mesg = "";
		
		try {
			File script = new File(file);
			
	        BufferedWriter out = new BufferedWriter(new FileWriter(script));
	        out.write(sb.toString());
	        out.close();
	        
	        mesg += "Created a script file: " + file + "\n";
	        boolean ok = script.setExecutable(true, false);
	        mesg += "Set the script file to be executable: ";
	        mesg += ok ? "ok." : "failed.";
	        
	    } catch (IOException e) {
	    	//printStackTrace();
	    	//g.error("Error generating EPIC script file", e);
	    	throw new Exception(e.getMessage());
	    } 
		
	    app.showMessage("Write script", mesg);
	    
		return file;
	}

	private String getScirptHeader() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to run EPIC spinup model" + ls); 
		sb.append("#" + ls);
		sb.append("# Written by: Fortran by Benson, Script by IE. 2012" + ls);
		sb.append("# Modified by: IE " + ls); 
		sb.append("#" + ls);
		sb.append("# Program: EPICapp.exe" + ls);
		sb.append("#         Needed environment variables included in the script file to run." + ls);        
		sb.append("# " + ls);
		sb.append("#***************************************************************************************" + ls + ls);
		
		return sb.toString();
	}

	private String getEnvironmentDef(String baseDir, String scenarioDir, 
			String simY, String ndepValue ) {
		StringBuilder sb = new StringBuilder();
		
		String ls = "\n";
		sb.append(ls + "#" + ls);
		sb.append("# Define environment variables" + ls);
		sb.append("#" + ls + ls);
		sb.append("setenv    SIM_YEAR " + simY + ls);
		sb.append("setenv    type    'app' " + ls);
		sb.append("setenv    EPIC_DIR " + baseDir + ls);
		sb.append("setenv    SCEN_DIR " + scenarioDir + ls);
		sb.append("setenv    COMM_DIR  $EPIC_DIR/common_data" +ls);
		sb.append("setenv    CO2_FAC  " + co2Factor.getText() + ls);
		sb.append("setenv    RUN_TD   " +  (String)runTiledrain.getSelectedItem()  + ls);
		 
		//ndepValue = "RFN0";
		if ( ndepValue.contains("CMAQ") )  ndepValue = "CMAQ";
		else if ( ndepValue.contains("2002") )  ndepValue = "dailyNDep_2004";
		else if ( ndepValue.contains("2010") )  ndepValue = "dailyNDep_2008";
		else if ( ndepValue.contains("EPIC") )  ndepValue = "RFN0";

		if ( ndepValue.length() == 4) 
			sb.append("setenv    NDEP_DIR   " + ndepValue + ls);
		else
			sb.append("setenv NDEP_DIR $COMM_DIR/EPIC_model/" 
					+ ndepValue + ls);
		System.out.println(sb);
		sb.append("setenv    SHARE_DIR $SCEN_DIR/share_data" + ls);
		sb.append("setenv    WEAT_DIR  $COMM_DIR/statWeath" + ls);
		
		sb.append("" + ls);
		sb.append("set    EXEC_DIR = " + baseDir + "/model/current" + ls);
		sb.append("" + ls);

		return sb.toString();
	}
	
	private String getRunDef(String cropNames, String cropIDs) {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		
		sb.append(ls + "#" + ls);
		sb.append("# set input variables" + ls);
		sb.append("set CROPS = " + cropNames + ls);
		sb.append("set CROPSNUM = " + cropIDs + ls);
		sb.append("#" + ls + ls);
	 
		sb.append("# Set output dir" + ls);
		sb.append("#" + ls + ls);
		
		sb.append("setenv EPIC_CMAQ_OUTPUT  $SCEN_DIR/output4CMAQ/$type" + ls);
		sb.append("if ( ! -e $EPIC_CMAQ_OUTPUT  ) mkdir -p $EPIC_CMAQ_OUTPUT" + ls);
		sb.append("if ( ! -e $EPIC_CMAQ_OUTPUT/year  ) mkdir -p $EPIC_CMAQ_OUTPUT/year" + ls);
		sb.append("if ( ! -e $EPIC_CMAQ_OUTPUT/daily  ) mkdir -p $EPIC_CMAQ_OUTPUT/daily" + ls);
		sb.append("if ( ! -e $EPIC_CMAQ_OUTPUT/toCMAQ  ) mkdir -p $EPIC_CMAQ_OUTPUT/toCMAQ" + ls);
		sb.append(" " + ls);
		sb.append("# run EPIC model spinup - rainfed " + ls);
		sb.append(" " + ls);
		sb.append("@ n = 1 " + ls);
		sb.append("foreach crop ( $CROPS ) " + ls);
		sb.append("   setenv CROP_NAME $crop" + ls);
		sb.append("   setenv CROP_NUM  $CROPSNUM[$n]" + ls); 
		sb.append("   setenv CROP_DIR  $SCEN_DIR/${CROP_NAME}" + ls);
		sb.append("   echo $CROP_NAME, $CROP_NUM" + ls);
		sb.append("   " + ls); 
		sb.append("   @ cropN = $CROP_NUM" + ls); 
		sb.append("   if ( $cropN != 0 )  then" + ls); 
		sb.append("#  run EPIC model application - rainf   " + ls);
		sb.append("      setenv WORK_DIR   $SCEN_DIR/${CROP_NAME}/$type/rainf" + ls);
		sb.append("      setenv SOIL_DIR   $SCEN_DIR/${CROP_NAME}/spinup/rainf/SOL " +ls );
		sb.append("      foreach out ( \"NCM\" \"NCS\" \"DFA\" \"OUT\" \"SOL\" \"TNA\" \"TNS\" )" + ls); 
		sb.append("        if ( ! -e $WORK_DIR/$out  ) mkdir -p $WORK_DIR/$out" + ls); 
		sb.append("      end " + ls);
		sb.append("      time $EXEC_DIR/EPICapp.exe " + ls);
		sb.append("      if ( $status == 0 ) then " + ls);
		sb.append("         echo  ==== Finished EPIC app run of CROP: $CROP_NAME, rainf $cropN" + ls);
		sb.append("      else " + ls);
		sb.append("         echo  ==== Error in EPIC app run of CROP: $CROP_NAME, rainf $cropN" + ls + ls);
		sb.append("         echo " + ls );
		sb.append("      endif " + ls);
		sb.append("   endif " + ls);
		sb.append("#  " + ls);
		sb.append("#  run EPIC model application - irrigated   " + ls);
		sb.append("#  " + ls);
		sb.append("   @ n = $n + 1" + ls);
		sb.append("   setenv CROP_NUM $CROPSNUM[$n]" + ls);
		sb.append("   echo $CROP_NAME, $CROP_NUM" + ls);
		sb.append("   " + ls); 
		sb.append("   @ cropN = $CROP_NUM" + ls);
		sb.append("   if ( $cropN != 0 )  then" + ls);
		sb.append("      setenv WORK_DIR   $SCEN_DIR/${CROP_NAME}/$type/irr" + ls); 
		sb.append("      setenv SOIL_DIR   $SCEN_DIR/${CROP_NAME}/spinup/irr/SOL " +ls );
		sb.append("      foreach out ( \"NCM\" \"NCS\" \"DFA\" \"OUT\" \"SOL\" \"TNA\" \"TNS\" )" + ls); 
		sb.append("        if ( ! -e $WORK_DIR/$out  ) mkdir -p $WORK_DIR/$out" + ls); 
		sb.append("      end" + ls); 
		sb.append("      time $EXEC_DIR/EPICapp.exe" + ls); 
		sb.append("      if ( $status == 0 ) then " + ls);
		sb.append("         echo  ==== Finished EPIC app run of CROP: $CROP_NAME, irr $cropN" + ls);
		sb.append("      else " + ls);
		sb.append("         echo  ==== Error in EPIC app run of CROP: $CROP_NAME, irr $cropN" + ls + ls);
		sb.append("         echo " + ls );
		sb.append("      endif " + ls);
		sb.append("   endif" + ls); 
		sb.append("   @ n = $n + 1" + ls);
		sb.append("end " + ls); 
	 		
		sb.append(ls);

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

	public void projectLoaded() {
		fields = (EpicAppFields) app.getProject().getPage(fields.getName());
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		if ( fields != null ){	
			String scenloc = domain.getScenarioDir();
			if (scenloc != null && scenloc.trim().length()>0 )
				this.scenarioDir.setText(scenloc);
			else 
				this.scenarioDir.setText(fields.getScenarioDir());
			simYear.setText(domain.getSimYear());
			runMessages.setText(fields.getMessage());
			nDepSel.setSelectedItem(fields.getNDepDir());
			co2Factor.setText(fields.getCO2Fac()==null? "380.00":fields.getCO2Fac());
			runTiledrain.setSelectedItem(fields.getRunTiledrain()==null?"NO":fields.getRunTiledrain());
		}else{
			newProjectCreated();
		}
	}

	public void saveProjectRequested() {
		if ( scenarioDir != null ) domain.setScenarioDir(scenarioDir.getText());
		if ( scenarioDir != null ) fields.setScenarioDir(scenarioDir.getText());
		if ( simYear != null ) domain.setSimYear(simYear.getText());
		if ( runMessages != null ) fields.setMessage(runMessages.getText());	
		if ( nDepSel != null ) fields.setNDepDir( (String) nDepSel.getSelectedItem());
		if ( co2Factor != null)  fields.setCO2Fac(co2Factor.getText());
		if ( runTiledrain != null ) fields.setRunTiledrain((String) runTiledrain.getSelectedItem());
		if ( runMessages != null ) fields.setMessage(runMessages.getText().trim());
	}

	@Override
	public void newProjectCreated() {
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		scenarioDir.setText(domain.getScenarioDir());	
		simYear.setText(domain.getSimYear());
		nDepSel.setSelectedIndex(1);
		runMessages.setText("");
		co2Factor.setText("380.00");
		runTiledrain.setSelectedIndex(1);
		if ( fields == null ) {
			fields = new EpicAppFields();
			app.getProject().addPage(fields);
		}
	}				
}

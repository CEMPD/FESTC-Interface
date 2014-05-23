package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.FestcGUI;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.Epic2CMAQFields;
import gov.epa.festc.core.proj.EpicAppFields;
import gov.epa.festc.core.proj.SiteInfoGenFields;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.FileRunner;
import gov.epa.festc.util.SpringLayoutGenerator;

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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

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
		
//	    {
//	      public void actionPerformed(ActionEvent ae)
//	      {
//	        JFileChooser fileChooser = new JFileChooser(scenarioDir.getText());
//	        fileChooser.setMultiSelectionEnabled(false);
//	        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//	        int returnVal = fileChooser.showOpenDialog(EpicRunAppPanel.this);
//	        if (returnVal != JFileChooser.APPROVE_OPTION) return;
//	        File selected = fileChooser.getSelectedFile();
//	        scenarioDir.setText(selected.getAbsolutePath());
//	        app.setCurrentDir(selected);
//	      }//actionPerformed()
//	    });
//		scenarioPanle.add(this.scenarioDir);
//		scenarioPanle.add(scenarioDirBrowser);

		this.simYear = new JTextField(40);
		layout.addLabelWidgetPair(Constants.LABEL_EPIC_SCENARIO, scenarioDir, panel);
		layout.addLabelWidgetPair("Simulation Year: ", simYear, panel);
		layout.makeCompactGrid(panel, 2, 2, // number of rows and cols
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
 
		String simY = this.simYear.getText();
		if ( simY == null || simY.isEmpty()) 
			throw new Exception("Please select simulation year. ");
		
		try {
			Integer.parseInt(simY);
		}catch(NumberFormatException e) {
			throw new Exception( "Simulation year is not an integer!");
		}
		
		String sSimYear = app.getSSimYear();
		if (sSimYear == null || sSimYear.trim().isEmpty()) {
			app.setSSimYear(sSimYear);
			sSimYear = simY;
		}	
		else if (sSimYear != null && !sSimYear.trim().isEmpty() 
				&& !sSimYear.endsWith(simY) && app.allowDiffCheck()) 
			throw new Exception("Current modeling year is inconsistent with previous one (" + sSimYear + ")");
		

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
		
		final String file = writeRunScript(baseDir, scenarioDir, seCropsString, cropIDs, simY);
		Thread populateThread = new Thread(new Runnable() {
			public void run() {
				runScript(file);
			}
		});
		populateThread.start();
	}

	protected String writeRunScript( String baseDir, String scenarioDir, 
			String cropNames, String cropIDs, String simY) throws Exception {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		
		String file = scenarioDir.trim() + "/scripts";
		if ( !file.endsWith(System.getProperty("file.separator"))) 
				file += System.getProperty("file.separator");
		file += "runEpicApp_" + timeStamp + ".csh";
		
		StringBuilder sb = new StringBuilder();
		sb.append(getScirptHeader());
		sb.append(getEnvironmentDef(baseDir, scenarioDir, simY));
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
		sb.append("# Program: EPIC0509app.exe" + ls);
		sb.append("#         Needed environment variables included in the script file to run." + ls);        
		sb.append("# " + ls);
		sb.append("#***************************************************************************************" + ls + ls);
		
		return sb.toString();
	}

	private String getEnvironmentDef(String baseDir, String scenarioDir, String simY) {
		StringBuilder sb = new StringBuilder();
		
		String ls = "\n";
		sb.append(ls + "#" + ls);
		sb.append("# Define environment variables" + ls);
		sb.append("#" + ls + ls);
		sb.append("setenv    SIM_YEAR " + simY + ls);
		sb.append("set type = 'app' " + ls);
		sb.append("setenv    EPIC_DIR " + baseDir + ls);
		sb.append("setenv    SCEN_DIR " + scenarioDir + ls);
		sb.append("setenv    COMM_DIR  $EPIC_DIR/common_data" +ls);
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
		sb.append("      time $EXEC_DIR/EPIC0509app.exe " + ls);
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
		sb.append("      time $EXEC_DIR/EPIC0509app.exe" + ls); 
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
		if ( fields != null ){	
			this.scenarioDir.setText(fields.getScenarioDir());
			simYear.setText(fields.getSimYear());
			runMessages.setText(fields.getMessage());
		}else{
			newProjectCreated();
		}
	}

	public void saveProjectRequested() {
		if ( scenarioDir != null ) fields.setScenarioDir(scenarioDir.getText());
		if ( simYear != null ) fields.setSimYear(simYear.getText());
		if ( runMessages != null ) fields.setMessage(runMessages.getText());		
	}

	@Override
	public void newProjectCreated() {
		DomainFields domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		scenarioDir.setText(domain.getScenarioDir());	
		simYear.setText(domain.getSimYear());
		runMessages.setText("");
		if ( fields == null ) {
			fields = new EpicAppFields();
			app.getProject().addPage(fields);
		}
	}				
}

package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.Mcip2EpicFields;
import gov.epa.festc.core.proj.SoilFilesFields;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.FileRunner;
import gov.epa.festc.util.SpringLayoutGenerator;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import simphony.util.messages.MessageCenter;

public class UtilGenerateSoilMatchPanel extends UtilFieldsPanel implements PlotEventListener {
	
	private FestcApplication app;
	private MessageCenter msg;
	private SoilFilesFields fields;

	private CropSelectionPanel cropSelectionPanel;	
	 
	public UtilGenerateSoilMatchPanel(FestcApplication application, MessageCenter msg) {
		app = application;
		fields = new SoilFilesFields();
		app.getProject().addPage(fields);
		app.addPlotListener(this);
		this.msg = msg;
		add(createPanel());
	}
	
	private JPanel createPanel() {
		JPanel mainPanel = new JPanel();		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		  
		mainPanel.add(scenPanel());
		mainPanel.add(cropsPanel());
		mainPanel.add(messageBox());
        return mainPanel;
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
					generateSoilMatchFiles();
				} catch (Exception exc) {
					//msg.error("ERROR", exc);
					app.showMessage("Run script", exc.getMessage());
				}
			}
		};
	}
	
	private void generateSoilMatchFiles() throws Exception {
		String baseDir = Constants.getProperty(Constants.EPIC_HOME, msg);
		if (baseDir == null || baseDir.isEmpty()) 
			throw new Exception("Base dir is empty, please specify in the configuration file!");
	 
		String scenarioDir = this.scenarioDir.getText();
		if ( scenarioDir == null || scenarioDir.isEmpty()) 
			throw new Exception("Please select scenario dir first!");
		 
		String seCropsString = cropSelectionPanel.selectedItemTostring();
		String[] seCrops = cropSelectionPanel.getSelectedCrops();
		if ( seCrops == null || seCrops.length == 0) 
			throw new Exception("Please select crop(s) first!"); 
		
		outMessages += "Epic base: " + baseDir + ls;
		outMessages += "Scen directory: " + scenarioDir + ls;
		
		final String file = writeRunScript(baseDir, scenarioDir, seCropsString);
		
		Thread populateThread = new Thread(new Runnable() {
			public void run() {
				runScript(file);
			}
		});
		populateThread.start();
	}
	
	private void runScript(final String file) {
		String log = file + ".log";
		 
		outMessages += "Script file: " + file + ls;
		outMessages += "Log file: " + log + ls;
		runMessages.setText(outMessages);
		runMessages.validate();
		FileRunner.runScript(file, log, msg);
	}
	
	protected String writeRunScript( 
			String baseDir, 
			String scenarioDir,
			String cropNames ) throws Exception {
		
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		
		String file = scenarioDir.trim() + "/scripts";
		if ( !file.endsWith(System.getProperty("file.separator"))) 
				file += System.getProperty("file.separator");
		file += "runEpicSoilMatch_" + timeStamp + ".csh";
		
		StringBuilder sb = new StringBuilder();
		sb.append(getScirptHeader());
		sb.append(getEnvironmentDef(baseDir, scenarioDir));
		sb.append(getRunDef(cropNames));
		
		String mesg = "";
		
		try {
			File script = new File(file);
			
	        BufferedWriter out = new BufferedWriter(new FileWriter(script));
	        out.write(sb.toString());
	        out.close();
	        
	        mesg += "Script file: " + file + ls;
	        boolean ok = script.setExecutable(true, false);
	        mesg += "Set the script file to be executable: ";
	        mesg += ok ? "ok." : "failed.";
	        
	    } catch (IOException e) {
	    	//e.printStackTrace();
	    	//msg.error("Error generating EPIC script file", e);
	    	throw new Exception(e.getMessage());
	    } 
		
	    app.showMessage("Write script", mesg);
	    
		return file;
	}
	
	private String getScirptHeader() {
		StringBuilder sb = new StringBuilder();
		 
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to run Soil Match Utility" + ls); 
		sb.append("#" + ls);
		sb.append("# Written by: Fortran by Benson, Script by IE. 2010" + ls);
		sb.append("# Modified by:" + ls); 
		sb.append("#" + ls);
		sb.append("# Program: SOILMATCH*.exe" + ls);
		sb.append("#         Needed environment variables included in the script file to run." + ls);        
		sb.append("# " + ls);
		sb.append("#***************************************************************************************" + ls + ls);
		
		return sb.toString();
	}
	
	private String getEnvironmentDef(String baseDir, String scenarioDir) {
		StringBuilder sb = new StringBuilder();
	
		sb.append(ls + "#" + ls);
		sb.append("# Define environment variables" + ls);
		sb.append("#" + ls + ls);
		sb.append("setenv    EPIC_DIR " + baseDir + ls);
		sb.append("setenv    SCEN_DIR " + scenarioDir + ls);
		sb.append("setenv    COMM_DIR $EPIC_DIR/common_data" + ls);
		sb.append("setenv    WORK_DIR $SCEN_DIR/work_dir" + ls);
		sb.append("setenv    SHARE_DIR $SCEN_DIR/share_data" + ls);
		sb.append("" + ls);
		sb.append("set    EXEC_DIR = " + baseDir + "/util/soilMatch" + ls);
		sb.append("" + ls);
		
		return sb.toString();
	}
		
	private String getRunDef(String cropNames) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(ls + "#" + ls);
		sb.append("# Generate soil match files " + ls);
		sb.append("#" + ls );
		sb.append("# set input variables" + ls);
		sb.append("set CROPS = " + cropNames + ls);
		sb.append("foreach crop ($CROPS) " + ls);
		sb.append("   setenv CROP_NAME $crop " + ls);
		sb.append("   rm -rf $SCEN_DIR/$CROP_NAME/NONRISOIL*.DAT >& /dev/null " + ls );
		sb.append("   rm -rf $SCEN_DIR/$CROP_NAME/SOILSKM*.LOC >& /dev/null" + ls +ls);
		sb.append("   echo ==== Begin soil match run for crop $CROP_NAME." +ls);
		sb.append("   echo ==== Running step 1 .... " + ls);
		sb.append("   time $EXEC_DIR/SOILMATCH1ST.exe" + ls ); 
		 
		sb.append(" " + ls);
		sb.append("   echo ==== Running step 2 .... " + ls);
		sb.append("   time $EXEC_DIR/SOILMATCH2ND.exe" + ls ); 
	 
		sb.append(" " + ls);
		sb.append("   echo ==== Running step 3 .... " + ls);
		sb.append("   time $EXEC_DIR/SOILMATCH3RD.exe" + ls ); 
	 
		sb.append(" " + ls);
		sb.append("   echo ==== Running step 4 .... " + ls);
		sb.append("   time $EXEC_DIR/SOILMATCH4TH.exe" + ls ); 
		 
		sb.append(" " + ls);
		sb.append("   echo ==== Running step 5 .... " + ls);
		sb.append("   time $EXEC_DIR/SOILMATCH5TH.exe" + ls ); 
	 
		sb.append(" " + ls);
		sb.append("   echo ==== Running step 6 .... " + ls);
		sb.append("   time $EXEC_DIR/SOILMATCH6TH.exe" + ls ); 
	 
		sb.append("   if ( $status == 0 ) then " + ls);
		sb.append("      echo  ==== Finished soil match run for crop $CROP_NAME. " + ls);
		sb.append("   else " + ls);
		sb.append("      echo  ==status== Error in soil match run for crop $CROP_NAME. "+ ls + ls);
		sb.append("      exit 1 " + ls );
		sb.append("   endif " + ls);
		sb.append(" " + ls);
		sb.append("   echo \" Merging *LOC to SOILLIST.DAT\"" + ls);
		sb.append("   cat $SCEN_DIR/$CROP_NAME/*LOC > $SCEN_DIR/$CROP_NAME/SOILLIST.DAT" + ls);
		sb.append("end " + ls);
		sb.append(ls);
		
//		outMessages += "  Inputs: ALL-CULTIVATED10-12-09.LST" + ls;  
//		outMessages += "          NRI-ALL-HUC8S-ALLCROPS.prn" + ls; 
//	    outMessages += "          HUC8_SITE_INFO-2REV.prn" + ls; 
//		outMessages += "          NRI-crop-codes-BELD4-codes.prn" + ls; 
//		outMessages += "          HUC8NRICROPSOIL.DAT" + ls; 
//		outMessages += "          HUCSITELATLONG.DAT" + ls; 
		outMessages += "  Step 1 output: $SCEN_DIR/$CROP  SOILSKM1.LOC" + ls;
		outMessages += "  Step 2 output: $SCEN_DIR/$CROP  SOILSKM2.LOC" + ls;	
		outMessages += "  ... "	;	
		outMessages += "  Final output : $SCEN_DIR/$CROP  *.LOC > SOILLIST.DAT" + ls;	
		 
		return sb.toString();
	}

	@Override
	public void projectLoaded() {
		fields = (SoilFilesFields) app.getProject().getPage(fields.getName());
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		if ( fields != null ){
			String scenloc = domain.getScenarioDir();
			if (scenloc != null && scenloc.trim().length()>0 )
				this.scenarioDir.setText(scenloc);
			else 
				this.scenarioDir.setText(fields.getScenarioDir());
			runMessages.setText(fields.getMessage());
		}else{
			newProjectCreated();
		}
		 
	}

	@Override
	public void saveProjectRequested() {
		if ( scenarioDir != null ) domain.setScenarioDir(scenarioDir.getText());
		if ( scenarioDir != null ) fields.setScenarioDir(scenarioDir.getText());
		if ( runMessages != null ) fields.setMessage(runMessages.getText());		
	}

	@Override
	public void newProjectCreated() {
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		scenarioDir.setText(domain.getScenarioDir());
		runMessages.setText("");
		if ( fields == null ) {
			fields = new SoilFilesFields();
			app.getProject().addPage(fields);
		}
	}				
}

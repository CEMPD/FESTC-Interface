package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.ManageSpinupFields;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.FileRunner;
import gov.epa.festc.util.ModelYearInconsistantException;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import simphony.util.messages.MessageCenter;

public class CreateSpinupManFilesPanel extends UtilFieldsPanel implements PlotEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7631048430725473655L;
	
	private FestcApplication app;
	private MessageCenter msg;
	 
	private ManageSpinupFields fields;
 
	private CropSelectionPanel cropSelectionPanel;
	
	public CreateSpinupManFilesPanel(FestcApplication application) {
		app = application;
		fields = new ManageSpinupFields();
		app.getProject().addPage(fields);
		app.addPlotListener(this);
		msg = app.getMessageCenter();
		add(createPanel());
	}
	
	private JPanel createPanel() {
		JPanel mainPanel = new JPanel();		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(getNorthPanel());
		
		mainPanel.add(scenPanel());
		mainPanel.add(fertYearPanel());
		mainPanel.add(cropsPanel());
		mainPanel.add(messageBox());
        return mainPanel;        
	}
	
	private JPanel cropsPanel(){
		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		JPanel buttonPanel = new JPanel();
		JButton btn = new JButton(generateManSpinupAction());
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
	
	private JPanel getNorthPanel() {
		JPanel panel = new JPanel();
		JLabel title = new JLabel(Constants.MAN_SPINUP, SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		return panel;
	}
	
	private Action generateManSpinupAction() {
		return new AbstractAction("Run") {

			public void actionPerformed(ActionEvent e) {
				try {					
					generateRunScript();					
				} catch (Exception exc) {
					//exc.printStackTrace();
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
		if ( scenarioDir == null || scenarioDir.isEmpty()) 
			throw new Exception("Please select scenario dir first!");

		String fYear = (String) this.fertYearSel.getSelectedItem();
		if ( fYear.trim().isEmpty() )
			throw new Exception("Please select fertilizer year!");
			 
		String sFYear = app.getSFertYear();
		if (sFYear == null || sFYear.trim().isEmpty()) {
			app.setSFertYear(fYear);
			sFYear = fYear;
		}	
		else if (sFYear != null && !sFYear.trim().isEmpty() 
				&& !sFYear.endsWith(fYear) && app.allowDiffCheck()) 
			throw new Exception("Current land use year is inconsistent with previous one (" + sFYear + ")");
		//System.out.println("sfYear: " + sFYear + " " + fYear);
		String seCropsString = cropSelectionPanel.selectedItemTostring();
		//System.out.println();
		String[] seCrops = cropSelectionPanel.getSelectedCrops();
		if ( seCrops == null || seCrops.length == 0 ) 
			throw new Exception("Please select crop(s) first!");

		outMessages += ls + "Epic base: " + baseDir + ls;
		outMessages += "Scen directory: " + scenarioDir + ls;
		outMessages += "Fertlizer year: " + fYear + ls;
		final String file = writeRunScript(baseDir, scenarioDir, seCropsString, fYear);
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
	
	private String writeRunScript( 
			String baseDir, 
			String scenarioDir,
			String cropNames,
			String fYear) throws Exception  {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

		String file = scenarioDir.trim() + "/scripts";
			    
		if ( !file.endsWith(System.getProperty("file.separator"))) 
				file += System.getProperty("file.separator");
		file += "runEpicManSpinup_" + timeStamp + ".csh";
		String mesg = "";
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(getScriptHeader());
			sb.append(getEnvironmentDef(baseDir, scenarioDir, fYear));
			sb.append(getManSu(cropNames));		

			File script = new File(file);

	        BufferedWriter out = new BufferedWriter(new FileWriter(script));
	        out.write(sb.toString());
	        out.close();
	        
	        mesg += "Script file: " + file + "\n";
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
	
	private String getScriptHeader() {
		StringBuilder sb = new StringBuilder();
		 
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to run management spinup utility" + ls); 
		sb.append("#" + ls);
		sb.append("# Written by: Fortran by Benson, Script by IE. 2012" + ls);
		sb.append("# Modified by:" + ls); 
		sb.append("#" + ls);
		sb.append("# Program: ManGenSU.exe" + ls);
		sb.append("# " + ls);
		sb.append("#***************************************************************************************" + ls + ls);
		
		return sb.toString();
	}
	
	private String getEnvironmentDef(String baseDir, String scenarioDir, String fYear) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(ls + "#" + ls);
		sb.append("# Define environment variables" + ls);
		sb.append("#" + ls + ls);

		sb.append("setenv    EPIC_DIR " + baseDir + ls);
		sb.append("setenv    SCEN_DIR " + scenarioDir + ls);
		sb.append("setenv    SOIL_DIR $EPIC_DIR/common_data/BaumerSoils" +ls);
		sb.append("setenv    MANG_DIR $EPIC_DIR/common_data/util/manageCreate/" + fYear + ls);
		sb.append("setenv    WEAT_DIR $EPIC_DIR/common_data/statWeath" + ls);
		sb.append("" + ls);
		sb.append("set    EXEC_DIR = " + baseDir + "/util/manageCreate" + ls);
		sb.append("" + ls);

		return sb.toString();
	}
	
	private String getManSu(String cropNames){
		StringBuilder sb = new StringBuilder();
		
		sb.append("#" + ls);
		sb.append("# set input variables" + ls);
		sb.append("set CROPS = " + cropNames + ls);
		sb.append("foreach crop ($CROPS) " + ls);
		sb.append("   setenv CROP_NAME $crop " + ls);
		sb.append("   if ( ! -e $SCEN_DIR/$CROP_NAME/spinup/manage/OPC ) " +
				" mkdir -p $SCEN_DIR/$CROP_NAME/spinup/manage/OPC " + ls);
		sb.append("#" + ls);
		String exe;
		exe = "$EXEC_DIR/ManGenSU.exe";
		
		sb.append(ls + "#" + ls);
		sb.append("#  Generate management spinup files " + ls);
		sb.append("#" + ls + ls);
		sb.append("    echo ==== Begin EPIC management spinup run of CROP: $CROP_NAME " + ls);
		sb.append("    time " + exe + ls ); 
		sb.append("    if ( $status == 0 ) then" + ls);
		sb.append("    echo ==== Finished EPIC management spinup run of CROP: $CROP_NAME" + ls);
		sb.append("    else " + ls);
		sb.append("    echo ==== Error in EPIC management spinup run of CROP: $CROP_NAME" + ls);
		sb.append("    endif " + ls);
		sb.append("end " + ls ); 
		sb.append(ls);

		return sb.toString();
	}

	public void projectLoaded() {
		fields = (ManageSpinupFields) app.getProject().getPage(fields.getName());
		
		if ( fields != null  ){
			this.scenarioDir.setText(fields.getScenarioDir());
			fertYearSel.setSelectedItem(fields.getFertYear());
			runMessages.setText(fields.getMessage());
		}else{
			newProjectCreated();
		}
	}

	public void saveProjectRequested() {
		if ( scenarioDir != null ) fields.setScenarioDir(scenarioDir.getText());
		if ( fertYearSel != null)  fields.setFertYear((String)fertYearSel.getSelectedItem());
		if ( runMessages != null ) fields.setMessage(runMessages.getText());		
	}

	@Override
	public void newProjectCreated() {
		DomainFields domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		scenarioDir.setText(domain.getScenarioDir());	
		runMessages.setText("");
		if ( fields == null ) {
			fields = new ManageSpinupFields();
			app.getProject().addPage(fields);
		}
	}				
}

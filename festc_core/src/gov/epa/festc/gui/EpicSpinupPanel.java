package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.EpicSpinupFields;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import simphony.util.messages.MessageCenter;

public class EpicSpinupPanel  extends UtilFieldsPanel implements PlotEventListener {
 
	private static final long serialVersionUID = -625272283986456313L;
	private JTextField scenarioDir;
	private FestcApplication app;

	private CropSelectionPanel cropSelectionPanel;
 
	private MessageCenter msg;
	
	private String baseDir = null;
	private String epicVer = null;

	private EpicSpinupFields fields;
	//private DomainFields domain;
	
	private JComboBox nDepSel;
	private JComboBox runTiledrain;
	private JTextField co2Factor;
 
	public EpicSpinupPanel(FestcApplication application) {
		app = application;
		fields = new EpicSpinupFields();
		app.getProject().addPage(fields);
		msg = app.getMessageCenter();
		baseDir = Constants.getProperty(Constants.EPIC_HOME, msg);
		epicVer = Constants.getProperty(Constants.EPIC_VER, msg).trim();
		app.addPlotListener(this);
		add(createPanel());
	}
	
	public void setMessageCenter(MessageCenter msgCtr) {
		msg = msgCtr;
	}

	private JPanel createPanel() {
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
		JLabel title = new JLabel(Constants.EPIC_SPINUP, SwingConstants.CENTER);
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

		nDepSel = new JComboBox(Constants.SU_NDEPS);
		nDepSel.setSelectedIndex(2);
		nDepSel.setToolTipText("RFN0: get NDep value from EPICCONT.DAT. ");
		 
		co2Factor = new JTextField(20);
		//co2Factor.setToolTipText("Default value is 413.00");
		 
		runTiledrain = new JComboBox(new String[] {"YES", "NO"});
		runTiledrain.setSelectedIndex(1);

		layout.addLabelWidgetPair(Constants.LABEL_EPIC_SCENARIO, scenarioDir, panel);	 
		layout.addLabelWidgetPair("CO2 Level (ppm): ", co2Factor, panel);
		layout.addLabelWidgetPair("Daily Average N Deposition: ", nDepSel, panel);
		layout.addLabelWidgetPair("Run Tiledrain : ", runTiledrain, panel);
		layout.makeCompactGrid(panel, 4, 2, // number of rows and cols
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
			throw new Exception( "Please select scenario dir first!");
		
		String co2Fac = co2Factor.getText();
		if (co2Fac == null || co2Fac.isEmpty()) 
			throw new Exception("co2 Level is not specified!");
		
		String ndepValue = (String) this.nDepSel.getSelectedItem();
		if ( ndepValue == null || ndepValue.isEmpty()) 
			throw new Exception( "Deposition dir is empty, please specify it!");
		
		try {
			Float.parseFloat(co2Fac);
		}catch(NumberFormatException e) {
			throw new Exception("CO2 Level is not a number!");
		}
	
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
				throw new Exception( "crop id is null for crop " + crop);
				 
			Integer cropIrID = cropID +1;
			cropIDs += " " + cropID;
			cropIDs += " " + cropIrID;
		}
		cropIDs += ")";
		
		outMessages += "Epic base: " + baseDir + ls;
		outMessages += "Scen directory: " + scenarioDir + ls;
		
		final String jobFile = writeRunScript(baseDir, scenarioDir, 
				seCropsString, cropIDs, ndepValue);
		String cropNums = getChosenCropNums();
		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);
		final String batchFile;
		if (qcmd != null && !qcmd.trim().isEmpty()) {
			batchFile = writeBatchFile(jobFile, scenarioDir);
		} else {
			batchFile = null;
		}

		Thread populateThread = new Thread(new Runnable() {
			public void run() {
				if (qcmd == null || qcmd.trim().isEmpty()) {
					runScript(jobFile);
				} else {
					runBatchScript(batchFile, jobFile, cropNums);
				}
			}
		});
		populateThread.start();
	}

	
	protected String writeRunScript( String baseDir, String scenarioDir, 
			String cropNames, String cropIDs, String ndepValue) throws Exception {
		
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		
		String file = scenarioDir.trim() + "/scripts";
		if ( !file.endsWith(System.getProperty("file.separator"))) 
				file += System.getProperty("file.separator");
		file += "runEpicSpinup_" + timeStamp + ".csh";
		
		StringBuilder sb = new StringBuilder();
		
		//scriptContent contains all run instructions for direct submit, or job array 
		// submit instructions if using a workload manager 
		String scriptContent = null;
		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);
		if (qcmd == null || qcmd.trim().isEmpty()){
			//no batch system - generate script using legacy code
			sb.append(getScirptHeader());
			sb.append(getEnvironmentDef(baseDir, scenarioDir, ndepValue));
			sb.append(getRunDef(cropNames, cropIDs));
			scriptContent = sb.toString();
		} else {
			//assume batch system that supports job arrays (SLURM, PBS, LSF, etc.)
			//create job array script
			scriptContent = createArrayTaskScript(baseDir, scenarioDir, ndepValue);
		}
		
		writeScriptFile(file, scriptContent);
	    
		return file;
	}
	
	//This is legacy code that has been moved to it's own method
	protected void writeScriptFile( String file, String content) {
			
		String mesg = "";
		
		try {
			File script = new File(file);
			
	        BufferedWriter out = new BufferedWriter(new FileWriter(script));
	        out.write(content);
	        out.close();
	        
	        mesg += "Script file: " + file + ls;
	        boolean ok = script.setExecutable(true, false);
	        mesg += "Set the script file to be executable: ";
	        mesg += ok ? "ok." : "failed.";
	        
	    } catch (IOException e) {
	    	//printStackTrace();
	    	//g.error("Error generating EPIC script file", e);
	    	app.showMessage("Write script", e.getMessage());
	    } 
		
	    app.showMessage("Write script", mesg);
	}
	
	protected String writeBatchFile(String jobFile, String scenarioDir) throws Exception {

		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		String batchFile = scenarioDir.trim() + "/scripts";
		if (!batchFile.endsWith(System.getProperty("file.separator")))
			batchFile += System.getProperty("file.separator");
		batchFile += "submitEpicSpinup_" + timeStamp + ".csh";

		StringBuilder sb = new StringBuilder();
		sb.append("#!/bin/csh" + ls + ls);

		// TODO - add #SBATCH options here

		String qSingModule = Constants.getProperty(Constants.QUEUE_SINGULARITY_MODULE, msg);
		if (qSingModule != null && !qSingModule.trim().isEmpty()) {
			sb.append("module load " + qSingModule + ls);

			String qSingImage = Constants.getProperty(Constants.QUEUE_SINGULARITY_IMAGE, msg);
			String qSingBind = Constants.getProperty(Constants.QUEUE_SINGULARITY_BIND, msg);
			if (qSingImage == null || qSingModule.trim().isEmpty()) {
				throw new Exception("Singularity image path must be specified");
			}
			sb.append("set CONTAINER = " + qSingImage + ls);
			sb.append("singularity exec");
			if (qSingBind != null && !qSingBind.trim().isEmpty()) {
				sb.append(" -B " + qSingBind);
			}
			sb.append(" $CONTAINER " + jobFile);
		} else {
			sb.append(jobFile);
		}

		writeScriptFile(batchFile, sb.toString());

		return batchFile;
	}
	
	// returns comma separated list of chosen crop numbers to run
	private String getChosenCropNums() throws Exception{
		String[] seCrops = cropSelectionPanel.getSelectedCrops();
		if ( seCrops == null || seCrops.length == 0) 
			throw new Exception( "Please select crop(s) first!");
		String crop = null;
		String cropIDs = "";
		for (int i=0; i<seCrops.length; i++) {
			crop = seCrops[i];
			Integer cropID = Constants.CROPS.get(crop);
			if ( cropID == null || cropID <= 0 )
				throw new Exception( "crop id is null for crop " + crop);
			Integer cropIrID = cropID +1;

			if (i!=0){
				cropIDs += "," + cropID;
			} else {
				cropIDs = "" + cropID;
			}
			cropIDs += "," + cropIrID;
		}
		
		return cropIDs;
	}
	
	private String createArrayTaskScript(String baseDir, String scenarioDir, 
			String ndepValue){
		
		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg).toLowerCase();
		String arrayIdEnvVar = "";
		
		if (qcmd.contains("sbatch")){
			//SLURM
			arrayIdEnvVar = "$SLURM_ARRAY_TASK_ID";
		} else if (qcmd.contains("qsub")){
			//PBS
			arrayIdEnvVar = "$PBS_ARRAYID";
		} else if (qcmd.contains("bsub")){
			//LSF
			arrayIdEnvVar = "$LSB_JOBINDEX";
		}
		
		
		StringBuilder sb = new StringBuilder();
		 
		//header
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to run EPIC spinup model job array task" + ls);
		sb.append("#   SLURM example cmd:" + ls);
		sb.append("#     sbatch --job-name=EPICSpinupArrayJob --output=submitEPICSpinup_JobArray_%A_%a.out --array=1,2,31,32 --time=4:00:00" + ls);
		sb.append("#       /PATH_TO_SCRIPT/runEpicSpinup_TIMESTAMP.csh" + ls);
		sb.append("#     where 1,2,31,32 are crop numbers" + ls);
		sb.append("#" + ls);
		sb.append("# Written by: Fortran by Benson, Original Script by IE. 2012" + ls);
		sb.append("# Modified by: EMVL " + ls); 
		sb.append("#" + ls);
		sb.append("# Program: EPICsu.exe" + ls);
		sb.append("#       Needed environment variables included in the script file to run." + ls);        
		sb.append("# " + ls);
		sb.append("#***************************************************************************************" + ls + ls);
		
		//environmental variables
		sb.append(getEnvironmentDef(baseDir, scenarioDir, ndepValue));
		
		//		
		sb.append("set CROPS = (HAY ALFALFA OTHGRASS BARLEY EBEANS CORNG CORNS COTTON OATS PEANUTS POTATOES RICE RYE)" + ls);
		sb.append("set CROPS = ($CROPS SORGHUMG SORGHUMS SOYBEANS SWHEAT WWHEAT OTHER CANOLA BEANS)" + ls);
		
		
		sb.append("setenv type 'spinup'" + ls);
		sb.append("# Set output dir" + ls);
		sb.append("setenv EPIC_CMAQ_OUTPUT $SCEN_DIR/output4CMAQ/$type" + ls);
		sb.append("if ( ! -e $EPIC_CMAQ_OUTPUT  ) mkdir -p $EPIC_CMAQ_OUTPUT" + ls);
		sb.append("if ( ! -e $EPIC_CMAQ_OUTPUT/5years  ) mkdir -p $EPIC_CMAQ_OUTPUT/5years" + ls);
		sb.append("if ( ! -e $EPIC_CMAQ_OUTPUT/daily  ) mkdir -p $EPIC_CMAQ_OUTPUT/daily"  + ls);
		sb.append("if ( ! -e $EPIC_CMAQ_OUTPUT/toCMAQ  ) mkdir -p $EPIC_CMAQ_OUTPUT/toCMAQ" + ls + ls);
		
//		sb.append("setenv CROP_NUM $SLURM_ARRAY_TASK_ID" + ls);
		sb.append("setenv CROP_NUM " + arrayIdEnvVar + ls);
		sb.append("@ rem = $CROP_NUM % 2" + ls);
		sb.append("@ ind  = ($CROP_NUM + $rem) / 2" + ls);
		sb.append("setenv CROP_NAME $CROPS[$ind]" + ls);
		sb.append("setenv CROP_DIR $SCEN_DIR/$CROPS[$ind]" + ls);
		
		sb.append("if ( $CROP_NUM != 0) then" + ls);
		sb.append("  if ( $rem == 1 ) then" + ls);
		sb.append("    set waterSrc = 'rainf'" + ls);
//		sb.append("    set WORK_DIR = $CROP_DIR/$type/rainf" + ls);
		sb.append("  else" + ls);
		sb.append("    set waterSrc = 'irr'" + ls);
//		sb.append("    set WORK_DIR = $CROP_DIR/$type/irr" + ls);
		sb.append("  endif" + ls);
		sb.append("  setenv WORK_DIR  $CROP_DIR/$type/$waterSrc" + ls);
		sb.append("  foreach out ( \"NCM\" \"NCS\" \"DFA\" \"OUT\" \"SOL\" \"TNA\" \"TNS\" )" + ls);
		sb.append("    if ( ! -e $WORK_DIR/$out ) mkdir -p $WORK_DIR/$out" + ls);
		sb.append("  end" + ls);
		sb.append("endif" + ls);
		sb.append(""+ls);
		
		sb.append("echo =======Running Crop $CROP_NAME" + ls);
		sb.append("time $EXEC_DIR/EPICsu.exe" + ls);
		sb.append("if ( $status == 0 ) then" + ls);
//		sb.append("   echo  ==== Finished EPIC spinup run of CROP: $CROP_NAME-$waterSrc-$SLURM_ARRAY_TASK_ID" + ls);
		sb.append("   echo  ==== Finished EPIC spinup run of CROP: $CROP_NAME-$waterSrc-" + arrayIdEnvVar + ls);
		sb.append("else" + ls);
		sb.append("   echo  ==== Error in EPIC spinup run of CROP: $CROP_NAME-$waterSrc-" + arrayIdEnvVar + ls);
		sb.append("echo" + ls);
		sb.append("endif" + ls);
		
		return sb.toString();

	}
	

	private String getScirptHeader() {
		StringBuilder sb = new StringBuilder();
		 
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to run EPIC spinup model" + ls); 
		sb.append("#" + ls);
		sb.append("# Written by: Fortran by Benson, Script by IE. 2012" + ls);
		sb.append("# Modified by: IE " + ls); 
		sb.append("#" + ls);
		sb.append("# Program: EPICsu.exe" + ls);
		sb.append("#       Needed environment variables included in the script file to run." + ls);        
		sb.append("# " + ls);
		sb.append("#***************************************************************************************" + ls + ls);
		
		return sb.toString();
	}

	private String getEnvironmentDef(String baseDir, String scenarioDir, 
			String ndepValue) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(ls + "#" + ls);
		sb.append("# Define environment variables" + ls);
		sb.append("#" + ls + ls);

		sb.append("setenv    EPIC_DIR " + baseDir + ls);
		sb.append("setenv    SCEN_DIR " + scenarioDir + ls);
		sb.append("setenv    COMM_DIR $EPIC_DIR/common_data" +ls);
		sb.append("setenv    SOIL_DIR $COMM_DIR/BaumerSoils" +ls);
		sb.append("setenv    WEAT_DIR $COMM_DIR/statWeath" + ls);
		sb.append("setenv    CO2_FAC  " + co2Factor.getText() + ls);	 
		sb.append("setenv    RUN_TD   " +  (String)runTiledrain.getSelectedItem()  + ls);
		
		//ndepValue = "RFN0";
		String ndepFile = "";
		if ( ndepValue.contains("2002") )  {
			ndepValue = "dailyNDep_2004";
			ndepFile = "ndep_5yrAver_20040101_to_20041231.nc";
		}
		else if ( ndepValue.contains("2010") )  {
			ndepValue = "dailyNDep_2008";
			ndepFile = "ndep_5yrAver_20080101_to_20081231.nc";
		}
		else if ( ndepValue.contains("EPIC") )  ndepValue = "RFN0";

		if ( ndepValue.length() == 4) 
			sb.append("setenv    NDEP_DIR   " + ndepValue + ls);
		else {
//			sb.append("setenv    NDEP_DIR $COMM_DIR/EPIC_model/" 
//					+ ndepValue + ls);
			sb.append("setenv    NDEP_DIR $COMM_DIR/EPIC_model/" 
					+ ls);
			sb.append("setenv    NDEP_INPUT_FILE  " + ndepFile + ls);
		}

		sb.append("setenv    SHARE_DIR $SCEN_DIR/share_data" + ls);
		 
		sb.append("" + ls);
		sb.append("set    EXEC_DIR = " + baseDir + "/model/current" + ls);
		sb.append("" + ls);


		return sb.toString();
	}
	
	private String getRunDef(String cropNames, String cropIDs) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(ls + "#" + ls);
		sb.append("# set input variables" + ls);
		sb.append("set CROPS = " + cropNames + ls);
		sb.append("set CROPSNUM = " + cropIDs + ls);
		sb.append("#" + ls + ls);

		sb.append("# Set output dir" + ls + ls);

		sb.append("setenv type  'spinup' " + ls);
		sb.append("setenv EPIC_CMAQ_OUTPUT  $SCEN_DIR/output4CMAQ/$type" + ls);
		sb.append("if ( ! -e $EPIC_CMAQ_OUTPUT  ) mkdir -p $EPIC_CMAQ_OUTPUT" + ls);
		sb.append("if ( ! -e $EPIC_CMAQ_OUTPUT/5years  ) mkdir -p $EPIC_CMAQ_OUTPUT/5years" + ls);
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
		sb.append("      setenv WORK_DIR   $SCEN_DIR/${CROP_NAME}/$type/rainf" + ls);
		sb.append("      foreach out ( \"NCM\" \"NCS\" \"DFA\" \"OUT\" \"SOL\" \"TNA\" \"TNS\" )" + ls); 
		sb.append("        if ( ! -e $WORK_DIR/$out  ) mkdir -p $WORK_DIR/$out" + ls); 
		sb.append("      end " + ls);
		if (epicVer.equalsIgnoreCase("1102")) sb.append("      time $EXEC_DIR/EPIC1102.exe " + ls);
		else   sb.append("      time $EXEC_DIR/EPICsu.exe " + ls);
		//sb.append("      time $EXEC_DIR/EPICsu.exe " + ls);
		sb.append("      if ( $status == 0 ) then " + ls);
		sb.append("         echo  ==== Finished launching EPIC spinup run of CROP: $CROP_NAME, rainf $cropN" + ls);
		sb.append("      else " + ls);
		sb.append("         echo  ==== Error in launching EPIC spinup run of CROP: $CROP_NAME, rainf $cropN" + ls + ls);
		sb.append("         echo " + ls );
		sb.append("      endif " + ls);
		sb.append("   endif " + ls);
		sb.append("#  " + ls);
		sb.append("# run EPIC model spinup - irrigated   " + ls);
		sb.append("#  " + ls);
		sb.append("   @ n = $n + 1" + ls);
		sb.append("   setenv CROP_NUM $CROPSNUM[$n]" + ls);
		sb.append("   echo $CROP_NAME, $CROP_NUM" + ls);
		sb.append("   " + ls); 
		sb.append("   @ cropN = $CROP_NUM" + ls);
		sb.append("   if ( $cropN != 0 )  then" + ls);
		sb.append("      setenv WORK_DIR   $SCEN_DIR/${CROP_NAME}/$type/irr" + ls); 
		sb.append("      foreach out ( \"NCM\" \"NCS\" \"DFA\" \"OUT\" \"SOL\" \"TNA\" \"TNS\" )" + ls); 
		sb.append("        if ( ! -e $WORK_DIR/$out  ) mkdir -p $WORK_DIR/$out" + ls); 
		sb.append("      end" + ls); 
		if (epicVer.equalsIgnoreCase("1102")) sb.append("      time $EXEC_DIR/EPIC1102.exe " + ls);
		else sb.append("      time $EXEC_DIR/EPICsu.exe " + ls);
		sb.append("      if ( $status == 0 ) then " + ls);
		sb.append("         echo  ==== Finished launching EPIC spinup run of CROP: $CROP_NAME, irr $cropN" + ls);
		sb.append("      else " + ls);
		sb.append("         echo  ==== Error in launching EPIC spinup run of CROP: $CROP_NAME, irr $cropN" + ls + ls);
		sb.append("         echo " + ls );
		sb.append("      endif " + ls);
		sb.append("   endif " + ls);
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
	
	private void runBatchScript(final String batchFile, final String jobFile, final String chosenCrops) {
		String log = jobFile + ".log";

		outMessages += "Batch Script file: " + batchFile + ls;
		outMessages += "Job Script file: " + jobFile + ls;
		outMessages += "Log file: " + log + ls;
		runMessages.setText(outMessages);
		runMessages.validate();

		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg).toLowerCase();
		StringBuilder sb = new StringBuilder();
		String qEpicSpinup = Constants.getProperty(Constants.QUEUE_EPIC_SPINUP, msg);
		
		if (qcmd.contains("sbatch")) {
			// SLURM
			sb.append("sbatch --job-name=EPICSpinupArrayJob --output=runEpicSpinup_JobArray_%A_%a.out --array="
					+ chosenCrops + " " + qEpicSpinup + " " + batchFile + ls);
		} else if (qcmd.contains("qsub")) {
			// PBS
			sb.append("qsub -N EPICSpinupArrayJob -t " + chosenCrops + " " + qEpicSpinup + " " + batchFile + ls);
		} else if (qcmd.contains("bsub")) {
			// LSF
			sb.append("bsub -J EPICSpinupArrayJob[" + chosenCrops + "] " + qEpicSpinup + " " + batchFile + ls);
		}

		FileRunner.runScriptwCmd(batchFile, log, msg, sb.toString());
	}


	public void projectLoaded() {
		fields = (EpicSpinupFields) app.getProject().getPage(fields.getName());
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		if ( fields != null ){
			String scenloc = domain.getScenarioDir();
			if (scenloc != null && scenloc.trim().length()>0 )
				this.scenarioDir.setText(scenloc);
			else 
				this.scenarioDir.setText(fields.getScenarioDir());
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
		nDepSel.setSelectedIndex(1);
		runMessages.setText("");
		co2Factor.setText("380.00");
		runTiledrain.setSelectedIndex(1);
		if ( fields == null ) {
			fields = new EpicSpinupFields();
			app.getProject().addPage(fields);
		}
	}				
	
}

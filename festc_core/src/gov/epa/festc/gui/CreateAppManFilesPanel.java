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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.ManageAppFields;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.FileRunner;
import gov.epa.festc.util.SpringLayoutGenerator;
import simphony.util.messages.MessageCenter;

public class CreateAppManFilesPanel extends UtilFieldsPanel implements PlotEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2056557987311527898L;
	
	private FestcApplication app;
	private MessageCenter msg;
	private ManageAppFields fields;
	//private DomainFields domain;
	 
	private CropSelectionPanel cropSelectionPanel;
	
	public CreateAppManFilesPanel(FestcApplication application) {
		app = application;
		msg = app.getMessageCenter();
		fields = new ManageAppFields();
		app.getProject().addPage(fields);
		app.addPlotListener(this);
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
	

	
	private JPanel getNorthPanel() {
		JPanel panel = new JPanel();
		JLabel title = new JLabel(Constants.MAN_APP, SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		return panel;
	}
	
	private Action runAction() {
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
		
		String sFYear = domain.getCFertYear();
		if (sFYear == null || sFYear.trim().isEmpty()) {
			domain.setCFertYear(fYear);
			sFYear = fYear;
		}	
		 
		else if (sFYear != null && !sFYear.trim().isEmpty() 
				&& !sFYear.endsWith(fYear) && app.allowDiffCheck()) 
			throw new Exception("Current modeling year is " + fYear + " inconsistent with previous one (" + sFYear + ")");
		 
		String seCropsString = cropSelectionPanel.selectedItemTostring();
		String[] seCrops = cropSelectionPanel.getSelectedCrops();
		if ( seCrops == null || seCrops.length == 0) 
			throw new Exception("Please select crop(s) first!");
		 
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
		
		outMessages += ls + "Epic base: " + baseDir + ls;
		outMessages += "Fertlizer year: " + fYear + ls;
		
		final String jobFile = writeRunScript(baseDir, scenarioDir, seCropsString, cropIDs, fYear);
		
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
//		
//		Thread populateThread = new Thread(new Runnable() {
//			public void run() {
//				runScript(file);
//			}
//		});
//		populateThread.start();
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
		String qManApp = Constants.getProperty(Constants.QUEUE_MAN_APP, msg);
		if (qcmd.contains("sbatch")) {
			// SLURM
			sb.append("sbatch --job-name=EPICManAppArrayJob --output=runEpicManApp_JobArray_%A_%a.out --array="
					+ chosenCrops + " " + qManApp + " " + batchFile + ls);
		} else if (qcmd.contains("qsub")) {
			// PBS
			sb.append("qsub -N EPICManAppArrayJob -t " + chosenCrops + " " + qManApp + " " + batchFile + ls);
		} else if (qcmd.contains("bsub")) {
			// LSF
			sb.append("bsub -J EPICManAppArrayJob[" + chosenCrops + "] " + qManApp + " " + batchFile + ls);
		}

		FileRunner.runScriptwCmd(batchFile, log, msg, sb.toString());
	}
	
	protected String writeRunScript( 
			String baseDir, 
			String scenarioDir,
			String cropNames,
			String cropIDs,
			String fYear) throws Exception  {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

		String file = scenarioDir.trim() + "/scripts";
			    
		if ( !file.endsWith(System.getProperty("file.separator"))) 
				file += System.getProperty("file.separator");
		file += "runEPICManApp_" + timeStamp + ".csh";
		
		StringBuilder sb = new StringBuilder();
		
		//scriptContent contains all run instructions for direct submit, or job array 
		// submit instructions if using a workload manager 
		String scriptContent = null;
		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);
		if (qcmd == null || qcmd.trim().isEmpty()){
			//no batch system - generate script using legacy code
			sb.append(getScriptHeader());
			sb.append(getEnvironmentDef(baseDir, scenarioDir, fYear));
			sb.append(getManSu(cropNames, cropIDs, fYear));
			//tile drain script
			sb.append(getRunTD());
			scriptContent = sb.toString();
		} else {
			//assume batch system that supports job arrays (SLURM, PBS, LSF, etc.)
			//create job array script
			scriptContent = createArrayTaskScript(baseDir, scenarioDir, fYear);
		}
		
		writeScriptFile(file, scriptContent);
		
//		String mesg = "";
//		try {
//			StringBuilder sb = new StringBuilder();
//			sb.append(getScriptHeader());
//			sb.append(getEnvironmentDef(baseDir, scenarioDir, fYear));
//			sb.append(getManSu(cropNames, cropIDs));	
//			//tile drain script
//			sb.append(getRunTD());
//
//			File script = new File(file);
//
//	        BufferedWriter out = new BufferedWriter(new FileWriter(script));
//	        out.write(sb.toString());
//	        out.close();
//	        
//	        mesg += "Created a script file: " + file + "\n";
//	        boolean ok = script.setExecutable(true, false);
//	        mesg += "Set the script file to be executable: ";
//	        mesg += ok ? "ok." : "failed.";
//	        
//	    } catch (IOException e) {
//	    	//e.printStackTrace();
//	    	throw new Exception(e.getMessage());
//	    } 
//		
//	    app.showMessage("Write script", mesg);
	    
		return file;
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
//			Integer cropIrID = cropID +1;
			if (i!=0){
				cropIDs += "," + cropID;
			} else {
				cropIDs = "" + cropID;
			}
//			cropIDs += "," + cropIrID;
		}
			
		return cropIDs;
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
		batchFile += "submitEpicManApp" + timeStamp + ".csh";

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
	
	
	private String createArrayTaskScript(String baseDir, String scenarioDir, 
			String fYear){
		
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
		sb.append("# Purpose:  to run management app utility job array task" + ls); 
		sb.append("#   SLURM example cmd:" + ls);
		sb.append("#     sbatch --job-name=EPICManAppArrayJob --output=submitEPICManApp_JobArray_%A_%a.out --array=1,31 --time=2:00:00" + ls);
		sb.append("#       /PATH_TO_SCRIPT/runEPICManApp_TIMESTAMP.csh" + ls);
		sb.append("#     where 1,31 are rainfed (odd) crop numbers only" + ls);
		sb.append("#" + ls);
		sb.append("# Written by: Fortran by Benson, Original Script by IE. 2012" + ls);
		sb.append("# Modified by: EMVL " + ls); 
		sb.append("#" + ls);
		sb.append("# Program: ManGenFERT.exe" + ls);        
		sb.append("# " + ls);
		sb.append("#***************************************************************************************" + ls + ls);
		
		sb.append(getEnvironmentDef(baseDir, scenarioDir, fYear));
		
		sb.append("set CROPS = (HAY ALFALFA OTHGRASS BARLEY EBEANS CORNG CORNS COTTON OATS PEANUTS POTATOES RICE RYE)" + ls );
		sb.append("set CROPS = ($CROPS SORGHUMG SORGHUMS SOYBEANS SWHEAT WWHEAT OTHER CANOLA BEANS)" + ls + ls);
		
		sb.append("setenv EPIC_CMAQ_OUTPUT $SCEN_DIR/output4CMAQ/spinup" + ls + ls);
		sb.append("# Set input variables" + ls);

//		sb.append("@ rem = $SLURM_ARRAY_TASK_ID % 2" + ls);
		sb.append("@ rem = " + arrayIdEnvVar + " % 2" + ls);
//		sb.append("@ ind  = ($SLURM_ARRAY_TASK_ID + $rem) / 2" + ls);
		sb.append("@ ind  = (" + arrayIdEnvVar+ " + $rem) / 2" + ls);
//		sb.append("@ cropRF = $SLURM_ARRAY_TASK_ID" + ls);
		sb.append("@ cropRF = " + arrayIdEnvVar + ls);
//		sb.append("@ cropIR = $SLURM_ARRAY_TASK_ID + 1" + ls);
		sb.append("@ cropIR = " + arrayIdEnvVar + " + 1" + ls);
		sb.append("setenv CROP_NAME $CROPS[$ind]" + ls);
		sb.append("setenv CROP_NUM_RF $cropRF" + ls);
		sb.append("setenv CROP_NUM_IR $cropIR" + ls);

		sb.append("echo $CROP_NAME, $CROP_NUM_RF, $CROP_NUM_IR" + ls);
		sb.append("if ( ! -e $SCEN_DIR/$CROP_NAME/app/manage/OPC )  mkdir -p $SCEN_DIR/$CROP_NAME/app/manage/OPC" + ls);

		sb.append("#" + ls);
		sb.append("echo ==== Begin EPIC management app run of CROP $CROP_NAME" + ls);
		sb.append("#" + ls);
		sb.append("time $EXEC_DIR/ManGenFERT.exe" + ls);
		sb.append("if ( $status == 0 ) then" + ls);
		sb.append("  echo  ==== Finished EPIC management app run of CROP: $CROP_NAME" + ls);
		sb.append("else" + ls);
		sb.append("  echo  ==== Error in EPIC management app run of CROP: $CROP_NAME" + ls);
		sb.append("  echo" + ls);
		sb.append("endif" + ls);


		sb.append("#" + ls);
		sb.append("# Run tile drain" + ls);

		sb.append("if ( ! -e $SCEN_DIR/$CROP_NAME/app/manage/tileDrain )  mkdir -p $SCEN_DIR/$CROP_NAME/app/manage/tileDrain" + ls);
		sb.append("cp $SCEN_DIR/$CROP_NAME/spinup/manage/tileDrain/SOILLISTALLDW.DAT $SCEN_DIR/$CROP_NAME/app/manage/tileDrain/SOILLIST.DAT" + ls);


		
		return sb.toString();

	}

	
	private String getScriptHeader() {
		StringBuilder sb = new StringBuilder();
		 
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to run management application utility" + ls); 
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
		sb.append("setenv    COMM_DIR $EPIC_DIR/common_data" +ls);
		sb.append("setenv    SOIL_DIR $EPIC_DIR/common_data/BaumerSoils" +ls);
		sb.append("setenv    MANG_DIR $EPIC_DIR/common_data/util/manageCreate/" + fYear + ls);
		sb.append("setenv    WEAT_DIR $EPIC_DIR/common_data/statWeath" + ls);
		sb.append("" + ls);
		sb.append("set    EXEC_DIR = " + baseDir + "/util/manageCreate" + ls);
		sb.append("" + ls);

		return sb.toString();
	}
	
	private String getManSu(String cropNames, String cropIDs, String fYear){
		StringBuilder sb = new StringBuilder();
		
		sb.append(ls + "#" + ls);
		sb.append("# set input variables" + ls);
		sb.append("set CROPS = " + cropNames + ls);
		sb.append(" set CROPSNUM =" + cropIDs + ls);
		sb.append(" setenv EPIC_CMAQ_OUTPUT  $SCEN_DIR/output4CMAQ/spinup " + ls);
		sb.append("@ n = 1 " + ls);
		sb.append("foreach crop ($CROPS) " + ls);
		sb.append("   setenv CROP_NAME $crop " + ls);
		sb.append("   setenv CROP_NUM_RF $CROPSNUM[$n]" + ls);
		sb.append("   @ n = $n + 1 " + ls);
		sb.append("   setenv CROP_NUM_IR $CROPSNUM[$n]" + ls);
		sb.append("   echo $CROP_NAME, $CROP_NUM_RF, $CROP_NUM_IR " + ls);
		
		sb.append("   if ( ! -e $SCEN_DIR/$CROP_NAME/app/manage/OPC )  " +
				"mkdir -p $SCEN_DIR/$CROP_NAME/app/manage/OPC " + ls);
		sb.append(ls + "#" + ls);
		sb.append("#  echo ==== Begin EPIC management app run of CROP $CROP_NAME" + ls);
		sb.append("#" + ls );
		if (fYear.contains("2011"))
			sb.append("   time $EXEC_DIR/ManGenFERT2011.exe " + ls );
		else
			sb.append("   time $EXEC_DIR/ManGenFERT.exe " + ls ); 
		sb.append("   if ( $status == 0 ) then " + ls);
		sb.append("      echo  ==== Finished EPIC management app run of CROP: $CROP_NAME" + ls);
		sb.append("   else " + ls);
		sb.append("      echo  ==== Error in EPIC management app run of CROP: $CROP_NAME" + ls + ls);
		sb.append("      echo " + ls );
		sb.append("   endif " + ls);
		sb.append("   @ n = $n + 1 " + ls);
		sb.append("end " + ls ); 
		sb.append(ls);		 	
		return sb.toString();
	}
	
	private String getRunTD(){
		StringBuilder sb = new StringBuilder();
		sb.append(ls + "#" + ls);
		sb.append("# Run tile drain " + ls + ls); 
		 
		sb.append("foreach crop ($CROPS) " + ls);
		sb.append("   setenv CROP_NAME $crop " + ls);
	 
		sb.append("  if ( ! -e $SCEN_DIR/$CROP_NAME/app/manage/tileDrain )  " +
				"mkdir -p $SCEN_DIR/$CROP_NAME/app/manage/tileDrain" + ls);
		sb.append("  cp $SCEN_DIR/$CROP_NAME/spinup/manage/tileDrain/SOILLISTALLDW.DAT " +
				"$SCEN_DIR/$CROP_NAME/app/manage/tileDrain/SOILLIST.DAT" + ls);
		sb.append("end " + ls ); 
		sb.append(ls);		 	
		return sb.toString();
	}
	
	public void newProjectCreated() {
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		scenarioDir.setText(domain.getScenarioDir());
		
		runMessages.setText("");
		if ( fields == null ) {
			fields = new ManageAppFields();
			app.getProject().addPage(fields);
		}
		this.fertYearSel.setSelectedItem(domain.getCFertYear());
	}
	 
	public void projectLoaded() {
		fields = (ManageAppFields) app.getProject().getPage(fields.getName());
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		if ( fields != null ) {
			String scenloc = domain.getScenarioDir();
			if (scenloc != null && scenloc.trim().length()>0 )
				this.scenarioDir.setText(scenloc);
			else 
				this.scenarioDir.setText(fields.getScenarioDir());
			this.fertYearSel.setSelectedItem(fields.getFertYear());
			runMessages.setText(fields.getMessage());
		}else{
			newProjectCreated();
		}
		domain.setCFertYear(null);
	}

	public void saveProjectRequested() {
		if ( scenarioDir != null ) domain.setScenarioDir(scenarioDir.getText());
		if ( scenarioDir != null ) fields.setScenarioDir(scenarioDir.getText());
		if ( fertYearSel != null ) fields.setFertYear((String) fertYearSel.getSelectedItem());
		if ( runMessages != null ) fields.setMessage(runMessages.getText());	
		domain.setCFertYear(null);
	}				
}

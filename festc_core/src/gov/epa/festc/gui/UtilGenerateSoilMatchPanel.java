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

	private JPanel cropsPanel() {

		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		JPanel buttonPanel = new JPanel();
		JButton btn = new JButton(runAction());
		btn.setPreferredSize(new Dimension(100, 50));
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
					// msg.error("ERROR", exc);
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
		if (scenarioDir == null || scenarioDir.isEmpty())
			throw new Exception("Please select scenario dir first!");

		String seCropsString = cropSelectionPanel.selectedItemTostring();
		String[] seCrops = cropSelectionPanel.getSelectedCrops();
		if (seCrops == null || seCrops.length == 0)
			throw new Exception("Please select crop(s) first!");

		outMessages += "Epic base: " + baseDir + ls;
		outMessages += "Scen directory: " + scenarioDir + ls;

		final String jobFile = writeRunScript(baseDir, scenarioDir, seCropsString);
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
		String qEpicSoilMatch = Constants.getProperty(Constants.QUEUE_SOIL_MATCH, msg);
		if (qcmd.contains("sbatch")) {
			// SLURM
			sb.append("sbatch --job-name=EPICSoilMatchArrayJob --output=runEpicSoilMatch_JobArray_%A_%a.out --array="
					+ chosenCrops + " " + qEpicSoilMatch + " " + batchFile + ls);
		} else if (qcmd.contains("qsub")) {
			// PBS
			sb.append("qsub -N EPICSoilMatchArrayJob -t " + chosenCrops + " " + qEpicSoilMatch + " " + batchFile + ls);
		} else if (qcmd.contains("bsub")) {
			// LSF
			sb.append("bsub -J EPICSoilMatchArrayJob[" + chosenCrops + "] " + qEpicSoilMatch + " " + batchFile + ls);
		}

		FileRunner.runScriptwCmd(batchFile, log, msg, sb.toString());
	}

	protected String writeRunScript(String baseDir, String scenarioDir, String cropNames) throws Exception {

		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

		String file = scenarioDir.trim() + "/scripts";
		if (!file.endsWith(System.getProperty("file.separator")))
			file += System.getProperty("file.separator");
		file += "runEpicSoilMatch_" + timeStamp + ".csh";

		StringBuilder sb = new StringBuilder();
		String scriptContent = null;
		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);
		if (qcmd == null || qcmd.trim().isEmpty()) {
			// no batch system
			sb.append(getScirptHeader());
			sb.append(getEnvironmentDef(baseDir, scenarioDir));
			sb.append(getRunDef(cropNames));
			scriptContent = sb.toString();
		} else {
			// assume batch system that supports job arrays (SLURM, LSF, etc)

			// create job array script
			// String taskScriptContent = createArrayTaskScript(baseDir,
			// scenarioDir);
			// String taskFile = scenarioDir.trim() + "/scripts";
			// if (!taskFile.endsWith(System.getProperty("file.separator")))
			// taskFile += System.getProperty("file.separator");
			// taskFile += "runEpicSoilMatchCrop_" + timeStamp + ".csh";
			// writeScriptFile(taskFile, taskScriptContent);
			//
			// scriptContent = createArraySubmitScript(taskFile, scenarioDir);

			scriptContent = createArrayTaskScript(baseDir, scenarioDir);

		}

		// create submit script
		writeScriptFile(file, scriptContent);

		return file;
	}

	// private String createArraySubmitScript(String taskScript, String
	// scenarioDir) throws Exception{
	//
	// StringBuilder sb = new StringBuilder();
	// sb.append("#!/bin/csh -f" + ls);
	// sb.append("#**************************************************************************************"
	// + ls);
	// sb.append("# Purpose: to submit batch job for job arrays" + ls);
	// sb.append("# #" + ls);
	// sb.append("# #" + ls);
	// sb.append("#
	// #***************************************************************************************"
	// + ls);
	// sb.append("# submit job array tasks"+ls);
	//
	// sb.append("set CROPSLIST = " + getChosenCropNums() + ls + ls);
	//
	// String qEpicSoilMatch = Constants.getProperty(Constants.QUEUE_SOIL_MATCH,
	// msg);
	// sb.append("sbatch --job-name=EPICSoilMatchArrayJob
	// --output=submitEPICSoilMatch_JobArray_%A_%a.out --array=$CROPSLIST " +
	// qEpicSoilMatch + " " + taskScript +ls);
	//
	// return sb.toString();
	// }

	private String createArrayTaskScript(String baseDir, String scenarioDir) throws Exception {

		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg).toLowerCase();
		String arrayIdEnvVar = "";

		if (qcmd.contains("sbatch")) {
			// SLURM
			arrayIdEnvVar = "$SLURM_ARRAY_TASK_ID";
		} else if (qcmd.contains("qsub")) {
			// PBS
			arrayIdEnvVar = "$PBS_ARRAYID";
		} else if (qcmd.contains("bsub")) {
			// LSF
			arrayIdEnvVar = "$LSB_JOBINDEX";
		}

		StringBuilder sb = new StringBuilder();

		// header
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to run Soil Match Utility" + ls);
		sb.append("#   SLURM example cmd:" + ls);
		sb.append(
				"#     sbatch --job-name=EPICSoilMatchArrayJob --output=submitEPICSoilMatch_JobArray_%A_%a.out --array=1,31 --time=2:00:00"
						+ ls);
		sb.append("#       /PATH_TO_SCRIPT/runEpicSoilMatch_TIMESTAMP.csh" + ls);
		sb.append("#     where 1,31 are rainfed (odd) crop numbers only" + ls);
		sb.append("#" + ls);
		sb.append("# Written by: Fortran by Benson, Original Script by IE. 2010" + ls);
		sb.append("# Modified by: EMVL " + ls);
		sb.append("#" + ls);
		sb.append("# Program: SOILMATCH*.exe" + ls);
		sb.append("#       Needed environment variables included in the script file to run." + ls);
		sb.append("# " + ls);
		sb.append("#***************************************************************************************" + ls + ls);

		// environmental variables
		sb.append(getEnvironmentDef(baseDir, scenarioDir));

		//
		sb.append("set CROPS = (HAY ALFALFA OTHGRASS BARLEY EBEANS CORNG CORNS COTTON OATS PEANUTS POTATOES RICE RYE)"
				+ ls);
		sb.append("set CROPS = ($CROPS SORGHUMG SORGHUMS SOYBEANS SWHEAT WWHEAT OTHER CANOLA BEANS)" + ls);

		sb.append("# Generate soil match files" + ls + ls);
		sb.append("# set input variables" + ls + ls);

		// sb.append("@ rem = $SLURM_ARRAY_TASK_ID % 2" + ls);
		sb.append("@ rem = " + arrayIdEnvVar + " % 2" + ls);
		// sb.append("@ ind = ($SLURM_ARRAY_TASK_ID + $rem) / 2" + ls + ls);
		sb.append("@ ind = (" + arrayIdEnvVar + " + $rem) / 2" + ls + ls);

		sb.append("setenv CROP_NAME $CROPS[$ind]" + ls + ls);

		sb.append("rm -rf $SCEN_DIR/$CROP_NAME/NONRISOIL*.DAT > & /dev/null" + ls);
		sb.append("rm -rf $SCEN_DIR/$CROP_NAME/SOILSKM*.LOC > & /dev/null" + ls + ls);

		sb.append("echo ==== Begin soil match run for crop $CROP_NAME" + ls);
		sb.append("echo ==== Running step 1 ...." + ls);
		sb.append("time $EXEC_DIR/SOILMATCH1ST.exe" + ls + ls);
		sb.append("echo ==== Running step 2 ...." + ls);
		sb.append("time $EXEC_DIR/SOILMATCH2ND.exe" + ls + ls);
		sb.append("echo ==== Running step 3 ...." + ls);
		sb.append("time $EXEC_DIR/SOILMATCH3RD.exe" + ls + ls);
		sb.append("echo ==== Running step 4 ...." + ls);
		sb.append("time $EXEC_DIR/SOILMATCH4TH.exe" + ls + ls);
		sb.append("echo ==== Running step 5 ...." + ls);
		sb.append("time $EXEC_DIR/SOILMATCH5TH.exe" + ls + ls);
		sb.append("echo ==== Running step 6 ...." + ls);
		sb.append("time $EXEC_DIR/SOILMATCH6TH.exe" + ls + ls);

		sb.append("if ($status == 0 ) then" + ls);
		sb.append("  echo ==== Finished soil match for crop $CROP_NAME." + ls);
		sb.append("else" + ls);
		sb.append("  echo ==status== Error in soil match run for crop $CROP_NAME." + ls);
		sb.append("  exit 1" + ls);
		sb.append("endif" + ls + ls);
		sb.append("echo \" Merging *LOC to SOILLIST.DAT\"" + ls);
		sb.append("cat $SCEN_DIR/$CROP_NAME/*LOC > $SCEN_DIR/$CROP_NAME/SOILLIST.DAT" + ls);

		return sb.toString();

	}

	// This is legacy code that has been moved to it's own method
	protected void writeScriptFile(String file, String content) {

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
			// printStackTrace();
			// g.error("Error generating EPIC script file", e);
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
		batchFile += "submitEpicSoilMatch_" + timeStamp + ".csh";

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
	// This only returns one crop id (rainf), not both rainf and irr variations
	private String getChosenCropNums() throws Exception {
		String[] seCrops = cropSelectionPanel.getSelectedCrops();
		if (seCrops == null || seCrops.length == 0)
			throw new Exception("Please select crop(s) first!");
		String crop = null;
		String cropIDs = "";
		for (int i = 0; i < seCrops.length; i++) {
			crop = seCrops[i];
			Integer cropID = Constants.CROPS.get(crop);
			if (cropID == null || cropID <= 0)
				throw new Exception("crop id is null for crop " + crop);
			Integer cropIrID = cropID + 1;

			if (i != 0) {
				cropIDs += "," + cropID;
			} else {
				cropIDs = "" + cropID;
			}
			// cropIDs += "," + cropIrID;
		}

		return cropIDs;
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
		sb.append("#" + ls);
		sb.append("# set input variables" + ls);
		sb.append("set CROPS = " + cropNames + ls);
		sb.append("foreach crop ($CROPS) " + ls);
		sb.append("   setenv CROP_NAME $crop " + ls);
		sb.append("   rm -rf $SCEN_DIR/$CROP_NAME/NONRISOIL*.DAT >& /dev/null " + ls);
		sb.append("   rm -rf $SCEN_DIR/$CROP_NAME/SOILSKM*.LOC >& /dev/null" + ls + ls);
		sb.append("   echo ==== Begin soil match run for crop $CROP_NAME." + ls);
		sb.append("   echo ==== Running step 1 .... " + ls);
		sb.append("   time $EXEC_DIR/SOILMATCH1ST.exe" + ls);

		sb.append(" " + ls);
		sb.append("   echo ==== Running step 2 .... " + ls);
		sb.append("   time $EXEC_DIR/SOILMATCH2ND.exe" + ls);

		sb.append(" " + ls);
		sb.append("   echo ==== Running step 3 .... " + ls);
		sb.append("   time $EXEC_DIR/SOILMATCH3RD.exe" + ls);

		sb.append(" " + ls);
		sb.append("   echo ==== Running step 4 .... " + ls);
		sb.append("   time $EXEC_DIR/SOILMATCH4TH.exe" + ls);

		sb.append(" " + ls);
		sb.append("   echo ==== Running step 5 .... " + ls);
		sb.append("   time $EXEC_DIR/SOILMATCH5TH.exe" + ls);

		sb.append(" " + ls);
		sb.append("   echo ==== Running step 6 .... " + ls);
		sb.append("   time $EXEC_DIR/SOILMATCH6TH.exe" + ls);

		sb.append("   if ( $status == 0 ) then " + ls);
		sb.append("      echo  ==== Finished soil match run for crop $CROP_NAME. " + ls);
		sb.append("   else " + ls);
		sb.append("      echo  ==status== Error in soil match run for crop $CROP_NAME. " + ls + ls);
		sb.append("      exit 1 " + ls);
		sb.append("   endif " + ls);
		sb.append(" " + ls);
		sb.append("   echo \" Merging *LOC to SOILLIST.DAT\"" + ls);
		sb.append("   cat $SCEN_DIR/$CROP_NAME/*LOC > $SCEN_DIR/$CROP_NAME/SOILLIST.DAT" + ls);
		sb.append("end " + ls);
		sb.append(ls);

		// outMessages += " Inputs: ALL-CULTIVATED10-12-09.LST" + ls;
		// outMessages += " NRI-ALL-HUC8S-ALLCROPS.prn" + ls;
		// outMessages += " HUC8_SITE_INFO-2REV.prn" + ls;
		// outMessages += " NRI-crop-codes-BELD4-codes.prn" + ls;
		// outMessages += " HUC8NRICROPSOIL.DAT" + ls;
		// outMessages += " HUCSITELATLONG.DAT" + ls;
		outMessages += "  Step 1 output: $SCEN_DIR/$CROP  SOILSKM1.LOC" + ls;
		outMessages += "  Step 2 output: $SCEN_DIR/$CROP  SOILSKM2.LOC" + ls;
		outMessages += "  ... ";
		outMessages += "  Final output : $SCEN_DIR/$CROP  *.LOC > SOILLIST.DAT" + ls;

		return sb.toString();
	}

	@Override
	public void projectLoaded() {
		fields = (SoilFilesFields) app.getProject().getPage(fields.getName());
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		if (fields != null) {
			String scenloc = domain.getScenarioDir();
			if (scenloc != null && scenloc.trim().length() > 0)
				this.scenarioDir.setText(scenloc);
			else
				this.scenarioDir.setText(fields.getScenarioDir());
			runMessages.setText(fields.getMessage());
		} else {
			newProjectCreated();
		}

	}

	@Override
	public void saveProjectRequested() {
		if (scenarioDir != null)
			domain.setScenarioDir(scenarioDir.getText());
		if (scenarioDir != null)
			fields.setScenarioDir(scenarioDir.getText());
		if (runMessages != null)
			fields.setMessage(runMessages.getText());
	}

	@Override
	public void newProjectCreated() {
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		scenarioDir.setText(domain.getScenarioDir());
		runMessages.setText("");
		if (fields == null) {
			fields = new SoilFilesFields();
			app.getProject().addPage(fields);
		}
	}
}

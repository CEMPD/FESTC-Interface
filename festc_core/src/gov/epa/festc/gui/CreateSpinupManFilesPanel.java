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
import javax.swing.JCheckBox;
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
	// private DomainFields domain;

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

	private JPanel cropsPanel() {
		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		JPanel buttonPanel = new JPanel();
		JButton btn = new JButton(generateManSpinupAction());
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
					// exc.printStackTrace();
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
			throw new Exception("Please select scenario dir first!");

		String fYear = (String) this.fertYearSel.getSelectedItem();
		if (fYear.trim().isEmpty())
			throw new Exception("Please select fertilizer year!");

		String sFYear = domain.getCFertYear();
		if (sFYear == null || sFYear.trim().isEmpty()) {
			domain.setCFertYear(fYear);
			sFYear = fYear;
		} else if (sFYear != null && !sFYear.trim().isEmpty() && !sFYear.endsWith(fYear) && app.allowDiffCheck())
			throw new Exception(
					"Current land use year " + fYear + " is inconsistent with previous one (" + sFYear + ")");
		// System.out.println("sfYear: " + sFYear + " " + fYear);
		String seCropsString = cropSelectionPanel.selectedItemTostring();
		// System.out.println();
		String[] seCrops = cropSelectionPanel.getSelectedCrops();
		if (seCrops == null || seCrops.length == 0)
			throw new Exception("Please select crop(s) first!");

		outMessages += ls + "Epic base: " + baseDir + ls;
		outMessages += "Scen directory: " + scenarioDir + ls;
		outMessages += "Fertlizer year: " + fYear + ls;
		final String jobFile = writeRunScript(baseDir, scenarioDir, seCropsString, fYear);

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

		// Thread populateThread = new Thread(new Runnable() {
		// public void run() {
		// runScript(file);
		// }
		// });
		// populateThread.start();
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
		String qManSpinup = Constants.getProperty(Constants.QUEUE_MAN_SPINUP, msg);
		if (qcmd.contains("sbatch")) {
			// SLURM
			sb.append("sbatch --job-name=EPICManSpinupArrayJob --output=runEpicManSpinup_JobArray_%A_%a.out --array="
					+ chosenCrops + " " + qManSpinup + " " + batchFile + ls);
		} else if (qcmd.contains("qsub")) {
			// PBS
			sb.append("qsub -N EPICManSpinupArrayJob -t " + chosenCrops + " " + qManSpinup + " " + batchFile + ls);
		} else if (qcmd.contains("bsub")) {
			// LSF
			sb.append("bsub -J EPICManSpinupArrayJob[" + chosenCrops + "] " + qManSpinup + " " + batchFile + ls);
		}

		FileRunner.runScriptwCmd(batchFile, log, msg, sb.toString());
	}

	private String writeRunScript(String baseDir, String scenarioDir, String cropNames, String fYear) throws Exception {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

		String file = scenarioDir.trim() + "/scripts";

		if (!file.endsWith(System.getProperty("file.separator")))
			file += System.getProperty("file.separator");
		file += "runEpicManSpinup_" + timeStamp + ".csh";

		StringBuilder sb = new StringBuilder();

		// scriptContent contains all run instructions for direct submit, or job
		// array
		// submit instructions if using a workload manager
		String scriptContent = null;
		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);
		if (qcmd == null || qcmd.trim().isEmpty()) {
			// no batch system - generate script using legacy code
			sb.append(getScriptHeader());
			sb.append(getEnvironmentDef(baseDir, scenarioDir, fYear));
			sb.append(getManSu(cropNames));
			sb.append(getRunTD(baseDir));
			scriptContent = sb.toString();
		} else {
			// assume batch system that supports job arrays (SLURM, PBS, LSF,
			// etc.)
			// create job array script
			scriptContent = createArrayTaskScript(baseDir, scenarioDir, fYear);
		}

		writeScriptFile(file, scriptContent);
		// String mesg = "";
		// try {
		// StringBuilder sb = new StringBuilder();
		// sb.append(getScriptHeader());
		// sb.append(getEnvironmentDef(baseDir, scenarioDir, fYear));
		// sb.append(getManSu(cropNames));
		//
		// sb.append(getRunTD(baseDir));
		//
		// File script = new File(file);
		//
		// BufferedWriter out = new BufferedWriter(new FileWriter(script));
		// out.write(sb.toString());
		// out.close();
		//
		// mesg += "Script file: " + file + "\n";
		// boolean ok = script.setExecutable(true, false);
		// mesg += "Set the script file to be executable: ";
		// mesg += ok ? "ok." : "failed.";
		//
		// } catch (IOException e) {
		// //e.printStackTrace();
		// //msg.error("Error generating EPIC script file", e);
		// throw new Exception(e.getMessage());
		// }
		//
		// app.showMessage("Write script", mesg);

		return file;
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
		batchFile += "submitEpicManSpinup" + timeStamp + ".csh";

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
			// Integer cropIrID = cropID +1;
			if (i != 0) {
				cropIDs += "," + cropID;
			} else {
				cropIDs = "" + cropID;
			}
			// cropIDs += "," + cropIrID;
		}

		return cropIDs;
	}

	private String createArrayTaskScript(String baseDir, String scenarioDir, String fYear) {

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
		sb.append("# Purpose:  to run management spinup utility job array task" + ls);
		sb.append("#   SLURM example cmd:" + ls);
		sb.append(
				"#     sbatch --job-name=EPICManSpinupArrayJob --output=submitEPICManSpinup_JobArray_%A_%a.out --array=1,31 --time=2:00:00"
						+ ls);
		sb.append("#       /PATH_TO_SCRIPT/runEpicManSpinup_TIMESTAMP.csh" + ls);
		sb.append("#     where 1,31 are rainfed (odd) crop numbers only" + ls);
		sb.append("#" + ls);
		sb.append("# Written by: Fortran by Benson, Original Script by IE. 2012" + ls);
		sb.append("# Modified by: EMVL " + ls);
		sb.append("#" + ls);
		sb.append("# Program: ManGenSU.exe" + ls);
		sb.append("# " + ls);
		sb.append("#***************************************************************************************" + ls + ls);

		sb.append(getEnvironmentDef(baseDir, scenarioDir, fYear));

		sb.append("set CROPS = (HAY ALFALFA OTHGRASS BARLEY EBEANS CORNG CORNS COTTON OATS PEANUTS POTATOES RICE RYE)"
				+ ls);
		sb.append("set CROPS = ($CROPS SORGHUMG SORGHUMS SOYBEANS SWHEAT WWHEAT OTHER CANOLA BEANS)" + ls + ls);

		sb.append("# Set output dir" + ls);

		// sb.append("setenv CROP_NUM $SLURM_ARRAY_TASK_ID" + ls);
		sb.append("setenv CROP_NUM " + arrayIdEnvVar + ls);
		sb.append("@ rem = $CROP_NUM % 2" + ls);
		sb.append("@ ind  = ($CROP_NUM + $rem) / 2" + ls);
		sb.append("setenv CROP_NAME $CROPS[$ind]" + ls);
		sb.append("if ( ! -e $SCEN_DIR/$CROP_NAME/spinup/manage/OPC )  mkdir -p $SCEN_DIR/$CROP_NAME/spinup/manage/OPC"
				+ ls);

		sb.append("#" + ls);
		sb.append("##  Generate management spinup files" + ls);
		sb.append("#" + ls);
		sb.append("#" + ls);
		sb.append("echo ==== Begin EPIC management spinup run of CROP: $CROP_NAME" + ls);
		sb.append("time $EXEC_DIR/ManGenSU.exe" + ls);
		sb.append("if ( $status == 0 ) then" + ls);
		sb.append("  echo ==== Finished EPIC management spinup run of CROP: $CROP_NAME" + ls);
		sb.append("else" + ls);
		sb.append("  echo ==== Error in EPIC management spinup run of CROP: $CROP_NAME" + ls);
		sb.append("endif" + ls);

		sb.append("#" + ls);
		sb.append("## Run tile drain" + ls);

		sb.append("setenv   WORK_DIR $SCEN_DIR/work_dir" + ls);
		sb.append("setenv   COMM_DIR $EPIC_DIR/common_data" + ls);
		sb.append("setenv   TYPE_NAME spinup" + ls);
		sb.append(
				"if ( ! -e $SCEN_DIR/$CROP_NAME/spinup/manage/tileDrain )  mkdir -p $SCEN_DIR/$CROP_NAME/spinup/manage/tileDrain"
						+ ls);
		sb.append("time $EXEC_DIR/soilDrain.exe" + ls);
		sb.append("  if ( $status == 0 ) then" + ls);
		sb.append("    echo  ==== Finished soil drain run for crop $CROP_NAME." + ls);
		sb.append("  else" + ls);
		sb.append("    echo  ==status== Error in soil drain run for crop $CROP_NAME." + ls);
		sb.append("    exit 1" + ls);
		sb.append("  endif" + ls);
		sb.append(
				"mv $SCEN_DIR/$CROP_NAME/spinup/manage/tileDrain/SOILLISTALLSU.DAT  $SCEN_DIR/$CROP_NAME/spinup/manage/tileDrain/SOILLIST.DAT"
						+ ls);

		return sb.toString();

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
		sb.append("setenv    SOIL_DIR $EPIC_DIR/common_data/BaumerSoils" + ls);
		sb.append("setenv    MANG_DIR $EPIC_DIR/common_data/util/manageCreate/" + fYear + ls);
		sb.append("setenv    WEAT_DIR $EPIC_DIR/common_data/statWeath" + ls);

		sb.append("" + ls);
		sb.append("set    EXEC_DIR = " + baseDir + "/util/manageCreate" + ls);
		sb.append("" + ls);

		return sb.toString();
	}

	private String getManSu(String cropNames) {
		StringBuilder sb = new StringBuilder();

		sb.append("#" + ls);
		sb.append("# set input variables" + ls);
		sb.append("set CROPS = " + cropNames + ls);
		sb.append("foreach crop ($CROPS) " + ls);
		sb.append("   setenv CROP_NAME $crop " + ls);
		sb.append("   if ( ! -e $SCEN_DIR/$CROP_NAME/spinup/manage/OPC ) "
				+ " mkdir -p $SCEN_DIR/$CROP_NAME/spinup/manage/OPC " + ls);
		sb.append("#" + ls);
		String exe;
		exe = "$EXEC_DIR/ManGenSU.exe";

		sb.append(ls + "#" + ls);
		sb.append("#  Generate management spinup files " + ls);
		sb.append("#" + ls + ls);
		sb.append("    echo ==== Begin EPIC management spinup run of CROP: $CROP_NAME " + ls);
		sb.append("    time " + exe + ls);
		sb.append("    if ( $status == 0 ) then" + ls);
		sb.append("    echo ==== Finished EPIC management spinup run of CROP: $CROP_NAME" + ls);
		sb.append("    else " + ls);
		sb.append("    echo ==== Error in EPIC management spinup run of CROP: $CROP_NAME" + ls);
		sb.append("    endif " + ls);
		sb.append("end " + ls);
		sb.append(ls);

		return sb.toString();
	}

	private String getRunTD(String baseDir) {
		StringBuilder sb = new StringBuilder();
		sb.append(ls + "#" + ls);

		sb.append("# Run tile drain " + ls + ls);
		sb.append("setenv   WORK_DIR $SCEN_DIR/work_dir" + ls);
		sb.append("setenv   COMM_DIR $EPIC_DIR/common_data" + ls);
		sb.append("setenv   TYPE_NAME spinup" + ls);
		// sb.append("set EXEC_DIR = " + baseDir + "/util/tileDrain" + ls +ls);
		sb.append("foreach crop ($CROPS) " + ls);
		sb.append("   setenv CROP_NAME $crop " + ls);
		sb.append("  if ( ! -e $SCEN_DIR/$CROP_NAME/spinup/manage/tileDrain )  "
				+ "mkdir -p $SCEN_DIR/$CROP_NAME/spinup/manage/tileDrain" + ls);
		sb.append("  time $EXEC_DIR/soilDrain.exe" + ls);
		sb.append("      if ( $status == 0 ) then" + ls);
		sb.append("        echo  ==== Finished soil drain run for crop $CROP_NAME. " + ls);
		sb.append("     else " + ls);
		sb.append("         echo  ==status== Error in soil drain run for crop $CROP_NAME. " + ls);
		sb.append("       exit 1 " + ls);
		sb.append("  endif " + ls);

		sb.append("  mv $SCEN_DIR/$CROP_NAME/spinup/manage/tileDrain/SOILLISTALLSU.DAT  "
				+ "$SCEN_DIR/$CROP_NAME/spinup/manage/tileDrain/SOILLIST.DAT" + ls);

		sb.append("end " + ls);
		sb.append(ls);
		return sb.toString();
	}

	public void projectLoaded() {
		fields = (ManageSpinupFields) app.getProject().getPage(fields.getName());
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		if (fields != null) {
			String scenloc = domain.getScenarioDir();
			if (scenloc != null && scenloc.trim().length() > 0)
				this.scenarioDir.setText(scenloc);
			else
				this.scenarioDir.setText(fields.getScenarioDir());
			fertYearSel.setSelectedItem(fields.getFertYear());
			runMessages.setText(fields.getMessage());
		} else {
			newProjectCreated();
		}
	}

	public void saveProjectRequested() {
		if (scenarioDir != null)
			domain.setScenarioDir(scenarioDir.getText());
		if (scenarioDir != null)
			fields.setScenarioDir(scenarioDir.getText());
		if (fertYearSel != null)
			fields.setFertYear((String) fertYearSel.getSelectedItem());
		if (runMessages != null)
			fields.setMessage(runMessages.getText());
	}

	@Override
	public void newProjectCreated() {
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());

		scenarioDir.setText(domain.getScenarioDir());
		runMessages.setText("");
		if (fields == null) {
			fields = new ManageSpinupFields();
			app.getProject().addPage(fields);
		}
	}
}

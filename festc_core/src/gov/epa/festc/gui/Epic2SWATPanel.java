package gov.epa.festc.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.Epic2SWATFields;
import gov.epa.festc.util.BrowseAction;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.FileRunner;
import gov.epa.festc.util.SpringLayoutGenerator;
import simphony.util.messages.MessageCenter;

public class Epic2SWATPanel extends UtilFieldsPanel implements PlotEventListener {
	private static final long serialVersionUID = 4247819754945274135L;
	private JTextField beld4Dir;
	private JButton beld4FileBrowser;
	private JButton ratioFileBrowser;
	private JButton weathFileBrowser;

	// private JTextField filesPrefix;
	private MessageCenter msg;
	private JTextField metdepFile;
	private JTextField depFile;
	private JTextField ratioFile;
	private JComboBox nDepSel;
	private JComboBox hucSel;
	private JFormattedTextField simYear;

	private FestcApplication app;
	private Epic2SWATFields fields;

	public Epic2SWATPanel(FestcApplication application) {
		app = application;
		msg = app.getMessageCenter();
		fields = new Epic2SWATFields();
		app.getProject().addPage(fields);
		app.addPlotListener(this);
		add(createPanel());
	}

	private JPanel createPanel() {
		init();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(getNorthPanel());
		mainPanel.add(getCenterPanel());
		mainPanel.add(getSouthPanel());
		mainPanel.add(messageBox());
		return mainPanel;
	}

	private JPanel getNorthPanel() {
		JPanel panel = new JPanel();
		JLabel title = new JLabel(Constants.EPIC2SWAT, SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

		return panel;
	}

	private JPanel getSouthPanel() {
		JPanel panel = new JPanel();
		JButton runDaily = new JButton(runDailyAction());
		panel.add(runDaily);
		JButton runNDep = new JButton(runNDepAction());
		panel.add(runNDep);
		JButton runWeather = new JButton(runWeatAction());
		panel.add(runWeather);
		JButton runSWATin = new JButton(runSWATAction());
		panel.add(runSWATin);

		panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

		return panel;
	}

	private JPanel getCenterPanel() {
		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();

		JPanel beld4DirPanel = new JPanel();
		beld4Dir = new JTextField(40);
		beld4Dir.setToolTipText("I.E. share_data/beld4_cmaq12km_2001.nc");
		beld4FileBrowser = new JButton(BrowseAction.browseAction(this, app.getCurrentDir(), "BELD4 file", beld4Dir));
		beld4DirPanel.add(beld4Dir);
		beld4DirPanel.add(beld4FileBrowser);

		JPanel weatherPanel = new JPanel();
		metdepFile = new JTextField(40);
		metdepFile.setToolTipText("Select Site_weather_dep* file");
		weathFileBrowser = new JButton(BrowseAction.browseAction(this, app.getCurrentDir(), "WETDEP file", metdepFile));
		weatherPanel.add(metdepFile);
		weatherPanel.add(weathFileBrowser);

		JPanel ratioPanel = new JPanel();
		ratioFile = new JTextField(40);
		ratioFile.setToolTipText("Huc8 Delivery Ratio file");
		ratioFileBrowser = new JButton(
				BrowseAction.browseAction(this, app.getCurrentDir(), "HUC8 Ratio file", ratioFile));
		ratioPanel.add(ratioFile);
		ratioPanel.add(ratioFileBrowser);

		nDepSel = new JComboBox(Constants.SWAT_NDEPS);
		nDepSel.setSelectedIndex(1);
		nDepSel.setToolTipText("Select CMAQ deposition. ");
		hucSel = new JComboBox(Constants.AREAS);
		hucSel.setSelectedIndex(1);
		hucSel.setToolTipText("Select HUC. ");

		JPanel filesPrefixPanel = new JPanel();
		// filesPrefix = new JTextField(40);
		// filesPrefixPanel.add(filesPrefix);
		//

		// JPanel depField = new JPanel();
		// depFile = new JTextField(40);
		// depField.add(depFile);
		//
		// JPanel ratioField = new JPanel();
		// ratioFile = new JTextField(40);
		// ratioField.add(ratioFile);

		// JPanel startDatePanel = new JPanel();
		// NumberFormat snf = NumberFormat.getNumberInstance();
		// snf.setGroupingUsed(false);
		// startDate = new JFormattedTextField(snf);
		// startDate.setColumns(40);
		// startDatePanel.add(startDate);

		JPanel simYearPanel = new JPanel();
		NumberFormat enf = NumberFormat.getNumberInstance();
		enf.setGroupingUsed(false);
		simYear = new JFormattedTextField(enf);
		simYear.setColumns(20);
		simYearPanel.add(simYear);
		simYear.setEditable(false);

		// layout.addLabelWidgetPair("Grid Description:",
		// getGridDescPanel(false), panel);
		layout.addLabelWidgetPair(Constants.LABEL_EPIC_SCENARIO, scenarioDirP, panel);
		layout.addLabelWidgetPair("BELD4 NetCDF File: ", beld4DirPanel, panel);
		layout.addLabelWidgetPair("Met dep File:", weatherPanel, panel);
		layout.addLabelWidgetPair("HUC8 Delivery Ratio File:", ratioPanel, panel);
		layout.addLabelWidgetPair("Simulation Year:", simYearPanel, panel);

		layout.addLabelWidgetPair("Daily Average N Deposition: ", nDepSel, panel);
		layout.addLabelWidgetPair("Area Selection: ", hucSel, panel);
		// layout.addLabelWidgetPair("EPIC extraction Prefix:",
		// filesPrefixPanel, panel);

		layout.makeCompactGrid(panel, 7, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading

		return panel;
	}

	private Action runDailyAction() {
		return new AbstractAction("EPIC") {
			private static final long serialVersionUID = 5806383737068197305L;

			public void actionPerformed(ActionEvent e) {
				try {
					validateFields();
					String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);
					final String jobFile = writeEpicScript();
					final String batchFile;
					if (qcmd != null && !qcmd.trim().isEmpty()) {
						batchFile = writeBatchFile(jobFile, scenarioDir.getText(), "EPIC");
					} else {
						batchFile = null;
					}

					Thread populateThread = new Thread(new Runnable() {
						public void run() {
							if (qcmd == null || qcmd.trim().isEmpty()) {
								runScript(jobFile);
							} else {
								runBatchScript(batchFile, jobFile);
							}
						}
					});
					populateThread.start();
				} catch (Exception exc) {
					app.showMessage("Run script", exc.getMessage());
				}
			}
		};
	}

	private Action runNDepAction() {
		return new AbstractAction("NDEP") {
			private static final long serialVersionUID = 5806383737068197305L;

			public void actionPerformed(ActionEvent e) {
				try {
					validateFields();
					String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);
					final String jobFile = writeNDepScript();
					final String batchFile;
					if (qcmd != null && !qcmd.trim().isEmpty()) {
						batchFile = writeBatchFile(jobFile, scenarioDir.getText(), "NDEP");
					} else {
						batchFile = null;
					}

					Thread populateThread = new Thread(new Runnable() {
						public void run() {
							if (qcmd == null || qcmd.trim().isEmpty()) {
								runScript(jobFile);
							} else {
								runBatchScript(batchFile, jobFile);
							}
						}
					});
					populateThread.start();
				} catch (Exception exc) {
					app.showMessage("Run script", exc.getMessage());
				}
			}
		};
	}

	private Action runWeatAction() {
		return new AbstractAction("DailyWETH") {
			private static final long serialVersionUID = 5806383737068197305L;

			public void actionPerformed(ActionEvent e) {
				try {
					validateFields();
					String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);
					final String jobFile = writeWeatScript();
					final String batchFile;
					if (qcmd != null && !qcmd.trim().isEmpty()) {
						batchFile = writeBatchFile(jobFile, scenarioDir.getText(), "WETH");
					} else {
						batchFile = null;
					}

					Thread populateThread = new Thread(new Runnable() {
						public void run() {
							if (qcmd == null || qcmd.trim().isEmpty()) {
								runScript(jobFile);
							} else {
								runBatchScript(batchFile, jobFile);
							}
						}
					});
					populateThread.start();
				} catch (Exception exc) {
					app.showMessage("Run script", exc.getMessage());
				}
			}
		};
	}

	private Action runSWATAction() {
		return new AbstractAction("SWAT INPUTS") {
			private static final long serialVersionUID = 5806383737068197305L;

			public void actionPerformed(ActionEvent e) {
				try {
					validateFields();
					String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);
					final String jobFile = writeSWATScript();
					final String batchFile;
					if (qcmd != null && !qcmd.trim().isEmpty()) {
						batchFile = writeBatchFile(jobFile, scenarioDir.getText(), "SWAT");
					} else {
						batchFile = null;
					}

					Thread populateThread = new Thread(new Runnable() {
						public void run() {
							if (qcmd == null || qcmd.trim().isEmpty()) {
								runScript(jobFile);
							} else {
								runBatchScript(batchFile, jobFile);
							}
						}
					});
					populateThread.start();
				} catch (Exception exc) {
					app.showMessage("Run script", exc.getMessage());
				}
			}
		};
	}

	private void validateFields() throws Exception {

		String sahome = Constants.getProperty(Constants.SA_HOME, msg);
		if (sahome == null || sahome.trim().isEmpty() || !(new File(sahome).exists()))
			throw new Exception("Error loading spacial allocator home:" + sahome + " doesn't exist");

		String scenarioDir = this.scenarioDir.getText();
		if (scenarioDir == null || scenarioDir.isEmpty())
			throw new Exception("Please select scenario dir first!");

		String sYear = simYear.getText();
		if (sYear == null || sYear.trim().isEmpty())
			throw new Exception("Simulation Year field is empty.");
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

	protected String writeBatchFile(String jobFile, String scenarioDir, String desc) throws Exception {

		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		String batchFile = scenarioDir.trim() + "/scripts";
		if (!batchFile.endsWith(System.getProperty("file.separator")))
			batchFile += System.getProperty("file.separator");
		batchFile += "submitEpic2CMAQ_" + desc + "_" + timeStamp + ".csh";

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

	private String writeEpicScript() throws Exception {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

		String baseDir = Constants.getProperty(Constants.EPIC_HOME, msg);
		String scenarioDir = this.scenarioDir.getText();
		File beld4F = new File(this.beld4Dir.getText());
		if (beld4F == null || beld4F.isDirectory() || !beld4F.exists())
			throw new Exception("Beld4 file is not existing!");

		outMessages += "Epic base: " + baseDir + ls;

		String file = scenarioDir.trim() + "/scripts";

		file = file.trim() + "/epic2swat_extract_dailyEpic_" + timeStamp + ".csh";
		// outMessages += file + ls;

		StringBuilder sb = new StringBuilder();
		sb.append(getEpicScriptHeader());
		String ls = "\n";

		sb.append("#" + ls);
		sb.append("# Set up runtime environment" + ls);
		sb.append("#" + ls);

		sb.append("setenv    EPIC_DIR  " + baseDir + ls);
		sb.append("setenv    SCEN_DIR  " + scenarioDir + ls);
		sb.append("setenv    SHARE_DIR  " + scenarioDir + "/share_data" + ls);
		sb.append("setenv    SIM_YEAR  " + simYear.getText() + ls + ls);

		sb.append("# Get site infomation" + ls);
		sb.append("setenv    SITE_FILE   ${SHARE_DIR}/EPICSites_Info.csv" + ls + ls);
		sb.append("# Define BELD4 input file, get crop fractions " + ls);
		sb.append("setenv DOMAIN_BELD4_NETCDF " + beld4Dir.getText() + ls + ls);

		sb.append("# EPIC input location" + ls);
		sb.append("setenv DAY_DIR   $SCEN_DIR/output4CMAQ/app/daily" + ls + ls);
		sb.append("# SWAT output location" + ls);
		sb.append("setenv OUTDIR   $SCEN_DIR/output4SWAT/dailyEPIC" + ls);
		sb.append("if ( ! -e $OUTDIR/county ) mkdir -p $OUTDIR/county" + ls);
		sb.append("if ( ! -e $OUTDIR/state ) mkdir -p $OUTDIR/state" + ls);
		sb.append("if ( ! -e $OUTDIR/domain ) mkdir -p $OUTDIR/domain" + ls);
		sb.append("if ( ! -e $OUTDIR/HUC8 ) mkdir -p $OUTDIR/HUC8" + ls);
		sb.append("if ( ! -e $OUTDIR/HUC6 ) mkdir -p $OUTDIR/HUC6" + ls);
		sb.append("if ( ! -e $OUTDIR/HUC2 ) mkdir -p $OUTDIR/HUC2" + ls + ls);

		// sb.append("setenv OUTFILE_PREFIX $OUTDIR/" + filesPrefix.getText() +
		// ls + ls);
		sb.append("setenv REGION " + hucSel.getSelectedItem() + ls + ls);

		// sb.append("cd $EPIC_DIR/util/swat/" + ls );
		sb.append("echo 'Run EPIC daily summary for swat: ' " + scenarioDir + ls);

		sb.append("R CMD BATCH --no-save --slave " + "$EPIC_DIR/util/swat/epic2swat_extract_dailyEPIC.R "
				+ "${SCEN_DIR}/scripts/epic2swat_extract_dailyEPIC.log" + ls + ls);

		String mesg = "";
		try {
			File script = new File(file);
			Runtime.getRuntime().exec("chmod 755 " + script.getAbsolutePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(script));
			out.write(sb.toString());
			out.close();
			mesg += "Created a script file: " + file + "\n";
			boolean ok = script.setExecutable(true, false);
			mesg += "Set the script file to be executable: ";
			mesg += ok ? "ok." : "failed.";
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		app.showMessage("Write script", mesg);
		return file;
	}

	private String writeNDepScript() throws Exception {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

		String baseDir = Constants.getProperty(Constants.EPIC_HOME, msg);
		String scenarioDir = this.scenarioDir.getText();

		String ndepSelection = (String) nDepSel.getSelectedItem();
		String ndepType = ndepSelection;
		if (ndepSelection.contains("2002"))
			ndepType = "dailyNDep_2004";
		else if (ndepSelection.contains("2010"))
			ndepType = "dailyNDep_2008";

		File beld4F = new File(this.beld4Dir.getText());
		if (beld4F == null || beld4F.isDirectory() || !beld4F.exists())
			throw new Exception("Beld4 file is not existing!");

		outMessages += "Epic base: " + baseDir + ls;

		String file = scenarioDir.trim() + "/scripts";
		// String year = domain.getSimYear();

		file = file.trim() + "/epic2swat_extract_NDEP_" + timeStamp + ".csh";
		// outMessages += file + ls;

		StringBuilder sb = new StringBuilder();
		sb.append(getNdepScriptHeader());
		String ls = "\n";

		sb.append("#" + ls);
		sb.append("# Set up runtime environment" + ls);
		sb.append("#" + ls);

		sb.append("setenv    EPIC_DIR  " + baseDir + ls);
		sb.append("setenv    SCEN_DIR  " + scenarioDir + ls);
		sb.append("setenv    SHARE_DIR  " + scenarioDir + "/share_data" + ls + ls);
		sb.append("# Get site infomation" + ls);
		sb.append("setenv    SITE_FILE   ${SHARE_DIR}/allSites_Info.csv" + ls + ls);

		sb.append("# Define BELD4 input file" + ls);
		sb.append("setenv DOMAIN_BELD4_NETCDF " + beld4Dir.getText() + ls + ls);
		sb.append("# Location of deposition files " + ls);

		sb.append("setenv    NDEP_TYPE " + ndepType + ls);
		sb.append("setenv    NDEP_FILE     " + metdepFile.getText() + ls);

		sb.append("# output location" + ls);
		sb.append("setenv OUTDIR   $SCEN_DIR/output4SWAT/NDEP/" + ndepType + ls);
		sb.append("if ( ! -e $OUTDIR) mkdir -p $OUTDIR" + ls);
		sb.append("if ( ! -e $OUTDIR/county ) mkdir -p $OUTDIR/county" + ls);
		sb.append("if ( ! -e $OUTDIR/state ) mkdir -p $OUTDIR/state" + ls);
		sb.append("if ( ! -e $OUTDIR/domain ) mkdir -p $OUTDIR/domain" + ls);
		sb.append("if ( ! -e $OUTDIR/HUC8 ) mkdir -p $OUTDIR/HUC8" + ls);
		sb.append("if ( ! -e $OUTDIR/HUC6 ) mkdir -p $OUTDIR/HUC6" + ls);
		sb.append("if ( ! -e $OUTDIR/HUC2 ) mkdir -p $OUTDIR/HUC2" + ls + ls);

		// sb.append("setenv REGION " + hucSel.getSelectedItem() + ls);
		// sb.append("cd $EPIC_DIR/util/swat/" + ls );
		sb.append("echo 'Extract daily depositon from yearly CMAQ or 2004/2008 averaged ndep: '" + scenarioDir + ls);
		sb.append("R CMD BATCH --no-save --slave " + "$EPIC_DIR/util/swat/epic2swat_extract_daily_ndepCMAQ.R "
				+ "${SCEN_DIR}/scripts/epic2swat_extract_daily_ndepCMAQ.log" + ls + ls);

		String mesg = "";
		try {
			File script = new File(file);
			Runtime.getRuntime().exec("chmod 755 " + script.getAbsolutePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(script));
			out.write(sb.toString());
			out.close();
			mesg += "Created a script file: " + file + "\n";
			boolean ok = script.setExecutable(true, false);
			mesg += "Set the script file to be executable: ";
			mesg += ok ? "ok." : "failed.";
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		app.showMessage("Write script", mesg);
		return file;
	}

	private String writeSWATScript() throws Exception {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

		String baseDir = Constants.getProperty(Constants.EPIC_HOME, msg);
		String scenarioDir = this.scenarioDir.getText();

		String ndepSelection = (String) nDepSel.getSelectedItem();
		String ndepType = ndepSelection;
		if (ndepSelection.contains("2002"))
			ndepType = "dailyNDep_2004";
		else if (ndepSelection.contains("2010"))
			ndepType = "dailyNDep_2008";

		File ratioF = new File(this.ratioFile.getText());
		if (ratioF == null || ratioF.isDirectory() || !ratioF.exists())
			throw new Exception("Ratio file is not existing!");
		File beld4F = new File(this.beld4Dir.getText());
		if (beld4F == null || beld4F.isDirectory() || !beld4F.exists())
			throw new Exception("Beld4 file is not existing!");

		outMessages += "Epic base: " + baseDir + ls;

		String file = scenarioDir.trim() + "/scripts";
		// String year = domain.getSimYear();

		file = file.trim() + "/extract_swatInputs_" + timeStamp + ".csh";
		// outMessages += file + ls;

		StringBuilder sb = new StringBuilder();
		sb.append(getSWATScriptHeader());
		String ls = "\n";

		sb.append("#" + ls);
		sb.append("# Set up runtime environment" + ls);
		sb.append("#" + ls);

		sb.append("setenv    EPIC_DIR   " + baseDir + ls);
		sb.append("setenv    SCEN_DIR   " + scenarioDir + ls);
		sb.append("setenv    SHARE_DIR  " + scenarioDir + "/share_data" + ls + ls);
		sb.append("setenv    NDEP_TYPE  " + ndepType + ls);
		sb.append("setenv    SIM_YEAR  " + simYear.getText() + ls + ls);
		sb.append("setenv    RUN_dailyEPIC  YES" + ls);
		sb.append("setenv    RUN_MET   YES" + ls);
		sb.append("setenv    RUN_NDEP  YES" + ls);

		sb.append("# Get site infomation" + ls);
		sb.append("setenv    SITE_FILE   ${SHARE_DIR}/AllSites_Info.csv" + ls + ls);
		sb.append("# Define ratio input file" + ls);
		sb.append("setenv    RATIO_FILE  " + ratioF + ls + ls);

		sb.append("# output location" + ls);
		sb.append("setenv OUTDIR   $SCEN_DIR/output4SWAT" + ls);
		sb.append("setenv SWAT_OUTDIR   $SCEN_DIR/output4SWAT/swat_inputs" + ls);
		sb.append("if ( ! -e $OUTDIR) mkdir -p $OUTDIR" + ls);
		sb.append("if ( ! -e $SWAT_OUTDIR ) mkdir -p $SWAT_OUTDIR" + ls);
		sb.append("if ( ! -e $SWAT_OUTDIR/dailydep ) mkdir -p $SWAT_OUTDIR/dailydep" + ls);
		sb.append("if ( ! -e $SWAT_OUTDIR/dailyweath ) mkdir -p $SWAT_OUTDIR/dailyweath" + ls);
		sb.append("if ( ! -e $SWAT_OUTDIR/EPICinputPoint ) mkdir -p $SWAT_OUTDIR/EPICinputPoint" + ls);

		sb.append("echo  'Extract swat inputs:  ' $SCEN_DIR" + ls);
		sb.append("R CMD BATCH --no-save --slave " + "$EPIC_DIR/util/swat/extract_swatInputs.R "
				+ "${SCEN_DIR}/scripts/extract_swatInputs_" + ndepType + ".log" + ls + ls);

		String mesg = "";
		try {
			File script = new File(file);
			Runtime.getRuntime().exec("chmod 755 " + script.getAbsolutePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(script));
			out.write(sb.toString());
			out.close();
			mesg += "Created a script file: " + file + "\n";
			boolean ok = script.setExecutable(true, false);
			mesg += "Set the script file to be executable: ";
			mesg += ok ? "ok." : "failed.";
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		app.showMessage("Write script", mesg);
		return file;
	}

	private String writeWeatScript() throws Exception {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

		String baseDir = Constants.getProperty(Constants.EPIC_HOME, msg);
		String scenarioDir = this.scenarioDir.getText();
		File beld4F = new File(this.beld4Dir.getText());
		if (beld4F == null || beld4F.isDirectory() || !beld4F.exists())
			throw new Exception("Beld4 file is not existing!");

		String ndepSelection = (String) nDepSel.getSelectedItem();
		String ndepType = ndepSelection;
		if (ndepSelection.contains("2002"))
			ndepType = "dailyNDep_2004";
		else if (ndepSelection.contains("2010"))
			ndepType = "dailyNDep_2008";

		outMessages += "Epic base: " + baseDir + ls;

		String file = scenarioDir.trim() + "/scripts";
		// String year = domain.getSimYear();

		file = file.trim() + "/epic2swat_daily_metCMAQ_" + timeStamp + ".csh";
		// outMessages += file + ls;

		StringBuilder sb = new StringBuilder();
		sb.append(getWeatScriptHeader());
		String ls = "\n";

		sb.append("#" + ls);
		sb.append("# Set up runtime environment" + ls);
		sb.append("#" + ls);

		sb.append("setenv    EPIC_DIR  " + baseDir + ls);
		sb.append("setenv    SCEN_DIR  " + scenarioDir + ls);
		sb.append("setenv    SHARE_DIR  " + scenarioDir + "/share_data" + ls + ls);
		sb.append("# Get site infomation" + ls);
		sb.append("setenv    SITE_FILE   ${SHARE_DIR}/allSites_Info.csv" + ls + ls);
		sb.append("# Define BELD4 input file, get crop fractions " + ls);
		sb.append("setenv DOMAIN_BELD4_NETCDF " + beld4Dir.getText() + ls + ls);

		sb.append("# met yearly file location" + ls);
		sb.append("setenv    NDEP_TYPE " + ndepType + ls);
		sb.append("setenv    DEPMET_FILE  " + metdepFile.getText() + ls + ls);

		sb.append("# output location" + ls);
		sb.append("setenv OUTDIR   $SCEN_DIR/output4SWAT/dailyWETH/" + ndepType + ls);
		sb.append("if ( ! -e $OUTDIR) mkdir -p $OUTDIR" + ls);
		sb.append("if ( ! -e $OUTDIR/county ) mkdir -p $OUTDIR/county" + ls);
		sb.append("if ( ! -e $OUTDIR/state ) mkdir -p $OUTDIR/state" + ls);
		sb.append("if ( ! -e $OUTDIR/domain ) mkdir -p $OUTDIR/domain" + ls);
		sb.append("if ( ! -e $OUTDIR/HUC8 ) mkdir -p $OUTDIR/HUC8" + ls);
		sb.append("if ( ! -e $OUTDIR/HUC6 ) mkdir -p $OUTDIR/HUC6" + ls);
		sb.append("if ( ! -e $OUTDIR/HUC2 ) mkdir -p $OUTDIR/HUC2" + ls + ls);

		sb.append("setenv REGION " + hucSel.getSelectedItem() + ls + ls);
		// sb.append("cd $EPIC_DIR/util/swat/" + ls );
		sb.append("echo 'Extract daily met/dep for SWAT from ' " + scenarioDir + ls);
		sb.append("R CMD BATCH --no-save --slave " + "$EPIC_DIR/util/swat/epic2swat_extract_daily_metCMAQ.R "
				+ "${SCEN_DIR}/scripts/epic2swat_extract_daily_metCMAQ.log" + ls + ls);

		String mesg = "";
		try {
			File script = new File(file);
			Runtime.getRuntime().exec("chmod 755 " + script.getAbsolutePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(script));
			out.write(sb.toString());
			out.close();
			mesg += "Created a script file: " + file + "\n";
			boolean ok = script.setExecutable(true, false);
			mesg += "Set the script file to be executable: ";
			mesg += ok ? "ok." : "failed.";
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		app.showMessage("Write script", mesg);
		return file;
	}

	private String getEpicScriptHeader() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:   Prepare runoff inputs for SWAT by extracting " + ls);
		sb.append("#           EPIC daily output files  output4CMAQ/app/daily/*NCD " + ls);
		sb.append("#" + ls);
		sb.append("#" + ls);
		sb.append("# Developed by: UNC Institute for the Environment" + ls);
		sb.append("# Date: 10/30/2018" + ls);
		sb.append("#" + ls);
		sb.append("# Program: $EPIC_DIR/util/swat/epic2swat_extract_daily_epic.R" + ls);
		sb.append("#" + ls);
		sb.append("#***************************************************************************************" + ls + ls);

		return sb.toString();
	}

	private String getNdepScriptHeader() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:   repare N Deposition inputs for SWAT by summarizing met data,  " + ls);
		sb.append("#           netcdf weather data under ${SHAREDIR}/ " + ls);
		sb.append("#           $COMMON_data/EPIC_model/dailyNDep_200? " + ls);
		sb.append("#" + ls);
		sb.append("#" + ls);
		sb.append("# Developed by: UNC Institute for the Environment" + ls);
		sb.append("# Date: 10/30/2017" + ls);
		sb.append("#" + ls);
		sb.append("# Program: $EPIC_DIR/util/swat/epic2swat_extract_daily_ndep.R" + ls);
		sb.append("#" + ls);
		sb.append("#***************************************************************************************" + ls + ls);

		return sb.toString();
	}

	private String getWeatScriptHeader() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:   Prepare N Deposition and weather inputs for SWAT by summarizing  " + ls);
		sb.append("#           netcdf weather data under ${SHAREDIR}/ " + ls);
		sb.append("#           $COMMON_data/EPIC_model/dailyNDep_200?/ " + ls);
		sb.append("#" + ls);
		sb.append("#" + ls);
		sb.append("# Developed by: UNC Institute for the Environment" + ls);
		sb.append("# Date: 10/30/2017" + ls);
		sb.append("#" + ls);
		sb.append("# Program: $EPIC_DIR/util/swat/epic2swat_daily_depWETHnc.R" + ls);
		sb.append("#" + ls);
		sb.append("#***************************************************************************************" + ls + ls);

		return sb.toString();
	}

	private String getSWATScriptHeader() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:   Prepare swat inputs: dailyEPIC, NDEP, and weather" + ls);
		sb.append("#" + ls);
		sb.append("# Developed by: UNC Institute for the Environment" + ls);
		sb.append("# Date: 10/30/2017" + ls);
		sb.append("#" + ls);
		sb.append("# Program: $EPIC_DIR/util/swat/extract_swatInputs.R" + ls);
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

	private void runBatchScript(final String batchFile, final String jobFile) {
		String log = jobFile + ".log";

		outMessages += "Batch Script file: " + batchFile + ls;
		outMessages += "Job Script file: " + jobFile + ls;
		outMessages += "Log file: " + log + ls;
		runMessages.setText(outMessages);
		runMessages.validate();

		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg).toLowerCase();
		StringBuilder sb = new StringBuilder();
		String qEpic2Swat = Constants.getProperty(Constants.QUEUE_EPIC2SWAT, msg);

		File script = new File(batchFile.replaceAll("\\\\", "\\\\\\\\"));

		sb.append(qcmd + " " + qEpic2Swat + " -o " + log + " " + script.getAbsolutePath());

		FileRunner.runScriptwCmd(batchFile, log, msg, sb.toString());
	}

	@Override
	public void projectLoaded() {
		fields = (Epic2SWATFields) app.getProject().getPage(fields.getName());
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		if (fields != null) {
			fields = (Epic2SWATFields) app.getProject().getPage(fields.getName());
			String scenloc = domain.getScenarioDir();
			if (scenloc != null && scenloc.trim().length() > 0)
				this.scenarioDir.setText(scenloc);
			else
				this.scenarioDir.setText(fields.getScenarioDir());
			//
			try {
				simYear.setValue(
						NumberFormat.getNumberInstance().parse(domain.getSimYear() == null ? "" : domain.getSimYear()));

			} catch (ParseException e) {
				simYear.setValue(0);
			}
			String year = simYear.getText();
			if (fields.getMetdep() == null) {
				String depmet = domain.getScenarioDir().trim() + "/share_data/site_weather_dep_" + year + "0101"
						+ "_to_" + year + "1231" + ".nc";
				this.metdepFile.setText(depmet);
			} else
				this.metdepFile.setText(fields.getMetdep());
			beld4Dir.setText(fields.getBeld4ncf());
			hucSel.setSelectedItem(fields.getHucSelection());
			nDepSel.setSelectedItem(fields.getNDepSelection());
			ratioFile.setText(fields.getRatioFile());
			// filesPrefix.setText(fields.getOutfileprefix());
			runMessages.setText(fields.getMessage());
		} else {
			newProjectCreated();
		}
	}

	@Override
	public void saveProjectRequested() {
		fields.setBeld4ncf(beld4Dir.getText());
		fields.setMetdep(metdepFile.getText());
		fields.setHucSelection((String) hucSel.getSelectedItem());
		fields.setNDepSelection((String) nDepSel.getSelectedItem());
		fields.setScenarioDir(scenarioDir.getText());
		if (ratioFile != null)
			fields.setRatioFile(ratioFile.getText() == null ? "" : ratioFile.getText());
		if (runMessages != null)
			fields.setMessage(runMessages.getText());
	}

	@Override
	public void newProjectCreated() {
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		String scenDir = domain.getScenarioDir().trim();
		scenarioDir.setText(domain.getScenarioDir());
		String year = domain.getNlcdYear().trim();
		// simYear.setValue(year);
		// filesPrefix.setText("");
		// String beld4file = fields.getBeld4ncf();
		// if ( beld4file == null || beld4file.trim().isEmpty() )
		String beld4file = scenDir + "/share_data/beld4_" + domain.getGridName() + "_" + year + ".nc";
		this.beld4Dir.setText(beld4file);
		String depmet = scenDir + "/share_data/site_weather_dep_" + year + "0101" + "_to_" + year + "1231" + ".nc";
		this.metdepFile.setText(depmet);
		nDepSel.setSelectedIndex(0);
		hucSel.setSelectedIndex(0);

		runMessages.setText("");
		ratioFile.setText("");
		// filesPrefix.setText("");
		try {
			simYear.setValue(NumberFormat.getNumberInstance().parse(year == null ? "" : domain.getSimYear()));
		} catch (ParseException e) {
			simYear.setValue(0);
		}

		// if ( fields == null ) {
		// fields = new Epic2SWATFields();
		app.getProject().addPage(fields);
		// }
	}

}

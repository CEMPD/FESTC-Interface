package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.Beld4DataGenFields;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.EpicYearlyAverage2CMAQFields;
import gov.epa.festc.util.BrowseAction;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.FileRunner;
import gov.epa.festc.util.SpringLayoutGenerator;

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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import simphony.util.messages.MessageCenter;

public class EpicYearlyAverage2CMAQPanel extends UtilFieldsPanel implements PlotEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MessageCenter msg;
	private FestcApplication app;
	private static final String indent = "            ";
	private EpicYearlyAverage2CMAQFields fields;

	private JRadioButton applicationBtn, spinupBtn;
	private boolean spinup = false;
	private JTextField beld4Dir;
	private JButton beld4DirBrowser;
	private String ls = "\n";
	// private JCheckBox swatDayBox;

	public EpicYearlyAverage2CMAQPanel(FestcApplication festcApp) {
		app = festcApp;
		msg = app.getMessageCenter();
		fields = new EpicYearlyAverage2CMAQFields();
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
		JLabel title = new JLabel(Constants.EPIC_YEAR, // Yearly Average for
														// CMAQ",
				SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

		return panel;
	}

	private JPanel getCenterPanel() {
		JPanel spinupPanel = new JPanel();
		this.applicationBtn = new JRadioButton(applicationSelection());
		this.spinupBtn = new JRadioButton(spinupSelection());
		this.applicationBtn.setSelected(true);
		this.spinupBtn.setSelected(false);
		spinupPanel.add(this.spinupBtn);
		spinupPanel.add(this.applicationBtn);

		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(this.spinupBtn);
		btnGroup.add(this.applicationBtn);

		JPanel beld4DirPanel = new JPanel();
		beld4Dir = new JTextField(40);
		beld4Dir.setToolTipText("I.E. share_data/beld4_cmaq12km_2001.nc");

		beld4DirBrowser = new JButton(BrowseAction.browseAction(this, app.getCurrentDir(), "BELD4 file", beld4Dir));
		beld4DirPanel.add(beld4Dir);
		beld4DirPanel.add(beld4DirBrowser);

		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();

		// JPanel swatPanel = new JPanel();
		// this.swatDayBox = new JCheckBox("Daily", true);
		// swatPanel.add(this.swatDayBox);

		layout.addLabelWidgetPair("Grid Description:", getGridDescPanel(false), panel);
		layout.addLabelWidgetPair(Constants.LABEL_EPIC_SCENARIO, scenarioDirP, panel);
		layout.addLabelWidgetPair("BELD4 NetCDF File: ", beld4DirPanel, panel);
		// layout.addLabelWidgetPair(" ", new JLabel(" "), panel);
		layout.addLabelWidgetPair(indent + "Output Type:", spinupPanel, panel);
		// layout.addLabelWidgetPair(" ", new JLabel(" "), panel);
		// layout.addLabelWidgetPair(" For Swat Inputs:", swatPanel, panel);
		// layout.addLabelWidgetPair(" ", new JLabel(" "), panel);

		layout.makeCompactGrid(panel, 4, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading

		return panel;
	}

	private Action applicationSelection() {
		return new AbstractAction("EPIC APP") {
			public void actionPerformed(ActionEvent e) {
				processSelec();
			}

		};
	}

	private Action spinupSelection() {
		return new AbstractAction("EPIC SPINUP") {
			public void actionPerformed(ActionEvent e) {
				processSelec();
			}

		};
	}

	private void processSelec() {
		if (applicationBtn.isSelected()) {
			spinup = false;
		} else {
			spinup = true;
		}
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
					app.showMessage("Write script", exc.getMessage());
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

		File beld4F = new File(this.beld4Dir.getText());
		if (beld4F == null || beld4F.isDirectory() || !beld4F.exists())
			throw new Exception("Beld4 file is not existing!");

		validateGrids();

		String sahome = Constants.getProperty(Constants.SA_HOME, msg);
		// if (sahome == null || sahome.trim().isEmpty() || !(new
		// File(sahome).exists()))
		if (sahome == null)
			throw new Exception("SA dir is empty, please specify it in the configuration file!");

		outMessages += "Epic base: " + baseDir + ls;
		outMessages += "SA home: " + sahome + ls;

		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);

		final String jobFile = writeRunScript(baseDir, scenarioDir, sahome);
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
					runBatchScript(batchFile, jobFile);
				}
			}
		});
		populateThread.start();
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

	protected String writeRunScript(String baseDir, String scenarioDir, String sahome) throws Exception {

		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		String file = scenarioDir.trim() + "/scripts";

		if (spinup)
			file = file.trim() + "/epicYearlyAverage2CMAQ_spinup" + timeStamp + ".csh";
		else
			file = file.trim() + "/epicYearlyAverage2CMAQ_app" + timeStamp + ".csh";

		StringBuilder sb = new StringBuilder();

		sb.append(getScriptHeader() + ls);
		sb.append("#" + ls + "# Set up runtime environment" + ls + "#" + ls);
		sb.append("setenv    EPIC_DIR " + baseDir + ls);
		sb.append("setenv    SCEN_DIR " + scenarioDir + ls);
		sb.append("setenv    SA_HOME " + sahome + ls);
		sb.append("source $SA_HOME/" + Constants.SA_SETUP_FILE + ls + ls);
		sb.append("#" + ls + "# Define domain grids" + ls + "#" + ls);
		sb.append("setenv GRID_PROJ \"" + proj4proj.getText().trim() + "\"" + ls + ls);
		sb.append("setenv GRID_ROWS     " + ((Number) rows.getValue()).intValue() + ls);
		sb.append("setenv GRID_COLUMNS  " + ((Number) cols.getValue()).intValue() + ls + ls);
		sb.append("setenv GRID_XMIN    " + ((Number) xmin.getValue()).doubleValue() + ls);
		sb.append("setenv GRID_YMIN    " + ((Number) ymin.getValue()).doubleValue() + ls + ls);
		sb.append("setenv GRID_XCELLSIZE " + ((Number) xSize.getValue()).doubleValue() + ls);
		sb.append("setenv GRID_YCELLSIZE " + ((Number) ySize.getValue()).doubleValue() + ls + ls);
		sb.append("setenv GRID_NAME  \"" + (gridName.getText() == null ? "" : gridName.getText().trim()) + "\"" + ls
				+ ls);
		sb.append("#" + ls + "# set EPIC output file directory which containts each day data" + ls + "#" + ls);

		if (spinup)
			sb.append("setenv DATA_DIR   $SCEN_DIR/output4CMAQ/spinup/5years/" + ls + ls);
		else
			sb.append("setenv DATA_DIR   $SCEN_DIR/output4CMAQ/app/year/" + ls + ls);

		sb.append("# Define BELD4 input file" + ls);
		sb.append("setenv DOMAIN_BELD4_NETCDF " + beld4Dir.getText() + ls + ls);

		sb.append("#" + ls + "# Output file:" + ls + "#" + ls);
		if (spinup)
			sb.append("setenv OUTPUT_NETCDF_FILE   $SCEN_DIR/output4CMAQ/spinup/toCMAQ/epic2cmaq_year.nc" + ls + ls);
		else
			sb.append("setenv OUTPUT_NETCDF_FILE   $SCEN_DIR/output4CMAQ/app/toCMAQ/epic2cmaq_year.nc" + ls + ls);

		sb.append("#Total from all crops" + ls);
		if (spinup)
			sb.append("setenv OUTPUT_NETCDF_FILE_TOTAL " + "$SCEN_DIR/output4CMAQ/spinup/toCMAQ/epic2cmaq_year_total.nc"
					+ ls + ls);
		else
			sb.append("setenv OUTPUT_NETCDF_FILE_TOTAL " + "$SCEN_DIR/output4CMAQ/app/toCMAQ/epic2cmaq_year_total.nc"
					+ ls + ls);

		sb.append("#" + ls + "# run the EPIC output processing program:" + ls + "#" + ls);
		sb.append("$SA_HOME/bin/64bits/extractEPICYearlyAverage2CMAQ.exe" + ls + ls);

		// Boolean swatDayYN = swatDayBox.isSelected()? true : false;
		// System.out.println(swatDayYN);
		// if (swatDayYN) sb.append(getScriptDaySwat());

		sb.append("#===================================================================" + ls);
		String mesg = "";
		try {
			File script = new File(file);
			Runtime.getRuntime().exec("chmod 755 " + script.getAbsolutePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(script));
			out.write(sb.toString());
			out.close();
			mesg += "Created a script file: " + file + ls;
			boolean ok = script.setExecutable(true, false);
			mesg += "Set the script file to be executable: ";
			mesg += ok ? "ok." : "failed.";
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		app.showMessage("Write script", mesg);
		return file;
	}
	
	protected String writeBatchFile(String jobFile, String scenarioDir) throws Exception {

		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		String batchFile = scenarioDir.trim() + "/scripts";
		if (!batchFile.endsWith(System.getProperty("file.separator")))
			batchFile += System.getProperty("file.separator");
		batchFile += "submitEpicYearlyAverage2CMAQ";
		if (spinup)
			batchFile += "submitEpicYearlyAverage2CMAQ_spinup" + timeStamp + ".csh";
		else
			batchFile += "submitEpicYearlyAverage2CMAQ_app" + timeStamp + ".csh";

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


	private String getScriptHeader() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to process EPIC modeling yearly average output for CMAQ bi-directional" + ls);
		sb.append("#           ammonia surface flux modeling" + ls);
		sb.append("#" + ls);
		sb.append("# Written by the Institute for the Environment at UNC, Chapel Hill" + ls);
		sb.append("# in support of the EPA CMAS project, 2010." + ls);
		sb.append("#" + ls);
		sb.append("# Written by:   L. R., NOV. 2010" + ls);
		sb.append("#" + ls);
		sb.append("# Call program: epicYearlyAverage2CMAQ.exe" + ls);
		sb.append("#               Needed environment variables included in the script file to run." + ls);
		sb.append("#" + ls);
		sb.append("#***************************************************************************************" + ls + ls);

		return sb.toString();
	}

	private String getScriptDaySwat() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose: Prepare inputs for SWAT by summarizing data to regions" + ls);
		sb.append("#           from the extracted EPIC *NCD output file:" + ls);
		sb.append("#" + ls);
		sb.append("#***************************************************************************************" + ls + ls);

		sb.append("#" + ls);
		sb.append("# Define output type and simulation year" + ls);
		sb.append("#" + ls);

		String year = domain.getSimYear();
		if (spinup)
			sb.append("setenv TYPE   \"spinup\"" + ls);
		else
			sb.append("setenv TYPE   \"app\"" + ls);
		sb.append("setenv YEAR   " + year + ls + ls);

		sb.append("setenv DATA_DIR   $SCEN_DIR/output4CMAQ/$TYPE/daily/" + ls + ls);

		sb.append("# Crop fraction data" + ls);
		sb.append("setenv  SITEFILE   ${SCEN_DIR}/share_data/EPICSites_Info.csv" + ls);

		sb.append("# set crops in the summary" + ls);
		sb.append("#setenv CROPS  \"BARLEY\"" + ls);
		sb.append("setenv CROPS \"ALL\"" + ls + ls);

		sb.append("# set output file" + ls);
		sb.append("if ( ! -e $DATA_DIR/swat ) mkdir -p $DATA_DIR/swat" + ls);
		sb.append("setenv  OUTFILE   $DATA_DIR/swat/HUC8_" + ls + ls);

		sb.append("echo \"Run daily summary for swat: \"  ${SCEN_DIR}" + ls);

		sb.append("R CMD BATCH --no-save --slave " + "$EPIC_DIR/util/misc/swat/epic2swat_daily_HUC8.R "
				+ "${SCEN_DIR}/scripts/epic2swat_daily_HUC8.log" + ls + ls);

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
		String qYearlyExt = Constants.getProperty(Constants.QUEUE_YEARLY_EXT, msg);

		File script = new File(batchFile.replaceAll("\\\\", "\\\\\\\\"));

		sb.append(qcmd + " " + qYearlyExt + " -o " + log + " " + script.getAbsolutePath());

		FileRunner.runScriptwCmd(batchFile, log, msg, sb.toString());
	}

	@Override
	public void projectLoaded() {
		fields = (EpicYearlyAverage2CMAQFields) app.getProject().getPage(fields.getName());
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		if (fields != null) {
			String scenloc = domain.getScenarioDir();
			if (scenloc != null && scenloc.trim().length() > 0)
				this.scenarioDir.setText(scenloc);
			else
				this.scenarioDir.setText(fields.getScenarioDir());
			rows.setValue(fields.getRows());
			cols.setValue(fields.getCols());
			xSize.setValue(fields.getXcellSize());
			ySize.setValue(fields.getYcellSize());
			xmin.setValue(fields.getXmin());
			ymin.setValue(fields.getYmin());
			proj4proj.setText(fields.getProj());
			gridName.setText(fields.getGridName());
			runMessages.setText(fields.getMessage());

			if ((fields.getBeld4ncf() == null || fields.getBeld4ncf().trim().isEmpty())) {
				String scenDir = fields.getScenarioDir().trim();
				String gridName = fields.getGridName().trim();
				String year = fields.getNlcdYear().trim();
				String beld4file = scenDir + "/share_data/beld4_" + gridName + "_" + year + ".nc";
				// System.out.println(beld4file);
				// File f = new File(beld4file);
				// if(f.exists()){
				this.beld4Dir.setText(beld4file);
				// }
				// else
				// this.beld4Dir.setText(scenDir);
			} else
				this.beld4Dir.setText(fields.getBeld4ncf());

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
		if (rows != null)
			fields.setRows(Integer.parseInt(rows.getText() == null ? "" : rows.getValue() + ""));
		if (cols != null)
			fields.setCols(Integer.parseInt(cols.getText() == null ? "" : cols.getValue() + ""));
		if (xSize != null)
			fields.setXcellSize(Float.parseFloat(xSize.getText() == null ? "" : xSize.getValue() + ""));
		if (ySize != null)
			fields.setYcellSize(Float.parseFloat(ySize.getText() == null ? "" : ySize.getValue() + ""));
		if (xmin != null)
			fields.setXmin(Float.parseFloat(xmin.getText() == null ? "" : xmin.getValue() + ""));
		if (ymin != null)
			fields.setYmin(Float.parseFloat(ymin.getText() == null ? "" : ymin.getValue() + ""));
		if (proj4proj != null)
			fields.setProj(proj4proj.getText() == null ? "" : proj4proj.getText());
		if (gridName != null)
			fields.setGridName(gridName.getText() == null ? "" : gridName.getText());

		if (runMessages != null)
			fields.setMessage(runMessages.getText());
		if (beld4Dir != null)
			fields.setBeld4ncf(beld4Dir.getText() == null ? "" : beld4Dir.getText());
	}

	@Override
	public void newProjectCreated() {
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());

		String scenDir = domain.getScenarioDir().trim();
		String gName = domain.getGridName().trim();
		String nlcdY = domain.getNlcdYear().trim();
		scenarioDir.setText(scenDir);
		rows.setValue(domain.getRows());
		cols.setValue(domain.getCols());
		xmin.setValue(domain.getXmin());
		ymin.setValue(domain.getYmin());
		xSize.setValue(domain.getXcellSize());
		ySize.setValue(domain.getYcellSize());
		proj4proj.setText(domain.getProj());
		gridName.setText(domain.getGridName());

		String beld4file = scenDir + "/share_data/beld4_" + gName + "_" + nlcdY + ".nc";
		// File f = new File(beld4file);
		// if(f.exists()){
		this.beld4Dir.setText(beld4file);
		// }
		// else
		// this.beld4Dir.setText(scenDir);

		runMessages.setText("");
		if (fields == null) {
			fields = new EpicYearlyAverage2CMAQFields();
			app.getProject().addPage(fields);
		}
	}
}

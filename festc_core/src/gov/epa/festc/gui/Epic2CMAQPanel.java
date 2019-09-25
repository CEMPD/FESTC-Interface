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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.Epic2CMAQFields;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.FileRunner;
import gov.epa.festc.util.SpringLayoutGenerator;
import simphony.util.messages.MessageCenter;

public class Epic2CMAQPanel extends UtilFieldsPanel implements PlotEventListener {
	private static final long serialVersionUID = 4247819754945274135L;
	//
	// private JTextField dataDir;
	// private JButton dataDirBrowser;
	// private JTextField outDir;
	// private JButton outDirBrowser;
	private JTextField filesPrefix;
	private MessageCenter msg;
	private JFormattedTextField startDate;
	private JFormattedTextField endDate;
	private JRadioButton applicationBtn, spinupBtn;
	private boolean spinup = false;

	private FestcApplication app;
	private Epic2CMAQFields fields;

	public Epic2CMAQPanel(FestcApplication festcApp) {
		app = festcApp;
		msg = app.getMessageCenter();
		fields = new Epic2CMAQFields();
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
		JLabel title = new JLabel(Constants.EPIC2CMAQ, SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

		return panel;
	}

	private JPanel getSouthPanel() {
		JPanel panel = new JPanel();
		JButton display = new JButton(runAction());
		panel.add(display);

		panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

		return panel;
	}

	private JPanel getCenterPanel() {
		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();

		JPanel filesPrefixPanel = new JPanel();
		filesPrefix = new JTextField(40);
		filesPrefixPanel.add(filesPrefix);

		JPanel startDatePanel = new JPanel();
		NumberFormat snf = NumberFormat.getNumberInstance();
		snf.setGroupingUsed(false);
		startDate = new JFormattedTextField(snf);
		startDate.setColumns(40);
		startDatePanel.add(startDate);

		JPanel endDatePanel = new JPanel();
		NumberFormat enf = NumberFormat.getNumberInstance();
		enf.setGroupingUsed(false);
		endDate = new JFormattedTextField(enf);
		endDate.setColumns(40);
		endDatePanel.add(endDate);

		JPanel butPanel = new JPanel();
		this.applicationBtn = new JRadioButton(applicationSelection());
		this.spinupBtn = new JRadioButton(spinupSelection());
		this.applicationBtn.setSelected(true);
		this.spinupBtn.setSelected(false);
		butPanel.add(this.spinupBtn);
		butPanel.add(this.applicationBtn);

		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(this.spinupBtn);
		btnGroup.add(this.applicationBtn);

		layout.addLabelWidgetPair("Grid Description:", getGridDescPanel(false), panel);
		layout.addLabelWidgetPair(Constants.LABEL_EPIC_SCENARIO, scenarioDirP, panel);
		layout.addLabelWidgetPair("Start Date (YYYYMMDD):", startDatePanel, panel);
		layout.addLabelWidgetPair("End Date (YYYYMMDD):", endDatePanel, panel);
		layout.addLabelWidgetPair("Output File Prefix:", filesPrefixPanel, panel);
		layout.addLabelWidgetPair("Output Type:", butPanel, panel);

		layout.makeCompactGrid(panel, 6, 2, // number of rows and cols
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

	private Action runAction() {
		return new AbstractAction("Run") {
			private static final long serialVersionUID = 5806383737068197305L;

			public void actionPerformed(ActionEvent e) {
				try {
					validateFields();

					String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);
					final String jobFile = writeScript();
					final String batchFile;
					if (qcmd != null && !qcmd.trim().isEmpty()) {
						batchFile = writeBatchFile(jobFile, scenarioDir.getText());
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
//		if (sahome == null || sahome.trim().isEmpty() || !(new File(sahome).exists()))
		if (sahome == null || sahome.trim().isEmpty())
			throw new Exception("Error loading spacial allocator home:" + sahome + " doesn't exist");

		String scenarioDir = this.scenarioDir.getText();
		if (scenarioDir == null || scenarioDir.isEmpty())
			throw new Exception("Please select scenario dir first!");

		validateGrids();

		if (filesPrefix.getText() == null || filesPrefix.getText().trim().isEmpty())
			throw new Exception("Please specify a valid output files prefix.");

		String start = startDate.getText();
		if (start == null || start.trim().isEmpty())
			throw new Exception("Start date field is empty.");

		if (start.trim().length() != 8)
			throw new Exception("Star date format is not right.");

		if (start.trim().charAt(0) == '0')
			throw new Exception("Start date value is invalid.");

		String end = endDate.getText();
		if (end == null || end.trim().isEmpty())
			throw new Exception("End date field is empty.");

		if (end.trim().length() != 8)
			throw new Exception("End date format is not right.");

		if (end.trim().charAt(0) == '0')
			throw new Exception("End date value is invalid.");
	}

	protected String writeScript() throws Exception {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		String scenario = scenarioDir.getText().trim();
		String file = scenario.trim() + "/scripts";

		String sahome = Constants.getProperty(Constants.SA_HOME, msg);

		outMessages += "SA home: " + sahome + ls;

		if (!file.endsWith(System.getProperty("file.separator")))
			file += System.getProperty("file.separator");

		// file += "epic2CMAQ_" + timeStamp + ".csh";
		if (spinup)
			file = file.trim() + "/epic2CMAQ_spinup_" + timeStamp + ".csh";
		else
			file = file.trim() + "/epic2CMAQ_app_" + timeStamp + ".csh";

		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append(getScirptHeader() + ls);
		sb.append("#" + ls + "# Set up runtime environment" + ls + "#" + ls);
		sb.append("source " + sahome.trim() + Constants.SA_SETUP_FILE + ls + ls);
		sb.append("setenv    SCEN_DIR " + scenario + ls);
		sb.append("setenv    SA_HOME " + sahome + ls);
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
		sb.append("#" + ls + "#set EPIC output file directory which containts each day data:" + ls + "#" + ls);
		if (spinup)
			sb.append("setenv DATA_DIR   $SCEN_DIR/output4CMAQ/spinup/daily/" + ls + ls);
		else
			sb.append("setenv DATA_DIR   $SCEN_DIR/output4CMAQ/app/daily/" + ls + ls);

		// sb.append("setenv DATA_DIR $SCEN_DIR/output4CMAQ/app/daily" + ls +
		// ls);
		sb.append("#" + ls + "#Set date and time range: YYYYMMDDHHMM" + ls + "#" + ls);
		sb.append("setenv START_DATE  " + startDate.getText() + ls);
		sb.append("setenv END_DATE    " + endDate.getText() + ls + ls);
		// sb.append("#" + ls + "# Output files: three output files for soil,
		// EPIC daily output, and fertilizer application data" + ls + "#" + ls);
		// sb.append("setenv SOIL_OUTPUT_NETCDF_FILE \"" +
		// outDir.getText().trim() + "/" + filesPrefix.getText().trim() +
		// "_soil.nc\"" + ls);
		// sb.append("setenv DAILY_OUTPUT_NETCDF_FILE \"" +
		// outDir.getText().trim() + "/" + filesPrefix.getText().trim() +
		// "_time\"" + ls + ls);
		// sb.append("setenv FERTILIZER_OUTPUT_NETCDF_FILE \"" +
		// outDir.getText().trim() + "/" + filesPrefix.getText().trim() +
		// "_fert.nc\"" + ls + ls);
		sb.append("#" + ls + "# Output file prefix for soil and EPIC daily output" + ls
				+ "# \"prefix\"_soil.nc for soil ouput and \"prefix\"_time\"yyyymm\".nc for daily EPIC output" + ls
				+ "#" + ls);
		if (spinup)
			sb.append("setenv OUTPUT_NETCDF_FILE_PREFIX   $SCEN_DIR/output4CMAQ/spinup/toCMAQ/"
					+ filesPrefix.getText().trim() + ls);
		else
			sb.append("setenv OUTPUT_NETCDF_FILE_PREFIX   $SCEN_DIR/output4CMAQ/app/toCMAQ/"
					+ filesPrefix.getText().trim() + ls);
		sb.append("#" + ls + "# run the EPIC output processing program" + ls + "#" + ls);
		sb.append("$SA_HOME/bin/64bits/extractEPIC2CMAQ.exe" + ls + ls);
		sb.append("if ( $status == 0 ) then" + ls);
		sb.append("    echo ==== Finished EPIC to CMAQ run. " + ls);
		sb.append("else " + ls);
		sb.append("    echo ==== Error in EPIC to CMAQ run. " + ls);
		sb.append("endif " + ls);
		sb.append("#===================================================================" + ls);

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
		batchFile += "submitEpic2CMAQ_" + timeStamp + ".csh";

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

	private String getScirptHeader() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to process EPIC modeling output for CMAQ bi-directional ammonia surface" + ls);
		sb.append("#           flux modeling " + ls);
		sb.append("#" + ls);
		sb.append("#" + ls);
		sb.append("# Written by the Institute for the Environment at UNC, Chapel Hill" + ls);
		sb.append("# in support of the CMAS project, 2010" + ls);
		sb.append("#" + ls);
		sb.append("# Written by:   LR, July-Sept 2010" + ls);
		sb.append("#" + ls);
		sb.append("# Program: extractEPIC2CMAQ.exe" + ls);
		sb.append("#          Needed environment variables included in the script file to run." + ls);
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
		String qEpic2Cmaq = Constants.getProperty(Constants.QUEUE_EPIC2CMAQ, msg);

		File script = new File(batchFile.replaceAll("\\\\", "\\\\\\\\"));

		sb.append(qcmd + " " + qEpic2Cmaq + " -o " + log + " " + script.getAbsolutePath());

		FileRunner.runScriptwCmd(batchFile, log, msg, sb.toString());
	}

	@Override
	public void projectLoaded() {
		fields = (Epic2CMAQFields) app.getProject().getPage(fields.getName());
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
			try {
				startDate.setValue(NumberFormat.getNumberInstance()
						.parse(fields.getStartdate() == null ? "" : fields.getStartdate()));
				endDate.setValue(
						NumberFormat.getNumberInstance().parse(fields.getEnddate() == null ? "" : fields.getEnddate()));
			} catch (ParseException e) {
				startDate.setValue(0);
				endDate.setValue(0);
			}
			filesPrefix.setText(fields.getOutfileprefix());
			runMessages.setText(fields.getMessage());
		} else {
			newProjectCreated();
		}
	}

	@Override
	public void saveProjectRequested() {
		if (scenarioDir != null)
			domain.setScenarioDir(scenarioDir.getText() == null ? "" : scenarioDir.getText());
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
		if (startDate != null)
			fields.setStartdate(startDate.getValue() == null ? "" : startDate.getText());
		if (endDate != null)
			fields.setEnddate(endDate.getValue() == null ? "" : endDate.getText());
		if (filesPrefix != null)
			fields.setOutfileprefix(filesPrefix.getText() == null ? "" : filesPrefix.getText());
		if (runMessages != null)
			fields.setMessage(runMessages.getText());
	}

	@Override
	public void newProjectCreated() {
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		scenarioDir.setText(domain.getScenarioDir());
		rows.setValue(domain.getRows());
		cols.setValue(domain.getCols());
		xmin.setValue(domain.getXmin());
		ymin.setValue(domain.getYmin());
		xSize.setValue(domain.getXcellSize());
		ySize.setValue(domain.getYcellSize());
		proj4proj.setText(domain.getProj());
		gridName.setText(domain.getGridName());
		runMessages.setText("");
		fields.setOutfileprefix("");
		try {
			startDate.setValue(NumberFormat.getNumberInstance().parse(domain.getSimYear().trim() + "0101"));
			endDate.setValue(NumberFormat.getNumberInstance().parse(domain.getSimYear().trim() + "1231"));
		} catch (ParseException e) {
			startDate.setValue(0);
			endDate.setValue(0);
		}

		if (fields == null) {
			fields = new Epic2CMAQFields();
			app.getProject().addPage(fields);
		}
	}

}

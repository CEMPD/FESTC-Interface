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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.Mcip2EpicFields;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.FileRunner;
import gov.epa.festc.util.SpringLayoutGenerator;
import simphony.util.messages.MessageCenter;

public class Mcip2EpicPanel extends UtilFieldsPanel implements PlotEventListener {
	private static final long serialVersionUID = -1426530506543484237L;
	private JTextField mdataDir;
	private JButton dataDirBrowser;

	private MessageCenter msg;
	private JFormattedTextField startDate;
	private JFormattedTextField endDate;

	private JComboBox depositionSel;
	private JTextField depositionDir;
	private JButton depositionDirBrowser;

	private JCheckBox dlyBox;

	private FestcApplication app;
	private Mcip2EpicFields fields;

	public Mcip2EpicPanel(FestcApplication festcApp) {
		app = festcApp;
		msg = app.getMessageCenter();
		fields = new Mcip2EpicFields();
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
		JLabel title = new JLabel(Constants.MC2EPIC, SwingConstants.CENTER);
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

		JPanel dataDirPanel = new JPanel();
		mdataDir = new JTextField(40);
		dataDirBrowser = new JButton(browseDirAction("MCIP data dir", mdataDir));
		dataDirPanel.add(mdataDir);
		dataDirPanel.add(dataDirBrowser);

		JPanel deposSPanel = new JPanel();
		depositionSel = new JComboBox(Constants.DEPSELECTIONS);
		depositionSel.setSelectedIndex(0);
		depositionSel.addActionListener(selectAction());
		deposSPanel.add(depositionSel);

		JPanel deposDirPanel = new JPanel();
		depositionDir = new JTextField(40);
		depositionDirBrowser = new JButton(browseDirAction("CMAQ deposition dir", depositionDir));
		deposDirPanel.add(depositionDir);
		deposDirPanel.add(depositionDirBrowser);

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

		JPanel dlyPanel = new JPanel();
		this.dlyBox = new JCheckBox("", false);
		dlyPanel.add(this.dlyBox);

		layout.addLabelWidgetPair("Grid Description:", getGridDescPanel(false), panel);
		layout.addLabelWidgetPair(Constants.LABEL_EPIC_SCENARIO, scenarioDirP, panel);
		layout.addLabelWidgetPair("Start Date (YYYYMMDD):", startDatePanel, panel);
		layout.addLabelWidgetPair("End Date (YYYYMMDD):", endDatePanel, panel);
		layout.addLabelWidgetPair("MCIP Data Directory:", dataDirPanel, panel);
		layout.addLabelWidgetPair("Deposition Selection: ", deposSPanel, panel);
		layout.addLabelWidgetPair("CMAQ Deposition Directory:", deposDirPanel, panel);
		layout.addLabelWidgetPair("Output DLY Files:", dlyPanel, panel);

		layout.makeCompactGrid(panel, 8, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading

		return panel;
	}

	private AbstractAction selectAction() {
		return new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				if (depositionSel.getSelectedItem() != null) {
					if ((depositionSel.getSelectedItem()).equals("CMAQ deposition directory")) {
						depositionDirBrowser.setEnabled(true);
						depositionDir.setEditable(true);
					} else {
						depositionDirBrowser.setEnabled(false);
						depositionDir.setEditable(false);
					}
				}
			}
		};
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

	private Action browseDirAction(final String name, final JTextField text) {
		return new AbstractAction("Browse...") {
			private static final long serialVersionUID = 482845697751457179L;

			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser;
				File file = new File(text.getText());

				if (file != null && file.isFile()) {
					chooser = new JFileChooser(file.getParentFile());
				} else if (file != null && file.isDirectory()) {
					chooser = new JFileChooser(file);
				} else
					chooser = new JFileChooser(app.getCurrentDir());

				chooser.setDialogTitle("Please select the " + name);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int option = chooser.showDialog(Mcip2EpicPanel.this, "Select");
				if (option == JFileChooser.APPROVE_OPTION) {
					File selected = chooser.getSelectedFile();
					text.setText("" + selected);
					app.setCurrentDir(selected);
				}
			}
		};
	}

	private void generateRunScript() throws Exception {

		String baseDir = Constants.getProperty(Constants.EPIC_HOME, msg);
		if (baseDir == null || baseDir.isEmpty())
			throw new Exception("Base dir is empty, please specify it in the configuration file!");

		String scenarioDir = this.scenarioDir.getText();
		validateScen(scenarioDir);
		validateGrids();

		String sahome = Constants.getProperty(Constants.SA_HOME, msg);
		// if (sahome == null || sahome.trim().isEmpty() || !(new
		// File(sahome).exists()))
		if (sahome == null)
			throw new Exception("SA dir is empty, please specify it in the configuration file!");

		if (!(new File(mdataDir.getText()).exists()))
			throw new Exception("MCIP data directory is invalid.");

		if ((depositionSel.getSelectedItem()).equals("CMAQ deposition directory")) {
			if (!(new File(depositionDir.getText()).exists()))
				throw new Exception("CMAQ deposition directory is invalid.");

		}

		String start = startDate.getText();
		if (start == null || start.trim().isEmpty())
			throw new Exception("Start date field is empty.");

		if (start.trim().length() != 8)
			throw new Exception("Start date value is invalid.");

		if (start.trim().charAt(0) == '0')
			throw new Exception("Start date value is invalid.");

		String end = endDate.getText();
		if (end == null || end.trim().isEmpty())
			throw new Exception("End date field is empty.");

		if (end.trim().length() != 8)
			throw new Exception("End date value is invalid.");

		if (end.trim().charAt(0) == '0')
			throw new Exception("End date value is invalid.");

		outMessages += "Epic base: " + baseDir + ls;
		outMessages += "SA home: " + sahome + ls;
		outMessages += "Scen directory: " + scenarioDir + ls;

		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);

		final String jobFile = writeRunScriptScript(baseDir, scenarioDir, sahome);
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
					// final String batchFile = writeBatchFile(jobFile,
					// scenarioDir);
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
	
	protected String writeBatchFile(String jobFile, String scenarioDir) throws Exception {

		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		String batchFile = scenarioDir.trim() + "/scripts";
		if (!batchFile.endsWith(System.getProperty("file.separator")))
			batchFile += System.getProperty("file.separator");
		batchFile += "submitSiteDailyWeather" + timeStamp + ".csh";

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


	protected String writeRunScriptScript(String baseDir, String scenarioDir, String sahome) throws Exception {
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!

		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

		String file = scenarioDir.trim() + "/scripts";
		file += "/generateEPICsiteDailyWeatherfromMCIP_CAMQ_" + timeStamp + ".csh";

		StringBuilder sb = new StringBuilder();
		String ls = "\n";

		sb.append(getScriptHeader() + ls);
		sb.append("#" + ls + "# Set up runtime environment" + ls + "#" + ls);
		sb.append("source " + sahome.trim() + Constants.SA_SETUP_FILE + ls + ls);

		sb.append("setenv    EPIC_DIR " + baseDir + ls);
		sb.append("setenv    SCEN_DIR " + scenarioDir + ls);
		sb.append("setenv    SA_HOME " + sahome + ls);
		sb.append("source $SA_HOME/" + Constants.SA_SETUP_FILE + ls + ls);

		sb.append("#" + ls + "# Define MCIP domain grid information" + ls + "#" + ls);
		sb.append("setenv GRID_PROJ \"" + proj4proj.getText().trim() + "\"" + ls + ls);
		sb.append("setenv GRID_ROWS     " + ((Number) rows.getValue()).intValue() + ls);
		sb.append("setenv GRID_COLUMNS  " + ((Number) cols.getValue()).intValue() + ls + ls);
		sb.append("setenv GRID_XMIN    " + ((Number) xmin.getValue()).doubleValue() + ls);
		sb.append("setenv GRID_YMIN    " + ((Number) ymin.getValue()).doubleValue() + ls + ls);
		sb.append("setenv GRID_XCELLSIZE " + ((Number) xSize.getValue()).doubleValue() + ls);
		sb.append("setenv GRID_YCELLSIZE " + ((Number) ySize.getValue()).doubleValue() + ls + ls);
		sb.append("setenv GRID_NAME  \"" + (gridName.getText() == null ? "" : gridName.getText().trim()) + "\"" + ls
				+ ls);
		sb.append("#" + ls + "#Set MCIP data directory which containts daily MCIP files" + ls
				+ "#Daily MCIP data files have to have names with METCRO2D*\"date\"" + ls
				+ "#The \"date\" can be in one of the format: YYYYMMDD, YYMMDD, YYYYDDD, YYDDD" + ls + "#" + ls);

		sb.append("setenv DATA_DIR   \"" + mdataDir.getText() + "\"" + ls + ls);
		sb.append("#" + ls + "#Set CMAQ output dry/wet deposition data directory which containts daily CMAQ files" + ls
				+ "#" + ls);

		String dataDirCmaq = "";
		if (depositionSel.getSelectedItem().equals("Zero"))
			dataDirCmaq = "Zero";
		else if (depositionSel.getSelectedItem().equals("Default"))
			dataDirCmaq = "Default";
		else
			dataDirCmaq = depositionDir.getText().trim();
		sb.append("setenv DATA_DIR_CMAQ   \"" + dataDirCmaq + "\"" + ls + ls);
		sb.append("#" + ls + "#Set date range: YYYYMMDD" + ls + "#" + ls);
		sb.append("setenv START_DATE  " + startDate.getText() + ls);
		sb.append("setenv END_DATE    " + endDate.getText() + ls + ls);
		sb.append("#" + ls + "#Set input EPIC site data file in ascii csv format as:  site_name,longitude,latitude" + ls
				+ "#" + ls);
		sb.append("setenv EPIC_SITE_FILE  \"$SCEN_DIR/share_data/allSites_Info.csv" + "\"" + ls + ls);
		sb.append("#" + ls + "# Set output directory which will store created EPIC site daily weather files:" + ls
				+ "#   1. \"site_name\".dly   2. WXPMRUN.DAT " + ls + "#" + ls);

		sb.append("setenv OUTPUT_DATA_DIR  $SCEN_DIR/share_data/ " + ls + ls);
		sb.append("#" + ls + "# Set NetCDF output file to store computed EPIC site daily weather and N deposition data"
				+ ls + "# Only set this variable when EPIC grids are the same as MCIP/CMAQ grids." + ls
				+ "# Otherwise, set it to NONE for no NetCDF file output" + ls + "#" + ls);
		sb.append("setenv OUTPUT_NETCDF_FILE  $SCEN_DIR/share_data/site_weather_dep_${START_DATE}_to_${END_DATE}.nc"
				+ ls + ls);

		String dlyYN = dlyBox.isSelected() ? "YES" : "NO";
		sb.append("setenv WRITE_DLY    " + dlyYN + ls);

		sb.append("$SA_HOME/bin/64bits/computeSiteDailyWeather.exe" + ls + ls);

		sb.append("   if ( $status == 0 ) then " + ls);
		sb.append("      echo  ==== Finished MCIP/CMAQ to EPIC run. " + ls);

		sb.append("      cp $OUTPUT_NETCDF_FILE $SCEN_DIR/share_data/site_weather_dep.nc" + ls);

		if (dlyBox.isSelected()) {
			sb.append("      echo  ==== Consolidating dly files. " + ls);
			sb.append(
					"      (cd $SCEN_DIR/share_data/dailyWETH && tar -czf dly_${START_DATE}_to_${END_DATE}.tar.gz *.dly --remove-files)"
							+ ls);
		}
		// end if statement

		sb.append("   else " + ls);
		sb.append("      echo  ==== Error in MCIP/CMAQ to EPIC runs." + ls + ls);
		sb.append("      echo " + ls);
		sb.append("   endif " + ls);
		sb.append("#===================================================================" + ls);

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
			throw new Exception(e.getMessage());
		}
		app.showMessage("Write script", mesg);
		return file;
	}

	private String getScriptHeader() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to extract six daily weather variable data from MCIP data for EPIC modeling" + ls);
		sb.append("#           sites and to create WXPMRUN.DAT run data file for WXPM3020 to compute" + ls);
		sb.append("#           monthly climate data." + ls);
		sb.append("#" + ls);
		sb.append("#   1. Radiation (MJ m^02, daily total)" + ls);
		sb.append("#   2. Tmax (C, daily)" + ls);
		sb.append("#   3. Tmin (C, daily)" + ls);
		sb.append("#   4. Precipitation (mm, daily total)" + ls);
		sb.append("#   5. Relative humidity (fraction, daily average)" + ls);
		sb.append("#   6. Windspeed (m s^-1, daily average)" + ls);
		sb.append("" + ls);
		sb.append("#     There are three steps involved in extraction:" + ls);
		sb.append("#     1. read in EPIC site lat and long location data, project them into MCIP data projection and"
				+ ls);
		sb.append("#        convert them into column and row in MCIP grids." + ls);
		sb.append(
				"#     2. loop through each day MCIP data to extract daily weather variables for each EPIC site" + ls);
		sb.append("#        and to write them into each EPIC site daily weather file." + ls);
		sb.append(
				"#     3. created WXPMRUN.DAT file for all generated EPIC daily weather files in order to run WXPM3020.DRB program."
						+ ls);
		sb.append(
				"#        The program computes monthly weather data to be named in monthly weather list file like WPM1US.DAT."
						+ ls);
		sb.append("#" + ls);
		sb.append("# Output files:" + ls);
		sb.append("#     1. \"site_name\".dly - EPIC daily weather input file " + ls);
		sb.append("#     2. WXPMRUN.DAT - to  WXPM3020.DRB for computing EPIC monthly weather" + ls);
		sb.append("#" + ls);
		sb.append("# Written by the Institute for the Environment at UNC, Chapel Hill" + ls);
		sb.append("# in support of the EPA CMAS Modeling, 2009." + ls);
		sb.append("#" + ls);
		sb.append("# Written by:   L. R., Aug. 2009" + ls);
		sb.append("# Modified by:" + ls);
		sb.append("#" + ls);
		sb.append("# Call program: computeSiteDailyWeather.exe" + ls);
		sb.append("#               Needed environment variables listed in this run script file." + ls);
		sb.append("#" + ls);
		sb.append("# Usage: ./generateEPICsiteDailyWeatherfromMCIP.csh" + ls);

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
		String qMc2Epic = Constants.getProperty(Constants.QUEUE_MC2EPIC, msg);

		File script = new File(batchFile.replaceAll("\\\\", "\\\\\\\\"));

		sb.append(qcmd + " " + qMc2Epic + " -o " + log + " " + script.getAbsolutePath());

		FileRunner.runScriptwCmd(batchFile, log, msg, sb.toString());
	}

	@Override
	public void projectLoaded() {
		fields = (Mcip2EpicFields) app.getProject().getPage(fields.getName());
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

			if (fields.getGridName() != null)
				this.gridName.setText(fields.getGridName());
			try {
				startDate.setValue(NumberFormat.getNumberInstance()
						.parse(fields.getStartdate() == null ? "" : fields.getStartdate()));
				endDate.setValue(
						NumberFormat.getNumberInstance().parse(fields.getEnddate() == null ? "" : fields.getEnddate()));
			} catch (ParseException e) {
				// NOTE: no-op;
			}
			mdataDir.setText(fields.getDatadir());
			depositionSel.setSelectedItem(fields.getDepSelection());
			depositionDir.setText(fields.getCmaqDepsDir());
			this.runMessages.setText(fields.getMessage());
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

		if (depositionSel != null)
			fields.setDepSelection((String) depositionSel.getSelectedItem());
		if (depositionDir != null)
			fields.setCmaqDepsDir(depositionDir.getText());
		if (mdataDir != null)
			fields.setDatadir(mdataDir.getText());
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
		mdataDir.setText("");
		depositionSel.setSelectedItem(0);
		depositionDir.setText("");

		this.runMessages.setText("");
		try {
			startDate.setValue(NumberFormat.getNumberInstance().parse(domain.getSimYear().trim() + "0101"));
			endDate.setValue(NumberFormat.getNumberInstance().parse(domain.getSimYear().trim() + "1231"));
		} catch (ParseException e) {
			startDate.setValue(0);
			endDate.setValue(0);
		}
		if (fields == null) {
			fields = new Mcip2EpicFields();
			app.getProject().addPage(fields);
		}
	}

}

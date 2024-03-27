package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.Beld4DataGenFields;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.SiteInfoGenFields;
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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import simphony.util.messages.MessageCenter;

public class CreateSiteInfoPanel extends UtilFieldsPanel implements PlotEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1403169880186710184L;

	private MessageCenter msg;
	private FestcApplication app;
	private SiteInfoGenFields fields;
	// private DomainFields domain;
	private JTextField beld4Dir;
	private JTextField minAcreas;
	private JButton beld4DirBrowser;

	public CreateSiteInfoPanel(FestcApplication festcApp) {
		app = festcApp;
		msg = FestcApplication.getMessageCenter();
		fields = new SiteInfoGenFields();
		app.getProject().addPage(fields);
		app.addPlotListener(this);
		add(createPanel());
	}

	private JPanel createPanel() {
		JPanel main = new JPanel();
		try {
			init();
			main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
			main.add(getNorthPanel());
			main.add(getCenterPanel());
			main.add(getSouthPanel());
			main.add(messageBox());

		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return main;

	}

	private JPanel getNorthPanel() {
		JPanel panel = new JPanel();
		JLabel title = new JLabel(Constants.SITE_INFO, SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

		return panel;
	}

	private JPanel getCenterPanel() {
		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();

		JPanel beld4DirPanel = new JPanel();
		beld4Dir = new JTextField(40);
		beld4Dir.setToolTipText("I.E. share_data/beld4_cmaq12km_2001.nc");

		beld4DirBrowser = new JButton(BrowseAction.browseAction(this, app.getCurrentDir(), "BELD4 file", beld4Dir));
		beld4DirPanel.add(beld4Dir);
		beld4DirPanel.add(beld4DirBrowser);

		JPanel minAcrePanel = new JPanel();
		minAcreas = new JTextField(20);
		minAcreas.setToolTipText("Default value is 0.0");
		minAcrePanel.add(minAcreas);

		// JPanel scenPanel = new JPanel();
		// scenPanel.add(scenarioDir);

		layout.addLabelWidgetPair("Grid Description: ", getGridDescPanel(false), panel);
		layout.addLabelWidgetPair(Constants.LABEL_EPIC_SCENARIO, scenarioDirP, panel);
		layout.addLabelWidgetPair("BELD4 NetCDF File: ", beld4DirPanel, panel);
		layout.addLabelWidgetPair("Minimum Crop Acres: ", minAcrePanel, panel);

		layout.makeCompactGrid(panel, 4, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading

		return panel;
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
					// TODO Auto-generated catch block
					// e1.printStackTrace();
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
		String beld4File = this.beld4Dir.getText();
		File beld4F = new File(beld4File);
		if (scenarioDir == null || scenarioDir.isEmpty())
			throw new Exception("Scenario dir is empty!");

		if (beld4F == null || beld4F.isDirectory() || !beld4F.exists())
			throw new Exception("Beld4 file is not existing!");

		String minAcres = minAcreas.getText();
		if (minAcres == null || minAcres.isEmpty())
			throw new Exception("Minimum Crop Acres is not specified!");

		String sMAcres = domain.getCMinAcres();
		if (sMAcres == null || sMAcres.trim().isEmpty()) {
			domain.setCMinAcres(minAcres);
			sMAcres = minAcres;
		} else if (sMAcres != null && !sMAcres.trim().isEmpty()
				&& (Float.parseFloat(sMAcres)) != (Float.parseFloat(minAcres)) && app.allowDiffCheck())
			throw new Exception(
					"Current minimum acre  " + minAcres + " is inconsistent with previous one (" + sMAcres + ")");

		try {
			Float.parseFloat(minAcres);
		} catch (NumberFormatException e) {
			throw new Exception("Minimum Crop Acres is not a number!");
		}

		validateGrids();

		String sahome = Constants.getProperty(Constants.SA_HOME, msg);
		// if (sahome == null || sahome.trim().isEmpty() || !(new
		// File(sahome).exists()))
		if (sahome == null)
			throw new Exception("SA dir is empty, please specify it in the configuration file!");

		outMessages += ls + "Epic base: " + baseDir + ls;
		outMessages += "SA home: " + sahome + ls;

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
		String batchFile = scenarioDir.trim() + "/scripts/";
		if (!batchFile.endsWith(System.getProperty("file.separator")))
			batchFile += System.getProperty("file.separator");
		batchFile += "submitSiteInfo_" + timeStamp + ".csh";

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
		String file = scenarioDir.trim() + "/scripts/";
		file = file.trim() + "generateSiteInfo_" + gridName.getText().trim() + "_" + timeStamp + ".csh";

		StringBuilder sb = new StringBuilder();

		// String netcdfout = (netcdfFile.getText() == null ||
		// netcdfFile.getText().trim().isEmpty()) ? "NONE" :
		// netcdfFile.getText().trim();
		sb.append(getScriptHeader() + ls);
		sb.append("#" + ls + "# Set up runtime environment variables" + ls + "#" + ls);
		sb.append("source " + sahome.trim() + Constants.SA_SETUP_FILE + ls + ls);

		sb.append(ls + "#" + ls);
		sb.append("# Define environment variables" + ls);
		sb.append("#" + ls + ls);
		sb.append("setenv    EPIC_DIR " + baseDir + ls);
		sb.append("setenv    SCEN_DIR " + scenarioDir + ls);
		sb.append("setenv    SA_HOME " + sahome + ls);
		sb.append("setenv    COMM_DIR $EPIC_DIR/common_data " + ls);
		sb.append("" + ls);

		sb.append("#" + ls + "# Define domain grids" + ls + "#" + ls);
		// sb.append("setenv GRID_PROJ \"+proj=lcc +a=6370000.0 +b=6370000.0
		// +lat_1=33 +lat_2=45 +lat_0=40 +lon_0=-97\"" + ls + ls);
		sb.append("setenv GRID_PROJ    \"" + proj4proj.getText().trim() + "\"" + ls);
		sb.append("setenv GRID_ROWS     " + ((Number) rows.getValue()).intValue() + ls);
		sb.append("setenv GRID_COLUMNS  " + ((Number) cols.getValue()).intValue() + ls + ls);
		sb.append("setenv GRID_XMIN    " + ((Number) xmin.getValue()).doubleValue() + ls);
		sb.append("setenv GRID_YMIN    " + ((Number) ymin.getValue()).doubleValue() + ls + ls);
		sb.append("setenv GRID_XCELLSIZE " + ((Number) xSize.getValue()).doubleValue() + ls);
		sb.append("setenv GRID_YCELLSIZE " + ((Number) ySize.getValue()).doubleValue() + ls + ls);
		String gridN = gridName.getText() == null ? "" : gridName.getText().trim();
		sb.append("setenv GRID_NAME  \"" + gridN + "\"" + ls + ls);

		sb.append("# Set minimum crop acres for site selection " + ls);
		sb.append("# Grids selected for EPIC modeling will have at " + ls);
		sb.append("# least one crop with acreage >= the minimum crop acres " + ls);
		sb.append("#  " + ls + ls);

		sb.append("setenv MINIMUM_CROP_ACRES   " + minAcreas.getText() + ls + ls);

		sb.append("# Define BELD4 input file" + ls);
		sb.append("setenv DOMAIN_BELD4_NETCDF " + beld4Dir.getText() + ls + ls);

		sb.append("# Define US county shapefiles with " + ls);
		sb.append("setenv COUNTY_SHAPEFILE $COMM_DIR/gisFiles/co99_d00_conus_cmaq_epic.shp" + ls + ls);

		sb.append("# Define North American State political boundary shapefile" + ls);
		sb.append("setenv COUNTRY_SHAPEFILE $COMM_DIR/gisFiles/na_bnd_camq_epic.shp" + ls + ls);

		sb.append("# US 8-digit HUC shapefile" + ls);
		sb.append("setenv HUC8_SHAPEFILE $COMM_DIR/gisFiles/conus_hucs_8_cmaq.shp" + ls + ls);

		sb.append("# Define Elevation image" + ls);
		sb.append("setenv ELEVATION_IMAGE $COMM_DIR/gisFiles/na_dem_epic.img" + ls + ls);

		sb.append("# Define slope image" + ls);
		sb.append("setenv SLOPE_IMAGE $COMM_DIR/gisFiles/na_slope_epic.img" + ls + ls);

		sb.append("#" + ls);
		sb.append("# Output files" + ls);
		sb.append("setenv OUTPUT_TEXT_FILE $SCEN_DIR/share_data/EPICSites_Info.csv" + ls);
		sb.append("setenv OUTPUT_TEXT_FILE2 $SCEN_DIR/share_data/EPICSites_Crop.csv" + ls);
		sb.append("setenv OUTPUT_TEXT_FILE3 $SCEN_DIR/share_data/allSites_Info.csv" + ls + ls);

		sb.append("# Run the tool" + ls);
		sb.append("$SA_HOME/bin/64bits/compute_EPICSiteData.exe" + ls + ls);

		sb.append("   if ( $status == 0 ) then " + ls);
		sb.append("      echo  ==== Finished crop site info generation. " + ls);
		sb.append("   else " + ls);
		sb.append("      echo  ==== Error in crop site info generation." + ls + ls);
		sb.append("      echo " + ls);
		sb.append("   endif " + ls);
		sb.append("#===================================================================" + ls);
		outMessages += "OUTPUT_TEXT_FILE $SCEN_DIR/share_data/EPICSite_Info.csv" + ls;
		outMessages += "OUTPUT_TEXT_FILE2 $SCEN_DIR/share_data/EPICSite_Crop.csv" + ls;
		outMessages += "OUTPUT_TEXT_FILE3 $SCEN_DIR/share_data/allSite_Info.csv" + ls;
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

	private String getScriptHeader() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to generate EPIC site table for EPIC modeling from:" + ls);
		sb.append("#     1. Grid description " + ls);
		sb.append("#     2. BELD4 NetCDF file - contains CROPF variable - 40 classes " + ls);
		sb.append("#     3. US county shapefile with attributes: STATE(string), COUNTY(string), " + ls);
		sb.append("#     FIPS(string), COUNTRY(string), STATEABB(string), REGION10(short)" + ls);
		sb.append("#     4. North American political state shapefile with attributes: " + ls);
		sb.append("#     COUNTRY(string), STATEABB(string) " + ls);
		sb.append("#     5. US 8-digit HUC boundary shapefile with attribute: HUC_8(string)" + ls);
		sb.append("#     6. DEM elevation data: meters and missing value=-9999" + ls);
		sb.append("#     7. DEM slope data: 0 to 90 degree with scalar 0.01 and missing value -9999" + ls);
		sb.append("#" + ls);
		sb.append("# Written by the Institute for the Environment at UNC, Chapel Hill" + ls);
		sb.append("# in support of the EPA CMAS project, 2012-2013." + ls);
		sb.append("#" + ls);
		sb.append("# Written by:   L. R., NOV. 2012" + ls);
		sb.append("#" + ls);
		sb.append("# Call program: compute_EPICSiteData.exe" + ls);
		sb.append("#               Needed environment variables included in the script file to run." + ls);
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
		String qSiteInfo = Constants.getProperty(Constants.QUEUE_SITE_INFO, msg);

		File script = new File(batchFile.replaceAll("\\\\", "\\\\\\\\"));

//		sb.append(qcmd + " " + qSiteInfo + " -o " + log + " " + script.getAbsolutePath());
		sb.append(script.getAbsolutePath() + " >& " + log);

		FileRunner.runScriptwCmd(batchFile, log, msg, sb.toString());
	}

	@Override
	public void newProjectCreated() {
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		rows.setValue(domain.getRows());
		cols.setValue(domain.getCols());
		xmin.setValue(domain.getXmin());
		ymin.setValue(domain.getYmin());
		xSize.setValue(domain.getXcellSize());
		ySize.setValue(domain.getYcellSize());
		proj4proj.setText(domain.getProj());
		gridName.setText(domain.getGridName());
		scenarioDir.setText(domain.getScenarioDir());

		String scenDir = domain.getScenarioDir().trim();
		String gName = domain.getGridName().trim();
		String nlcdY = domain.getNlcdYear().trim();
		String beld4file = scenDir + "/share_data/beld4_" + gName + "_" + nlcdY + ".nc";
		// File f = new File(beld4file);
		// if(f.exists()){
		this.beld4Dir.setText(beld4file);
		// }
		// else
		// this.beld4Dir.setText(scenDir);
		runMessages.setText("");
		minAcreas.setText("0.0");
		if (fields == null) {
			fields = new SiteInfoGenFields();
			app.getProject().addPage(fields);
		}
	}

	@Override
	public void projectLoaded() {
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		fields = (SiteInfoGenFields) app.getProject().getPage(fields.getName());

		if (fields != null) {
			String scenloc = domain.getScenarioDir();
			if (scenloc != null && scenloc.trim().length() > 0)
				this.scenarioDir.setText(scenloc);
			else
				this.scenarioDir.setText(fields.getScenarioDir());
			String scenDir = domain.getScenarioDir().trim();
			String gridNames = domain.getGridName().trim();
			String year = domain.getNlcdYear() == null ? "" : domain.getNlcdYear().trim();
			String beld4file = fields.getBeld4ncf();
			if (beld4file == null || beld4file.trim().isEmpty())
				beld4file = scenDir + "/share_data/beld4_" + gridName + "_" + year + ".nc";
			this.beld4Dir.setText(beld4file);
			this.runMessages.setText(fields.getMessage());
			rows.setValue(domain.getRows());
			cols.setValue(domain.getCols());
			xmin.setValue(domain.getXmin());
			ymin.setValue(domain.getYmin());
			xSize.setValue(domain.getXcellSize());
			ySize.setValue(domain.getYcellSize());
			proj4proj.setText(domain.getProj());
			gridName.setText(gridNames);
			minAcreas.setText(fields.getMinAcres() == null ? "0.0" : fields.getMinAcres());
			domain.setCMinAcres(domain.getCMinAcres());
		} else {
			newProjectCreated();
		}
		// setFalseEditable();
	}

	@Override
	public void saveProjectRequested() {
		if (scenarioDir != null)
			domain.setScenarioDir(scenarioDir.getText());
		if (scenarioDir != null)
			fields.setScenarioDir(scenarioDir.getText());
		if (rows != null)
			fields.setRows(Integer.parseInt(rows.getText() == null ? "0" : rows.getValue() + ""));
		if (beld4Dir != null)
			fields.setBeld4ncf(beld4Dir.getText() == null ? "" : beld4Dir.getText());
		if (cols != null)
			fields.setCols(Integer.parseInt(cols.getText() == null ? "0" : cols.getValue() + ""));
		if (xSize != null)
			fields.setXcellSize(Float.parseFloat(xSize.getText() == null ? "0" : xSize.getValue() + ""));
		if (ySize != null)
			fields.setYcellSize(Float.parseFloat(ySize.getText() == null ? "0" : ySize.getValue() + ""));
		if (xmin != null)
			fields.setXmin(Float.parseFloat(xmin.getText() == null ? "0" : xmin.getValue() + ""));
		if (ymin != null)
			fields.setYmin(Float.parseFloat(ymin.getText() == null ? "0" : ymin.getValue() + ""));
		if (proj4proj != null)
			fields.setProj(proj4proj.getText() == null ? "" : proj4proj.getText());
		if (gridName != null)
			fields.setGridName(gridName.getText() == null ? "" : gridName.getText());
		if (runMessages != null)
			fields.setMessage(runMessages.getText());
		if (minAcreas != null)
			fields.setMinAcres(minAcreas.getText());
		fields.setBeld4ncf(beld4Dir.getText().trim());
		domain.setCMinAcres(minAcreas.getText());
	}

}

package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.SiteFilesFields;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.FileRunner;
import gov.epa.festc.util.SpringLayoutGenerator;

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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import simphony.util.messages.MessageCenter;

public class UtilGenerateSiteFilesPanel extends UtilFieldsPanel implements PlotEventListener{
	private static final long serialVersionUID = 1326060715078017117L;
	
	private FestcApplication app;
	private MessageCenter msg;
	private SiteFilesFields fields;
	 
	private JTextField minAcreas;
	
	public UtilGenerateSiteFilesPanel(FestcApplication application, MessageCenter msg) {
		app = application;
		init();
		fields = new SiteFilesFields();
		app.getProject().addPage(fields);
		app.addPlotListener(this);
		this.msg = msg;
		add(createPanel());
	}
	
	private JPanel createPanel() {
		JPanel mainPanel = new JPanel();		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
        JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
	
		//this.scenarioDir = new JTextField(40);	
		JPanel minAcrePanel = new JPanel();
		minAcreas = new JTextField(20);
		minAcreas.setToolTipText("Default value is 0.0");
		minAcrePanel.add(minAcreas);
		 
		JPanel buttonPanel = new JPanel();
		JButton btn = new JButton(generateSiteFilesAction());
		buttonPanel.add(btn);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 0));
		
		layout.addLabelWidgetPair(Constants.LABEL_EPIC_SCENARIO, scenarioDirP, panel);
		layout.addLabelWidgetPair("Minimum Crop Acres: ", minAcrePanel, panel); 
	
		layout.makeCompactGrid(panel, 2, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading

		mainPanel.add(panel);
		mainPanel.add(buttonPanel);
		mainPanel.add(messageBox());
        return mainPanel;   
	}		

	private Action generateSiteFilesAction() {
		return new AbstractAction("Generate Site Files") {
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
		String baseDir = Constants.getProperty(Constants.EPIC_HOME, msg);
		if (baseDir == null || baseDir.isEmpty()) 
			throw new Exception("Base dir is empty, please specify in the configuration file!");
	 
		String scenarioDir = this.scenarioDir.getText(); 
		validateScen(scenarioDir);
		
		String minAcres = minAcreas.getText();
		if (minAcres == null || minAcres.isEmpty()) 
			throw new Exception("Minimum Crop Acres is not specified!");
		
		String sMAcres = domain.getCMinAcres();
		if (sMAcres == null || sMAcres.trim().isEmpty()) {
			domain.setCMinAcres(minAcres);
			sMAcres = minAcres;
		}	
		else if (sMAcres != null && !sMAcres.trim().isEmpty() 
			&& (Float.parseFloat(sMAcres))!=(Float.parseFloat(minAcres)) && app.allowDiffCheck()) 
			throw new Exception("Current minimum acre "+minAcres+ " is inconsistent with previous one (" + sMAcres + ")");	 
		
			 
		try {
			Float.parseFloat(minAcres);
		}catch(NumberFormatException e) {
			throw new Exception("Minimum Crop Acres is not a number!");
		}
		
		outMessages += "Epic base: " + baseDir + ls;
		outMessages += "Scen directory: " + scenarioDir + ls;
		
		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);

		final String jobFile = writeRunScriptScript(baseDir, scenarioDir);
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
		String qEpicSite = Constants.getProperty(Constants.QUEUE_EPIC_SITE, msg);

		File script = new File(batchFile.replaceAll("\\\\", "\\\\\\\\"));

		sb.append(qcmd + " " + qEpicSite + " -o " + log + " " + script.getAbsolutePath());

		FileRunner.runScriptwCmd(batchFile, log, msg, sb.toString());
	}
	
	protected String writeBatchFile(String jobFile, String scenarioDir) throws Exception {

		Date now = new Date(); // java.util.Date, NOT java.sql.Date or
								// java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		String batchFile = scenarioDir.trim() + "/scripts";
		if (!batchFile.endsWith(System.getProperty("file.separator")))
			batchFile += System.getProperty("file.separator");
		batchFile += "submitEpicSiteFile_" + timeStamp + ".csh";

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

	
	private String writeRunScriptScript( 
			String baseDir, 
			String scenarioDir ) throws Exception {

		Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

		String file = scenarioDir.trim() + "/scripts";
		if ( !file.endsWith(System.getProperty("file.separator"))) 
				file += System.getProperty("file.separator");
		file += "generateEpicSiteFile_" + timeStamp + ".csh";
		
		StringBuilder sb = new StringBuilder();
		sb.append(getScirptHeader());
		sb.append(getEnvironmentDef(baseDir, scenarioDir));
		sb.append(getSiteGrid());
		sb.append(getBeld4HUC8());
		
		String mesg = "";
		
		try {
			File script = new File(file);
			
	        BufferedWriter out = new BufferedWriter(new FileWriter(script));
	        out.write(sb.toString());
	        out.close();
	        
	        mesg += "Script file: " + file + ls;
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
	
	private String getScirptHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to run Site Creation and HUC8 Utilities" + ls); 
		sb.append("#" + ls);
		sb.append("# Written by: Fortran by Benson, Script by IE. 2010" + ls);
		sb.append("# Modified by:" + ls); 
		sb.append("#" + ls);
		sb.append("# Program: SITE_FILE_CREATOR.exe and SITEBELD4HUC8.exe" + ls);
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
		sb.append("setenv    WORK_DIR " + scenarioDir + "/work_dir" +ls);
		sb.append("setenv    SHARE_DIR " + scenarioDir + "/share_data" + ls);
		sb.append("setenv    SIT_DIR  " + "$SHARE_DIR/SIT" + ls);
		 
		return sb.toString();
	}
	
	private String getSiteGrid() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(ls + "#" + ls);
		sb.append("# set input variables" + ls);
		sb.append("#" + ls);
		
		sb.append("setenv INFILE1 \"EPICSites_Info.csv\"" + ls);
		sb.append(ls);
		
		sb.append(ls + "#" + ls);
		sb.append("# Set output variable" + ls);
		sb.append("#" + ls + ls);
		sb.append("if ( ! -e $SHARE_DIR/SITELIST.DAT ) rm -f $SHARE_DIR/SITELIST.DAT" + ls);
		sb.append("if ( ! -e $SIT_DIR  ) mkdir -p $SIT_DIR" + ls); 
		sb.append("if ( ! -e $WORK_DIR  ) mkdir -p $WORK_DIR" + ls);
		
		sb.append(ls);

		String exe;
		exe = "$EPIC_DIR//util/siteCreate/SITE_FILE_CREATOR.exe";
		
		sb.append(ls + "#" + ls);
		sb.append("# Generate site files " + ls);
		sb.append("#" + ls );
		sb.append("time " + exe + ls ); 
		sb.append("if ( $status == 0 ) then " + ls);
		sb.append("   echo  ==== Finished site creation step1. " + ls);
		sb.append("else " + ls);
		sb.append("   echo  ==== Error site creation step1." + ls );
		sb.append("   exit 1 " + ls );
		sb.append("endif " + ls);
		sb.append(ls);
		
		outMessages += "Exectable1: $EPIC_DIR//util/siteCreate/SITE_FILE_CREATOR.exe" +ls;
		outMessages += "Outputs: 1. SITELIST.DAT  ($SCEN_DIR/share_data)" +ls;
		outMessages += "Outputs: 2. All sitefiles ($SCEN_DIR//share_data/SIT)" +ls;

		return sb.toString();
	}
	
	private String getBeld4HUC8() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("# Delineation Soil, Site and Crop fractions" + ls);
		sb.append("#" + ls);
		sb.append("# set input variables" + ls);
		sb.append("#" + ls);
		
		sb.append("setenv INFILE2 \"EPICSites_Crop.csv\"" + ls +ls); 
		
		sb.append("# Set minimum crop acres for site selection " + ls );
		sb.append("setenv MINIMUM_CROP_ACRES   " + minAcreas.getText() +ls +ls );
		
		sb.append("# Create directories for crops " + ls);
		sb.append("foreach crop ( HAY ALFALFA OTHGRASS BARLEY EBEANS CORNG CORNS " +
				" COTTON OATS PEANUTS POTATOES RICE RYE SORGHUMG SORGHUMS SOYBEANS " +
				" SWHEAT WWHEAT OTHER CANOLA BEANS )" +ls );
		sb.append("  if ( ! -e $SCEN_DIR/$crop ) mkdir -p $SCEN_DIR/$crop " + ls);
		sb.append("end"  +ls ); 

		String exe;
		exe = "$EPIC_DIR/util/siteCreate/SITEBELD4HUC8.exe";
		sb.append("time " + exe + ls ); 
		sb.append("if ( $status == 0 ) then " + ls);
		sb.append("   echo  ==== Finished site creation step2. " + ls);
		sb.append("else " + ls);
		sb.append("   echo  ==== Error in site creation step2." + ls );
		sb.append("endif " + ls);
		sb.append("#" + ls + ls);
	  
		outMessages += "Exectable2: $EPIC_DIR/util/siteCreate/SITEBELD4HUC8.exe"+ls;
		//outMessages += "Input: SITELIST.DAT "+ls;
		outMessages += "Outputs: crop list files, $SCEN_DIR/$CROP ${CROP}-LIST.DAT...."+ls+ls;
		
		return sb.toString();
	}

	@Override
	public void projectLoaded() {
		fields = (SiteFilesFields) app.getProject().getPage(fields.getName());
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		if ( fields != null ){
			String scenloc = domain.getScenarioDir();
			if (scenloc != null && scenloc.trim().length()>0 )
				this.scenarioDir.setText(scenloc);
			else 
				this.scenarioDir.setText(fields.getScenarioDir());
			runMessages.setText(fields.getMessage());
			minAcreas.setText(fields.getMinAcres()==null? "0.0":fields.getMinAcres());
		} else{
			newProjectCreated();
		}
		domain.setCMinAcres(null);
	}

	@Override
	public void saveProjectRequested() {
		if ( scenarioDir != null ) domain.setScenarioDir(scenarioDir.getText());
		if ( scenarioDir != null ) fields.setScenarioDir(scenarioDir.getText());
		if ( runMessages != null ) fields.setMessage(runMessages.getText());
		if ( minAcreas != null)   fields.setMinAcres(minAcreas.getText());
		if ( runMessages != null ) fields.setMessage(runMessages.getText());
		domain.setCMinAcres(null);
	}

	@Override
	public void newProjectCreated() {
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		scenarioDir.setText(domain.getScenarioDir());	
		runMessages.setText("");
		minAcreas.setText("0.0");
		 
		if ( fields == null ) {
			fields = new SiteFilesFields();
			app.getProject().addPage(fields);
		}
	}
		
}

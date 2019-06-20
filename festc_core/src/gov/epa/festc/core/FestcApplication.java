package gov.epa.festc.core;

import gov.epa.festc.core.proj.CallBack;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.ProjectLoader;
import gov.epa.festc.core.proj.SiteInfoGenFields;
import gov.epa.festc.gui.CopyProjectPanel;
import gov.epa.festc.gui.DeleteProjectPanel;
import gov.epa.festc.gui.DomainPanel;
import gov.epa.festc.gui.HelpWindowWithContents;
import gov.epa.festc.gui.PlotEventListener;
import gov.epa.festc.gui.SaveProjectPanel;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.CustomDialog;
import gov.epa.festc.util.FileRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Level;

import saf.core.ui.util.FileChooserUtilities;
import simphony.util.messages.MessageCenter;
import simphony.util.messages.MessageEvent;
import simphony.util.messages.MessageEventListener;

/**
 * Main FEST-C application facade.
 * 
 * @author IE, UNC
 * @version $Revision$ $Date$
 */
public class FestcApplication implements ListSelectionListener,
		ListDataListener, CallBack {

	private static MessageCenter msg = MessageCenter
			.getMessageCenter(FestcApplication.class);

	private FestcGUI gui;

	private Project project;

	private File currentDir;
	private File infoDir;
	
	private File projFile;
	
	private String epicHome;
	private String workdir;
	
	private String sSimYear;	
	private String sFertYear;
	private String sMinAcre;
	
	private boolean allowDiffCheck;
	
	private enum ProjectEvent {CREATED, OPENED, SAVE, SAVED};
	
	private List<PlotEventListener> plotListeners = new ArrayList<PlotEventListener>();

	public FestcApplication() {
		project = new Project();
	}

	public FestcGUI getGui() {
		return gui;
	}

	public void init(FestcGUI gui) {
		this.gui = gui;
		MessageCenter.addMessageListener(new MessageEventListener() {
			public void messageReceived(MessageEvent messageEvent) {
				if (messageEvent.getLevel().equals(Level.ERROR)) {
					FestcApplication.this.gui.showMessage("Error", 
							(messageEvent.getMessage() != null ? messageEvent.getMessage().toString() : "")
							+ ": "
							+ messageEvent.getThrowable().getMessage());
				}
			}
		});

		//		String curdir = Constants.getProperty(Constants.EPIC_HOME, msg);
		//		String usrhome = Constants.getProperty(Constants.USER_HOME, msg);
		epicHome = Constants.getProperty(Constants.EPIC_HOME, msg);
		workdir = Constants.getProperty(Constants.WORK_DIR, msg);
		this.currentDir = new File(workdir + "/scenarios/scenariosInfo/");
		this.infoDir = new File(workdir + "/scenarios/scenariosInfo/");
		File logdir = new File(workdir + "/scenarios/scenariosInfo/logs");
		if ( !logdir.exists())
			logdir.mkdirs();
		String allow = Constants.getProperty(Constants.ALLOW_DIFF_CHECK, msg);
		allowDiffCheck = (allow != null && allow.equalsIgnoreCase("true")) ? true : false;
		//System.out.println("Allow: " + allow + allowDiffCheck);
	}

	/**
	 * Gets the currently loaded Project.
	 * 
	 * @return the currently loaded Project.
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * Gets the currently browsed directory.
	 * 
	 * @return the currently browsed directory.
	 */
	public File getCurrentDir() {
		return currentDir;
	}

	/**
	 * Sets the current directory for next browsing.
	 * 
	 * @return the current directory.
	 */
	public void setCurrentDir(File curdir) {
		currentDir = curdir;
	}
	
	/***
	 * Gets the modeling year associated with the app
	 */
	
//	public String getSSimYear() {
//		return this.sSimYear;
//	}
//	 
//	public void setSSimYear(String year) {
//		this.sSimYear = year;
//	}
	
	/***
	 * Sets the fertilizer year associated with the app
	 */
	
//	public String getSFertYear() {
//		return this.sFertYear;
//	}
//	
//	public void setSFertYear(String year) {
//		this.sFertYear = year;
//	}
	
	/***
	 * Sets the minimum crop acres associated with the app
	 */
	
//	public String getSMinAcre() {
//		return this.sMinAcre;
//	}
//	
//	public void setSMinAcre(String minAcre) {
//		this.sMinAcre = minAcre;
//	}

	/**
	 * Exits the application.
	 * 
	 * @return true if the exit was successful and the application should be
	 *         terminated, otherwise false.
	 */
	public boolean exit() {
		//
		return true;
	}

	public void valueChanged(ListSelectionEvent e) {
		//
	}

	// listeners for the formula model
	public void contentsChanged(ListDataEvent e) {
	}

	/**
	 * Shows help.
	 */
	public void showHelp() {
		String helpDir = System.getProperty("user.dir");
		if (helpDir == null)
			return;
		helpDir = helpDir + "/help";
		//String helpName1 = helpDir + "/userManualIndex.htm";
		String helpName2 = helpDir + "/userManual.htm";
		HelpWindowWithContents.showContents(null, "FEST-C User Guide",
				null, helpName2);

		//msg.warn("help: " + helpName1);
		msg.warn("help: " + helpName2);
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * Create a new project -- this is to create a new scenario from scratch.
	 * 
	 */
	public void createProject() {
		new CustomDialog(this, gui.getFrame(), true, new DomainPanel(this), 
				Constants.NEW_SCENARIO, "Create a New Scenario", projFile);
	}
	
	/**
	 * Copy from an existing project -- This opens a dialog.
	 * 
	 */
	public void copyProject() {
		new CustomDialog(this, gui.getFrame(), true, new CopyProjectPanel(this), 
				Constants.COPY_SCENARIO, "Copy from an Existing Scenario", projFile);
	}
	
	public void deleteProject() {
		new CustomDialog(this, gui.getFrame(), true, new DeleteProjectPanel(this), 
				Constants.DELETE_SCENARIO, "Delete an Existing Scenario", projFile);
	}
	
	public static MessageCenter getMsg() {
		return msg;
	}

	public File getProjFile() {
		return projFile;
	}

	public String getEpicHome() {
		return epicHome;
	}
	
	public String getWorkDir() {
		return workdir;
	}

	public List<PlotEventListener> getPlotListeners() {
		return plotListeners;
	}

	/**
	 * Opens a saved project -- a set of text fields and combobox selections. This opens a
	 * file dialog.
	 */
	public void openProject() {
		if (projFile != null ) {
//			int option = JOptionPane.showConfirmDialog(null, "Do you want to save scenario? ", "Confirmation", JOptionPane.YES_NO_OPTION);
//			if ( option == 0 )
				saveProject();
		}
		File file = FileChooserUtilities.getOpenFile(infoDir);
		openProject(file);
	}
	
	/**
	 * Opens the specified file as a project.
	 * 
	 * @param file the project file
	 */
	public void openProject(File file) {
		if (file != null) {
			try {
				ProjectLoader projLoader = new ProjectLoader();
				project = new Project();
				projLoader.load(file, project);
				projFile = file;
				currentDir = file.getParentFile();
				firePlotEvent(ProjectEvent.OPENED);
			} catch (Exception e) {
				msg.error("Error while loading project: ", e);
			}
		}
	}

	/**
	 * Saves the current project -- the current set of text fields and comboboxes.
	 * @throws Exception 
	 */
	public void saveProject() {
		new CustomDialog(this, gui.getFrame(), true, new SaveProjectPanel(projFile), Constants.SAVE_SCENARIO, "Save Scenario", projFile);		
	}
	
	/**
	 * Saves the current project as -- the current set of text fields and comboboxes.
	 */
	public void saveProjectAs() {
		File file = FileChooserUtilities.getSaveFile(currentDir);
		//if (file != null) {
			saveProj(file);
		//}
	}
	
	private void saveProj(File file) {
		if (file != null) {
			firePlotEvent(ProjectEvent.SAVE);

			try {
				ProjectLoader projLoader = new ProjectLoader();
				projLoader.save(file, project);
				projFile = file;
				currentDir = file.getParentFile();
			} catch (Exception e) {
				e.printStackTrace();
				msg.error("Error while saving project", e);
			}
		}
	}
	
	public void showTab(String tabId) {
		gui.setActive(tabId);
	}

	public static MessageCenter getMessageCenter() {
		return msg;
	}
	
	public void addPlotListener(PlotEventListener listener) {
		plotListeners.add(listener);
	}
	
	public void firePlotEvent(ProjectEvent event) {
		for (PlotEventListener listener : plotListeners) {
			if (event == ProjectEvent.CREATED)
				listener.newProjectCreated();
			
			if (event == ProjectEvent.OPENED)
				listener.projectLoaded();
			
			if (event == ProjectEvent.SAVE)
				listener.saveProjectRequested();
		}
	}
	
	public void showMessage(String title, String message) {
		this.gui.showMessage(title, message);
	}

	@Override
	public void onCall(String cmd, JPanel contentPanel) throws Exception {
		if (cmd.equals(Constants.NEW_SCENARIO)) {
			
			DomainPanel panel = (DomainPanel)contentPanel;
			panel.validateFields();
			String newScenario = panel.getScenaName();
			 
			projFile = new File(workdir + "/scenarios/scenariosInfo/", newScenario);
			if ( projFile.isFile() )
				throw new Exception("New scenario name already exist.");
			
			DomainFields domain =(DomainFields) project.getPage(DomainFields.class.getCanonicalName());
			if (domain == null ){
				domain = new DomainFields();
				project.addPage(domain);
			}
			domain.setGridName(panel.getGridName());			 
			domain.setProj(panel.getProj4proj());
		 
			domain.setCols(panel.getCols());
			domain.setRows(panel.getRows());
			domain.setXcellSize(panel.getxSize());
			domain.setYcellSize(panel.getySize());
			domain.setXmin(panel.getXmin());
			domain.setYmin(panel.getYmin());
			domain.setSimYear(panel.getSimuYear());
			domain.setNlcdYear(panel.getNlcdYear());
			domain.setCMinAcres("0.0");
			domain.setScenarioDir(workdir + "/scenarios/" + newScenario);	 
			 
			project.setName(newScenario);		 	
			
			/***
			 * NOTE: to populate all the domain info from newly created scenario, 
			 * a new method newProjectCreated() is added to PlotEventListener interface.
			 */
			 
			firePlotEvent(ProjectEvent.CREATED); 
			saveProj(projFile);
			
			newScenarioFold(newScenario, panel.getSimuYear());
			System.out.println("Created New Scenario: " + newScenario);
		}
		
		if (cmd.equals(Constants.COPY_SCENARIO)) {
			CopyProjectPanel panel = (CopyProjectPanel)contentPanel;
			panel.validateFields();
			String existScenNameWdir = panel.getExistScenario();
			String newScenName = panel.getNewScenName();		
			File existScenFile = new File(existScenNameWdir);
			//copyScenarioFold( existScenNameWdir, newScenName, panel.getSimuYear());
			if ( ! existScenFile.isFile() )
				throw new Exception("Scenario " + existScenNameWdir + " does not exist. " );
			
			try {
				openProject(existScenFile);
			} catch (Exception e) {
				msg.warn("Error copying scenario " + existScenNameWdir + ".", e.getMessage());
			} 
			SiteInfoGenFields fields = (SiteInfoGenFields) project.getPage(SiteInfoGenFields.class.getCanonicalName());
			DomainFields domain =(DomainFields) project.getPage(DomainFields.class.getCanonicalName());
			if (domain == null ){
				domain = new DomainFields();
				project.addPage(domain);
			}
			domain.setGridName(fields.getGridName());	 
			domain.setProj(fields.getProj());
			domain.setCols(fields.getCols());
			domain.setRows(fields.getRows());
			domain.setXcellSize(fields.getXcellSize());
			domain.setYcellSize(fields.getYcellSize());
			domain.setXmin(fields.getXmin());
			domain.setYmin(fields.getYmin());
			domain.setScenarioDir(workdir + "/scenarios/" + newScenName);
			domain.setSimYear(panel.getSimuYear());	
			// get default nlcd year from simulation year
			domain.setNlcdYear();
			domain.setCMinAcres(domain.getCMinAcres());
			project.setName(newScenName);
			
			projFile = new File(workdir + "/scenarios/scenariosInfo/", newScenName);
			if ( projFile.isFile() )
				throw new Exception("New scenario \"" + newScenName +  "\" already exist.");
			 
			System.out.println("Creating Scenario: " + newScenName);
			firePlotEvent(ProjectEvent.CREATED);
			System.out.println("Saving Scenario: " + newScenName);
			saveProj(projFile);
			/***
			 * NOTE: We still need to copy all the files from the existing scenario folder to the new scenario folder
			 */	
			System.out.println("Coping Scenario: " + newScenName);
			copyScenarioFold( existScenNameWdir, newScenName, panel.getSimuYear());
			//System.out.println("Finished Coping Scenario: " + newScenName);
		}
		
		if (cmd.equals(Constants.DELETE_SCENARIO)) {
			DeleteProjectPanel panel = (DeleteProjectPanel)contentPanel;
			panel.validateFields();
			String existScenName = panel.getExistScenario();
			 
			File existScenFile = new File(workdir + "/scenarios/scenariosInfo/", existScenName);
			if ( ! existScenFile.isFile() )
				throw new Exception("Scenario " + existScenName + " does not exist. " );
			int option = JOptionPane.showConfirmDialog(null, "Confirm that you want to delete scenario: " + existScenName + "? ", "Confirmation", JOptionPane.YES_NO_OPTION);
		    if ( option == 0 )
		    	deleteScenarioFold(existScenName);
		}

		if (cmd.equals(Constants.SAVE_SCENARIO)) {
			if (projFile != null) {
				firePlotEvent(ProjectEvent.SAVE);
				saveProj(projFile);
			}
		}
	}
	
	private void deleteScenarioFold(String oldName) {
		String file = workdir + "/scenarios/scenariosInfo/logs/delete_" +oldName + ".csh";
		String scenFile = workdir + "/scenarios/scenariosInfo/" +oldName;
		String oldScenDir = workdir + "/scenarios/" + oldName ;
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to delete an existing scenario" + ls); 
		sb.append("#" + ls);
		sb.append("#" + ls);
		sb.append("# Written by the Institute for the Environment at UNC, Chapel Hill" + ls);
		sb.append("# in support of the CMAS project, 2013" + ls);
		sb.append("#" + ls);
		sb.append("#" + ls);
		sb.append("#***************************************************************************************" + ls + ls);
		sb.append("rm -rf " + oldScenDir + ls);
		sb.append("rm -f " + scenFile + ls);
		try {
			File script = new File(file);
			Runtime.getRuntime().exec("chmod 755 " + script.getAbsolutePath());
	        BufferedWriter out = new BufferedWriter(new FileWriter(script));
	        out.write(sb.toString());
	        out.close();
	    } catch (IOException e) {
	    	msg.error("Error generating delete scenario script file", e);
	    	return;
	    }
		
		String log = file + ".log";
		FileRunner.runScript(file, log, msg);	 
		
	}
	
	private void copyScenarioFold(String oldNameWdir, String newName, String year) {
		Integer lIndex = oldNameWdir.lastIndexOf("/");
		String oldName = oldNameWdir.substring(lIndex+1);
		Integer sIndex =  oldNameWdir.indexOf("scenarios");
		String oldDir = oldNameWdir.substring(0, sIndex);
        //System.out.printf(oldDir + "  " + oldName);

		String file = workdir + "/scenarios/scenariosInfo/logs/copyScenario_" + newName+ "_from_" +oldName + ".csh"; ; 
		String oldScenDir = oldDir + "/scenarios/" + oldName ;
		String newScenDir = workdir + "/scenarios/" + newName ;
		String commonDir = epicHome + "/common_data"  ;
		
		year = "2"+(Integer.parseInt(year)-1);
		
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to copy all files from an existing scenario" + ls); 
		sb.append("#" + ls);
		sb.append("#" + ls);
		sb.append("# Written by the Institute for the Environment at UNC, Chapel Hill" + ls);
		sb.append("# in support of the CMAS project, 2010" + ls);
		sb.append("#" + ls);
		sb.append("#" + ls);
		sb.append("setenv EPIC_DIR   " +epicHome   +ls ); 
		sb.append("setenv SCEN_DIR   " +newScenDir   +ls  );
		sb.append("setenv COMM_DIR   " +commonDir   +ls + ls ); 
		sb.append("#***************************************************************************************" + ls + ls);
		sb.append("cp -R " + oldScenDir  + " " + newScenDir + ls);
		sb.append("sed -i '1s/^.\\{,8\\}/   " + year+ "/' $SCEN_DIR/share_data/EPICCONT.DAT"  +ls ) ;
		
		try {
			File script = new File(file);
			Runtime.getRuntime().exec("chmod 755 " + script.getAbsolutePath());
	        BufferedWriter out = new BufferedWriter(new FileWriter(script));
	        out.write(sb.toString());
	        out.close();
	    } catch (IOException e) {
	    	msg.error("Error generating copy scenario script file", e);
	    	return;
	    }
		
		String log = file + ".log";
		FileRunner.runScript(file, log, msg);
		
	}

	private void newScenarioFold(String newName, String year) {
		String file = workdir + "/scenarios/scenariosInfo/logs/createScenario_"+ newName + ".csh";  
		String newScenDir = workdir + "/scenarios/" + newName ;
		String commonDir = epicHome + "/common_data"  ;
		year = "2"+(Integer.parseInt(year)-1);
		
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh " + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to copy all files from an existing scenario" + ls); 
		sb.append("#" + ls);
		sb.append("#" + ls);
		sb.append("# Written by the Institute for the Environment at UNC, Chapel Hill" + ls);
		sb.append("# in support of the CMAS project, 2010" + ls);
		sb.append("#" + ls); 
		sb.append("#" + ls);
		sb.append("#***************************************************************************************" + ls + ls);
		sb.append("setenv EPIC_DIR   " +epicHome   +ls ); 
		sb.append("setenv SCEN_DIR   " +newScenDir   +ls  );
		sb.append("setenv COMM_DIR   " +commonDir   +ls + ls ); 
		
		sb.append("# mkdir case dirs   " +ls ); 
		sb.append("mkdir -p  $SCEN_DIR/share_data"   +ls ); 
		sb.append("mkdir -p  $SCEN_DIR/scripts"   +ls ); 
		sb.append("mkdir -p  $SCEN_DIR/work_dir"   +ls ); 
		
		String copyCmd = "cp $COMM_DIR/EPIC_model/app/EPICCONT.DAT $SCEN_DIR/share_data/."   +ls;
		
		String qSingModule = Constants.getProperty(Constants.QUEUE_SINGULARITY_MODULE, msg);
		if (qSingModule != null && !qSingModule.trim().isEmpty()) {
			sb.append("module load " + qSingModule + ls);
		}
		String qSingImage = Constants.getProperty(Constants.QUEUE_SINGULARITY_IMAGE, msg);
		String qSingBind = Constants.getProperty(Constants.QUEUE_SINGULARITY_BIND, msg);
		if (qSingImage != null && !qSingModule.trim().isEmpty()) {
			sb.append("set CONTAINER = " + qSingImage + ls);
			sb.append("singularity exec");
			if (qSingBind != null && !qSingBind.trim().isEmpty()) {
				sb.append(" -B " + qSingBind);
			}
			sb.append(" $CONTAINER " + copyCmd);
		} else {
			sb.append(copyCmd);
		}
		sb.append("sed -i '1s/^.\\{,8\\}/   " + year+ "/' $SCEN_DIR/share_data/EPICCONT.DAT"  +ls ) ;

		try {
			File script = new File(file);
			Runtime.getRuntime().exec("chmod 755 " + script.getAbsolutePath());
	        BufferedWriter out = new BufferedWriter(new FileWriter(script));
	        out.write(sb.toString());
	        out.close();
	    } catch (IOException e) {
	    	msg.error("Error generating new scenario script file", e);
	    	return;
	    }
		
		String log = file + ".log";
			
		FileRunner.runScript(file, log, msg);
		
		
//		msg.warn("Please modify " + newScenDir + "/share_data/EPICCONT.DAT ", "");
	}

	public boolean allowDiffCheck() {
		return allowDiffCheck;
	}

}

package gov.epa.festc.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.FileRunner;
import gov.epa.festc.util.SpringLayoutGenerator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import simphony.util.messages.MessageCenter;

public class UtilGenerateSoilCreationPanel extends JPanel {
	
	private static final String LABEL_EPIC_SCENARIO = "Select Scenario Direcotry:";
	
	private static final String EXE_SUB_DIR = "/util/soilMatch"; // may need to change later
	private static final String EXE_NAME    = "BELD4HUC8.exe";   // need to change later
	
	private FestcApplication app;
	private MessageCenter msg;
	
	private JTextField scenarioDir;
	private JButton scenarioDirBrowser;
 
	private JTextArea runMessages;
	private String outMessages;
	private String ls ="\n";
	
	public UtilGenerateSoilCreationPanel(FestcApplication application, MessageCenter msg) {
		app = application;
		this.msg = msg;
		add(createPanel());
	}
	
	private JPanel createPanel() {
		JPanel mainPanel = new JPanel();		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		 
        JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		
		JPanel scenarioPanle = new JPanel();
		this.scenarioDir = new JTextField(40);
		this.scenarioDirBrowser = new JButton(browseDirAction(LABEL_EPIC_SCENARIO, scenarioDir));
		scenarioPanle.add(this.scenarioDir);
		scenarioPanle.add(scenarioDirBrowser);
		
		JPanel buttonPanel = new JPanel();
		JButton btn = new JButton(generateSoilCreationAction());
		btn.setEnabled(false);
		buttonPanel.add(btn);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 0));
		
		this.runMessages = new JTextArea(20, 45);	 
		
		layout.addLabelWidgetPair(LABEL_EPIC_SCENARIO, scenarioPanle, panel);
		layout.addLabelWidgetPair("", new JLabel(""), panel);
	
		layout.makeCompactGrid(panel, 2, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading

		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
		this.runMessages = new JTextArea("", 6, 60);
		runMessages.setLineWrap(true);
		JScrollPane messageScroll = new JScrollPane(runMessages, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		messagePanel.add(new JLabel("Run Messages: "));
		messagePanel.add(messageScroll);
		
		mainPanel.add(panel);		
		mainPanel.add(buttonPanel);
		mainPanel.add(messagePanel);
        return mainPanel;
	}
	
	private Action browseDirAction(final String name, final JTextField text) {
		return new AbstractAction("Browse...") {
			private static final long serialVersionUID = 2207093276203069799L;

			public void actionPerformed(ActionEvent e) {
				chooseDir( name, text);
			}
		};
	}

	private void chooseDir( final String name, final JTextField text) {
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
		chooser.setAcceptAllFileFilterUsed(false);

		int option = chooser.showDialog(UtilGenerateSoilCreationPanel.this,
				"Select");
		if (option == JFileChooser.APPROVE_OPTION) {
			File selected = chooser.getSelectedFile();
			if (selected.exists() && selected.isDirectory()) {
				text.setText("" + selected); 
			} else {
				text.setText("" + selected.getParent());
			}
			app.setCurrentDir(selected);
		}
	}
	

	private Action generateSoilCreationAction() {
		return new AbstractAction("RUN") {
			private static final long serialVersionUID = 5558465823154735475L;

			public void actionPerformed(ActionEvent e) {
				try {
					
					generateSoilCreationFiles();
					
				} catch (Exception exc) {
					//msg.error("ERROR", exc);
					app.showMessage("Run script", exc.getMessage());
				}
			}


		};
	}
	
	private void generateSoilCreationFiles() {
		String baseDir = Constants.getProperty(Constants.EPIC_HOME, msg);
		if (baseDir == null || baseDir.isEmpty()) {
			app.showMessage("Utilitied", "Base dir is empty, please specify in the configuration file!");
			return;
		}
		outMessages += "Epic base: " + baseDir + ls;
		
		String scenarioDir = this.scenarioDir.getText();
		if ( scenarioDir == null || scenarioDir.isEmpty()) {
			app.showMessage("Utilities", "Please select scenario dir first!");
			return;
		}
		 
		final String file = writeSiteGenerationScript(baseDir, scenarioDir);
		
		Thread populateThread = new Thread(new Runnable() {
			public void run() {
				runScript(file);
			}
		});
		populateThread.start();
	}
	
	private void runScript(final String file) {
		String log = file + ".log";
		 
		runMessages.validate();
		FileRunner.runScript(file, log, msg);
	}
	
	protected String writeSiteGenerationScript( 
			String baseDir, 
			String scenarioDir) {
		
		Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		String file = scenarioDir.trim() + "/scripts";
	    
		if ( !file.endsWith(System.getProperty("file.separator"))) 
				file += System.getProperty("file.separator");
		file += "runEpicSiteGeneration_" + timeStamp + ".csh";
		
		StringBuilder sb = new StringBuilder();
		sb.append(getScirptHeader());
		sb.append(getEnvironmentDef(baseDir, scenarioDir));
		sb.append(getRunDef());
		
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
	        outMessages += mesg +ls;
	        
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	//msg.error("Error generating EPIC script file", e);
	    	app.showMessage("Write script", e.getMessage());
	    } 
		
	    app.showMessage("Write script", mesg);
	    
		return file;
	}
	
	private String getScirptHeader() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		sb.append("#!/bin/csh -f" + ls);
		sb.append("#**************************************************************************************" + ls);
		sb.append("# Purpose:  to run EPIC model" + ls); 
		sb.append("#" + ls);
		sb.append("# Written by: Fortran by Benson, Script by IE. 2010" + ls);
		sb.append("# Modified by:" + ls); 
		sb.append("#" + ls);
		sb.append("# Program: Site12kmGrid.exe" + ls);
		sb.append("#         Needed environment variables included in the script file to run." + ls);        
		sb.append("# " + ls);
		sb.append("#***************************************************************************************" + ls + ls);
		
		return sb.toString();
	}
	
	private String getEnvironmentDef(String baseDir, String scenarioDir) {
		StringBuilder sb = new StringBuilder();
		
		String ls = "\n";
		sb.append(ls + "#" + ls);
		sb.append("# Define environment variables" + ls);
		sb.append("#" + ls + ls);
		sb.append("setenv EXBASE   " + baseDir + EXE_SUB_DIR + ls);
		sb.append("setenv EXEC     " + EXE_NAME + ls);
		sb.append("setenv SCEN_DIR " + scenarioDir + ls); // may not need this
		sb.append("" + ls);

		return sb.toString();
	}
	
	private String getRunDef() {
		StringBuilder sb = new StringBuilder();
		String ls = "\n";
		String exe;
		exe = "$EXBASE/$EXEC";
		
		sb.append(ls + "#" + ls);
		sb.append("# Generate soil match files " + ls);
		sb.append("#" + ls + ls);
		sb.append("time " + exe + ls ); 
		sb.append("exit()" + ls);
		sb.append(ls);

		return sb.toString();
	}
		
}

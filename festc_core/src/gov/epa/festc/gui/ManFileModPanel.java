package gov.epa.festc.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.FestcGUI;
import gov.epa.festc.core.proj.DomainFields;
import gov.epa.festc.core.proj.ManFileModFields;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.SpringLayoutGenerator;
import simphony.util.messages.MessageCenter;

public class ManFileModPanel extends UtilFieldsPanel implements PlotEventListener {
	private static final long serialVersionUID = 8617841823458370182L;
	
	private static final String indent = "            ";
	//private static final String LABEL_EPIC_SCENARIO = indent + "Select Scenario Direcotry:";
	 
	private JList lstCrops;
	
	private MessageCenter msg;
	private String baseDir;
	private String scenDir;
	private String contFile;
	private String paramFile;
	private String fileFile;
	private String irrFile;
	private String rainFile;
	private String epicVer;
	
	private FestcGUI parent;
	private FestcApplication app;
	private ManFileModFields fields;
	 
	private JRadioButton applicationBtn, spinupBtn;
	private boolean isSpinup = false;

	public ManFileModPanel(FestcApplication application) {
		app = application;
		msg = app.getMessageCenter();
		epicVer = Constants.getProperty(Constants.EPIC_VER, msg).trim();
		fields = new ManFileModFields();
		app.getProject().addPage(fields);
		app.addPlotListener(this);
		add(createPanel());
	}
	
	private JPanel createPanel() {
		JPanel main = new JPanel();
		init();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		main.add(getNorthPanel() );
		main.add(getCenterPanel() );
		main.add(getSouthPanel() );
		main.add(messageBox());
		return main;
	}

	private JPanel getNorthPanel() {
		JPanel panel = new JPanel();
		JLabel title = new JLabel(Constants.EDIT_INFILES, SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		return panel;
	}

	private JPanel getSouthPanel() {		
		return new JPanel();
	}

	private JPanel getCenterPanel() {
		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		
		//this.scenarioDir = new JTextField(40);
		
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

		JPanel cropsPanel = new JPanel(new BorderLayout());
		cropsPanel.add(new JLabel("Crops/Grasses"), BorderLayout.NORTH);
		lstCrops = new JList(Constants.CROPS.keySet().toArray());
		lstCrops.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstCrops.setVisibleRowCount(20);
		JScrollPane scrollCropList = new JScrollPane(lstCrops);
		scrollCropList.setPreferredSize(new Dimension(120, 220));
		cropsPanel.add(scrollCropList, BorderLayout.SOUTH);
		 
		JPanel leftPanel = new JPanel();
		leftPanel.add(cropsPanel);
		 
		layout.addLabelWidgetPair(Constants.LABEL_EPIC_SCENARIO, scenarioDirP, panel);
		layout.addLabelWidgetPair(indent + "Simulation Type:", spinupPanel, panel);
		layout.addLabelWidgetPair("   ", new JLabel("   "), panel);
		layout.addWidgetPair(leftPanel, rightPanel(), panel);

		layout.makeCompactGrid(panel, 4, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading

		return panel;
	}

	private JPanel rightPanel(){
		JPanel rightPanel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		
		JPanel contPanel = new JPanel();
		JButton vcontButton = new JButton(viewAction("cont"));
		JButton econtButton = new JButton(editAction("cont"));
		contPanel.add(vcontButton);
		contPanel.add(econtButton);
		JPanel paramPanel = new JPanel();
		JButton vparamButton = new JButton(viewAction("param"));
		JButton eparamButton = new JButton(editAction("param"));
		paramPanel.add(vparamButton);
		paramPanel.add(eparamButton);
		JPanel filePanel = new JPanel();
		JButton vfileButton = new JButton(viewAction("file"));
		JButton efileButton = new JButton(editAction("file"));
		filePanel.add(vfileButton);
		filePanel.add(efileButton);
		JPanel irrPanel = new JPanel();
		JButton virrButton = new JButton(viewAction("irr"));
		JButton eirrButton = new JButton(editAction("irr"));
		irrPanel.add(virrButton);
		irrPanel.add(eirrButton);
		JPanel rainPanel = new JPanel();
		JButton vrainButton = new JButton(viewAction("rain"));
		JButton erainButton = new JButton(editAction("rain"));
		rainPanel.add(vrainButton);
		rainPanel.add(erainButton);
		
		layout.addLabelWidgetPair("EPICCONT.DAT", contPanel, rightPanel);
		if ( epicVer.equalsIgnoreCase("1102") ) 
			layout.addLabelWidgetPair("PARM1102.DAT", paramPanel, rightPanel);
		else 
			layout.addLabelWidgetPair("PARM0509.DAT", paramPanel, rightPanel);
		layout.addLabelWidgetPair("EPICFILE.DAT", filePanel, rightPanel);
		 
		layout.addLabelWidgetPair("RAINFED EPICRUNFILE", rainPanel, rightPanel);
		layout.addLabelWidgetPair("IRRIGATED EPICRUNFILE", irrPanel, rightPanel);

		layout.makeCompactGrid(rightPanel, 5, 2, // number of rows and cols
				10, 10, // initial X and Y
				50, 12); // x and y pading
		
		return rightPanel;
	}
	
	public void setParent(FestcGUI parent) {
		this.parent = parent;
	}
	
	public Action viewAction(final String name){
		return new AbstractAction("View"){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				if (validateFields()){

					String file = getFile(name);
					String title = "View: " + file;

					ViewFileDialog dialog = new ViewFileDialog(parent.getFrame(), title, new File(file), false);
					dialog.setVisible(true);
				}
			 
			}
		};
	}
	
	public Action editAction(final String name){
		return new AbstractAction("Edit"){
			public void actionPerformed(ActionEvent e) {
				if ( validateFields()) {
					String file = getFile(name);
					String title = "Edit: " + file;
					ViewFileDialog dialog = new ViewFileDialog(parent.getFrame(), title, new File(file), true);
					dialog.setVisible(true);
				}
			}
		};
	}

	
	private String getFile(String name){
		String file="";
		if ( name.equalsIgnoreCase("cont")) 
			file = contFile;
		else if ( name.equalsIgnoreCase("param"))
				file = paramFile;
		else if ( name.equalsIgnoreCase("file"))
			file = fileFile;
		else if ( name.equalsIgnoreCase("irr"))
			file = irrFile;
		else if ( name.equalsIgnoreCase("rain"))
			file = rainFile;
        return file;	
	}
	
	private boolean validateFields(){
		String type = "";
		if ( isSpinup ) 
			type = "spinup";
		else
			type = "app";
			
		baseDir = Constants.getProperty(Constants.EPIC_HOME, msg);
		baseDir = baseDir.trim();
		if (baseDir == null || baseDir.isEmpty()) {
			app.showMessage("Utilities", "Base dir is empty, please specify it in the configuration file!");
			return false;
		}
		 
		scenDir = this.scenarioDir.getText();
		if (scenDir == null || scenDir.isEmpty()) {
			app.showMessage("Utilities", "Scenario dir is empty!");
			return false;
		}
		
		String selCrop = (String) lstCrops.getSelectedValue();
		if ( selCrop == null || selCrop.trim().length() == 0) {
			app.showMessage("View/Edit Files: ", "Please select a crop first!");
			return false;
		}
		if ( selCrop.equalsIgnoreCase("POTATOES") && type.equalsIgnoreCase("spinup")) 
			contFile = baseDir + "/common_data/EPIC_model/" + type + "/EPICCONT_POTATOES.DAT"; 
		else if ( type.equalsIgnoreCase("spinup")) 
			contFile = baseDir + "/common_data/EPIC_model/" + type + "/EPICCONT.DAT"; 
		else
			contFile = scenDir + "/share_data/EPICCONT.DAT"; 
		
		if ( epicVer.equalsIgnoreCase("1102"))
			paramFile = baseDir + "/compmon_data/EPIC_model/" + type + "/PARM1102.DAT";
		else
			paramFile = baseDir + "/compmon_data/EPIC_model/" + type + "/PARM0509.DAT";
		
		fileFile = baseDir + "/common_data/EPIC_model/" + type + "/EPICFILE.DAT";
		if  (type.equalsIgnoreCase("spinup")) {
			irrFile = scenDir + "/" + selCrop + "/" + type + "/manage/EPICRUNFILEIRR.DAT";
			rainFile = scenDir + "/" + selCrop + "/" + type + "/manage/EPICRUNFILERAIN.DAT";
		}
		else {
			irrFile = scenDir + "/" + selCrop + "/" + type + "/manage/EPICRUNFILEIRRDW.DAT";
			rainFile = scenDir + "/" + selCrop + "/" + type + "/manage/EPICRUNFILERAINDW.DAT";
		}
		outMessages += "Epic base: " + baseDir + ls +ls; 
		outMessages += "Files: " + ls;
		outMessages +=  contFile + ls;
		outMessages +=  paramFile + ls;
		outMessages +=  fileFile + ls;
		outMessages +=  irrFile + ls;
		outMessages +=  rainFile + ls;
		 
		runMessages.setText(outMessages);
		return true;
	}
	

	private Action spinupSelection() {
		return new AbstractAction("EPIC SPINUP") {
			public void actionPerformed(ActionEvent e) {
				processSpinup();
			}
				
		};
	}
	
	private Action applicationSelection() {
		return new AbstractAction("EPIC APP") {
			public void actionPerformed(ActionEvent e) {
				processSpinup();
			}
				
		};
	}
	
	private void processSpinup() {
		if ( applicationBtn.isSelected()) {
			isSpinup = false;
		} else {
			isSpinup = true;
		}
		validateFields(); 
	}

	@Override
	public void newProjectCreated() {
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		scenarioDir.setText(domain.getScenarioDir());
		runMessages.setText("");
		if ( fields == null ) {
			fields = new ManFileModFields();
			app.getProject().addPage(fields);
		}
	}
	
	public void projectLoaded() {
		fields = (ManFileModFields) app.getProject().getPage(fields.getName());
		domain = (DomainFields) app.getProject().getPage(DomainFields.class.getCanonicalName());
		if ( fields != null ){
			String scenloc = domain.getScenarioDir();
			if (scenloc != null && scenloc.trim().length()>0 )
				this.scenarioDir.setText(scenloc);
			else 
				this.scenarioDir.setText(fields.getScenarioDir());
			if ( fields.getMessage() != null ) 
				runMessages.setText(fields.getMessage());
			else
				runMessages.setText("");
		}
		else{
			newProjectCreated();
		}
	}

	public void saveProjectRequested() {
		if ( scenarioDir != null ) domain.setScenarioDir(scenarioDir.getText());
		if ( scenarioDir != null ) fields.setScenarioDir(scenarioDir.getText());
		if ( runMessages != null ) fields.setMessage(runMessages.getText().trim());
	}				
}

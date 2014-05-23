package gov.epa.festc.gui;

import java.io.File;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.util.BrowseAction;
import gov.epa.festc.util.SpringLayoutGenerator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class CopyProjectPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3788755800192153895L;

	JTextField scenario;
	JTextField newScenName;
	JTextField simuYear;
	
	public CopyProjectPanel(FestcApplication app) {
		super(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();

		JPanel existScen = new JPanel();
		scenario = new JTextField(20);
		newScenName = new JTextField(20);
		simuYear = new JTextField(20);
		
		File scenFileHome = new File(app.getEpicHome() + "/scenarios/scenariosInfo");
		//System.out.println(scenFileHome);
		if (!scenFileHome.exists()) scenFileHome = app.getCurrentDir();
		
		JButton browser = new JButton(BrowseAction.browseAction(this, scenFileHome, "scenario file", scenario));
		existScen.add(scenario);
		existScen.add(browser);

		JPanel newScen = new JPanel();
		newScen.add(newScenName);
		
		JPanel simuYearPanel = new JPanel();
		simuYearPanel.add(simuYear);

		layout.addLabelWidgetPair("Existing Scenario:", existScen, this);
		layout.addLabelWidgetPair("New Scenario Name:", newScen, this);
		layout.addLabelWidgetPair("Simulation Year: ", simuYearPanel, this);

		layout.makeCompactGrid(this, 3, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getExistScenario() {
		return scenario.getText();
	}

	public String getNewScenName() {
		return newScenName.getText();
	}
	
	public void validateFields() throws Exception{
		 
		String existScenario = scenario.getText() == null ? "" : scenario.getText();
		if (existScenario.trim().isEmpty())
			throw new Exception("Existing scenario is empty.");
		 
		String newScenN = newScenName.getText() == null ? "" : newScenName.getText();
		if (newScenN.trim().isEmpty())
			throw new Exception("New scenario name is empty.");	
		if (newScenN.trim().length() > 16)
			throw new Exception(" New scenario name is too long (larger than 16 chars).");
		if (newScenN.trim().contains(" ") )
			throw new Exception(" New scenario name has space in between.");
		
		String simuY = simuYear.getText() == null ? "" : simuYear.getText();
		if (simuY.trim().isEmpty())
			throw new Exception("Simulation year is empty.");
		
		try {
			Integer.parseInt(simuY);
		}catch(NumberFormatException e) {
			throw new Exception("Simulation year is not a number.");
		}
		
	}
	
	public String getSimuYear() throws Exception {
		return simuYear.getText();
	}

}

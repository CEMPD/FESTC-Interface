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

public class DeleteProjectPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3788755800192153895L;

	JTextField scenario;
	
	public DeleteProjectPanel(FestcApplication app) {
		super(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();

		JLabel from = new JLabel("Existing Scenario:");

		JPanel existScen = new JPanel();
		scenario = new JTextField(20);
		
		File scenFileHome = new File(app.getEpicHome() + "/scenarios/scenariosInfo");
		System.out.println(scenFileHome);
		if (!scenFileHome.exists()) scenFileHome = app.getCurrentDir();
		
		JButton browser = new JButton(BrowseAction.browseAction(this, scenFileHome, "scenario file", scenario));
		existScen.add(scenario);
		existScen.add(browser);
 
		layout.addWidgetPair(from, existScen, this); 

		layout.makeCompactGrid(this, 1, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getExistScenario() {
		return scenario.getText();
	}

 
	public void validateFields() throws Exception{
		 
		String existScenario = scenario.getText() == null ? "" : scenario.getText();
		if (existScenario.trim().isEmpty())
			throw new Exception("Existing scenario is empty.");
		if (existScenario.trim().contains(" ") )
			throw new Exception("Existing scenario name has space in between.");
	}
	
}

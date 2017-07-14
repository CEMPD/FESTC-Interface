package gov.epa.festc.gui;

import java.io.File;

import gov.epa.festc.core.FestcApplication;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SaveProjectPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1146710595018432609L;
	JTextField scenarioName;

	public SaveProjectPanel(File projFile) {
		scenarioName = new JTextField(30);

		if (projFile == null) {
			JLabel warning = new JLabel("No opened scenario to save. ");
			this.add(warning);
		}
		else{
			JLabel warning = new JLabel("Save Scenario: " + projFile.getName() + "?");
			this.add(warning);
		}
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public String getScenarioName() {
		return scenarioName.getText();
	}
	
}

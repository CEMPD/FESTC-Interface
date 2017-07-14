package gov.epa.festc.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.util.Constants;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import simphony.util.messages.MessageCenter;

public class CreateSoilFilesPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2445494569047990742L;
	
	private FestcApplication app;
	private MessageCenter msg;
	
	private JPanel northPanel = null;
	private JPanel centerPanel = null; 
	
	public CreateSoilFilesPanel(FestcApplication application) {
		super(new GridLayout(1, 1));
		app = application;
		msg = app.getMessageCenter();
		add(createPanel());
	}
	
	private JPanel createPanel() {
		JPanel main = new JPanel(new BorderLayout());
		
		northPanel = getNorthPanel();
		centerPanel = getCenterPanel();
		main.add(northPanel, BorderLayout.NORTH);
		main.add(centerPanel, BorderLayout.CENTER);
		return main;
	}
	
	private JPanel getNorthPanel() {
		JPanel panel = new JPanel();
		JLabel title = new JLabel(Constants.SOIL_MATCH, SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		return panel;
	}
	
	private JPanel getCenterPanel() { 
		JPanel panel = new JPanel(); //new GridLayout(1, 1)); 
        JComponent panel2 = new UtilGenerateSoilMatchPanel(app, this.msg);
   
        panel.add(panel2);
		return panel;
	}
	
	protected JComponent makeXXCreatePanel() {
        JPanel panel = new JPanel(false);

        return panel;
    }	

}

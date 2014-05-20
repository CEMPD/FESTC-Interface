package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.util.Constants;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import simphony.util.messages.MessageCenter;

public class CreateSiteFilesPanel extends JPanel {

	//private static final String TITLE = "Epic Site File Generation";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FestcApplication app;
	private MessageCenter msg;
	
	
	private JPanel northPanel = null;
	private JPanel centerPanel = null;

	public CreateSiteFilesPanel(FestcApplication application) {
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
//		main.add(getSouthPanel(), BorderLayout.SOUTH);
		
		return main;
	}
	
	private JPanel getNorthPanel() {
		JPanel panel = new JPanel();
		JLabel title = new JLabel(Constants.EPIC_SITE, SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		return panel;
	}
	
	private JPanel getCenterPanel() { 
		        
		UtilGenerateSiteFilesPanel sitePanel = new UtilGenerateSiteFilesPanel(app, this.msg);
       
		return sitePanel;
	}
	
	protected JComponent makeXXCreatePanel() {
        JPanel panel = new JPanel(false);

        return panel;
    }
}



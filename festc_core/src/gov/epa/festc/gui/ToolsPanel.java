package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.FestcConstants;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.SpringLayoutGenerator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class ToolsPanel extends JPanel {
	private static final long serialVersionUID = 8082261605112772487L;
	private FestcApplication app;

	public ToolsPanel(FestcApplication festcApp) {
		app = festcApp;
		add(createPanel());
		super.requestFocusInWindow();
	}

	private JPanel createPanel() {
		JPanel main = new JPanel(new BorderLayout());
		main.add(getNorthPanel(), BorderLayout.NORTH);
		main.add(getCenterPanel(), BorderLayout.CENTER);
		Dimension d = main.getMinimumSize();
		d.width = 120;
		main.setMinimumSize(d);
		d = main.getMaximumSize();
		d.width = 120;
		main.setMaximumSize(d);
		return main;
	}

	private JPanel getNorthPanel() {
		JPanel panel = new JPanel();
		JLabel title = new JLabel(
				"FEST-C Tools", //"Fertilizer Emission Scenario Tool for CMAQ (FEST-C)", // Tools", 
				SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

		return panel;
	}

	private JPanel getCenterPanel() {
		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();
		//hsb = Color.RGBtoHSB(r,g,b,hsb);
		
		JGradientButton landUsebutton = new JGradientButton(buttonAction(Constants.BELD4_GEN, FestcConstants.BELD4_VIEW),
				 Color.getHSBColor(210f / 360, 0.25f, 0.95f));
		JGradientButton sitegenbutton = new JGradientButton(buttonAction(Constants.SITE_INFO, FestcConstants.SITE_INFO_VIEW),
				 Color.getHSBColor(210f / 360, 0.25f, 0.95f));
		JGradientButton m2pbutton = new JGradientButton(buttonAction(Constants.MC2EPIC, FestcConstants.MCIP2EPIC_VIEW),
				Color.getHSBColor(210f / 360, 0.25f, 0.95f));
		JGradientButton sitefilebutton = new JGradientButton(buttonAction(Constants.EPIC_SITE, FestcConstants.SITE_FILE_VIEW),
				Color.getHSBColor(120f / 360, 0.25f, 0.95f));
		JGradientButton genbutton = new JGradientButton(buttonAction(Constants.SOIL_MATCH, FestcConstants.GEN_SOIL_MAN_FILES_VIEW),
				Color.getHSBColor(120f / 360, 0.25f, 0.95f));
		JGradientButton manfilespinbutton = new JGradientButton(buttonAction(Constants.MAN_SPINUP, FestcConstants.MAN_FILE_SPINUP_VIEW),
				Color.getHSBColor(120f / 360, 0.25f, 0.95f));
		JGradientButton modbutton = new JGradientButton(buttonAction(Constants.EDIT_INFILES, FestcConstants.MANAGE_VIEW),
				Color.getHSBColor(50f / 360, 0.25f, 0.95f));
		JGradientButton epicbutton = new JGradientButton(buttonAction(Constants.EPIC_SPINUP, FestcConstants.EPIC_VIEW),
				Color.getHSBColor(30f / 360, 0.25f, 0.95f));
		JGradientButton manfileappbutton = new JGradientButton(buttonAction(Constants.MAN_APP, FestcConstants.MAN_FILE_APP_VIEW),
				Color.getHSBColor(30f / 360, 0.25f, 0.95f));
		JGradientButton epicappbutton = new JGradientButton(buttonAction(Constants.EPIC_APP, FestcConstants.EPIC4APP_VIEW),
				Color.getHSBColor(30f / 360, 0.25f, 0.95f));
		JGradientButton eya2cbutton = new JGradientButton(buttonAction(Constants.EPIC_YEAR, FestcConstants.EPIC_YEARLY_AVERAGE2CMAQ_VIEW),
				 Color.getHSBColor(230f / 360, 0.40f, 0.95f));
		JGradientButton e2cbutton = new JGradientButton(buttonAction(Constants.EPIC2CMAQ, FestcConstants.EPIC2CMAQ_VIEW), 
				 Color.getHSBColor(230f / 360, 0.40f, 0.95f));
		JGradientButton e2wbutton = new JGradientButton(buttonAction(Constants.EPIC2SWAT, FestcConstants.EPIC2SWAT_VIEW), 
				 Color.getHSBColor(230f / 360, 0.40f, 0.95f));
		JButton visbutton = new JGradientButton(buttonAction(Constants.VISU, FestcConstants.VISUALIZE_VIEW), 
				Color.getHSBColor(230f / 360, 0.40f, 0.95f));
				
		landUsebutton.setPreferredSize(new Dimension(340, 40));
		sitegenbutton.setPreferredSize(new Dimension(340, 40));
		m2pbutton.setPreferredSize(new Dimension(340, 40));
		sitefilebutton.setPreferredSize(new Dimension(340, 40));
		genbutton.setPreferredSize(new Dimension(340, 40));
		manfilespinbutton.setPreferredSize(new Dimension(340, 40));
		modbutton.setPreferredSize(new Dimension(340, 40));
		epicbutton.setPreferredSize(new Dimension(340, 40));
		eya2cbutton.setPreferredSize(new Dimension(340, 40));
		manfileappbutton.setPreferredSize(new Dimension(340, 40));
		epicappbutton.setPreferredSize(new Dimension(340, 40));
		e2cbutton.setPreferredSize(new Dimension(340, 40));
		e2wbutton.setPreferredSize(new Dimension(340, 40));
		visbutton.setPreferredSize(new Dimension(340, 40));
		
		landUsebutton.setFont(new Font("Default", Font.BOLD, 14));
		sitegenbutton.setFont(new Font("Default", Font.BOLD, 14));
		m2pbutton.setFont(new Font("Default", Font.BOLD, 14));
		sitefilebutton.setFont(new Font("Default", Font.BOLD, 14));
		genbutton.setFont(new Font("Default", Font.BOLD, 14));
		manfilespinbutton.setFont(new Font("Default", Font.BOLD, 14));
		modbutton.setFont(new Font("Default", Font.BOLD, 14));
		epicbutton.setFont(new Font("Default", Font.BOLD, 14));
		eya2cbutton.setFont(new Font("Default", Font.BOLD, 14));
		manfileappbutton.setFont(new Font("Default", Font.BOLD, 14));
		epicappbutton.setFont(new Font("Default", Font.BOLD, 14));
		e2cbutton.setFont(new Font("Default", Font.BOLD, 14));
		e2wbutton.setFont(new Font("Default", Font.BOLD, 14));
		visbutton.setFont(new Font("Default", Font.BOLD, 14));
			
		layout.addLabelWidgetPair("", landUsebutton, panel);
		layout.addLabelWidgetPair("", sitegenbutton, panel);
		layout.addLabelWidgetPair("", m2pbutton, panel);
		layout.addLabelWidgetPair("", sitefilebutton, panel);
		layout.addLabelWidgetPair("", genbutton, panel);
		layout.addLabelWidgetPair("", manfilespinbutton, panel);
		layout.addLabelWidgetPair("", modbutton, panel);
		layout.addLabelWidgetPair("", epicbutton, panel);	
		layout.addLabelWidgetPair("", manfileappbutton, panel);
		layout.addLabelWidgetPair("", epicappbutton, panel);
		layout.addLabelWidgetPair("", eya2cbutton, panel);
		layout.addLabelWidgetPair("", e2cbutton, panel);
		layout.addLabelWidgetPair("", e2wbutton, panel);
		layout.addLabelWidgetPair("", visbutton, panel);
		
		layout.makeCompactGrid(panel, 14, 2, // number of rows and cols
				1, 1, // initial X and Y
				1, 1); // x and y pading

		return panel;
	}

	private Action buttonAction(final String name, final String viewId) {
		return new AbstractAction(name) {
			private static final long serialVersionUID = -957559355824942766L;

			public void actionPerformed(ActionEvent e) {
				app.showTab(viewId);
			}
		};
	}
	
    private class JGradientButton extends JButton {
		private static final long serialVersionUID = -2397650058508947005L;
		private Color bckGrndColor;
    	
        private JGradientButton(Action action, Color bckGrndColor){
            super(action);
            setContentAreaFilled(false);
            setFocusPainted(false); // used for demonstration
            this.bckGrndColor = bckGrndColor;
        }

        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setPaint(new GradientPaint(
                    new Point(0, getHeight() / 3), 
                    Color.WHITE, 
                    new Point(0, 0), 
                    bckGrndColor, true));
            g2.fillRect(0, 0, getWidth(), (getHeight() * 2) / 3);
            g2.setPaint(bckGrndColor);
            g2.fillRect(0, (getHeight() * 2) / 3, getWidth(), getHeight());
            g2.dispose();

            super.paintComponent(g);
        }
    }

}

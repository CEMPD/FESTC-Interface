package gov.epa.festc.core;

import gov.epa.festc.gui.Beld4DataGenPanel;
import gov.epa.festc.gui.CreateAppManFilesPanel;
import gov.epa.festc.gui.CreateSiteFilesPanel;
import gov.epa.festc.gui.CreateSiteInfoPanel;
import gov.epa.festc.gui.CreateSoilFilesPanel;
import gov.epa.festc.gui.CreateSpinupManFilesPanel;
import gov.epa.festc.gui.Epic2CMAQPanel;
import gov.epa.festc.gui.EpicRunAppPanel;
import gov.epa.festc.gui.EpicSpinupPanel;
import gov.epa.festc.gui.EpicYearlyAverage2CMAQPanel;
import gov.epa.festc.gui.ManFileModPanel;
import gov.epa.festc.gui.Mcip2EpicPanel;
import gov.epa.festc.gui.ToolsPanel;
import gov.epa.festc.gui.VisualizationPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.flexdock.plaf.PlafManager;
import org.flexdock.view.View;

import saf.core.ui.GUIBarManager;
import saf.core.ui.IAppConfigurator;
import saf.core.ui.ISAFDisplay;
import saf.core.ui.IWindowCustomizer;
import saf.core.ui.view.ViewManager;

/**
 * Application configurator for FEST-C.
 * <p>
 * <p/> The methods in this interface are called by the SAF application
 * initialization mechanism during points in the applications lifecycle. On
 * application start up, the order in which they are called is:
 * <ol>
 * <li> #preWindowOpen </li>
 * <li> #createLayout </li>
 * <li> #fillBars </li>
 * <li> #postWindowOpen </li>
 * </ol>
 * 
 * @author IE, UNC
 * @version $Revision$ $Date$
 */
public class FestcConfigurator implements IAppConfigurator {

	private FestcApplication festcApp;

	private ViewManager viewManager;

	/**
	 * Creates a FestcConfigurator.
	 * 
	 * @param appthe main FEST-C application object
	 * 
	 */
	public FestcConfigurator(FestcApplication app) {
		festcApp = app;
	}
	

	/**
	 * Creates the initial layout in the main application window. Typically,
	 * implementors would add the initial application views here, setting up the
	 * initial gui layout.
	 * 
	 * @param viewManager
	 *            the ViewManager used to create the initial layout
	 */
	public void createLayout(ViewManager viewManager) {
		this.viewManager = viewManager;

		ToolsPanel toolsPanel = new ToolsPanel(festcApp);
		View activeview = viewManager.createView(FestcConstants.TOOLS_VIEW, new JScrollPane(toolsPanel),
				ViewManager.HideLocation.UNSPECIFIED, ViewManager.FLOAT | ViewManager.HIDE);
		activeview.setTabText("Tools");
		activeview.setTitle("Tools");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.TOOLS_GROUP, activeview);
	
		Beld4DataGenPanel beld4Gen = new Beld4DataGenPanel(festcApp);
		View view = viewManager.createView(FestcConstants.BELD4_VIEW, new JScrollPane(beld4Gen), ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("Beld4 Data...");
		view.setTitle("BELD4 Data Generation");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		CreateSiteInfoPanel siteInfo = new CreateSiteInfoPanel(festcApp);
		view = viewManager.createView(FestcConstants.SITE_INFO_VIEW, new JScrollPane(siteInfo), ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("Crop Site Info...");
		view.setTitle("Site Utilities");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		Mcip2EpicPanel mcip2epic = new Mcip2EpicPanel(festcApp);
		JScrollPane pane = new JScrollPane(mcip2epic);
		view = viewManager.createView(FestcConstants.MCIP2EPIC_VIEW, pane, ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("MCIP/CMAQ to EPIC");
		view.setTitle("MCIP/CMAQ to EPIC");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		CreateSiteFilesPanel siteMan = new CreateSiteFilesPanel(festcApp);
		view = viewManager.createView(FestcConstants.SITE_FILE_VIEW, new JScrollPane(siteMan), ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("EPIC Site file...");
		view.setTitle("EPIC Site Utilities");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		CreateSoilFilesPanel soilMatchpanel = new CreateSoilFilesPanel(festcApp);
		view = viewManager.createView(FestcConstants.GEN_SOIL_MAN_FILES_VIEW, new JScrollPane(soilMatchpanel), ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("EPIC Soil ...");
		view.setTitle("EPIC Soil Utilities");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		CreateSpinupManFilesPanel manfilespinup = new CreateSpinupManFilesPanel(festcApp);
		view = viewManager.createView(FestcConstants.MAN_FILE_SPINUP_VIEW, new JScrollPane(manfilespinup), ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("Management Spinup...");
		view.setTitle("Management File Generation for Spinup");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		ManFileModPanel managePanel = new ManFileModPanel(festcApp);
		view = viewManager.createView(FestcConstants.MANAGE_VIEW, new JScrollPane(managePanel), ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("View/Edit EPIC...");
		view.setTitle("View/Edit EPIC Inputs");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		EpicSpinupPanel epicSuPanel = new EpicSpinupPanel(festcApp);
		view = viewManager.createView(FestcConstants.EPIC_VIEW, new JScrollPane(epicSuPanel), ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("EPIC Runs for Spinup");
		view.setTitle("EPIC Runs for Spinup");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		EpicYearlyAverage2CMAQPanel epicYearlyAverage2cmaqPanel = new EpicYearlyAverage2CMAQPanel(festcApp);
		view = viewManager.createView(FestcConstants.EPIC_YEARLY_AVERAGE2CMAQ_VIEW, new JScrollPane(epicYearlyAverage2cmaqPanel), ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("EPIC Yearly Extraction"); //EPIC Yearly Ave. to CMAQ");
		view.setTitle("EPIC Yearly Extraction"); //EPIC Yearly Ave. to CMAQ");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		CreateAppManFilesPanel manfileapp = new CreateAppManFilesPanel(festcApp);
		view = viewManager.createView(FestcConstants.MAN_FILE_APP_VIEW, new JScrollPane(manfileapp), ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("Management File...");
		view.setTitle("Management File Generation for Application");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		EpicRunAppPanel epicrunapp = new EpicRunAppPanel(festcApp);
		view = viewManager.createView(FestcConstants.EPIC4APP_VIEW, new JScrollPane(epicrunapp), ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("EPIC Runs...");
		view.setTitle("EPIC Runs for Application");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		Epic2CMAQPanel epic2cmaqPanel = new Epic2CMAQPanel(festcApp);
		view = viewManager.createView(FestcConstants.EPIC2CMAQ_VIEW, new JScrollPane(epic2cmaqPanel), ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("EPIC to CMAQ");
		view.setTitle("EPIC to CMAQ");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);

		VisualizationPanel visPanel = new VisualizationPanel(festcApp);
		view = viewManager.createView(FestcConstants.VISUALIZE_VIEW, new JScrollPane(visPanel), ViewManager.HideLocation.UNSPECIFIED,
				ViewManager.FLOAT | ViewManager.HIDE);
		view.setTabText("Visualization");
		view.setTitle("Visualization");
		viewManager.addViewToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		festcApp.init(new FestcGUI(viewManager));
		managePanel.setParent(festcApp.getGui());
		//epicSuPanel.setParent(festcApp.getGui());
		toolsPanel.requestFocusInWindow();
		festcApp.getGui().setActive(activeview.getPersistentId());
	}

	public void setStatusOneText(String text) {
		viewManager.getBarManager().setStatusBarText("verdi.status.one", text);
	}
	
	public void setStatusTwoText(String text) {
		viewManager.getBarManager().setStatusBarText("verdi.status.two", text);
	}
	
	/**
	 * Optionally adds menu items and actions to the menu and tool bars. This
	 * can be used to programmatically add tool bar and menus / menu items for
	 * those that are not described in an xml plugin file.
	 * 
	 * @param guiBarManager
	 *            the GUIBarManager used to configure tool and menu bars.
	 */
	public void fillBars(GUIBarManager guiBarManager) {
		viewManager.createWindowMenu();
		JTextField fld = new JTextField();
		Font font = fld.getFont().deriveFont(Font.BOLD);
		guiBarManager.setStatusBarFont("festc.status.two", font);
		guiBarManager.setStatusBarFont("festc.status.one", font);
		guiBarManager.addToolBarComponent(FestcConstants.FORMULA_BAR_GROUP, "", Box.createHorizontalGlue());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(new JLabel("Selected Formula:"));
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		JLabel label = new JLabel("    ");
		label.setForeground(Color.BLUE);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		panel.add(label);
		guiBarManager.addToolBarComponent(FestcConstants.FORMULA_BAR_GROUP, FestcConstants.FORMULA_LABEL, panel);
		guiBarManager.addToolBarComponent(FestcConstants.FORMULA_BAR_GROUP, "", Box
				.createRigidArea(new Dimension(10, 0)));
	}

	/**
	 * Performs some arbitrary clean up type actions immediately prior to
	 * closing the main application window.
	 */
	public void postWindowClose() {
	}

	/**
	 * Performs some arbitrary actions immediately after the main application
	 * window has been open.
	 * 
	 * @param display
	 *            the display representing the main application window.
	 */
	public void postWindowOpen(ISAFDisplay display) {
		festcApp.getGui().setFrame(display.getFrame());
	}

	/**
	 * Performs some arbitrary clean up type actions immediately prior to
	 * closing the main application window. The calls the pave application to
	 * determine whether the app should exit or not.
	 * 
	 * @return true if the window can continue to close, false to veto the
	 *         window close.
	 */
	public boolean preWindowClose() {	
		if ( festcApp.getProjFile() != null ){
			int option = JOptionPane.showConfirmDialog(null, "Do you want to save scenario? ", "Confirmation", JOptionPane.YES_NO_OPTION);
			if ( option == 0 )
				festcApp.saveProject();
		}
		return festcApp.exit();
	}

	/**
	 * Performs some arbitrary actions prior to the main application window
	 * opening. This can be setting the application's look and feel, using the
	 * customizer parameter to set the initial window's size, title and so on.
	 * 
	 * @param customizer
	 *            the customizer used to customize the initial application
	 *            window
	 * @return true if the application should continue to open, or false to
	 *         close stop application initialization. Note that return false can
	 *         be a normal condition, such as a login failing.
	 */
	public boolean preWindowOpen(IWindowCustomizer customizer) {
		try {
			String lf = UIManager.getSystemLookAndFeelClassName();
			if (lf.toLowerCase().contains("gtk"))
				lf = UIManager.getCrossPlatformLookAndFeelClassName();
			UIManager.setLookAndFeel(lf);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		PlafManager.setPreferredTheme("repast.simphony", ViewManager.class.getResource("/saf/core/ui/saf-themes.xml"));
		customizer.useStoredFrameBounds(600, 600);
		customizer.useSavedViewLayout();
		customizer.setTitle(
				//"FEST-C"
				"Fertilizer Emission Scenario Tool for CMAQ (FEST-C v1.2)"
				);
		return true;
	}
}

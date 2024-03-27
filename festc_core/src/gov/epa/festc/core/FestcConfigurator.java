package gov.epa.festc.core;

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

import gov.epa.festc.gui.Beld4DataGenPanel;
import gov.epa.festc.gui.CreateAppManFilesPanel;
import gov.epa.festc.gui.CreateSiteFilesPanel;
import gov.epa.festc.gui.CreateSiteInfoPanel;
import gov.epa.festc.gui.CreateSoilFilesPanel;
import gov.epa.festc.gui.CreateSpinupManFilesPanel;
import gov.epa.festc.gui.Epic2CMAQPanel;
import gov.epa.festc.gui.Epic2SWATPanel;
import gov.epa.festc.gui.EpicRunAppPanel;
import gov.epa.festc.gui.EpicSpinupPanel;
import gov.epa.festc.gui.EpicYearlyAverage2CMAQPanel;
import gov.epa.festc.gui.ManFileModPanel;
import gov.epa.festc.gui.Mcip2EpicPanel;
import gov.epa.festc.gui.ToolsPanel;
import gov.epa.festc.gui.VisualizationPanel;
import saf.core.ui.GUIBarManager;
import saf.core.ui.IAppConfigurator;
import saf.core.ui.ISAFDisplay;
import saf.core.ui.IWindowCustomizer;
import saf.core.ui.dock.DefaultDockableFrame;
import saf.core.ui.dock.DefaultDockingManager;
import saf.core.ui.dock.DockingManager.MinimizeLocation;

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

	private DefaultDockingManager DockingManager;
//	private  int CLOSE = 1;
//	  int MINIMIZE = 2;
//	  int MAXIMIZE = 4;
//	  int FLOAT = 8;
	
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
	 * @param DockingManager
	 *            the DockingManager used to create the initial layout
	 */
	public void createLayout(DefaultDockingManager DockingManager) {
		this.DockingManager = DockingManager;

		ToolsPanel toolsPanel = new ToolsPanel(festcApp);
		DefaultDockableFrame activeview = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.TOOLS_VIEW, 
				new JScrollPane(toolsPanel), MinimizeLocation.UNSPECIFIED, 4 );
		activeview.setTitle("Tools");
		activeview.setToolTip("Tools");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.TOOLS_GROUP, activeview);
		
		Beld4DataGenPanel beld4Gen = new Beld4DataGenPanel(festcApp);
		DefaultDockableFrame fview = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.BELD4_VIEW, 
				new JScrollPane(beld4Gen),MinimizeLocation.UNSPECIFIED, 2|4|8 );
		fview.setTitle("Beld4 Data...");
		fview.setToolTip("BELD4 Data Generation");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, fview);
		
		CreateSiteInfoPanel siteInfo = new CreateSiteInfoPanel(festcApp);
		DefaultDockableFrame view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.SITE_INFO_VIEW, 
				new JScrollPane(siteInfo), MinimizeLocation.UNSPECIFIED, 2|4|8);
		view.setTitle("Crop Site Info...");
		view.setToolTip("Site Utilities");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		Mcip2EpicPanel mcip2epic = new Mcip2EpicPanel(festcApp);
		JScrollPane pane = new JScrollPane(mcip2epic);
		view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.MCIP2EPIC_VIEW, pane);
		view.setTitle("MCIP/CMAQ to EPIC");
		view.setToolTip("MCIP/CMAQ to EPIC");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		CreateSiteFilesPanel siteMan = new CreateSiteFilesPanel(festcApp);
		view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.SITE_FILE_VIEW, new JScrollPane(siteMan));
		view.setTitle("EPIC Site file...");
		view.setToolTip("EPIC Site Utilities");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		CreateSoilFilesPanel soilMatchpanel = new CreateSoilFilesPanel(festcApp);
		view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.GEN_SOIL_MAN_FILES_VIEW, new JScrollPane(soilMatchpanel));
		view.setTitle("EPIC Soil ...");
		view.setToolTip("EPIC Soil Utilities");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		CreateSpinupManFilesPanel manfilespinup = new CreateSpinupManFilesPanel(festcApp);
		view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.MAN_FILE_SPINUP_VIEW, new JScrollPane(manfilespinup) );
		view.setTitle("Management Spinup...");
		view.setToolTip("Management File Generation for Spinup");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		ManFileModPanel managePanel = new ManFileModPanel(festcApp);
		view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.MANAGE_VIEW, new JScrollPane(managePanel) );
		view.setTitle("View/Edit EPIC...");
		view.setToolTip("View/Edit EPIC Inputs");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		EpicSpinupPanel epicSuPanel = new EpicSpinupPanel(festcApp);
		view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.EPIC_VIEW, new JScrollPane(epicSuPanel) );
		view.setTitle("EPIC Runs for Spinup");
		view.setToolTip("EPIC Runs for Spinup");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		EpicYearlyAverage2CMAQPanel epicYearlyAverage2cmaqPanel = new EpicYearlyAverage2CMAQPanel(festcApp);
		view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.EPIC_YEARLY_AVERAGE2CMAQ_VIEW, new JScrollPane(epicYearlyAverage2cmaqPanel) );
		view.setTitle("EPIC Yearly Extraction"); //EPIC Yearly Ave. to CMAQ");
		view.setToolTip("EPIC Yearly Extraction"); //EPIC Yearly Ave. to CMAQ");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		CreateAppManFilesPanel manfileapp = new CreateAppManFilesPanel(festcApp);
		view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.MAN_FILE_APP_VIEW, new JScrollPane(manfileapp) );
		view.setTitle("Management File...");
		view.setToolTip("Management File Generation for Application");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		EpicRunAppPanel epicrunapp = new EpicRunAppPanel(festcApp);
		view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.EPIC4APP_VIEW, new JScrollPane(epicrunapp) );
		view.setTitle("EPIC Runs...");
		view.setToolTip("EPIC Runs for Application");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
		Epic2CMAQPanel epic2cmaqPanel = new Epic2CMAQPanel(festcApp);
		view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.EPIC2CMAQ_VIEW, new JScrollPane(epic2cmaqPanel) );
		view.setTitle("EPIC to CMAQ");
		view.setToolTip("EPIC to CMAQ");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);

		Epic2SWATPanel epic2swatPanel = new Epic2SWATPanel(festcApp);
		view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.EPIC2SWAT_VIEW, new JScrollPane(epic2swatPanel) );
		view.setTitle("EPIC to SWAT");
		view.setToolTip("EPIC to SWAT");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);

		VisualizationPanel visPanel = new VisualizationPanel(festcApp);
		view = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.VISUALIZE_VIEW, new JScrollPane(visPanel) );
		view.setTitle("Visualization");
		view.setToolTip("Visualization");
		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, view);
		
//		Beld4DataGenPanel beld4Gen = new Beld4DataGenPanel(festcApp);
//		DefaultDockableFrame fview = (DefaultDockableFrame) DockingManager.createDockable(FestcConstants.BELD4_VIEW, 
//				new JScrollPane(beld4Gen),MinimizeLocation.UNSPECIFIED, 2|4|8 );
//		fview.setTitle("Beld4 Data...");
//		fview.setToolTip("BELD4 Data Generation");
//		DockingManager.addDockableToGroup(FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID, fview);
//		
		festcApp.init(new FestcGUI(DockingManager));
		managePanel.setParent(festcApp.getGui());
		//epicSuPanel.setParent(festcApp.getGui());
		//System.out.println(activeview.getID());
		festcApp.showTab(fview.getID());
		toolsPanel.requestFocusInWindow();
		//DockingManager.setActiveDockable(fview.getID());
	}

	public void setStatusOneText(String text) {
		DockingManager.getBarManager().setStatusBarText("festc.status.one", text);
	}
	
	public void setStatusTwoText(String text) {
		DockingManager.getBarManager().setStatusBarText("festc.status.two", text);
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
		//DockingManager.createWindowMenu();
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
//			int option = JOptionPane.showConfirmDialog(null, "Do you want to save scenario? ", "Confirmation", JOptionPane.YES_NO_OPTION);
//			if ( option == 0 )
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
		
		//PlafManager.setPreferredTheme("repast.simphony", DockingManager.class.getResource("/saf/core/ui/saf-themes.xml"));
		customizer.useStoredFrameBounds(600, 600);
		customizer.useSavedLayout(); 
		customizer.setTitle(
				//"FEST-C"
				"Fertilizer Emission Scenario Tool for CMAQ (FEST-C v2.1)"
				);
		return true;
	}


 
}

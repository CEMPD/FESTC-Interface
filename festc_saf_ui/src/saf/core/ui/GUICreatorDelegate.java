package saf.core.ui;
import java.awt.event.ActionEvent;
/**
 * @author Nick Collier
public class GUICreatorDelegate {
	private GUICreatorDelegate() {
			props.load(strm);
			if (dockingFacClass.equals("")) {
			registerMacOSX();
	private void registerMacOSX() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
			Class[] defArgs = {Boolean.class};
	static GUICreatorDelegate getInstance() {
	public void addBarItemDescriptor(BarItemDescriptor descriptor) {
	public DockingFactory getDockingFactory() {
	public ISAFDisplay createDisplay(IAppConfigurator configurator, Workspace workspace) 
		JToolBar toolBar = new JToolBar();
		display.init(wCustomizer, barManager);
		display.getFrame().addWindowListener(new WindowAdapter() {
		DockingManager vManager = dockingFactory.getViewManager(barManager, perspectives);
		if (statusBarDescriptor != null) {
		configurator.fillBars(barManager);
		if (help != null) {
		barManager.createPerspectiveMenu(vManager);
		vManager.init();
	public void runDisplay(IAppConfigurator configurator, ISAFDisplay display) {
	public void setMenuTreeDescriptor(MenuTreeDescriptor mtDescriptor) {
	public void setPerspectives(List<Perspective> perspectives) {
	public void setHelp(Help help) {
	public void setApplicationPrefs(AppPreferences prefs) {
	public void setStatusBarDescriptor(StatusBarDescriptor descriptor) {
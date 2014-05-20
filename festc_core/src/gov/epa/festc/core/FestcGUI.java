package gov.epa.festc.core;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.flexdock.view.View;

import saf.core.ui.actions.ViewEvent;
import saf.core.ui.view.ViewListener;
import saf.core.ui.view.ViewManager;
import simphony.util.messages.MessageCenter;

/**
 * Facade for gui related application operations.
 * 
 * @author IE, UNC
 * @version $Revision$ $Date$
 */
public class FestcGUI implements ViewListener {

	private static final MessageCenter ctr = MessageCenter
			.getMessageCenter(FestcGUI.class);

	private ViewManager viewManager;
	private JFrame frame;
	private java.util.List<String> viewList = new ArrayList<String>(); 
	private java.util.List<JFrame> framesToDisplay = new ArrayList<JFrame>(); 

	public FestcGUI(ViewManager viewManager) {
		this.viewManager = viewManager;
	}

	public void undockAllPlots() {
		java.util.List<View> views = viewManager.getViews(
				FestcConstants.PERSPECTIVE_ID, FestcConstants.MAIN_GROUP_ID);
		for (View view : views) {
			viewManager.floatView(view);
		}
	}
	
	public void setStatusTwoText(String text) {
		viewManager.getBarManager().setStatusBarText("festc.status.two", text);
	}

	public void setStatusOneText(String text) {
		viewManager.getBarManager().setStatusBarText("festc.status.one", text);
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
		Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/epa_logo.JPG"));
		this.frame.setIconImage(image);
		
		for (JFrame f : framesToDisplay) {
			f.setLocationRelativeTo(FestcGUI.this.frame);
			f.setVisible(true);
		}
		framesToDisplay.clear();
	}

	public ViewManager getViewManager() {
		return viewManager;
	}

	public void showMessage(String title, String message) {
		JOptionPane.showMessageDialog(frame, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	public boolean askMessage(String title, String message) {
		return JOptionPane.showConfirmDialog(frame, message + ". Continue?",
				title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	/**
	 * Invoked when a view is closed.
	 * 
	 * @param evt
	 */

	public void viewClosed(ViewEvent evt) {
		ctr.info("View closed at: ", new Date());
	}

	/**
	 * Invoked when a view receives a float request. The float can be overriden
	 * by setting the events handled property to true.
	 * 
	 * @param evt
	 *            details the float request
	 */
	public void floatRequested(ViewEvent evt) {
	}

	/**
	 * Invoked when a view receives a restore request. The restore can be
	 * overriden by setting the events handled property to true.
	 * 
	 * @param evt
	 *            details the restore request
	 */
	public void restoreRequested(ViewEvent evt) {
	}

	/**
	 * Invoked when a user attempts to close a view. The close can be overriden
	 * by setting the events handled property to true.
	 * 
	 * @param evt
	 *            the event describing the view etc.
	 */
	public void closeRequested(ViewEvent evt) {
	}

	/**
	 * Invoked when a view is floated.
	 * 
	 * @param evt
	 */
	public void viewFloated(ViewEvent evt) {
		//
	}

	/**
	 * Invoked when a view is maximized.
	 * 
	 * @param evt
	 */
	public void viewMaximized(ViewEvent evt) {
		// todo implement method
	}

	/**
	 * Invoked when a view is minimized.
	 * 
	 * @param evt
	 */
	public void viewMinimized(ViewEvent evt) {
		// todo implement method
	}

	/**
	 * Invoked when a view is restored to its default docking position from a
	 * floated, minimized, or maximized state.
	 * 
	 * @param evt
	 */
	public void viewRestored(ViewEvent evt) {
	}

	/**
	 * Returns the list of open plot view ids
	 * 
	 * @return ArrayList<String>
	 */
	public java.util.List<String> getViewList() {
		return viewList;
	}

	public void setActive(String tabId) {
		viewManager.setActiveView(tabId);
	}
	
	public void refreshViews() {
		
	}
	
}

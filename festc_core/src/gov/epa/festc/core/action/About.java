package gov.epa.festc.core.action;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.gui.AboutDialog;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import saf.core.ui.actions.AbstractSAFAction;

/**
 * Action to show the about dialog.
 *
 * @author IE, UNC
 * @version $Revision$ $Date$
 */
public class About extends AbstractSAFAction<FestcApplication> {

	private static final long serialVersionUID = 4707751873774402860L;

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		JFrame frame = workspace.getApplicationMediator().getGui().getFrame();
		AboutDialog dialog = new AboutDialog(frame);
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}
}

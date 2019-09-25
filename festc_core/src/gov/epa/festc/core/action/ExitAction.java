package gov.epa.festc.core.action;

import gov.epa.festc.core.FestcApplication;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import saf.core.ui.actions.AbstractSAFAction;

/**
 * Action for exit files.
 *
 * @version $Revision$ $Date$
 */
public class ExitAction extends AbstractSAFAction<FestcApplication> {
	private static final long serialVersionUID = 4008068827373247119L;

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		FestcApplication app = workspace.getApplicationMediator();
		if ( app.getProjFile() != null ){
			//			int option = JOptionPane.showConfirmDialog(null, "Do you want to save scenario? ", "Confirmation", JOptionPane.YES_NO_OPTION);
			//			if ( option == 0 )
			app.saveProject();
		}
		System.exit(0);
	}
}

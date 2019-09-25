package gov.epa.festc.core.action;

import gov.epa.festc.core.FestcApplication;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import saf.core.ui.actions.AbstractSAFAction;

/**
 * Action for opening files.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class CreateAction extends AbstractSAFAction<FestcApplication> {
	private static final long serialVersionUID = 2948227677960767437L;

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		FestcApplication app = workspace.getApplicationMediator();
		if ( app.getProjFile() != null ){
//			int option = JOptionPane.showConfirmDialog(null, "Do you want to save scenario? ", "Confirmation", JOptionPane.YES_NO_OPTION);
//			if ( option == 0 )
			app.saveProject();
			app.createProject();
		}
		else
			app.createProject();
	}
}

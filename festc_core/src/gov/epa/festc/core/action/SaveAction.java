package gov.epa.festc.core.action;

import gov.epa.festc.core.FestcApplication;

import java.awt.event.ActionEvent;

import saf.core.ui.actions.AbstractSAFAction;

/**
 * Action for opening files.
 *
 * @version $Revision$ $Date$
 */
public class SaveAction extends AbstractSAFAction<FestcApplication> {
	private static final long serialVersionUID = 8373565559359050502L;

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		workspace.getApplicationMediator().saveProject();
	}
}

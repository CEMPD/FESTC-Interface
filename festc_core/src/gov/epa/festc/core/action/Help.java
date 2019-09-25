package gov.epa.festc.core.action;

import gov.epa.festc.core.FestcApplication;

import java.awt.event.ActionEvent;

import saf.core.ui.actions.AbstractSAFAction;

/**
 * Action to show the help
 *
 * @author IE, UNC
 * @version $Revision$ $Date$
 */
public class Help extends AbstractSAFAction<FestcApplication> {

	private static final long serialVersionUID = -6430759498830546714L;

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		workspace.getApplicationMediator().showHelp();
	}
}

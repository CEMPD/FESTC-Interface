package gov.epa.festc.gui;

import gov.epa.festc.core.VersionInfo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.JXHeader;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * @author IE, UNC
 */
public class AboutDialog extends JDialog {
	//private static final long serialVersionUID = -3158674310794549095L;
	public AboutDialog(Frame owner) {
		super(owner);
		initComponents();
		xHeader1.setDescription("Version: " + VersionInfo.getVersion() + " " + VersionInfo.getDate());
	}

	public AboutDialog(Dialog owner) {
		super(owner);
		initComponents();
		xHeader1.setDescription("Version: " + VersionInfo.getVersion() + " " + VersionInfo.getDate());
	}

	private void okButtonActionPerformed(ActionEvent e) {
		this.dispose();
	}

	private void initComponents() {
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		scrollPane1 = new JScrollPane();
		xHeader1 = new JXHeader();
		buttonBar = new JPanel();
		okButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setTitle("About FEST-C");
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new FormLayout(
					"default:grow",
					"default"));

				//======== scrollPane1 ========
				{
					xHeader1.setTitle("Fertilizer Emission Scenario Tool for CMAQ (FEST-C)");
					scrollPane1.setViewportView(xHeader1);
				}
				contentPanel.add(scrollPane1, cc.xy(1, 1));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				buttonBar.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.GLUE_COLSPEC,
						FormFactory.BUTTON_COLSPEC
					},
					RowSpec.decodeSpecs("pref")));

				//---- okButton ----
				okButton.setText("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				buttonBar.add(okButton, cc.xy(2, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
	}

	private JPanel dialogPane;
	private JPanel contentPanel;
	private JScrollPane scrollPane1;
	private JXHeader xHeader1;
	private JPanel buttonBar;
	private JButton okButton;
}


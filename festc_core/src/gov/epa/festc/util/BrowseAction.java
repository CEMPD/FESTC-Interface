package gov.epa.festc.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

public class BrowseAction {
	
	public static Action browseAction(final Component parent, final File currentDir, final String name, final JTextField text) {
		return new AbstractAction("Browse...") {
			private static final long serialVersionUID = 0L;

			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser;
				File file = new File(text.getText());

				if (file != null && file.isFile())
					chooser = new JFileChooser(file.getParent());
				else if (file != null && file.isDirectory())
					chooser = new JFileChooser(file);
				else
					chooser = new JFileChooser(currentDir);
                 
				chooser.setDialogTitle("Please select the " + name);

				int option = chooser.showDialog(parent, "Select");
				if (option == JFileChooser.APPROVE_OPTION) {
					File selected = chooser.getSelectedFile();
					if ( name == "scenario file" )
						text.setText(selected.getName());
					else
						text.setText("" + selected);
				}
			}
		};
	}

}

package gov.epa.festc.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

public class FileAction {
	
	public static Action browseDirAction(final Component parent, final String name, final JTextField text) {
		return new AbstractAction("Browse...") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8638458392705010745L;

			public void actionPerformed(ActionEvent e) {
				try {
					chooseDir( parent, name, text);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		};
	}
	
	public static Action browseFileAction(final Component parent, final JTextField text, final String path, final String type) {
		return new AbstractAction("Browse...") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5490744884831755232L;

			public void actionPerformed(ActionEvent e) {
				String file = chooseFile(parent, path, type);
				text.setText(file);
			}
		};
	}
	
	public static void chooseDir( final Component parent, final String name, final JTextField text) throws IOException {
		JFileChooser chooser;
		File file = new File(text.getText());

		if (file != null && file.isFile()) {
			chooser = new JFileChooser(file.getParentFile());
		} else if (file != null && file.isDirectory()) {
			chooser = new JFileChooser(file);
		} else {
			File directory = new File (".");
			chooser = new JFileChooser(directory.getCanonicalPath());
		}

		chooser.setDialogTitle("Please select the " + name);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		int option = chooser.showDialog(parent,
				"Select");
		if (option == JFileChooser.APPROVE_OPTION) {
			File selected = chooser.getSelectedFile();
			if (selected.exists() && selected.isDirectory()) {
				text.setText("" + selected); 
			} else {
				text.setText("" + selected.getParent());
			}
		}
	}
	
	public static String chooseFile(Component parent, final String filePath, final String type) {
		JFileChooser chooser;
		File file = new File("" + filePath);

		if (file != null && file.exists())
			chooser = new JFileChooser(file);
		else { 
			File directory = new File (".");
			try {
				chooser = new JFileChooser(directory.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
				chooser = new JFileChooser();
			}
		}

		chooser.setDialogTitle("Please select the " + type);

		int option = chooser.showDialog(parent,
				"Select");
		if (option == JFileChooser.APPROVE_OPTION) {
			File selected = chooser.getSelectedFile();
			return "" + selected;
		}
		
		return null;
	}
}

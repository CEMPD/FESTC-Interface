package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.core.proj.SiteFilesFields;
import gov.epa.festc.core.proj.VisualizationFields;
import gov.epa.festc.util.Constants;
import gov.epa.festc.util.SpringLayoutGenerator;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import simphony.util.messages.MessageCenter;

public class VisualizationPanel extends JPanel implements PlotEventListener {
	private static final long serialVersionUID = 7114847591403108679L;
	private JTextField mcipFile;
	private JButton mcipFileBrowser;
	private JTextField beldFile;
	private JButton beldFileBrowser;
	private JTextField epicFile;
	private JButton epicFileBrowser;
	private JRadioButton mcipButton;
	private JRadioButton beldButton;
	private JRadioButton epicButton;
	private MessageCenter msg;
	private FestcApplication app;
	private VisualizationFields fields;

	public VisualizationPanel(FestcApplication application) {
		app = application;
		msg = app.getMessageCenter();
		fields = new VisualizationFields();
		app.getProject().addPage(fields);
		app.addPlotListener(this);
		add(createPanel());
	}

	private JPanel createPanel() {
		JPanel main = new JPanel(new BorderLayout());
		main.add(getNorthPanel(), BorderLayout.NORTH);
		main.add(getCenterPanel(), BorderLayout.CENTER);
		main.add(getSouthPanel(), BorderLayout.SOUTH);
		return main;
	}

	private JPanel getNorthPanel() {
		JPanel panel = new JPanel();
		JLabel title = new JLabel(Constants.VISU, SwingConstants.CENTER);
		title.setFont(new Font("Default", Font.BOLD, 20));

		panel.add(title);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 60, 0));

		return panel;
	}

	private JPanel getSouthPanel() {
		JPanel panel = new JPanel();
		JButton display = new JButton(displayAction());
		panel.add(display);

		panel.setBorder(BorderFactory.createEmptyBorder(60, 0, 10, 0));

		return panel;
	}

	private JPanel getCenterPanel() {
		JPanel panel = new JPanel(new SpringLayout());
		SpringLayoutGenerator layout = new SpringLayoutGenerator();

		mcipButton = new JRadioButton("MCIP Data:");
		beldButton = new JRadioButton("Model Grid BELD Data:");
		epicButton = new JRadioButton("EPIC Output Data:");
		ButtonGroup group = new ButtonGroup();
		epicButton.setSelected(true);
		group.add(mcipButton);
		group.add(beldButton);
		group.add(epicButton);

		JPanel mcip = new JPanel();
		mcipFile = new JTextField(40);
		mcipFileBrowser = new JButton(browseAction("MCIP data file", mcipFile));
		mcip.add(mcipFile);
		mcip.add(mcipFileBrowser);

		JPanel beld = new JPanel();
		beldFile = new JTextField(40);
		beld.add(beldFile);
		beldFileBrowser = new JButton(browseAction("BELD data file", beldFile));
		beld.add(beldFileBrowser);

		JPanel epic = new JPanel();
		epicFile = new JTextField(40);
		epicFileBrowser = new JButton(browseAction("EPIC data file", epicFile));
		epic.add(epicFile);
		epic.add(epicFileBrowser);

//		String[] vars = new String[] { "Select a variable...",
//				"CANOPY(Percent)", "IMPERV(Percent)", "LANDMASK(None)",
//				"LU_INDEX(Category)", "XLAT_M(degrees latitude)",
//				"XLONG_M(degrees longitude" };
//		JComboBox varBox = new JComboBox(vars);
//		epic.add(varBox);

		layout.addWidgetPair(mcipButton, mcip, panel);
		layout.addWidgetPair(beldButton, beld, panel);
		layout.addWidgetPair(epicButton, epic, panel);

		layout.makeCompactGrid(panel, 3, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading

		return panel;
	}

	private Action displayAction() {
		return new AbstractAction("Display") {
			private static final long serialVersionUID = 8505323087261015010L;

			public void actionPerformed(ActionEvent e) {
				String temp = epicFile.getText();

				if (beldButton.isSelected())
					temp = beldFile.getText();

				if (mcipButton.isSelected())
					temp = mcipFile.getText();

				if (temp == null || temp.trim().isEmpty())
					return;

				temp = temp.trim();
				
				File file = new File(temp);

				if (!file.isFile() || !file.exists())
					return;

				final String filePath = temp;
				
				Thread populateThread = new Thread(new Runnable() {
					public void run() {
						callVerdi(filePath);
					}
				});
				populateThread.start();
			}
		};
	}

	private Action browseAction(final String name, final JTextField text) {
		return new AbstractAction("Browse...") {
			private static final long serialVersionUID = 0L;

			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser;
				File file = new File(text.getText());

				if (file != null && file.exists())
					chooser = new JFileChooser(file);
				else
					chooser = new JFileChooser(app.getCurrentDir());

				chooser.setDialogTitle("Please select the " + name);

				int option = chooser.showDialog(VisualizationPanel.this,
						"Select");
				if (option == JFileChooser.APPROVE_OPTION) {
					File selected = chooser.getSelectedFile();
					text.setText("" + selected);
					app.setCurrentDir(selected.getParentFile());
				}
			}
		};
	}

	private void callVerdi(final String file) {
		String visProgHome = Constants.getProperty(Constants.VISUAL_PROGRAM_HOME, msg);
		String visProg = Constants.getProperty(Constants.VISUAL_PROGRAM, msg);
		File ncfile = new File(file.replaceAll("\\\\", "\\\\\\\\"));
		ProcessBuilder pb = null; 
		String cmd = visProgHome.replaceAll("\\\\", "\\\\\\\\");

		String osName = System.getProperty("os.name");
		if (osName.equals("Linux") || osName.trim().equalsIgnoreCase("Mac OS X")) {
			cmd += "/" + visProg + " " + ncfile;
			System.out.println("Command: " + cmd);
			pb = new ProcessBuilder("csh", "-c", cmd);
		}

		// set up the working directory.
		pb.directory(ncfile.getParentFile());

		// merge child's error and normal output streams.
		pb.redirectErrorStream(true);

		Process p = null;

		try {
			p = pb.start();
			final InputStream es = p.getErrorStream();
			final InputStream is = p.getInputStream();

			// spawn two threads to handle I/O with child while we wait for it
			// to complete.
			Thread esthread = new Thread(new Runnable() {
				public void run() {
					readMsg(msg, es, "ERROR");
				}
			});
			esthread.start();
			
			Thread isthread = new Thread(new Runnable() {
				public void run() {
					readMsg(msg, is, "INPUT");
				}
			});
			isthread.start();


			if (Constants.DEBUG) {
				System.out.println("Verdi starting Process started: " + p.toString());
				msg.info("Verdi starting Process started: " + p.toString());
			}

			p.waitFor();
			es.close();
			is.close();
			
			if (Constants.DEBUG) {
				System.out.println("Verdi starting Process finished.");
				msg.info("Verdi starting Process finished.");
			}
		} catch (IOException e) {
			System.out.println("Error happened: " + e.getMessage());
			e.printStackTrace();
		} 
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.out.println("Interrupted: " + e.getMessage());
			e.printStackTrace();
		} 
		catch (Exception e) {
			System.out.println("Error happened: " + e.getMessage());
			e.printStackTrace();
		}
		finally {
			if (p != null) {
				try {
					p.getErrorStream().close();
					p.getInputStream().close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

	private void readMsg(MessageCenter msgcenter, InputStream is, String type) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        try {
            String message = reader.readLine();
            
            if (type.equals("ERROR") && message != null)
            	msgcenter.warn("Running Script:", new Exception(message));
            
            if (type.equals("INPUT"))
            	msgcenter.info("Start running script.", (message != null ? message : ""));
        } catch (Exception e) {
        	msgcenter.warn("Error reading message:", e);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            	msgcenter.warn("Error closing reading message:", e);
            }
        }

	}
	
	@Override
	public void projectLoaded() {
		fields = (VisualizationFields) app.getProject().getPage(fields.getName());
		if ( fields != null ){
			mcipFile.setText(fields.getMcipDataDir());
			beldFile.setText(fields.getBeldDataDir());
			epicFile.setText(fields.getEpicDataDir());
			mcipButton.setSelected(fields.isMcipDataSelected());
			beldButton.setSelected(fields.isBeldDataSelected());
			epicButton.setSelected(fields.isEpicDataSelected());
		} else{
			newProjectCreated();
		}
	}

	@Override
	public void saveProjectRequested() {
		if (mcipFile != null) fields.setMcipDataDir(mcipFile.getText());
		if (beldFile != null) fields.setBeldDataDir(beldFile.getText());
		if (epicFile != null) fields.setEpicDataDir(epicFile.getText());
		if (mcipButton != null) fields.setMcipDataSelected(mcipButton.isSelected());
		if (beldButton != null) fields.setBeldDataSelected(beldButton.isSelected());
		if (epicButton != null) fields.setEpicDataSelected(epicButton.isSelected());
	}

	@Override
	public void newProjectCreated() {
		fields.setMcipDataDir("");
		fields.setBeldDataDir("");
		fields.setEpicDataDir("");
		if (fields == null){
			fields = new VisualizationFields();
			app.getProject().addPage(fields);
		}
	}

}

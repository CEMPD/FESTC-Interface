package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ViewFileDialog extends JDialog {
	private static final long serialVersionUID = 712640590623729433L;
	private File file;
	private JTextArea text;
	 
	private boolean editable = false;
	private int maxWidth, preferedWidth;
	private int maxHeight, preferedHeight;
	private FestcApplication app;
	
	ViewFileDialog() {
		super.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/epa_logo.JPG")));
	}
	
	ViewFileDialog(Frame parent, String title, File file, boolean editable) {
        super(parent);
        super.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/epa_logo.JPG")));
        
        this.file = file;
        this.editable = editable;

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5)); // Border Layout does not respect maxSize
        //contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(createScrollTextPane(file));
        contentPane.add(createButtonsPanel(), BorderLayout.SOUTH);
        //contentPane.add(createButtonsPanel());
        setTitle(title);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        maxWidth = (int) (screenSize.getWidth()*0.7);
        maxHeight = (int) (screenSize.getHeight()*0.7);
        preferedWidth = (int) (screenSize.getWidth()/3);
        preferedHeight = (int) (screenSize.getHeight()/2);
        
        setMinimumSize(new Dimension(150,350));
        setPreferredSize(new Dimension(preferedWidth,preferedHeight));
        setMaximumSize(new Dimension(maxWidth,maxHeight));
        setSize(preferedWidth, preferedHeight);
        
//        System.out.println(preferedWidth);
//        System.out.println(preferedHeight);
        
        if (parent != null) {
        	//setPreferredSize(parent.getSize());
        	setLocation(getPointToCenter(parent));
        }
        
        pack();
        setModal(true);
    }

	private JComponent createScrollTextPane(File file) {
		text = new JTextArea();
		text.setText(getFileContent(file));
		text.setEditable(this.editable);
		text.setMaximumSize(new Dimension(this.maxWidth,this.maxHeight));
		ScrollablePanel panel = new ScrollablePanel();
		panel.add(text);
		//panel.setScrollableWidth( ScrollablePanel.ScrollableSizeHint.FIT );
		panel.setScrollableBlockIncrement(
		    ScrollablePanel.VERTICAL, ScrollablePanel.IncrementType.PERCENT, 200);
		panel.setScrollableBlockIncrement(
			    ScrollablePanel.HORIZONTAL, ScrollablePanel.IncrementType.PERCENT, 200);
		//return panel;
		//JScrollPane scroll = new JScrollPane(text);
		JScrollPane scroll = new JScrollPane(panel);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setMaximumSize(new Dimension(this.maxWidth,this.maxHeight));
		scroll.setPreferredSize(new Dimension(this.preferedWidth,this.preferedHeight));

		return scroll;
	}
	
	private void saveToFile() throws IOException{
		if ( file == null) {
			return;
		}
		
		BufferedWriter writer = new BufferedWriter( new FileWriter(file));
		String content = text.getText();
		if (writer!=null) {
			writer.write( content);
			writer.flush();
			writer.close();
		}
	}

	private String getFileContent(File file) {
		StringBuilder sb = new StringBuilder();

		if (file == null || !file.exists()) {
			return sb.toString();
		}

		BufferedReader reader = null;
		String line = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null)
				sb.append(line + "\n");
		} catch (IOException e) {
			sb.append("Error reading file!\n");
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					sb.append("Error closing file. " + e.getMessage() + "\n");
				}
		}

		return sb.toString();
	}
	
	private Action save() {
		return new AbstractAction("Save") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 3366464532536102377L;

			public void actionPerformed(ActionEvent e) {
				
				try {
					saveToFile();
				} catch (Exception ex) {
					app.showMessage("Save file", ex.getMessage());
				}
				
			}
		};
	}

	private JPanel createButtonsPanel() {
		JPanel container = new JPanel();

		if ( this.editable) {
			JButton saveButton = new JButton(" Save ");
			saveButton.addActionListener(save());
			container.add(saveButton);
			JButton closeButton = new JButton(" Close ");
			closeButton.addActionListener(close());
			container.add(closeButton);
		} else {
			JButton closeButton = new JButton(" Close ");
			closeButton.addActionListener(close());
			container.add(closeButton);
		}

//		JButton saveButton = new JButton("Save");
//		saveButton.addActionListener(saveFileAction());
//		container.add(saveButton);
//
//		JButton saveAsButton = new JButton("Save As...");
//		saveAsButton.setMargin(new Insets(2, 4, 2, 4));
//		saveAsButton.addActionListener(saveAsAction());
//		container.add(saveAsButton);

		return container;
	}
	
	private Action close() {
		return new AbstractAction() {
			private static final long serialVersionUID = 751779481914111010L;

			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
	}
	
	  public static Point getPointToCenter(Component comp) {
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    
	    if (comp == null) {
	       return new Point((int)screenSize.getWidth()/2,(int)screenSize.getHeight()/2);
	    }
	    
	    Dimension frameSize = comp.getSize();
	    
	    if (frameSize.height > screenSize.height) {
	      frameSize.height = screenSize.height;
	    }
	    
	    if (frameSize.width > screenSize.width) {
	      frameSize.width = screenSize.width;
	    }
	    
	    return new Point( (screenSize.width - frameSize.width) / 2,
	                      (screenSize.height - frameSize.height) / 2);
	  }
	  
	  // do not use the following now - get back to it later!!
	  /*
	  public static void main(String[] args) {
		  SwingUtilities.invokeLater(new Runnable() {

			  @Override
			  public void run() {
				  new ViewFileDialog().makeUI();
			  }
		  });
	  }
	  
	  public void makeUI() {
		  final JFrame frame = new ViewFileDialog("") {

			  @Override
			  public void paint(Graphics g) {
				  Dimension d = getSize();
				  Dimension m = getMaximumSize();
				  boolean resize = d.width > m.width || d.height > m.height;
				  d.width = Math.min(m.width, d.width);
				  d.height = Math.min(m.height, d.height);
				  if (resize) {
					  Point p = getLocation();
					  setVisible(false);
					  setSize(d);
					  setLocation(p);
					  setVisible(true);
				  }
				  super.paint(g);
			  }
		  };
		  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOS E);
		  frame.setSize(400, 400);
		  frame.setMaximumSize(new Dimension(500, 500));
		  frame.setMinimumSize(new Dimension(300, 300));
		  frame.setLocationRelativeTo(null);
		  frame.setVisible(true);
	  }
	  
	}
	*/

}


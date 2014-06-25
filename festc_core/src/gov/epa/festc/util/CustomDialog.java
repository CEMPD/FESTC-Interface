package gov.epa.festc.util;

import gov.epa.festc.core.proj.CallBack;
import gov.epa.festc.gui.MessagePanel;
import gov.epa.festc.gui.SingleLineMessagePanel;

import javax.swing.JDialog; 

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.io.File;

import java.awt.event.ActionEvent;

public class CustomDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 8618516328408350335L;
	private JPanel mainPanel = null;
    private JButton yesButton = null;
    private JButton noButton = null;
    private MessagePanel messagePanel;
    private CallBack callBack;
    private JPanel customPane;
    private String action;
    //private File projFile;

    public CustomDialog(CallBack callback, JFrame frame, boolean modal, JPanel customPane, 
    		String action, String msg, File projFile) {
        super(frame, modal);
        this.setTitle(msg);
        
        this.callBack = callback;
        this.action = action;
        this.customPane = customPane;
        //this.projFile = projFile;
        
        messagePanel = new SingleLineMessagePanel();
        
        mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel);
        mainPanel.add(messagePanel, BorderLayout.NORTH);
        mainPanel.add(customPane, BorderLayout.CENTER);
        
        JPanel buttonPane = new JPanel();
        yesButton = new JButton(action);
        yesButton.addActionListener(this);
        yesButton.setToolTipText(msg);
        buttonPane.add(yesButton); 
        noButton = new JButton("Cancel");
        noButton.setToolTipText("Cancel " + action);
        noButton.addActionListener(this);
        if ( projFile == null && action == Constants.SAVE_SCENARIO)
        	yesButton.setVisible(false);
        buttonPane.add(noButton, BorderLayout.LINE_END);
        JPanel buttonLayout = new JPanel(new BorderLayout());
        buttonLayout.add(new JLabel(""), BorderLayout.LINE_START);
        buttonLayout.add(buttonPane, BorderLayout.LINE_END);
        buttonLayout.setBorder(BorderFactory.createEmptyBorder(10, 2, 10, 10));
        
        mainPanel.add(buttonLayout, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }
    
    public void hideYesButton(){
    	yesButton.setVisible(false);
    	//yesButton.hide();
    }

    public void actionPerformed(ActionEvent e) {
        if(yesButton == e.getSource()) {
        	if (callBack != null) {
        		try {
					callBack.onCall(action, customPane);
				} catch (ModelYearInconsistantException excpt) {
					messagePanel.setMessage(excpt.getMessage() + ". Are you sure to continue?");
					return;
				} catch (Exception e1) {
					//e1.printStackTrace();
					messagePanel.setError(e1.getMessage());
					return;
				}
        	}
        }
        
        setVisible(false);
    }
    
}
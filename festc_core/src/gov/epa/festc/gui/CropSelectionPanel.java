package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.util.Constants;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class CropSelectionPanel extends JPanel {

	private ListWidget lstCrops;
	private ListWidget selectedCrops;
	private FestcApplication app;

	private JButton selectCropsButton;
	private JButton unselectCropsButton;

	public CropSelectionPanel(FestcApplication application) {
		app = application;
		add(createPanel());
	}

	private JPanel createPanel() {
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.X_AXIS));
		main.add(Box.createRigidArea(new Dimension(8,0)));
		main.add(getWestPanel());
		main.add(Box.createRigidArea(new Dimension(8,0)));
		main.add(getButtonPanel());
		main.add(Box.createRigidArea(new Dimension(8,0)));
		main.add(getEastPanel());
		main.add(Box.createRigidArea(new Dimension(8,0)));
		main.repaint();
		return main;
	}

	private JPanel getWestPanel() {
//		JPanel leftPanel = new JPanel();
		JPanel cropsPanel = new JPanel(new BorderLayout());
		cropsPanel.add(new JLabel("Crops/Grasses"), BorderLayout.NORTH);
		lstCrops = new ListWidget(Constants.CROPS.keySet().toArray());
		//lstCrops.setVisibleRowCount(12);
		
		cropsPanel.add(Box.createRigidArea(new Dimension(0,10)), BorderLayout.CENTER);
		JScrollPane scrollCropList = new JScrollPane(lstCrops, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollCropList.setPreferredSize(new Dimension(120, 200));
		
		cropsPanel.add(scrollCropList, BorderLayout.SOUTH);
//		leftPanel.add(cropsPanel);
//		leftPanel.add(getButtonPanel());
        return cropsPanel;
	}
	
	private JPanel getButtonPanel(){
		JPanel buttonPanel =  new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 
		JPanel includeButtonPanel =  new JPanel();
		includeButtonPanel.setLayout(new BorderLayout(0, 0));
//		includeButtonPanel.setPreferredSize(new Dimension(50, 45));
//		includeButtonPanel.setMinimumSize(new Dimension(50, 45));
		JPanel excludeButtonPanel =  new JPanel();
		excludeButtonPanel.setLayout(new BorderLayout(0, 0));
//		excludeButtonPanel.setPreferredSize(new Dimension(50, 45));
//		excludeButtonPanel.setMinimumSize(new Dimension(50, 45));
		
		selectCropsButton = new JButton(setCropsData());
		excludeButtonPanel.add(selectCropsButton, BorderLayout.SOUTH);		
		
		unselectCropsButton = new JButton(unSetCropsData());
		includeButtonPanel.add(unselectCropsButton, BorderLayout.NORTH);
		buttonPanel.add(excludeButtonPanel);
        buttonPanel.add(Box.createRigidArea(new Dimension(0,10)));
        buttonPanel.add(includeButtonPanel);
		 
		return buttonPanel;
	}
	
	private JPanel getEastPanel(){
		JPanel seCropsPanel = new JPanel(new BorderLayout());
		seCropsPanel.add(new JLabel("Selected Crops"), BorderLayout.NORTH);
		selectedCrops = new ListWidget(new String[]{});
		//selectedCrops.setVisibleRowCount(12);
		seCropsPanel.add(Box.createRigidArea(new Dimension(0,10)), BorderLayout.CENTER);
		JScrollPane seScrollCropList = new JScrollPane(selectedCrops, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		seScrollCropList.setPreferredSize(new Dimension(120, 200));
		seCropsPanel.add(seScrollCropList, BorderLayout.SOUTH);
		return seCropsPanel;
		 
	}
	
	public String selectedItemTostring(){
		int size = selectedCrops.getAllElements().length;
		String[] seCrops = selectedCrops.getAllElements();
		String crops = "\"";
		for ( int i =0; i<size; i++){			 
		    crops += " " + seCrops[i];
		}
		crops += "\"";
		return crops; 
	}
	
	public String[] getSelectedCrops(){
		return selectedCrops.getAllElements();
	}

	private Action setCropsData() {
		return new AbstractAction("Select =>") {
			public void actionPerformed(ActionEvent e) {
				
				int[] indices = lstCrops.getSelectedIndices();
				Object[] cropNames = lstCrops.getSelectedValues();
				if ( cropNames == null || cropNames.length==0) {
					// TODO report error
					app.showMessage("Select crop(s)", "Please select some crops first!");
					return;
				}
				else {    
					for ( int i= 0; i < indices.length; i++)
						selectedCrops.addElement(cropNames[i]);
					lstCrops.removeElements(cropNames);
                }				
			}
		};
	}
	
	private Action unSetCropsData() {
		return new AbstractAction("<= Unselect") {

			public void actionPerformed(ActionEvent e) {
				Object[] cropNames = selectedCrops.getSelectedValues();
				if ( cropNames == null || cropNames.length==0) {
					 
					app.showMessage("Select crop(s)", "Please select some crops first!");
					return;
				}
				else { 	
					selectedCrops.removeElements(cropNames);
					String[] allSelected = selectedCrops.getAllElements();	
					lstCrops.setElements(Constants.CROPS.keySet().toArray());
				    lstCrops.removeElements(allSelected);
				    lstCrops.revalidate();
                }				
			}
		};
	}

}

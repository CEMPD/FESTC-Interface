package gov.epa.festc.test;


public class TestCropDataTablePanel {
    private static void createAndShowGUI() throws Exception {
    	
//        List<CropRowData> tableData = new ArrayList<CropRowData>();
//        
//        CropRowData row = new CropRowData(false, "Corn", "1", "Category", "Run File", "File", "Cont File");
//        tableData.add( row);
//        row = new CropRowData(true, "Corn2", "2", "Category", "Run File", "File", "Cont File");
//        tableData.add( row);
//        
//        //Create and set up the window.
//        JFrame frame = new JFrame("TableDemo");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// 
//        //Create and set up the content pane.
//        JPanel tablePane = new CropDataTablePanel(new CropTableModel(tableData), "", frame, null);
//        tablePane.setOpaque(true); //content panes must be opaque
//        frame.setContentPane(tablePane);
// 
//        //Display the window.
//        frame.pack();
//        frame.setVisible(true);
//        
//        ((CropDataTablePanel) tablePane).addRow(true, "Corn3", "3", "Category", "Run File", "File", "Cont File");
    }
    
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
					createAndShowGUI();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
    }
}

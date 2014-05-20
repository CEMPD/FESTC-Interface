package gov.epa.festc.gui;

import gov.epa.festc.core.FestcApplication;
import gov.epa.festc.util.ModelYearInconsistantException;
import gov.epa.festc.util.SpringLayoutGenerator;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class DomainPanel extends UtilFieldsPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2383585074083309830L;

	JTextField simuYear;
	JTextField scenaName; // new scenario name
	private String modelYear;
	private FestcApplication app;

	public DomainPanel(FestcApplication app) {
		super(new SpringLayout());	
		init();
		this.app = app;
		modelYear = app.getSSimYear();
		SpringLayoutGenerator layout = new SpringLayoutGenerator();

		simuYear = new JTextField(40);
		scenaName = new JTextField(40);

		JPanel simuYearPanel = new JPanel();
		simuYearPanel.add(simuYear);

		JPanel scenaNamePanel = new JPanel();
		scenaNamePanel.add(scenaName);

		layout.addLabelWidgetPair("Grid Description: ", getGridDescPanel(true), this);
		layout.addLabelWidgetPair("Simulation Year: ", simuYearPanel, this);
		layout.addLabelWidgetPair("Scenario Name: ", scenaNamePanel, this);

		layout.makeCompactGrid(this, 3, 2, // number of rows and cols
				10, 10, // initial X and Y
				5, 5); // x and y pading
	}

	// private JComponent getNewGridDescPanel(boolean isNew) {
	// JPanel panel = new JPanel();
	// panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	//
	// JPanel gridNamePanel = new JPanel(new SpringLayout());
	// SpringLayoutGenerator gridNameLayout = new SpringLayoutGenerator();
	//
	// gridName = new JTextField(28);
	// gridName.setToolTipText("Use full description, ie. +proj=lcc +a=6370000.0 +b=6370000.0 +lat_1=33 +lat_2=45 +lat_0=40 +lon_0=-97");
	// //if (isNew )
	// gridName.setText("+proj=lcc +a=6370000.0 +b=6370000.0 +lat_1=33 +lat_2=45 +lat_0=40 +lon_0=-97");
	// proj4proj = new JTextField(28);
	//
	// rows = new JFormattedTextField(NumberFormat.getNumberInstance());
	// rows.setColumns(3);
	// cols = new JFormattedTextField(NumberFormat.getNumberInstance());
	// cols.setColumns(3);
	// xmin = new JFormattedTextField(NumberFormat.getNumberInstance());
	// xmin.setColumns(9);
	// ymin = new JFormattedTextField(NumberFormat.getNumberInstance());
	// ymin.setColumns(9);
	// xSize = new JFormattedTextField(NumberFormat.getNumberInstance());
	// xSize.setColumns(5);
	// ySize = new JFormattedTextField(NumberFormat.getNumberInstance());
	// ySize.setColumns(5);
	//
	// JPanel colRowPanel = new JPanel(new SpringLayout());
	// SpringLayoutGenerator colRowLayout = new SpringLayoutGenerator();
	// colRowLayout.addLabelWidgetPair("Rows:", rows, colRowPanel);
	// colRowLayout.addLabelWidgetPair("Columns:", cols, colRowPanel);
	// colRowLayout.makeCompactGrid(colRowPanel, 2, 2, 5, 5, 5, 5);
	//
	// JPanel xyMinPanel = new JPanel(new SpringLayout());
	// SpringLayoutGenerator xyMinLayout = new SpringLayoutGenerator();
	// xyMinLayout.addLabelWidgetPair("XMin:", xmin, xyMinPanel);
	// xyMinLayout.addLabelWidgetPair("YMin:", ymin, xyMinPanel);
	// xyMinLayout.makeCompactGrid(xyMinPanel, 2, 2, 5, 5, 5, 5);
	//
	// JPanel cellSizePanel = new JPanel(new SpringLayout());
	// SpringLayoutGenerator cellSizeLayout = new SpringLayoutGenerator();
	// cellSizeLayout.addLabelWidgetPair("XCellSize:", xSize, cellSizePanel);
	// cellSizeLayout.addLabelWidgetPair("YCellSize:", ySize, cellSizePanel);
	// cellSizeLayout.makeCompactGrid(cellSizePanel, 2, 2, 5, 5, 5, 5);
	//
	// panel.add(colRowPanel);
	// panel.add(cellSizePanel);
	// panel.add(xyMinPanel);
	//
	// gridNameLayout.addLabelWidgetPair("Proj4Projection: ", proj4proj,
	// gridNamePanel);
	// gridNameLayout.addLabelWidgetPair("Grid Name:", gridName, gridNamePanel);
	// gridNameLayout.makeCompactGrid(gridNamePanel, 2, 2, // number of rows and
	// cols
	// 10, 10, // initial X and Y
	// 5, 5); // x and y pading
	//
	// JPanel container = new JPanel();
	// container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	// container.add(panel);
	// container.add(gridNamePanel);
	//
	// return container;
	// }

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void validateFields() throws Exception{
		validateGrids();
		
		String simuY = simuYear.getText() == null ? "" : simuYear.getText();
		if (simuY.trim().isEmpty())
			throw new Exception("Simulation year is empty.");
		
		try {
			Integer.parseInt(simuY);
		}catch(NumberFormatException e) {
			throw new Exception("Simulation year is not a number.");
		}
		
		
		String scemN = scenaName.getText() == null ? "" : scenaName.getText();
		if (scemN.trim().isEmpty())
			throw new Exception(" New scenario name is empty.");
		if (scemN.trim().length() > 16)
			throw new Exception(" New scenario name is too long (larger than 16 chars).");
		if (scemN.trim().contains(" ") )
			throw new Exception(" New scenario name has space in between.");
	}
	
	public String getSimuYear() throws Exception {
		return simuYear.getText();
	}

	public String getScenaName() throws Exception {
		return scenaName.getText();
	}

	public String getGridName() {
		return gridName.getText();
	}

	public String getProj4proj() {
		return proj4proj.getText();
	}

	public int getRows() {
		if (rows.getValue() != null) {
			return Integer.parseInt(rows.getValue() + "");
		}
		return 0;
	}

	public int getCols() {
		if (cols.getValue() != null) {
			return Integer.parseInt(cols.getValue() + "");
		}
		return 0;
	}

	public float getXmin() {
		if (xmin.getValue() != null) {
			return Float.parseFloat(xmin.getValue() + "");
		}
		return 0;
	}

	public float getYmin() {
		if (ymin.getValue() != null) {
			return Float.parseFloat(ymin.getValue() + "");
		}
		return 0;
	}

	public float getxSize() {
		if (xSize.getValue() != null) {
			return Float.parseFloat(xSize.getValue() + "");
		}
		return 0;
	}

	public float getySize() {
		if (xSize.getValue() != null) {
			return Float.parseFloat(ySize.getValue() + "");
		}
		return 0;
	}

}

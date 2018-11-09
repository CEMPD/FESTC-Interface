package gov.epa.festc.core.proj;

public abstract class PageFields {
	protected String message;
	protected String scenarioDir;
	
	protected int rows;
	protected int cols;
	protected float xcellsize;
	protected float ycellsize;
	protected float xmin;
	protected float ymin;
	 
	protected String proj4projection;
	protected String gridName;
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String msg) {
		this.message = msg;
	}
	
	public void setScenarioDir(String scenarioDir) {
		this.scenarioDir = scenarioDir;
	}

	public String getScenarioDir() {
		return scenarioDir;
	}
	
	public void setRows(int rows) {
		this.rows = rows;
	}
	public int getRows() {
		return rows;
	}
	public void setCols(int cols) {
		this.cols = cols;
	}
	public int getCols() {
		return cols;
	}
	public void setXcellSize(float xcellsize) {
		this.xcellsize = xcellsize;
	}
	public float getXcellSize() {
		return xcellsize;
	}
	public void setYcellSize(float ycellsize) {
		this.ycellsize = ycellsize;
	}
	public float getYcellSize() {
		return ycellsize;
	}
	public void setXmin(float xmin) {
		this.xmin = xmin;
	}
	public float getXmin() {
		return xmin;
	}
	public void setYmin(float ymin) {
		this.ymin = ymin;
	}
	public float getYmin() {
		return ymin;
	}
	
	public void setProj(String proj) {
		this.proj4projection = proj;
	}
	public String getProj() {
		return proj4projection;
	}
	public void setGridName(String gridName) {
		this.gridName = gridName;
	}
	public String getGridName() {
		return gridName;
	}
	
	public abstract String getName();
}

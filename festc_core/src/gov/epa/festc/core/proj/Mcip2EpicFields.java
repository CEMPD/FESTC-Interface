package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.Mcip2EpicFields")
public class Mcip2EpicFields  extends PageFields{

	private String startdate;
	private String enddate;
	private String mcipDataDir;
	private String depSelection;
	private String cmaqDepsDir;
	
	public Mcip2EpicFields() {
		//NOTE: no-op
	}

	public String getStartdate() {
		return startdate;
	}

	public void setStartdate(String startdate) {
		this.startdate = startdate;
	}

	public String getEnddate() {
		return enddate;
	}

	public void setEnddate(String enddate) {
		this.enddate = enddate;
	}

	public String getDatadir() {
		return mcipDataDir;
	}

	public void setDatadir(String datadir) {
		this.mcipDataDir = datadir;
	}

	public String getCmaqDepsDir() {
		return cmaqDepsDir;
	}

	public void setCmaqDepsDir(String cmaqDepsDir) {
		this.cmaqDepsDir = cmaqDepsDir;
	}
	
	public String getDepSelection() {
		return depSelection;
	}

	public void setDepSelection(String depSelection) {
		this.depSelection = depSelection;
	}


	public void setGridName(String gridName) {
		this.gridName = gridName;
	}

	public String getGridName() {
		return gridName;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return Mcip2EpicFields.class.getCanonicalName();
	}
	
}

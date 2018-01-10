package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

import gov.epa.festc.gui.Epic2CMAQPanel;

@XmlRootElement(name = "gov.epa.festc.core.proj.Epic2SWATFields")
public class Epic2SWATFields extends PageFields{
	
	private String beld4ncf;
	private String metdep;
	private String nDepSelection;
	private String hucSelection;
	private String outfileprefix;
	
	public Epic2SWATFields() {
		//NOTE: no-op
	}
	
	public String getNDepSelection() {
		return nDepSelection;
	}

	public void setNDepSelection(String nDepSelection) {
		this.nDepSelection = nDepSelection;
	}

	public String getHucSelection() {
		return hucSelection;
	}

	public void setHucSelection(String hucSelection) {
		this.hucSelection = hucSelection;
	}

//	public String getWeathPath() {
//		return weathPath;
//	}
//
//	public void setWeathPath(String weathPath) {
//		this.weathPath = weathPath;
//	}
	
	public void setBeld4ncf(String beld4ncf) {
		this.beld4ncf = beld4ncf;
	}
	public String getBeld4ncf() {
		return beld4ncf;
	}
	
	public void setMetdep(String metdep) {
		this.metdep = metdep;
	}
	public String getMetdep() {
		return metdep;
	}

	public String getOutfileprefix() {
		return outfileprefix;
	}

	public void setOutfileprefix(String outfileprefix) {
		this.outfileprefix = outfileprefix;
	}


	@Override
	public String getName() {
		return Epic2SWATFields.class.getCanonicalName();
	}
	
}

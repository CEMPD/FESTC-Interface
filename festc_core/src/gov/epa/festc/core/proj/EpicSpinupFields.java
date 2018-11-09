package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.EpicSpinupFields")
public class EpicSpinupFields extends DomainFields {
 
	private String finishedCrops;
	private String nDepDir;
	private String co2Fac;
	private String runTiledrain;
	public EpicSpinupFields() {
		//NOTE: no-op
	}

	public String getFinishedCrops() {
		return finishedCrops;
	}
	
	public void setFinishedCrops(String finishedCrops) {
		this.finishedCrops = finishedCrops;
	}
	
	public String getNDepDir() {
		return nDepDir;
	}
	
	public void setRunTiledrain(String runTiledrain) {
		this.runTiledrain = runTiledrain;
	}
	
	public String getRunTiledrain() {
		return runTiledrain;
	}
	
	public void setNDepDir(String nDepDir) {
		this.nDepDir = nDepDir;
	}
	
	public void setCO2Fac(String co2fac) {
		this.co2Fac = co2fac;
	}
	public String getCO2Fac() {
		return co2Fac;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return EpicSpinupFields.class.getCanonicalName();
	}
	
}

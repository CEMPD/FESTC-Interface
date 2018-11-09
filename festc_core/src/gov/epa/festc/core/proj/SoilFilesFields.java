package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.SoilFilesFields")
public class SoilFilesFields extends PageFields {

	private String finishedCrops;
	
	public SoilFilesFields() {
		//NOTE: no-op
	}

	public String getFinishedCrops() {
		return finishedCrops;
	}
	
	public void setFinishedCrops(String finishedCrops) {
		this.finishedCrops = finishedCrops;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return SoilFilesFields.class.getCanonicalName();
	}
	
}

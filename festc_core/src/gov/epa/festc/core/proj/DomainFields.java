package gov.epa.festc.core.proj;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gov.epa.festc.core.proj.DomainFields")
public class DomainFields extends PageFields {

	protected String simYear;
	protected String nlcdYear;
	protected String cfertYear;
	protected String cminAcres;
	//protected String csimYear;

	
	public String getCMinAcres() {
		return cminAcres;
	}

	public String getCFertYear() {
		return cfertYear;
	}
		
	public String getSimYear() {
		return simYear;
	}
	
	public String getNlcdYear() {
		return nlcdYear;
	}
	
	
	public void setCMinAcres(String acres) {
		this.cminAcres = acres;
	}
	
	public void setCFertYear(String cfertYear) {
		this.cfertYear = cfertYear;
	}

	public void setSimYear(String year) {
		this.simYear = year;
	}
	
	public void setNlcdYear(String year) {
		this.nlcdYear = year;
	}
	
	public void setNlcdYear() {
		Integer sYear =  Integer.parseInt(simYear);
		if ( sYear < 2006 )  this.nlcdYear = "2001";
		if ( sYear >= 2006 )  this.nlcdYear = "2006"; // sYear >= 2006 && sYear < 2011
		//if ( sYear >= 2011 )  this.nlcdYear = "2011";
	}
	

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return DomainFields.class.getCanonicalName();
	}
	
}

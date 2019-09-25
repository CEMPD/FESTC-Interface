package gov.epa.festc.core.proj;

import gov.epa.festc.core.Project;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Saves elements of the application to disk so they can be loaded later.
 *
 * @author IE, UNC Chapel Hill
 * @version $Revision$ $Date$
 */
public class ProjectLoader {
	private JAXBContext context;
	private Marshaller m;
	private Unmarshaller um;

	public ProjectLoader() throws JAXBException {
	    // create JAXB context and instantiate marshaller/unmarshaller
	    // SiteInfoGenFields: EPIC Site Info: 
		// Mcip2EpicFields: MCIP/EPIC to EPIC
		 
		// SiteFilesFields : EPIC Site file generation ( run two executables )
		// SoilFilesFields : EPIC Soil file generation ( run soil match)
		// ManageSpinupFields: Management file generation for spinup
		// ManFileModFields: View/Edit EPIC Inputs
		// EpicSpinupFields : Epic runs for spinup 
		// EpicYearlyAverage2CMAQFields: EPIC Yearly Output
		// Epic2CMAQFields: 
		// Epic2SWATFields: 
	    context = JAXBContext.newInstance(Project.class, Beld4DataGenFields.class, Mcip2EpicFields.class, SiteFilesFields.class, 
	    		SoilFilesFields.class, ManageSpinupFields.class, ManFileModFields.class, EpicSpinupFields.class, 
	    		EpicYearlyAverage2CMAQFields.class, ManageAppFields.class, EpicAppFields.class, 
	    		VisualizationFields.class, SiteInfoGenFields.class, Epic2CMAQFields.class,
	    		Epic2SWATFields.class,DomainFields.class);
	    m = context.createMarshaller();
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    um = context.createUnmarshaller();
	}

	/**
	 * Saves the specified project to the specified file.
	 *
	 * @param file    the file to save the model to
	 * @param project the project to save
	 * @throws IOException if there is an error duing saving
	 * @throws JAXBException 
	 * @throws JAXBException if the xml mapping error occurs
	 */
	public void save(File file, Project project) throws IOException, JAXBException {
		m.marshal(project, file);
	}

	/**
	 * Loads the project data in specified file into the
	 * specified project.
	 *
	 * @param file    the file containing the data to load
	 * @param project the project to load the data into
	 * @param manager used to create the Datasets
	 * @param creator the creator used to properly create the formula items
	 * @throws IOException if there is an error during loading.
	 * @throws JAXBException if the xml mapping error occurs
	 */
	public void load(File file, Project project) throws JAXBException, IOException {
		Project loaded = (Project) um.unmarshal(new FileReader(file));
		project.setPageList(loaded.getPageList());
	}
}

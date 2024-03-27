package gov.epa.festc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import simphony.util.messages.MessageCenter;

public class Constants {
	public static final String LABEL_EPIC_SCENARIO = "Scenario Directory:";
	public static final String PROPERTY_COMMENT_CHAR = "#";
	public static final String CONFIG_HOME = "config.home";
	public static final String PROJECT_HOME = "project.home";
	public static final String VERDI_HOME = "verdi.home";
	public static final String VISUAL_PROGRAM_HOME = "visual.program.home";
	public static final String VISUAL_PROGRAM = "visual.program";
	public static final String SA_HOME = "sa.home";
	public static final String SA_SETUP_FILE = "/bin/sa_setup.csh";
	public static final String EPIC_HOME = "epic.home";
	public static final String QUEUE_NAME = "queue.name";
//	public static final String QUEUE_BMEM = "queue.bigmem";
	//public static final String QUEUE_PRELOG = "queue.prelog";
	public static final String QUEUE_OPT = "queue.option";
	public static final String QUEUE_CMD = "queue.cmd";
	public static final String QUEUE_SINGULARITY_MODULE = "queue.singularitymodule";
	public static final String QUEUE_SINGULARITY_IMAGE = "queue.singularityimage";
	public static final String QUEUE_SINGULARITY_BIND = "queue.singularitybind";
	
	public static final String QUEUE_BELD4_CMD = "queue.beld4";
	public static final String QUEUE_SITE_INFO = "queue.siteinfo";
	public static final String QUEUE_MC2EPIC = "queue.mc2epic";
	public static final String QUEUE_EPIC_SITE = "queue.epicsite";
	public static final String QUEUE_SOIL_MATCH = "queue.soilmatch";
	public static final String QUEUE_MAN_SPINUP = "queue.manspinup";
	public static final String QUEUE_EPIC_SPINUP = "queue.epicspinup";
	public static final String QUEUE_MAN_APP = "queue.manapp";
	public static final String QUEUE_EPIC_APP = "queue.epicapp";
	public static final String QUEUE_YEARLY_EXT = "queue.yearlyext";
	public static final String QUEUE_EPIC2CMAQ = "queue.epic2cmaq";
	public static final String QUEUE_EPIC2SWAT = "queue.epic2swat";
	
	public static final String WORK_DIR = "work.dir";
	public static final String EPIC_VER = "epic.ver";
	public static final String USER_HOME = "user.home";
	public static final String LINE_SEPARATOR = "line.separator";
	public static final String NEW_SCENARIO = "Create";
	public static final String COPY_SCENARIO = "Copy";
	public static final String SAVE_SCENARIO = "Save";
	public static final String DELETE_SCENARIO = "delete";
	public static final String ALLOW_DIFF_CHECK = "allow.diff.check";
	public static final String PROPERTY_FILE = "/festc/config.properties";
	public static final String[] PROJECTIONS = new String[] {"Stand Lambert", "Other"};
	public static final String[] DEPSELECTIONS = new String[] {"CMAQ deposition directory", "Default", "Zero"};

	// EPIC Action Names
	public static final String BELD4_GEN = "BELD4 Data Generation";
	public static final String SITE_INFO = "Crop Site Info Generation";
	public static final String MC2EPIC = "WRF/CMAQ to EPIC";
	public static final String EPIC_SITE = "EPIC Site File Generation";
	public static final String SOIL_MATCH = "Soil Match for EPIC Spinup";
	public static final String MAN_SPINUP = "Management File Generation for Spinup";
	public static final String EDIT_INFILES = "View/Edit EPIC Inputs";
	public static final String[] FERTYEARS = new String[] {"2001", "2006", "2011"};
	public static final String[] NLCDYEARS = new String[] {"2001", "2006", "2011"};
	public static final String[] NDEPS = new String[] {"CMAQ", "EPIC parameter input file", 
		                               "2002-2006 5-year CMAQ average",
		                               "2006-2010 5-year CMAQ average"};
	public static final String[] SU_NDEPS = new String[] {"EPIC parameter input file", 
        "2002-2006 5-year CMAQ average",
        "2006-2010 5-year CMAQ average"};
	public static final String[] SWAT_NDEPS = new String[] {"CMAQ", 
			"2002-2006 5-year CMAQ average",
	        "2006-2010 5-year CMAQ average"}; //dir:dailyNDep_2004, dailyNDep_2008
	public static final String[] AREAS = new String[] {"Domain","State","County","HUC8","HUC6","HUC2"};
	public static final String EPIC_SPINUP = "EPIC Runs for Spinup";
	public static final String MAN_APP = "Management File Generation for Application";
	public static final String EPIC_APP = "EPIC Runs for Application";
	public static final String EPIC_YEAR = "EPIC Yearly Extraction";	
	public static final String EPIC2CMAQ = "EPIC to CMAQ";
	public static final String EPIC2SWAT = "EPIC to SWAT";
	public static final String VISU = "Visualization";
	
	// Crop list
	public static final HashMap<String, Integer> CROPS = new HashMap<String, Integer>(){
		private static final long serialVersionUID = -7600822219153629808L;
		{
			put("HAY", 1);
			put("ALFALFA", 3);
			put("OTHGRASS", 5);
			put("BARLEY", 7);
			put("EBEANS", 9);
			put("CORNG", 11);
			put("CORNS", 13);
			put("COTTON", 15);
			put("OATS", 17);
			put("PEANUTS", 19);
			put("POTATOES", 21);
			put("RICE", 23);
			put("RYE", 25);
			put("SORGHUMG", 27);
			put("SORGHUMS", 29);			
			put("SOYBEANS", 31);
			put("SWHEAT", 33);
			put("WWHEAT", 35);
			put("OTHER", 37);
			put("CANOLA", 39);
			put("BEANS", 41);
		}
	};

	public static final String[] CATEGORIES = new String[]{
		"SIT", 
		"SOL", 
		"OPC",
		"OPS"
	};
	
	public static final String BOX_SIZE = "This is used to calculate combobox sizes.";
 
	public static boolean DEBUG=true;
	
	private static boolean propertiesLoaded = false;
	

	private static void loadPropertyFile(MessageCenter msg) {

		File file = new File(System.getProperty(USER_HOME) + PROPERTY_FILE);
		try {
			if (!file.exists()) {			
				msg.warn("Error loading config properties", new Exception("File " + file + " doesn't exist."));
				System.out.println("File " + file + " doesn't exist.");
				System.exit(1);
			}
			
			if (file.exists()) {
				System.getProperties().load(new FileInputStream(file));
				propertiesLoaded = true;
				System.out.println("Config file " + file + " loaded at: " + new Date());
			}
			 
			if ( System.getProperty("debug") == null) {
				DEBUG = false;
			} else {
				DEBUG = "TRUE".equalsIgnoreCase(System.getProperty("debug").trim()); // init the debug according to config file
			}
			
			test();
			
		} catch (IOException e) {
			msg.warn("Error loading config properties", e);
		}
	}
	
	public static String getProperty(String key, MessageCenter msg) {
		if (!propertiesLoaded)
			loadPropertyFile(msg);
		
		String prop = System.getProperty(key);
		if ( prop != null) {
			if (prop.contains(PROPERTY_COMMENT_CHAR)) {
				prop = prop.substring(0, prop.indexOf(PROPERTY_COMMENT_CHAR));
			}
			prop = prop.trim();
		}
		return prop;
	}
	
	private static void test() {
		MessageCenter msg = MessageCenter.getMessageCenter(Constants.class);
		System.out.println(getProperty("visual.program.home", msg));
		System.out.println(getProperty("visual.program", msg));
		
	}
	
}

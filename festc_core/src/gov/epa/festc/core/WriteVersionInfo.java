package gov.epa.festc.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;

public class WriteVersionInfo {
	private static String version = "2.1";
	private static String timestamp = new Date(new java.util.Date().getTime()).toString(); //in the yyyy-mm-dd format
	private static String ls = System.getProperty("line.separator");
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String versionInfoClass = "package gov.epa.festc.core;" + ls + ls
			+ "/**" + ls 
			+ " * NOTE: Auto-generated file. Please don't try to change." + ls
			+ " * @version $Revision$ $Date$" + ls
			+ " */" + ls
			+ "public class VersionInfo {" + ls
			+ "	public static final String version = \"" + version + "\";" + ls
			+ "	public static final String date = \"" + timestamp + "\";" + ls + ls
			+ "	public static String getVersion() {" + ls
			+ "		return version;" + ls
			+ "	}" + ls + ls
			+ "	public static String getDate() {" + ls
			+ "		return date;" + ls 
			+ "	}" + ls + ls
			+ "}";
		
		File file = new File(System.getProperty("user.dir") + "/../festc_core/src/gov/epa/festc/core/VersionInfo.java");

		try {
			FileWriter writer = new FileWriter(file);
			writer.write(versionInfoClass);
			writer.close();
		} catch (IOException e) {
			System.out.println("Error writting VersionInfo.java file. " + e.getMessage());
		}
	}

}

package gov.epa.festc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import simphony.util.messages.MessageCenter;

public class FileRunner { // TODO: change this to a cross-platform launcher
	
	public static void runScript(final String file, final String logText, final MessageCenter msg) {
		String qcmd = Constants.getProperty(Constants.QUEUE_CMD, msg);
		String qopt = Constants.getProperty(Constants.QUEUE_OPT, msg);
		String qname = Constants.getProperty(Constants.QUEUE_NAME, msg);
		String qbigmem = Constants.getProperty(Constants.QUEUE_BMEM, msg);
		String workdir = Constants.getProperty(Constants.WORK_DIR, msg);
		boolean useBigMem = false;
		if (file.contains("epic2CMAQ_"))  useBigMem = true;
		
		if (workdir == null || workdir.trim().isEmpty())
			workdir = Constants.getProperty(Constants.USER_HOME, msg);
		
		String time = DateFormat.format_MMDDYYYYHHmmss(new Date());
		String autolog = workdir + File.separator + time + "-EPIC-to-CMAQ.log";
		String log = (logText == null || logText.trim().isEmpty()) ? autolog : logText.trim();
		File script = new File(file.replaceAll("\\\\", "\\\\\\\\"));
		String scriptDir = script.getParent();
		//System.out.print(scriptDir);
		try {
			Runtime.getRuntime().exec("chmod 755 " + script);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		//Set up qcmd or use ./ if qcmd is null
		String cmd = "cd " + scriptDir+ "\n";

		//if ( qcmd != null && qcmd !="" )
		cmd = cmd + qcmd + " -q " + qname;

		//System.out.println("qcmd="+qcmd + qname);
		if (qcmd != null && qcmd.equalsIgnoreCase("qsub")) {
			qopt += " -j oe ";
			cmd = cmd + " " + qopt;
		}

		if (useBigMem) 
			cmd = cmd + " " + qbigmem + " -o " + log + " " + script.getAbsolutePath();
		else
			cmd = cmd + " -o " + log + " " + script.getAbsolutePath();

		if ((qcmd == null || qcmd.trim().isEmpty()) && (qname == null || qname.trim().isEmpty()))
			cmd = "cd " + scriptDir+ "\n" + script.getAbsolutePath() + " > " + log ;

		cmd = cmd + "\ncd - \n";
		if (Constants.DEBUG) {
			System.out.println("Command = " + cmd);
			msg.info("Command = " + cmd);
		}
		
		ProcessBuilder pb = new ProcessBuilder("cmd", "/C", cmd);
		
		String osName = System.getProperty("os.name" );
		
        if(osName.equals("Linux"))
        	pb = new ProcessBuilder("csh", "-c", cmd);

		// set up the working directory.
		pb.directory(script.getParentFile());

		// merge child's error and normal output streams.
		pb.redirectErrorStream(true);

		Process p = null;

		if (Constants.DEBUG) {
			System.out.println("Starting a new process to run the script file...");
			msg.info("Starting a new process to run the script file...");
		}

		try {
			p = pb.start();
			final InputStream es = p.getErrorStream();
			final InputStream is = p.getInputStream();

			// spawn two threads to handle I/O with child while we wait for it
			// to complete.
			Thread esthread = new Thread(new Runnable() {
				public void run() {
					readMsg(msg, es, "ERROR");
				}
			});
			esthread.start();
			
			Thread isthread = new Thread(new Runnable() {
				public void run() {
					readMsg(msg, is, "INPUT");
				}
			});
			isthread.start();


			if (Constants.DEBUG) {
				System.out.println("Process started: " + p.toString());
				msg.info("Process started: " + p.toString());
			}

			p.waitFor();
			es.close();
			is.close();
			
			if (Constants.DEBUG) {
				System.out.println("Job submitted.");
				msg.info("Job submitted.");
			}
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			//
		} finally {
			if (p != null) {
				try {
					p.getErrorStream().close();
				} catch (IOException e) {
					//
				}
			}
		}
	}
	
	private static void readMsg(MessageCenter msgcenter, InputStream is, String type) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        try {
            String message = reader.readLine();
            
            if (type.equals("ERROR") && message != null)
            	msgcenter.warn("Running Script:", new Exception(message));
            
            if (type.equals("INPUT"))
            	msgcenter.info("Start running script.", (message != null ? message : ""));
        } catch (Exception e) {
        	msgcenter.warn("Error reading message:", e);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            	msgcenter.warn("Error closing reading message:", e);
            }
        }

	}
}

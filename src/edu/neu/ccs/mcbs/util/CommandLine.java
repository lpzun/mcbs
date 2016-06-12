package edu.neu.ccs.mcbs.util;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class CommandLine {
	@Option(name = "-f", aliases = {
	        "--input-file" }, usage = "input thread transition system")
	private String fileTTS;

	@Option(name = "-i", aliases = {
	        "--initial" }, usage = "an initial thread state (e.g., 0|0)")
	private String initlTS;

	@Option(name = "-a", aliases = {
	        "--target" }, usage = "an target thread state (e.g., 0|0)")
	private String finalTS;

	@Option(name = "-m", aliases = {
	        "--mode" }, usage = "an explore mode:\n \"S\" = sequential BS\n "
	                + "\"C\" = concurrent BS\n " + "\"D\" = cloud-based BS")
	private String mode;

	@Option(name = "-l", aliases = {
	        "--adj-list" }, usage = "show adjacency list")
	private boolean printAdjList;

	@Option(name = "-all", aliases = {
	        "--all" }, usage = "show all of above message")
	private boolean printAll;

	@Option(name = "-v", aliases = {
	        "--version" }, usage = "show version information and exit")
	private boolean printVersion;

	@Option(name = "-h", aliases = { "--help" }, usage = "show help message")
	private boolean printHelp;

	@Argument
	private List<String> arguments = new ArrayList<>();

	/**
	 * 
	 * @param args
	 */
	public CommandLine(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			if (!arguments.isEmpty())
				throw new CmdLineException(parser, "No argument is given",
				        new Throwable());

			if (this.printHelp)
				parser.printUsage(System.out);
		} catch (CmdLineException e) {
			e.printStackTrace();
			parser.printUsage(System.out);
		}
	}

	/**
	 * @return the fileTTS
	 */
	public String getFileTTS() {
		return fileTTS;
	}

	/**
	 * @return the initlTS
	 */
	public String getInitlTS() {
		return initlTS;
	}

	/**
	 * @return the finalTS
	 */
	public String getFinalTS() {
		return finalTS;
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @return the printAdjList
	 */
	public boolean isPrintAdjList() {
		return printAdjList;
	}

	/**
	 * @return the printAll
	 */
	public boolean isPrintAll() {
		return printAll;
	}

	/**
	 * @return the printVersion
	 */
	public boolean isPrintVersion() {
		return printVersion;
	}

	/**
	 * @return the printHelp
	 */
	public boolean isPrintHelp() {
		return printHelp;
	}
}

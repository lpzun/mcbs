package edu.neu.ccs.mcbs;

import edu.neu.ccs.mcbs.util.TTS;

public class Main {

	/**
	 * The entry of the program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// System.out.println("MCBS");
			//
			// ThreadState ts = new ThreadState(1, 2);
			// System.out.println(ts);
			//
			// GlobalState gs = new GlobalState(ts);
			//
			// System.out.println(gs);
			//
			// Transition t = new Transition(0, 1, Transition.Type.FORK);
			// System.out.println(t);
			//
			// switch (t.getType()) {
			// case NORM:
			// System.out.println(t);
			// break;
			// default:
			// System.out.println("Not a normal transition!");
			// break;
			// }
			//
			String filename = "./examples/toy-1.tts";
			String initlTS = "0|0,1";
			String finalTS = "0|0,12,12";

			TTS parser = new TTS(filename, initlTS, finalTS);
			parser.parseState(finalTS, '|');

			// CommandLine cmdline = new CommandLine(args);
			//
			// String filename = cmdline.getFileTTS();
			// String initlTS = cmdline.getInitlTS();
			// String finalTS = cmdline.getFinalTS();
			// if (cmdline.isPrintVersion())
			// System.out.println("version");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package edu.neu.ccs.mcbs.imp;

import edu.neu.ccs.mcbs.util.TTS;

/**
 * Sequential Backward Search
 * 
 * @author Peizun
 * @date Jun 9, 2016
 */
public class SBS {

	private TTS tts;

	/**
	 * Constructor for sequential Backward search
	 * @param filename
	 * @param initlS
	 * @param finalS
	 */
	public SBS(String filename, String initlS, String finalS) {
		tts = new TTS(filename, initlS, finalS);
	}

}

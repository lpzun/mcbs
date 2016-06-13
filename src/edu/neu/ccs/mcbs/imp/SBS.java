package edu.neu.ccs.mcbs.imp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import edu.neu.ccs.mcbs.util.TTS;
import edu.neu.ccs.mcbs.util.ThreadState;
import edu.neu.ccs.mcbs.util.Transition;
import edu.neu.ccs.mcbs.util.Utilities;
import edu.neu.ccs.mcbs.util.GlobalState;

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
	 * 
	 * @param filename
	 * @param initlS
	 * @param finalS
	 */
	public SBS(String filename, String initlS, String finalS) {
		tts = new TTS(filename, initlS, finalS);
	}

	/**
	 * The sequential Backward Search
	 * 
	 * @return boolean true : if final state is coverable false: otherwise
	 */
	public boolean sequential_BS() {
		ThreadState initlTS = new ThreadState(0, 0);
		/// define worklist
		Queue<GlobalState> worklist = new LinkedList<>();
		worklist.add(tts.getFinalState());

		/// define expanded list and initialize it
		ArrayList<List<GlobalState>> expanded = new ArrayList<>(ThreadState.S);
		for (int i = 0; i < ThreadState.S; ++i) {
			expanded.add(i, new LinkedList<>());
		}

		while (!worklist.isEmpty()) {
			GlobalState _tau = worklist.poll();
			System.out.println(_tau); // TODO delete--------------

			Integer s = _tau.getShareState();
			// step 1: if \exists t \in expanded such that
			// t <= _tau, then discard _tau
			if (!Utilities.minimal(_tau, expanded.get(s)))
				continue;

			// step 2: compute all cover preimages and handle
			// them one by one
			List<GlobalState> images = this.step(_tau);
			for (GlobalState tau : images) {
				System.out.println("  " + tau);// TODO delete--------------
				if (Utilities.coverable(initlTS, tau))
					return true;
				// if tau is in upward(T_init), return true
				worklist.add(tau);
			}
			// step 3: insert _tau into the expanded states
			// (1) minimize the set of expanded states
			// (2) append tau to the set of expanded states
			expanded.set(s, Utilities.minimize(_tau, expanded.get(s)));
		}
		return false;
	}

	/**
	 * Compute _tau's cover preimages
	 * 
	 * @param _tau
	 * @return cover preimages
	 */
	private List<GlobalState> step(GlobalState _tau) {
		List<GlobalState> images = new ArrayList<>();
		ArrayList<Integer> activeLR = tts.getActiveLR()[_tau.getShareState()];
		if (activeLR != null) {
			for (Integer r : activeLR) {
				Transition tran = tts.getActiveR().get(r);
				ThreadState prev = tts.getActiveTS().get(tran.getSrc());
				ThreadState curr = tts.getActiveTS().get(tran.getDst());
				Map<Integer, Short> _Z = _tau.getLocalParts();
				switch (tran.getType()) {
				case BRCT: {
					// do something
				}
					break;
				case FORK: {
					if (_Z.containsKey(prev.getLocalState())) {
						Map<Integer, Short> Z = Utilities
						        .decrement(curr.getLocalState(), _Z);
						// GlobalState tau = new
						// GlobalState(prev.getShareState(),
						// Z);
						images.add(new GlobalState(prev.getShareState(), Z));
					}
				}
					break;
				default: {
					Map<Integer, Short> Z = Utilities.updateCounter(
					        prev.getLocalState(), curr.getLocalState(), _Z);
					// GlobalState tau = new GlobalState(prev.getShareState(),
					// Z);
					images.add(new GlobalState(prev.getShareState(), Z));
				}
					break;
				}
			}
		}
		return images;
	}
}

package edu.neu.ccs.mcbs.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import edu.neu.ccs.mcbs.util.GlobalState;
import edu.neu.ccs.mcbs.util.TTS;
import edu.neu.ccs.mcbs.util.ThreadState;
import edu.neu.ccs.mcbs.util.Transition;
import edu.neu.ccs.mcbs.util.Utilities;

/**
 * Concurrent Backward Search.
 * 
 * This file defines the multithreading backward search.
 * 
 * @author Peizun Liu
 * @date Jun 9, 2016
 * @version 1.0
 * @see SBS, MBS, DBS
 */
public class CBS {
	/// Thread Transition System
	private TTS tts;

	/// define worklist: a list of BlockingQueue
	private BlockingQueue<GlobalState> worklist;
	/// define expanded: a list of lists
	private ArrayList<List<GlobalState>> expanded;
	
	private boolean COVERABLE = false;
	private Integer nTHREADS;

	/**
	 * Constructor for multi-threading backward search
	 * 
	 * @param filename
	 * @param initlS
	 * @param finalS
	 */
	public CBS(String filename, String initlS, String finalS) {
		tts = new TTS(filename, initlS, finalS);

		nTHREADS = Runtime.getRuntime().availableProcessors();
		System.out.println("The size of thread pool " + nTHREADS);

		/// define and initialize worklist
		worklist = new LinkedBlockingQueue<>();

		/// define and initialize expanded
		expanded = new ArrayList<>(ThreadState.S);
		for (int s = 0; s < ThreadState.S; ++s) {
			expanded.add(s, new CopyOnWriteArrayList<>());
		}
	}

	/**
	 * The Concurrent BackWard Search
	 * 
	 * @return boolean
	 *         <p>
	 *         true: if final state is coverable; false: otherwise
	 */
	public boolean concurrentBWS() {
		worklist.add(tts.getFinalState());

		/// step 1: setting the multi-threading backward search...
		/// (1) define the thread pool
		/// (2) define the completion service
		/// (3) define the list of futures
		final ExecutorService pool = Executors.newFixedThreadPool(nTHREADS);
		final CompletionService<Boolean> excs = new ExecutorCompletionService<>(
		        pool);
		final List<Future<Boolean>> futures = new ArrayList<>(nTHREADS);
		try {
			/// step 2: declare and submit all tasks, and meanwhile, add it to
			/// future list to watch it.
			IntStream.range(0, nTHREADS).forEach(s -> {
				System.out.println("Start Task: " + s);
				futures.add(excs.submit(() -> this.coverBWS(s)));
			});

			// System.out.println("------------1-----------");
			/// step 3: watching all tasks, if any of them completes its task.
			/// The program will proceed based on the execution result. If the
			/// thread returns coverable, then it will
			/// (1) set coverable flag as true, and
			/// (2) cancel all other tasks.
			for (int s = 0; s < futures.size(); ++s) {
				Future<Boolean> future = excs.take();
				if (future.get()) {
					this.setCOVERABLE(true);
					break;
				}
			} // the end of iterating over futures
		} catch (InterruptedException e) {
			System.err.println("Cancel other tasks");
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			for (final Future<Boolean> future : futures) {
				if (!future.isDone())
					future.cancel(true);
			}
		}
		System.out.println("-------------task finished----------");
		/// step 4: shut down all tasks
		try {
			pool.shutdown();
			pool.awaitTermination(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			System.err.println("Tasks interrupted");
		} finally {
			if (!pool.isTerminated())
				pool.shutdownNow();
			System.out.println("Shutdown finished");
		}
		if (this.isCOVERABLE())
			return true;
		return false;
	}

	/**
	 * The function body of coverable backward search
	 * 
	 * @param threadID
	 * @return
	 * @throws InterruptedException
	 */
	private boolean coverBWS(Integer threadID) throws InterruptedException {
		while (!worklist.isEmpty()) {
			// GlobalState _tau = worklist.take();
			GlobalState _tau = worklist.poll(100, TimeUnit.MILLISECONDS);
			if (_tau == null)
				break;

			// System.out.println("Thread " + threadID + ".....");
			// System.out.println(_tau);

			/// step 1: if \exists t \in T_init such that
			/// _tau <= t, then discard _tau
			if (Utilities.coverable(tts.getInitlState(), _tau))
				return true;

			Integer s = _tau.getShareState();
			/// step 1: if \exists t \in expanded such that
			/// t <= _tau, then discard _tau
			if (!Utilities.minimal(_tau, expanded.get(s))) {
				// System.out.println("nonminimal.........");
				continue;
			}

			/// step 2: compute all cover preimages and put them
			/// into their corresponding blocking queues
			this.step(_tau);
			// System.out.println("after step.........");
			/// step 3: insert _tau into the expanded states
			/// (1) minimize the set of expanded states
			/// (2) append tau to the set of expanded states
			expanded.set(s, Utilities.minimize(_tau, expanded.get(s)));
		}
		return false;
	}

	/**
	 * Compute cover preimages
	 * 
	 * @param _tau
	 * @param threadID
	 * @throws InterruptedException
	 */
	private void step(GlobalState _tau) throws InterruptedException {
		ArrayList<Integer> activeLR = tts.getActiveLR()[_tau.getShareState()];
		if (activeLR != null) {
			for (Integer r : activeLR) {
				final Transition tran = tts.getActiveR().get(r);
				final ThreadState prev = tts.getActiveTS().get(tran.getSrc());
				final ThreadState curr = tts.getActiveTS().get(tran.getDst());
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
						worklist.put(new GlobalState(prev.getShareState(), Z));
					}
				}
					break;
				default: {
					Map<Integer, Short> Z = Utilities.updateCounter(
					        prev.getLocalState(), curr.getLocalState(), _Z);
					worklist.put(new GlobalState(prev.getShareState(), Z));
				}
					break;
				}
			}
		}
	}

	/**
	 * @return the cOVERABLE
	 */
	public synchronized boolean isCOVERABLE() {
		return COVERABLE;
	}

	/**
	 * @param cOVERABLE
	 *            the cOVERABLE to set
	 */
	public synchronized void setCOVERABLE(boolean cOVERABLE) {
		COVERABLE = cOVERABLE;
	}

	/**
	 * @return the tts
	 */
	public TTS getTTS() {
		return tts;
	}

	/**
	 * @param tts
	 *            the tts to set
	 */
	public void setTTS(TTS tts) {
		this.tts = tts;
	}
}

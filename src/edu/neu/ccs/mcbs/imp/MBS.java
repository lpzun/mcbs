package edu.neu.ccs.mcbs.imp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.List;
import java.util.Map;

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
 * @see SBS, CBS
 */
public class MBS {
	/// Thread Transition System
	private TTS tts;

	/// define worklist: a list of BlockingQueue
	private ArrayList<BlockingQueue<GlobalState>> worklist;
	/// define expanded: a list of lists
	private ArrayList<List<GlobalState>> expanded;

	// private AtomicBoolean fRUNNING;
	private AtomicBoolean fTERMINATE;
	private ArrayList<AtomicBoolean> fRUNNING;

	private boolean COVERABLE = false;

	private Integer nTHREADS;

	/**
	 * Constructor for multi-threading backward search
	 * 
	 * @param filename
	 * @param initlS
	 * @param finalS
	 */
	public MBS(String filename, String initlS, String finalS) {
		tts = new TTS(filename, initlS, finalS);

		nTHREADS = ThreadState.S;
		System.out.println(nTHREADS);

		/// define and initialize worklist
		worklist = new ArrayList<>(nTHREADS);
		for (int i = 0; i < nTHREADS; ++i) {
			worklist.add(i, new LinkedBlockingQueue<>());
		}

		// fRUNNING = new AtomicBoolean(true);
		fTERMINATE = new AtomicBoolean(false);
		fRUNNING = new ArrayList<>(nTHREADS);
		for (int i = 0; i < nTHREADS; ++i) {
			fRUNNING.add(new AtomicBoolean(false));
		}

		/// define and initialize expanded
		expanded = new ArrayList<>(ThreadState.S);
		for (int s = 0; s < ThreadState.S; ++s) {
			expanded.add(s, new LinkedList<>());
		}
	}

	/**
	 * The concurrent BackWard Search
	 * 
	 * @return boolean
	 *         <p>
	 *         true : if final state is coverable; false: otherwise
	 */
	public boolean concurrentBWS() {
		worklist.get(tts.getFinalState().getShareState())
		        .add(tts.getFinalState());

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
			IntStream.range(1, nTHREADS).forEach(s -> {
				System.out.println("Start Task: " + s);
				futures.add(excs.submit(() -> this.coverBWS(s)));
			});
			futures.add(excs.submit(() -> this.initCoverBWS(0)));
			// System.out.println(futures.size() + "===================");

			this.monitor();

			System.out.println("---------1--------------");
			/// step 3: watching all tasks, if any of them completes its task.
			/// The program will proceed based on the execution result. If the
			/// thread returns coverable, then it will
			/// (1) set coverable flag as true, and
			/// (2) cancel all other tasks.
			for (int s = 0; s < futures.size(); ++s) {
				Future<Boolean> future = futures.get(s);
				if (future.get()) {
					this.setCOVERABLE(true);
					break;
				}
			}

		} catch (InterruptedException e) {
			System.out.println("Cancel all other tasks");
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			for (final Future<Boolean> future : futures) {
				if (!future.isDone())
					future.cancel(true);
			}
		}
		System.out.println("---------task finished--------------");
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
		if (isCOVERABLE())
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
		while (!fTERMINATE.get()) {
			GlobalState _tau = worklist.get(threadID).poll(100,
			        TimeUnit.MILLISECONDS);
			if (_tau == null)
				continue;

			fRUNNING.get(threadID).compareAndSet(false, true);
			Integer s = _tau.getShareState();
			/// step 1: if \exists t \in expanded such that
			/// t <= _tau, then discard _tau
			if (!Utilities.minimal(_tau, expanded.get(s))) {
				fRUNNING.get(threadID).compareAndSet(true, false);
				continue;
			}

			/// step 2: compute all cover preimages and put them
			/// into their corresponding blocking queues
			this.step(_tau, threadID);

			/// step 3: insert _tau into the expanded states
			/// (1) minimize the set of expanded states
			/// (2) append tau to the set of expanded states
			expanded.set(s, Utilities.minimize(_tau, expanded.get(s)));
			fRUNNING.get(threadID).compareAndSet(true, false);
		}
		System.out.println(threadID + " shutdown....");
		return false;
	}

	/**
	 * The function body of coverable backward search
	 * 
	 * @param threadID
	 * @return
	 * @throws InterruptedException
	 */
	private boolean initCoverBWS(Integer threadID) throws InterruptedException {
		while (!fTERMINATE.get()) {
			GlobalState _tau = worklist.get(threadID).poll(100,
			        TimeUnit.MILLISECONDS);
			if (_tau == null)
				continue;

			fRUNNING.get(threadID).compareAndSet(false, true);
			System.out.println("Thread " + threadID + ".....");
			System.out.println(_tau);
			/// step 1: if \exists t \in T_init such that
			/// _tau <= t, then discard _tau
			if (Utilities.coverable(tts.getInitlState(), _tau)) {
				fTERMINATE.compareAndSet(false, true);
				return true;
			}

			Integer s = _tau.getShareState();
			/// step 1: if \exists t \in expanded such that
			/// t <= _tau, then discard _tau
			if (!Utilities.minimal(_tau, expanded.get(s))) {
				fRUNNING.get(threadID).compareAndSet(true, false);
				continue;
			}

			/// step 2: compute all cover preimages and put them
			/// into their corresponding blocking queues
			this.step(_tau, threadID);

			/// step 3: insert _tau into the expanded states
			/// (1) minimize the set of expanded states
			/// (2) append tau to the set of expanded states
			expanded.set(s, Utilities.minimize(_tau, expanded.get(s)));
			fRUNNING.get(threadID).compareAndSet(true, false);
		}
		System.out.println(threadID + " shutdown....");
		return false;
	}

	/**
	 * Compute cover preimages
	 * 
	 * @param _tau
	 * @param s
	 * @throws InterruptedException
	 */
	private void step(GlobalState _tau, Integer s) throws InterruptedException {
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
						worklist.get(prev.getShareState())
						        .put(new GlobalState(prev.getShareState(), Z));
					}
				}
					break;
				default: {
					Map<Integer, Short> Z = Utilities.updateCounter(
					        prev.getLocalState(), curr.getLocalState(), _Z);
					worklist.get(prev.getShareState())
					        .put(new GlobalState(prev.getShareState(), Z));
				}
					break;
				}
			}
		}
	}

	private void monitor() throws InterruptedException {
		while (true) {
			boolean running = false;
			for (int i = 0; i < fRUNNING.size(); ++i) {
				if (fRUNNING.get(i).get()) {
					running = true;
					break;
				}
			}
			if (running) {
				TimeUnit.MILLISECONDS.sleep(100);
			} else {
				fTERMINATE.compareAndSet(false, true);
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

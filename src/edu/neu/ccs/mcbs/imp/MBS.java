package edu.neu.ccs.mcbs.imp;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
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

	private AtomicBoolean fRUNNING;
	// private ArrayList<AtomicBoolean> fRUNNING;

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
			worklist.add(i, new ArrayBlockingQueue<>(Short.MAX_VALUE));
		}

		fRUNNING = new AtomicBoolean(true);
		// fRUNNING = new ArrayList<>(nTHREADS);
		// for (int i = 0; i < nTHREADS; ++i) {
		// fRUNNING.add(new AtomicBoolean(true));
		// }

		/// define and initialize expanded
		expanded = new ArrayList<>(ThreadState.S);
		for (int s = 0; s < ThreadState.S; ++s) {
			expanded.add(s, new CopyOnWriteArrayList<>());
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
			IntStream.range(0, nTHREADS).forEach(s -> {
				System.out.println("Start Task: " + s);
				futures.add(excs.submit(() -> this.coverBWS(s)));
			});
			// System.out.println(futures.size() + "===================");

			System.out.println("---------1--------------");
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
			}
		} catch (InterruptedException e) {
			// e.printStackTrace();
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
		// System.out.println("---------2--------------");
		while (!worklist.get(threadID).isEmpty() || fRUNNING.get()) {
			// System.out.println("-----------4--------------");
			GlobalState _tau = worklist.get(threadID).take();
			// GlobalState _tau = worklist.get(threadID).poll(100,
			// TimeUnit.MILLISECONDS);
			// if (_tau == null)
			// break;

			System.out.println("Thread " + threadID + ".....");
			System.out.println(_tau);
			/// step 1: if \exists t \in T_init such that
			/// _tau <= t, then discard _tau
			if (Utilities.coverable(tts.getInitlState(), _tau))
				return true;

			Integer s = _tau.getShareState();
			/// step 1: if \exists t \in expanded such that
			/// t <= _tau, then discard _tau
			if (!Utilities.minimal(_tau, expanded.get(s))) {
				// fRUNNING.get(threadID).compareAndSet(true, false);
				fRUNNING.compareAndSet(true, false);
				System.out.println("I am here..." + _tau);
				continue;
			}

			/// step 2: compute all cover preimages and put them
			/// into their corresponding blocking queues
			this.step(_tau, threadID);

			/// step 3: insert _tau into the expanded states
			/// (1) minimize the set of expanded states
			/// (2) append tau to the set of expanded states
			expanded.set(s, Utilities.minimize(_tau, expanded.get(s)));

			// fRUNNING.get(threadID).compareAndSet(true, false);
		}
		fRUNNING.compareAndSet(true, false);
		System.out.println("Thread " + threadID + ": -------finished");
		return false;
	}

	/**
	 * Compute cover preimages
	 * 
	 * @param _tau
	 * @param threadID
	 * @throws InterruptedException
	 */
	private void step(GlobalState _tau, Integer threadID)
	        throws InterruptedException {
		ArrayList<Integer> activeLR = tts.getActiveLR()[_tau.getShareState()];
		if (activeLR != null) {
			// fRUNNING.get(threadID).compareAndSet(false, true); // set running
			for (Integer r : activeLR) {
				final Transition tran = tts.getActiveR().get(r);
				final ThreadState prev = tts.getActiveTS().get(tran.getSrc());
				final ThreadState curr = tts.getActiveTS().get(tran.getDst());
				Map<Integer, Short> _Z = _tau.getLocalParts();

				// fRUNNING.get(prev.getShareState()).compareAndSet(false,
				// true);
				fRUNNING.compareAndSet(false, true);

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
			// fRUNNING.get(threadID).compareAndSet(true, false); // reset
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

	///////////////////////////////////////////////////////
	/// concurrent testing
	///////////////////////////////////////////////////////
	public void test() {
		// Runnable
		// ExecutorService executor = Executors.newSingleThreadExecutor();
		// executor.submit(() -> {
		// String threadName = Thread.currentThread().getName();
		// System.out.println("Hello " + threadName);
		// });
		//
		// try {
		// System.out.println("Attempt to shutdown executor");
		// executor.shutdown();
		// executor.awaitTermination(5, TimeUnit.SECONDS);
		// } catch (InterruptedException e) {
		// System.err.println("tasks interrupted");
		// } finally {
		// if (!executor.isTerminated()) {
		// System.err.println("cancel non-finished tasks");
		// }
		// executor.shutdownNow();
		// System.out.println("shutdown finished");
		// }

		// Callables and futures

		// Callable<Integer> task = () -> {
		// try {
		// TimeUnit.SECONDS.sleep(1);
		// return 123;
		// } catch (InterruptedException e) {
		// throw new IllegalStateException("task interrupted", e);
		// }
		// };
		//
		// try {
		// // ExecutorService executor = Executors.newFixedThreadPool(1);
		// // Future<Integer> future = executor.submit(task);
		// // System.out.println("future done? " + future.isDone());
		// // Integer result = future.get();
		// //
		// // System.out.println("future done? " + future.isDone());
		// // System.out.println("result: " + result);
		//
		// ExecutorService executor = Executors.newFixedThreadPool(1);
		// Future<Integer> future = executor.submit(() -> {
		// try {
		// TimeUnit.SECONDS.sleep(2);
		// return 123;
		// } catch (InterruptedException e) {
		// throw new IllegalStateException("", e);
		// }
		// });
		// future.get(3, TimeUnit.SECONDS);
		// } catch (InterruptedException | ExecutionException e) {
		// e.printStackTrace();
		// } catch (TimeoutException e) {
		// e.printStackTrace();
		// }

		// ExecutorService executor = Executors.newWorkStealingPool();
		// List<Callable<String>> callables = Arrays.asList(() -> "task1",
		// () -> "task2", () -> "task3");
		//
		// try {
		// executor.invokeAll(callables).stream().map(future -> {
		// try {
		// return future.get();
		// } catch (Exception e) {
		// throw new IllegalStateException(e);
		// }
		// }).forEach(System.out::println);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// ExecutorService executor = Executors.newWorkStealingPool();
		// List<Callable<Boolean>> callables = Arrays.asList(callable("task1",
		// 10),
		// callable("task2", 50), callable("task3", 100));
		// try {
		// executor.invokeAll(callables).stream().map(future -> {
		// try {
		// return future.get();
		// } catch (Exception e) {
		// throw new IllegalStateException(e);
		// }
		// }).forEach(System.out::println);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		ArrayList<Integer> arrayList = new ArrayList<>(30);
		System.out.println("size: " + arrayList.size());

		int nPROC = Runtime.getRuntime().availableProcessors();

		System.out.println("the number of processors: " + nPROC);

		final ExecutorService pool = Executors.newFixedThreadPool(4);

		final CompletionService<Boolean> ecs = new ExecutorCompletionService<>(
		        pool);
		List<Future<Boolean>> futures = new ArrayList<>(4);
		try {
			for (int i = 1; i < 5; ++i) {
				futures.add(ecs.submit(task("task" + i, i)));
			}

			for (int i = 1; i < 5; ++i) {
				final Future<Boolean> future = ecs.take();
				final Boolean isCoverable = future.get();
				if (isCoverable) {
					this.setCOVERABLE(true);
					break;
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Cancel all other tasks");
		} catch (ExecutionException e) {

		} finally {
			for (Future<Boolean> future : futures)
				if (!future.isDone())
					future.cancel(true);
			// System.out.println("cancel non-finished tasks");
		}

		try {
			pool.shutdown();
			pool.awaitTermination(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			System.out.println("Tasks interrupted");
		} finally {
			if (!pool.isTerminated()) {
				pool.shutdownNow();
			}
			System.out.println("shutdown finished!");
		}
	}

	Callable<Boolean> callable(String result, long sleepSeconds) {
		return () -> {
			int i = 0;
			while (!isCOVERABLE() && i < sleepSeconds) {
				System.out.println(result + " " + i);
				if (i == 25) {
					setCOVERABLE(true);
					return true;
				}
				++i;
			}
			return false;
		};
	}

	Callable<Boolean> task(String result, long sleepSeconds) {
		return () -> {
			TimeUnit.SECONDS.sleep(sleepSeconds);
			System.out.println(result);
			if (result.equals("task3"))
				return true;
			return false;
		};
	}

}

package edu.neu.ccs.mcbs.util;

public class ThreadState {

	/**
	 * The number of shared states
	 */
	public static Integer S = 0;

	/**
	 * The number of local states
	 */
	public static Integer L = 0;

	/**
	 * Shared state: defined as Integer
	 */
	private Integer shareState;

	/**
	 * local state: defined as Integer
	 */
	private Integer localState;

	/**
	 * Default constructor
	 */
	public ThreadState() {
		this.shareState = 0;
		this.localState = 0;
	}

	/**
	 * Constructor with shared state and local state
	 * 
	 * @param shareState
	 * @param localState
	 */
	public ThreadState(Integer shareState, Integer localState) {
		this.shareState = shareState;
		this.localState = localState;
	}

	/**
	 * Constructor with shared state and local state
	 * 
	 * @param shareState
	 * @param localState
	 */
	public ThreadState(String s) {
		String[] state = s.split("\\s+");
		this.shareState = Integer.parseInt(state[0]);
		this.localState = Integer.parseInt(state[1]);
	}

	/**
	 * Constructor with shared state and local state
	 * 
	 * @param shareState
	 * @param localState
	 */
	public ThreadState(String shareState, String localState) {
		this.shareState = Integer.parseInt(shareState);
		this.localState = Integer.parseInt(localState);
	}

	/**
	 * Copy constructor
	 * 
	 * @param ts
	 */
	public ThreadState(ThreadState ts) {
		this(ts.getShareState(), ts.getLocalState());
	}

	/**
	 * The getter for shared state
	 * 
	 * @return shared state
	 */
	public Integer getShareState() {
		return shareState;
	}

	/**
	 * The getter for local state
	 * 
	 * @return local state
	 */
	public Integer getLocalState() {
		return localState;
	}

	@Override
	public String toString() {
		return "(" + shareState + "," + localState + ")";
	}

	/**
	 * overriding equals
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((localState == null) ? 0 : localState.hashCode());
		result = prime * result
		        + ((shareState == null) ? 0 : shareState.hashCode());
		return result;
	}

	/**
	 * overriding equals
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		ThreadState other = (ThreadState) obj;
		if (localState != null && other.localState != null && shareState != null
		        && other.shareState != null)
			return localState == other.getLocalState()
			        && shareState == other.getShareState();
		return false;
	}
}

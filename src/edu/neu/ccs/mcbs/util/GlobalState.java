package edu.neu.ccs.mcbs.util;

import java.util.SortedMap;
import java.util.TreeMap;

public class GlobalState {

	/**
	 * shared state
	 */
	private Integer shareState;

	/**
	 * Local part: storing in a sorted map
	 */
	private SortedMap<Integer, Short> localParts;

	/**
	 * Default constructor
	 */
	public GlobalState() {
		this.shareState = 0;
		this.localParts = new TreeMap<Integer, Short>();
	}

	/**
	 * Constructor with a thread state
	 * 
	 * @param ts
	 */
	public GlobalState(ThreadState ts) {
		this.shareState = ts.getShareState();
		this.localParts = new TreeMap<Integer, Short>();
		this.localParts.put(ts.getLocalState(), (short) 1);
	}

	/**
	 * 
	 * @param shareState
	 * @param localPart
	 */
	public GlobalState(Integer shareState,
	        SortedMap<Integer, Short> localPart) {
		this.shareState = shareState;
		this.localParts = localPart;
	}

	/**
	 * Copy constructor
	 * 
	 * @param gs
	 */
	public GlobalState(GlobalState gs) {
		this.shareState = gs.getShareState();
		this.localParts = gs.getLocalParts();
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
	 * The getter for local parts
	 * 
	 * @return local parts
	 */
	public SortedMap<Integer, Short> getLocalParts() {
		return localParts;
	}

	@Override
	public String toString() {
		return "<" + shareState + "|" + localParts + ">";
	}

}

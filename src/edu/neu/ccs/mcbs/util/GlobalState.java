package edu.neu.ccs.mcbs.util;

import java.util.HashMap;
import java.util.Map;

public class GlobalState {

    /**
     * shared state
     */
    private final Integer shareState;

    /**
     * Local part: storing in a sorted map
     */
    private final Map<Integer, Short> localParts;

    /**
     * Default constructor
     */
    public GlobalState() {
        this.shareState = 0;
        this.localParts = new HashMap<>();
    }

    /**
     * Constructor with a thread state
     *
     * @param ts
     */
    public GlobalState(ThreadState ts) {
        this.shareState = ts.getShareState();
        this.localParts = new HashMap<>();
        this.localParts.put(ts.getLocalState(), (short) 1);
    }

    /**
     *
     * @param shareState
     * @param localPart
     */
    public GlobalState(Integer shareState, Map<Integer, Short> localPart) {
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
     * Getter for shared state
     *
     * @return shared state
     */
    public Integer getShareState() {
        return shareState;
    }

    /**
     * Getter for local parts
     *
     * @return local parts
     */
    public Map<Integer, Short> getLocalParts() {
        return localParts;
    }

    @Override
    public String toString() {
        return "(" + shareState + "|" + localParts + ")";
    }
}

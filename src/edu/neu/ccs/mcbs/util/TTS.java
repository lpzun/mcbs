package edu.neu.ccs.mcbs.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TTS {

    private ThreadState initlState;

    private GlobalState finalState;

    // TTS in transition
    private List<Transition> activeR;
    // thread states
    private List<ThreadState> activeTS;
    // incoming edges for shared states, represented in ID
    private ArrayList<Integer>[] activeLR;

    /**
     * Constructor for parser
     *
     * @param filename
     * @param initlS
     * @param finalS
     */
    public TTS(String filename, String initlS, String finalS) {
        try {
            this.finalState = new GlobalState();
            this.finalState = new GlobalState();

            this.initlState = parseThreadState(initlS, '|');
            this.finalState = parseGlobalState(finalS, '|');

            System.out.println(this.initlState);
            System.out.println(this.finalState);

            this.activeR = new ArrayList<>();
            this.activeTS = new ArrayList<>();
            this.parseTTS(filename, "#");
        } catch (McbsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate the global state
     *
     * @param sstate
     * @param sep
     * @return A global state
     * @throws McbsException
     */
    public final ThreadState parseThreadState(String sstate, char sep)
            throws McbsException {
        System.out.println(sstate);
        if (sstate.indexOf(sep) == -1) {
            List<String> noCommTS = this.removeComment(sstate, "#");
            if (noCommTS.size() < 1) {
                throw new McbsException("Illegal thread state format!");
            }
            sstate = noCommTS.get(0);

        }

        String[] state = sstate.split("\\|");
        if (state.length != 2) {
            throw new McbsException("Illegal thread state format!");
        }

        return new ThreadState(Integer.parseInt(state[0]),
                Integer.parseInt(state[1]));
    }

    /**
     * Generate the global state
     *
     * @param sstate
     * @param sep
     * @return A global state
     * @throws McbsException
     */
    public final GlobalState parseGlobalState(String sstate, char sep)
            throws McbsException {
        System.out.println(sstate);
        if (sstate.indexOf(sep) == -1) {
            List<String> noCommTS = this.removeComment(sstate, "#");
            if (noCommTS.size() < 1) {
                throw new McbsException("Illegal global state format!");
            }
            sstate = noCommTS.get(0);

        }
        List<String> state = Stream.of(sstate).map(s -> s.split("(\\||\\,)"))
                .flatMap(Arrays::stream).collect(Collectors.toList());
        // handle shared part
        Integer shareState = Integer.parseInt(state.get(0));
        // handle local part
        Map<Integer, Short> localPart = new HashMap<>();
        for (int i = 1; i < state.size(); ++i) {
            Integer localState = Integer.parseInt(state.get(i));
            Short count = localPart.get(localState);
            if (count == null) {
                localPart.put(localState, (short) 1);
            } else {
                localPart.put(localState, (short) (count + 1));
            }
        }
        return new GlobalState(shareState, localPart);
    }

    /**
     * Remove all commented line from a TTS file
     *
     * @param filename TTS file name
     * @param comment define notation of comment, the default is "#"
     * @return processing the input TTS file, and extract infos
     * @throws McbsException
     */
    @SuppressWarnings("unchecked")
    private void parseTTS(String filename, String comment)
            throws McbsException {
        List<String> noCommTTS = removeComment(filename, comment);
        if (noCommTTS.isEmpty()) {
            throw new McbsException("TTS file is empty!");
        }

        // step 1: get the size of TTS
        List<String> ttsSize = noCommTTS.stream().limit(1)
                .map(line -> line.split("\\s+")).flatMap(Arrays::stream)
                .collect(Collectors.toList());
        if (ttsSize.size() != 2) {
            throw new McbsException("TTS file's size is incorrect!");
        }
        ThreadState.S = Integer.parseInt(ttsSize.get(0));
        ThreadState.L = Integer.parseInt(ttsSize.get(1));
        // removing the first element which stores the size of TTS
        noCommTTS.remove(0);

        List<String> forIndexTS = new ArrayList<>();
        // get all distinguish thread state
        noCommTTS.stream().map(line -> line.split("\\s(\\+|-|~)>\\s"))
                .flatMap(Arrays::stream).distinct().sorted().forEach(s -> {
            String[] ts = s.split("\\s+");
            if (ts.length < 2) {
                try {
                    throw new McbsException(
                            "Thread state format is incorrect");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            forIndexTS.add(ts[0] + ts[1]);
            this.activeTS.add(new ThreadState(ts[0], ts[1]));
        });

        activeLR = new ArrayList[ThreadState.S];
        for (String stran : noCommTTS) {
            String[] list = stran.split("\\s+");
            if (list.length < 5) {
                throw new McbsException("TTS's transition is incorrect!");
            }

            Integer src = forIndexTS.indexOf(list[0] + list[1]);
            Integer dst = forIndexTS.indexOf(list[3] + list[4]);

            // define transition r, and determine its transition type
            Transition r = new Transition();
            if (list[2].compareTo("->") == 0) {
                r = new Transition(src, dst, Transition.Type.NORM);
            } else if (list[2].compareTo("+>") == 0) {
                r = new Transition(src, dst, Transition.Type.FORK);
            } else if (list[2].compareTo("+>") == 0) {
                r = new Transition(src, dst, Transition.Type.BRCT);
            } else {
                throw new McbsException("TTS's transition is incorrect!");
            }

            Integer s2 = Integer.parseInt(list[3]);
            if (this.activeLR[s2] == null) {
                this.activeLR[s2] = new ArrayList<>();
            }
            this.activeLR[s2].add(this.activeR.size());
            this.activeR.add(r);
        }

        // System.out.println(ThreadState.S + " " + ThreadState.L);
        // System.out.println("After: ");
        // for (Transition r : this.activeR) {
        // this.printTransition(r);
        // }
        // System.out.println("Incoming edge: ");
        // for (int i = 0; i < this.activeLR.length; ++i) {
        // System.out.println("Shared State: " + i);
        // if (this.activeLR[i] != null)
        // for (Integer rid : this.activeLR[i]) {
        // System.out.print(" ");
        // printTransition(this.activeR.get(rid));
        // }
        // }
    }

    /**
     * Remove all commented line from a TTS file
     *
     * @param filename TTS file's name
     * @param comment the notation of comment, the default is \#
     * @return the input file after removing comments
     */
    private List<String> removeComment(String filename, String comment) {
        List<String> noCommentTTS = new ArrayList<>();
        try {
            noCommentTTS = Files.lines(Paths.get(filename))
                    .filter(line -> !line.startsWith(comment))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return noCommentTTS;
    }

    /**
     * Print the original transition
     *
     * @param r: transition
     */
    @SuppressWarnings("unused")
    private void printTransition(Transition r) {
        System.out.print(this.activeTS.get(r.getSrc()));
        switch (r.getType()) {
            case NORM:
                System.out.print(" -> ");
                break;
            case FORK:
                System.out.print(" +> ");
                break;
            default:
                System.out.print(" ~> ");
                break;
        }
        System.out.print(" " + r.getDst());
        System.out.println(this.activeTS.get(r.getDst()));
    }

    ///////////////////////////////////////////////////////
    /// Public Part
    ///////////////////////////////////////////////////////
    /**
     * Getter for initial state
     *
     * @return the initlState
     */
    public ThreadState getInitlState() {
        return initlState;
    }

    /**
     * Getter for final state
     *
     * @return the finalState
     */
    public GlobalState getFinalState() {
        return finalState;
    }

    /**
     * Getter for all transitions in TTS
     *
     * @return the activeR
     */
    public List<Transition> getActiveR() {
        return activeR;
    }

    /**
     * Getter for all thread states in TTS
     *
     * @return the activeTS
     */
    public List<ThreadState> getActiveTS() {
        return activeTS;
    }

    /**
     * Getter for incoming edges (ID), grouping by shared states
     *
     * @return the activeLR
     */
    public ArrayList<Integer>[] getActiveLR() {
        return activeLR;
    }

}

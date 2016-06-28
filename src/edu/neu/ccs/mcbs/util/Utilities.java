package edu.neu.ccs.mcbs.util;

import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is a class with only utilities. It contains only static methods. Thus,
 * we set its constructor as private.
 *
 * @author Peizun Liu
 * @date Jun 9, 2016
 */
public class Utilities {

    private Utilities() {
    }

    /**
     * U-pdate counter
     *
     * @param inc
     * @param dec
     * @param _Z
     * @return local state part
     */
    public static Map<Integer, Short> updateCounter(Integer inc, Integer dec,
            Map<Integer, Short> _Z) {
        // step 0: immediately return if inc == dec
        if (inc == dec) {
            return _Z;
        }

        Map<Integer, Short> Z = new HashMap<>(_Z);
        // step 1: update counter for the decremental
        Short ldec = Z.get(dec);
        if (ldec != null) {
            if (ldec == 1) {
                Z.remove(dec);
            } else {
                Z.put(dec, (short) (ldec - 1));
            }
        }

        // step 2: update counter for the incremental
        Short linc = Z.get(inc);
        if (linc != null) {
            Z.put(inc, (short) (linc + 1));
        } else {
            Z.put(inc, (short) 1);
        }
        return Z;
    }

    /**
     * Increment counter
     *
     * @param inc
     * @param Z
     * @return local state part
     */
    public static Map<Integer, Short> increment(Integer inc,
            Map<Integer, Short> Z) {
        // step 2: update counter for the incremental
        Short linc = Z.get(inc);
        if (linc != null) {
            Z.put(inc, (short) (linc + 1));
        } else {
            Z.put(inc, (short) 1);
        }
        return Z;
    }

    /**
     * Decrement counter
     *
     * @param dec
     * @param Z
     * @return local state part
     */
    public static Map<Integer, Short> decrement(Integer dec,
            Map<Integer, Short> Z) {
        // step 1: update counter for the decremental
        Short ldec = Z.get(dec);
        if (ldec != null) {
            if (ldec == 1) {
                Z.remove(dec);
            } else {
                Z.put(dec, (short) (ldec - 1));
            }
        }
        return Z;
    }

    /**
     * To determine whether tau reaches a initial state
     *
     * @param s an initial thread state
     * @param tau a global state
     * @return boolean true: tau covers a initial state; false, otherwise
     */
    public static boolean coverable(ThreadState s, GlobalState tau) {
        if (s.getShareState() == tau.getShareState()
                && tau.getLocalParts().size() == 1) {
            return tau.getLocalParts().containsKey(s.getLocalState());
        }
        return false;
    }

    /**
     * To determine whether global state tau1 covers global state tau2
     *
     * @param tau1
     * @param tau2
     * @return boolean true: tau1 covers tau2; false, otherwise
     */
    public static boolean covers(GlobalState tau1, GlobalState tau2) {
        if (tau1.getShareState() != tau2.getShareState()) {
            return false;
        }

        final Map<Integer, Short> Z1 = tau1.getLocalParts();
        final Map<Integer, Short> Z2 = tau2.getLocalParts();

        if (Z1.size() < Z2.size()) {
            return false;
        }

        for (final Integer p : Z2.keySet()) {
            if (Z1.get(p) == null || Z1.get(p) < Z2.get(p)) {
                return false;
            }
        }

        return true;
    }

    /**
     * To determine whether global state tau1 covers global state tau2
     *
     * @param tau1
     * @param tau2
     * @return boolean true: tau1 covers tau2; false, otherwise
     */
    public static boolean covers(Map<Integer, Short> Z1,
            Map<Integer, Short> Z2) {

        if (Z1.size() < Z2.size()) {
            return false;
        }

        for (final Integer p : Z2.keySet()) {
            if (Z1.get(p) == null || Z1.get(p) < Z2.get(p)) {
                return false;
            }
        }
        return true;
    }

    /**
     * To determine whether global state tau1 is covered by a global state tau2
     *
     * @param tau1
     * @param tau2
     * @return boolean true: tau1 is covered by tau2; false, otherwise
     */
    public static boolean coveredby(GlobalState tau1, GlobalState tau2) {
        return covers(tau2, tau1);
    }

    /**
     * To determine if tau is the minimal state in W
     *
     * @param tau
     * @param W a list of global states
     * @return bool true : false:
     */
    public static boolean minimal(GlobalState tau, List<GlobalState> W) {
        for (final GlobalState w : W) {
            if (covers(tau, w)) {
                return false;
            }
        }
        return true;
    }

    /**
     * To determine if tau is the minimal state in W
     *
     * @param z
     * @param W a list of global states
     * @return bool true : false:
     */
    public static boolean minimal(Map<Integer, Short> z,
            List<Map<Integer, Short>> W) {
        for (final Map<Integer, Short> w : W) {
            if (covers(z, w)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Minimize a worklist W, aka removing all _tau that covers tau
     *
     * @param tau
     * @param W
     * @return minimized W
     */
    public static List<GlobalState> minimize(GlobalState tau,
            List<GlobalState> W) {
        W.removeIf(w -> covers(w, tau));
        W.add(tau);
        return W;
    }

    /**
     * Minimize a worklist W, aka removing all _tau that covers tau
     *
     * @param tau
     * @param W
     * @return minimized W
     */
    public static List<GlobalState> cminimize(GlobalState tau,
            List<GlobalState> W) {
        List<GlobalState> _W = W.parallelStream().filter(w -> !covers(w, tau))
                .collect(Collectors.toList());
        _W.add(tau);
        return _W;
    }

    /**
     * Minimize a worklist W, aka removing all _tau that covers tau
     *
     * @param z
     * @param W
     * @return minimized W
     */
    public static List<Map<Integer, Short>> minimize(Map<Integer, Short> z,
            List<Map<Integer, Short>> W) {
        W.removeIf(w -> covers(w, z));
        W.add(z);
        return W;
    }

    /**
     * Minimize a worklist W, aka removing all _tau that covers tau
     *
     * @param z
     * @param W
     * @return minimized W
     */
    public static List<Map<Integer, Short>> cminimize(Map<Integer, Short> z,
            List<Map<Integer, Short>> W) {
        List<Map<Integer, Short>> _W = W.parallelStream()
                .filter(w -> !covers(w, z)).collect(Collectors.toList());
        _W.add(z);
        return _W;
    }
}

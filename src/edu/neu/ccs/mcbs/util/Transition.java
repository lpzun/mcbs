package edu.neu.ccs.mcbs.util;

import java.util.List;

public class Transition {

    /**
     * Type of transition
     *
     * NORM ->: the normal thread state transition FORK +>: the thread state
     * creation transition BRCT ~>: the broadcast transition
     *
     * @author Peizun Liu
     */
    public enum Type {
        NORM, FORK, BRCT
    }

    private final Integer src;

    private final Integer dst;

    private final Type type;

    public Transition() {
        this.src = 0;
        this.dst = 0;
        this.type = Type.NORM;
    }

    /**
     *
     * @param src
     * @param dst
     * @param type
     */
    public Transition(Integer src, Integer dst, Type type) {
        this.src = src;
        this.dst = dst;
        this.type = type;
    }

    /**
     * Copy constructor
     *
     * @param t
     */
    public Transition(Transition t) {
        this.src = t.getSrc();
        this.dst = t.getDst();
        this.type = t.getType();
    }

    public Integer getSrc() {
        return src;
    }

    public Integer getDst() {
        return dst;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        String s = Integer.toString(src);
        switch (type) {
            case NORM:
                s += " -> ";
                break;
            case FORK:
                s += " +> ";
                break;
            default:
                s += " ~> ";
                break;
        }
        s += Integer.toString(dst);
        return s;
    }

    /**
     *
     * @param R
     * @return
     */
    public String toString(List<Transition> R) {
        String s = R.get(src).toString();
        switch (type) {
            case NORM:
                s += " -> ";
                break;
            case FORK:
                s += " +> ";
                break;
            default:
                s += " ~> ";
                break;
        }
        s += R.get(dst).toString();
        return s;
    }

    /**
     * (non-Javadoc)
     *
     * @return
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dst == null) ? 0 : dst.hashCode());
        result = prime * result + ((src == null) ? 0 : src.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /**
     * (non-Javadoc)
     *
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Transition)) {
            return false;
        }
        Transition other = (Transition) obj;
        if (src != null && dst != null && other.getSrc() != null
                && other.getDst() != null) {
            return src == other.getSrc() && dst == other.getDst();
        }
        return false;
    }

}

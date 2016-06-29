package edu.neu.ccs.mcbs;

import edu.neu.ccs.mcbs.imp.CBS;
import edu.neu.ccs.mcbs.imp.MBS;
import edu.neu.ccs.mcbs.imp.SBS;
import edu.neu.ccs.mcbs.util.CommandLine;
import edu.neu.ccs.mcbs.util.McbsException;

public class Main {
    /**
     * The entry of the program
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            // starting time
            long startTime = System.currentTimeMillis();

            CommandLine cmd = new CommandLine(args);
            String filename = cmd.getFileTTS();
            String initlS = "0|0";
            String finalS = cmd.getFinalTS();

            String mode = cmd.getMode();
            if (mode == null || mode.equals("")) {
                throw new McbsException("Please assigne mode");
            }

            boolean isCoverable = false;
            System.out.println(
                    "======================================================");
            if (mode.equals("S")) {
                SBS sbs = new SBS(filename, initlS, finalS);
                isCoverable = sbs.sequentialBWS();
                System.out.print(sbs.getTTS().getFinalState());
            } else if (mode.equals("M")) {
                MBS mbs = new MBS(filename, initlS, finalS);
                // mbs.test();
                isCoverable = mbs.concurrentBWS();
                System.out.print(mbs.getTTS().getFinalState());
            } else if (mode.equals("C")) {
                CBS cbs = new CBS(filename, initlS, finalS);
                isCoverable = cbs.concurrentBWS();
                System.out.print(cbs.getTTS().getFinalState());

            }
            if (isCoverable) {
                System.out.println(" is coverable: verification failed!");
            } else {
                System.out.println(" is uncoverable: verification sucessful!");
            }
            System.out.println(
                    "======================================================");

            // terminate time
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            System.out.println("runtime: " + (double) elapsedTime / 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

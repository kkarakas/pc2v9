package edu.csus.ecs.pc2.core.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.ParseArguments;
import edu.csus.ecs.pc2.core.exception.IllegalContestState;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.InternalContest;
import edu.csus.ecs.pc2.exports.ccs.ResultsFile;
import edu.csus.ecs.pc2.exports.ccs.ScoreboardFile;

/**
 * CCS Extractor.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class Extractor {

    // private static final String DEBUG_OPTION_STRING = "--debug";

    private static final String LOGIN_OPTION_STRING = "--login";

    private static final String PASSWORD_OPTION_STRING = "--password";

    public static final String RUNS_TSV_FILENAME = "runs.tsv";

    public static final String SCOREBOARD_TSV_FILENAME = "scoreboard";

    public static final String RESULTS_TSV_FILENAME = "results";

    public static final String SUBMISSION_TSV_FILENAME = "submission";

    public Extractor(String[] args) {
        initialize(args);
    }

    private void initialize(String[] args) {

        try {

            String[] requireArguementArgs = {};

            ParseArguments arguments = new ParseArguments(args, requireArguementArgs);

            if (args.length == 0) {
                usage();
                System.exit(2);
            }

            if (arguments.isOptPresent("--help")) {
                usage();
                System.exit(0);
            }

            if (arguments.getArgCount() == 0) {
                System.err.println("Missing CCS filename");
                System.exit(2);
            }

            // Get loginId
            String loginName = "";
            if (arguments.isOptPresent(LOGIN_OPTION_STRING)) {
                loginName = arguments.getOptValue(LOGIN_OPTION_STRING);
            }

            // CCS standard
            if (arguments.isOptPresent("-u")) {
                loginName = arguments.getOptValue("-u");
            }

            // get password (optional if joe password)
            String password = "";
            if (arguments.isOptPresent(PASSWORD_OPTION_STRING)) {
                password = arguments.getOptValue(PASSWORD_OPTION_STRING);
            }

            // CCS standard
            if (arguments.isOptPresent("-w")) {
                password = arguments.getOptValue("-w");
            }

            String[] argList = arguments.getArgList();
            String cccFilename = argList[0];
            String outputFilename = null;
            if (argList.length > 1) {
                outputFilename = argList[1];
            }

            System.out.println("loginName " + loginName);
            System.out.println("password " + password);
            System.out.println("cccFilename " + cccFilename);
            System.out.println("outputFilename " + outputFilename);

            IReport report = getReportForFilename(cccFilename);

            Plugin plugin = login(loginName, password);
            report.setContestAndController(plugin.getContest(), plugin.getController());
            String[] lines = report.createReport(null);

            if (outputFilename == null || outputFilename.trim().length() == 0) {
                /*
                 * write to stdout
                 */
                for (String string : lines) {
                    System.out.println(string);
                }
            } else {
                PrintWriter printWriter = new PrintWriter(new FileOutputStream(outputFilename, false), true);
                for (String string : lines) {
                    printWriter.println(string);
                }
                printWriter.close();
                printWriter = null;
            }

        } catch (LoginFailureException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * results.tsv report.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    // $HeadURL$
    class ResultsTSVReport extends EmptyReport {
        /**
         * 
         */
        private static final long serialVersionUID = -994643840065683641L;

        private ResultsFile resultsFile = new ResultsFile();

        @Override
        public void setContestAndController(IInternalContest inContest, IInternalController inController) {
            super.setContestAndController(inContest, inController);
        }

        @Override
        public String getReportTitle() {
            return "results.tsv report";
        }

        @Override
        public String[] createReport(Filter filter) {
            try {
                return resultsFile.createTSVFileLines(getContest());
            } catch (IllegalContestState e) {
                e.printStackTrace();
                return new String[0];
            }
        }
    }

    /**
     * scoreboard.tsv report.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    // $HeadURL$
    class ScoreboardTSVReport extends EmptyReport {
        /**
         * 
         */
        private static final long serialVersionUID = -994643840065683641L;

        private ScoreboardFile scoreboardFile = new ScoreboardFile();

        @Override
        public void setContestAndController(IInternalContest inContest, IInternalController inController) {
            super.setContestAndController(inContest, inController);
        }

        @Override
        public String getReportTitle() {
            return "results.tsv report";
        }

        @Override
        public String[] createReport(Filter filter) {
            try {
                return scoreboardFile.createTSVFileLines(getContest());
            } catch (IllegalContestState e) {
                e.printStackTrace();
                return new String[0];
            }
        }
    }

    /**
     * empty report (stub).
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    // $HeadURL$
    class EmptyReport implements IReport {

        /**
         * 
         */
        private static final long serialVersionUID = -7612441954778315221L;

        private IInternalContest contest = null;

        
        public void setContestAndController(IInternalContest inContest, IInternalController inController) {
            contest = inContest;
        }

        
        public String getPluginTitle() {
            return null;
        }

        
        public void writeReport(PrintWriter printWriter) throws Exception {
        }

        
        public void setFilter(Filter filter) {
        }

        
        public void printHeader(PrintWriter printWriter) {
        }

        
        public void printFooter(PrintWriter printWriter) {
        }

        
        public String getReportTitle() {
            return "Empty report";
        }

        
        public Filter getFilter() {
            return null;
        }

        
        public String createReportXML(Filter filter) throws IOException {
            return null;
        }

        
        public void createReportFile(String filename, Filter filter) throws IOException {
        }

        
        public String[] createReport(Filter filter) {
            return null;
        }

        public IInternalContest getContest() {
            return contest;
        }

    };

    private IReport getReportForFilename(String name) {

        if (name == null || name.trim().length() == 0) {
            return null;
        }
        if (SUBMISSION_TSV_FILENAME.equals(name) || RUNS_TSV_FILENAME.equals(name)) {
            return new RunsTSVReport();
        } else if (SCOREBOARD_TSV_FILENAME.equals(name)) {
            return new ScoreboardTSVReport();
        } else if (RESULTS_TSV_FILENAME.equals(name)) {
            return new ResultsTSVReport();
        } else {
            return null;
        }
    }

    class Plugin {
        private IInternalController controller;

        private IInternalContest contest;

        public void setContestAndController(IInternalContest inContest, IInternalController inController) {
            this.controller = inController;
            this.contest = inContest;
        }

        public IInternalController getController() {
            return controller;
        }

        public IInternalContest getContest() {
            return contest;
        }

    }

    /**
     * Login to server.
     * 
     * @param login
     * @param password
     * @return
     * @throws LoginFailureException
     */
    private Plugin login(String login, String password) throws LoginFailureException {

        IInternalContest internalContest = new InternalContest();
        InternalController controller = new InternalController(internalContest);

        controller.setUsingGUI(false);
        controller.setUsingMainUI(false);
        controller.setClientAutoShutdown(false);

        try {
            controller.start(new String[0]);
            internalContest = controller.clientLogin(internalContest, login, password);

            Plugin plugin = new Plugin();
            plugin.setContestAndController(internalContest, controller);
            return plugin;

        } catch (Exception e) {
            throw new LoginFailureException(e.getMessage());
        }
    }

    public static void usage() {

        String[] lines = { "Usage: [options] CCS_reportname [outfilename]", //
                "", //
                LOGIN_OPTION_STRING + " loginName - login name (or use -u)", //
                PASSWORD_OPTION_STRING + " password - login password (or use -w) ", //
                "", //
                "CCS_reportname - one of the following submissions, scoreboard, results", //
                "", //
        };

        for (String s : lines) {
            System.out.println(s);
        }

        VersionInfo info = new VersionInfo();
        System.out.println(info.getSystemVersionInfo());
    }

    public static void main(String[] args) {
        new Extractor(args);
    }
}

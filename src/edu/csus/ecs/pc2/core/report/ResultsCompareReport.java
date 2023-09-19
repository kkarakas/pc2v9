// Copyright (C) 1989-2023 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.core.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.Constants;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.imports.clics.FieldCompareRecord;
import edu.csus.ecs.pc2.core.imports.clics.FileComparison;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.ClientSettings;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.report.FileComparisonUtilities.AwardKey;
import edu.csus.ecs.pc2.core.report.FileComparisonUtilities.ResultTSVKey;
import edu.csus.ecs.pc2.core.report.FileComparisonUtilities.ScoreboardKey;
import edu.csus.ecs.pc2.exports.ccs.ResultsFile;

/**
 * Compare contest results files report.
 * 
 * @author Douglas A. Lane <pc2@ecs.csus.edu>
 */
public class ResultsCompareReport implements IReport {

    private String primaryCCSResultsDir = null;

    private String pc2ResultsDir = null;

    private IInternalContest contest;

    private IInternalController controller;

    private Log log;

    private Filter filter = new Filter();

    private FileComparison resultsCompare;

    //    private FileComparison awardsFileCompare;
    //    private FileComparison scoreboardJsonCompare;

    private JLabel resultsPassFailLabel;


    //    private ResultTSVKey resultTSVKey = new FileComparisonUtilities.ResultTSVKey();
    //    private AwardKey awardsKey = new FileComparisonUtilities.AwardKey();
    //    private ScoreboardKey scoreboardKey = new FileComparisonUtilities.ScoreboardKey();

    /**
     * 
     */
    private static final long serialVersionUID = -796328654541676730L;

    public ResultsCompareReport(IInternalContest contest, IInternalController controller, String primaryCCSResultsDir, String pc2ResultsDir) {
        super();
        this.primaryCCSResultsDir = primaryCCSResultsDir;
        this.pc2ResultsDir = pc2ResultsDir;
        setContestAndController(contest, controller);
    }

    public ResultsCompareReport() {
        ;
    }

    @Override
    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        this.contest = inContest;
        this.controller = inController;
        log = controller.getLog();

        /**
         * Fetch directories from ClientSettings.
         */
        ClientSettings settings = contest.getClientSettings(contest.getClientId());

        if (pc2ResultsDir == null) {
            String pc2rsdir = settings.getProperty(ClientSettings.PC2_RESULTS_DIR);
            pc2ResultsDir = pc2rsdir;
        }

        if (primaryCCSResultsDir == null) {
            String primaryResDir = settings.getProperty(ClientSettings.PRIMARY_CCS_RESULTS_DIR);
            primaryCCSResultsDir = primaryResDir;
        }

    }

    @Override
    public String getPluginTitle() {
        return "Results Compare Report";
    }

    @Override
    public void createReportFile(String filename, Filter inFilter) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileOutputStream(filename, false), true);
        filter = inFilter;

        try {
            printHeader(printWriter);

            try {
                writeReport(printWriter);
            } catch (Exception e) {
                printWriter.println("Exception in report: " + e.getMessage());
                e.printStackTrace(printWriter);
            }

            printFooter(printWriter);

            printWriter.close();
            printWriter = null;

        }

        catch (Exception e) {
            log.log(Log.INFO, "Exception writing report", e);
            printWriter.println("Exception generating report " + e.getMessage());
        }

    }

    private List<String> createFileComparisonReport(String filename, FileComparison fileComparison) {

        List<String> lines = new ArrayList<String>();

        lines.add("File: " + filename);
        lines.add("");

        List<FieldCompareRecord> fields = fileComparison.getComparedFields();
        long differentFields = fileComparison.getNumberDifferences();
        int totalFieldsCompared = fields.size();

        String compSummary = "NOT MATCHING " + differentFields + " differences";
        if (differentFields == 0) {
            if (totalFieldsCompared == 0) {
                compSummary = "NOT MATCHING no fields compared";
            } else {
                compSummary = "All MATCH " + totalFieldsCompared + " match";
            }
        }
        lines.add("  Summary  " + compSummary);

        lines.add("");

        String previousKey = "";

        if (differentFields != 0) {

            Collections.sort(fields, new FieldCompareRecordComparator());

            for (FieldCompareRecord fieldCompareRecord : fields) {

                String theKey = fieldCompareRecord.getKey();
                if (!previousKey.equals(theKey)) {
                    previousKey = theKey;
                    lines.add("");
                    lines.add("Row value: " + theKey);
                }

                if (!fieldCompareRecord.getState().equals(ComparisonState.SAME)) {
                    lines.add(String.format("   field %-18s %8s '%s' '%s'", fieldCompareRecord.getFieldName(), fieldCompareRecord.getState().toString(), fieldCompareRecord.getValueOne(),
                            fieldCompareRecord.getValueTwo()));
                }
            }
        }

        return lines;
    }

    @Override
    public String[] createReport(Filter filter) {

        String resultsFilename = pc2ResultsDir + File.separator + ResultsFile.RESULTS_FILENAME;

        String scoreboardJsonFilename = pc2ResultsDir + File.separator + Constants.SCOREBOARD_JSON_FILENAME;

        String awardsFileName = pc2ResultsDir + File.separator + Constants.AWARDS_JSON_FILENAME;

        ExportFilesUtiltiites.writeResultsFiles(contest, getPc2ResultsDir());

        System.out.println("debug 22 resultsFilename = " + resultsFilename);
        System.out.println("debug 22 scoreboardJsonFilename = " + scoreboardJsonFilename);
        System.out.println("debug 22 awardsFileName = " + awardsFileName);

        String[] filesToCompare = { //
                ResultsFile.RESULTS_FILENAME, //
                Constants.SCOREBOARD_JSON_FILENAME, //
                Constants.AWARDS_JSON_FILENAME, //
        };

        String sourceDir = pc2ResultsDir;
        String targetDir = primaryCCSResultsDir;
        if (targetDir == null) {
            throw new RuntimeException("Primary CCS directory is not assigned (is null)");
        }

        String compareMessage = "Comparison Summary:   FAILED - no such directory (cdp directory not set) " + targetDir;

        List<String> outList = new ArrayList<String>();

        String[] headerLines = { //

                "Primary CCS Results dir: " + getPrimaryCCSResultsDir(), //
                "pc2 results dir        : " + getPc2ResultsDir(), //
                "compared files         : " + String.join(", ", filesToCompare), //
                "", //
                compareMessage, //
                "", };

        // TODO 760 add header lines

        outList.add(compareMessage);  // debug 22 remove

        if (new File(targetDir).isDirectory()) {


            ResultTSVKey resultTSVKey = new FileComparisonUtilities.ResultTSVKey();
            AwardKey awardsKey = new FileComparisonUtilities.AwardKey();
            ScoreboardKey scoreboardKey = new FileComparisonUtilities.ScoreboardKey();

            FileComparison resultsCompare = FileComparisonUtilities.createTSVFileComparison(ResultsFile.RESULTS_FILENAME, sourceDir, targetDir, resultTSVKey);
            FileComparison awardsFileCompare = FileComparisonUtilities.createJSONFileComparison(Constants.AWARDS_JSON_FILENAME, sourceDir, targetDir, awardsKey);
            FileComparison scoreboardJsonCompare = FileComparisonUtilities.createJSONFileComparison(Constants.SCOREBOARD_JSON_FILENAME, sourceDir, targetDir, scoreboardKey);
            
            long totalDifferences = //
                    resultsCompare.getNumberDifferences() + //
                            awardsFileCompare.getNumberDifferences() + //
                            scoreboardJsonCompare.getNumberDifferences();

            if (totalDifferences == 0) {
                outList.add("Summary:   All PASS - no differences found");
            } else {
                outList.add("Summary:   FAIL - there are " + totalDifferences + "  differences found");
            }
            
            // add details to reort
            List<String> lines = createFileComparisonReport(ResultsFile.RESULTS_FILENAME, resultsCompare);
            outList.addAll(lines);
            lines = createFileComparisonReport(Constants.AWARDS_JSON_FILENAME, awardsFileCompare);
            outList.addAll(lines);
            lines = createFileComparisonReport(Constants.SCOREBOARD_JSON_FILENAME, scoreboardJsonCompare);
            outList.addAll(lines);

        }

        return (String[]) outList.toArray(new String[outList.size()]);
    }

    @Override
    public String createReportXML(Filter filter) throws IOException {
        throw new RuntimeException("createReportXML not implemented");
    }

    @Override
    public void writeReport(PrintWriter printWriter) throws Exception {

        try {

            if (primaryCCSResultsDir == null || !(new File(primaryCCSResultsDir).isDirectory())) {
                throw new RuntimeException("Primary CCS Results directory not defined or not a directory");
            }

            if (pc2ResultsDir == null || !(new File(pc2ResultsDir).isDirectory())) {
                throw new RuntimeException("pc2 Results directory not defined or not a directory");
            }

            // results.tsv
            // (results.csv file if/when available #351)
            // scoreboard.json
            // awards.json

            String[] lines = createReport(filter);

            for (String line : lines) {
                printWriter.println(line);
            }

        } catch (RuntimeException rte) {
            log.log(Log.INFO, "Exception writing report", rte);
            printWriter.println("Error/problem generating report " + rte.getMessage());
            rte.printStackTrace(printWriter);
        }
    }

    @Override
    public String getReportTitle() {
        return "Results Compare Report";
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public void printHeader(PrintWriter printWriter) {

        printWriter.println(new VersionInfo().getSystemName());
        printWriter.println("Date: " + Utilities.getL10nDateTime());
        printWriter.println(new VersionInfo().getSystemVersionInfo());
        printWriter.println();
        printWriter.println(getReportTitle() + " Report");

    }

    @Override
    public void printFooter(PrintWriter printWriter) {
        printWriter.println();
        printWriter.println("end report: " + getReportTitle());

    }

    public String getPrimaryCCSResultsDir() {
        return primaryCCSResultsDir;
    }

    public String getPc2ResultsDir() {
        return pc2ResultsDir;
    }
}

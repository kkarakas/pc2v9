package edu.csus.ecs.pc2.list;

import java.io.File;

import edu.csus.ecs.pc2.core.model.ElementId;
import edu.csus.ecs.pc2.core.model.Run;

public class SubmissionSample {

    private ElementId elementId = null;
    private String problemName = null;
    private ElementId problem = null;

    private String languageName = null;
    private ElementId language = null;

    private String sampleType = null;

    private File srcFile = null;

    private Run run = null;

    public SubmissionSample(String probShortName, ElementId prob, String langName, ElementId lang, String type, File srcFile) {
        elementId = new ElementId(getClass().getName());
        problemName = probShortName;
        problem = prob;
        languageName = langName;
        language = lang;
        sampleType = type;
        this.srcFile = srcFile;
    }

    public ElementId getElementId() {
        return(elementId);
    }

    public void setRun(Run r) {
        run = r;
    }

    public Run getRun() {
        return run;
    }

    // JB this is wrong - needs fixing
    public boolean isRunForSubmission(Run r) {

        if(srcFile != null) {
            return(false);
        }
        return(false);
    }

    public String getProblemName() {
        return problemName;
    }

    public ElementId getProblem() {
        return problem;
    }

    public String getLanguageName() {
        return languageName;
    }

    public ElementId getLanguage() {
        return language;
    }

    public String getSampleType() {
        return sampleType;
    }

    public File getSourceFile() {
        return srcFile;
    }

    @Override
    public String toString() {
        String str = getProblemName() + " (" + getSampleType() + ") Language: " + getLanguageName() + ": " + getSourceFile().getAbsolutePath();
        Run run = getRun();
        if(run != null) {
            str = str + " (RunId " + run.getNumber() + ")";
        }
        return(str);
    }
}

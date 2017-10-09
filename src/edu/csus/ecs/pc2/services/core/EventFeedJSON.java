package edu.csus.ecs.pc2.services.core;

import java.util.Arrays;
import java.util.Vector;

import edu.csus.ecs.pc2.core.exception.IllegalContestState;
import edu.csus.ecs.pc2.core.list.AccountComparator;
import edu.csus.ecs.pc2.core.list.ClarificationComparator;
import edu.csus.ecs.pc2.core.list.GroupComparator;
import edu.csus.ecs.pc2.core.list.RunComparator;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.Clarification;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.Group;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Judgement;
import edu.csus.ecs.pc2.core.model.Language;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.Run;
import edu.csus.ecs.pc2.core.model.RunResultFiles;

/**
 * Event feed information in the CLICS JSON format.
 * 
 * @author Douglas A. Lane, PC^2 Team, pc2@ecs.csus.edu
 */
// TODO for all sections pass in Key rather than hard coded inside method
public class EventFeedJSON extends JSONUtilities {

  
    private JudgementTypeJSON judgementTypeJSON = new JudgementTypeJSON();

    private LanguageJSON languageJSON = new LanguageJSON();

    private SubmissionJSON submissionJSON = new SubmissionJSON();

    private ProblemJSON problemJSON = new ProblemJSON();

    private TeamJSON teamJSON = new TeamJSON();

    private OrganizationJSON organizationJSON = new OrganizationJSON();

    private ContestJSON contestJSON = new ContestJSON();

    private GroupJSON groupJSON = new GroupJSON();

    private AwardJSON awardJSON = new AwardJSON();

    private ClarificationJSON clarificationJSON = new ClarificationJSON();
    
    private JudgementJSON judgementJSON = new JudgementJSON();
    
    private TeamMemberJSON teamMemberJSON = new TeamMemberJSON();

    private RunJSON runJSON = new RunJSON();
    
    
    /**
    * Event Id Sequence.
    * 
    * @see #nextEventId()
    */
   protected long eventIdSequence = 0;
    
    /**
     * Start event id.
     * 
     * /event-feed?events=<event_list>
     */
    private String startEventId = null;

    boolean pastStartEvent = true;

    /**
     * List of events to output.
     * 
     */
    private String eventFeedList = null;

    public String getContestJSON(IInternalContest contest) {

        if (!isPastStartEvent()) {
            return null;
        }
        
        judgementTypeJSON.
        updatePenaltySettings(contest);

        StringBuilder stringBuilder = new StringBuilder();

        appendJSONEvent(stringBuilder, CONTEST_KEY, eventIdSequence, EventFeedOperation.CREATE, getContestJSONFields(contest));
        stringBuilder.append(NL);
        return stringBuilder.toString();

    }

    public String getContestJSONFields(IInternalContest contest) {
        return contestJSON.createJSON(contest);
    }

    /**
     * List of judgements.
     * 
     */
    public String getJudgementTypeJSON(IInternalContest contest) {

        StringBuilder stringBuilder = new StringBuilder();

        Judgement[] judgements = contest.getJudgements();
        for (Judgement judgement : judgements) {

            if (isPastStartEvent()) {

                appendJSONEvent(stringBuilder, JUDGEMENT_TYPE_KEY, eventIdSequence, EventFeedOperation.CREATE, getJudgementTypeJSON(contest, judgement));
                stringBuilder.append(NL);
            }

        }

        return stringBuilder.toString();
    }

    String getJudgementTypeJSON(IInternalContest contest, Judgement judgement) {
        return judgementTypeJSON.createJSON(contest, judgement);
    }

    /**
     * Get all languages JSON.
     * 
     * @param contest
     * @return
     */
    public String getLanguageJSON(IInternalContest contest) {

        StringBuilder stringBuilder = new StringBuilder();

        Language[] languages = contest.getLanguages();
        int id = 1;
        for (Language language : languages) {

            if (isPastStartEvent()) {

                appendJSONEvent(stringBuilder, LANGUAGE_KEY, eventIdSequence, EventFeedOperation.CREATE, getLanguageJSON(contest, language, id));
                stringBuilder.append(NL);
            }

            id++;
        }

        return stringBuilder.toString();

    }

    /**
     * get JSON for a language.
     * 
     * @param contest
     * @param language
     * @param languageNumber sequence number
     * @return
     */
    public String getLanguageJSON(IInternalContest contest, Language language, int languageNumber) {
        return languageJSON.createJSON(contest, language, languageNumber);
    }

    public String getProblemJSON(IInternalContest contest) {

        StringBuilder stringBuilder = new StringBuilder();

        Problem[] problems = contest.getProblems();
        int id = 1;
        for (Problem problem : problems) {

            if (isPastStartEvent()) {
                appendJSONEvent(stringBuilder, PROBLEM_KEY, eventIdSequence, EventFeedOperation.CREATE, getProblemJSON(contest, problem, id));
                stringBuilder.append(NL);
            }
            id++;
        }

        return stringBuilder.toString();
    }

    public String getProblemJSON(IInternalContest contest, Problem problem, int problemNumber) {
        return problemJSON.createJSON(contest, problem,problemNumber);
    }

    public String getGroupJSON(IInternalContest contest) {

        StringBuilder stringBuilder = new StringBuilder();
        Group[] groups = contest.getGroups();

        Arrays.sort(groups, new GroupComparator());
        for (Group group : groups) {

            if (isPastStartEvent()) {

                appendJSONEvent(stringBuilder, GROUPS_KEY, eventIdSequence, EventFeedOperation.CREATE, getGroupJSON(contest, group));
                stringBuilder.append(NL);
            }
        }

        return stringBuilder.toString();
    }

    protected String getGroupJSON(IInternalContest contest, Group group) {
        return groupJSON.createJSON(contest, group);

    }

    public String getOrganizationJSON(IInternalContest contest) {
        return organizationJSON.createJSON(contest);
    }

    /**
     * Get all sites' teams.
     * 
     * @param contest
     * @return
     */
    public Account[] getTeamAccounts(IInternalContest inContest) {
        Vector<Account> accountVector = inContest.getAccounts(ClientType.Type.TEAM);
        Account[] accounts = (Account[]) accountVector.toArray(new Account[accountVector.size()]);
        Arrays.sort(accounts, new AccountComparator());

        return accounts;
    }

    public String getTeamJSON(IInternalContest contest) {

        StringBuilder stringBuilder = new StringBuilder();

        Account[] accounts = getTeamAccounts(contest);
        Arrays.sort(accounts, new AccountComparator());

        for (Account account : accounts) {

            if (isPastStartEvent()) {

                appendJSONEvent(stringBuilder, TEAM_KEY, eventIdSequence, EventFeedOperation.CREATE, getTeamJSON(contest, account));
                stringBuilder.append(NL);
            }
        }

        return stringBuilder.toString();
    }



    public String getTeamJSON(IInternalContest contest, Account account) {
        return teamJSON.createJSON(contest, account);
    }

    /**
     * Get team member info.
     * 
     */
    public String getTeamMemberJSON(IInternalContest contest) {

        StringBuilder stringBuilder = new StringBuilder();

        Account[] accounts = getTeamAccounts(contest);
        Arrays.sort(accounts, new AccountComparator());

        for (Account account : accounts) {
            String[] names = account.getMemberNames();

            if (names.length > 0) {
                for (String teamMemberName : names) {

                    if (isPastStartEvent()) {
                        appendJSONEvent(stringBuilder, TEAM_MEMBERS_KEY, eventIdSequence, EventFeedOperation.CREATE, getTeamMemberJSON(contest, account, teamMemberName));
                        stringBuilder.append(NL);
                    }
                }
            }
        }

        return stringBuilder.toString();
    }

    protected String getTeamMemberJSON(IInternalContest contest, Account account, String teamMemberName) {
        return teamMemberJSON.createJSON(contest, account,teamMemberName );
    }

    /**
     * Get run submission.
     * 
     * @param contest
     * @return
     */
    public String getSubmissionJSON(IInternalContest contest) {

        StringBuilder stringBuilder = new StringBuilder();
        Run[] runs = contest.getRuns();

        Arrays.sort(runs, new RunComparator());
        for (Run run : runs) {

            if (isPastStartEvent()) {

                appendJSONEvent(stringBuilder, SUBMISSION_KEY, eventIdSequence, EventFeedOperation.CREATE, getSubmissionJSON(contest, run));
                stringBuilder.append(NL);
            }
        }

        return stringBuilder.toString();

    }

    private String getSubmissionJSON(IInternalContest contest, Run run) {
        return submissionJSON.createJSON(contest, run);
    }

    /**
     * List of all runs' judgements..
     * 
     */
    public String getJudgementJSON(IInternalContest contest) {

        StringBuilder stringBuilder = new StringBuilder();
        Run[] runs = contest.getRuns();

        Arrays.sort(runs, new RunComparator());

        for (Run run : runs) {
            
            if (run.isJudged()){

                if (isPastStartEvent()) {

                    appendJSONEvent(stringBuilder, JUDGEMENT_KEY, eventIdSequence, EventFeedOperation.CREATE, getJudgementJSON(contest, run));
                    stringBuilder.append(NL);
                }

            }
        }

        return stringBuilder.toString();
    }

    private String getJudgementJSON(IInternalContest contest, Run run) {
        return judgementJSON.createJSON(contest, run);
    }

    /**
     * Return test cases.
     * 
     * @param contest
     */
    public String getRunJSON(IInternalContest contest) {

        StringBuilder stringBuilder = new StringBuilder();
        Run[] runs = contest.getRuns();

        Arrays.sort(runs, new RunComparator());
        for (Run run : runs) {

            if (isPastStartEvent()) {
                RunResultFiles files = null; // TODO CLICS must fetch files to get main file name, or put mainfile name into Run
                appendJSONEvent(stringBuilder, RUN_KEY, eventIdSequence, EventFeedOperation.CREATE, getRunJSON(contest, run, files));
                stringBuilder.append(NL);
            }
        }

        return stringBuilder.toString();
    }

    private String getRunJSON(IInternalContest contest, Run run, RunResultFiles files) {
        return runJSON.createJSON(contest, run, files);
    }

    /**
     * Clarification Answer.
     * @param contest
     * @return
     */
    public String getClarificationJSON(IInternalContest contest) {
        StringBuilder stringBuilder = new StringBuilder();
        Clarification[] clarifications = contest.getClarifications();

        Arrays.sort(clarifications, new ClarificationComparator());
        for (Clarification clarification : clarifications) {

            if (isPastStartEvent()) {

                appendJSONEvent(stringBuilder, CLARIFICATIONS_KEY, eventIdSequence, EventFeedOperation.CREATE, getClarificationJSON(contest, clarification));
                stringBuilder.append(NL);
            }
        }

        return stringBuilder.toString();
    }

    String getClarificationJSON(IInternalContest contest, Clarification clarification) {
        return clarificationJSON.createJSON(contest, clarification);

    }

    public String getAwardJSON(IInternalContest contest) {
        return awardJSON.createJSON(contest);
    }

    /**
     * Returns a JSON string listing the current contest event feed
     * 
     * @param contest - the current contest
     * @return a JSON string giving event feed in JSON
     * @throws IllegalContestState
     */
    public String createJSON(IInternalContest contest) throws IllegalContestState {

        if (contest == null) {
            return "[]";
        }

        //        Vector<Account> accountlist = contest.getAccounts(Type.TEAM);
        //        if (accountlist.size() == 0) {
        //            return "[]";
        //        }
        //        Account[] accounts = (Account[]) accountlist.toArray(new Account[accountlist.size()]);
        //
        //        Group[] groups = contest.getGroups();
        //        final Map<ElementId, String> groupMap = new HashMap<ElementId, String>();
        //        for (Group group : groups) {
        //            groupMap.put(group.getElementId(), group.getDisplayName());
        //        }
        StringBuffer buffer = new StringBuffer();

        //        contest = new SampleContest().createStandardContest();
        
        if (eventFeedList != null){
            
            appendAllJSONEvents (contest, buffer, eventFeedList);
            
        } else {

            String json = getContestJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
            json = getJudgementTypeJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
            json = getLanguageJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
            json = getProblemJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
            json = getGroupJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
            json = getOrganizationJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
            json = getTeamJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
            json = getTeamMemberJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
            json = getSubmissionJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
            json = getJudgementJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
            json = getRunJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
            json = getClarificationJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
            json = getAwardJSON(contest);
            if (json != null) {
                buffer.append(json);
            }
        }
        return buffer.toString();
    }

    /**
     * Appends named event types onto a buffer.
     * 
     * valid events are:  awards, clarifications, contests, groups, judgement-types, 
     * judgements, languages, organizations, problems, runs, submissions, team-members, teams
     * 
     * @param contest
     * @param buffer 
     * @param eventlist list of events types, comma delimited 
     * @throws IllegalArgumentException if any event in eventlist is not valid
     */
    private void appendAllJSONEvents(IInternalContest contest, StringBuffer buffer, String eventlist) throws IllegalArgumentException {

        String[] events = eventlist.split(",");

        for (String name : events) {
            name = name.trim();

            switch (name) {
                case CONTEST_KEY:
                    appendNotNull(buffer, getContestJSON(contest));
                    break;
                case JUDGEMENT_TYPE_KEY:
                    appendNotNull(buffer, getJudgementTypeJSON(contest));
                    break;
                case LANGUAGE_KEY:
                    appendNotNull(buffer, getLanguageJSON(contest));
                    break;
                case PROBLEM_KEY:
                    appendNotNull(buffer, getProblemJSON(contest));
                    break;
                case GROUPS_KEY:
                    appendNotNull(buffer, getGroupJSON(contest));
                    break;
                case ORGANIZATION_KEY:
                    appendNotNull(buffer, getOrganizationJSON(contest));
                    break;
                case TEAM_KEY:
                    appendNotNull(buffer, getTeamJSON(contest));
                    break;
                case TEAM_MEMBERS_KEY:
                    appendNotNull(buffer, getTeamMemberJSON(contest));
                    break;
                case SUBMISSION_KEY:
                    appendNotNull(buffer, getSubmissionJSON(contest));
                    break;
                case JUDGEMENT_KEY:
                    appendNotNull(buffer, getJudgementJSON(contest));
                    break;
                case RUN_KEY:
                    appendNotNull(buffer, getRunJSON(contest));
                    break;
                case CLARIFICATIONS_KEY:
                    appendNotNull(buffer, getClarificationJSON(contest));
                    break;
                case AWARD_KEY:
                    appendNotNull(buffer, getAwardJSON(contest));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown event type '" + name + "' in list " + eventlist);
            }
        }
    }


    /**
     * Get next event id.
     */
    public String nextEventId() {
        eventIdSequence++;
        return getEventId(eventIdSequence);
    }

    /**
     * get event id. 
     * 
     * @param sequenceNumber ascending number
     * @return event Id
     */
    public String getEventId(long sequenceNumber) {
        return "pc2-" + sequenceNumber;
    }

    /**
     * Increment event (feed) id and test whether past start event id.
     * 
     * If no {@link #startEventId} defined then returns true;
     * If {@link #startEventId} defined will increment event id and
     *  if that event id is equal to the new event id returns true.
     *  
     *  if {@link #startEventId} is found will return true on
     *  each successive call.
     * 
     * @return true if no {@link #startEventId} defined, or if past {@link #startEventId} 
     */
    public boolean isPastStartEvent() {

        if (nextEventId().equals(getStartEventId()) && !pastStartEvent) {
            pastStartEvent = true;
        }
        return pastStartEvent;
    }

    // TODO technical deficit - move these methods
    // TODO move pair methods into JsonUtilities

    public String getStartEventId() {
        return startEventId;
    }

    public void setStartEventId(String startEventId) {

        pastStartEvent = false;
        this.startEventId = startEventId;
    }

    public void setEventFeedList(String eventFeedList) {
        this.eventFeedList = eventFeedList;
    }

    public String getEventFeedList() {
        return eventFeedList;
    }
    
    public long getEventIdSequence() {
        return eventIdSequence;
    }
    
    public void setEventIdSequence(long eventIdSequence) {
        this.eventIdSequence = eventIdSequence;
    }
}
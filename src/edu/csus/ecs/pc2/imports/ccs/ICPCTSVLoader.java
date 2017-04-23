package edu.csus.ecs.pc2.imports.ccs;

import java.io.FileNotFoundException;
import java.util.HashMap;

import edu.csus.ecs.pc2.core.PermissionGroup;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.ElementId;
import edu.csus.ecs.pc2.core.model.Group;
import edu.csus.ecs.pc2.core.security.PermissionList;
import edu.csus.ecs.pc2.core.util.TabSeparatedValueParser;

/**
 * ICPC CCS TSV File Loader.
 * 
 * Loads group and teams.tsv files into Group and Account lists.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public final class ICPCTSVLoader {

    private ICPCTSVLoader() {
        super();
    }

    // From CCS Specification

    // team.tsv

    // Field Description Example Type
    // 1 Label teams fixed string (always same value)
    // 2 Version number 1 integer
    //
    //
    // Then follow several lines with the following format (one per team).
    // Field Description Example Type
    // 1 Team number 22 integer
    // 2 Reservation ID 24314 integer
    // 3 Group ID 4 integer
    // 4 Team name Hoos string
    // 5 Institution name University of Virginia string
    // 6 Institution short name U Virginia string
    // 7 Country USA string ISO 3166-1 alpha-3

    private static final int TEAM_TSV_FIELDS = 7;
    // plus 
    // 8 institute id "INST-" + integer
    private static final int TEAM2_TSV_FIELDS = 8;

    // From CCS Specification

    // groups.tsv

    // Field Description Example Type
    // 1 Label groups fixed string (always same value)
    // 2 Version number 1 integer
    //
    // Then follow several lines with the following format (one per team group).
    // Field Description Example Type
    // 1 Group ID 902 integer
    // 2 Group name North America string

    private static final int GROUP_TSV_FIELDS = 2;

    /**
     * Null string value.
     */
    private static final String NULL_STRING = "null";

    private static int siteNumber = 1;

    private static Group[] groups = new Group[0];

    /**
     * Load teams.tsv, use {@link #loadGroups(String)} first.
     * 
     * {@link #setGroups(Group[])} must be invoked before using this method so groupIds are assigned.
     * 
     * @param filename
     * @return
     * @throws Exception
     */
    public static Account[] loadAccounts(String filename) throws Exception {

        String[] lines = CCSListUtilities.filterOutCommentLines(Utilities.loadFile(filename));

        if (lines.length == 0) {
            throw new FileNotFoundException(filename);
        }
        
        int i = 0;

        String firstLine = lines[i];

        /**
         * Read first line, header/version info
         */
        // 1 Label teams fixed string (always same value)
        // 2 Version number 1 integer
        String[] fields = TabSeparatedValueParser.parseLine(firstLine);

        // TODO CCS check for 'teams' when tsv file contains that value.
        // validate first line
        // String[] fields = firstLine.split("\t");
        
//        if (!fields[0].trim().equals("teams")) {
//            throw new InvalidValueException("Expecting 'teams' got '" + fields[0] + "' in " + filename);
//        }

        i++;

        Account[] accounts = new Account[lines.length - 1];

        int accountCount = 0;
        ClientType.Type team = ClientType.Type.TEAM;
        PermissionList teamPermissionList = new PermissionGroup().getPermissionList (team);

        for (; i < lines.length; i++) {

            // fields = lines[i].split("\t");
            fields = TabSeparatedValueParser.parseLine(lines[i]);

            if (!(fields.length == TEAM_TSV_FIELDS || fields.length == TEAM2_TSV_FIELDS) && i == 1) {
                throw new InvalidFileFormat("Expected " + TEAM_TSV_FIELDS + " fields, found " + fields.length + " is this a valid team.tsv file? (" + filename + ")");
            }

            accounts[accountCount] = accountFromFields(fields, accountCount, lines[i]);
            accounts[accountCount].clearListAndLoadPermissions(teamPermissionList);
            accountCount++;
        }

        return accounts;
    }

    private static Account accountFromFields(String[] fields, int accountCount, String originalLine) throws InvaildNumberFields, InvalidValueException {
        
        if (!(fields.length == TEAM_TSV_FIELDS || fields.length == TEAM2_TSV_FIELDS)) {
            throw new InvaildNumberFields("Expected " + TEAM_TSV_FIELDS + " (or "+ TEAM2_TSV_FIELDS + ") fields, found " + fields.length + " invalid team.tsv line: " + originalLine);
        }
        
        int fieldnum = 0;
        String numberStr = fields[fieldnum++];
        String reservationIdStr = fields[fieldnum++];
        String groupIDStr = fields[fieldnum++];
        String teamName = fields[fieldnum++];
        String schoolName = fields[fieldnum++];
        String schoolShortName = fields[fieldnum++];
        String countryCode = fields[fieldnum++];
        String institutionCode = "";
        if (fields.length == TEAM2_TSV_FIELDS) {
            institutionCode = fields[fieldnum++];
        }

        // 1 Team number 22 integer
        // 2 Reservation ID 24314 integer
        // 3 Group ID 4 integer
        // 4 Team name Hoos string
        // 5 Institution name University of Virginia string
        // 6 Institution short name U Virginia string
        // 7 Country USA string ISO 3166-1 alpha-3

        int number = 0;
        
        if (numberStr != null && NULL_STRING.equalsIgnoreCase(numberStr.trim())){
            /**
             * If team number field is 'null' then assign a team number.
             * The input teams.tsv from the CMS as of 2/24/2013 contains a constant
             * string 'null' where the team number should be.
             */
            number = accountCount + 1;
        } else {
            try {
                number = Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                throw new InvalidValueException("Expecting team number got '" + numberStr + "' on line " + originalLine, e);
            }
        }

        int groupNumber = 0; 
        try {
            groupNumber = Integer.parseInt(groupIDStr);
        } catch (NumberFormatException e) {
            throw new InvalidValueException("Expecting group number got '" + numberStr + "' on line " + originalLine, e);
        }

        ClientId clientId = new ClientId(siteNumber, ClientType.Type.TEAM, number);
        Account account = new Account(clientId, getJoePassword(clientId), siteNumber);

        account.setDisplayName(schoolName);
        account.setShortSchoolName(schoolShortName);
        account.setLongSchoolName(schoolName);
        account.setExternalName(schoolName);
        account.setTeamName(teamName);
        account.setExternalId(reservationIdStr);
        account.setCountryCode(countryCode);
        if (institutionCode.equals("")) {
            account.setInstitutionCode(institutionCode);
        }
        
        if (groupNumber != 0){
            /**
             * Only lookup group number if not zero.
             */
            ElementId groupId = getGroupForNumber(groupNumber);
            
            if (groupId != null) {
                account.setGroupId(groupId);
            } else {
                throw new InvalidValueException("Unknown group number '" + groupNumber + "' on line " + originalLine);
            }
        }

        return account;
    }

    private static ElementId getGroupForNumber(int groupId) {

        for (Group group : groups) {
            if (group.getGroupId() == groupId) {
                return group.getElementId();
            }
        }

        return null;
    }

    /**
     * return joe password for clientid.
     * 
     * @param clientId
     * @return
     */
    private static String getJoePassword(ClientId clientId) {
        return clientId.getClientType().toString().toLowerCase() + clientId.getClientNumber();
    }

    public static void setGroups(Group[] groups) {
        ICPCTSVLoader.groups = groups;
    }

    /**
     * Load groups into this class.
     * 
     * @param filename
     * @return list of groups or new Group[0]
     * @throws Exception
     */
    public static Group[] loadGroups(String filename) throws Exception {
        
        String[] lines = CCSListUtilities.filterOutCommentLines(Utilities.loadFile(filename));
        
        if (lines.length == 0) {
            throw new FileNotFoundException(filename);
        }

        int i = 0;

        String firstLine = lines[i];

        // validate first line
        // String[] fields = firstLine.split("\t");
        String[] fields = TabSeparatedValueParser.parseLine(firstLine);

        // 1 Label groups fixed string (always same value)
        // 2 Version number 1 integer

        // TODO CCS do check for 'groups' when tsv file contains that field.
//        if (!fields[0].trim().equals("groups")) {
//            throw new InvalidValueException("Expecting 'groups' got '" + fields[0] + "' in " + filename);
//        }

        i++;

        groups = new Group[lines.length - 1];

        int groupAccount = 0;

        for (; i < lines.length; i++) {

            // fields = lines[i].split("\t");
            fields = TabSeparatedValueParser.parseLine(lines[i]);

            if (fields.length != GROUP_TSV_FIELDS && i == 1) {
                throw new InvalidFileFormat("Expected " + GROUP_TSV_FIELDS + " fields, found " + fields.length + " is this a valid groups.tsv file? (" + filename + ")");
            }
            if (fields.length != GROUP_TSV_FIELDS) {
                throw new InvaildNumberFields("Expected " + GROUP_TSV_FIELDS + " fields, found " + fields.length + " invalid groups.tsv line " + lines[i]);
            }

            int fieldnum = 0;
            String numberStr = fields[fieldnum++];
            String groupName = fields[fieldnum++];

            // Field Description Example Type
            // 1 Group ID 902 integer
            // 2 Group name North America string

            int number = 0;
            try {
                number = Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                throw new InvalidValueException("Expecting group number got '" + numberStr + "' on line " + lines[i], e);
            }

            Group group = new Group(groupName);
            group.setGroupId(number);

            groups[groupAccount] = group;
            groupAccount++;
        }

        return groups;
    }

    public static HashMap<Integer, String> loadPasswordsFromAccountsTSV(String filename) throws Exception {
        String[] lines = CCSListUtilities.filterOutCommentLines(Utilities.loadFile(filename));

        HashMap<Integer, String> passwordMap = new HashMap<Integer, String>();
        // do not care about the first line (line 0), so start with 1
        for (int i = 1; i < lines.length; i++) {
            String[] fields = TabSeparatedValueParser.parseLine(lines[i]);
            if (fields[0].equals("team")) {
                // need to parse the number out of team-\d\d\d in field 2 and the password from field 3
                String account = fields[2];
                int dash = account.indexOf('-');
                if (dash > 0) {
                    account = account.substring(dash + 1);
                } else {
                    account = account.substring(5);
                }
                System.err.println("for " + fields[2] + " found acount number '" + account + "'");
                Integer number = new Integer(account);
                String password = fields[3];
                passwordMap.put(number, password);
            }

        }
        return passwordMap;
    }
}

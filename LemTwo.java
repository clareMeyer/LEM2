import java.util.*;

import java.io.*;
import java.nio.file.*;
import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

public class LemTwo {

    String attVal[] = new String[2];
    // Map<[attribute, value], {cases}>
    static ArrayList<TheInfo> theList = new ArrayList<TheInfo>();
    // Map<decision, {cases}>
    static List<TheInfo> decisionsList = new ArrayList<>();

    static List<String> attributes = new ArrayList<>();
    static String decisionName;

    static int num = 0;
    static TheInfo pick;

    static ArrayList<TheInfo> ruleConditions = new ArrayList<>();
    static ArrayList<ArrayList<Integer>> ruleCases = new ArrayList<ArrayList<Integer>>();
    static ArrayList<Integer> casesCovered = new ArrayList<Integer>();

    static String outputFileName;

    static boolean writeAnd;

    public static void main(final String[] args) throws IOException {

        Scanner getInfo = new Scanner(System.in);
        String fileName = " ";

        while (!Files.isReadable(Paths.get(fileName))) {
            if (fileName != " ") {
                System.out.println("ENTER VALID NAME");
            }
            System.out.print("Enter file name: ");
            fileName = getInfo.nextLine();
        }

        System.out.print("Enter rule file name: ");
        outputFileName = getInfo.nextLine();

        getInfo.close();

        int numAttributes = 0;
        int numNumAttributes = 0;
        int numStringAttributes = 0;

        final String delim = "[\\n\\t ]";

        final String content = Files.readString(Paths.get(fileName));
        content.replaceAll("!.*?!", "");

        String contentArr[] = content.split(delim);

        int count = 1;
        while (contentArr[count].contains("a")) {
            numAttributes += 1;
            count += 1;
        }
        count += 3;
        for (int i = 0; i < numAttributes; i++) {
            attributes.add(contentArr[count + i]);
        }
        count += numAttributes;
        decisionName = contentArr[count];

        count += 1;

        count++;
        contentArr = Arrays.copyOfRange(contentArr, count, contentArr.length);

        final Map<String, String> types = new HashMap<>();
        for (int i = 0; i < numAttributes; i++) {
            if (contentArr[i].matches("[+-]?\\d*(\\.\\d+)?")) {
                System.out.println("isNumber: " + contentArr[i]);
                types.put(attributes.get(i), "number");
                numNumAttributes++;
            } else {
                types.put(attributes.get(i), "string");
                numStringAttributes++;
            }
            count++;
        }

        final int numCases = contentArr.length / (numAttributes + 1);
        List<List<Double>> valsList = new ArrayList<List<Double>>();

        ///////////// FOR CUTS/////////////////
        // compile and sort all the values for each attribute that is numeric
        // vasList is a list that holds a list of sorted values for each numeric
        ///////////// attribute
        if (numNumAttributes != 0) {

            for (int i = 0; i < numAttributes; i++) {
                List<Double> holdForAtt = new ArrayList<>();
                for (int j = 0; j < numCases; j++) {
                    final int real = i + (j * (numAttributes + 1));
                    if (types.get(attributes.get(i)) == "number") {
                        if (!holdForAtt.contains(Double.valueOf(contentArr[real]))) {
                            holdForAtt.add(Double.valueOf(contentArr[real]));
                        }
                    }
                }
                Collections.sort(holdForAtt);
                valsList.add(holdForAtt);
            }

            // get the cut values for each attribute
            // pleasework holds a list of cut values for each attribute
            ArrayList<ArrayList<String>> pleaseWork = getCutValues(valsList);

            // get a list of just the cut values not the ranges for each attribute
            List<List<Double>> cuts = getJustCuts(valsList);

            // all is a list that has a list for each attribute
            // each attribute list has lists of cases for each interval
            ArrayList<ArrayList<ArrayList<Integer>>> all = new ArrayList<ArrayList<ArrayList<Integer>>>();

            // for each numerical attribute
            for (int i = 0; i < numAttributes; i++) {
                if ((types.get(attributes.get(i)) == "number")) {
                    // get the number of cuts for this attribute
                    int numCuts = pleaseWork.get(i).size();

                    // holdIt is a list of cases for each interval for this one attribute
                    // this is initialized with 0s because I need values I can change
                    ArrayList<ArrayList<Integer>> holdIt = new ArrayList<ArrayList<Integer>>();
                    for (int e = 0; e < numCuts; e++) {
                        ArrayList<Integer> a = new ArrayList<Integer>();
                        a.add(0);
                        holdIt.add(a);
                    }

                    // for the number of cases, going down the line for this attribute
                    for (int j = 0; j < numCases; j++) {
                        // real will allow us to get the original value of the case
                        int real = i + (j * (numAttributes + 1));
                        // for each cut want to go through and if its less you add it to that case
                        for (int c = 0; c < numCuts / 2; c++) {
                            if (Double.valueOf(contentArr[real]) <= cuts.get(i).get(c)) {
                                ArrayList<Integer> hold = new ArrayList<>();
                                hold = holdIt.get(c * 2);
                                hold.add(j + 1);
                                holdIt.set(c * 2, hold);
                            }
                        }
                    }

                    // fill in the odd cases because they are every case that wasnt in the privious
                    // one
                    for (int h = 1; h < numCuts; h = h + 2) {
                        ArrayList<Integer> holdAgain = new ArrayList<>();
                        ArrayList<Integer> allNums = new ArrayList<>();
                        for (int p = 1; p <= numCases; p++) {
                            allNums.add(p);
                        }

                        holdAgain = holdIt.get(h - 1);
                        // remove the placeholder 0
                        holdAgain.remove(0);

                        allNums.removeAll(holdAgain);

                        holdIt.set(h, allNums);
                    }
                    // add this to all
                    all.add(holdIt);
                }
            }
            // at this point all holds
            // [[[caseCut1], [caseCut3], ..., [lastCaseCut]],[interval2], ... , [lastinter]]

            // create a map of <[attribute, value], cases> where each value is now the new
            // range
            // at this point the cases are empty
            // Map<ArrayList<String>, ArrayList<Integer>> attValList = new HashMap<>();
            for (int i = 0; i < pleaseWork.size(); i++) {
                for (int j = 0; j < pleaseWork.get(i).size(); j++) {
                    TheInfo newOne = new TheInfo();

                    newOne.setAtt(attributes.get(i));
                    newOne.setVal(pleaseWork.get(i).get(j));
                    newOne.setCases(all.get(i).get(j));

                    theList.add(newOne);
                }
            }
        }
        ///////// END OF FOR CUTS////////////////////////////////////////
        // for each case
        for (int i = 0; i < numCases; i++) {
            for (int j = 0; j < numAttributes; j++) {
                final int real = j + (i * (numAttributes + 1));
                // only need to do this for the symbol attributes beacause already took care
                // of the numerical ones
                if (types.get(attributes.get(j)) == "string") {
                    // hold = {attribute, value}
                    TheInfo hold = new TheInfo();
                    // ArrayList<String> hold = new ArrayList<>();
                    hold.setAtt(attributes.get(j));
                    hold.setVal(contentArr[real]);
                    // hold.add(contentArr[real]);

                    // if the attribute, value pair is already in the map, just add the case
                    // to the cases that are already in there

                    if (theList.contains(hold)) {
                        int index = theList.indexOf(hold);
                        ArrayList<Integer> hold2 = theList.get(index).getCases();
                        hold2.add(i + 1);
                        theList.get(index).setCases(hold2);
                        // otherwise you are going to add it to the list and put in a new case list
                    } else {
                        ArrayList<Integer> hold2 = new ArrayList<>();
                        hold2.add(i + 1);
                        hold.setCases(hold2);
                        theList.add(hold);
                    }
                }
            }

            // now need to deal with the decisions, the decision is at the end of every case
            int dec = (i * (numAttributes + 1)) + numAttributes;
            // add decision, {case numbers} to that map
            TheInfo holdDec = new TheInfo();
            holdDec.setAtt("decision");
            holdDec.setVal(contentArr[dec]);

            if (decisionsList.contains(holdDec)) {
                int index = decisionsList.indexOf(holdDec);
                ArrayList<Integer> holdCases = decisionsList.get(index).getCases();
                holdCases.add(i + 1);
                decisionsList.get(index).setCases(holdCases);
            } else {
                ArrayList<Integer> holdCases = new ArrayList<>();
                holdCases.add(i + 1);
                holdDec.setCases(holdCases);
                decisionsList.add(holdDec);
            }
        }
        // at this point all the attribute, value pairs and their cases are in baseMap

        Collections.sort(theList, new Comparator<TheInfo>() {
            public int compare(final TheInfo o1, final TheInfo o2) {
                return o1.getAtt().compareToIgnoreCase(o2.getAtt());
            }
        });

        final ArrayList<ArrayList<TheInfo>> answer = getRuleSet();

        ////////////////////////////////////////////////////////////////
        //////////////////// PRINTING TO OUTPUT FILE////////////////////
        ///////////////////////////////////////////////////////////////
        // PrintStream out = new PrintStream(new File(outputFileName));
        // System.setOut(out);

        answer.forEach((ruleSet) -> {
            ArrayList<Integer> holdNums = getThreeNums(ruleSet);
            System.out.println(holdNums.get(0) + ", " + holdNums.get(1) + ", " + holdNums.get(2));
            ArrayList<Integer> holdCases = overlappingCases(getSetCases(ruleSet));
            // System.out.println("Rule: " + holdCases + ":") ;
            // System.out.println("*******************************");
            writeAnd = false;
            ruleSet.forEach((rule) -> {
                if (writeAnd == true) {
                    System.out.print(" & ");
                }
                rule.print();
                writeAnd = true;
            });
            TheInfo holdDec = getDecision(holdCases.get(0));
            System.out.println("-> (" + holdDec.getAtt() + ", " + holdDec.getVal() + ") " + holdCases);
        });
        System.out.print("\n");

        // out.close();
        ////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////
    }

    // first number is the number of conditions in the rule
    // second number is the number of cases the rule set covers (overlapping size)
    // third number is the number of cases that the rule set covers intersected with
    // the goal
    // --this is to check for consistency Im pretty sure
    public static ArrayList<Integer> getThreeNums(ArrayList<TheInfo> ruleSet) {
        ArrayList<Integer> holdNums = new ArrayList<Integer>();

        ArrayList<Integer> holdCases = overlappingCases(getSetCases(ruleSet));
        ArrayList<ArrayList<Integer>> checkConsis = new ArrayList<ArrayList<Integer>>();
        checkConsis.add(holdCases);
        checkConsis.add(getDecision(holdCases.get(0)).getCases());

        ArrayList<Integer> theCheck = overlappingCases(checkConsis);

        int first = ruleSet.size();
        int second = holdCases.size();
        int third = theCheck.size();

        holdNums.add(first);
        holdNums.add(second);
        holdNums.add(third);

        return holdNums;
    }

    // goes through and gets the value for the decsion for the
    // case number provided because the desionsList is a list of decsions
    // and the case numbers with that decison
    public static TheInfo getDecision(Integer theCase) {
        TheInfo answer2 = new TheInfo();
        for(int i=0; i<decisionsList.size(); i++){
            if(decisionsList.get(i).getCases().contains(theCase)){
                answer2 = decisionsList.get(i);
            }
        }
        return answer2;
    }

    //returns the list that holds the list of cuts for each numerical attribute
    //you pass in the list of values that each numerical attribtue has to calculate
    // where the cuts should be
    public static List<List<Double>> getJustCuts(List<List<Double>> lists) {
        List<List<Double>> holdCuts = new ArrayList<List<Double>>();
        //for the number of numerical attributes (this should be numNumAttributes)
        for (int i = 0; i < lists.size(); i++) {
            //for the number of values in each numberical attribtue (this should be numCases)
            for (int k = 0; k < lists.get(i).size() - 1; k++) {
                //going to hold the list of cuts for this attribute
                List<Double> holdIt = new ArrayList<>();
                //have to double traverse this list of attribtues to get 
                // the cut values, its average of each successive pair of numbers
                for (int p = k + 1; p < lists.get(i).size(); p++) {
                    Double num1 = lists.get(i).get(k);
                    Double num2 = lists.get(i).get(p);

                    Double cut = (num1 + num2) / 2;
                    holdIt.add(cut);
                    k = k + 1;
                }
                holdCuts.add(holdIt);
            }
        }
        return holdCuts;
    }

    //gets the list of all the {attribute, newInterval value} for each numberical attribtue
    public static ArrayList<ArrayList<String>> getCutValues(List<List<Double>> lists) {
        ArrayList<ArrayList<String>> cuts = new ArrayList<ArrayList<String>>();
        //holds the cuts for each numerical attribtue
        List<List<Double>> holdCuts = getJustCuts(lists);
        
        for (int i = 0; i < holdCuts.size(); i++) {
            Double num1 = lists.get(i).get(0);
            ArrayList<String> forAtt = new ArrayList<>();
            for (int j = 0; j < holdCuts.get(i).size(); j++) {
                String sCut1 = (Double.toString(num1) + ".." + Double.toString(holdCuts.get(i).get(j)));
                String sCut2 = (Double.toString(holdCuts.get(i).get(j))) + ".."
                        + Double.toString(lists.get(i).get(lists.get(i).size() - 1));

                forAtt.add(sCut1);
                forAtt.add(sCut2);
            }
            cuts.add(forAtt);
        }
        return cuts;
    }

    //returns a deep copy of a given ArrayList<TheInfo>
    public static ArrayList<TheInfo> dcListInfo(ArrayList<TheInfo> toCopy) {
        final ArrayList<TheInfo> cloneList = new ArrayList<>();

        for (int i = 0; i < toCopy.size(); i++) {
            cloneList.add(toCopy.get(i));
        }

        return cloneList;
    }

    // returns a deep copy of a given ArrayList<Integer>
    public static ArrayList<Integer> dcListInt(ArrayList<Integer> toCopy) {
        ArrayList<Integer> cloneList = new ArrayList<>();

        for (int i = 0; i < toCopy.size(); i++) {
            cloneList.add(toCopy.get(i));
        }

        return cloneList;
    }

    //returns the set of rules for the entire problem
    public static ArrayList<ArrayList<TheInfo>> getRuleSet() {
        // MAP OF DECISIONS NEEDS TO BE LIST INSTEAD OF THEINFO
        final ArrayList<ArrayList<TheInfo>> allRules = new ArrayList<ArrayList<TheInfo>>();
        // for each decision, so this is getting the rules per decision
        for (int d = 0; d < decisionsList.size(); d++) {
            //holds the cases for each decision, these are the goals
            ArrayList<Integer> holdRule = (dcListInt(decisionsList.get(d).getCases()));
            ArrayList<Integer> changingRule = dcListInt(holdRule);
            // ArrayList<Integer> dCases = decisionsList.get(d).getCases();
            // ArrayList<Integer> holdRule = new ArrayList<>();
            // ArrayList<Integer> tempCases = new ArrayList<>();
            // for (int i = 0; i < dCases.size(); i++) {
            //     holdRule.add(dCases.get(i));
            //     tempCases.add(dCases.get(i));
            // }

            ArrayList<TheInfo> holdList = dcListInfo(theList);
            // ArrayList<TheInfo> holdList = new ArrayList<TheInfo>();
            // for (int i = 0; i < theList.size(); i++) {
            //     holdList.add(theList.get(i));
            // }

            final ArrayList<TheInfo> holdARule = new ArrayList<TheInfo>();

            while (changingRule.size() != 0) {
                ArrayList<TheInfo> aRule = getOneRule(changingRule, holdRule, holdList, holdARule);

                aRule = checkForDrop(holdRule, aRule, 0);

                allRules.add(new ArrayList<TheInfo>(aRule));

                final ArrayList<ArrayList<Integer>> holdSet = getSetCases(aRule);
                final ArrayList<Integer> covered = overlappingCases(holdSet);
                changingRule = getNewGoal(changingRule, covered, false);
                holdList = new ArrayList<TheInfo>();
                for (int i = 0; i < theList.size(); i++) {
                    holdList.add(theList.get(i));
                }

                holdARule.clear();
                aRule.clear();
            }
        }
        return allRules;
    }

    // goal, the rule, the condition youre trying to drop
    public static ArrayList<TheInfo> checkForDrop(final ArrayList<Integer> goal, final ArrayList<TheInfo> oneRule,
            int condNum) {
        if (oneRule.size() == 1)
            return oneRule;
        for (int i = 0; i < oneRule.size(); i++) {
            final TheInfo holdCond = oneRule.get(condNum);
            oneRule.remove(condNum);
            if (isContained(goal, overlappingCases(getSetCases(oneRule)))) {
                return (checkForDrop(goal, oneRule, 0));
            } else {
                oneRule.add(0, holdCond);
                condNum++;
            }
        }
        return oneRule;
    }

    public static ArrayList<ArrayList<Integer>> getSetCases(final ArrayList<TheInfo> rules) {
        final ArrayList<ArrayList<Integer>> answer = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < rules.size(); i++) {
            answer.add(rules.get(i).getCases());
        }
        return answer;
    }

    public static ArrayList<Integer> getNewGoal(final ArrayList<Integer> oldGoal, final ArrayList<Integer> covered,
            final boolean intersection) {
        final ArrayList<Integer> hold = new ArrayList<>();
        for (int i = 0; i < oldGoal.size(); i++) {
            hold.add(oldGoal.get(i));
        }

        if (intersection == false) {
            for (int i = 0; i < covered.size(); i++) {
                hold.remove(covered.get(i));
            }
        } else {
            hold.retainAll(covered);
        }
        return hold;
    }

    public static ArrayList<TheInfo> getOneRule(final ArrayList<Integer> goal, final ArrayList<Integer> holdGoal,
            final ArrayList<TheInfo> ongoingList, final ArrayList<TheInfo> ruleCond) {

        if (goal.size() == 0) {
            return ruleCond;
        }

        pick = getPick(goal, ongoingList);
        final ArrayList<Integer> casesCovered = pick.getCases();
        ruleCond.add(pick);

        for (int i = 0; i < ongoingList.size(); i++) {
            if (ongoingList.get(i).getAtt() == pick.getAtt() && ongoingList.get(i).getVal() == pick.getVal()) {
                ongoingList.remove(i);
                break;
            }
        }

        final ArrayList<ArrayList<Integer>> hold = getSetCases(ruleCond);
        if (isContained(holdGoal, overlappingCases(hold))) {
            return ruleCond;
        }

        return (getOneRule(getNewGoal(goal, casesCovered, true), holdGoal, ongoingList, ruleCond));
    }

    public static ArrayList<Integer> overlappingCases(final ArrayList<ArrayList<Integer>> ruleSet) {

        final ArrayList<ArrayList<Integer>> holdIt = new ArrayList<ArrayList<Integer>>();

        for (int i = 0; i < ruleSet.size(); i++) {
            final ArrayList<Integer> holdInside = new ArrayList<Integer>();
            for (int j = 0; j < ruleSet.get(i).size(); j++) {
                holdInside.add(ruleSet.get(i).get(j));
            }
            holdIt.add(holdInside);
        }

        if (holdIt.size() == 0) {
            return null;
        }
        if (holdIt.size() == 1) {
            return holdIt.get(0);
        } else {
            final ArrayList<Integer> hold1 = holdIt.get(0);
            final ArrayList<Integer> hold2 = holdIt.get(1);
            hold1.retainAll(hold2);
            holdIt.remove(1);
            holdIt.set(0, hold1);
            return (overlappingCases(holdIt));
        }
    }

    public static Boolean isContained(final ArrayList<Integer> goal, final ArrayList<Integer> overCases) {
        for (int i = 0; i < overCases.size(); i++) {
            if (!goal.contains(overCases.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static TheInfo getPick(final ArrayList<Integer> goal, final ArrayList<TheInfo> checkList) {
        final Map<Integer, ArrayList<Integer>> overCases = new HashMap<>();
        // object#, #overcases
        final Map<Integer, Integer> check1 = new HashMap<>();
        final Map<Integer, Integer> check2 = new HashMap<>();
        checkList.forEach((obj) -> {
            final ArrayList<Integer> hold = obj.overlappingCases(goal);
            overCases.put(num, hold);
            check1.put(num, hold.size());
            num++;
        });
        num = 0;

        final Map<Integer, Integer> sorted1 = check1.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        final Integer firstKey = sorted1.keySet().iterator().next();
        final Integer firstValue = sorted1.get(firstKey);
        sorted1.remove(firstKey);

        final Integer secondKey = sorted1.keySet().iterator().next();

        if (firstValue != check1.get(secondKey)) {
            return (checkList.get(firstKey));
        } else {
            check2.put(firstKey, checkList.get(firstKey).getNumCases());
            check2.put(secondKey, checkList.get(secondKey).getNumCases());

            sorted1.remove(secondKey);

            while (sorted1.values().iterator().hasNext() && sorted1.values().iterator().next() == firstValue) {
                final int hold = sorted1.keySet().iterator().next();
                check2.put(hold, checkList.get(hold).getNumCases());
                sorted1.remove(hold);
            }
            final Map<Integer, Integer> sorted2 = check2.entrySet().stream().sorted(comparingByValue())
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

            // System.out.println("SORTED2: " + sorted2);
            return (checkList.get(sorted2.keySet().iterator().next()));
        }
    }
}

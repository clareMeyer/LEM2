import java.util.*;

import java.io.*;
import java.nio.file.*;
import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

public class LemTwo {

    String attVal[] = new String[2];
    //Map<[attribute, value], {cases}>
    static Map<ArrayList<String>, ArrayList<Integer>> baseMap= new HashMap<>();
    //Map<decision, {cases}>
    static Map<String, ArrayList<Integer>> decisions = new HashMap<>();
    static List<TheInfo> decisionsList = new ArrayList<>();

    static List<String> attributes = new ArrayList<>();
    static String decisionName;

    static ArrayList<TheInfo> theList = new ArrayList<TheInfo>();

    static int num = 0;
    static TheInfo pick;

    static ArrayList<TheInfo> ruleConditions = new ArrayList<>();
    static ArrayList<ArrayList<Integer>> ruleCases = new ArrayList<ArrayList<Integer>>();
    static ArrayList<Integer> casesCovered = new ArrayList<Integer>();

    public static void main(String[] args) throws IOException {

        Scanner getInfo = new Scanner(System.in);
        System.out.print("Enter file name: ");
        String fileName = getInfo.nextLine();
        System.out.println("The fileName: " + fileName);
        getInfo.close();

        int numAttributes = 0;
        String delim = "[\\n\\t ]";

        String content = Files.readString(Paths.get(fileName));
        content.replaceAll("!.*?!", "");

        String contentArr[] = content.split(delim);

        int count = 1;
        while(contentArr[count].contains("a")){
            numAttributes += 1;
            count +=1;
        }
        count += 3;
        for(int i=0; i<numAttributes; i++){
            attributes.add(contentArr[count+i]);
        }
        count += numAttributes;
        decisionName = contentArr[count];

        System.out.println("Num attributes: " + numAttributes);
        System.out.println("attributes: " + attributes);
        System.out.println("decisionName: " + decisionName);

        count += 1;
        System.out.println("count: " + count + " " + contentArr[count]);

        count++;
        contentArr = Arrays.copyOfRange(contentArr, count, contentArr.length);

        int numCases = contentArr.length/(numAttributes+1);
        System.out.print(numCases);
        for(int i=0; i<numCases; i++){
            for(int j=0; j<numAttributes; j++){
                int real = j + (i*(numAttributes+1));
                ArrayList<String> hold = new ArrayList<>();
                hold.add(attributes.get(j));
                hold.add(contentArr[real]);

                // add [attribute, value], {case numbers} to the map
                if (baseMap.containsKey(hold)) {
                    ArrayList<Integer> hold2 = baseMap.get(hold);
                    hold2.add(i+1);
                    baseMap.put(hold, hold2);
                } else {
                    ArrayList<Integer> hold2 = new ArrayList<>();
                    hold2.add(i+1);
                    baseMap.put(hold, hold2);
                }
            }
            int dec = (i*(numAttributes+1)) + numAttributes;
            // add decision, {case numbers} to that map
            if (decisions.containsKey(contentArr[dec])) {
                ArrayList<Integer> holdCases = decisions.get(contentArr[dec]);
                holdCases.add(i+1);
                decisions.put(contentArr[dec], holdCases);
            } else {
                ArrayList<Integer> holdCases = new ArrayList<>();
                holdCases.add(i+1);
                decisions.put(contentArr[dec], holdCases);
            }
        }

        baseMap.forEach((attributeVal, cases) -> {
            theList.add(new TheInfo(attributeVal.get(0), attributeVal.get(1), cases));
        });

        Collections.sort(theList, new Comparator<TheInfo>() {
            public int compare(TheInfo o1, TheInfo o2) {
                return o1.getAtt().compareToIgnoreCase(o2.getAtt());
            }
        });

        decisions.forEach((attributeVal, cases) -> {
            decisionsList.add(new TheInfo("decision", attributeVal, cases));
        });

        for(int i=0; i<theList.size(); i++){
            theList.get(i).print();
            System.out.print("\n");
        }

 
        ArrayList<ArrayList<TheInfo>> answer = getRuleSet();
            answer.forEach((ruleSet) -> {
                System.out.println("Rule: " + overlappingCases(getSetCases(ruleSet)) + ":") ;
                System.out.println("*******************************");
                ruleSet.forEach((rule) -> {
                    rule.print();
                });
            });
            System.out.print("\n");
    }
    public static ArrayList<TheInfo> deepCopy(ArrayList<TheInfo> toCopy){
        ArrayList<TheInfo> clone = new ArrayList<>();

       for(int i=0; i<toCopy.size(); i++){
            clone.add((TheInfo)toCopy.get(i).clone());
        }

        return clone;
    }

    public static ArrayList<ArrayList<TheInfo>> getRuleSet(){
        //MAP OF DECISIONS NEEDS TO BE LIST INSTEAD OF THEINFO
        ArrayList<ArrayList<TheInfo>> allRules = new ArrayList<ArrayList<TheInfo>>();
        //for each decision 
        for(int d=0; d<decisionsList.size(); d++){
            decisionsList.get(d).print();
            ArrayList<Integer> dCases = decisionsList.get(d).getCases();
            ArrayList<Integer> holdRule = new ArrayList<>();
            ArrayList<Integer> tempCases = new ArrayList<>();
            for(int i=0; i<dCases.size(); i++){
                holdRule.add(dCases.get(i));
                tempCases.add(dCases.get(i));
            }

            ArrayList<TheInfo> holdList = new ArrayList<TheInfo>();
            for(int i=0; i<theList.size(); i++){
                holdList.add(theList.get(i));
            }
     
            ArrayList<TheInfo> holdARule = new ArrayList<TheInfo>();

            while(tempCases.size()!=0){
                ArrayList<TheInfo> aRule = getOneRule(tempCases, holdRule, holdList, holdARule);

                // System.out.println(tempCases);
                // aRule.get(0).print();

                aRule = checkForDrop(holdRule, aRule, 0);

                allRules.add(new ArrayList<TheInfo>(aRule));

                ArrayList<ArrayList<Integer>> holdSet = getSetCases(aRule);
                ArrayList<Integer> covered = overlappingCases(holdSet);
                tempCases = getNewGoal(tempCases, covered, false);
                holdList = new ArrayList<TheInfo>();
                for (int i = 0; i < theList.size(); i++) {
                    holdList.add(theList.get(i));
                }

                holdARule.clear();
                aRule.clear();
            }

            // completeRules.put(decisionsList.get(d), allRules);
        }
        return allRules;
    }

    //goal, the rule, the condition youre trying to drop
    public static ArrayList<TheInfo> checkForDrop(ArrayList<Integer> goal, ArrayList<TheInfo> oneRule, int condNum){
        if(oneRule.size() == 1) return oneRule;
        for(int i=0; i<oneRule.size(); i++){
            TheInfo holdCond = oneRule.get(condNum);
            oneRule.remove(condNum);
            if(isContained(goal, overlappingCases(getSetCases(oneRule)))){
                return (checkForDrop(goal, oneRule, 0));
            } else {
                oneRule.add(0, holdCond);
                condNum++;
            }
        }
        return oneRule;
    }

    public static ArrayList<ArrayList<Integer>> getSetCases(ArrayList<TheInfo> rules){
        ArrayList<ArrayList<Integer>> answer = new ArrayList<ArrayList<Integer>>();
        for(int i=0; i<rules.size(); i++){
            answer.add(rules.get(i).getCases());
        }
        return answer;
    }

    public static ArrayList<Integer> getNewGoal(ArrayList<Integer> oldGoal, ArrayList<Integer> covered, boolean intersection){
        ArrayList<Integer> hold = new ArrayList<>();
        for(int i=0; i<oldGoal.size(); i++){
            hold.add(oldGoal.get(i));
        }

        if(intersection == false){
            for (int i = 0; i < covered.size(); i++) {
                hold.remove(covered.get(i));
            }
        }
        else {
            hold.retainAll(covered);
        }
        return hold;
    }

    public static ArrayList<TheInfo> getOneRule(ArrayList<Integer> goal, ArrayList<Integer> holdGoal, ArrayList<TheInfo> ongoingList, 
                                                    ArrayList<TheInfo> ruleCond){
        

        System.out.println("GOAL: " + goal);                                                
        if(goal.size() == 0){
            return ruleCond;
        }             
                                           
        pick = getPick(goal, ongoingList);
        System.out.print("Pick: ");
        pick.print();
        ArrayList<Integer> casesCovered = pick.getCases();
        ruleCond.add(pick);

        for(int i=0; i<ongoingList.size(); i++){
            if(ongoingList.get(i).getAtt() == pick.getAtt() && ongoingList.get(i).getVal() == pick.getVal()){
                ongoingList.remove(i);
                break;
            }
        }

        ArrayList<ArrayList<Integer>> hold = getSetCases(ruleCond);
        System.out.println("OverlappingCases: " + overlappingCases(hold));
        if(isContained(holdGoal, overlappingCases(hold))){
            return ruleCond;
        }

        System.out.println("THISITHSIHT:SIHTISHTISHTISTHISTH");
        return(getOneRule(getNewGoal(goal, casesCovered, true), holdGoal, ongoingList, ruleCond));
    }

    public static ArrayList<Integer> overlappingCases(ArrayList<ArrayList<Integer>> ruleSet) {

        ArrayList<ArrayList<Integer>> holdIt = new ArrayList<ArrayList<Integer>>();
        
        for(int i=0; i<ruleSet.size(); i++){
            ArrayList<Integer> holdInside = new ArrayList<Integer>();
            for(int j=0; j<ruleSet.get(i).size(); j++){
                holdInside.add(ruleSet.get(i).get(j));
            }
            holdIt.add(holdInside);
        }

        if(holdIt.size() == 0){
            return null;
        }
        if(holdIt.size() == 1){
            return holdIt.get(0);
        } else {
            ArrayList<Integer> hold1 = holdIt.get(0);
            ArrayList<Integer> hold2 = holdIt.get(1);
            hold1.retainAll(hold2);
            holdIt.remove(1);
            holdIt.set(0, hold1);
            return(overlappingCases(holdIt));
        }
    }

    public static Boolean isContained(ArrayList<Integer> goal, ArrayList<Integer> overCases){
        for(int i=0; i<overCases.size(); i++){
            if(!goal.contains(overCases.get(i))){
                return false;
            }
        }
        return true;
    }

    public static TheInfo getPick(ArrayList<Integer> goal, ArrayList<TheInfo> checkList){
        Map<Integer, ArrayList<Integer>> overCases = new HashMap<>();
        //object#, #overcases
        Map<Integer, Integer> check1 = new HashMap<>();
        Map<Integer, Integer> check2 = new HashMap<>();
        checkList.forEach((obj) -> {
            ArrayList<Integer> hold = obj.overlappingCases(goal);
            // System.out.println("HOLD: " + hold);
            overCases.put(num, hold);
            check1.put(num, hold.size());
            num++;
        });
        num = 0;


        Map<Integer, Integer> sorted1 = check1
            .entrySet()
            .stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .collect(
                toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new)
            );
        
        // System.out.println(sorted1);

        Integer firstKey = sorted1.keySet().iterator().next();
        Integer firstValue = sorted1.get(firstKey);
        sorted1.remove(firstKey);

        Integer secondKey = sorted1.keySet().iterator().next();

        if(firstValue != check1.get(secondKey)){
            return(checkList.get(firstKey));
        } else {
            check2.put(firstKey, checkList.get(firstKey).getNumCases());
            check2.put(secondKey, checkList.get(secondKey).getNumCases());
            
            sorted1.remove(secondKey);
            
            // System.out.println("SORTED1: " + sorted1);

            while(sorted1.values().iterator().hasNext() && sorted1.values().iterator().next() == firstValue){
                // System.out.println("HERE");
                int hold = sorted1.keySet().iterator().next();
                check2.put(hold, checkList.get(hold).getNumCases());
                sorted1.remove(hold);
            }
            Map<Integer, Integer> sorted2 = check2.entrySet().stream()
                    .sorted(comparingByValue())
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
            
            // System.out.println("SORTED2: " + sorted2);
            return(checkList.get(sorted2.keySet().iterator().next()));
        }
    }
}
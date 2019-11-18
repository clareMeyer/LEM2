import java.util.*;

// import jdk.internal.jline.internal.InputStreamReader;

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

    static List<String> attributes = new ArrayList<>();
    static String decisionName;

    static ArrayList<TheInfo> theList = new ArrayList<TheInfo>();

    static int num = 0;
    static TheInfo pick;

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
        System.out.println("Decisions: " + decisions);

        baseMap.forEach((attributeVal, cases) -> {
            theList.add(new TheInfo(attributeVal.get(0), attributeVal.get(1), cases));
        });

        Collections.sort(theList, new Comparator<TheInfo>() {
            public int compare(TheInfo o1, TheInfo o2) {
                return o1.getAtt().compareToIgnoreCase(o2.getAtt());
            }
        });

        for(int i=0; i<theList.size(); i++){
            theList.get(i).print();
            System.out.print("\n");
        }



        while(decisions.size() != 0){
            String decisionName = decisions.keySet().iterator().next();
            ArrayList<Integer> decisionCases = decisions.get(decisionName);

            System.out.println("Decison: " + decisionName);
            ArrayList<TheInfo> hold = getRule(decisionCases);
            for (int i = 0; i < hold.size(); i++) {
                hold.get(i).print();
            }

            decisions.remove(decisionName);
        }

        //need to do check now like after first rule what cases left
        // getPick(decisions.get("positive"), theList).print();
        // getPick(decisions.get("negative"), theList).print();
    }

    public static ArrayList<TheInfo> getRule(ArrayList<Integer> goal){
        // ArrayList<Integer> pick = getPick(goal)
        System.out.println("THE NEW GOAL: " + goal);
        ArrayList<TheInfo> ongoingList = theList;
        pick = getPick(goal, ongoingList);
        ArrayList<Integer> cases = pick.getCases();
        ArrayList<TheInfo> picks = new ArrayList<>();
        picks.add(pick);
        while(!isContained(goal, cases)){
            Iterator<TheInfo> iter = ongoingList.iterator();
            while(iter.hasNext()){
                TheInfo hold = iter.next();
                if (hold.getAtt() == pick.getAtt() && hold.getVal() == pick.getVal()) {
                    iter.remove();
                }
            }
            pick = getPick(goal, ongoingList);
            picks.add(pick);

            cases = overlappingCases(cases, pick.getCases());
            // cases = pick.getCases();
            // picks.add(pick);
        }
        pick = new TheInfo();
        return picks;
    }

    public static ArrayList<Integer> overlappingCases(ArrayList<Integer> cases1, ArrayList<Integer> cases2) {
        ArrayList<Integer> aMatch = new ArrayList<>();
        ArrayList<Integer> first;
        ArrayList<Integer> second;
        int len1 = cases1.size();
        int len2 = cases2.size();
        int max = Math.max(len1, len2);
        if(max == len1){
            first = cases1;
            second = cases2;
        } else {
            first = cases2;
            second = cases1;
        }
        for (int i = 0; i < max; i++) {
            int hold = first.get(i);
            if (second.contains(hold)) {
                aMatch.add(hold);
            }
        }
        return aMatch;
    }

    public static Boolean isContained(ArrayList<Integer> goal, ArrayList<Integer> cases){
        for(int i=0; i<cases.size(); i++){
            if(!goal.contains(cases.get(i))){
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
        
        // System.out.print(sorted1);

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
            
            System.out.println(firstValue + " " + sorted1.values().iterator().next());
            while(sorted1.values().iterator().next() == firstValue){
                int hold = sorted1.keySet().iterator().next();
                check2.put(hold, checkList.get(hold).getNumCases());
                sorted1.remove(hold);
            }
            Map<Integer, Integer> sorted2 = check2.entrySet().stream()
                    .sorted(comparingByValue())
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
            
            System.out.println("sorted2: " + sorted2);

            return(checkList.get(sorted2.keySet().iterator().next()));
        }
    }
}
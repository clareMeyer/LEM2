import java.util.*;

// import jdk.internal.jline.internal.InputStreamReader;

// import java.io.*;
// import java.nio.file.*;

public class TheInfo {
    int numCases;
    String m_att;
    String m_val;
    ArrayList<Integer> m_cases;

    TheInfo() {

    }

    TheInfo(String attribute, String value, ArrayList<Integer> cases) {
        m_att = attribute;
        m_val = value;
        m_cases = cases;
        numCases = cases.size();
    }

    public void setInfo(String attribute, String value, ArrayList<Integer> cases) {
        m_att = attribute;
        m_val = value;
        m_cases = cases;
        numCases = cases.size();
    }

    public String getAtt() {
        return m_att;
    }

    public String getVal() {
        return m_val;
    }

    public ArrayList<Integer> getCases() {
        return m_cases;
    }

    public int getNumCases() {
        return numCases;
    }

    public ArrayList<Integer> overlappingCases(ArrayList<Integer> goal) {
        ArrayList<Integer> aMatch = new ArrayList<>();
        for (int i = 0; i < m_cases.size(); i++) {
            int hold = m_cases.get(i);
            if (goal.contains(hold)) {
                aMatch.add(hold);
            }
        }
        return aMatch;
    }

    public void print() {
        System.out.print("(" + m_att + ", " + m_val + ")" );
    }

    public Object clone() {
        TheInfo aClone = new TheInfo();
        try {
            aClone = (TheInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return aClone;
    }
}
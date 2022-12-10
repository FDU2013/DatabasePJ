package sonar;

import java.util.ArrayList;
import java.util.List;

public class test {
    public static void main(String[] args) {
        List<String> aa = new ArrayList<>();
        aa.add("sas");
        aa.add("sas");
        aa.add("sas");
        String x = aa.get(1);
        x = "sad";
        for(String s : aa){
            s = "s";
            System.out.println(s);
        }
        for(String s : aa){
            System.out.println(s);
        }
    }
}

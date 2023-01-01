package sonar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SonarScanner {
    private static final String command = "sonar-scanner -D sonar.projectKey=";

    public static void ScanRepo(String path, String id)  {
//        try {
//            String cmd = "cmd /c cd " + path +  " && " + command + id;
//            System.out.println(cmd);
//            //Runtime.getRuntime().exec("e:");
//            //Runtime.getRuntime().exec("cd " + path);
//            Process process = Runtime.getRuntime().exec(cmd);
//            //Runtime.getRuntime().exec("cd " + path);
//            //Process process = Runtime.getRuntime().exec(command + id);
//            process.waitFor(40, TimeUnit.SECONDS);
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
        Scanner input = null;
        StringBuilder result = new StringBuilder();
        Process process = null;
        String cmd = "cmd /c cd " + path +  " && " + command + id;
        System.out.println("In scanning.Waiting...");
        try {
            process = Runtime.getRuntime().exec(cmd);
            //process.waitFor(20, TimeUnit.SECONDS);
            InputStream is = process.getInputStream();
            input = new Scanner(is);
            while (input.hasNextLine()) {
                result.append(input.nextLine()).append("\n");
            }
            result.insert(0, "Executed command: " + cmd + "\nOutput info:\n");
        } catch (IOException  e) {
            System.out.println("In scanning.Waiting.1..");
            e.printStackTrace();
        } finally {
            if (input != null) {
                input.close();
            }
            if (process != null) {
                process.destroy();
            }
        }
        System.out.println(result);;
    }

//    public static void main(String[] args) {
//        //test:
//        ScanRepo("C:\\MyMine\\CS_SE\\Projects\\SonarTest", "okoko");
//    }
}

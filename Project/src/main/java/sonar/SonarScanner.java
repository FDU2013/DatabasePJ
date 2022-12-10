package sonar;

import java.io.IOException;
import java.io.InputStream;
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
//        StringBuilder result = new StringBuilder();
        Process process = null;
        String cmd = "cmd /c cd " + path +  " && " + command + id;
        System.out.println(cmd);
        try {
            process = Runtime.getRuntime().exec(cmd);
            assert process != null;
            process.waitFor(40, TimeUnit.SECONDS);

            InputStream is = process.getInputStream();
            input = new Scanner(is);
//            while (input.hasNextLine()) {
//                result.append(input.nextLine()).append("\n");
//            }
//            result.insert(0, cmd + "\n"); //加上命令本身，打印出来
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                input.close();
            }
            if (process != null) {
                process.destroy();
            }
        }
        //System.out.println(result);;
    }
}

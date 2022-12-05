package sonar;

import java.io.IOException;

public class Scanner {
    private static final String command = "sonar-scanner -D sonar.projectKey=";

    private void ScanRepo(String path, String repoName, String commit)  {
        try {
            Runtime.getRuntime().exec("cd " + path);
            Runtime.getRuntime().exec(command + repoName + "_" + commit);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

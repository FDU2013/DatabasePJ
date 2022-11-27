import cn.edu.fudan.issue.entity.dbo.Location;
import cn.edu.fudan.issue.entity.dbo.RawIssue;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class IssueUtil {

    private static final String SEARCH_API = "http://127.0.0.1:9000/api/issues/search";
    private static final String AUTHORIZATION = "Basic YWRtaW46MTIzNDU2Nzg=";
    static List <RawIssue> resultRawIssues;

    public static JSONObject getSonarIssueResults(String componentKeys) throws IOException {

        String url_s = SEARCH_API + "?" + "componentKeys=" + componentKeys;
        URL url = new URL(url_s);

        URLConnection connection = url.openConnection();

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(15000);

        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        connection.setRequestProperty("authorization", AUTHORIZATION);

        connection.connect();

        //读返回值
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = in.readLine()) != null) {
            result.append(line);
        }
        in.close();

        return JSONObject.parseObject(result.toString());
    }

    public static List<Location> getLocations(JSONObject issue, String repoPath) throws Exception {
        int startLine = 0;
        int endLine = 0;
        String filePath = null;

        String sonarPath;
        String[] sonarComponents;
        List<Location> locations = new ArrayList<>();
        JSONArray flows = issue.getJSONArray("flows");
        if (flows.size() == 0) {
            JSONObject textRange = issue.getJSONObject("textRange");
            if (textRange != null) {
                startLine = textRange.getIntValue("startLine");
                endLine = textRange.getIntValue("endLine");
            } else {
                return new ArrayList<>();
            }

            sonarPath = issue.getString("component");
            if (sonarPath != null) {
                sonarComponents = sonarPath.split(":");
                if (sonarComponents.length >= 2) {
                    filePath = sonarComponents[sonarComponents.length - 1];
                }
            }

            Location mainLocation = new Location(startLine, endLine, filePath, repoPath);
            locations.add(mainLocation);
        }
        else {
            for (int i = 0; i < flows.size(); i++) {
                JSONObject flow = flows.getJSONObject(i);
                JSONArray flowLocations = flow.getJSONArray("locations");
                for (int j = 0; j < flowLocations.size(); j++) {
                    JSONObject flowLocation = flowLocations.getJSONObject(j);

                    String flowComponent = flowLocation.getString("component");
                    JSONObject flowTextRange = flowLocation.getJSONObject("textRange");
                    if (flowTextRange == null || flowComponent == null) {
                        continue;
                    }

                    int flowStartLine = flowTextRange.getIntValue("startLine");
                    int flowEndLine = flowTextRange.getIntValue("endLine");

                    String flowFilePath = null;
                    String[] flowComponents = flowComponent.split(":");
                    if (flowComponents.length >= 2) {
                        flowFilePath = flowComponents[flowComponents.length - 1];
                    }

                    Location location = new Location(flowStartLine, flowEndLine, flowFilePath, repoPath);
                    locations.add(location);
                }
            }
        }

        return locations;
    }

    private static boolean getSonarResult(String repoUuid, String commit, String repoPath, String key, String jgitRepoPath, String preFixPath) {
        //获取issue数量
        try {
            JSONArray sonarRawIssues = getSonarIssueResults(repoUuid + "_" + commit + key).getJSONArray("issues");
            System.out.println(sonarRawIssues);
            for (int j = 0; j < sonarRawIssues.size(); j++) {
                JSONObject sonarIssue = sonarRawIssues.getJSONObject(j);

                //仅解析java文件且非test文件夹
//                String component = sonarIssue.getString("component");
//                if (FileFilter.javaFilenameFilter(component)) {
//                    continue;
//                }

                //解析location
                List<Location> locations = getLocations(sonarIssue, repoPath);
                locations.forEach(location->System.out.println(location.getStartLine()));
                if (locations.isEmpty()) {
                    continue;
                }

                //解析rawIssue
//                RawIssue rawIssue = RawIssue(repoUuid, commit, ToolEnum.SONAR.getType(), sonarIssue, repoPath, jgitRepoPath, preFixPath);
                RawIssue rawIssue = new RawIssue(commit);
//                locations.forEach(location -> location.setFilePath(rawIssue.getFileName()));
                rawIssue.setLocations(locations);
//                rawIssue.setStatus(RawIssueStatus.ADD.getType());
                String rawIssueUuid = RawIssue.generateRawIssueUUID(rawIssue);
                rawIssue.setUuid(rawIssueUuid);
//                locations.forEach(location -> location.setRawIssueId(rawIssueUuid));


                resultRawIssues.add(rawIssue);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }





    public static void main(String[] args) throws IOException {
        getSonarResult("a", "a", "", "a", "", "");
        resultRawIssues.forEach(System.out::println);
    }

}

package scanner;

import cn.edu.fudan.issue.entity.dbo.Location;
import cn.edu.fudan.issue.entity.dbo.RawIssue;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class IssueUtil {

    private static final String SEARCH_API_URL = "http://127.0.0.1:9000/api/issues/search";
    private static final String AUTHORIZATION = "Basic YWRtaW46MTIzNDU=";
    private List <RawIssue> resultRawIssues;

    public JSONObject getSonarIssueResults(String id) throws IOException {
        URL url = new URL(SEARCH_API_URL + "?componentKeys=" + id + "&additionalFields=_all&s=FILE_LINE&resolved=false");

        URLConnection connection = url.openConnection();

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        connection.setRequestProperty("authorization", AUTHORIZATION);
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 Edg/107.0.1418.56");

        connection.connect();

        //读返回值
        BufferedReader resp = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String temp_line;
        StringBuilder result = new StringBuilder();
        while ((temp_line = resp.readLine()) != null) {
            result.append(temp_line);

        }
        resp.close();

        return JSONObject.parseObject(result.toString());
    }

    private boolean getSonarResult(String repoUuid, String commit, String repoPath) throws IOException {
        //获取issue数量
        try {
            JSONArray sonarRawIssues = getSonarIssueResults(repoUuid + "_" + commit).getJSONArray("issues");
            //解析sonar的issues为平台的rawIssue
            for (int j = 0; j < sonarRawIssues.size(); j++) {
                JSONObject sonarIssue = sonarRawIssues.getJSONObject(j);

                //解析location
                List<Location> locations = getLocations(sonarIssue, repoPath);
                if (locations.isEmpty()) {
                    continue;
                }
                //解析rawIssue
                RawIssue rawIssue = getRawIssue(repoUuid, commit, getType(), sonarIssue, repoPath);
                locations.forEach(location -> location.setFilePath(rawIssue.getFileName()));
                rawIssue.setLocations(locations);
                String rawIssueUuid = RawIssue.generateRawIssueUUID(rawIssue);
                rawIssue.setUuid(rawIssueUuid);
                locations.forEach(location -> location.setRawIssueId(rawIssueUuid));
                resultRawIssues.add(rawIssue);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Location> getLocations(JSONObject issue, String repoPath) throws Exception {
        int startLine = 0;
        int endLine = 0;
        String sonarPath;
        String[] sonarComponents;
        String filePath = null;
        List<Location> locations = new ArrayList<>();
        JSONArray flows = issue.getJSONArray("flows");
        if (flows.size() == 0) {
            //第一种针对issue中的textRange存储location
            JSONObject textRange = issue.getJSONObject("textRange");
            if (textRange != null) {
                startLine = textRange.getIntValue("startLine");
                endLine = textRange.getIntValue("endLine");
            } else {
                // 无 location 行号信息的 issue 过滤掉
                return new ArrayList<>();
            }

            sonarPath = issue.getString("component");
            if (sonarPath != null) {
                sonarComponents = sonarPath.split(":");
                if (sonarComponents.length >= 2) {
                    filePath = sonarComponents[sonarComponents.length - 1];
                }
            }

            Location mainLocation = getLocation(startLine, endLine, filePath, repoPath);
            locations.add(mainLocation);
        } else {
            //第二种针对issue中的flows中的所有location存储
            for (int i = 0; i < flows.size(); i++) {
                JSONObject flow = flows.getJSONObject(i);
                JSONArray flowLocations = flow.getJSONArray("locations");
                //一个flows里面有多个locations， locations是一个数组，目前看sonar的结果每个locations都是一个location，但是不排除有多个。
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

                    Location location = getLocation(flowStartLine, flowEndLine, flowFilePath, repoPath);
                    locations.add(location);
                }
            }
        }

        return locations;
    }

    static Location getLocation(int startLine, int endLine, String filePath, String repoPath){
        Location location = new Location();
        location.setStartLine(startLine);
        location.setEndLine(endLine);
        location.setStartToken(0);
        location.setFilePath(filePath);
        return location;
    }

    static RawIssue getRawIssue(String repoUuid, String commit, String type, String repoPath){
        RawIssue rawIssue = new RawIssue();
        rawIssue.setUuid(repoUuid);
        rawIssue.setType(type);
        rawIssue.setFileName("src/main/resources/testFile/commit1/MatchFailed.java");
        rawIssue.setDetail("Cast one of the operands of this multiplication operation to a \"long\".---MINOR");
        rawIssue.setCommitId("commit1");
        return rawIssue;
    }
}
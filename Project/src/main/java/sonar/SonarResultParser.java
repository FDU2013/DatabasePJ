package sonar;

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


public class SonarResultParser {

    private static final String SEARCH_API_URL = "http://127.0.0.1:9000/api/issues/search";
    private static final String AUTHORIZATION = "Basic YWRtaW46MTIzNA==";

    private List<RawIssue> resultRawIssues;

    public  SonarResultParser(){
        resultRawIssues = new ArrayList<>();
    }

    public List<RawIssue> getResultRawIssues() {
        return resultRawIssues;
    }

    public static void main(String[] args) {
        //test:
        try {
            JSONObject json = getSonarIssueResults("fg_main_b8024d988226df0b529a7566d905f4e67ee3aa31", 1 );
            System.out.println(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject getSonarIssueResults(String id, int page) throws IOException {
        URL url = new URL(SEARCH_API_URL + "?componentKeys=" + id + "&additionalFields=_all&s=FILE_LINE&resolved=false&p=" + page);
        //System.out.println(SEARCH_API_URL + "?componentKeys=" + id + "&p=" + page);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        connection.setRequestProperty("authorization", AUTHORIZATION);
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("User-Agent","PostmanRuntime/7.29.0");
        connection.connect();

        //读返回值
        BufferedReader resp = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String temp_line;
        StringBuilder result = new StringBuilder();
        while ((temp_line = resp.readLine()) != null) {
            result.append(temp_line);

        }
        resp.close();
        //System.out.println(result);
        return JSONObject.parseObject(result.toString());
    }

    public boolean getSonarResult(String repoUuid, String branchName, String commit) {
        //获取issue数量
        try {
            JSONObject json = getSonarIssueResults(repoUuid + "_" + branchName + "_" + commit, 1);
            //System.out.println(repoUuid + "_" + branchName + "_" + commit);
            //System.out.println(json.toString());
            int pageSize = 100;
            int issueTotal = json.getIntValue("total");
            //分页取sonar的issue
            int maxPage = issueTotal % pageSize > 0 ? issueTotal / pageSize + 1 : issueTotal / pageSize;
            System.out.println("issue total "+issueTotal +"  maxPage "+ maxPage);
            //解析sonar的issues为平台的rawIssue
            //System.out.println("issue size:"+ sonarRawIssues.size());
            for(int i = 1; i <= maxPage; i++) {
                JSONArray sonarRawIssues = getSonarIssueResults(repoUuid + "_" + branchName + "_" + commit, i).getJSONArray("issues");
                //System.out.println("issue json size "+sonarRawIssues.size());
                for (int j = 0; j < sonarRawIssues.size(); j++) {
                    JSONObject sonarIssue = sonarRawIssues.getJSONObject(j);
                    //解析location
                    List<Location> locations = getLocations(sonarIssue);
                    if (locations.isEmpty()) {
                        //System.out.println("continue");
                        continue;
                    }
                    //解析rawIssue
                    RawIssue rawIssue = getRawIssue(repoUuid, commit, sonarIssue, j);
                    rawIssue.setLocations(locations);
                    //System.out.println("add rawIssue");
                    this.resultRawIssues.add(rawIssue);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static List<Location> getLocations(JSONObject issue) throws Exception {
        int startLine = 0;
        int endLine = 0;
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
            Location mainLocation = getLocation(startLine, endLine);
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
                    Location location = getLocation(flowStartLine, flowEndLine);
                    locations.add(location);
                }
            }
        }
        return locations;
    }

    public static Location getLocation(int startLine, int endLine){
        Location location = new Location();
        location.setStartLine(startLine);
        location.setEndLine(endLine);
        location.setStartToken(0);
        return location;
    }

    public static RawIssue getRawIssue(String repoUuid, String commit, JSONObject issue, Integer index){
        RawIssue rawIssue = new RawIssue();
        rawIssue.setUuid(repoUuid + "_" + index.toString());
        String type = issue.getString("type");
        rawIssue.setType(type);
        String filePath = null;
        String sonarPath = issue.getString("component");
        if (sonarPath != null) {
            String[] sonarComponents = sonarPath.split(":");
            if (sonarComponents.length >= 2) {
                filePath = sonarComponents[sonarComponents.length - 1];
            }
        }
        rawIssue.setFileName(filePath);
        rawIssue.setDetail(issue.getString("message"));
        rawIssue.setCommitId(commit);
        return rawIssue;
    }
}

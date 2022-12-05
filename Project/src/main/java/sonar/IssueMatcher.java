package sonar;

import cn.edu.fudan.issue.core.process.RawIssueMatcher;
import cn.edu.fudan.issue.entity.dbo.RawIssue;
import cn.edu.fudan.issue.util.AnalyzerUtil;
import cn.edu.fudan.issue.util.AstParserUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IssueMatcher {
    //private static final String SEPARATOR = System.getProperty("file.separator");
    private static Set<String> getAllMethodsAndFields(File file) throws IOException {
        assert file.isDirectory();
        Set<String> res = new HashSet<>();
        File[] fs = file.listFiles();
        if(fs != null) {
            for (File f : fs) {
                if (f.isDirectory())
                    res.addAll(getAllMethodsAndFields(f));
                if (f.isFile()) {
                    String name = f.getName();
                    String[] parts = name.split("\\.");
                    name = parts[parts.length - 1];
                    if(name.equals("java"))
                        res.addAll(AstParserUtil.getMethodsAndFieldsInFile(f.getAbsolutePath()));
                }
            }
        }
        return res;
    }

    public static List<List<RawIssue>> match(List<RawIssue> preIssues, List<RawIssue> curIssues, String absoluteRepoPath) {
        List<List<RawIssue>> res = new ArrayList<>();
        //AnalyzerUtil.addExtraAttributeInRawIssues(preIssues, absoluteRepoPath);
        //AnalyzerUtil.addExtraAttributeInRawIssues(curIssues, absoluteRepoPath);
        File repoDir = new File(absoluteRepoPath);
        Set<String> all = null;
        try {
            all = getAllMethodsAndFields(repoDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        RawIssueMatcher.match(preIssues, curIssues, all);
        res.add(preIssues);
        res.add(curIssues);
        return res;
    }
}

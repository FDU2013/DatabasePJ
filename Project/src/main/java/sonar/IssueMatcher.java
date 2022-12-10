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
        File[] fs = file.listFiles();
        if(fs == null)
            return null;
        Set<String> res = new HashSet<>();
        for (File f : fs) {
            if (f.isDirectory()) {
                Set<String> out = getAllMethodsAndFields(f);
                if(out != null)
                    res.addAll(out);
            }
            if (f.isFile()) {
                String name = f.getName();
                String[] parts = name.split("\\.");
                name = parts[parts.length - 1];
                if(name.equals("java"))
                    //System.out.println(f.getName());
                    res.addAll(AstParserUtil.getMethodsAndFieldsInFile(f.getAbsolutePath()));
                }
            }

        return res;
    }

    public static void match(List<RawIssue> preIssues, List<RawIssue> curIssues, String absoluteRepoPath) {
        //List<List<RawIssue>> res = new ArrayList<>();
        File repoDir = new File(absoluteRepoPath);
        Set<String> all = null;
        try {
            all = getAllMethodsAndFields(repoDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        AnalyzerUtil.addExtraAttributeInRawIssues(preIssues, absoluteRepoPath);
        AnalyzerUtil.addExtraAttributeInRawIssues(curIssues, absoluteRepoPath);
        RawIssueMatcher.match(preIssues, curIssues, all);
        //res.add(preIssues);
        //res.add(curIssues);
        //return res;
    }
}

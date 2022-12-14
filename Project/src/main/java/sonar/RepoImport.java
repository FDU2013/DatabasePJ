package sonar;

import cn.edu.fudan.issue.entity.dbo.Location;
import cn.edu.fudan.issue.entity.dbo.RawIssue;
import common.CommitProperty;
import common.INSTANCE_STATUS;
import crud.*;
import entity.*;
import init.Connect;
import util.GitUtil;

import java.sql.Timestamp;
import java.util.*;

import static common.CommitProperty.*;
import static common.EnumUtil.SonarString2CaseType;
import static common.EnumUtil.String2CaseType;
import static common.INSTANCE_STATUS.*;


public class RepoImport {
    private final String repoName;
    private final String path;
    private final GitUtil gitUtil;
    private Integer repositoryId;
    private Integer curBranchId;

    public RepoImport(String repoName, String path){
        assert repoName != null;
        assert path != null;
        this.repoName = repoName;
        this.path = path;
        this.gitUtil = new GitUtil();
        this.gitUtil.initGit(this.path);
    }

    public void importRepo(String description) {
        Repository repo = new Repository(null, repoName, description, path);
        try {
            repositoryId = RepositoryCRUD.insertRepository(repo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void importAllBranch(){
        List<String> allBranch = gitUtil.getAllBranch();
        Scanner s = new Scanner(System.in);
        System.out.println("请输入sonarQube的账号：");
        String account = s.nextLine();
        System.out.println("请输入sonarQube的密码：");
        String password = s.nextLine();
        s.close();
        SonarResultParser.initParser(account, password);
        for(String branch : allBranch) {
            curBranchId = importBranch(branch);
            try {
                importAllCommitAndIssue(branch);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public Integer importBranch(String name){
        Integer id = 0;
        Branch branch = new Branch(null, repositoryId, name, null);
        try {
            id = BranchCRUD.insertBranch(branch);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public void importAllCommitAndIssue(String branchName) throws Exception {
        //List<String> allBranch = gitUtil.getAllBranch();
        //f(!allBranch.contains(branchName))
        //branchName = allBranch.get(0);
        //branchName = "main";
        //curBranchId = allBranch.indexOf(branchName) + 1;
        //curBranchId = 1;
        gitUtil.checkoutBranch(branchName);
        ArrayList<HashMap<CommitProperty, Object>> allCommit = gitUtil.getAllCommit();
        List<IssueInstance> preIssuesInstance = new ArrayList<>();
        List<RawIssue> preRawIssues;
        try {
            Connect.startTransaction();
            GitCommit lastCommit = importCommit(allCommit.get(allCommit.size() - 1));
            gitUtil.checkoutCommit(lastCommit.getHash_val());
            String sonarID = repoName + "_" + branchName + "_" + lastCommit.getHash_val();
            SonarScanner.ScanRepo(path, sonarID);
            SonarResultParser parser = new SonarResultParser();
            Thread.sleep(15000);
            parser.getSonarResult(repoName, branchName, lastCommit.getHash_val());
            preRawIssues = parser.getResultRawIssues();
            for (RawIssue issue : preRawIssues) {
                Integer caseId = IssueCaseCRUD.insertIssueCase(lastCommit.getCommit_id(), SonarString2CaseType(issue.getType()));
                preIssuesInstance.add(importIssueInstance(caseId, lastCommit, issue, APPEAR));
            }
            Connect.commit();
        } catch (Exception e){
            e.printStackTrace();
            Connect.rollBack();
            return;
        }

        for(int i = allCommit.size() - 2; i >= 0; i--){
            HashMap<CommitProperty, Object> commitInfo = allCommit.get(i);
            List<IssueInstance> curIssuesInstance = new ArrayList<>();
            List<RawIssue> curRawIssues;
            try {
                GitCommit commit = importCommit(commitInfo);
                gitUtil.checkoutCommit(commit.getHash_val());
                String id = repoName + "_" + branchName + "_" + commit.getHash_val();
                SonarScanner.ScanRepo(path, id);
                SonarResultParser parser1 = new SonarResultParser();

                Thread.sleep(15000);
                parser1.getSonarResult(repoName, branchName, commit.getHash_val());
                curRawIssues = parser1.getResultRawIssues();
                //System.out.println("size" + curRawIssues.size());
                IssueMatcher.match(preRawIssues, curRawIssues, path);
                Connect.startTransaction();
                for (int j = 0; j < curRawIssues.size(); j++) {
                    RawIssue issue = curRawIssues.get(j);
                    if (!issue.isMapped()) {
                        Integer caseId = IssueCaseCRUD.insertIssueCase(commit.getCommit_id(), SonarString2CaseType(issue.getType()));
                        System.out.println("appear issueCase: (id) -- " + caseId);
                        curIssuesInstance.add(importIssueInstance(caseId, commit, issue, APPEAR));
                    } else {
                        RawIssue mappedIssue = issue.getMappedRawIssue();
                        IssueInstance mappedIssueInstance = preIssuesInstance.get(preRawIssues.indexOf(mappedIssue));
                        if (compareRawIssue(issue, mappedIssue)) {
                            System.out.println("keep issueCase: (id) -- " + mappedIssueInstance.getIssue_case_id());
                            curIssuesInstance.add(mappedIssueInstance);
                        } else {
                            System.out.println("update issueCase: (id) -- " + mappedIssueInstance.getIssue_case_id());
                            curIssuesInstance.add(importIssueInstance(mappedIssueInstance.getIssue_case_id(), commit, issue, UPDATE));
                        }
                    }
                }
                for (int j = 0; j < preRawIssues.size(); j++) {
                    RawIssue issue = preRawIssues.get(j);
                    if (!issue.isMapped()) {
                        IssueInstance thisInstance = preIssuesInstance.get(j);
                        System.out.println("disappear issueCase: (id) -- " + thisInstance.getIssue_case_id());
                        curIssuesInstance.add(importIssueInstance(thisInstance.getIssue_case_id(), commit, issue, DISAPPEAR));
                        IssueCaseCRUD.solveIssueCase(thisInstance.getIssue_case_id(), commit.getCommit_id());
                    }
                }
                Connect.commit();
            } catch (Exception e){
                e.printStackTrace();
                Connect.rollBack();
                return;
            }
            for(RawIssue rawIssue : curRawIssues){
                rawIssue.resetMappedInfo();
            }
            preRawIssues = curRawIssues;
            preIssuesInstance = curIssuesInstance;
//            preRawIssues.clear();
//            preRawIssues.addAll(curRawIssues);
//            preIssuesInstance.clear();
//            preIssuesInstance.addAll(curIssuesInstance);
        }
    }

    private IssueInstance importIssueInstance(Integer caseId, GitCommit commit, RawIssue issue, INSTANCE_STATUS status) throws Exception {
        IssueInstance instance = new IssueInstance(null, caseId, commit.getCommit_id(),
                status, issue.getFileName(), issue.getDetail());
        instance.setIssue_instance_id(IssueInstanceCRUD.insertIssueInstance(instance));
        int sequence = 0;
        for (Location location : issue.getLocations()){
            IssueLocation issueLocation = new IssueLocation(instance.getIssue_instance_id(),
                    sequence, location.getStartLine(), location.getEndLine());
            IssueLocationCRUD.insertIssueLocation(issueLocation);
            sequence++;
        }
        return instance;
    }

    public GitCommit importCommit(HashMap<CommitProperty, Object> commitInfo){
        Integer commitId = 0;
        GitCommit gitCommit = new GitCommit(curBranchId, null,
                (String)commitInfo.get(HASH),
                new Timestamp(((Date)commitInfo.get(TIME)).getTime()),
                (String)commitInfo.get(AUTHOR)
        );
        try {
            commitId = GitCommitCRUD.insertGitCommit(gitCommit);
            gitCommit.setCommit_id(commitId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gitCommit;
    }

    private boolean compareRawIssue(RawIssue rawIssue1, RawIssue rawIssue2){
        if(!rawIssue1.getFileName().equals(rawIssue2.getFileName())) return false;
        if(!rawIssue1.getDetail().equals(rawIssue2.getDetail())) return false;
        List<Location> location1 = rawIssue1.getLocations();
        List<Location> location2 = rawIssue1.getLocations();
        return listValEquals(location1, location2);
    }


    public static <T> boolean listValEquals(List<T> t1, List<T> t2) {
        if (t1 == t2) { // 为空or引用地址一致
            return true;
        } else if (t1.size() != t2.size()) {
            return false;
        }
        for (T t : t1) {
            if (!t2.contains(t)) {
                return false;
            }
        }
        return true;
    }
}

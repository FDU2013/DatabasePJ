package sonar;

import cn.edu.fudan.issue.entity.dbo.Location;
import cn.edu.fudan.issue.entity.dbo.RawIssue;
import common.CommitProperty;
import common.INSTANCE_STATUS;
import crud.*;
import entity.*;
import util.GitUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
        for(String branch : allBranch) {
           // Integer id =
            System.out.println(importBranch(branch));
            //TODO sds
            //if(branch.equals("refs/heads/master")) curBranchId = id;
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
        List<String> allBranch = gitUtil.getAllBranch();
        //f(!allBranch.contains(branchName))
        //branchName = allBranch.get(0);
        branchName = "main";
        //curBranchId = allBranch.indexOf(branchName) + 1;
        curBranchId = 1;
        gitUtil.checkoutBranch(branchName);
        ArrayList<HashMap<CommitProperty, Object>> allCommit = gitUtil.getAllCommit();
        GitCommit lastCommit = importCommit(allCommit.get(allCommit.size() - 2));
        gitUtil.checkoutCommit(lastCommit.getHash_val());
        String sonarID = repoName + "_" + branchName + "_" + lastCommit.getHash_val();
        SonarScanner.ScanRepo(path, sonarID);
        SonarResultParser parser = new SonarResultParser();
        List<IssueInstance> preIssuesInstance = new ArrayList<>();
        Thread.sleep(10000);
        parser.getSonarResult(repoName, branchName, lastCommit.getHash_val());
        List<RawIssue> preRawIssues = parser.getResultRawIssues();
        for(RawIssue issue : preRawIssues){
            Integer caseId = IssueCaseCRUD.insertIssueCase(lastCommit.getCommit_id(), SonarString2CaseType(issue.getType()));
            preIssuesInstance.add(importIssueInstance(caseId, lastCommit, issue, APPEAR));
        }

        for(int i = allCommit.size() - 3; i >= 0; i--){
            HashMap<CommitProperty, Object> commitInfo = allCommit.get(i);
            GitCommit commit = importCommit(commitInfo);
            gitUtil.checkoutCommit(commit.getHash_val());
            String id = repoName + "_" + branchName + "_" + commit.getHash_val();
            SonarScanner.ScanRepo(path, id);
            SonarResultParser parser1 = new SonarResultParser();
            List<IssueInstance> curIssuesInstance = new ArrayList<>();

            parser1.getSonarResult(repoName, branchName, commit.getHash_val());
            List<RawIssue> curRawIssues = parser1.getResultRawIssues();
            System.out.println("size" + curRawIssues.size());
            IssueMatcher.match(preRawIssues, curRawIssues, path);
            for(int j = 0; j < curRawIssues.size(); j++){
                RawIssue issue = curRawIssues.get(j);
                if(!issue.isMapped()){
                    Integer caseId = IssueCaseCRUD.insertIssueCase(commit.getCommit_id(), SonarString2CaseType(issue.getType()));
                    System.out.println("appear" + caseId);
                    curIssuesInstance.add(importIssueInstance(caseId, commit, issue, APPEAR));
                } else {
                    RawIssue mappedIssue = issue.getMappedRawIssue();
                    IssueInstance mappedIssueInstance = preIssuesInstance.get(preRawIssues.indexOf(mappedIssue));
                    if(compareRawIssue(issue, mappedIssue)){
                        System.out.println("keep" + mappedIssueInstance.getIssue_case_id());
                        curIssuesInstance.add(mappedIssueInstance);
                    } else {
                        System.out.println("update" + mappedIssueInstance.getIssue_case_id());
                        curIssuesInstance.add(importIssueInstance(mappedIssueInstance.getIssue_case_id(), commit, issue, UPDATE));
                    }
                }
            }
            for(int j = 0; j < preRawIssues.size(); j++){
                RawIssue issue = preRawIssues.get(j);
                if(!issue.isMapped()){
                    IssueInstance thisInstance = preIssuesInstance.get(j);
                    System.out.println("disappear" + thisInstance.getIssue_case_id());
                    curIssuesInstance.add(importIssueInstance(thisInstance.getIssue_case_id(), commit, issue, DISAPPEAR));
                    IssueCaseCRUD.solveIssueCase(thisInstance.getIssue_case_id(), commit.getCommit_id());
                }
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
        if (t1 == t2) { // 为空or引用地址一致时
            return true;
        } else if (t1.size() != t2.size()) { // 数量一致, 过滤掉了list1中有{1,1,3},list2中有{1,3,4}的场景
            return false;
        }
        for (T t : t1) {
            if (!t2.contains(t)) { // equals比较
                return false;
            }
        }
        return true;
    }
}

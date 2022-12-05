package sonar;

import common.CommitProperty;
import crud.BranchCRUD;
import crud.GitCommitCRUD;
import crud.RepositoryCRUD;
import entity.Branch;
import entity.GitCommit;
import entity.Repository;
import util.GitUtil;

import javax.xml.crypto.Data;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static common.CommitProperty.*;

public class RepoImport {
    private String repoName;
    private String path;
    private GitUtil gitUtil;
    private Integer repositoryId;

    public RepoImport(String repoName, String path){
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
            importBranch(branch);
        }
    }

    public void importBranch(String name){
        Branch branch = new Branch(null, repositoryId, name, null);
        try {
            BranchCRUD.insertBranch(branch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void importAllCommitAndIssue(){
        gitUtil.checkoutBranch("main");
        ArrayList<HashMap<CommitProperty, Object>> allCommit = gitUtil.getAllCommit();
        for(int i = allCommit.size() - 1; i >= 0; i--){
            HashMap<CommitProperty, Object> commit = allCommit.get(i);
            Integer commitId = importCommit(commit);
        }
    }

    public Integer importCommit(HashMap<CommitProperty, Object> commitInfo){
        Integer commitId = 0;
        GitCommit gitCommit = new GitCommit(null, null,
                (String)commitInfo.get(HASH),
                new Timestamp(((Date)commitInfo.get(TIME)).getTime()),
                (String)commitInfo.get(AUTHOR)
        );
        try {
            commitId = GitCommitCRUD.insertGitCommit(gitCommit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commitId;
    }

    public void importIssue(){

    }
}

package util;

import common.CommitProperty;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static common.CommitProperty.*;

public class GitUtil {
    private Git git;

    public void initGit(String repoPath){
        Git git = null;
        try {
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(Paths.get(repoPath, ".git").toFile())
                    .build();
            git = new Git(repository);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.git = git;
    }

    public ArrayList<HashMap<CommitProperty, Object>> getAllCommit() {
        Iterable<RevCommit> logIterable = null;
        try {
            logIterable = this.git.log().call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        assert logIterable != null;
        Iterator<RevCommit> logIterator = logIterable.iterator();
        int row = 0;
        ArrayList<HashMap<CommitProperty, Object>> list = new ArrayList<>();
        while (logIterator.hasNext()) {
            HashMap<CommitProperty, Object> map = new HashMap<>();
            RevCommit commit = logIterator.next();
            Date commitDate = commit.getAuthorIdent().getWhen();
            String commitPerson = commit.getAuthorIdent().getName() ;
            String commitID = commit.getName();
            String commitMessage = commit.getFullMessage();
            map.put(HASH, commitID);
            map.put(TIME, commitDate);
            map.put(AUTHOR, commitPerson);
            map.put(MESSAGE, commitMessage);
            list.add(row, map);
            row++;
        }
        return list;
    }

    public void checkoutCommit(String commitHash) {
        try {
            git.checkout()
                    .setName(commitHash)
                    .call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public void checkoutBranch(String branchName) {
        try {
            git.checkout()
                    .setName(branchName)
                    .call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllBranch(){
        List<String> res = new ArrayList<>();
        List<Ref> allBranch = null;
        try {
            allBranch = this.git.branchList().call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        assert allBranch != null;
        for (Ref ref : allBranch) {
            res.add(ref.getName());
        }
        return res;
    }
}

package sort;

import entity.GitCommit;

import java.util.Comparator;

public class GitCommitTimeComparator implements Comparator<GitCommit> {
    @Override
    public int compare(GitCommit commit1, GitCommit commit2) {
        return commit1.getCommit_time().compareTo(commit2.getCommit_time());
    }
}

package sort;

import entity.GitCommit;
import entity.IssueLocation;

import java.util.Comparator;

public class IssueLocationComparator implements Comparator<IssueLocation> {
    @Override
    public int compare(IssueLocation location1, IssueLocation location2) {
        return location1.getSequence().compareTo(location2.getSequence());
    }
}

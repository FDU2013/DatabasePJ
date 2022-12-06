package sort;

import entity.IssueCase;

import java.util.Comparator;

public class UnsolvedCaseLivingTimeComparator implements Comparator<IssueCase> {
    @Override
    public int compare(IssueCase case1, IssueCase case2) {
        return case1.getAppear_time().compareTo(case2.getAppear_time());
    }
}

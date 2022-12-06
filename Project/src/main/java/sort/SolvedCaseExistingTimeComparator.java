package sort;

import common.ExtendedInstance;
import entity.IssueCase;

import java.util.Comparator;

public class SolvedCaseExistingTimeComparator implements Comparator<IssueCase> {
    @Override
    public int compare(IssueCase case1, IssueCase case2) {
        Long t1 = case1.getSolve_time().getTime() - case1.getAppear_time().getTime();
        Long t2 = case2.getSolve_time().getTime() - case2.getAppear_time().getTime();
        return t1.compareTo(t2);
    }
}

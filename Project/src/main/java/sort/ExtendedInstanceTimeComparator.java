package sort;

import common.ExtendedInstance;
import entity.GitCommit;

import java.util.Comparator;

public class ExtendedInstanceTimeComparator implements Comparator<ExtendedInstance> {
    @Override
    public int compare(ExtendedInstance instance1, ExtendedInstance instance2) {
        return instance1.getAppear_time().compareTo(instance2.getAppear_time());
    }
}

package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sort.IssueLocationComparator;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueLocation {
    Integer issue_instance_id;
    Integer sequence;
    Integer start_line;
    Integer end_line;

    public static void PrintLocationList(List<IssueLocation> locations){
        locations.sort(new IssueLocationComparator());
        for (IssueLocation location:locations){
            System.out.printf("[%d,%d] ",location.getStart_line(),location.getEnd_line());
        }
    }
}

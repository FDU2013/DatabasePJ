package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueLocation {
    Integer issue_instance_id;
    Integer sequence;
    Integer start_line;
    Integer end_line;

    public IssueLocation(Integer sequence, Integer startLine, Integer endLine){
        this.sequence = sequence;
        this.start_line = startLine;
        this.end_line = endLine;
    }
}

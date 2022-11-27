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
}

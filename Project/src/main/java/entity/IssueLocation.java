package entity;

import lombok.Data;

@Data
public class IssueLocation {
    Integer issue_instance_id;
    Integer sequence;
    String file_name;
    String file_path;
    Integer start_line;
    Integer end_line;
}

package entity;

import common.INSTANCE_STATUS;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueInstance {
    Integer issue_instance_id;
    Integer issue_case_id;
    Integer commit_id;
    INSTANCE_STATUS instance_status;
    String file_path;
    String message;
}

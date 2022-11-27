package entity;

import common.INSTANCE_STATUS;
import lombok.Data;

@Data
public class IssueInstance {
    Integer issue_instance_id;
    Integer issue_case_id;
    Integer commit_id;
    INSTANCE_STATUS instance_status;
}

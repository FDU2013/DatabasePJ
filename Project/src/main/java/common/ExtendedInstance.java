package common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class ExtendedInstance {
    Integer issue_instance_id;
    Integer issue_case_id;
    Integer commit_id;
    INSTANCE_STATUS instance_status;
    String file_path;
    String message;
    CASE_TYPE type;
    Timestamp appear_time;
    String appear_committer;
    Timestamp solve_time;
    String solve_committer;
}

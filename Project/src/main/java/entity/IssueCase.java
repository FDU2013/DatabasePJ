package entity;

import common.CASE_STATUS;
import common.CASE_TYPE;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueCase {
    Integer issue_case_id;
    Integer appear_commit_id;
    Integer solve_commit_id;
    CASE_STATUS case_status;
    CASE_TYPE type;
    Timestamp appear_time;
    String appear_committer;
    Timestamp solve_time;
    String solve_committer;
}

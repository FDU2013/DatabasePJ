package entity;

import common.CASE_STATUS;
import common.CASE_TYPE;
import lombok.Data;

import java.util.Date;

@Data
public class IssueCase {
    Integer issue_case_id;
    Integer appear_commit_id;
    Integer solve_commit_id;
    CASE_STATUS case_status;
    CASE_TYPE type;
    Date appear_time;
    String appear_committer;
    Date solve_time;
    String solve_committer;
}

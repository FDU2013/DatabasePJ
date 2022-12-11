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
    Integer latest_instance_id;

    public ExtendedInstance(ExtendedInstance instance){
         issue_instance_id = instance.issue_instance_id;
         issue_case_id = instance.issue_case_id;
         commit_id = instance.commit_id;
         instance_status = instance.instance_status;
         file_path = instance.file_path;
         message = instance.message;
         type = instance.type;
         appear_time = instance.appear_time;
         appear_committer = instance.appear_committer;
         solve_time = instance.solve_time;
         solve_committer = instance.solve_committer;
         latest_instance_id = instance.latest_instance_id;
    }

}

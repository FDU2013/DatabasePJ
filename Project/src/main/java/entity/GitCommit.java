package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitCommit {
    Integer branch_id;
    Integer commit_id;
    String hash_val;
    Date commit_time;
    String committer;
}

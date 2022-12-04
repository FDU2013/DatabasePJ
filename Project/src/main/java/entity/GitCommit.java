package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitCommit {
    Integer branch_id;
    Integer commit_id;
    String hash_val;
    Timestamp commit_time;
    String committer;
}

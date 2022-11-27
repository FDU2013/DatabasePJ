package entity;

import lombok.Data;

import java.util.Date;

@Data
public class GitCommit {
    Integer branch_id;
    Integer commit_id;
    String hash_val;
    Date commit_time;
    String committer;
}

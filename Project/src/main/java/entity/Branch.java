package entity;

import lombok.Data;

@Data
public class Branch {
    Integer branch_id;
    Integer repository_id;
    String name;
    String description;
}

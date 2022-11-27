package entity;

import lombok.Data;

@Data
public class Repository {
    Integer repository_id ;
    String name;
    String description;
    String url;
}

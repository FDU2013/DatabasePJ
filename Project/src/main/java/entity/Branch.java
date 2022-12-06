package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Branch {
    Integer branch_id;
    Integer repository_id;
    String name;
    String description;

    public void print(){
        System.out.println("分支名："+this.name);
        System.out.println("描述："+this.description);
    }
}

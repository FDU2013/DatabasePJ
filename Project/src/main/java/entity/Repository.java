package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Repository {
    Integer repository_id ;
    String name;
    String description;
    String url;

    public void print(){
        System.out.println("仓库名："+this.name);
        System.out.println("描述："+this.description);
        System.out.println("url："+this.url);
    }
}

package crud;

import entity.Repository;

import java.sql.Connection;

public class RepositoryCRUD {
    public static Integer insertRepository(Repository repository) throws Exception{
        Connection connection = Launch.getConnection();


        return 0;
    }
}

package crud;

import init.Connect;
import entity.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class RepositoryCRUD {
    public static Integer insertRepository(Repository repository) throws Exception{
        Connection connection = Connect.getConnection();
        String sql = "insert into repository(name,description,url) values(?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1,repository.getName());
        ps.setString(2,repository.getDescription());
        ps.setString(3,repository.getUrl());
        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next()){
            return rs.getInt(1);
        }
        throw new Exception();
    }
}

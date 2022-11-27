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
        String sql = "insert into repository values(null,'"
                +repository.getName()+"','"
                +repository.getDescription()+"','"
                +repository.getUrl()+");";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next()){
            return rs.getInt(1);
        }
        throw new Exception();
    }
}

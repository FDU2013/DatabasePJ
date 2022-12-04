package crud;

import entity.Branch;
import init.Connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class BranchCRUD {
    public static Integer insertBranch(Branch branch)throws Exception{
        Connection connection = Connect.getConnection();
        String sql = "insert into branch(repository_id,name,description) values(?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1,branch.getRepository_id());
        ps.setString(2,branch.getName());
        ps.setString(3,branch.getDescription());
        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next()){
            return rs.getInt(1);
        }
        throw new Exception();
    }
}

package crud;

import entity.Branch;
import entity.GitCommit;
import init.BranchView;
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
        if(branch.getDescription() == null){
            ps.setString(3,"null");
        }else{
            ps.setString(3,branch.getDescription());
        }

        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next()){
            return rs.getInt(1);
        }
        throw new Exception();
    }

    public static Branch getOnlyBranchFromResult(ResultSet rs) throws Exception {
        if(rs.next()){
            Branch branch = new Branch(
                    rs.getInt("branch_id"),
                    rs.getInt("repository_id"),
                    rs.getString("name"),
                    rs.getString("description"));
            if(rs.next())throw new Exception();
            return branch;
        }
        throw new Exception();
    }

    public static Branch getBranchByName(String name)throws Exception{
        Connection connection = Connect.getConnection();
        Integer repo_id = BranchView.getCurrentRepo();
        String sql = "select * from branch where name=? and repository_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1,name);
        ps.setInt(2,repo_id);
        ResultSet rs = ps.executeQuery();
        return getOnlyBranchFromResult(rs);
    }

    public static Branch getBranchByID(Integer branch_id)throws Exception{
        Connection connection = Connect.getConnection();
        Integer repo_id = BranchView.getCurrentRepo();
        String sql = "select * from branch where branch_id=? and repository_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,branch_id);
        ps.setInt(2,repo_id);
        ResultSet rs = ps.executeQuery();
        return getOnlyBranchFromResult(rs);
    }
}

package crud;

import entity.Branch;
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

    public static Repository getOnlyRepositoryFromResult(ResultSet rs) throws Exception {
        if(rs.next()){
            Repository repo = new Repository(
                    rs.getInt("repository_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("url"));
            if(rs.next())throw new Exception();
            return repo;
        }
        throw new Exception();
    }

    public static Repository getRepositoryByName(String name)throws Exception{
        Connection connection = Connect.getConnection();
        String sql = "select * from repository where name=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1,name);
        ResultSet rs = ps.executeQuery();
        return getOnlyRepositoryFromResult(rs);
    }

    public static Repository getRepositoryByID(Integer repo_id)throws Exception{
        Connection connection = Connect.getConnection();
        String sql = "select * from repository where repository_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,repo_id);
        ResultSet rs = ps.executeQuery();
        return getOnlyRepositoryFromResult(rs);
    }
}

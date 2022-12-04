package crud;

import common.EnumUtil;
import entity.IssueInstance;
import init.Connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class IssueInstanceCRUD {
    public static Integer insertIssueInstance(IssueInstance issueInstance) throws Exception{
        Connection connection = Connect.getConnection();
        String sql = "insert into issue_instance(issue_case_id,commit_id,instance_status,file_path,message) values(?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1,issueInstance.getIssue_case_id());
        ps.setInt(2,issueInstance.getCommit_id());
        ps.setString(3, EnumUtil.Enum2String(issueInstance.getInstance_status()));
        ps.setString(4,issueInstance.getFile_path());
        ps.setString(5,issueInstance.getMessage());
        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next()){
            return rs.getInt(1);
        }
        throw new Exception();
    }

}

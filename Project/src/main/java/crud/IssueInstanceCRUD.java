package crud;

import common.EnumUtil;
import common.ExtendedInstance;
import entity.IssueInstance;
import init.Connect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    private static List<ExtendedInstance> getAllExtendedInstanceFromResult(ResultSet rs) throws Exception {
        List<ExtendedInstance> list = new ArrayList<>();
        while(rs.next()){
            ExtendedInstance extendedInstance = new ExtendedInstance(
                    rs.getInt("issue_instance_id"),
                    rs.getInt("issue_case_id"),
                    rs.getInt("commit_id"),
                    EnumUtil.String2InstanceStatus(rs.getString("instance_status")),
                    rs.getString("file_path"),
                    rs.getString("message"),
                    EnumUtil.String2CaseType(rs.getString("type")),
                    rs.getTimestamp("appear_time"),
                    rs.getString("appear_committer"),
                    rs.getTimestamp("solve_time"),
                    rs.getString("solve_committer"),
                    null);
            list.add(extendedInstance);
        }
        return list;
    }

    public static List<ExtendedInstance> getAllExtendedInstanceByCommitId(Integer commit_id, boolean use_index) throws Exception {
        Connection connection = Connect.getConnection();
        String sql = "select * from issue_case join issue_instance using(issue_case_id) where commit_id=?";
        if(!use_index) sql = "select * from issue_case join issue_instance ignore index(instance_index_on_commit_id) using(issue_case_id) where commit_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,commit_id);
        ResultSet rs = ps.executeQuery();
        return getAllExtendedInstanceFromResult(rs);
    }

    public static List<ExtendedInstance> getAllExtendedInstanceByCommitIdNoCache(Integer commit_id, boolean use_index) throws Exception {
        Connection connection = Connect.getConnection();
        String sql = "select SQL_NO_CACHE * from issue_case join issue_instance using(issue_case_id) where commit_id=?";
        if(!use_index) sql = "select SQL_NO_CACHE * from issue_case join issue_instance ignore index(instance_index_on_commit_id) using(issue_case_id) where commit_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,commit_id);
        ResultSet rs = ps.executeQuery();
        return getAllExtendedInstanceFromResult(rs);
    }




}

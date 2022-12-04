package crud;

import common.EnumUtil;
import entity.IssueLocation;
import init.Connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class IssueLocationCRUD {
    public static void insertIssueLocation(IssueLocation location) throws Exception{
        Connection connection = Connect.getConnection();
        String sql = "insert into issue_location(issue_instance_id,sequence,start_line,end_line) values(?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,location.getIssue_instance_id());
        ps.setInt(2,location.getSequence());
        ps.setInt(3, location.getStart_line());
        ps.setInt(4,location.getEnd_line());
        Integer rs = ps.executeUpdate();
        if(rs==0) {
            throw new Exception();
        }
    }
}

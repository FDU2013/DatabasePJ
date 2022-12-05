package crud;

import common.EnumUtil;
import common.ExtendedInstance;
import entity.IssueLocation;
import init.Connect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    private static List<IssueLocation> getAllLocationFromResult(ResultSet rs) throws Exception {
        List<IssueLocation> list = new ArrayList<>();
        while(rs.next()){
            IssueLocation location = new IssueLocation(
                    rs.getInt("issue_instance_id"),
                    rs.getInt("sequence"),
                    rs.getInt("start_line"),
                    rs.getInt("end_line"));
            list.add(location);
        }
        return list;
    }

    public static List<IssueLocation> getLocationByInstanceId(Integer issue_instance_id) throws Exception {
        Connection connection = Connect.getConnection();
        String sql = "select * from issue_location where issue_instance_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,issue_instance_id);
        ResultSet rs = ps.executeQuery();
        return getAllLocationFromResult(rs);
    }

}

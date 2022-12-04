package crud;

import common.CASE_TYPE;
import common.EnumUtil;
import entity.GitCommit;
import entity.IssueCase;
import init.Connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class IssueCaseCRUD {
    //插入一个新的issue的时候，只需要说明appear_commit_id和type即可，solve的信息以后补全，至于time和committer，这里会自己补全
    public static Integer insertIssueCase(Integer appear_commit_id, CASE_TYPE type) throws Exception{
        Connection connection = Connect.getConnection();
        String sql = "insert into issue_case(appear_commit_id,case_status,type,appear_time,appear_committer) values(?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1,appear_commit_id);
        ps.setString(2,"UNSOLVED");
        ps.setString(3, EnumUtil.Enum2String(type));
        GitCommit gitCommit = GitCommitCRUD.selectGitCommitByCommitId(1);
        ps.setString(4, gitCommit.getCommit_time().toString());
        ps.setString(5, gitCommit.getCommitter());
        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next()){
            return rs.getInt(1);
        }
        throw new Exception();
    }

//    public static void SolveIssueCase(Integer issue_case_id, Integer ){
//
//    }
}

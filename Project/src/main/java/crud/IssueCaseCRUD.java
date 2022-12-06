package crud;

import common.CASE_TYPE;
import common.EnumUtil;
import common.ExtendedInstance;
import entity.GitCommit;
import entity.IssueCase;
import init.BranchView;
import init.Connect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IssueCaseCRUD {
    //插入一个新的issue的时候，只需要说明appear_commit_id和type即可，solve的信息以后补全，至于time和committer，这里会自己补全
    public static Integer insertIssueCase(Integer appear_commit_id, CASE_TYPE type) throws Exception{
        Connection connection = Connect.getConnection();
        String sql = "insert into issue_case(appear_commit_id,case_status,type,appear_time,appear_committer) values(?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1,appear_commit_id);
        ps.setString(2,"UNSOLVED");
        ps.setString(3, EnumUtil.Enum2String(type));
        GitCommit gitCommit = GitCommitCRUD.selectGitCommitByCommitId(appear_commit_id);
        ps.setString(4, gitCommit.getCommit_time().toString());
        ps.setString(5, gitCommit.getCommitter());
        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next()){
            return rs.getInt(1);
        }
        throw new Exception();
    }

    public static void solveIssueCase(Integer issue_case_id, Integer solve_commit_id) throws Exception {
        Connection connection = Connect.getConnection();
        String sql = "update issue_case set solve_commit_id=?, solve_time=?, solve_committer=?, case_status=? where issue_case_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,solve_commit_id);
        GitCommit gitCommit = GitCommitCRUD.selectGitCommitByCommitId(solve_commit_id);
        ps.setString(2, gitCommit.getCommit_time().toString());
        ps.setString(3, gitCommit.getCommitter());
        ps.setString(4,"SOLVED");
        ps.setInt(5, issue_case_id);

        Integer rs = ps.executeUpdate();
        if(rs==0){
            throw new Exception();
        }
    }

    public static List<IssueCase> getAllIssueCaseFromResult(ResultSet rs) throws Exception {
        List<IssueCase> list = new ArrayList<>();
        while(rs.next()){
            IssueCase issueCase = new IssueCase(
                    rs.getInt("issue_case_id"),
                    rs.getInt("appear_commit_id"),
                    rs.getInt("solve_commit_id"),
                    EnumUtil.String2CaseStatus(rs.getString("case_status")),
                    EnumUtil.String2CaseType(rs.getString("type")),
                    rs.getTimestamp("appear_time"),
                    rs.getString("appear_committer"),
                    rs.getTimestamp("solve_time"),
                    rs.getString("solve_committer"));
            list.add(issueCase);
        }
        return list;
    }

    public static List<IssueCase> getAppearIssueCaseByCommit(GitCommit gitCommit) throws Exception {
        Connection connection = Connect.getConnection();
        String sql = "select * from issue_case where appear_commit_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,gitCommit.getCommit_id());
        ResultSet rs = ps.executeQuery();
        return getAllIssueCaseFromResult(rs);
    }

    public static List<IssueCase> getSolvedIssueCaseByCommit(GitCommit gitCommit) throws Exception {
        Connection connection = Connect.getConnection();
        String sql = "select * from issue_case where solve_commit_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,gitCommit.getCommit_id());
        ResultSet rs = ps.executeQuery();
        return getAllIssueCaseFromResult(rs);
    }

    public static IssueCase getIssueCaseByInstanceID(Integer issue_case_id) throws Exception {
        Connection connection = Connect.getConnection();
        String sql = "select * from issue_case where issue_case_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,issue_case_id);
        ResultSet rs = ps.executeQuery();
        List<IssueCase> cases = getAllIssueCaseFromResult(rs);
        if(cases.size()==0)throw new Exception();
        return cases.get(0);
    }

    public static List<IssueCase> getSelfProducedAndSelfSolvedIssueCaseByCommitter(String committer) throws Exception {
        Connection connection = Connect.getConnection();
        Integer branch_id = BranchView.getCurrentBranch();
        String sql = "select * from issue_case where appear_committer=? and solve_committer=? and case_status='SOLVED' and appear_commit_id in (select commit_id from git_commit where branch_id=?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1,committer);
        ps.setString(2,committer);
        ps.setInt(3,branch_id);
        ResultSet rs = ps.executeQuery();
        return getAllIssueCaseFromResult(rs);
    }

    public static List<IssueCase> getSelfProducedAndOthersSolvedIssueCaseByCommitter(String committer) throws Exception {
        Connection connection = Connect.getConnection();
        Integer branch_id = BranchView.getCurrentBranch();
        String sql = "select * from issue_case where appear_committer=? and solve_committer<>? and case_status='SOLVED' and appear_commit_id in (select commit_id from git_commit where branch_id=?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1,committer);
        ps.setString(2,committer);
        ps.setInt(3,branch_id);
        ResultSet rs = ps.executeQuery();
        return getAllIssueCaseFromResult(rs);
    }

    public static List<IssueCase> getOthersProducedAndSelfSolvedIssueCaseByCommitter(String committer) throws Exception {
        Connection connection = Connect.getConnection();
        Integer branch_id = BranchView.getCurrentBranch();
        String sql = "select * from issue_case where appear_committer<>? and solve_committer=? and case_status='SOLVED' and appear_commit_id in (select commit_id from git_commit where branch_id=?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1,committer);
        ps.setString(2,committer);
        ps.setInt(3,branch_id);
        ResultSet rs = ps.executeQuery();
        return getAllIssueCaseFromResult(rs);
    }

    public static List<IssueCase> getSelfProducedAndNotSolvedIssueCaseByCommitter(String committer) throws Exception {
        Connection connection = Connect.getConnection();
        Integer branch_id = BranchView.getCurrentBranch();
        String sql = "select * from issue_case where appear_committer=? and case_status='UNSOLVED' and appear_commit_id in (select commit_id from git_commit where branch_id=?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1,committer);
        ps.setInt(2,branch_id);
        ResultSet rs = ps.executeQuery();
        return getAllIssueCaseFromResult(rs);
    }

    private static ExtendedInstance getFirstExtendedInstanceFromResult(ResultSet rs) throws Exception {
        List<ExtendedInstance> list = new ArrayList<>();
        if(rs.next()){
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
            return extendedInstance;
        }
        throw new Exception();
    }

    public static ExtendedInstance getExtendedInstanceByCaseId(Integer issue_case_id) throws Exception {
        Connection connection = Connect.getConnection();
        String sql = "select * from issue_case join issue_instance using(issue_case_id) where issue_case_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,issue_case_id);
        ResultSet rs = ps.executeQuery();
        return getFirstExtendedInstanceFromResult(rs);
    }
}

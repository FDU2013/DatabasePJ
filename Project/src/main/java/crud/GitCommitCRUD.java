package crud;

import entity.GitCommit;
import entity.IssueCase;
import entity.IssueInstance;
import init.BranchView;
import init.Connect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GitCommitCRUD {
    public static Integer insertGitCommit(GitCommit gitCommit)throws Exception{
        Connection connection = Connect.getConnection();
        String sql = "insert into git_commit(branch_id,hash_val,commit_time,committer) values(?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1,gitCommit.getBranch_id());
        ps.setString(2,gitCommit.getHash_val());
        ps.setTimestamp(3,gitCommit.getCommit_time());
        ps.setString(4,gitCommit.getCommitter());
        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next()){
            return rs.getInt(1);
        }
        throw new Exception();
    }

    public static GitCommit getFirstGitCommitFromResult(ResultSet rs) throws Exception {
        if(rs.next()){
            GitCommit gitCommit = new GitCommit(
                    rs.getInt("branch_id"),
                    rs.getInt("commit_id"),
                    rs.getString("hash_val"),
                    rs.getTimestamp("commit_time"),
                    rs.getString("committer"));
            return gitCommit;
        }
        throw new Exception();
    }

    public static List<GitCommit> getAllGitCommitFromResult(ResultSet rs) throws Exception {
        List<GitCommit> list = new ArrayList<>();
        while(rs.next()){
            GitCommit gitCommit = new GitCommit(
                    rs.getInt("branch_id"),
                    rs.getInt("commit_id"),
                    rs.getString("hash_val"),
                    rs.getTimestamp("commit_time"),
                    rs.getString("committer"));
            list.add(gitCommit);
        }
        return list;
    }

//    private static List<Integer> getAllCommit

    public static GitCommit selectGitCommitByCommitIdWithoutBranch(Integer commit_id) throws Exception {
        Connection connection = Connect.getConnection();
        String sql = "select * from git_commit where commit_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,commit_id);
        ResultSet rs = ps.executeQuery();
        return getFirstGitCommitFromResult(rs);
    }
    public static GitCommit selectGitCommitByCommitId(Integer commit_id) throws Exception {
        Connection connection = Connect.getConnection();
        Integer branch_id = BranchView.getCurrentBranch();
        String sql = "select * from git_commit where commit_id=? and branch_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,commit_id);
        ps.setInt(2,branch_id);
        ResultSet rs = ps.executeQuery();
        return getFirstGitCommitFromResult(rs);
    }

    public static GitCommit selectGitCommitByHashVal(String hash_val) throws Exception {
        Connection connection = Connect.getConnection();
        Integer branch_id = BranchView.getCurrentBranch();

        String sql = "select * from git_commit where hash_val=? and branch_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1,hash_val);
        ps.setInt(2,branch_id);
        ResultSet rs = ps.executeQuery();
        return getFirstGitCommitFromResult(rs);
    }

    public static GitCommit getLatestCommit() throws Exception {
        Connection connection = Connect.getConnection();
        Integer branch_id = BranchView.getCurrentBranch();
        String sql = "select * from git_commit,(select max(commit_time) AS max_time from git_commit where branch_id=?)AS b WHERE git_commit.commit_time = b.max_time and branch_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,branch_id);
        ps.setInt(2,branch_id);
        ResultSet rs = ps.executeQuery();
        return getFirstGitCommitFromResult(rs);
    }



    public static List<GitCommit> getAllCommitUntilOneCommit(GitCommit gitCommit) throws Exception {
        Connection connection = Connect.getConnection();
        Integer branch_id = BranchView.getCurrentBranch();
        String sql = "select * from git_commit where commit_time<=? and branch_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setTimestamp(1,gitCommit.getCommit_time());
        ps.setInt(2,branch_id);
        ResultSet rs = ps.executeQuery();
        return getAllGitCommitFromResult(rs);
    }

    public static List<GitCommit> getAllCommitBetween(Timestamp start_time, Timestamp end_time) throws Exception {
        Connection connection = Connect.getConnection();
        Integer branch_id = BranchView.getCurrentBranch();
        String sql = "select * from git_commit where branch_id=? and commit_time between ? and ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,branch_id);
        ps.setTimestamp(2,start_time);
        ps.setTimestamp(3,end_time);
        ResultSet rs = ps.executeQuery();
        return getAllGitCommitFromResult(rs);
    }
}

package crud;

import entity.GitCommit;
import entity.IssueCase;
import entity.IssueInstance;
import init.Connect;

import java.sql.*;
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


    public static GitCommit selectGitCommitByCommitId(Integer commit_id) throws Exception {
        Connection connection = Connect.getConnection();
        String sql = "select * from git_commit where commit_id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,commit_id);
        ResultSet rs = ps.executeQuery();
        return getFirstGitCommitFromResult(rs);
    }

    public static GitCommit selectGitCommitByHashVal(String hash_val) throws Exception {
        Connection connection = Connect.getConnection();
        String sql = "select * from git_commit where hash_val=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1,hash_val);
        ResultSet rs = ps.executeQuery();
        return getFirstGitCommitFromResult(rs);
    }

    public static GitCommit getLatestCommit() throws Exception {
        Connection connection = Connect.getConnection();
        String sql = "select * from git_commit,(select max(commit_time) AS max_time from git_commit )AS b WHERE git_commit.commit_time = b.max_time";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        return getFirstGitCommitFromResult(rs);
    }



}

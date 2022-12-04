import crud.BranchCRUD;
import crud.GitCommitCRUD;
import crud.RepositoryCRUD;
import entity.Branch;
import entity.GitCommit;
import entity.Repository;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class CRUDTest {
    public static void allTest(){
//        RepositoryTest();
//        BranchTest();
//        CommitTest1();
//        CommitTest2();
    }

    public static void RepositoryTest(){
        Repository repository = new Repository(0,"repo1","this is repo1","/C:");
        try {
            Integer id = RepositoryCRUD.insertRepository(repository);
            System.out.println(id.toString());
            System.out.println("add repo1 succ");
        }catch (Exception e){
            System.out.println("fail");
            e.printStackTrace();
        }
    }

    public static void BranchTest(){
        Branch branch = new Branch(0,1,"branch1","main branch");
        try {
            Integer id = BranchCRUD.insertBranch(branch);
            System.out.println(id.toString());
            System.out.println("add branch1 succ");
        }catch (Exception e){
            System.out.println("fail");
            e.printStackTrace();
        }
    }

    public static void CommitTest1(){
        try {
            String date = "2022-01-01 00:00:01";
            Timestamp timeStamp=new Timestamp(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date).getTime());
            GitCommit gitCommit = new GitCommit(1,0,"abcde",timeStamp,"zjx");

            Integer id = GitCommitCRUD.insertGitCommit(gitCommit);
            System.out.println(id.toString());
            System.out.println("add commit1:abcde succ");
        }catch (Exception e){
            System.out.println("fail");
            e.printStackTrace();
        }
    }

    public static void CommitTest2(){
        try {
            String date = "2022-02-02 00:00:01";
            Timestamp timeStamp=new Timestamp(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date).getTime());
            GitCommit gitCommit = new GitCommit(1,0,"fghijk",timeStamp,"zjx");

            Integer id = GitCommitCRUD.insertGitCommit(gitCommit);
            System.out.println(id.toString());
            System.out.println("add commit2:fghijk succ");
        }catch (Exception e){
            System.out.println("fail");
            e.printStackTrace();
        }
    }


}

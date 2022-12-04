import common.CASE_TYPE;
import common.INSTANCE_STATUS;
import crud.*;
import entity.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class CRUDTest {
    public static void allTest(){
        RepositoryTest();
        BranchTest();
        CommitTest1();
        CommitTest2();
        CaseTest1();
        CaseTest2();
        InstanceTest();
        LocationTest();
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

    public static void CaseTest1(){
        try {
            Integer id = IssueCaseCRUD.insertIssueCase(1, CASE_TYPE.SMELL);
            System.out.println(id.toString());
            System.out.println("add issue_case1 succ");
        }catch (Exception e){
            System.out.println("fail");
            e.printStackTrace();
        }
    }

    public static void CaseTest2(){
        try {
            IssueCaseCRUD.solveIssueCase(1, 2);
            System.out.println("update issue_case1 succ");
        }catch (Exception e){
            System.out.println("fail");
            e.printStackTrace();
        }
    }

    public static void InstanceTest(){
        try {
            IssueInstance issueInstance = new IssueInstance(null,1,1, INSTANCE_STATUS.APPEAR,"C:/","This is a smell");
            Integer id = IssueInstanceCRUD.insertIssueInstance(issueInstance);
            System.out.println(id.toString());
            System.out.println("insert issue_instance1 succ");

            issueInstance.setInstance_status(INSTANCE_STATUS.DISAPPEAR);
            issueInstance.setCommit_id(2);
            id = IssueInstanceCRUD.insertIssueInstance(issueInstance);
            System.out.println(id.toString());
            System.out.println("insert issue_instance2 succ");
        }catch (Exception e){
            System.out.println("fail");
            e.printStackTrace();
        }
    }

    public static void LocationTest(){
        try {
            IssueLocation issueLocation1 = new IssueLocation(1,1,1,2);
            IssueLocationCRUD.insertIssueLocation(issueLocation1);
            System.out.println("insert issue_location1 succ");

            IssueLocation issueLocation2 = new IssueLocation(1,2,5,6);
            IssueLocationCRUD.insertIssueLocation(issueLocation2);
            System.out.println("insert issue_location2 succ");


        }catch (Exception e){
            System.out.println("fail");
            e.printStackTrace();
        }
    }


}

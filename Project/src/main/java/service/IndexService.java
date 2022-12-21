package service;


import common.ExtendedInstance;
import crud.GitCommitCRUD;
import crud.IssueCaseCRUD;
import crud.IssueInstanceCRUD;
import entity.GitCommit;
import entity.IssueCase;
import init.BranchView;
import sort.GitCommitTimeComparator;

import java.sql.Timestamp;
import java.util.*;

public class IndexService {
    private static GitCommit gitCommit;

    public static void compare(){
        if(!BranchView.isValid())return;
        Scanner s = new Scanner(System.in);
        System.out.println("是否使用索引?(y/n)");
        String use = s.nextLine();
        switch (use){
            case "y":
                compareCommitService(true);
                compareTimeService(true);
                break;
            case "n":
                compareCommitService(false);
                compareTimeService(false);
                break;
        }
    }

    public static void compareCommitService(boolean use_index){
        if (use_index) {
            System.out.println("------------------------------------");
            System.out.println("现在使用索引，模拟CommitService，查看最新版本的引入和消除，以及累积情况");
        }else {
            System.out.println("------------------------------------");
            System.out.println("现在禁用索引，模拟CommitService，查看最新版本的引入和消除，以及累积情况");
        }

        imitateCommitService(use_index);
        System.out.println("------------------------------------");

    }

    private static void imitateCommitService(boolean use_index){
        //直接模拟查看最新版本的累积/增量
        long startTime = System.currentTimeMillis();
        try{
            gitCommit = GitCommitCRUD.getLatestCommit();
        } catch (Exception e){
            //e.printStackTrace();
            System.out.println("获取最新commit失败");
            return;
        }
        Step2SelectCase(use_index);
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
    }

    private static void Step2SelectCase(boolean use_index){
        Scanner s = new Scanner(System.in);
        try{
            List<IssueCase> appearList = IssueCaseCRUD.getAppearIssueCaseByCommitNoCache(gitCommit,use_index);
            List<IssueCase> solveList = IssueCaseCRUD.getSolvedIssueCaseByCommitNoCache(gitCommit,use_index);
            calDetailList(use_index);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("在Step2SelectCase中出现异常");
        }
    }

    private static void calDetailList(boolean use_index) throws Exception{
        //先拿到所有的commit
        List<GitCommit> commits = GitCommitCRUD.getAllCommitUntilOneCommitNoCache(gitCommit);
        commits.sort(new GitCommitTimeComparator());
        for(GitCommit commit:commits){
            //逐个考虑commit，把每个commit的所有issue_instance拿出来
            List<ExtendedInstance> extendedInstances = IssueInstanceCRUD.getAllExtendedInstanceByCommitIdNoCache(commit.getCommit_id(),use_index);
        }
    }
    private static Timestamp start_time, end_time;
    public static void compareTimeService(boolean use_index){
        try {
            Step1DecideTime();
            List<GitCommit> commits = GitCommitCRUD.getAllCommitBetween(start_time,end_time,true);
            if(commits.isEmpty()){
                System.out.println("该时间段内无commit");
                return;
            }
            commits.sort(new GitCommitTimeComparator());
            System.out.printf("这段时间内有 %d 个commit\n",commits.size());
            Step2CalChange(commits,use_index);
        } catch (Exception e) {

        }
    }
    private static void Step1DecideTime()throws Exception{
        Scanner s = new Scanner(System.in);
        System.out.println("\n---------------------");
        System.out.println("请按照YYYY-MM-DD HH:MM:SS的格式输入时间");
        System.out.println("请输入起始时间");
        start_time = Timestamp.valueOf(s.nextLine());
        System.out.println("请输入终止时间");
        end_time = Timestamp.valueOf(s.nextLine());
    }

    private static void Step2CalChange(List<GitCommit> commits, boolean use_index) throws Exception {
        for(GitCommit commit:commits){
            //逐个考虑commit，把每个commit的所有issue_instance拿出来
            List<ExtendedInstance> extendedInstances = IssueInstanceCRUD.getAllExtendedInstanceByCommitIdNoCache(commit.getCommit_id(),use_index);
        }

    }


}

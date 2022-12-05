package service;

import common.ExtendedInstance;
import common.TimeUtil;
import crud.GitCommitCRUD;
import crud.IssueCaseCRUD;
import crud.IssueInstanceCRUD;
import entity.GitCommit;
import entity.IssueCase;
import entity.IssueInstance;
import sort.GitCommitTimeComparator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

public class TimeService {
    private static Timestamp start_time, end_time;

    public static void timeInterAct() {
        try {
            Step1DecideTime();
            List<GitCommit> commits = GitCommitCRUD.getAllCommitBetween(start_time,end_time);
            if(commits.isEmpty()){
                System.out.println("该时间段内无commit");
                return;
            }
            commits.sort(new GitCommitTimeComparator());
            System.out.printf("这段时间内有 %d 个commit\n",commits.size());
            Step2CalChange(commits);
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

    private static void Step2CalChange(List<GitCommit> commits) throws Exception {
        TreeMap<Integer, ExtendedInstance> appear_map = new TreeMap<>();
        List<ExtendedInstance> solve_list = new ArrayList<>();
        Integer appear_and_solve=0;
        Integer appear=0;
        for(GitCommit commit:commits){
            //逐个考虑commit，把每个commit的所有issue_instance拿出来
            List<ExtendedInstance> extendedInstances = IssueInstanceCRUD.getAllExtendedInstanceByCommitId(commit.getCommit_id());
            for(ExtendedInstance instance:extendedInstances){
                switch (instance.getInstance_status()){
                    case APPEAR:
                        appear_map.put(instance.getIssue_case_id(),instance);
                        break;
                    case UPDATE:
                        break;
                    case DISAPPEAR:
                        if(appear_map.containsKey(instance.getIssue_case_id())){
                            appear_and_solve++;
                        }
                        solve_list.add(instance);
                        break;
                    default:
                        System.out.println("error in Step2CalChange()");
                        throw new Exception();
                }
            }
        }
        List<ExtendedInstance> appear_list = new ArrayList<>(appear_map.values());
        appear = appear_list.size();
        System.out.println("--------------------");
        System.out.println("--这段时间内新引入的缺陷：");
        PrintNewIssueCaseInfoByTypeAndTotal(appear_list);

        System.out.println("--------------------");
        System.out.println("--这段时间解决的缺陷：");
        PrintSolvedIssueCaseInfoByTypeAndTotal(solve_list);
        if(appear>0)
        System.out.printf("--这段时间内新引入缺陷的解决率：%.2f%%",appear_and_solve*1.0/appear);
    }

    private static void PrintNewIssueCaseInfoByTypeAndTotal(List<ExtendedInstance> appear_list) throws Exception {
        Integer bugNum=0,smellNum=0,sechotNum=0,vulnNum=0;
        for (ExtendedInstance instance : appear_list){
            IssueCase issueCase = IssueCaseCRUD.getIssueCaseByInstanceID(instance.getIssue_case_id());
            switch (issueCase.getType()){
                case BUG:bugNum++;break;
                case SECHOT:sechotNum++;break;
                case SMELL:smellNum++;break;
                case VULN:vulnNum++;break;
                default:
                    System.out.println("error type in PrintNewIssueCaseInfoByTypeAndTotal()");
            }
        }
        System.out.println("Bug    : "+bugNum.toString());
        System.out.println("Smell  : "+smellNum.toString());
        System.out.println("Sechot : "+sechotNum.toString());
        System.out.println("Vuln   : "+vulnNum.toString());
        System.out.println("--Total  : "+appear_list.size());
    }

    private static void PrintSolvedIssueCaseInfoByTypeAndTotal(List<ExtendedInstance> solve_list) throws Exception {
        List<ExtendedInstance> bugs = new ArrayList<>();
        List<ExtendedInstance> smells = new ArrayList<>();
        List<ExtendedInstance> sechots = new ArrayList<>();
        List<ExtendedInstance> vulns = new ArrayList<>();

        for(ExtendedInstance instance: solve_list){
            switch (instance.getType()){
                case BUG:bugs.add(instance);break;
                case SMELL:smells.add(instance);break;
                case SECHOT:sechots.add(instance);break;
                case VULN:vulns.add(instance);break;
            }
        }
        Integer bugNum= bugs.size(), smellNum=smells.size(),sechotNum=sechots.size(),vulnNum=vulns.size();
        //思考一下这里会不会有溢出的问题
        Long bugs_time_cost=0L,smells_time_cost=0L,sechots_time_cost=0L,vuln_time_cost=0L;
        System.out.println("Bug    : "+bugNum);
        if(bugNum>0){
            bugs_time_cost = SolvedCasesTotalTimeCost(bugs);
            System.out.printf("--Bug修复平均耗时: %s\n", TimeUtil.getTimeDifferenceString(bugs_time_cost/bugNum));
        }
        System.out.println("Smell  : "+smellNum);
        if(smellNum>0){
            smells_time_cost = SolvedCasesTotalTimeCost(smells);
            System.out.printf("--Smell修复平均耗时: %s\n", TimeUtil.getTimeDifferenceString(smells_time_cost/smellNum));
        }
        System.out.println("Sechot : "+sechotNum);
        if(sechotNum>0){
            sechots_time_cost = SolvedCasesTotalTimeCost(sechots);
            System.out.printf("--Sechot修复平均耗时: %s\n", TimeUtil.getTimeDifferenceString(sechots_time_cost/sechotNum));
        }
        System.out.println("Vuln   : "+vulnNum);
        if(vulnNum>0){
            vuln_time_cost = SolvedCasesTotalTimeCost(vulns);
            System.out.printf("--Vuln修复平均耗时: %s\n", TimeUtil.getTimeDifferenceString(vuln_time_cost/vulnNum));
        }
        System.out.println("--Total  : "+solve_list.size());
        Integer totalNum = bugNum+smellNum+sechotNum+vulnNum;
        Long total_time = bugs_time_cost+smells_time_cost+sechots_time_cost+vuln_time_cost;
        if(totalNum>0){
            System.out.printf("--总修复平均耗时: %s\n", TimeUtil.getTimeDifferenceString(total_time/totalNum));
        }
    }

    private static Long SolvedCasesTotalTimeCost(List<ExtendedInstance> instances) throws Exception {
        Long ans = 0L;
        for (ExtendedInstance instance:instances){
            IssueCase issueCase = IssueCaseCRUD.getIssueCaseByInstanceID(instance.getIssue_case_id());
            ans += issueCase.getSolve_time().getTime() - issueCase.getAppear_time().getTime();
        }
        return ans;
    }


}

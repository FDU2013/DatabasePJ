package service;

import common.ExtendedInstance;
import common.TimeUtil;
import crud.GitCommitCRUD;
import crud.IssueCaseCRUD;
import crud.IssueInstanceCRUD;
import crud.IssueLocationCRUD;
import entity.GitCommit;
import entity.IssueCase;
import entity.IssueLocation;
import sort.ExtendedInstanceTimeComparator;
import sort.GitCommitTimeComparator;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

public class CommitService {
    private static GitCommit gitCommit;
    public static void commitInterAct(){
        System.out.println("---------------------");
        System.out.println("--latest 查看最新版本的累积/增量");
        System.out.println("--choose 查看指定版本的累积/增量");
        System.out.println("--exit 返回");
        System.out.println("请输入指令：");

        Step1DecideCommit();
    }

    private static void Step1DecideCommit(){
        Scanner s = new Scanner(System.in);
        //1.处理得到对应的commit
        switch(s.nextLine()){
            case "latest":
                try{
                    gitCommit = GitCommitCRUD.getLatestCommit();
                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println("获取最新commit失败");
                    return;
                }
                break;
            case "choose":
                System.out.println("hash/commit_id?(输入h/c)");
                try{
                    String horc = s.nextLine();
                    System.out.println("请输入对应的值：");
                    switch(horc){
                        case "h":
                            String hash_val = s.nextLine();
                            gitCommit = GitCommitCRUD.selectGitCommitByHashVal(hash_val);
                            break;
                        case "c":
                            Integer commit_id = Integer.parseInt(s.nextLine());
                            gitCommit = GitCommitCRUD.selectGitCommitByCommitId(commit_id);
                            break;
                        default:
                            System.out.println("错误的指令");
                            return;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println("获取对应commit失败");
                    return;
                }
                break;
            case "exit":
                System.out.println("返回");
                return;
            default:
                System.out.println("错误的指令");
                return;
        }
        System.out.println(gitCommit.getCommit_id());

        Step2SelectCase();
    }

    private static void Step2SelectCase(){
        Scanner s = new Scanner(System.in);
        try{

            System.out.println("-----------------");
            System.out.println("--新引入的缺陷：");
            List<IssueCase> appearList = IssueCaseCRUD.getAppearIssueCaseByCommit(gitCommit);
            PrintIssueCaseNumByType(appearList);

            System.out.println("-----------------");
            System.out.println("--解决的缺陷：");
            List<IssueCase> solveList = IssueCaseCRUD.getSolvedIssueCaseByCommit(gitCommit);
            PrintIssueCaseNumByType(solveList);
            System.out.println("-----------------");
            System.out.println("--详细列表");
            calDetailList();
            System.out.println("-----------------");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("在Step2SelectCase中出现异常");
        }

    }

    private static void calDetailList() throws Exception{
        //先拿到所有的commit
        List<GitCommit> commits = GitCommitCRUD.getAllCommitUntilOneCommit(gitCommit);
        commits.sort(new GitCommitTimeComparator());
        TreeMap<Integer, ExtendedInstance> map = new TreeMap<>();
        for(GitCommit commit:commits){
            //逐个考虑commit，把每个commit的所有issue_instance拿出来
            List<ExtendedInstance> extendedInstances = IssueInstanceCRUD.getAllExtendedInstanceByCommitId(commit.getCommit_id());
            for(ExtendedInstance instance:extendedInstances){
                switch (instance.getInstance_status()){
                    case APPEAR:
                        instance.setLatest_instance_id(instance.getIssue_instance_id());
                        map.put(instance.getIssue_case_id(),instance);
                        break;
                    case UPDATE:
                        map.get(instance.getIssue_case_id()).setLatest_instance_id(instance.getIssue_instance_id());
                        break;
                    case DISAPPEAR:
                        map.remove(instance.getIssue_case_id());
                        break;
                    default:
                        System.out.println("error in 详细列表");
                        throw new Exception();
                }
            }
        }
        List<ExtendedInstance> existingInstances = new ArrayList<>(map.values());
        map.clear();
        PrintExistingIssueByTypeOrderByTime(existingInstances);
    }




    private static void PrintExistingIssueByTypeOrderByTime(List<ExtendedInstance> existingInstances){
        List<ExtendedInstance> bugs = new ArrayList<>();
        List<ExtendedInstance> smells = new ArrayList<>();
        List<ExtendedInstance> sechots = new ArrayList<>();
        List<ExtendedInstance> vulns = new ArrayList<>();
        for(ExtendedInstance instance: existingInstances){
            switch (instance.getType()){
                case BUG:bugs.add(instance);break;
                case SMELL:smells.add(instance);break;
                case SECHOT:sechots.add(instance);break;
                case VULN:vulns.add(instance);break;
            }
        }
        System.out.println("------BUG------");
        PrintInstanceByTime(bugs);
        System.out.println("------SMELL------");
        PrintInstanceByTime(smells);
        System.out.println("------SECHOT------");
        PrintInstanceByTime(sechots);
        System.out.println("------VULN------");
        PrintInstanceByTime(vulns);
    }
    private static void PrintInstanceByTime(List<ExtendedInstance> list){
        if(list.isEmpty()){
            System.out.println("== 无 ==");
            return;
        }
        list.sort(new ExtendedInstanceTimeComparator());
        Timestamp now_time = new Timestamp(System.currentTimeMillis());
        long diffsum=0;
        long midtime;
        int size = list.size();
        System.out.println("缺陷ID--|------引入时间--------|---存续时间---|-----------文件路径及行号-----------");
        for(ExtendedInstance instance:list){
            String appear_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(instance.getAppear_time());
            long diff = TimeUtil.getTimeDifference(now_time,instance.getAppear_time());
            diffsum+=diff;
            System.out.printf("%-6d | %s | %s | %s  ",instance.getIssue_case_id(),appear_time,TimeUtil.getTimeDifferenceString(diff),instance.getFile_path());
            try{
                IssueLocation.PrintLocationList(IssueLocationCRUD.getLocationByInstanceId(instance.getLatest_instance_id()));
            }catch (Exception e){
                e.printStackTrace();
            }
            System.out.println("");
        }
        System.out.println("--平均存续时间  ："+TimeUtil.getTimeDifferenceString(diffsum/size));
        if(size%2==0){
            midtime = list.get(size/2).getAppear_time().getTime() + list.get(size/2-1).getAppear_time().getTime();
            midtime/=2;
        }else{
            midtime = list.get(size/2).getAppear_time().getTime();
        }
        System.out.println("--存续时间中位数："+TimeUtil.getTimeDifferenceString(now_time.getTime()-midtime));
    }


    private static void PrintIssueCaseNumByType(List<IssueCase> list){
        Integer bugNum=0,smellNum=0,sechotNum=0,vulnNum=0;
        for (IssueCase issueCase : list){
            switch (issueCase.getType()){
                case BUG:bugNum++;break;
                case SECHOT:sechotNum++;break;
                case SMELL:smellNum++;break;
                case VULN:vulnNum++;break;
                default:
                    System.out.println("error type in PrintIssueCaseNumByType()");
            }
        }
        System.out.println("Bug    : "+bugNum.toString());
        System.out.println("Smell  : "+smellNum.toString());
        System.out.println("Sechot : "+sechotNum.toString());
        System.out.println("Vuln   : "+vulnNum.toString());
        System.out.println("--Total  : "+list.size());
    }



}

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
import init.BranchView;
import sort.ExtendedInstanceTimeComparator;
import sort.GitCommitTimeComparator;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommitService {
    private static GitCommit gitCommit;
    public static void commitInterAct(){
        if(!BranchView.isValid())return;
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
                    //e.printStackTrace();
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
                    //e.printStackTrace();
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
            List<IssueCase> appearList = IssueCaseCRUD.getAppearIssueCaseByCommit(gitCommit,true);
            PrintIssueCaseNumByType(appearList);

            System.out.println("-----------------");
            System.out.println("--解决的缺陷：");
            List<IssueCase> solveList = IssueCaseCRUD.getSolvedIssueCaseByCommit(gitCommit,true);
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
        Set<Integer> delete_set = new HashSet<>();
        for(GitCommit commit:commits){
            //逐个考虑commit，把每个commit的所有issue_instance拿出来
            List<ExtendedInstance> extendedInstances = IssueInstanceCRUD.getAllExtendedInstanceByCommitId(commit.getCommit_id(),true);
            for(ExtendedInstance instance:extendedInstances){
                switch (instance.getInstance_status()){
                    case APPEAR:
                        instance.setLatest_instance_id(instance.getIssue_instance_id());
                        map.put(instance.getIssue_case_id(), new ExtendedInstance(instance));
                        break;
                    case UPDATE:
                        if(delete_set.contains(instance.getIssue_case_id())){
                            System.out.println("case_id: "+instance.getIssue_case_id());
                            System.out.println("case_instance: "+instance.getIssue_instance_id());
                        }
                        assert !delete_set.contains(instance.getIssue_case_id());
                        try{
                            map.get(instance.getIssue_case_id()).setLatest_instance_id(instance.getIssue_instance_id());
                        }catch (Exception e){
                            System.out.println("case_id: "+instance.getIssue_case_id());
                            System.out.println("case_instance: "+instance.getIssue_instance_id());
                            throw new Exception();
                        }
                        //map.get(instance.getIssue_case_id()).setLatest_instance_id(instance.getIssue_instance_id());
                        break;
                    case DISAPPEAR:
                        map.remove(instance.getIssue_case_id());
                        delete_set.add(instance.getIssue_case_id());
                        break;
                    default:
                        System.out.println("error in 详细列表");
                        throw new Exception();
                }
            }
        }
        List<ExtendedInstance> existingInstances = new ArrayList<>(map.values());
        map.clear();
        PrintExistingIssueByTypeOrderByTime(existingInstances,true);
    }




    public static void PrintExistingIssueByTypeOrderByTime(List<ExtendedInstance> existingInstances,boolean show_time_diff_to_now){
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
        PrintInstanceByTime(bugs,show_time_diff_to_now);
        System.out.println("------SMELL------");
        PrintInstanceByTime(smells,show_time_diff_to_now);
        System.out.println("------SECHOT------");
        PrintInstanceByTime(sechots,show_time_diff_to_now);
        System.out.println("------VULN------");
        PrintInstanceByTime(vulns,show_time_diff_to_now);

        if(show_time_diff_to_now){
            Scanner s = new Scanner(System.in);
            Integer days,hours;
            System.out.println("是否筛选存续时长较久的缺陷？(y/n)");
            if("y".equals(s.nextLine())){
                System.out.println("请输入持续天数");
                days = Integer.parseInt(s.nextLine());
                System.out.println("请再输入小时");
                hours = Integer.parseInt(s.nextLine());
            }else return;
            Long diff_limit = (hours+ 24L *days)*(1000*60*60L);
            System.out.println("------BUG------");
            PrintLivingTimeGreaterThan(bugs,diff_limit);
            System.out.println("------SMELL------");
            PrintLivingTimeGreaterThan(smells,diff_limit);
            System.out.println("------SECHOT------");
            PrintLivingTimeGreaterThan(sechots,diff_limit);
            System.out.println("------VULN------");
            PrintLivingTimeGreaterThan(vulns,diff_limit);
        }


    }
    private static void PrintInstanceByTime(List<ExtendedInstance> list,boolean show_time_diff_to_now){
        if(list.isEmpty()){
            System.out.println("== 无 ==");
            return;
        }
        list.sort(new ExtendedInstanceTimeComparator());
        Timestamp now_time = new Timestamp(System.currentTimeMillis());
        long diffsum=0;
        long midtime;
        int size = list.size();
        if(show_time_diff_to_now){
            System.out.println("缺陷ID--|------引入时间--------|---引入者---|---存续时间---|-----------文件路径及行号-----------");
        }else {
            System.out.println("缺陷ID--|------引入时间--------|---引入者---|-----------文件路径-----------");
        }

        for(ExtendedInstance instance:list){
            String appear_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(instance.getAppear_time());
            if(show_time_diff_to_now){
                long diff = TimeUtil.getTimeDifference(now_time,instance.getAppear_time());
                diffsum+=diff;
                System.out.printf("%-6d | %s | %-9s | %s | %s  ",instance.getIssue_case_id(),appear_time,instance.getAppear_committer(),TimeUtil.getTimeDifferenceString(diff),instance.getFile_path());
                try{
                    IssueLocation.PrintLocationList(IssueLocationCRUD.getLocationByInstanceId(instance.getLatest_instance_id()));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else {
                System.out.printf("%-6d | %s | %-9s | %s  ",instance.getIssue_case_id(),appear_time,instance.getAppear_committer(),instance.getFile_path());
            }

            System.out.println("");
        }
        if(show_time_diff_to_now){
            System.out.println("--平均存续时间  ："+TimeUtil.getTimeDifferenceString(diffsum/size));
            if(size%2==0){
                midtime = list.get(size/2).getAppear_time().getTime() + list.get(size/2-1).getAppear_time().getTime();
                midtime/=2;
            }else{
                midtime = list.get(size/2).getAppear_time().getTime();
            }
            System.out.println("--存续时间中位数："+TimeUtil.getTimeDifferenceString(now_time.getTime()-midtime));
        }
    }

    private static void PrintLivingTimeGreaterThan(List<ExtendedInstance> list,Long diff_limit) {

        Timestamp now_time = new Timestamp(System.currentTimeMillis());
        System.out.println("缺陷ID--|------引入时间--------|---引入者---|---存续时间---|-----------文件路径及行号-----------");
        for(ExtendedInstance instance:list){
            String appear_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(instance.getAppear_time());
            long diff = TimeUtil.getTimeDifference(now_time,instance.getAppear_time());
            if(diff<diff_limit)continue;
            System.out.printf("%-6d | %s | %-9s | %s | %s  ",instance.getIssue_case_id(),appear_time,instance.getAppear_committer(),TimeUtil.getTimeDifferenceString(diff),instance.getFile_path());
            try{
                IssueLocation.PrintLocationList(IssueLocationCRUD.getLocationByInstanceId(instance.getLatest_instance_id()));
            }catch (Exception e){
                e.printStackTrace();
            }
            System.out.println("");
        }
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

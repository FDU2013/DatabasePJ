package service;

import common.ExtendedInstance;
import common.TimeUtil;
import crud.IssueCaseCRUD;
import entity.IssueCase;
import init.BranchView;
import sort.SolvedCaseExistingTimeComparator;
import sort.UnsolvedCaseLivingTimeComparator;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CommitterService {
    private static String committer;
    public static void committerInterAct() {
        if(!BranchView.isValid())return;
        Step1DecideName();
        try {
            System.out.println("---------------------");
            System.out.println("--自己引入自己解决的缺陷：");
            List<IssueCase> self_produce_self_solve = IssueCaseCRUD.getSelfProducedAndSelfSolvedIssueCaseByCommitter(committer);
            PrintSolvedDetail(self_produce_self_solve);
            if(!WaitForContinue())return;

            System.out.println("---------------------");
            System.out.println("--自己引入他人解决的缺陷：");
            List<IssueCase> self_produce_others_solve = IssueCaseCRUD.getSelfProducedAndOthersSolvedIssueCaseByCommitter(committer);
            PrintSolvedDetail(self_produce_others_solve);
            if(!WaitForContinue())return;

            System.out.println("---------------------");
            System.out.println("--他人引入自己解决的缺陷：");
            List<IssueCase> others_produce_self_solve = IssueCaseCRUD.getOthersProducedAndSelfSolvedIssueCaseByCommitter(committer);
            PrintSolvedDetail(others_produce_self_solve);
            if(!WaitForContinue())return;

            System.out.println("---------------------");
            System.out.println("--自己引入还未解决的缺陷：");
            List<IssueCase> self_produce_not_solve = IssueCaseCRUD.getSelfProducedAndNotSolvedIssueCaseByCommitter(committer);
            PrintNotSolvedDetail(self_produce_not_solve);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static boolean WaitForContinue() {
        Scanner s = new Scanner(System.in);
        System.out.println("---------------------");
        System.out.println("--按c继续查看,按e结束：");
        while(true){
            switch (s.nextLine()){
                case "c":return true;
                case "e":return false;
            }
        }
    }

    private static void PrintNotSolvedDetail(List<IssueCase> unsolved_cases) throws Exception {
        List<IssueCase> bugs = new ArrayList<>();
        List<IssueCase> smells = new ArrayList<>();
        List<IssueCase> sechots = new ArrayList<>();
        List<IssueCase> vulns = new ArrayList<>();
        for(IssueCase issueCase: unsolved_cases){
            switch (issueCase.getType()){
                case BUG:bugs.add(issueCase);break;
                case SMELL:smells.add(issueCase);break;
                case SECHOT:sechots.add(issueCase);break;
                case VULN:vulns.add(issueCase);break;
            }
        }
        System.out.println("------BUG------");
        PrintNotSolvedInstances(bugs);
        System.out.println("------SMELL------");
        PrintNotSolvedInstances(smells);
        System.out.println("------SECHOT------");
        PrintNotSolvedInstances(sechots);
        System.out.println("------VULN------");
        PrintNotSolvedInstances(vulns);
    }

    private static void PrintNotSolvedInstances(List<IssueCase> cases) throws Exception {
        if(cases.isEmpty()){
            System.out.println("== 无 ==");
            return;
        }
        Long total_time = 0L;
        Timestamp now_time = new Timestamp(System.currentTimeMillis());
        System.out.println("缺陷ID--|------引入时间--------|---存续时间---|---引入者---|-----------文件路径-----------");
        cases.sort(new UnsolvedCaseLivingTimeComparator());
        for(IssueCase issueCase:cases){
            ExtendedInstance instance = IssueCaseCRUD.getExtendedInstanceByCaseId(issueCase.getIssue_case_id());
            String appear_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(instance.getAppear_time());
            long diff = TimeUtil.getTimeDifference(now_time,instance.getAppear_time());
            total_time+=diff;
            System.out.printf("%-6d | %s | %s | %-9s | %s ",instance.getIssue_case_id(),appear_time,TimeUtil.getTimeDifferenceString(diff),instance.getAppear_committer(),instance.getFile_path());
            System.out.println("");
        }
        Integer num = cases.size();
        System.out.printf("--总数： %d 个\n",num);
        System.out.printf("--存续时间平均值： %s \n",TimeUtil.getTimeDifferenceString(total_time/num));
        if(num%2==0){
            long mean_time = now_time.getTime() - cases.get(num/2).getAppear_time().getTime();
            mean_time += now_time.getTime() - cases.get(num/2-1).getAppear_time().getTime();
            mean_time/=2;
            System.out.printf("--存续时间中位数： %s \n",TimeUtil.getTimeDifferenceString(mean_time));
        }else {
            long mean_time = now_time.getTime() - cases.get(num/2).getAppear_time().getTime();
            System.out.printf("--存续时间中位数： %s \n",TimeUtil.getTimeDifferenceString(mean_time));
        }
    }

    private static void PrintSolvedDetail(List<IssueCase> solved_cases) throws Exception {
        List<IssueCase> bugs = new ArrayList<>();
        List<IssueCase> smells = new ArrayList<>();
        List<IssueCase> sechots = new ArrayList<>();
        List<IssueCase> vulns = new ArrayList<>();
        for(IssueCase issueCase: solved_cases){
            switch (issueCase.getType()){
                case BUG:bugs.add(issueCase);break;
                case SMELL:smells.add(issueCase);break;
                case SECHOT:sechots.add(issueCase);break;
                case VULN:vulns.add(issueCase);break;
            }
        }
        System.out.println("------BUG------");
        PrintSolvedInstances(bugs);
        System.out.println("------SMELL------");
        PrintSolvedInstances(smells);
        System.out.println("------SECHOT------");
        PrintSolvedInstances(sechots);
        System.out.println("------VULN------");
        PrintSolvedInstances(vulns);
    }

    private static void PrintSolvedInstances(List<IssueCase> cases) throws Exception {
        if(cases.isEmpty()){
            System.out.println("== 无 ==");
            return;
        }
        Long total_time = 0L;
        System.out.println("缺陷ID--|------引入时间--------|-------解决时间--------|--持续时间--|---引入者---|---解决者---|-----------文件路径-----------");
        cases.sort(new SolvedCaseExistingTimeComparator());
        for(IssueCase issueCase:cases){
            ExtendedInstance instance = IssueCaseCRUD.getExtendedInstanceByCaseId(issueCase.getIssue_case_id());
            String appear_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(instance.getAppear_time());
            String solve_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(instance.getSolve_time());
            long diff = TimeUtil.getTimeDifference(instance.getSolve_time(),instance.getAppear_time());
            total_time+=diff;
            System.out.printf("%-6d | %s | %s | %s | %-9s | %-9s | %s ",instance.getIssue_case_id(),appear_time,solve_time,TimeUtil.getTimeDifferenceString(diff),instance.getAppear_committer(),instance.getSolve_committer(),instance.getFile_path());
            System.out.println("");
        }
        Integer num = cases.size();
        System.out.printf("--总数： %d 个\n",num);
        System.out.printf("--存续时间平均值： %s \n",TimeUtil.getTimeDifferenceString(total_time/num));
        if(num%2==0){
            long mean_time = cases.get(num/2).getSolve_time().getTime() - cases.get(num/2).getAppear_time().getTime();
            mean_time += cases.get(num/2-1).getSolve_time().getTime() - cases.get(num/2-1).getAppear_time().getTime();
            mean_time/=2;
            System.out.printf("--存续时间中位数： %s \n",TimeUtil.getTimeDifferenceString(mean_time));
        }else {
            long mean_time = cases.get(num/2).getSolve_time().getTime() - cases.get(num/2).getAppear_time().getTime();
            System.out.printf("--存续时间中位数： %s \n",TimeUtil.getTimeDifferenceString(mean_time));
        }
    }

    private static void Step1DecideName(){
        Scanner s = new Scanner(System.in);
        System.out.println("\n---------------------");
        System.out.println("请输入要查询的committer：");
        committer = s.nextLine();

    }
}

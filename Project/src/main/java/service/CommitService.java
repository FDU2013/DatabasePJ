package service;

import crud.GitCommitCRUD;
import crud.IssueCaseCRUD;
import entity.GitCommit;
import entity.IssueCase;

import java.util.List;
import java.util.Scanner;

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
            //TODO

            System.out.println("-----------------");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("在Step2SelectCase中出现异常");
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

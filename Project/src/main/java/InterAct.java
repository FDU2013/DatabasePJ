import service.CommitService;
import service.CommitterService;
import service.TimeService;

import java.util.Scanner;

public class InterAct {
    public static void InterActWithUserCommand(){
        boolean exitFlag = false;
        Scanner s = new Scanner(System.in);
        while(!exitFlag){
            System.out.println("\n请输入指令：                 -------(输入help查看所有指令)");
            String command = s.nextLine();
            switch (command){
                case "help":
                    printHelp();
                    break;
                case "commit":
                    CommitService.commitInterAct();
                    break;
                case "time":
                    TimeService.timeInterAct();
                    break;
                case "committer":
                    CommitterService.committerInterAct();
                    break;
                case "exit":
                    exitFlag = true;
                    break;
                default:
                    System.out.println("错误的指令，输入help查看所有指令\n\n");
            }
        }

    }

    private static void printHelp(){
        System.out.println("--help   查看所有指令");
        System.out.println("--commit 查看指定版本");
        System.out.println("--time   查看指定时间段");
        System.out.println("--committer 查看指定程序员");
        System.out.println("--exit 退出");
    }



}

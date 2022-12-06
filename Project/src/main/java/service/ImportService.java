package service;

import java.util.Scanner;

public class ImportService {
    public static void ImportServiceInterAct(){
        Scanner s = new Scanner(System.in);
        System.out.println("---------------------");
        System.out.println("--请输入要导入的仓库路径");
        String path = s.nextLine();
        System.out.println("--请给该仓库命名");
        String repo_name = s.nextLine();


    }
}

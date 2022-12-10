package service;

import sonar.RepoImport;

import java.util.Scanner;

public class ImportService {
    public static void ImportServiceInterAct() {
        Scanner s = new Scanner(System.in);
        System.out.println("---------------------");
        System.out.println("--请输入要导入的仓库路径");
        String path = s.nextLine();
        System.out.println("--请给该仓库命名");
        String repo_name = s.nextLine();
        System.out.println("--仓库描述/备注");
        String description = s.nextLine();
        RepoImport repoImport = new RepoImport(repo_name, path);
        repoImport.importRepo(description);
        repoImport.importAllBranch();
        try {
            repoImport.importAllCommitAndIssue("refs/heads/master");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package init;

import crud.BranchCRUD;
import crud.RepositoryCRUD;
import entity.Branch;
import entity.Repository;

import java.util.Scanner;

public class BranchView {
    public static boolean vaild;
    public static Integer current_branch_id;
    public static Integer current_repo_id;
    public static Integer getCurrentRepo(){
        return current_repo_id;
    }
    public static Integer getCurrentBranch(){
        return current_branch_id;
    }

    public static void BranchViewInit(){
        vaild = false;
        Scanner s = new Scanner(System.in);
        System.out.println("是否现在就选择仓库/分支?(y/n)");
        if(!"y".equals(s.nextLine())){
            return;
        }
        ChooseBranch();
    }

    public static void ChooseBranch(){
        Scanner s = new Scanner(System.in);
        System.out.println("仓库名称/repository_id?(n/i)");
        try{
            if ("n".equals(s.nextLine())) {
                System.out.println("请输入仓库名称：");
                checkoutRepo(s.nextLine());
            }else {
                System.out.println("请输入仓库id：");
                checkoutRepo(Integer.parseInt(s.nextLine()));
            }
        }catch (Exception e){
            System.out.println("仓库不存在或存在异常");
            return;
        }

        System.out.println("分支名称/branch_id?(n/i)");
        try{
            if ("n".equals(s.nextLine())) {
                System.out.println("请输入分支名称：");
                checkout(s.nextLine());
            }else {
                System.out.println("请输入分支id：");
                checkout(Integer.parseInt(s.nextLine()));
            }
        }catch (Exception e){
            vaild = false;
            System.out.println("分支不存在或存在异常");
            return;
        }
        vaild = true;
    }


    public static void checkout(String branch_name) throws Exception {
        Branch branch;
        branch = BranchCRUD.getBranchByName(branch_name);

        current_branch_id = branch.getRepository_id();
        branch.print();
    }

    public static void checkout(Integer branch_id) throws Exception {
        Branch branch;
        branch = BranchCRUD.getBranchByID(branch_id);
        current_branch_id = branch.getRepository_id();
        branch.print();
    }

    public static void checkoutRepo(String repo_name) throws Exception {
        Repository repository;
        repository = RepositoryCRUD.getRepositoryByName(repo_name);
        current_repo_id = repository.getRepository_id();
        repository.print();
    }

    public static void checkoutRepo(Integer repo_id) throws Exception {
        Repository repository;
        repository = RepositoryCRUD.getRepositoryByID(repo_id);
        current_repo_id = repository.getRepository_id();
        repository.print();
    }

    public static boolean isValid() {
        if(vaild)return true;
        System.out.println("当前查看的repo/branch无效，请尝试checkout进入有效的仓库/分支");
        return false;
    }
}

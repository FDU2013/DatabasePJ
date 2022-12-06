import init.BranchView;
import init.Connect;
import init.CreateTable;



public class Launch {

    public static void main(String[] args) throws ClassNotFoundException {

        try{
            //和数据库建立连接
            Connect.connectToMySQL();
        } catch (Exception e){
            System.out.println("fail in connetToMySQL()");
            return;
        }
        //创建数据表
        CreateTable.createTable();
        BranchView.BranchViewInit();

        //CRUDTest.allTest();

        InterAct.InterActWithUserCommand();

    }


}

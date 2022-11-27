package init;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Scanner;

public class Connect {

    public static Connection connection;
    //mysql驱动包名
    private static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
    public static Connection getConnection(){
        return connection;
    }

    public static void connectToMySQL() throws Exception {
        Scanner s = new Scanner(System.in);
        //1.注册驱动
        Class.forName(DRIVER_NAME);
        //2.建立连接
        //数据库连接地址
        System.out.println("请输入数据库端口号/数据库名称，例：3306/dblab1");
        String URL = "jdbc:mysql://localhost:"+s.nextLine();
        //用户名
        System.out.println("请输入数据库账户");
        String USER_NAME = s.nextLine();
        //密码
        System.out.println("请输入数据库密码");
        String PASSWORD = s.nextLine();
        try{
            connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
        } catch (Exception e){
            System.out.println("connect to database fail!");
            throw new Exception();
        }
        if(connection!=null){
            System.out.println("succ connect!");
        } else{
            System.out.println("fail connect!");
            throw new Exception();
        }
    }
}
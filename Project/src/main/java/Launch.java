import java.io.*;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Launch {
    //mysql驱动包名
    private static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
    //相对地址
    //private static final String URL_BASE = "src/main/resources/";
    private static final String URL_BASE = "";
    //数据库连接对象
    private static Connection connection;

    public static void main(String[] args) throws ClassNotFoundException {
        //0.输入数据库信息
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
            return;
        }
        if(connection!=null){
            System.out.println("succ connect!");
        } else{
            System.out.println("fail connect!");
            return;
        }
        //3创建数据表
        try{
            runSqlByReadFileContent(URL_BASE+"sqlfile/create_table.sql", "UTF-8");
            System.out.println("create table successfully!");
        } catch (Exception e){
            System.out.println("create table fail!");
            return;
        }

    }




    /**
     * @方法描述：批量执行SQL语句
     * @param sql 包含待执行的SQL语句的ArrayList集合
     * @return int 是否成功执行
     */
    private static int batchDate(ArrayList<String> sql){
        try {
            Statement st = connection.createStatement();
            for(String subsql :sql){
                st.addBatch(subsql);
            }
            st.executeBatch();
            return 1;
        }catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 以行为单位读取文件，并将文件的每一行格式化到ArrayList中，常用于读面向行的格式化文件
     */
    private static ArrayList<String> readFileByLines(String filePath,String charsetName) throws Exception {
        ArrayList<String> listStr=new ArrayList<>();
        StringBuffer sb=new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filePath), charsetName));
            String tempString = null;
            int flag=0;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                // System.out.println("line " + line + ": " + tempString);
                if(tempString.trim().equals(""))
                    continue;
                if(tempString.substring(tempString.length()-1).equals(";")){
                    if(flag==1){
                        sb.append(tempString);
                        listStr.add(sb.toString());
                        sb.delete(0,sb.length());
                        flag=0;
                    }
                    else
                        listStr.add(tempString);
                }else{
                    flag=1;
                    sb.append(tempString);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        return listStr;
    }

    /**
     * 读取文件内容到SQL中执行
     * @param sqlPath SQL文件的路径：如：D:/TestProject/web/sql/脚本.Sql
     */
    private static void runSqlByReadFileContent(String sqlPath,String charsetName){
        try {
            ArrayList<String> sqlStr = readFileByLines(sqlPath, charsetName);
            if (sqlStr.size() > 0) {
                int num=batchDate(sqlStr);
                if(num>0)
                    System.out.println("执行成功");
                else
                    System.out.println("未有执行的SQL语句");
            }
            else{
                System.out.println("没有需要执行的SQL语句");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

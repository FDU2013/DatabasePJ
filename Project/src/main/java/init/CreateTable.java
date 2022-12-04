package init;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;

public class CreateTable {
    //相对地址
    //private static final String URL_BASE = "src/main/resources/";
    private static final String URL_BASE = "";

    public static void createTable(){
        try{
            runSqlByReadFileContent(URL_BASE+"src/main/resources/sqlfile/create_table.sql", "UTF-8");
        } catch (Exception e){
            System.out.println("create table fail!");
            return;
        }
        System.out.println("create table successfully!");
    }



    /**
     * @方法描述：批量执行SQL语句
     * @param sql 包含待执行的SQL语句的ArrayList集合
     * @return int 是否成功执行
     */
    private static int batchDate(ArrayList<String> sql){
        Connection connection = Connect.getConnection();
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

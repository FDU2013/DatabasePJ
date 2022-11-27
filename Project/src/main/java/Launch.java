import init.Connect;
import init.CreateTable;

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

        CRUDTest.allTest();

        InterAct.InterActWithUserCommand();

    }


}

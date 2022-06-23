package com.sequoiadp.testcommon;

/*
 * @Description   :
 * @Author        :  Fangjun
 * @CreateTime    : 2022/6/7 15:02
 * @LastEditTime  : 2022/6/7 15:02
 * @LastEditors   :
 */

import org.testng.annotations.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public abstract class SDPTestBase {
    @Parameters({"HOSTNAME", "PORT", "ROOTUSER", "ROOTPWD", "TESTUSER",
            "TESTPWD", "DBNAME", "TESTGROUP"})
    @BeforeSuite(alwaysRun = true)
    public static void initSuite(String HOSTNAME, String PORT, String ROOTUSER, String ROOTPWD, String TESTUSER, String TESTPWD, String DBNAME, String TESTGROUP) {
        ParaBeen.setConfig("hostName",HOSTNAME);
        ParaBeen.setConfig("port",PORT);
        ParaBeen.setConfig("rootUser",ROOTUSER);
        ParaBeen.setConfig("rootPwd",ROOTPWD);
        ParaBeen.setConfig("testUser",TESTUSER);
        ParaBeen.setConfig("testPwd",TESTPWD);
        ParaBeen.setConfig("dbName",DBNAME);
        ParaBeen.setConfig("testGroup",TESTGROUP);
        ParaBeen.setConfig("url","jdbc:hive2://" + HOSTNAME + ":" + PORT );
    }

    public static String getConfig(String key){
        return ParaBeen.getConfig(key);
    }

    @BeforeTest
    public void cleanandbuild() throws SQLException {
        Connection conn = HiveConnection.getInstance().getAdminConnect();
        Statement st;
        st = conn.createStatement();
        String clensdb="drop database if exists "+ getConfig("dbName") + " cascade;";
        st.executeQuery(clensdb);
        String createdbsql = "create database if not exists " + getConfig("dbName") + ";";
        st.executeQuery(createdbsql);
        String dropgrouosql = "drop group " + getConfig("testGroup") + ";";
        st.executeQuery(dropgrouosql);
        conn.close();
    }

    @AfterTest
    public void recycle() throws SQLException {
        Connection conn = HiveConnection.getInstance().getAdminConnect();
        Statement st;
        st = conn.createStatement();
        String clensdb=HiveConnection.getInstance().dropSql("database",getConfig("dbName"));
        st.executeQuery(clensdb);
        conn.close();
    }

    protected String tableName;
    private boolean hasUsage=true;
    private boolean hasGroup=false;
    private Connection conn;


    public void setTableName(String tablename){
        tableName=tablename;
    }

    protected void notUsage(){
        hasUsage=false;
    }

    protected void hasGroup(){
        hasGroup=true;
    }

    @BeforeClass
    public void setup() throws SQLException {
        conn = HiveConnection.getInstance().getAdminConnect();
        Statement st ;
        st = conn.createStatement();
        String usesql = "use " + getConfig("dbName") + ";" ;
        st.executeQuery(usesql);
        //建表
        String createtablsql = "create table if not exists " + tableName + "(id int,name varchar(20));";
        st.executeQuery(createtablsql);
        //插入数据
        String insertsql = "insert into " + tableName + " values(1001,'swtshs');";
        st.executeQuery(insertsql);
        if(hasUsage) {
            String grantDatabase = HiveConnection.getInstance().grantSql("usage","database",getConfig("dbName"),"user",getConfig("testUser"));
            st.executeQuery(grantDatabase);
        }
        if(hasGroup){
            String creategroup = "create group "+getConfig("testGroup") + " ;";
            st.executeQuery(creategroup);
        }
        st.close();
    }

    @AfterClass
    public void tearDown() throws SQLException {
        Statement st;
        st = conn.createStatement();
        String usageSql = HiveConnection.getInstance().usageSql(getConfig("dbName"));
        st.executeQuery(usageSql);
        if(hasGroup){
            String dropgroup = "drop group " + getConfig("testGroup") + ";";
            st.executeQuery(dropgroup);
        }
        if(hasUsage) {
            String revokeusagesql  = HiveConnection.getInstance().revokeDbFromUser("usage");
            st.executeQuery(revokeusagesql);
        }
        String droptablesql = HiveConnection.getInstance().dropSql("table",tableName);
        st.executeQuery(droptablesql);
        st.close();
    }

}

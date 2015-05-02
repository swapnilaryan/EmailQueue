/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Project;

import java.sql.*;
import java.util.Vector;
/**
 *
 * @author swapnilaryan
 */
public class SqlEmail 
{
    public static int MYSQL_PORT = 3306;    //Default Mysql port number
    
    public static String MYSQL_USER = "root",      //Default Mysql User
                        MYSQL_PASSWORD = " ",     //Default Mysql User password
                        MYSQL_SERVER = "localhost",
                        HOST = "jdbc:mysql://localhost:3306/",      //Default Mysql Host address
                        DB_NAME = "CouponDunia",        //Default Database name
                        DB_URL = HOST + DB_NAME,        //Default Database connection url with database param
                        JDBC_DRIVER = "com.mysql.jdbc.Driver",      //JDBC driver name
                        TABLE_NAME = "EmailQueue";      //Default Table name
    
    private static Connection connection = null;
    private int DBCreated = 0, TableCreated = 0;
    
    public SqlEmail()
    {
        
        if(SqlEmail.connection == null)
        {
            try
            {
                Class.forName(JDBC_DRIVER);
                //connecting with HOST as we are not sure about CouponDunia DB.
                SqlEmail.connection = DriverManager.getConnection(HOST,MYSQL_USER,MYSQL_PASSWORD);
                
                Statement s1 = connection.createStatement(); //Creates a Statement object for sending SQL statements to the database. 
                
                s1.executeUpdate("CREATE DATABASE IF NOT EXISTS "+ DB_NAME);
                DBCreated = 1;
            }
            catch(ClassNotFoundException e)
            {
                System.out.println("Error: Class "+JDBC_DRIVER+" not found");
            }
            catch(SQLException e)
            {
                System.out.println("Error: "+ e.getMessage());
            }
            finally
            {
                if(DBCreated == 1)//Database created so we connect using DB_URL instead of HOST
                {
                    try
                    {
                        SqlEmail.connection.close();
                        SqlEmail.connection = DriverManager.getConnection(DB_URL,MYSQL_USER,MYSQL_PASSWORD);
                        
                        if(TableCreated == 0)
                        {
                            Statement s1 = connection.createStatement();
                            s1.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("
                            + "  `id` int(11) NOT NULL AUTO_INCREMENT,"
                            + "  `from_email_address` varchar(100) NOT NULL,"
                            + "  `to_email_address` varchar(100) NOT NULL,"
                            + "  `subject` varchar(200) NOT NULL,"
                            + "  `body` varchar(1000) NOT NULL,"
                            + "  `sent_bit` int(11) NOT NULL DEFAULT '0',"
                            + "  PRIMARY KEY (`id`)) ENGINE=InnoDB  DEFAULT CHARSET=latin1;");
                            
                            TableCreated = 1;
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            
        } 
    }//end of SqlEmail Constructor
    
    //adds an email record to Table : TABLE_NAME 
    public int InsertMail(String from, String to, String subject, String body)
    {
        if(SqlEmail.connection == null)
            return -1;
        else
        {
            Statement s2 = null;
            try
            {
                s2 = SqlEmail.connection.createStatement();
                    
                String query = "INSERT INTO "+ SqlEmail.TABLE_NAME
                        
                        + "(from_email_address, to_email_address, subject, body)"
                        + "VALUES ('"+ from +"', '"+to+"', '"+subject+"', '"+body +"')";
                    
                int no_of_rows_affected = s2.executeUpdate(query);
                return no_of_rows_affected;                  
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return -1;
            }
            finally
            {
                try
                {
                    s2.close();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    return -1;
                }
            } 
        } 
    }//end of InsertMail
    
    //fetch all the emails
    public Vector<String[]> FetchEmail(String from, String to) throws Exception 
    {
        if(SqlEmail.connection == null) 
            return null;
        else 
        {
            Vector<String[]> result = new Vector<String[]>();
            
            Statement s3 = null;
            ResultSet rs1 = null;
            try 
            {
                s3 = SqlEmail.connection.createStatement();
            
                String query = "SELECT id, from_email_address, to_email_address, subject, body FROM " 
                        + SqlEmail.TABLE_NAME + " WHERE from_email_address = '" 
                        + from + "' AND to_email_address = '" 
                        + to + "' AND sent_bit = 0 ORDER BY id";
                
                rs1 = s3.executeQuery(query);
                while(rs1.next()) 
                {
                    String[] row = new String[5];

                    row[0] = ""+rs1.getInt("id");
                    row[1] = rs1.getString("from_email_address");
                    row[2] = rs1.getString("to_email_address");
                    row[3] = rs1.getString("subject");
                    row[4] = rs1.getString("body");

                    result.add(row);
                }
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
                throw e;
            } 
            finally 
            {
                rs1.close();
                s3.close();
            }
            return result;
        }
    }//end of FetchEmail
    
    public Vector<String[]> FetchDistinctEmail() throws Exception
    {
        if(SqlEmail.connection == null) 
            return null;
        else 
        {
            Vector<String[]> result = new Vector<String[]>();
        
            SqlEmail email = new SqlEmail();
            ResultSet rs2 = null;
            Statement s4 = null;
            try 
            {
                s4 = SqlEmail.connection.createStatement();
            
                String query = "SELECT DISTINCT from_email_address, to_email_address FROM "+TABLE_NAME+";";
                rs2 = s4.executeQuery(query);
                
                while(rs2.next()) 
                {
                    String[] row = {
                        rs2.getString("from_email_address"),
                        rs2.getString("to_email_address")
                    };

                    result.add(row);
                }
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
                throw e;
            }
            finally 
            {
                rs2.close();
                s4.close();
            }
            return result;
        }
    }//end of FetchDistinctEmail
    
    //Sets the sent_bit to 1 of all emails that are sent
    public int MarkSentEmail(Vector<Integer> id) throws SQLException 
    {
        if(SqlEmail.connection == null) 
            return -1;
        else 
        {
            int no_of_rows_updated = 0;   //Total number of rows updated.
            Statement s5 = null;
            try 
            {
                s5 = SqlEmail.connection.createStatement();
                if(id.size() > 1) 
                {
                    System.out.println("---------------" + id.size() + " records are being updated----------------");
                    String idArray = "(";
                    for(int i=0; i<id.size()-1; i++) 
                    {
                        idArray += id.get(i);
                        idArray += ",";
                    }
                    idArray += id.get(id.size()-1);
                    idArray += ")";
                    String query = "UPDATE "+TABLE_NAME+" SET sent_bit = 1 WHERE id IN " + idArray;
                    no_of_rows_updated = s5.executeUpdate(query);
                }
            } 
            catch (SQLException e) 
            {
                e.printStackTrace();
                throw e;
            } 
            finally 
            {
                s5.close();
            }
            return no_of_rows_updated;
        }
    }//end of MarkSentEmail
    
} //end of class SqlEmail

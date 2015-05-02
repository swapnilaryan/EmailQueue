package Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.exit;
import java.util.Scanner;
import java.util.Vector;
/**
 *
 * @author swapnilaryan
 */
public class MainMail 
{
    public static int No_of_Threads = 2;
    public static Vector<String[]> mailPairs = null;        //stores the distinct <from, to> mail pairs from the database
    private MainMail() {} //prevent the object creation of this class
    
    public static void main(String args[]) throws IOException
    {
        int insertDM = InputFromUser(); // for reconfiguration of settings
        
        if(insertDM == 1)
        {
            InsDM(); //insert Dummy mails
        }
        
        mailPairs = DistinctMailPairs();     //gets the distinct (from_email_address, to_email_address) pairs from DB
        
        if(mailPairs != null ) {
            //Creating threads to handle fast email sending
            MultipleSend ms;
            for(int i=1; i <= No_of_Threads; i++) {
                ms = new MultipleSend(i);
                ms.start();
            }
        }
    }//end of main
    
    private static int InputFromUser()throws IOException
    {
        InputStreamReader isr = new InputStreamReader(System.in) ;
        BufferedReader  br = new BufferedReader(isr) ;
        
        System.out.println("The default configuration is: ");
        System.out.println("Number of Threads: "+MainMail.No_of_Threads);
        
        System.out.println("Mysql User: " + SqlEmail.MYSQL_USER);
        System.out.println("Mysql "+SqlEmail.MYSQL_USER+" password: "+ SqlEmail.MYSQL_PASSWORD);
        System.out.println("Mysql Server Address: " + SqlEmail.MYSQL_SERVER);
        System.out.println("Mysql Database Name: " + SqlEmail.DB_NAME);
        System.out.println("Mysql Table Name: "+ SqlEmail.TABLE_NAME);
        
        System.out.println("SMTP Server Address: "+ SMTPSend .SMTP_HOST);
        System.out.println("SMTP User SetFieldsEmail: "+ SMTPSend .SMTP_USER);
        System.out.println("SMTP User Password: "+ SMTPSend .SMTP_PASSWORD);
        
        System.out.println("\n");
        //System.out.println("To exit enter 'quit', whenever asked for Y/N option");
        String choice;
        
        while(true) //while(1) 
        {
            System.out.println("Enter Y/y for reconfiguration");
            System.out.println("Enter N/n for not making any changes");
            System.out.println("Enter 'quit' to exit");
            System.out.print("Your Choice: ");
            choice = br.readLine();
            if("quit".equals(choice)) 
            {
                System.exit(0);
            } 
            else if("Y".equals(choice) || "y".equals(choice)) 
            {
                System.out.print("Enter no. of Threads You want to create: ");
                MainMail.No_of_Threads = Integer.parseInt(br.readLine());

                System.out.print("Enter MySQL User Name: ");
                SqlEmail.MYSQL_USER = br.readLine();
                System.out.print("Enter MySQL User Password: ");
                SqlEmail.MYSQL_PASSWORD = br.readLine();
                System.out.print("Enter MySQL Server Address: ");
                SqlEmail.MYSQL_SERVER = br.readLine();
                System.out.print("Enter MySQL Database Name: ");
                SqlEmail.DB_NAME = br.readLine();
                System.out.print("Enter Table Name: ");
                SqlEmail.TABLE_NAME = br.readLine();
                
                System.out.print("Enter the SMTP Server Address: ");
                SMTPSend.SMTP_HOST = br.readLine();
                System.out.print("Enter the SMTP User : ");
                SMTPSend.SMTP_USER = br.readLine();
                System.out.print("Enter the SMTP User Password: ");
                SMTPSend.SMTP_PASSWORD = br.readLine();
                
                SqlEmail.HOST = "jdbc:mysql://"+SqlEmail.MYSQL_SERVER+":"+SqlEmail.MYSQL_PORT+"/";
                SqlEmail.DB_URL = SqlEmail.HOST + SqlEmail.DB_NAME;
                
                break;
            }
            
            else if("N".equals(choice) || "n".equals(choice))
            {
                break;
            }
            
            else
            {
                System.out.println("\nWrong Choice ..! Please Try Again");
            }      
        }
        
        System.out.println("By default, no data would be inserted into the table");
        System.out.print("Would you like to insert dummy data (Y/N/'quit'): ");
        choice = br.readLine();
        
        if("quit".equals(choice)) 
        {
            System.exit(0);
            return 0;
        } 
        else if("Y".equals(choice) || "y".equals(choice)) 
        {
            return 1;
        } 
        else 
        {
            return 0;
        }
    }//end of InputFromUser
    
    //function to insert dummy mails 
    //it inserts dummy mails into the table mentioned. There is no need to write custom sql syntax. 
    public static void  InsDM() 
    {
        SetFieldsEmail NewRow;
        for(int i=0; i<10; i++) 
        {
            for(int j=0; j<5; j++) 
            {
                NewRow = new SetFieldsEmail("fromswap"+i+"@gmail.com", "toaryan"+j+"@gmail.com", "This is subject "+(i*5+j), "This is body");
                NewRow.save();
            }
        }
    }//end of InsDM
    
    //gets the distinct <from, to> mail pairs from the database
    public static Vector<String[]> DistinctMailPairs() 
    {
        SqlEmail se = new SqlEmail();
        Vector<String[]> distinctRows = null;
        try 
        {
            distinctRows = se.FetchDistinctEmail();     //uses the FetchDistinctEmail() utility function from SqlEmail class
            return distinctRows;
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            System.out.println("Could not fetch email pairs: " + e.getMessage());
            return null;
        }
    }//end of DistinctMailPairs
}
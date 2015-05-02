package Project;

import java.sql.SQLException;
import java.util.Vector;
import javax.mail.MessagingException;

/**
 *
 * @author swapnilaryan
 */
public class MultipleSend extends Thread 
{
    private Thread thrd = null;
    public String ThreadName;      //the name of the thread
    private int ThreadIndex;        //thread number, or can be termed as offset from which pair of emails this thread would start fetching
    
    public Vector<Integer> sentId;

    public MultipleSend(int num)
    {
        this.ThreadIndex = num;
        this.ThreadName = "Thread_"+num;
        sentId = new Vector<Integer>();
        
        System.out.println("Creating " +  ThreadName );
    }
    
    @Override
    public void run() {
        System.out.println("Running " +  ThreadName );
        try 
        {
            //Each threads fetches emails of the email pairs occuring at a distance of NUM_THREADS to prevent 
            //multiple sending of emails as well as preventing any email not being sent
            
            for(int i=this.ThreadIndex-1 ; i<MainMail.mailPairs.size(); i = i + MainMail.No_of_Threads) 
            {    
                String[] str = MainMail.mailPairs.get(i);
                SetFieldsEmail[] emails = this.GetEmails(str[0], str[1]);   //gets all the emails for the email pair 
                if(emails.length > 0) {
                    String from = emails[0].from;
                    String to = emails[0].to;

                    if(this.SendEmails(from, to, emails) == 1) 
                    {          //passes params to send all the 'emails' for the given to and from 
                        System.out.println(
                                this.ThreadName + ": " 
                                + emails.length + " emails from "
                                + from + " email to " 
                                + to + " email is sent");
                        
                    } 
                    else 
                    {
                        System.out.println(
                                this.ThreadName + ": Could not send "
                                + emails.length + "the emails from "
                                + from + " email to " 
                                + to + " email");
                    }
                }
            }
            try 
            {
                new SqlEmail().MarkSentEmail(this.sentId);     //after each threads finishes sending mails, they update the sent_bit = 1
            } 
            catch (SQLException e) 
            {
                System.out.println(this.ThreadName + ": Could not update sent_bit of email hvaing ID");
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            System.out.println("Error: " +  ThreadName + " - " + e.getMessage());
        }
        System.out.println("Thread " +  ThreadName + " exiting.");
    }

    @Override
    public void start ()           //starts the thread's execution
    {
        System.out.println("Starting " +  ThreadName );
        if (thrd == null) {
            thrd = new Thread(this, ThreadName);
            thrd.start ();
        }
    }
    
    public SetFieldsEmail[] GetEmails(String from, String to)
    {
        Vector<String[]> mailRows;
        SqlEmail se = new SqlEmail();
        try {
            mailRows = se.FetchEmail(from, to);   //gets all the emails for the given from, to email pairs using utility functions of SqlEmailUtil class
            
            SetFieldsEmail[] mails = new SetFieldsEmail[mailRows.size()];   //array of EmailQueue objects for storing all the details of records returned fetched from database
            
            for(int i=0; i<mailRows.size(); i++) 
            {
                String[] str = mailRows.get(i);

                mails[i] = new SetFieldsEmail();
                mails[i].id = Integer.parseInt(str[0]);
                mails[i].from = str[1];
                mails[i].to = str[2];
                mails[i].subject = str[3];
                mails[i].body = str[4];
            }
            return mails;
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            System.out.println("Could not get emails: " + e.getMessage());
            return null;
        }
    }//end of GetEmails
    
    public int SendEmails(String from, String to, SetFieldsEmail[] mails) 
    {
        SMTPSend smtps = new SMTPSend();
        
        try 
        {
            smtps.SendEmail(from, to, mails, this);       //appends ThreadSend object so that the thread could be identified from that point
            return 1;
        } 
        catch (MessagingException ex) 
        {
            ex.printStackTrace();
            System.out.println("Failed to send emails " + ex.getMessage());
            return 0;
        }
    }// end of SendEmails
}//End of MultipleSend

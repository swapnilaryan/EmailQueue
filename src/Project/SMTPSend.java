/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Project;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
/**
 *
 * @author swapnilaryan
 */
public class SMTPSend 
{
    public static String SMTP_HOST = "localhost",      //The SMTP host address
                         SMTP_USER = "root@localhost", //The SMPTP User name/email
                         SMTP_PASSWORD = " ";        //SMTP password     
    private static Properties prop = null;             //The properties for the session
    private static Session sObj = null;
    
    public SMTPSend()
    {
        if(SMTPSend.prop == null) //Set values to the prop object if it is null 
        {         
            
            SMTPSend.prop = new Properties();
            SMTPSend.prop.put("mail.smtp.auth", "true");
            SMTPSend.prop.put("mail.smtp.starttls.enable", "true");
            SMTPSend.prop.put("mail.smtp.host", SMTPSend.SMTP_HOST);
            SMTPSend.prop.put("mail.smtp.port", "25");
        }
        if(SMTPSend.sObj == null)        //Creates a new session object only if it is not been created earlier
        {     
            sObj = Session.getInstance(prop, new javax.mail.Authenticator() 
            {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() 
                {
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
                }
            });
        }
    }//end of SMTPSend() constructor
    
    //Sends the mail, the thread responsible for sending this mail is represented by "th" object
    public boolean SendEmail(String from, String to, SetFieldsEmail[] emails, MultipleSend thrd ) throws MessagingException 
    {
        try
        {
            Message msg = new MimeMessage(sObj);
            
            //Since all the messages have same from and to fields, it is set only once
            
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            
            //all the mails are then sent one by one which has the same from and to email pairs
            for(int i=0; i<emails.length; i++) {
                msg.setSubject(emails[i].subject);
                msg.setText(emails[i].body);
                
                Transport.send(msg);
                
                thrd.sentId.addElement(emails[i].id);
            }
            return true;
        } 
        catch (MessagingException e) 
        {
            throw e;
        }
    }//end of SendEmail
}//end of SMTPSend class

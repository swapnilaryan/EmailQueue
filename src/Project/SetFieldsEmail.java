package Project;

/**
 *
 * @author swapnilaryan
 */
public class SetFieldsEmail 
{
    public String from, to, subject, body;
    public int id;
    
    public SetFieldsEmail()
    {
        
    }
    
    public SetFieldsEmail(String from, String to, String sub, String body)
    {
        this.from = from;
        this.to = to;
        this.subject = sub;
        this.body = body;
    }
    
    //adds new row to database
    public int save()
    {
        SqlEmail se = new SqlEmail();
        int no_of_rows_affected = se.InsertMail(this.from,this.to,this.subject,this.body);
        
        if(no_of_rows_affected == -1)
            return 0;
        else
            return 1;
    }
}

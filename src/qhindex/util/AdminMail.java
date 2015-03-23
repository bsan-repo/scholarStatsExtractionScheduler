/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.util;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Boris Sanchez
 */
public class AdminMail {
    private static String USER_NAME = "bor.helper.acc";  // GMail user name (just the part before "@gmail.com")
    private static String PASSWORD = "1234casa"; // GMail password
    private static String RECIPIENT = "bor.helper.acc@gmail.com";
    
    public void sendMailToAdmin(String title, String msg){
        Properties props = System.getProperties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.user", USER_NAME);
        props.put("mail.smtp.password", PASSWORD);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(USER_NAME));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(RECIPIENT));

            message.setSubject(title);
            message.setText(msg);
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", USER_NAME, PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            Debug.print("Exception while sending admin e-mail: "+ae.toString());
        }
        catch (MessagingException me) {
            Debug.print("Exception while sending admin e-mail: "+me.toString());
        }
    }
}

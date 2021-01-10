package com2u.de.LifeMonitoring;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaMail {

	Properties emailProperties;
	Session mailSession;
	MimeMessage emailMessage;
	String emailHost = "smtp.gmail.com";
	String fromUser = "vision2u.de@gmail.com";
	String fromUserEmailPassword = "PPH2goou";
	String emailPort = "587";


	public static void main(String args[]) throws AddressException,
			MessagingException {

		JavaMail javaEmail = new JavaMail();

		String emailHost = "smtp.gmail.com";
		String fromUser = "vision2u.de@gmail.com";//just the id alone without @gmail.com
		String fromUserEmailPassword = "PPH2goou";
		String emailPort = "587";//gmail's smtp port

		javaEmail.setMailServerProperties(emailHost, fromUser, fromUserEmailPassword, emailPort);
		String[] toEmails = { "Patrick.Hess@com2u.de" };
		String emailSubject = "Java Email";
		String emailBody = "This is an email sent by JavaMail api.";
		javaEmail.createEmailMessage(toEmails, emailSubject, emailBody);
		javaEmail.sendEmail();
	}

	public void setMailServerProperties(String _emailHost, String _fromUser, String _fromUserEmailPassword, String _emailPort) {

		emailHost = _emailHost;
		fromUser = _fromUser;//just the id alone without @gmail.com
		fromUserEmailPassword = _fromUserEmailPassword;
		emailPort = _emailPort;//gmail's smtp port

		emailProperties = System.getProperties();
		emailProperties.put("mail.smtp.port", emailPort);
		emailProperties.put("mail.smtp.auth", "true");
		emailProperties.put("mail.smtp.starttls.enable", "true");

	}

	public void createEmailMessage(String[] toEmails, String emailSubject, String emailBody) throws AddressException,
			MessagingException {
		mailSession = Session.getDefaultInstance(emailProperties, null);
		emailMessage = new MimeMessage(mailSession);

		for (int i = 0; i < toEmails.length; i++) {
			emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmails[i]));
		}

		emailMessage.setSubject(emailSubject);
		emailMessage.setContent(emailBody, "text/html");//for a html email
		//emailMessage.setText(emailBody);// for a text email

	}

	public void sendEmail() throws AddressException, MessagingException {
		Transport transport = mailSession.getTransport("smtp");
		transport.connect(emailHost, fromUser, fromUserEmailPassword);
		transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
		transport.close();
		System.out.println("Email sent successfully.");
	}

}

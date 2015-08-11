package utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import models.User;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import controllers.Users;

import play.Logger;
import play.Play;
import play.libs.WS;

public class MailSender {

	/**
	 * send comment to data sender email
	 * */
	public static void sendCommentByGmail(User user)
			throws MalformedURLException, EmailException {
		String fromAddr = Play.configuration.getProperty("mail.fromAddr",
				"your@mail.com");
		String fromName = Play.configuration.getProperty("mail.fromName",
				"Online Examination System");
		String toAddr = user.email;
		String subject = "Password Reset";
		String body = "Dear Concern,"
				+ "\n\n"
				+ "To change password click bellow link : "
				+ "http://"
				+ Play.configuration.getProperty("application.baseUrl",
						"127.0.0.1") + ":"
				+ Play.configuration.getProperty("http.port", "80")
				+ "/Users/resetPasswordForm" + "?" + "email=" + user.email + "&"
				+ "passwordResetId=" + WS.encode(user.passwordResetId)

		;
		// replace newline to html line break
		body = body.replaceAll("\n", "<br/>");

		sendMail(fromAddr, fromName, toAddr, subject, body);

	}

	/**
	 * send mail
	 * */
	public static void sendMail(String fromAddr, String fromName,
			String toAddr, String subject, String body) throws EmailException,
			MalformedURLException {
		HtmlEmail email = new HtmlEmail();
		email.addTo(toAddr);
		email.setFrom(fromAddr, fromName);
		email.setSubject(subject);
		// embed the image and get the content id
		/*
		 * URL url = new
		 * URL("http://www.playframework.com/assets/images/favicon.png"); String
		 * cid = email.embed(url, "Zenexity logo");
		 */
		// set the html message
		// email.setHtmlMsg("<html>"+ body
		// +"Zenexity logo - <img src=\"cid:"+cid+"\"></html>");
		email.setHtmlMsg("<html>" + body + "</html>");
		// set the alternative message
		email.setTextMsg(body);

		/*
		 * //email conf #smtp conf for mPower mail send
		 * mail.smtp.host=smtp.gmail.com
		 * mail.smtp.user=agriculture.ext@gmail.com mail.smtp.pass=easypassword
		 * mail.smtp.channel=ssl
		 */
		email.setHostName(Play.configuration.getProperty("mail.smtp.host"));
		email.setAuthentication(
				Play.configuration.getProperty("mail.smtp.user"),
				Play.configuration.getProperty("mail.smtp.pass"));
		email.setSSL(true);
		email.setCharset("UTF-8");

		email.send();
	}

}
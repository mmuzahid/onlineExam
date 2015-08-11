package utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import models.TblUser;

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
	public static void sendCommentByGmail(TblUser user)
			throws MalformedURLException, EmailException {
		String fromAddr = Play.configuration.getProperty("mail.fromAddr",
				"your@mail.com");
		String fromName = Play.configuration.getProperty("mail.fromName",
				"Exam Hub");
		String toAddr = user.email;
		String subject = "Password Reset";
		String body = "Dear Concern,"
				+ "\n\n"
				+ "To change password click bellow link : "
				+ "http://"
				+ Play.configuration.getProperty("application.baseUrl",
						"http://examhub.herokuapp.com") + ":"
				+ Play.configuration.getProperty("default.http.port", "80")
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
		email.setHtmlMsg("<html>" + body + "</html>");
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
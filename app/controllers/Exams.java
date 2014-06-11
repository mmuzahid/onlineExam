package controllers;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.sun.org.apache.xerces.internal.impl.validation.ValidationState;

import models.AnswerOption;
import models.AnswerPaper;
import models.AnswerPaperStatus;
import models.Comment;
import models.Exam;
import models.ExamQuestionSetType;
import models.ExamStatus;
import models.ExamVisibility;
import models.Question;
import models.Role;
import models.User;
import models.UserAnswer;
import play.Logger;
import play.data.validation.Valid;
import play.libs.Crypto;
import play.libs.Crypto.HashType;
import play.mvc.With;
import play.mvc.results.RenderText;
import utils.ExtraUtils;
import utils.MailSender;
import utils.ReportData;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.ExternalRestrictions;
import controllers.deadbolt.Unrestricted;

/**
 * Exams Controller -	Exams controller of the web application
 */
@With(Deadbolt.class)
public class Exams extends Controller{

	@ExternalRestrictions("Edit Exam")
	public static void createExam() {
		List<User> authorList = new ArrayList<User>();
		User currentUser = User.findByLogin(Security.connected());
		
		if (Role.isAdmin(currentUser.role)){
			authorList = User.findAll();			
		}
		else {
			authorList.add(currentUser);
		}
		
		render(authorList);
	}
	
	@ExternalRestrictions("Edit Exam")	
	public static void editExam(long id) {
		Exam exam = Exam.findById(id);
		notFoundIfNull(exam);
		List<User> authorList = new ArrayList<User>();
		User currentUser = User.findByLogin(Security.connected());
		
		if (Role.isAdmin(currentUser.role)){
			authorList = User.findAll();			
		}
		else {
			if (exam.author != currentUser) {
				error(401,"Unauthorized access");
			}
			authorList.add(currentUser);
		}
		
		render("@createExam", exam, authorList);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void submitExam(Exam exam) {

		if (exam.examPeriod == null || exam.examPeriod.isEmpty()){
		validation.required(null).key("exam.examPeriod").message("Start Time must be less than End Time");
		}
		exam.startTime = ExtraUtils.toDate(exam.examPeriod.split("-")[0]);
		exam.endTime = ExtraUtils.toDate(exam.examPeriod.split("-")[1]);
		
		validation.valid(exam);

		if (exam.startTime.after(exam.endTime)) {
			validation.required(null).key("exam.examPeriod").message("Start Time must be less than End Time");
			validation.valid(exam);
		}
		
		if (validation.hasErrors()) {
			List<User> authorList = new ArrayList<User>();
			User currentUser = User.findByLogin(Security.connected());
			if (Role.isAdmin(currentUser.role)){
				authorList = User.findAll();			
			}
			else {
				authorList.add(currentUser);
			}
			render("@createExam", exam, authorList);
		}
		
		if (exam.questionSetType == ExamQuestionSetType.RANDOM  && exam.questionList.size() < exam.questionPerExam) {
			int qSetSize=exam.questionList.size();
			validation.min(qSetSize, exam.questionPerExam).key("exam.questionPerExam").message("Question Per Exam must be less than or equal "+qSetSize);
			List<User> authorList = new ArrayList<User>();
			User currentUser = User.findByLogin(Security.connected());
			
			if (Role.isAdmin(currentUser.role)){
				authorList = User.findAll();			
			}
			else {
				authorList.add(currentUser);
			}
			render("@createExam", exam, authorList);
		}
		
		if (exam.questionSetType == ExamQuestionSetType.FIXED) {
			exam.questionPerExam = exam.questionList==null ? 0 : exam.questionList.size();
		}
		
		try {
			boolean isNew = exam.id == null;
			
			exam.save();
			if (isNew) {
				questionsExam(exam.id);
			}
			else {
				listExam();
			}
			
		}
		catch(PersistenceException pe){
			List<User> authorList = new ArrayList<User>();
			User currentUser = User.findByLogin(Security.connected());
			
			if (Role.isAdmin(currentUser.role)){
				authorList = User.findAll();			
			}
			else {
				authorList.add(currentUser);
			}String tmp = exam.examCode;
			exam.examCode = null;
			validation.required(exam.examCode).key("exam.examCode").message("Exam Code must be unique");
			exam.examCode = tmp;
			render("@createExam", exam, authorList);
		}
		error();
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void listExam() {
		List<Exam> examList = new ArrayList<Exam>();
		
		User currentUser = User.findByLogin(Security.connected());
		if (!Role.isAdmin(currentUser.role)){
			examList = Exam.findByAuthor(currentUser);
		}
		else {
			examList = Exam.findAll();
		}
		render(examList);
	}
	/** Question actions **/
	
	@ExternalRestrictions("Edit Exam")
	public static void questionsExam(long id) {
		
		Exam exam = Exam.findById(id);
		List<String> questionTypes = Question.allQusetionType();
		render(exam, questionTypes);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void deleteExam(long id) {
		Exam exam = Exam.findById(id);
		notFoundIfNull(exam);
		
		if (AnswerPaper.count("exam = ?", exam) > 0) {
			error("Dependencies Exists, Used by another Entity.");
		}
		
		exam.delete();
		ok();
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void addQuestion(long id) {
		
		Exam exam = Exam.findById(id);
		List<String> questionTypes = Question.allQusetionType();
		render("@editQuestion", exam, questionTypes);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void editQuestion(long id){
		
		Question question = Question.findById(id);
		notFoundIfNull(question);

		Exam exam = question.exam;
		List<String> questionTypes = new ArrayList<String>();
		questionTypes.add(question.quesType);
		
		render(exam, question, questionTypes);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void saveQuestion(@Valid Question question) {
		
		if (validation.hasErrors()) {
			Exam exam = question.exam;
			List<String> questionTypes = new ArrayList<String>();
			if (question.id != null) {
				questionTypes.add(question.quesType);
			}
			else {
				questionTypes = Question.allQusetionType();
			}
			
			render("@editQuestion", question, exam, questionTypes);
		}
		
		question.save();
		flash.success("Question saved successfully.");
		
		editQuestion(question.id);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void editAnswerOption(long id){
		
		AnswerOption answerOption = AnswerOption.findById(id);
		notFoundIfNull(answerOption);
		List<String> questionTypes = Question.allQusetionType();
		Question question = answerOption.question;
		Exam exam = question.exam;
		
		render("@editQuestion", exam, question, questionTypes, answerOption);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void saveAnswerOption(AnswerOption answerOption) {
		
		/**set check-box answer as option is a answer or not */
		String isAnswer = request.params.get("answerOption.isAnswer");
		if (isAnswer == null) {
			answerOption.isAnswer = false;
		}
		else {
			answerOption.isAnswer = true;
		}
		
		/**for radio button - ensure single answer*/
		if (answerOption.question.quesType.equals("radio") && answerOption.isAnswer) {
			AnswerOption correctAnswer = AnswerOption.find("question = ? and isAnswer = ?", answerOption.question, true).first();
			if (correctAnswer != null && answerOption.id != correctAnswer.id){
				flash.error("Multiple answer not possible for 'radio' question");
				editQuestion(answerOption.question.id);
			}
		}
		
		//try to save
		try{
			answerOption.save();
		}
		catch(PersistenceException pe){
			flash.error("Option must be unique");
		}
		
		flash.success("Option saved successfully.");
		editQuestion(answerOption.question.id);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void deleteAnswerOption(long id) {
		AnswerOption answerOption = AnswerOption.findById(id);
		notFoundIfNull(answerOption);
		answerOption.delete();
		ok();
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void deleteQuestion(long id) {
		
		Question question = Question.findById(id);
		notFoundIfNull(question);
		if (question.exam.questionSetType == ExamQuestionSetType.RANDOM && question.exam.questionPerExam >= question.exam.questionList.size()) {
			error("Too Few Question Set for Random Exam");
		}
		
		if (UserAnswer.count("question = ?", question) > 0) {
			error("Dependencies Exists, Used by another Entity.");
		}
		
		question.delete();
		
		ok();
		
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void previewExam(long id){
	
		Exam exam = Exam.findById(id);
		
		List<Question> questionList = exam.questionList;
		
		render(exam, questionList);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void startExam(long id){
	
		AnswerPaper answerPaper = AnswerPaper.findById(id);
		notFoundIfNull(answerPaper);
		Exam exam = answerPaper.exam;
		List<Question> questionList = new ArrayList<Question>();
		User participate =  User.findByLogin(Security.connected());

		if (participate != answerPaper.participate) {
			error(401,"Unauthorized Access");
		}
		
		if (answerPaper.exam.status != ExamStatus.ACTIVE) {
			//renderText("Currently Exam status: " + answerPaper.exam.status);
		}
		else{
			questionList = answerPaper.questionList;
		}
		render(exam, questionList, answerPaper);
	}

	/**Answer paper submission*/
	@ExternalRestrictions("Edit Exam")
	public static void submitAnswerPaper() {
		String answerPaperId = request.params.get("answerPaperId");//submitted answer paper db id
		notFoundIfNull(answerPaperId);
		AnswerPaper answerPaper = AnswerPaper.findById(Long.parseLong(answerPaperId));
		
		if (answerPaper.status != AnswerPaperStatus.ACTIVE) {//check submitted answer paper status
			error("status : " + answerPaper.status);
		}
		if (answerPaper.exam.status != ExamStatus.ACTIVE) {
			error("exam status : " + answerPaper.exam.status);
		}
		if (!answerPaper.exam.isValidPeriod()) {
			error("Exam Period out of range");
		}
		
		answerPaper.done();
		
		notFoundIfNull(answerPaper);
		Exam exam = answerPaper.exam;
		notFoundIfNull(exam);		
	
		int score = 0;//score of exam
		for (Question question : answerPaper.questionList) {//for each question of answer paper
			String userAnswerValue = "";
			String correctAnswerValue = "";
			List<String> answerValueList = new ArrayList<String>();//selected option key - currently same as 'selectedOptionIdList'
			List<Long> selectedOptionIdList = new ArrayList<Long>();//selected answer option by participate
			
			String[] paramOptIds = request.params.getAll(""+question.id);//all submitted options for a question 
			if (paramOptIds != null) {
				answerValueList = Arrays.asList(paramOptIds);
			}	
			for(String optId : answerValueList) {//set selectedOptions for a question
				selectedOptionIdList.add(Long.parseLong(optId));
			}
			if (selectedOptionIdList.size() > 0) {//set user answer string
				List<String> userAnswerLabelValueList = AnswerOption.find
						("SELECT optionLabel FROM AnswerOption Where question = ? and id in (:ids)"
								, question)
								.setParameter("ids", selectedOptionIdList).fetch();
				//Collections.sort(userAnswerLabelValueList);
				
				userAnswerValue = StringUtils.join(userAnswerLabelValueList, "\n");
			}

			List<String> correctAnswerOptionList = AnswerOption.find
					("SELECT optionLabel FROM AnswerOption Where question = ? and isAnswer = ?"
							, question, true).fetch();
			if (correctAnswerOptionList.size() > 0) {//set correct answer string
				//Collections.sort(correctAnswerOptionList);
				correctAnswerValue = StringUtils.join(correctAnswerOptionList, "\n"); 
			}
			
			List<String> allAnswerOptionList = AnswerOption.find
					("SELECT optionLabel FROM AnswerOption Where question = ?"
							, question).fetch();	
			String allOptions = StringUtils.join(allAnswerOptionList, "\n");//all options of the question
			
			/**set user answer object*/
			UserAnswer userAnswer = new UserAnswer(answerPaper, question, userAnswerValue);
			userAnswer.questionTitle = question.quesTitle;
			userAnswer.correctAnswer = correctAnswerValue;
			userAnswer.options = allOptions;
			userAnswer.isCorrect = ((userAnswerValue == null && correctAnswerValue == null ) 
									|| (userAnswerValue != null && userAnswerValue.equals(correctAnswerValue)));
			userAnswer.save();
			
			if (userAnswer.isCorrect) {
				score++;
			}
			
		}//end of quesion loop
		
		answerPaper.numberOfCorrectAnswer = score;
		answerPaper.save();
		
		answerPaper.exam.totalParticipates++;
		answerPaper.exam.save();
		
		viewAnswerPaper(answerPaper.id);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void listAnswerPaper() {
		
		List<AnswerPaper> answerPaperList = new ArrayList<AnswerPaper>();
		
		User currentUser = User.findByLogin(Security.connected());
		if (Role.isAdmin(currentUser.role)){
			answerPaperList = AnswerPaper.find("ORDER BY id DESC").fetch();		
		}
		else {
			answerPaperList = AnswerPaper.find("participate = ? ORDER BY id DESC", currentUser).fetch();	
		}
		
		render(answerPaperList);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void viewAnswerPaper(long id) {
		
		AnswerPaper answerPaper = AnswerPaper.findById(id);
		notFoundIfNull(answerPaper);
		
		User currentUser = User.findByLogin(Security.connected());
		if (!Role.isAdmin(currentUser.role)){
			if (currentUser != answerPaper.participate)	{
				error(401,"Unauthorized Access");
			}
		}
		
		
		List<UserAnswer> userAnswerList = UserAnswer.find("answerPaper = ?", answerPaper).fetch();
				
		render(answerPaper, userAnswerList);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void viewParticipateAnswer(long id) {
		
		AnswerPaper answerPaper = AnswerPaper.findById(id);
		notFoundIfNull(answerPaper);
		
		User currentUser = User.findByLogin(Security.connected());
		if (!Role.isAdmin(currentUser.role)){
			if (currentUser != answerPaper.exam.author)	{
				error(401,"Unauthorized Access");
			}
		}
		
		
		List<UserAnswer> userAnswerList = UserAnswer.find("answerPaper = ?", answerPaper).fetch();
				
		render(answerPaper, userAnswerList);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void examParticipates(long id){

		Exam exam = Exam.findById(id);
		notFoundIfNull(exam);
		
		User currentUser = User.findByLogin(Security.connected());
		if (!Role.isAdmin(currentUser.role)){
			if (currentUser != exam.author)	{
				error(401,"Unauthorized Access");
			}
		}
		
		List<AnswerPaper> answerPaperList = AnswerPaper.find("exam = ?", exam).fetch();

		render(exam, answerPaperList);

	}


	//exam search
	public static void searchExam(String searchKey) {
		List<Exam> searchedExamList = Exam.find(
				" author.name LIKE (:authorName)" +
				" OR examName LIKE (:searchKey)" +
				" OR examCode LIKE (:searchKey)")
				.setParameter("authorName", "%" + searchKey + "%")
				.setParameter("searchKey", "%" + searchKey + "%")
				.fetch();
		render(searchedExamList, searchKey);
	}
	
	// request and start public exam
	public static void requestForExam(long id) {

		Exam exam = Exam.findById(id);
		notFoundIfNull(exam);

		if (!exam.isValidPeriod()) {
			error("Exam Period out of range");

		}

		User participate = User.findByLogin(Security.connected());

		if (exam.visibility == ExamVisibility.PUBLIC) {
			AnswerPaper answerPaper = new AnswerPaper(exam, participate).save();
			startExam(answerPaper.id);
		} else {
			renderText("This is a Private Exam");
		}
	}

	@ExternalRestrictions("Edit Exam")
	public static void questionComment(long id){
		Question question = Question.findById(id);
		notFoundIfNull(question);
		List<Comment> commentList = Comment.find("question = ? order by id desc", question).fetch();
		render(question, commentList);
	}
	
	@ExternalRestrictions("Edit Exam")
	public static void submitComment(Comment comment) {
		comment.commenter = User.findByLogin(Security.connected());	
		
		validation.valid(comment);
		
		if (validation.hasErrors()) {
			flash.error("Errors !" + validation.errors());
			questionComment(comment.question.id);
		}
		flash.success("Comment Successfully Saved");
		comment.save();
	
		questionComment(comment.question.id);
		
	}

	@ExternalRestrictions("Edit Exam")
	public static void reportExam(long id) {
		Exam exam = Exam.findById(id);
		notFoundIfNull(exam);
	
		List<ReportData> listReportData = ReportData.getDataByPercent(exam);
		
		render(exam, listReportData);

	}	

}

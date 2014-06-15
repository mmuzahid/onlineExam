package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.joda.time.DateTime;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class AnswerPaper extends Model{
	
	@Required
	@ManyToOne
	public Exam exam;//exam of answer paper
	
	@Required
	@Enumerated(EnumType.STRING)
	public ExamQuestionSetType examQuestionSetType;//preserve 'questionSetType'
	
	@Required
	@ManyToOne
	public TblUser participate;//user who participates
	
	@Required
	@Email
	public String invitedEmail;//invitation email
	
	public Date date;//date of exam
	public long duration;//duration spend by participate
	
	public Date startTime;//start time of exam
	
	public Date endTime;//submit time of exam
	
	public Date submitTime;//submit time of exam
	
	@Enumerated(EnumType.STRING)
	public AnswerPaperStatus status;//answer paper status
	
	@OneToMany(mappedBy = "answerPaper")
	public List<UserAnswer> userAnswerList;//submitted answer list
	
	@ManyToMany
	public List<Question> questionList;//questions of answer paper
	
	public int numberOfCorrectAnswer;
	
	public AnswerPaper(Exam exam, TblUser participate) {
		this.exam = exam;
		this.participate = participate;
		this.date = new DateTime().toDate();
		this.startTime = new DateTime().toDate();
		this.status = AnswerPaperStatus.ACTIVE;
		this.questionList = getQuestionList(exam);
		this.examQuestionSetType = exam.questionSetType;
		this.invitedEmail = participate.email;
	}
	
	public AnswerPaper(Exam exam, String email) {
		this.exam = exam;
		this.participate = TblUser.findByEmail(email);
		this.date = new DateTime().toDate();
		this.startTime = new DateTime().toDate();
		this.status = AnswerPaperStatus.ACTIVE;
		this.questionList = Question.find("exam = ?", this.exam).fetch();
		this.invitedEmail = email;
	}

	/**change status to submitted and set end time and duration*/
	public void done() {
		this.status = AnswerPaperStatus.SUBMITED;
		this.endTime = new DateTime().toDate();
		this.submitTime = new DateTime().toDate();
		this.duration = this.endTime.getTime() - this.startTime.getTime();
	}
	
	public List<Question> getQuestionList(Exam exam) {
		List<Question> qList = new ArrayList<Question>();
		if (exam.questionSetType == ExamQuestionSetType.RANDOM) {
			qList = Question.find("exam = ?", this.exam).fetch();
			Collections.shuffle(qList);
			qList = qList.subList(0, exam.questionPerExam);
			return qList;
		}
		return Question.find("exam = ?", this.exam).fetch();
	}
	
	public String getTimeElapsed() {
		String diff = "";
		long timeDiff = Math.abs(this.endTime.getTime() - this.startTime.getTime());
		diff = String.format("%d hour(s) %d min(s) %d sec(s)"
				, TimeUnit.MILLISECONDS.toHours(timeDiff)
				,TimeUnit.MILLISECONDS.toMinutes(timeDiff) 
					- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff))
				, /*TimeUnit.MILLISECONDS.toSeconds(timeDiff)*/
					TimeUnit.MILLISECONDS.toSeconds(timeDiff)
					- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
				);
		return diff;
	}
}

package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class UserAnswer extends Model{
	
	@ManyToOne
	public AnswerPaper answerPaper;
	
	@ManyToOne
	public Question question;
	
	@Column(columnDefinition="varchar(1023)")
	public String questionTitle;
	
	public String options;
	
	public String answer;
	
	public String correctAnswer;
	
	public boolean isCorrect;
	
	public UserAnswer(AnswerPaper answerPaper, Question question) {
		this.answerPaper = answerPaper;
		this.question = question;
	}
	
	
	public UserAnswer(AnswerPaper answerPaper, Question question, String answer) {
		this.answerPaper = answerPaper;
		this.question = question;
		this.answer = answer;
	}
}

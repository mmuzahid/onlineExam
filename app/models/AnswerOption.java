package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.db.jpa.Model;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames={"optionLabel","question_id"})
							} )
public class AnswerOption extends Model{
	
	@ManyToOne
	public Question question;
	public String optionLabel;

	public boolean isAnswer;
	
	
	public AnswerOption(Question question, String optionLabel) {
		this.question = question;
		this.optionLabel = optionLabel;
	}
	
	public AnswerOption(Question question, String optionLabel, boolean isAnswer) {
		this(question, optionLabel);
		this.isAnswer = isAnswer;
	}
	
	@Override
	public String toString() {
		return this.optionLabel;
	}
}

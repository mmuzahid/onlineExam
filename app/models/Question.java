package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
//@Table(uniqueConstraints = {@UniqueConstraint(columnNames={"exam_id","quesId"})})
public class Question extends Model{
	
	@Required
	@ManyToOne
	public Exam exam;

	@Required
	@Column(columnDefinition="varchar(1023)")
	public String quesTitle;
	
	@Required
	public String quesType;
	
	@OneToMany(mappedBy="question", cascade = CascadeType.ALL)
	public List<AnswerOption> answerOptionList;

	public static List<String> allQusetionType(){
		List<String> allQType = new ArrayList<String>();
		allQType.add("checkbox");
		allQType.add("radio");
		
		return allQType;
	}
	
}

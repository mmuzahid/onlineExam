package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cascade;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import utils.ExtraUtils;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames={"examCode","author_id"})})
public class Exam extends Model {
	
	/**Basic minimal propertes*/
	@Required
	public String examName;// name of exam
	
	@Required
	public String examCode;//exam code; unique author-wise
	
//	public Date examDate;//date of exam
	
	@Transient
	public String examPeriod;
	
	@Required
	public Date startTime;
	
	@Required
	public Date endTime;
	
	/**exam behavior*/
	
	@Enumerated(EnumType.STRING)
	public ExamVisibility visibility = ExamVisibility.PUBLIC;//future plan	
	
	@Enumerated(EnumType.STRING)
	public ExamStatus status = ExamStatus.ACTIVE;
	
	@Column(columnDefinition="varchar(1023)")
	public String noticeMessage;// exam notice
	
	@Enumerated(EnumType.STRING)
	public ExamQuestionSetType questionSetType = ExamQuestionSetType.FIXED;

	@Enumerated(EnumType.STRING)
	public ExamDurationType durationType = ExamDurationType.NO_TIME_LIMIT;//future plan
	
	public long duration;
	
	@Min(0)
	public int questionPerExam;// for 'questionSetType = ExamQuestionSetType.RANDOM'
	
	@ManyToOne
	public User author;// author of exam
	
	@ManyToMany
	public List<User> participateList;// list of participates - currently useless
	
	@OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
	public List<Question> questionList = new ArrayList<Question>();//question set

	public int totalParticipates = 0;
	
	public String getExamPeriod() {
		
		if (this.examPeriod == null || this.examPeriod.isEmpty()) {
			return ExtraUtils.toDateString(this.startTime,"MM/dd/yyyy hh:mm a") 
					+ " - " + ExtraUtils.toDateString(this.endTime,"MM/dd/yyyy hh:mm a");
	
		}
		else return this.examPeriod;
	}
	public static List<Exam> findByAuthor(User currentUser) {
		return Exam.find("author = ?", currentUser).fetch();
	}

	public boolean isValidPeriod() {
		Date now = new Date();
		if (now.before(startTime) || now.after(endTime)) {
			return false;
		}
		return true;
	}
	
}

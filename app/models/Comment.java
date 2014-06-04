package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import net.sf.oval.constraint.MaxLength;
import net.sf.oval.constraint.NotBlank;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.chronostamp.NoChronostamp;

@NoChronostamp
@Entity
public class Comment extends Model{

	@Required(message="No Question Founds")
	@ManyToOne
	public Question question;
	
	@Required(message="Commneter missing")
	@ManyToOne
	public User commenter;
	
	@Required(message="Content Missing")
	@Column(columnDefinition="varchar(1023)")
	@MaxLength(value=1023, message="Content must be less than 1023 characters")
	public String content;
	
	@Required(message="Date Missing")
	public Date date = new Date();
	
}

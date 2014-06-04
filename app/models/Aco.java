package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import play.Logger;
import play.data.validation.Required;
import play.db.jpa.Model;

import models.deadbolt.ExternalizedRestriction;
import models.deadbolt.ExternalizedRestrictions;
import controllers.deadbolt.ExternalizedRestrictionsAccessor;

@Entity
public class Aco extends Model implements ExternalizedRestriction, ExternalizedRestrictions, ExternalizedRestrictionsAccessor {

	@Required
    public String name;

    public String parent;

	@ManyToMany
	public List<Role> roles = new ArrayList<Role>();

	public Aco() {

	}

	public Aco(String name) {
		this.name = name;
	}

	public Aco(String name, String parent) {
		this(name);
		this.parent = parent;
	}

	@Override
	public List<ExternalizedRestriction> getExternalisedRestrictions() {
		List<ExternalizedRestriction> tmp = new ArrayList<ExternalizedRestriction>();
		for(final Role role: this.roles) {
			tmp.add(new ExternalizedRestriction() {
				@Override
				public List<String> getRoleNames() {
					List<String> roleList = new ArrayList<String>();
					roleList.add(role.getRoleName());
					return roleList;
				}
			});
		}
		return tmp;
	}

	@Override
	public List<String> getRoleNames() {
		// TODO : Improve this part
		List<String> roleList = new ArrayList<String>();
		for(Role role: roles) {
			roleList.add(role.getRoleName());
		}

		return roleList;
	}

	public static Aco findByName(String name) {
		return Aco.find("byName", name).first();
	}

	@Override
	public ExternalizedRestrictions getExternalizedRestrictions(String name) {
		Aco aco = Aco.findByName(name);
		if(aco == null) {
			aco = new Aco(name);
			Role admin = Role.findById(1L);
			aco.roles.add(admin);
			aco.save();
		}
		if(aco.roles.size() == 0) {
			return null;
		}
		return aco;
	}

}
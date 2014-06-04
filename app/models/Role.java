package models;

import play.*;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.*;

import javax.persistence.*;

import models.deadbolt.RoleHolder;

import java.util.*;

/**
 * Role Model - Contain ACL Role data.
 */

@Entity
public class Role extends Model implements models.deadbolt.Role {

	@Required
	@Unique(message="Role Name must be unique")
	public String name;

	public Role(String name) {
		this.name = name;
	}

	// From Role Interface
	@Override
	public String getRoleName() {
		return this.name;
	}

	public static boolean isAdmin(Role role) {
		return role.id == 1L;
	}
	
}
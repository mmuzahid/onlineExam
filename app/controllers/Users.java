package controllers;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.Gson;

import annotations.Mobile;

import models.Aco;


import models.Role;
import models.User;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.libs.Crypto;
import play.libs.Crypto.HashType;
import play.mvc.With;
import utils.ExtraUtils;
import utils.MailSender;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.ExternalRestrictions;
import controllers.deadbolt.Unrestricted;

@With(Deadbolt.class)
public class Users extends Controller {

	@ExternalRestrictions("View User")
	public static void list() {
		List<User> users = User.find("id <> 1").fetch();
		render(users);
	}
	
	/**Register module*/
	@Unrestricted
	public static void signup() {
		List<Role> signupRoles = Role.find("id <> 1").fetch();
		render(signupRoles);
	}
	@Unrestricted
	public static void register(@Valid User user) {
		if (validation.hasErrors()) {
			List<Role> signupRoles = Role.find("id <> 1").fetch();
			render("@signup", user, signupRoles);
		}
		
		if (Role.isAdmin(user.role)) {
			error(401, "Unauthorized Access");
		}
		
		user.save();
		flash.success("Hi, " + user.name + " , Please signin by your username and password");
		try {
			Secure.login();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@ExternalRestrictions("Edit User")
	public static void create() {
		List<Role> roles = Role.findAll();
		render("@edit", roles);
	}

	@ExternalRestrictions("Edit User")
	public static void edit(Long id) {
		User user = User.findById(id);
		notFoundIfNull(user, "user not found");
		user.password = null;
		List<Role> roles = Role.findAll();
		render(user, roles);
	}

	@ExternalRestrictions("Edit Profile")
	public static void profile() {
		User user = User.findByLogin(Security.connected());
		notFoundIfNull(user, "user not found");
		user.password = null;
		List<Role> roles = Role.findAll();
		render(user, roles);
	}
	

	@ExternalRestrictions("Edit Profile")
	public static void submitProfile(@Valid User user) {
		if (validation.hasErrors()) {
			List<Role> roles = Role.findAll();
			render("@profile", user, roles);
		}
		
		User loggedUser = User.findByLogin(Security.connected());
		if (!Role.isAdmin(loggedUser.role)) {
			// TODO need to update in future - validation for non-admin users
			if (Role.isAdmin(user.role) || user.id != loggedUser.id) {	
				error(401, "Unauthorized Access");
			}
		}
		
		user.save();
		flash.success("Profile Saved Successfully.");
		profile();
	}
	
	@ExternalRestrictions("Edit User")
	public static void submit(@Valid User user) {
		if (validation.hasErrors()) {
			List<Role> roles = Role.findAll();
			render("@edit", user, roles);
		}
		user.save();
		flash.success("User Saved Successfully.");
		list();
	}

	@ExternalRestrictions("Edit User")
	public static void delete(Long id) {
		if (request.isAjax()) {
			notFoundIfNull(id, "id not provided");
			User user = User.findById(id);
			notFoundIfNull(user, "user not found");
			String userName = user.login;
			user.delete();
			response.status=200;
			renderText("Username '"  + userName + "'");
		}
	}

	
	/* Roles */
	@ExternalRestrictions("Edit User")
	public static void roleList() {
		List<Role> roles = Role.find("id <> 1").fetch();
		render(roles);
	}

	@ExternalRestrictions("Edit User")
	public static void roleCreate() {
		render("@roleEdit");
	}

	@ExternalRestrictions("Edit User")
	public static void roleEdit(Long id) {
		Role role = Role.findById(id);
		notFoundIfNull(role, "user not found");
		render(role);
	}

	@ExternalRestrictions("Edit User")
	public static void roleSubmit(@Valid Role role) {
		if (validation.hasErrors()) {
			render("@roleEdit", role);
		}
		role.save();
		roleList();
	}

	@ExternalRestrictions("Edit User")
	public static void roleDelete(Long id) {
		if (request.isAjax()) {
			notFoundIfNull(id, "id not provided");
			Role role = Role.findById(id);
			notFoundIfNull(role, "user not found");
			role.delete();
			ok();
		}
	}

	/* Access Control List */
	@ExternalRestrictions("ACL")
	public static void acl() {
		List<Role> roles = Role.findAll();
		List<Aco> acos = Aco.find("name <> 'ACL'").fetch();
		render(roles, acos);
	}

	@ExternalRestrictions("ACL")
	public static void updatePermission(long acoId, long roleId, boolean state) {
		notFoundIfNull(acoId);
		notFoundIfNull(roleId);
		notFoundIfNull(state);
		Aco aco = Aco.findById(acoId);
		Role role = Role.findById(roleId);
		if (role.id == 1) {
			ok();
		}
		notFoundIfNull(aco);
		notFoundIfNull(role);
		if (state) {
			aco.roles.add(role);
		} else {
			aco.roles.remove(role);
		}
		aco.save();
		ok();
	}


	
	@Unrestricted
	public static void forgotPassword() {
		render();
	}
	
	@Unrestricted
	public static void resetPasswordRequest(String userId) {
		User user = null;
		String passwordResetId = null;
	
		if (validation.email(userId).ok) {
			user = User.findByEmail(userId);
		}
		else {
			user = User.findByLogin(userId);
		}
	
		
		if (user != null) {
			passwordResetId =  Crypto.passwordHash(user.email +(long)(Math.random()*100000 + 3333300000L), HashType.SHA512);
			user.passwordResetId = passwordResetId;
	
			try {
				MailSender.sendCommentByGmail(user);
				user._save();
				flash.success("Password reset link sent to your email");
			} catch (MalformedURLException e) {
				flash.error("Invalid URL!");
				e.printStackTrace();
			} catch (EmailException e) {
				flash.error("Mail sending failed! Please Try Later.");
				e.printStackTrace();
			}
			catch (Exception e) {
				flash.error("unexpected error :" + e.getMessage());
			}
			
		}
		else {
			flash.error("User not registered!!!");
		}
	
		forgotPassword();
	}
	
	
	@Unrestricted
	public static void resetPasswordForm(String email, String passwordResetId) {
		render(email, passwordResetId);
	}
	
	@Unrestricted
	public static void resetPassword() {
		String email = request.params.get("email");
		String passwordResetId = request.params.get("passwordResetId");
		String newPassword = request.params.get("user.password");
		String confirmPassword = request.params.get("user.confirmPassword");
	
		User user = User.findByEmail(email);
		
		if (user == null) {
			flash.error("User not found");
		}
		else if (user.passwordResetId != null && user.passwordResetId.equals(passwordResetId)){
			user.password = newPassword;
			user.confirmPassword = confirmPassword;
			user.passwordResetId = null;
	
			if (validation.equals(confirmPassword, newPassword).ok) {
				user.save();
				flash.success("Password Reset Successfully");
				try {
					Secure.login();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			else {
				flash.error("Password mismatch");
			}
		}
		else {
			flash.error("Reset Link Expired");
		}
		
		render("@resetPasswordForm", email, passwordResetId);
	
	}
	
}

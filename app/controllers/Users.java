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
import models.TblUser;
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

	@ExternalRestrictions("View TblUser")
	public static void list() {
		//List<TblUser> tblUsers = TblUser.find("id <> 1").fetch();
		List<TblUser> tblUsers = TblUser.find("login <> 'root'").fetch();
		render(tblUsers);
	}
	
	/**Register module*/
	@Unrestricted
	public static void signup() {
		List<Role> signupRoles = Role.find("id <> 1").fetch();
		render(signupRoles);
	}
	@Unrestricted
	public static void register(@Valid TblUser tblUser) {
		if (validation.hasErrors()) {
			List<Role> signupRoles = Role.find("id <> 1").fetch();
			render("@signup", tblUser, signupRoles);
		}
		
		if (Role.isAdmin(tblUser.role)) {
			error(401, "Unauthorized Access");
		}
		
		tblUser.save();
		flash.success("Hi, " + tblUser.name + " , Please signin by your tblUsername and password");
		try {
			Secure.login();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@ExternalRestrictions("Edit TblUser")
	public static void create() {
		List<Role> roles = Role.findAll();
		render("@edit", roles);
	}
	

	@ExternalRestrictions("Edit TblUser")
	public static void edit(Long id) {
		TblUser tblUser = TblUser.findById(id);
		notFoundIfNull(tblUser, "tblUser not found");
		tblUser.password = null;
		List<Role> roles = Role.findAll();
		render(tblUser, roles);
	}

	@ExternalRestrictions("Edit Profile")
	public static void profile() {
		TblUser tblUser = TblUser.findByLogin(Security.connected());
		notFoundIfNull(tblUser, "tblUser not found");
		tblUser.password = null;
		List<Role> roles = Role.findAll();
		render(tblUser, roles);
	}
	

	@ExternalRestrictions("Edit Profile")
	public static void submitProfile(@Valid TblUser tblUser) {
		if (validation.hasErrors()) {
			List<Role> roles = Role.findAll();
			render("@profile", tblUser, roles);
		}
		tblUser.save();
		flash.success("Profile Saved Successfully.");
		profile();
	}
	
	@ExternalRestrictions("Edit TblUser")
	public static void submit(@Valid TblUser tblUser) {
		if (validation.hasErrors()) {
			List<Role> roles = Role.findAll();
			render("@edit", tblUser, roles);
		}
		tblUser.save();
		flash.success("User Saved Successfully.");
		list();
	}

	@ExternalRestrictions("Edit TblUser")
	public static void delete(Long id) {
		if (request.isAjax()) {
			notFoundIfNull(id, "id not provided");
			TblUser tblUser = TblUser.findById(id);
			notFoundIfNull(tblUser, "tblUser not found");
			String tblUserName = tblUser.login;
			tblUser.delete();
			response.status=200;
			renderText("Username '"  + tblUserName + "'");
		}
	}

	
	/* Roles */
	@ExternalRestrictions("Edit TblUser")
	public static void roleList() {
		List<Role> roles = Role.find("id <> 1").fetch();
		render(roles);
	}

	@ExternalRestrictions("Edit TblUser")
	public static void roleCreate() {
		render("@roleEdit");
	}

	@ExternalRestrictions("Edit TblUser")
	public static void roleEdit(Long id) {
		Role role = Role.findById(id);
		notFoundIfNull(role, "tblUser not found");
		render(role);
	}

	@ExternalRestrictions("Edit TblUser")
	public static void roleSubmit(@Valid Role role) {
		if (validation.hasErrors()) {
			render("@roleEdit", role);
		}
		role.save();
		roleList();
	}

	@ExternalRestrictions("Edit TblUser")
	public static void roleDelete(Long id) {
		if (request.isAjax()) {
			notFoundIfNull(id, "id not provided");
			Role role = Role.findById(id);
			notFoundIfNull(role, "TblUser not found");
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
	public static void resetPasswordRequest(String tblUserId) {
		TblUser tblUser = null;
		String passwordResetId = null;
	
		if (validation.email(tblUserId).ok) {
			tblUser = TblUser.findByEmail(tblUserId);
		}
		else {
			tblUser = TblUser.findByLogin(tblUserId);
		}
	
		
		if (tblUser != null) {
			passwordResetId =  Crypto.passwordHash(tblUser.email +(long)(Math.random()*100000 + 3333300000L), HashType.SHA512);
			tblUser.passwordResetId = passwordResetId;
	
			try {
				MailSender.sendCommentByGmail(tblUser);
				tblUser._save();
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
		String newPassword = request.params.get("tblUser.password");
		String confirmPassword = request.params.get("tblUser.confirmPassword");
	
		TblUser tblUser = TblUser.findByEmail(email);
		
		if (tblUser == null) {
			flash.error("User not found");
		}
		else if (tblUser.passwordResetId != null && tblUser.passwordResetId.equals(passwordResetId)){
			tblUser.password = newPassword;
			tblUser.confirmPassword = confirmPassword;
			tblUser.passwordResetId = null;
	
			if (validation.equals(confirmPassword, newPassword).ok) {
				tblUser.save();
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

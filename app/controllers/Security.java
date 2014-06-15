package controllers;

import models.tblUser;

/**
 * Security - Security handler.
 */
public class Security extends Secure.Security {

	/**
	 * Authenticate.
	 *
	 * @param username the username
	 * @param password the password
	 * @return true, if successful
	 */
	static boolean authenticate(String username, String password) {
		return tblUser.authenticate(username, password) != null;
    }

}

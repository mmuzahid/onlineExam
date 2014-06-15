package controllers;

import models.Aco;
import models.tblUser;
import models.deadbolt.RoleHolder;
import play.Logger;
import controllers.Secure.Security;
import controllers.deadbolt.DeadboltHandler;
import controllers.deadbolt.ExternalizedRestrictionsAccessor;
import controllers.deadbolt.RestrictedResourcesHandler;
import controllers.deadbolt.Unrestricted;

/**
 * AcessControlList - Deadbolt handler.
 */
public class AccessControlList extends Controller implements DeadboltHandler {

	public static void index() {
        render();
    }

	@Override
	public void beforeRoleCheck() {
		Unrestricted actionUnrestricted = getActionAnnotation(Unrestricted.class);
        if(actionUnrestricted == null) {
			// Note that if you provide your own implementation of Secure's Security class you would refer to that instead
			if (!Security.isConnected()) {
			    try {
			        if (!session.contains("username")) {
			        	flash.put("url", "GET".equals(request.method) ? request.url : "/");
				        Secure.login();
				    }
				}
				catch (Throwable t) {
					// handle this in an app-specific way
			    }
			}
        }
	}

	@Override
	public RoleHolder getRoleHolder() {
		String name = Secure.Security.connected();
		// Logger.info(name);
		return tblUser.find("byLogin", name).first();
	}

	@Override
	public void onAccessFailure(String controllerClassName) {
		Logger.error("Hit an authorisation issue when trying to access [%s]", controllerClassName);
		forbidden();
	}

	@Override
	public ExternalizedRestrictionsAccessor getExternalizedRestrictionsAccessor() {
		return new Aco();
	}

	@Override
	public RestrictedResourcesHandler getRestrictedResourcesHandler() {
		return null;
	}

}

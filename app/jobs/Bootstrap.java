/*
 * Copyright (C) 2011 mPower Health
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package jobs;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import controllers.deadbolt.ExternalRestrictions;

import play.*;
import play.jobs.*;
import play.mvc.Controller;
import play.test.Fixtures;
import models.Aco;
import models.*;


/**
 * Bootstrap Job - Process jobs when application starts
 */
@OnApplicationStart
public class Bootstrap extends Job {

	/* (non-Javadoc)
	 * @see play.jobs.Job#doJob()
	 */
	public void doJob() {
		housekeeping();
		loadInitialData();
		makeAccessControlObjects();
		setAccessForSystemUser();
	}

	private void setAccessForSystemUser() {
		//set access for system user role
		Role sysRole = Role.findById(2L);
		String[] acoNames = {"Edit Exam", "Edit Profile"};
		for (String acoName : acoNames) {
			Aco aco = Aco.findByName(acoName);
			if(aco != null) {
				aco.roles.add(sysRole);
				aco.save();
			}
			else {
				Logger.warn("Aco '%s' not exists", acoName);
			}
		}
	}

	private void housekeeping() {
		/*// Check if 'aggregate.uploadFolder' exists or not, if not then create it
		String path = Play.configuration.getProperty("aggregate.uploadDir") + File.separator;
		File dir = new File(path);
		if(!dir.exists()) {
			dir.mkdirs();
		}*/

	}

	private void loadInitialData() {
		if(User.count() == 0) {
			Fixtures.loadModels("initial-data.yml");
		}
	}
	
	private void makeAccessControlObjects() {
		List<Class> controllers = Play.classloader.getAssignableClasses(Controller.class);
		for(Class controller : controllers) {
			Method[] methods = controller.getMethods();
			for(Method method : methods) {
				if (Modifier.isStatic(method.getModifiers())
						&& !method.getDeclaringClass().equals(Controller.class)
						&& method.isAnnotationPresent(ExternalRestrictions.class)) {

					ExternalRestrictions annotation = method.getAnnotation(ExternalRestrictions.class);
					for(String name : annotation.value()) {
						Aco aco = Aco.findByName(name);
						if(aco == null) {
							aco = new Aco(name, controller.getSimpleName());
							Role admin = Role.findById(1L);
							aco.roles.add(admin);
							aco.save();
						}
					}
				}
			}
		}
	}

}
package controllers;

import play.*;
import play.libs.XML;
import play.libs.XPath;
import play.mvc.*;
import utils.ExtraUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.xml.internal.messaging.saaj.soap.impl.ElementFactory;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.ExternalRestrictions;
import controllers.deadbolt.Unrestricted;

import models.*;

/**
 * Application Controller -	Default controller of the web application
 */
@With(Deadbolt.class)
public class Application extends Controller {

    /**
     * Index.
     */
	//@ExternalRestrictions("Home")
    @Unrestricted
	public static void index() {
    	render();
    }
    
    @Unrestricted
	public static void about() {
    	render();
    }

}
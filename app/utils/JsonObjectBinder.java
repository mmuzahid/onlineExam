package utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import play.Logger;
import play.data.binding.Global;
import play.data.binding.TypeBinder;


@Global
public class JsonObjectBinder implements TypeBinder<JsonObject> {

    @Override
	public Object bind(String name, Annotation[] annotations, String value,
			Class actualClass, Type genericType) throws Exception {
 //   	Logger.info("hit binder");
    	JsonParser parser = new JsonParser();
    	JsonElement jsonElement = parser.parse(value);
    	return jsonElement.getAsJsonObject();
	}
}
package controllers;

import mac.MessageAuthenticationCode;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.security.NoSuchAlgorithmException;

@MessageAuthenticationCode
public class Application extends Controller {
  
    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }


    public static Result api(String path) throws NoSuchAlgorithmException {
        ObjectNode ret = Json.newObject();
        return ok(ret);
    }
}

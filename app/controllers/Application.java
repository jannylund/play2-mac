package controllers;

import mac.MessageAuthenticationCode;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

/**
 * Annotation on class should protect index().
 *
 * Copyright Oy Feadro AB
 * User: jan
 * Date: 2013-06-05
 * Time: 01:22
 */
@MessageAuthenticationCode
public class Application extends Controller {
  
    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
}

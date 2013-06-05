package controllers;

import mac.MessageAuthenticationCode;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

/**
 * Annotation on method should protect single method..
 *
 * Copyright Oy Feadro AB
 * User: jan
 * Date: 2013-06-05
 * Time: 01:22
 */
public class Example extends Controller {

    public static Result unprotected() {
        return ok(index.render("unprotected"));
    }

    @MessageAuthenticationCode
    public static Result macprotected() {
        return ok(index.render("protected"));
    }
}

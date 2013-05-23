package mac;

import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Oy Feadro AB
 * User: jan
 * Date: 2013-05-24
 * Time: 01:14
 */
@With(MessageAuthenticationCodeAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageAuthenticationCode {
    String value() default "";
}

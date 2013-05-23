package mac;

import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static play.Play.application;

/**
 * Oy Feadro AB
 *
 * Support Message Authentication Codes according to Pushers API: http://pusher.com/docs/rest_api#authentication
 *
 * TODO:
 * 1. Fix support for selecting hash via auth_version.
 * 2. Validate that the body actually exists and has a correct md5.
 *
 * User: jan
 * Date: 2013-05-24
 * Time: 01:15
 */
public class MessageAuthenticationCodeAction extends Action<MessageAuthenticationCode> {
    public static final String MAC_ALREADY_VALIDATED = "mac-already-validated";
    public static final String QUERYSTRING_AUTH_KEY = "auth_key";
    public static final String QUERYSTRING_AUTH_TIMESTAMP = "auth_timestamp";
    public static final String QUERYSTRING_AUTH_VERSION = "auth_version";
    public static final String QUERYSTRING_AUTH_SIGNATURE = "auth_signature";
    public static final String QUERYSTRING_BODY_MD5 = "body_md5";

    public static final String CONFIG_BASE = "messageauthenticationcode";
    public static final String CONFIG_TIME = CONFIG_BASE + ".time";
    public static final String CONFIG_TIME_PAST = CONFIG_TIME + ".past";
    public static final String CONFIG_TIME_FUTURE = CONFIG_TIME + ".future";
    public static final String CONFIG_KEYS_BASE = CONFIG_BASE + ".keys.";


    @Override
    public Result call(Http.Context ctx) throws Throwable {
        if(!ctx.args.containsKey(MAC_ALREADY_VALIDATED)) {
            ctx.args.put(MAC_ALREADY_VALIDATED, "");

            String requestMethod = ctx.request().method();
            String authKey = ctx.request().getQueryString(QUERYSTRING_AUTH_KEY);
            String authSignature = ctx.request().getQueryString(QUERYSTRING_AUTH_SIGNATURE);
            String authTimestamp = ctx.request().getQueryString(QUERYSTRING_AUTH_TIMESTAMP);

            /**
             * Get the secret for this auth key.
             */
            String secret = getSecret(authKey);
            if(secret == null || secret.equals("")) {
                return forbidden();
            }

            /**
             * Check if timestamp is valid.
             */
            if(!checkValidTimestamp(authTimestamp)) {
                return forbidden();
            }

            /**
             * Sort queryParameters sans auth_signature and make a string out of them.
             */
            Map<String, String[]> queryParameters = new TreeMap<String, String[]>(ctx.request().queryString());
            queryParameters.remove(QUERYSTRING_AUTH_SIGNATURE);

            ArrayList<String> queryP = new ArrayList<String>();
            for(Map.Entry<String, String[]> entry: queryParameters.entrySet()) {
                queryP.add(entry.getKey() + "=" + StringUtils.join(entry.getValue(), ","));
            }
            String queryString = StringUtils.join(queryP, "&");
            String path = ctx.request().path();

            /**
             * Sign and check validity.
             */

            String hmac = hmacDigest(StringUtils.join(Arrays.asList(requestMethod, path, queryString), "\n"), secret, "HmacSHA256");

            Logger.debug("hmac: " + hmac);
            Logger.debug("string: " + StringUtils.join(Arrays.asList(requestMethod, path, queryString), "\n"));

            if(!hmac.equals(authSignature)) {
                Logger.warn("MessageAuthenticationCode: Verification failed.");
                Logger.debug("was: " + authSignature);
                return forbidden();
            }

            /*
            String value = configuration.value();
            String csp = (value == null || value.isEmpty()) ? POLICIES : value;

            ctx.response().setHeader(OFFICIAL, csp);
            ctx.response().setHeader(MOZILLA, csp);
            ctx.response().setHeader(WEBKIT, csp);
            */
        }
        return delegate.call(ctx);
    }

    public static Boolean checkValidTimestamp(String authTimestamp) {
        /**
         * Disable timestamp check if we don't have a config for it.
         */
        if(!application().configuration().keys().containsAll(Arrays.asList(CONFIG_TIME_PAST, CONFIG_TIME_FUTURE))) {
            Logger.warn("MessageAuthenticationCode: Timestamp validation is disabled.");
            return true;
        }

        Long now = System.currentTimeMillis() / 1000;
        Long timestamp;
        try {
            timestamp = Long.parseLong(authTimestamp);
        } catch (NumberFormatException e) {
            Logger.warn("MessageAuthenticationCode: Timestamp validation failed.");
            return false;
        }

        if(timestamp < (now - application().configuration().getLong(CONFIG_TIME_PAST)) ||
           timestamp > (now + application().configuration().getLong(CONFIG_TIME_FUTURE))) {
            Logger.warn("MessageAuthenticationCode: Timestamp validation failed.");
            return false;
        }

        return true;
    }

    public static String getSecret(String authKey) {
        if(application().configuration().keys().contains(CONFIG_KEYS_BASE + "\"" + authKey + "\"")) {
            return application().configuration().getString(CONFIG_KEYS_BASE + "\"" + authKey + "\"");
        }
        return null;
    }


    /**
     * Calculate the hash.
     *
     * @param msg
     * @param keyString
     * @param algorithm
     * @return
     */
    public static String hmacDigest(String msg, String keyString, String algorithm) {
        String digest = null;
        try {
            SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(key);

            byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));

            StringBuffer hash = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            digest = hash.toString();
        } catch (UnsupportedEncodingException e) {
        } catch (InvalidKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        return digest;
    }
}

import mac.MessageAuthenticationCodeAction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.test.FakeApplication;
import play.test.Helpers;

import static org.fest.assertions.Assertions.assertThat;

public class ApplicationTest {
    public static FakeApplication app;

    @BeforeClass
    public static void initialize() {
        app = Helpers.fakeApplication();
        Helpers.start(app);
    }

    @AfterClass
    public static void teardown() {
        Helpers.stop(app);
    }

    /**
     * Test that the signature matches pushers examples.
     */
    @Test
    public void test_pusher_compatibility() {
        String message = "POST\n/apps/3/events\nauth_key=278d425bdf160c739803&auth_timestamp=1353088179&auth_version=1.0&body_md5=ec365a775a4cd0599faeb73354201b6f";
        String secret = "7ad3773142a6692b25b8";
        String pusherSignature = "da454824c97ba181a32ccc17a72625ba02771f50b50e1e7430e47a1f3f457e6c";
        String signature = MessageAuthenticationCodeAction.hmacDigest(message, secret, "HmacSHA256");

        assertThat(signature).isEqualTo(pusherSignature);
    }

    @Test
    public void test_timestamp_validation() {
        /**
         * Make sure timestamp validation is enabled in configuration.
         */
        assertThat(MessageAuthenticationCodeAction.checkValidTimestamp("0")).isEqualTo(false);

        /**
         * Make sure current timestamp is valid.
         */
        Long nowUnixTime = System.currentTimeMillis() / 1000;
        assertThat(MessageAuthenticationCodeAction.checkValidTimestamp(nowUnixTime.toString())).isEqualTo(true);

        /**
         * Make sure tomorrow as well as yesterday fails.
         */
        Long tomorrow = nowUnixTime + 60 * 60 * 24;
        Long yesterday = nowUnixTime - 60 * 60 * 24;
        assertThat(MessageAuthenticationCodeAction.checkValidTimestamp(tomorrow.toString())).isEqualTo(false);
        assertThat(MessageAuthenticationCodeAction.checkValidTimestamp(yesterday.toString())).isEqualTo(false);

        /**
         * Make sure within 5 minutes form now works.
         */
        Long future = nowUnixTime + 60 * 5;
        Long past = nowUnixTime - 60 * 5;
        assertThat(MessageAuthenticationCodeAction.checkValidTimestamp(future.toString())).isEqualTo(true);
        assertThat(MessageAuthenticationCodeAction.checkValidTimestamp(past.toString())).isEqualTo(true);
    }

    @Test
    public void test_md5_calculation() {
        // TODO


    }



}

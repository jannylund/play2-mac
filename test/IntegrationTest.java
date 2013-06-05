import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class IntegrationTest {
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

    @Test
    public void test_no_mac() {
        /**
         * Test class annotated to require MAC.
         */
        Result result = route(fakeRequest(GET, "/"));
        assertThat(status(result)).isEqualTo(FORBIDDEN);

        /**
         * Test method annotated to require MAC.
         */
        result = route(fakeRequest(GET, "/macprotected"));
        assertThat(status(result)).isEqualTo(FORBIDDEN);

        /**
         * Test unprotected method.
         */
        result = route(fakeRequest(GET, "/unprotected"));
        assertThat(status(result)).isEqualTo(OK);
    }


    @Test
    public void test_bad_mac() {
        // TODO

    }


    @Test
    public void test_correct_mac() {
        // TODO

    }
}

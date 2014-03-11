package org.jboss.aerogear.unifiedpush.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.List;

import org.arquillian.extension.smarturl.SchemeName;
import org.arquillian.extension.smarturl.UriScheme;
import org.jboss.aerogear.test.PushApplicationWorker;
import org.jboss.aerogear.test.UPS;
import org.jboss.aerogear.test.model.PushApplication;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.ArquillianRule;
import org.jboss.arquillian.junit.ArquillianRules;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.RestAssured;

@RunWith(ArquillianRules.class)
public class UpsWithRuleTest {

    @ArquillianRule
    public static UnifiedPushServer ups = new UnifiedPushServer() {

        @Override
        protected UnifiedPushServer setup(UPS ups) {
            // register new PushApplication
            PushApplication app = ups.with(new PushApplicationWorker()).generate().register();
            assertThat(app, is(notNullValue()));

            return this;
        }
    };

    @BeforeClass
    public static void setup() {
        RestAssured.keystore(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD);
    }

    @Deployment(testable = false)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.unifiedPushServer();
    }

    @Test
    public void verifyAppIsCreated() throws Exception {
        List<PushApplication> apps = ups.getSession().with(PushApplicationWorker.class).findAll();
        assertThat(apps, is(notNullValue()));
        assertThat(apps.size(), is(1));
    }

    @Test
    public void verifyAppIsCreated2() throws Exception {
        List<PushApplication> apps = ups.getSession().with(PushApplicationWorker.class).findAll();
        assertThat(apps, is(notNullValue()));
        assertThat(apps.size(), is(1));
    }

}
package de.osiam.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class LoginIT {
	
    protected String endpointAddress = "http://localhost:8180/osiam-server";
    protected String clientId = "example-client";
    protected String clientSecret = "secret";
    protected OsiamConnector oConnector;
    
	@Test
	public void login_with_client_credentials(){
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder(endpointAddress).
                setClientId(clientId).
                setClientSecret(clientSecret).
                setGrantType(GrantType.CLIENT_CREDENTIALS).
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        oConnector.retrieveAccessToken();
	}
	

}
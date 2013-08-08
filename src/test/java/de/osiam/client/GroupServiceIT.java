package de.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.OsiamGroupService;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.query.QueryResult;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.MultiValuedAttribute;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class GroupServiceIT extends AbstractIntegrationTestBase {

    static private String VALID_GROUP_UUID = "65c8909a-d23d-4a0c-b66c-db909e531ef6";
    private UUID uuidStandardGroup;
    private QueryResult<Group> queryResult;
    private OsiamGroupService service;

    @Before
    public void setUp() throws URISyntaxException {

        service = new OsiamGroupService.Builder(endpointAddress).build();
    }

    @Test
    public void get_a_valid_group() throws Exception {
        given_a_test_group_UUID();
        Group group = service.getGroupByUUID(uuidStandardGroup, accessToken);
        assertEquals(VALID_GROUP_UUID, group.getId());
    }

    @Test
    public void ensure_all_values_are_deserialized_correctly() throws Exception {
        given_a_test_group_UUID();
        Group actualGroup = service.getGroupByUUID(uuidStandardGroup, accessToken);

        assertEquals("Group", actualGroup.getMeta().getResourceType());
        Date created = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-07-31 21:43:18");
        assertEquals(created, actualGroup.getMeta().getCreated());
        assertEquals(created, actualGroup.getMeta().getLastModified());
        assertEquals(VALID_GROUP_UUID, actualGroup.getId());
        assertEquals("test_group01", actualGroup.getDisplayName());
        Set<MultiValuedAttribute> users = actualGroup.getMembers();
        int count = 0;
        for (MultiValuedAttribute multiValuedAttribute : users) {
            Object value = multiValuedAttribute.getValue();
            assertTrue(value.getClass().equals(String.class));
            String userId = (String) multiValuedAttribute.getValue();
            assertEquals(VALID_USER_UUID, userId);
            count++;
        }
        assertEquals(1, count);
    }

    @Test(expected = NoResultException.class)
    public void get_an_invalid_group_raises_exception() throws Exception {
        service.getGroupByUUID(UUID.fromString("b01e0710-e9b9-4181-995f-4f1f59dc2999"), accessToken);
    }

    @Test(expected = UnauthorizedException.class)
    public void provide_an_invalid_access_token_raises_exception() throws Exception {
        given_a_test_group_UUID();
        given_an_invalid_access_token();

        service.getGroupByUUID(uuidStandardGroup, accessToken);
        fail();
    }

    @Test
    public void search_for_group_by_displayName(){
        String searchString = "displayName eq test_group01";
        whenSingleGroupIsSearchedByQueryString(searchString);
        queryResultContainsOnlyValidGroiup();
    }

    @Test
    public void search_for_group_by_non_used_displayName(){
        String searchString = "displayName eq thisIsNoGroup";
        whenSingleGroupIsSearchedByQueryString(searchString);
        queryResultContainsNoValidUser();
    }

    private void queryResultContainsOnlyValidGroiup(){
        assertTrue(queryResult != null);
        assertEquals(queryResult.getTotalResults(), 1);
        queryResultContainsValidGroup();
    }

    private void queryResultContainsNoValidUser(){
        assertTrue(queryResult != null);
        assertEquals(queryResult.getTotalResults(), 0);
    }

    private void queryResultContainsValidGroup(){
        assertTrue(queryResult != null);
        for(Group actGroup : queryResult.getResources()){
            if(actGroup.getId().equals(VALID_GROUP_UUID)){
                return; // OK
            }
        }
        fail("Valid group could not be found.");
    }
    private void given_a_test_group_UUID() {
        uuidStandardGroup = UUID.fromString(VALID_GROUP_UUID);
    }

    private void whenSingleGroupIsSearchedByQueryString(String queryString) {
        queryResult = service.searchGroupsByQueryString(queryString, accessToken);
    }

}
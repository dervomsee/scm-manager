package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.inject.util.Providers;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.PageResult;
import sonia.scm.api.rest.JSONContextResolver;
import sonia.scm.api.rest.ObjectMapperProvider;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Collections;

import static java.net.URI.create;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.api.v2.resources.DispatcherMock.createDispatcher;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
@RunWith(MockitoJUnitRunner.Silent.class)
public class RepositoryRoleRootResourceTest {

  public static final String EXISTING_ROLE = "existingRole";
  public static final RepositoryRole REPOSITORY_ROLE = new RepositoryRole("existingRole", Collections.singleton("verb"), "xml");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(create("/"));

  @Rule
  public ShiroRule shiroRule = new ShiroRule();

  @Mock
  private RepositoryRoleManager repositoryRoleManager;

  @InjectMocks
  private RepositoryRoleToRepositoryRoleDtoMapperImpl roleToDtoMapper;

  @InjectMocks
  private RepositoryRoleDtoToRepositoryRoleMapperImpl dtoToRoleMapper;

  private RepositoryRoleCollectionToDtoMapper collectionToDtoMapper;

  private Dispatcher dispatcher;

  @Captor
  private ArgumentCaptor<RepositoryRole> modifyCaptor;
  @Captor
  private ArgumentCaptor<RepositoryRole> createCaptor;
  @Captor
  private ArgumentCaptor<RepositoryRole> deleteCaptor;

  @Before
  public void init() {
    collectionToDtoMapper = new RepositoryRoleCollectionToDtoMapper(roleToDtoMapper, resourceLinks);

    RepositoryRoleCollectionResource collectionResource = new RepositoryRoleCollectionResource(repositoryRoleManager, dtoToRoleMapper, collectionToDtoMapper, resourceLinks);
    RepositoryRoleResource roleResource = new RepositoryRoleResource(dtoToRoleMapper, roleToDtoMapper, repositoryRoleManager);
    RepositoryRoleRootResource rootResource = new RepositoryRoleRootResource(Providers.of(collectionResource), Providers.of(roleResource));

    doNothing().when(repositoryRoleManager).modify(modifyCaptor.capture());
    when(repositoryRoleManager.create(createCaptor.capture())).thenAnswer(invocation -> invocation.getArguments()[0]);
    doNothing().when(repositoryRoleManager).delete(deleteCaptor.capture());

    dispatcher = createDispatcher(rootResource);
    dispatcher.getProviderFactory().registerProviderInstance(new JSONContextResolver(new ObjectMapperProvider().get()));

    when(repositoryRoleManager.get(EXISTING_ROLE)).thenReturn(REPOSITORY_ROLE);
    when(repositoryRoleManager.getPage(any(), any(), anyInt(), anyInt())).thenReturn(new PageResult<>(singletonList(REPOSITORY_ROLE), 1));
  }

  @Test
  public void shouldGetNotFoundForNotExistingRole() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + "noSuchRole");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void shouldGetExistingRole() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + "existingRole");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString())
      .contains(
        "\"name\":\"existingRole\"",
        "\"verbs\":[\"verb\"]",
        "\"self\":{\"href\":\"/v2/repository-roles/existingRole\"}",
        "\"delete\":{\"href\":\"/v2/repository-roles/existingRole\"}"
      );
  }

  @Test
  @SubjectAware(username = "dent")
  public void shouldNotGetDeleteLinkWithoutPermission() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + "existingRole");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString())
      .doesNotContain("delete");
  }

  @Test
  public void shouldUpdateRole() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + "existingRole")
      .contentType(VndMediaType.REPOSITORY_ROLE)
      .content(content("{'name': 'existingRole', 'verbs': ['write', 'push']}"));
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(repositoryRoleManager).modify(any());
    assertThat(modifyCaptor.getValue().getName()).isEqualTo("existingRole");
    assertThat(modifyCaptor.getValue().getVerbs()).containsExactly("write", "push");
  }

  @Test
  public void shouldNotChangeRoleName() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + "existingRole")
      .contentType(VndMediaType.REPOSITORY_ROLE)
      .content(content("{'name': 'changedName', 'verbs': ['write', 'push']}"));
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    verify(repositoryRoleManager, never()).modify(any());
  }

  @Test
  public void shouldFailForUpdateOfNotExistingRole() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + "noSuchRole")
      .contentType(VndMediaType.REPOSITORY_ROLE)
      .content(content("{'name': 'noSuchRole', 'verbs': ['write', 'push']}"));
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    verify(repositoryRoleManager, never()).modify(any());
  }

  @Test
  public void shouldCreateRole() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2)
      .contentType(VndMediaType.REPOSITORY_ROLE)
      .content(content("{'name': 'newRole', 'verbs': ['write', 'push']}"));
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_CREATED);
    verify(repositoryRoleManager).create(any());
    assertThat(createCaptor.getValue().getName()).isEqualTo("newRole");
    assertThat(createCaptor.getValue().getVerbs()).containsExactly("write", "push");
    Object location = response.getOutputHeaders().getFirst("Location");
    assertThat(location).isEqualTo(create("/v2/repository-roles/newRole"));
  }

  @Test
  public void shouldDeleteRole() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .delete("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + EXISTING_ROLE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(repositoryRoleManager).delete(any());
    assertThat(deleteCaptor.getValue().getName()).isEqualTo(EXISTING_ROLE);
  }

  @Test
  public void shouldGetAllRoles() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString())
      .contains(
        "\"name\":\"existingRole\"",
        "\"verbs\":[\"verb\"]",
        "\"self\":{\"href\":\"/v2/repository-roles",
        "\"delete\":{\"href\":\"/v2/repository-roles/existingRole\"}",
        "\"create\":{\"href\":\"/v2/repository-roles/\"}"
      );
  }

  @Test
  @SubjectAware(username = "dent")
  public void shouldNotGetCreateLinkWithoutPermission() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString())
      .doesNotContain(
        "create"
      );
  }

  private byte[] content(String data) {
    return data.replaceAll("'", "\"").getBytes();
  }
}

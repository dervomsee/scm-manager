package sonia.scm.web.protocol;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.NotFoundException;
import sonia.scm.PushStateDispatcher;
import sonia.scm.repository.DefaultRepositoryProvider;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.HttpScmProtocol;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class HttpProtocolServletTest {

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private PushStateDispatcher dispatcher;
  @Mock
  private UserAgentParser userAgentParser;

  @InjectMocks
  private HttpProtocolServlet servlet;

  @Mock
  private RepositoryService repositoryService;
  @Mock
  private UserAgent userAgent;

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private HttpScmProtocol protocol;

  @Before
  public void init() {
    initMocks(this);
    when(userAgentParser.parse(request)).thenReturn(userAgent);
    when(userAgent.isBrowser()).thenReturn(false);
    NamespaceAndName existingRepo = new NamespaceAndName("space", "repo");
    when(serviceFactory.create(not(eq(existingRepo)))).thenThrow(new NotFoundException("Test", "a"));
    when(serviceFactory.create(existingRepo)).thenReturn(repositoryService);
  }

  @Test
  public void shouldDispatchBrowserRequests() throws ServletException, IOException {
    when(userAgent.isBrowser()).thenReturn(true);
    when(request.getRequestURI()).thenReturn("uri");

    servlet.service(request, response);

    verify(dispatcher).dispatch(request, response, "uri");
  }

  @Test
  public void shouldHandleBadPaths() throws IOException, ServletException {
    when(request.getPathInfo()).thenReturn("/illegal");

    servlet.service(request, response);

    verify(response).setStatus(400);
  }

  @Test
  public void shouldHandleNotExistingRepository() throws IOException, ServletException {
    when(request.getPathInfo()).thenReturn("/not/exists");

    servlet.service(request, response);

    verify(response).setStatus(404);
  }

  @Test
  public void shouldDelegateToProvider() throws IOException, ServletException {
    when(request.getPathInfo()).thenReturn("/space/name");
    NamespaceAndName namespaceAndName = new NamespaceAndName("space", "name");
    doReturn(repositoryService).when(serviceFactory).create(namespaceAndName);
    Repository repository = new Repository();
    when(repositoryService.getRepository()).thenReturn(repository);
    when(repositoryService.getProtocol(HttpScmProtocol.class)).thenReturn(protocol);

    servlet.service(request, response);

    verify(request).setAttribute(DefaultRepositoryProvider.ATTRIBUTE_NAME, repository);
    verify(protocol).serve(request, response, null);
    verify(repositoryService).close();
  }
}

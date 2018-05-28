package sonia.scm.api.rest.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.apache.shiro.SecurityUtils;
import sonia.scm.security.Role;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Collection;
import java.util.stream.Collectors;

@Singleton
@Path("usersnew")
public class UserNewResource extends AbstractManagerResource<User, UserException>
{

  /** Field description */
  public static final String PATH_PART = "usersnew";

  @Inject
  public UserNewResource(UserManager userManager) {
    super(userManager);
  }

  @Override
  protected GenericEntity<Collection<User>> createGenericEntity(Collection<User> items) {
    return null;
  }

  @Override
  protected String getId(User user) {
    return user.getName();
  }

  @Override
  protected String getPathPart() {
    return PATH_PART;
  }

  @GET
  @Path("{id}")
  @TypeHint(UserDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 404, condition = "not found, no group with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public Response get(@Context Request request, @Context UriInfo uriInfo, @PathParam("id") String id)
  {
    if (SecurityUtils.getSubject().hasRole(Role.ADMIN))
    {
      User user = manager.get(id);
      UserDto userDto = UserMapper.INSTANCE.userToUserDto(user, uriInfo);
      return Response.ok(userDto).build();
    }
    else
    {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
  }

  /**
   * Returns all users. <strong>Note:</strong> This method requires admin privileges.
   *
   * @param request the current request
   * @param start the start value for paging
   * @param limit the limit value for paging
   * @param sortby sort parameter
   * @param desc sort direction desc or aesc
   *
   * @return
   */
  @GET
  @TypeHint(User[].class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public Response getAll(@Context Request request, @Context UriInfo uriInfo, @DefaultValue("0")
  @QueryParam("start") int start, @DefaultValue("-1")
                         @QueryParam("limit") int limit, @QueryParam("sortby") String sortby,
                         @DefaultValue("false")
                         @QueryParam("desc") boolean desc)
  {
    Collection<User> items = fetchItems(sortby, desc, start, limit);
    items.stream().map(user -> UserMapper.INSTANCE.userToUserDto(user, uriInfo)).collect(Collectors.toList());
    return Response.ok(new GenericEntity<Collection<User>>(items) {}).build();
  }
}
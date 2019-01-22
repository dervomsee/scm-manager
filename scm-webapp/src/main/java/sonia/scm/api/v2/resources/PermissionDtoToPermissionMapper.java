package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.RepositoryPermission;

@Mapper
public abstract class PermissionDtoToPermissionMapper {

  public abstract RepositoryPermission map(RepositoryPermissionDto permissionDto);

  /**
   * this method is needed to modify an existing permission object
   *
   * @param target the target permission
   * @param repositoryPermissionDto the source dto
   * @return the mapped target permission object
   */
  public abstract void modify(@MappingTarget RepositoryPermission target, RepositoryPermissionDto repositoryPermissionDto);

}
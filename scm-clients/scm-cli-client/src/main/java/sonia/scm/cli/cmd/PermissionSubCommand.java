/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.cli.cmd;

//~--- non-JDK imports --------------------------------------------------------

import org.kohsuke.args4j.Argument;

import sonia.scm.cli.I18n;
import sonia.scm.cli.wrapper.RepositoryWrapper;
import sonia.scm.client.RepositoryClientHandler;
import sonia.scm.client.ScmClientSession;
import sonia.scm.repository.Permission;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class PermissionSubCommand extends TemplateSubCommand
{

  /**
   * Method description
   *
   *
   * @param permissions
   */
  protected abstract void modifyPermissions(List<Permission> permissions);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getId()
  {
    return id;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   */
  public void setId(String id)
  {
    this.id = id;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void run()
  {
    ScmClientSession session = createSession();
    RepositoryClientHandler handler = session.getRepositoryHandler();
    Repository repository = handler.get(id);

    if (repository != null)
    {
      List<Permission> permissions = repository.getPermissions();

      if (permissions == null)
      {
        permissions = new ArrayList<Permission>();
      }

      modifyPermissions(permissions);
      repository.setPermissions(permissions);
      handler.modify(repository);

      Map<String, Object> env = new HashMap<String, Object>();

      env.put("repository", new RepositoryWrapper(repository));
      renderTemplate(env, GetRepositorySubCommand.TEMPLATE);
    }
    else
    {
      output.println(i18n.getMessage(I18n.REPOSITORY_NOT_FOUND));
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Argument(
    usage = "optionRepositoryId",
    metaVar = "repositoryid",
    required = true
  )
  private String id;
}

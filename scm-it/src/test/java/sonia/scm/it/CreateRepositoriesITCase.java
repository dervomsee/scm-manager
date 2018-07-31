/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
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
 */


package sonia.scm.it;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import sonia.scm.util.IOUtil;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static sonia.scm.it.RegExMatcher.matchesPattern;
import static sonia.scm.it.RestUtil.createResourceUrl;
import static sonia.scm.it.RestUtil.given;

@RunWith(Parameterized.class)
public class CreateRepositoriesITCase {

  private final String repositoryType;

  public CreateRepositoriesITCase(String repositoryType) {
    this.repositoryType = repositoryType;
  }

  @Parameters(name = "{0}")
  public static Collection<String[]> createParameters() {
    Collection<String[]> params = new ArrayList<>();

    params.add(new String[]{"git"});
    params.add(new String[]{"svn"});

    if (IOUtil.search("hg") != null) {
      params.add(new String[]{"hg"});
    }

    return params;
  }

  @After
  public void cleanup() {
    TestData.cleanup();
  }

  @Test
  public void shouldCreateSuccessfully() throws IOException {
    String repositoryJson = RestUtil.readJson("repository-" + repositoryType + ".json");
    String repositoryUrl =
      given(VndMediaType.REPOSITORY)
      .body(repositoryJson)

      .when()
      .post(createResourceUrl("repositories"))

      .then()
      .statusCode(201)
      .extract()
      .header("location");

    given(VndMediaType.REPOSITORY)

      .when()
      .get(repositoryUrl)

      .then()
      .statusCode(200)
      .body(
        "name", equalTo("HeartOfGold-" + repositoryType),
        "type", equalTo(repositoryType),
        "creationDate", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z"),
        "lastModified", is(nullValue()),
        "_links.self.href", equalTo(repositoryUrl)
      );
  }

  @Test
  public void shouldRejectMultipleCreations() throws IOException {
    String repositoryJson = RestUtil.readJson("repository-" + repositoryType + ".json");
    given(VndMediaType.REPOSITORY)
      .body(repositoryJson)

      .when()
      .post(createResourceUrl("repositories"))

      .then()
      .statusCode(201);

    given(VndMediaType.REPOSITORY)
      .body(repositoryJson)

      .when()
      .post(createResourceUrl("repositories"))

      .then()
      .statusCode(409);
  }
}
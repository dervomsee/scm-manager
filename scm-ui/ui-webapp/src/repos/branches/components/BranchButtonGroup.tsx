/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Branch, Repository } from "@scm-manager/ui-types";
import { Button, ButtonAddons } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  repository: Repository;
  branch: Branch;
};

class BranchButtonGroup extends React.Component<Props> {
  render() {
    const { repository, branch, t } = this.props;

    const changesetLink = `/repo/${repository.namespace}/${repository.name}/branch/${encodeURIComponent(
      branch.name
    )}/changesets/`;
    const sourcesLink = `/repo/${repository.namespace}/${repository.name}/sources/${encodeURIComponent(branch.name)}/`;

    return (
      <ButtonAddons>
        <Button link={changesetLink} icon="exchange-alt" label={t("branch.commits")} reducedMobile={true} />
        <Button link={sourcesLink} icon="code" label={t("branch.sources")} reducedMobile={true} />
      </ButtonAddons>
    );
  }
}

export default withTranslation("repos")(BranchButtonGroup);
//@flow
import React from "react";
import type { Repository } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type Props = {
  repository: Repository,
  editUrl: string,
  t: string => string
};

class GeneralRepoNavLink extends React.Component<Props> {
  isEditable = () => {
    return this.props.repository._links.update;
  };

  render() {
    const { editUrl, t } = this.props;

    if (!this.isEditable()) {
      return null;
    }
    return <NavLink to={editUrl} icon="fas fa-cog" label={t("repositoryRoot.menu.generalNavLink")} />;
  }
}

export default translate("repos")(GeneralRepoNavLink);

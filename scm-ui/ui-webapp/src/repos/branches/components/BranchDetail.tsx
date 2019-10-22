import React from "react";
import { Repository, Branch } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import BranchButtonGroup from "./BranchButtonGroup";
import DefaultBranchTag from "./DefaultBranchTag";

type Props = {
  repository: Repository;
  branch: Branch;
  // context props
  t: (p: string) => string;
};

class BranchDetail extends React.Component<Props> {
  render() {
    const { repository, branch, t } = this.props;

    return (
      <div className="media">
        <div className="media-content subtitle">
          <strong>{t("branch.name")}</strong> {branch.name} <DefaultBranchTag defaultBranch={branch.defaultBranch} />
        </div>
        <div className="media-right">
          <BranchButtonGroup repository={repository} branch={branch} />
        </div>
      </div>
    );
  }
}

export default translate("repos")(BranchDetail);
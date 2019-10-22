import React from "react";
import { translate, InjectedTranslateProps } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
// @ts-ignore
import { Change, Diff as DiffComponent, getChangeKey, Hunk } from "react-diff-view";
import { Button, ButtonGroup } from "../buttons";
import Tag from "../Tag";
import Icon from "../Icon";
import { File, Hunk as HunkType, DiffObjectProps } from "./DiffTypes";

type Props = DiffObjectProps &
  InjectedTranslateProps & {
    file: File;
    defaultCollapse?: boolean;
  };

type Collapsible = {
  collapsed?: boolean;
};

type State = Collapsible & {
  sideBySide: boolean;
};

const DiffFilePanel = styled.div`
  /* remove bottom border for collapsed panels */
  ${(props: Collapsible) => (props.collapsed ? "border-bottom: none;" : "")};
`;

const FlexWrapLevel = styled.div`
  /* breaks into a second row
     when buttons and title become too long */
  flex-wrap: wrap;
`;

const FullWidthTitleHeader = styled.div`
  max-width: 100%;
`;

const TitleWrapper = styled.span`
  margin-left: 0.25rem;
`;

const ButtonWrapper = styled.div`
  /* align child to right */
  margin-left: auto;
`;

const HunkDivider = styled.hr`
  margin: 0.5rem 0;
`;

const ChangeTypeTag = styled(Tag)`
  margin-left: 0.75rem;
`;

const ModifiedDiffComponent = styled(DiffComponent)`
  /* column sizing */
  > colgroup .diff-gutter-col {
    width: 3.25rem;
  }
  /* prevent following content from moving down */
  > .diff-gutter:empty:hover::after {
    font-size: 0.7rem;
  }
  /* smaller font size for code */
  & .diff-line {
    font-size: 0.75rem;
  }
  /* comment padding for sidebyside view */
  &.split .diff-widget-content .is-indented-line {
    padding-left: 3.25rem;
  }
  /* comment padding for combined view */
  &.unified .diff-widget-content .is-indented-line {
    padding-left: 6.5rem;
  }
`;

class DiffFile extends React.Component<Props, State> {
  static defaultProps: Partial<Props> = {
    defaultCollapse: false
  };

  constructor(props: Props) {
    super(props);
    this.state = {
      collapsed: !!this.props.defaultCollapse,
      sideBySide: false
    };
  }

  // collapse diff by clicking collapseDiffs button
  componentDidUpdate(prevProps: Props) {
    const { defaultCollapse } = this.props;
    if (prevProps.defaultCollapse !== defaultCollapse) {
      this.setState({
        collapsed: defaultCollapse
      });
    }
  }

  toggleCollapse = () => {
    const { file } = this.props;
    if (file && !file.isBinary) {
      this.setState(state => ({
        collapsed: !state.collapsed
      }));
    }
  };

  toggleSideBySide = () => {
    this.setState(state => ({
      sideBySide: !state.sideBySide
    }));
  };

  setCollapse = (collapsed: boolean) => {
    this.setState({
      collapsed
    });
  };

  createHunkHeader = (hunk: HunkType, i: number) => {
    if (i > 0) {
      return <HunkDivider />;
    }
    return null;
  };

  collectHunkAnnotations = (hunk: HunkType) => {
    const { annotationFactory, file } = this.props;
    if (annotationFactory) {
      return annotationFactory({
        hunk,
        file
      });
    }
  };

  handleClickEvent = (change: Change, hunk: HunkType) => {
    const { file, onClick } = this.props;
    const context = {
      changeId: getChangeKey(change),
      change,
      hunk,
      file
    };
    if (onClick) {
      onClick(context);
    }
  };

  createCustomEvents = (hunk: HunkType) => {
    const { onClick } = this.props;
    if (onClick) {
      return {
        gutter: {
          onClick: (change: Change) => {
            this.handleClickEvent(change, hunk);
          }
        }
      };
    }
  };

  renderHunk = (hunk: HunkType, i: number) => {
    return (
      <Hunk
        key={hunk.content}
        hunk={hunk}
        header={this.createHunkHeader(hunk, i)}
        widgets={this.collectHunkAnnotations(hunk)}
        customEvents={this.createCustomEvents(hunk)}
      />
    );
  };

  renderFileTitle = (file: File) => {
    if (file.oldPath !== file.newPath && (file.type === "copy" || file.type === "rename")) {
      return (
        <>
          {file.oldPath} <Icon name="arrow-right" color="inherit" /> {file.newPath}
        </>
      );
    } else if (file.type === "delete") {
      return file.oldPath;
    }
    return file.newPath;
  };

  hoverFileTitle = (file: File): string => {
    if (file.oldPath !== file.newPath && (file.type === "copy" || file.type === "rename")) {
      return `${file.oldPath} > ${file.newPath}`;
    } else if (file.type === "delete") {
      return file.oldPath;
    }
    return file.newPath;
  };

  renderChangeTag = (file: File) => {
    const { t } = this.props;
    if (!file.type) {
      return;
    }
    const key = "diff.changes." + file.type;
    let value = t(key);
    if (key === value) {
      value = file.type;
    }
    const color =
      value === "added" ? "success is-outlined" : value === "deleted" ? "danger is-outlined" : "info is-outlined";

    return <ChangeTypeTag className={classNames("is-rounded", "has-text-weight-normal")} color={color} label={value} />;
  };

  render() {
    const { file, fileControlFactory, fileAnnotationFactory, t } = this.props;
    const { collapsed, sideBySide } = this.state;
    const viewType = sideBySide ? "split" : "unified";

    let body = null;
    let icon = "angle-right";
    if (!collapsed) {
      const fileAnnotations = fileAnnotationFactory ? fileAnnotationFactory(file) : null;
      icon = "angle-down";
      body = (
        <div className="panel-block is-paddingless">
          {fileAnnotations}
          <ModifiedDiffComponent className={viewType} viewType={viewType}>
            {file.hunks.map(this.renderHunk)}
          </ModifiedDiffComponent>
        </div>
      );
    }
    const collapseIcon = file && !file.isBinary ? <Icon name={icon} color="inherit" /> : null;

    const fileControls = fileControlFactory ? fileControlFactory(file, this.setCollapse) : null;
    return (
      <DiffFilePanel className={classNames("panel", "is-size-6")} collapsed={(file && file.isBinary) || collapsed}>
        <div className="panel-heading">
          <FlexWrapLevel className="level">
            <FullWidthTitleHeader
              className={classNames("level-left", "is-flex", "has-cursor-pointer")}
              onClick={this.toggleCollapse}
              title={this.hoverFileTitle(file)}
            >
              {collapseIcon}
              <TitleWrapper className={classNames("is-ellipsis-overflow", "is-size-6")}>
                {this.renderFileTitle(file)}
              </TitleWrapper>
              {this.renderChangeTag(file)}
            </FullWidthTitleHeader>
            <ButtonWrapper className={classNames("level-right", "is-flex")}>
              <ButtonGroup>
                <Button
                  action={this.toggleSideBySide}
                  icon={sideBySide ? "align-left" : "columns"}
                  label={t(sideBySide ? "diff.combined" : "diff.sideBySide")}
                  reducedMobile={true}
                />
                {fileControls}
              </ButtonGroup>
            </ButtonWrapper>
          </FlexWrapLevel>
        </div>
        {body}
      </DiffFilePanel>
    );
  }
}

export default translate("repos")(DiffFile);
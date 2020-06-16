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

import React, { FC, useRef, useState, MouseEvent, useLayoutEffect, useReducer, Dispatch } from "react";
import { Person, Repository } from "@scm-manager/ui-types";

// @ts-ignore
import { LightAsync as ReactSyntaxHighlighter, createElement } from "react-syntax-highlighter";
import { arduinoLight } from "react-syntax-highlighter/dist/cjs/styles/hljs";
import styled from "styled-components";
import DateShort from "./DateShort";
import { SingleContributor } from "./repos/changesets";
import DateFromNow from "./DateFromNow";
import { Link } from "react-router-dom";
import { DateInput } from "./dates";

// TODO move types to ui-types

export type AnnotatedSource = {
  lines: AnnotatedLine[];
  language: string;
};

export type AnnotatedLine = {
  author: Person;
  code: string;
  description: string;
  lineNumber: number;
  revision: string;
  when: Date;
};

type Props = {
  source: AnnotatedSource;
  repository: Repository;
  baseDate?: DateInput;
};

const LineElement = styled.div`
  display: inline-block;
  margin: 0;
  padding: 0;
  height: 100%;
  vertical-align: top;
`;

const Author = styled(LineElement)`
  width: 8em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
`;

const When = styled(LineElement)`
  display: inline-block;

  width: 6.5em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  margin: 0 0.5em;
`;

const LineNumber = styled(LineElement)`
  width: 3em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  border-left: 1px solid lightgrey;
  border-right: 1px solid lightgrey;

  text-align: right;

  padding: 0 0.5em;
`;

const PopoverContainer = styled.div`
  position: absolute;
  left: 2.25em;
  z-index: 100;
  width: 30em;
  display: block;

  &:before {
    position: absolute;
    content: "";
    border-style: solid;
    pointer-events: none;
    height: 0;
    width: 0;
    top: 100%;
    left: 5.5em;
    border-color: transparent;
    border-bottom-color: white;
    border-left-color: white;
    border-width: 0.4rem;
    margin-left: -0.4rem;
    margin-top: -0.4rem;
    -webkit-transform-origin: center;
    transform-origin: center;
    box-shadow: -1px 1px 2px rgba(10, 10, 10, 0.2);
    transform: rotate(-45deg);
  }
`;

const Line = styled.div`
  margin: 0;
  padding: 0;
  height: 1.5em;
  vertical-align: top;
`;

const PreTag = styled.pre`
  position: relative;
`;

const SmallHr = styled.hr`
  margin: 0.5em 0;
`;

const PopoverHeading = styled.div`
  height: 1.5em;
`;

const PopoverDescription = styled.p`
  margin-top: 0.5em;
`;

const shortRevision = (revision: string) => {
  if (revision.length > 7) {
    return revision.substring(0, 7);
  }
  return revision;
};

type LineProps = {
  annotation: AnnotatedLine;
  showAnnotation: boolean;
  nr: number;
  dispatch: Dispatch<Action>;
};

type PopoverProps = {
  annotation: AnnotatedLine;
  offsetTop?: number;
  repository: Repository;
  baseDate?: DateInput;
  dispatch: Dispatch<Action>;
};

const Popover: FC<PopoverProps> = ({ annotation, offsetTop, repository, baseDate, dispatch }) => {
  const [height, setHeight] = useState(125);
  const ref = useRef<HTMLDivElement>(null);
  useLayoutEffect(() => {
    if (ref.current) {
      setHeight(ref.current.clientHeight);
    }
  }, [ref]);

  const onMouseEnter = (e: MouseEvent) => {
    dispatch({
      type: "enter-popover"
    });
  };

  const OnMouseLeave = (e: MouseEvent) => {
    dispatch({
      type: "leave-popover"
    });
  };

  const top = (offsetTop || 0) - height - 5;
  return (
    <PopoverContainer
      ref={ref}
      onMouseEnter={onMouseEnter}
      onMouseLeave={OnMouseLeave}
      className="box changeset-details is-family-primary"
      style={{ top: `${top}px` }}
    >
      <PopoverHeading className="is-clearfix">
        <SingleContributor className="is-pulled-left" person={annotation.author} />
        <DateFromNow className="is-pulled-right" date={annotation.when} baseDate={baseDate} />
      </PopoverHeading>
      <SmallHr />
      <p>
        Changeset{" "}
        <Link to={`/repo/${repository.namespace}/${repository.name}/code/changeset/${annotation.revision}`}>
          {shortRevision(annotation.revision)}
        </Link>
      </p>
      <PopoverDescription className="content">{annotation.description}</PopoverDescription>
    </PopoverContainer>
  );
};

const Metadata = styled(LineElement)`
  cursor: help;
`;

const EmptyMetadata = styled(LineElement)`
  width: 16.7em;
`;

const dispatchDeferred = (dispatch: Dispatch<Action>, action: Action) => {
  setTimeout(() => dispatch(action), 250);
};

const AnnotateLine: FC<LineProps> = ({ annotation, showAnnotation, dispatch, nr, children }) => {
  const link = useRef<HTMLDivElement>(null);

  const onMouseEnter = (e: MouseEvent) => {
    if (showAnnotation) {
      dispatchDeferred(dispatch, {
        annotation,
        line: nr,
        offset: link.current!.offsetTop,
        type: "enter-line"
      });
    }
  };

  const OnMouseLeave = (e: MouseEvent) => {
    if (showAnnotation) {
      dispatchDeferred(dispatch, {
        line: nr,
        type: "leave-line"
      });
    }
  };

  if (!showAnnotation) {
    return (
      <Line>
        <EmptyMetadata />
        <LineNumber>{nr}</LineNumber> <LineElement>{children}</LineElement>
      </Line>
    );
  }

  return (
    <Line>
      <Metadata className="has-text-info" onMouseOver={onMouseEnter} onMouseOut={OnMouseLeave} ref={link}>
        <Author className="trigger">{annotation.author.name}</Author>{" "}
        <When>
          <DateShort value={annotation.when} />
        </When>{" "}
      </Metadata>
      <LineNumber>{nr}</LineNumber> <LineElement>{children}</LineElement>
    </Line>
  );
};

type State = {
  annotation?: AnnotatedLine;
  offset?: number;
  line?: number;
  onPopover: boolean;
  onLine: boolean;
};

type EnterLine = {
  annotation: AnnotatedLine;
  offset: number;
  line: number;
  type: "enter-line";
};

type LeaveLine = {
  line: number;
  type: "leave-line";
};

type EnterPopover = {
  type: "enter-popover";
};

type LeavePopover = {
  type: "leave-popover";
};

const initialState = {
  onPopover: false,
  onLine: false
};

type Action = EnterLine | LeaveLine | EnterPopover | LeavePopover;

const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "enter-line": {
      if (state.onPopover) {
        return state;
      }
      return {
        annotation: action.annotation,
        offset: action.offset,
        line: action.line,
        onLine: true,
        onPopover: false
      };
    }
    case "leave-line": {
      if (state.onPopover) {
        return {
          ...state,
          onLine: false
        };
      }
      return initialState;
    }
    case "enter-popover": {
      return {
        ...state,
        onPopover: true
      };
    }
    case "leave-popover": {
      if (state.onLine) {
        return {
          ...state,
          onPopover: false
        };
      }
      return initialState;
    }
  }
};

const Annotate: FC<Props> = ({ source, repository, baseDate }) => {
  const [state, dispatch] = useReducer(reducer, initialState);

  const defaultRenderer = ({ rows, stylesheet, useInlineStyles }: any) => {
    let lastRevision = "";
    return rows.map((node: any, i: number) => {
      const line = createElement({
        node,
        stylesheet,
        useInlineStyles,
        key: `code-segement${i}`
      });

      if (i + 1 < rows.length) {
        const annotation = source.lines[i];
        const newAnnotation = annotation.revision !== lastRevision;
        lastRevision = annotation.revision;
        return (
          <AnnotateLine dispatch={dispatch} annotation={annotation} showAnnotation={newAnnotation} nr={i + 1}>
            {line}
          </AnnotateLine>
        );
      }

      return line;
    });
  };

  let popover = null;
  if ((state.onPopover || state.onLine) && state.annotation) {
    popover = (
      <Popover
        annotation={state.annotation}
        dispatch={dispatch}
        offsetTop={state.offset}
        repository={repository}
        baseDate={baseDate}
      />
    );
  }

  const code = source.lines.reduce((content, line) => {
    content += line.code + "\n";
    return content;
  }, "");

  return (
    <div style={{ position: "relative" }}>
      {popover}
      <ReactSyntaxHighlighter
        showLineNumbers={false}
        language={source.language}
        style={arduinoLight}
        renderer={defaultRenderer}
        PreTag={PreTag}
      >
        {code}
      </ReactSyntaxHighlighter>
    </div>
  );
};

export default Annotate;

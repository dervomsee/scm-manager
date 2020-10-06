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

import React, { FC } from "react";
import { DateFromNow, DeleteButton } from "@scm-manager/ui-components";
import { ApiKey } from "./SetApiKeys";
import { Link } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";

type Props = {
  apiKey: ApiKey;
  onDelete: (link: string) => void;
};

export const ApiKeyEntry: FC<Props> = ({ apiKey, onDelete }) => {
  const [t] = useTranslation("users");
  let deleteButton;
  if (apiKey?._links?.delete) {
    deleteButton = (
      <DeleteButton label={t("apiKey.delete")} action={() => onDelete((apiKey._links.delete as Link).href)}/>
    );
  }

  return (
    <>
      <tr>
        <td>{apiKey.displayName}</td>
        <td>{apiKey.permissionRole}</td>
        <td className="is-hidden-mobile">
          <DateFromNow date={apiKey.created}/>
        </td>
        <td>{deleteButton}</td>
      </tr>
    </>
  );
};

export default ApiKeyEntry;

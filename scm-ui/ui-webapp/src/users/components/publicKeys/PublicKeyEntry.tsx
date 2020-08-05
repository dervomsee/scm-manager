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
import {DateFromNow, DeleteButton, DownloadButton} from "@scm-manager/ui-components/src";
import { PublicKey } from "./SetPublicKeys";
import { useTranslation } from "react-i18next";
import { Link } from "@scm-manager/ui-types";

type Props = {
  publicKey: PublicKey;
  onDelete: (link: string) => void;
};

export const PublicKeyEntry: FC<Props> = ({ publicKey, onDelete }) => {
  const [t] = useTranslation("users");

  let deleteButton;
  if (publicKey?._links?.delete) {
    deleteButton = (
      <DeleteButton label={t("publicKey.delete")} action={() => onDelete((publicKey._links.delete as Link).href)} />
    );
  }
  let downloadButton;
  if (publicKey?._links?.raw) {
    downloadButton = (
      <DownloadButton displayName={t("publicKey.download")} url={(publicKey?._links?.raw as Link).href} />
    );
  }

  return (
    <>
      <tr>
        <td>{publicKey.displayName}</td>
        <td>
          <DateFromNow date={publicKey.created} />
        </td>
        <td className="is-hidden-mobile">{publicKey.id}</td>
        <td>{deleteButton}</td>
        <td>{downloadButton}</td>
      </tr>
    </>
  );
};

export default PublicKeyEntry;

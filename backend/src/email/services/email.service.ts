import { Injectable, HttpException, HttpStatus } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import axios from 'axios';
import { map, catchError } from 'rxjs/operators';
import { EmailEntity } from '../model/email.entity';
import * as coreConsts from "../../core/CoreConstants";

const oauth = require('axios-oauth-client');

@Injectable()
export class EmailService {
  constructor(private httpService: HttpService) {}

  getToken() {
    const getClientCredentials = oauth.client(axios.create(), {
      url: process.env.EMAIL_TOKEN_URL,
      grant_type: coreConsts.oauthClientGrantType,
      client_id: process.env.EMAIL_USERNAME,
      client_secret: process.env.EMAIL_PASSWORD,
      scope: '',
    });

    return getClientCredentials()
      .then((res) => {
        if (res) return res.access_token;
      })
      .catch((e) => {
        throw new HttpException(e, HttpStatus.INTERNAL_SERVER_ERROR);
      });
  }

  create(email: EmailEntity) {
    const email_subject = 'Old Growth Field Observation form and package';
    const email_tag = 'field_verification_email'; // might link this tag to the submission id
    const email_type = 'text';

    if (
      !process.env.EMAIL_TOKEN_URL ||
      !process.env.EMAIL_API_URL ||
      !process.env.EMAIL_FROM
    ) {
      throw new HttpException(
        'Failed to send email, server side missing config of authentication url or CHES email server url or from email address or to email address',
        HttpStatus.BAD_REQUEST,
      );
    }

    if (!email.emailTo) {
      throw new HttpException(
        'Failed to send email, missing required emailTo parameter',
        HttpStatus.BAD_REQUEST,
      );
    }

    return this.getToken()
      .then((access_token) => {
        if (access_token) {
          return this.httpService
            .post(
              `${process.env.EMAIL_API_URL}/email`,
              {
                bcc: [],
                bodyType: email_type,
                body: email.emailBody || 'Hello World',
                cc: [],
                delayTS: 0,
                encoding: coreConsts.encoding,
                from: process.env.EMAIL_FROM,
                priority: 'normal',
                subject: email_subject,
                to: email.emailTo,
                tag: email_tag,
                attachments: email.emailAttachments || [],
              },
              {
                headers: { Authorization: `Bearer ${access_token}` },
              },
            )
            .pipe(
              map((r) => {
                return { status: r.status, data: r.data };
              }),
            )
            .pipe(
              catchError((e) => {
                throw new HttpException(e, HttpStatus.INTERNAL_SERVER_ERROR);
              }),
            );
        }
        throw new HttpException(
          'Failed to send email, failed to get the authentication token',
          HttpStatus.BAD_REQUEST,
        );
      })
      .catch((e) => {
        throw new HttpException(e, HttpStatus.INTERNAL_SERVER_ERROR);
      });
  }
}

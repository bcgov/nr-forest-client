import { Injectable, HttpException, HttpStatus } from '@nestjs/common';
import axios from 'axios';
import { ClientFindByNameDto } from '../dtos/client-findbyname.dto';
import { PageOptionsDto } from '../../pagination/dtos/page-option.dto';
import { PageDto } from '../../pagination/dtos/page.dto';
import { ClientPublicView } from '../entities/clientPublicView.interface';

@Injectable()
export class ClientPublicViewService {
  async findByNumber(
    clientNumber: string,
  ): Promise<ClientPublicView[] | HttpException> {
    if (!process.env.API_URL)
      return new HttpException('Missing API URL', HttpStatus.BAD_REQUEST);

    if (!process.env.X_API_KEY)
      return new HttpException('Missing API KEY', HttpStatus.BAD_REQUEST);

    return axios
      .get(process.env.API_URL + '/client/findByNumber', {
        params: { clientNumber },
        headers: { 'X-API-KEY': process.env.X_API_KEY },
      })
      .then((r) => {
        return r.data;
      })
      .catch((e) => {
        throw new HttpException(
          `Failed to get client information by client number: ${e}`,
          HttpStatus.INTERNAL_SERVER_ERROR,
        );
      });
  }

  async findByNames(
    clientFindByNameDto: ClientFindByNameDto,
    pageOptionsDto: PageOptionsDto,
  ): Promise<PageDto<ClientPublicView> | HttpException> {
    if (!process.env.API_URL)
      return new HttpException('Missing API URL', HttpStatus.BAD_REQUEST);

    if (!process.env.X_API_KEY)
      return new HttpException('Missing API KEY', HttpStatus.BAD_REQUEST);

    return axios
      .get(process.env.API_URL + '/client/findByName', {
        params: { ...clientFindByNameDto, ...pageOptionsDto },
        headers: { 'X-API-KEY': process.env.X_API_KEY },
      })
      .then((r) => {
        return r.data;
      })
      .catch((e) => {
        throw new HttpException(
          `Failed to get client information by names: ${e}`,
          HttpStatus.INTERNAL_SERVER_ERROR,
        );
      });
  }

  async findAllNonIndividuals(
    pageOptionsDto: PageOptionsDto,
  ): Promise<PageDto<ClientPublicView> | HttpException> {
    if (!process.env.API_URL)
      return new HttpException('Missing API URL', HttpStatus.BAD_REQUEST);

    if (!process.env.X_API_KEY)
      return new HttpException('Missing API KEY', HttpStatus.BAD_REQUEST);

    return axios
      .get(process.env.API_URL + '/client/findAllNonIndividuals', {
        params: { ...pageOptionsDto },
        headers: { 'X-API-KEY': process.env.X_API_KEY },
      })
      .then((r) => {
        return r.data;
      })
      .catch((e) => {
        throw new HttpException(
          `Failed to get the list of non-individual clients: ${e}`,
          HttpStatus.INTERNAL_SERVER_ERROR,
        );
      });
  }
}

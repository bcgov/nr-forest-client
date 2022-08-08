import { Injectable, HttpException, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { ClientPublicViewEntity } from '../entities/clientPublicView.entity';
import { ClientPublicView } from '../entities/clientPublicView.interface';

@Injectable()
export class ClientPublicViewService {
  constructor(
    @InjectRepository(ClientPublicViewEntity)
    private clientPublicViewRepository: Repository<ClientPublicViewEntity>,
  ) {}

  // the full non individual client list is too long, currently only set to return 10
  findInViewAllNonIndividualClients(): Promise<ClientPublicView[]> {
    return this.clientPublicViewRepository
      .createQueryBuilder('V_CLIENT_PUBLIC')
      .where('V_CLIENT_PUBLIC.CLIENT_TYPE_CODE != :clientTypeCode', {
        clientTypeCode: 'I',
      })
      .take(10)
      .getMany();
  }

  async findInViewByNumber(
    clientNumber: string,
  ): Promise<ClientPublicView[] | HttpException> {
    if (!clientNumber || clientNumber == '')
      return new HttpException(
        'Client number is required',
        HttpStatus.BAD_REQUEST,
      );

    return this.clientPublicViewRepository
      .createQueryBuilder('V_CLIENT_PUBLIC')
      .where('V_CLIENT_PUBLIC.CLIENT_NUMBER LIKE :clientNumber', {
        clientNumber: `%${clientNumber}%`,
      })
      .take(10)
      .getMany();
  }

  async findInViewByName(
    clientName: string,
    clientFirstName: string,
    clientMiddleName: string,
  ): Promise<ClientPublicView[] | HttpException> {
    if (!clientName && !clientFirstName && !clientMiddleName)
      return new HttpException(
        'Must provide one of clientName, clientFirstName, clientMiddleName',
        HttpStatus.BAD_REQUEST,
      );

    let sqlWhereStr = '1=1';
    if (clientName && clientName !== '')
      sqlWhereStr =
        sqlWhereStr +
        ' AND LOWER(V_CLIENT_PUBLIC.CLIENT_NAME) LIKE LOWER(:clientName)';
    if (clientFirstName && clientFirstName !== '')
      sqlWhereStr =
        sqlWhereStr +
        ' AND LOWER(V_CLIENT_PUBLIC.LEGAL_FIRST_NAME) LIKE LOWER(:clientFirstName)';
    if (clientMiddleName && clientMiddleName !== '')
      sqlWhereStr =
        sqlWhereStr +
        ' AND LOWER(V_CLIENT_PUBLIC.LEGAL_MIDDLE_NAME) LIKE LOWER(:clientMiddleName)';

    return this.clientPublicViewRepository
      .createQueryBuilder('V_CLIENT_PUBLIC')
      .where(sqlWhereStr, {
        clientName: `%${clientName}%`,
        clientFirstName: `%${clientFirstName}%`,
        clientMiddleName: `%${clientMiddleName}%`,
      })
      .take(10)
      .getMany();
  }
}

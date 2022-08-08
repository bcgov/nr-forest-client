import { Injectable } from '@nestjs/common';
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

  async findInViewBy(clientNumber: string): Promise<ClientPublicView[]> {
    let sqlWhereStr = '1 = 1';

    if (clientNumber && clientNumber !== '') {
      sqlWhereStr = sqlWhereStr + ' AND C.CLIENT_NUMBER LIKE :clientNumber';
    }

    return this.clientPublicViewRepository
      .createQueryBuilder()
      .select('C')
      .from(ClientPublicViewEntity, 'C')
      .where(sqlWhereStr, {
        clientNumber: '%' + clientNumber,
      })
      .take(10)
      .getMany();
  }
}

import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, Not } from 'typeorm';
import { ClientPublicEntity } from '../entities/clientPublic.entity';
import { ClientPublic } from '../entities/clientPublic.interface';

@Injectable()
export class ClientPublicService {
  constructor(
    @InjectRepository(ClientPublicEntity)
    private clientPublicRepository: Repository<ClientPublicEntity>,
  ) {}

  findByClientNumber(clientNumber: string): Promise<ClientPublic[]> {
    return this.clientPublicRepository.find({
      where: { CLIENT_NUMBER: clientNumber },
    });
  }

  // the full non individual client list is too long, currently only set to return 10
  findAllNonIndividualClient(): Promise<ClientPublic[]> {
    return this.clientPublicRepository
      .createQueryBuilder('V_CLIENT_PUBLIC')
      .where('V_CLIENT_PUBLIC.CLIENT_TYPE_CODE!=:CLIENT_TYPE_CODE', {
        CLIENT_TYPE_CODE: 'I',
      })
      .take(10)
      .getMany();
  }
}

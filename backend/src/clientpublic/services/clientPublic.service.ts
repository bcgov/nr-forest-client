import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { ClientPublicEntity } from '../entities/clientPublic.entity';
import { ClientPublic } from '../entities/clientPublic.interface';

@Injectable()
export class ClientPublicService {
  constructor(
    @InjectRepository(ClientPublicEntity)
    private clientPublicRepository: Repository<ClientPublicEntity>,
  ) {}

  findAllClients(): Promise<ClientPublic[]> {
    return this.clientPublicRepository.createQueryBuilder('client').getMany();
  }

}

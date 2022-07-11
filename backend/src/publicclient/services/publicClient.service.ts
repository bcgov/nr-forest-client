import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PublicClientEntity } from '../entities/publicClient.entity';
import { PublicClient } from '../entities/publicClient.interface';

@Injectable()
export class PublicClientService {
  constructor(
    @InjectRepository(PublicClientEntity)
    private publicClientRepository: Repository<PublicClientEntity>,
  ) {}

  // not use this, the full client list is too long to get
  findAllPublic(): Promise<PublicClientEntity[]> {
    return this.publicClientRepository.find();
  }

  findByClientNumber(clientNumber: string): Promise<PublicClient[]> {
    return this.publicClientRepository.find({
      where: { CLIENT_NUMBER: clientNumber },
    });
  }
}

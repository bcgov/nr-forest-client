import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ClientPublicController } from './controllers/clientPublic.controller';
import { ClientPublicEntity } from './entities/clientPublic.entity';
import { ClientPublicService } from './services/clientPublic.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([ClientPublicEntity]),
  ],
  controllers: [ClientPublicController],
  providers: [ClientPublicService],
})
export class ClientPublicModule {}

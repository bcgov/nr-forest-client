import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PublicClientController } from './controllers/publicClient.controller';
import { PublicClientEntity } from './entities/publicClient.entity';
import { PublicClientService } from './services/publicClient.service';

@Module({
  imports: [TypeOrmModule.forFeature([PublicClientEntity])],
  controllers: [PublicClientController],
  providers: [PublicClientService],
})
export class PublicClientModule {}

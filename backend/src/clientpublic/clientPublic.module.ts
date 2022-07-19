import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
// import { Transport, ClientsModule } from '@nestjs/microservices';
import { ClientPublicController } from './controllers/clientPublic.controller';
import { ClientPublicEntity } from './entities/clientPublic.entity';
import { ClientPublicService } from './services/clientPublic.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([ClientPublicEntity]),
    // ClientsModule.register([
    //   { name: 'CLIENTPUBLIC_MICROSERVICE', transport: Transport.TCP },
    // ]),
  ],
  controllers: [ClientPublicController],
  providers: [ClientPublicService],
})
export class ClientPublicModule {}

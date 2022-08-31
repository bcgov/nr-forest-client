import { Module } from '@nestjs/common';
import { ClientPublicViewController } from './controllers/clientPublicView.controller';
import { ClientPublicViewEntity } from './entities/clientPublicView.entity';
import { ClientPublicViewService } from './services/clientPublicView.service';

@Module({
  imports: [ClientPublicViewEntity],
  controllers: [ClientPublicViewController],
  providers: [ClientPublicViewService],
})
export class ClientPublicViewModule {}

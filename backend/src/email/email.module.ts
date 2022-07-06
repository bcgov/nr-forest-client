import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { EmailService } from './services/email.service';
import { EmailController } from './controllers/email.controller';

@Module({
  imports: [HttpModule],
  controllers: [EmailController],
  providers: [EmailService],
})
export class EmailModule {}

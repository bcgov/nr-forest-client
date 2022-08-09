import { Controller, Get } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { ClientPublicService } from '../services/clientPublic.service';

@ApiTags('Client')
@Controller('client')
export class ClientPublicController {
  constructor(private readonly clientPublicService: ClientPublicService) {}

  @Get('/findAllClients')
  findAllClients() {
    return this.clientPublicService.findAllClients();
  }

}

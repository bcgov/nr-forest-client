import { Controller, Get, Param } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { ClientPublicService } from '../services/clientPublic.service';

@ApiTags('Client Public')
@Controller('clientPublic')
export class ClientPublicController {
  constructor(private readonly clientPublicService: ClientPublicService) {}

  @Get('/client/:clientNumber')
  findByClientNumber(@Param('clientNumber') clientNumber: string) {
    return this.clientPublicService.findByClientNumber(clientNumber);
  }

  // need to add limit on this, the full client list is too long to get
  @Get('/clients/nonIndividual')
  findAllNonIndividualClients() {
    return this.clientPublicService.findAllNonIndividualClients();
  }
}

import { Controller, Get, Query } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { ApiQuery } from '@nestjs/swagger';
import { ClientPublicService } from '../services/clientPublic.service';

@ApiTags('Client')
@Controller('client')
export class ClientPublicController {
  constructor(private readonly clientPublicService: ClientPublicService) {}

  @Get('/clientNumber')
  findByClientNumber(@Query('clientNumber') clientNumber: string) {
    return this.clientPublicService.findByClientNumber(clientNumber);
  }

  @Get('/findBy')
  @ApiQuery({
    name: 'clientNumber',
    required: false,
    type: String,
  })
  @ApiQuery({
    name: 'clientName',
    required: false,
    type: String,
  })
  findBy(
    @Query('clientNumber') clientNumber?: string,
    @Query('clientName') clientName?: string,
  ) {
    return this.clientPublicService.findBy(clientNumber, clientName);
  }

  // need to add limit on this, the full client list is too long to get
  @Get('/allNonIndividualClients')
  findAllNonIndividualClients() {
    return this.clientPublicService.findAllNonIndividualClients();
  }
}

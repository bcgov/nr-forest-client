import { Controller, Get, Query } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { ApiQuery } from '@nestjs/swagger';
import { ClientPublicViewService } from '../services/clientPublicView.service';

@ApiTags('Client View')
@Controller('clientView')
export class ClientPublicViewController {
  constructor(
    private readonly clientPublicViewService: ClientPublicViewService,
  ) {}

  @Get('/findInViewBy')
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
  @ApiQuery({
    name: 'companyName',
    required: false,
    type: String,
  })
  findBy(
    @Query('clientNumber') clientNumber?: string,
    @Query('clientName') clientName?: string,
    @Query('companyName') companyName?: string,
  ) {
    return this.clientPublicViewService.findInViewBy(clientNumber, clientName, companyName);
  }

  // need to add limit on this, the full client list is too long to get
  @Get('/findInViewAllNonIndividualClients')
  findInViewAllNonIndividualClients() {
    return this.clientPublicViewService.findInViewAllNonIndividualClients();
  }
}

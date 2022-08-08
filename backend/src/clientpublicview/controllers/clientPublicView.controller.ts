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
  findBy(@Query('clientNumber') clientNumber?: string) {
    return this.clientPublicViewService.findInViewBy(clientNumber);
  }

  // need to add limit on this, the full client list is too long to get
  @Get('/findInViewAllNonIndividualClients')
  findInViewAllNonIndividualClients() {
    return this.clientPublicViewService.findInViewAllNonIndividualClients();
  }
}

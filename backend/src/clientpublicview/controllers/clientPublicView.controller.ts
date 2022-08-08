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

  @Get('/findInViewByNumber')
  @ApiQuery({
    name: 'clientNumber',
    required: true,
    type: String,
  })
  findByNumber(@Query('clientNumber') clientNumber: string) {
    return this.clientPublicViewService.findInViewByNumber(clientNumber);
  }

  @Get('/findInViewByName')
  @ApiQuery({
    name: 'clientName',
    required: false,
    type: String,
  })
  @ApiQuery({
    name: 'clientFirstName',
    required: false,
    type: String,
  })
  @ApiQuery({
    name: 'clientMiddleName',
    required: false,
    type: String,
  })
  findBy(
    @Query('clientName') clientName: string,
    @Query('clientFirstName') clientFirstName: string,
    @Query('clientMiddleName') clientMiddleName: string,
  ) {
    return this.clientPublicViewService.findInViewByName(
      clientName,
      clientFirstName,
      clientMiddleName,
    );
  }

  // need to add limit on this, the full client list is too long to get
  @Get('/findInViewAllNonIndividualClients')
  findInViewAllNonIndividualClients() {
    return this.clientPublicViewService.findInViewAllNonIndividualClients();
  }
}

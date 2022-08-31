import { Controller, Get, Query } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { ApiQuery } from '@nestjs/swagger';
import { ClientFindByNameDto } from '../dtos/client-findbyname.dto';
import { PageOptionsDto } from '../../pagination/dtos/page-option.dto';
import { ClientPublicViewService } from '../services/clientPublicView.service';

@ApiTags('Client View')
@Controller('client')
export class ClientPublicViewController {
  constructor(
    private readonly clientPublicViewService: ClientPublicViewService,
  ) {}

  @Get('/findByNumber')
  @ApiQuery({
    name: 'clientNumber',
    required: true,
    type: String,
    description: 'The number of the client',
  })
  findByNumber(@Query('clientNumber') clientNumber: string) {
    return this.clientPublicViewService.findByNumber(clientNumber);
  }

  @Get('/findByName')
  findByName(
    @Query() clientFindByNameDto: ClientFindByNameDto,
    @Query() pageOptionsDto: PageOptionsDto,
  ) {
    return this.clientPublicViewService.findByNames(
      clientFindByNameDto,
      pageOptionsDto,
    );
  }

  @Get('/findAllNonIndividuals')
  findAllNonIndividuals(@Query() pageOptionsDto: PageOptionsDto) {
    return this.clientPublicViewService.findAllNonIndividuals(pageOptionsDto);
  }
}

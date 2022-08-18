import { Controller, Get, Query } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { ApiQuery } from '@nestjs/swagger';
import { PageOptionsDto } from '../../pagination/dtos/page-option.dto';
import { ClientPublicViewService } from '../services/clientPublicView.service';

@ApiTags('Client View')
@Controller('clientPublicView')
export class ClientPublicViewController {
  constructor(
    private readonly clientPublicViewService: ClientPublicViewService,
  ) {}

  @Get('/findById')
  @ApiQuery({
    name: 'clientNumber',
    required: true,
    type: String,
  })
  findByNumber(@Query('clientNumber') clientNumber: string) {
    return this.clientPublicViewService.findByNumber(clientNumber);
  }

  @Get('/findByName')
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
  findByName(
    @Query('clientName') clientName: string,
    @Query('clientFirstName') clientFirstName: string,
    @Query('clientMiddleName') clientMiddleName: string,
    @Query() pageOptionsDto: PageOptionsDto,
  ) {
    return this.clientPublicViewService.findByName(
      clientName,
      clientFirstName,
      clientMiddleName,
      pageOptionsDto,
    );
  }

  @Get('/findAllNonIndividuals')
  findAllNonIndividuals(@Query() pageOptionsDto: PageOptionsDto) {
    return this.clientPublicViewService.findAllNonIndividuals(pageOptionsDto);
  }
}

import { Controller, Get, Param } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { PublicClientService } from '../services/publicClient.service';

@ApiTags('Public Client')
@Controller('v_public_client')
export class PublicClientController {
  constructor(private readonly publicClientService: PublicClientService) {}

  // not use this, the full client list is too long
  // @Get('/findAllPublic')
  // findAllPublic() {
  //   return this.publicClientService.findAllPublic();
  // }

  @Get('/findClientPublic/:clientNumber')
  findByClientNumber(@Param('clientNumber') clientNumber: string) {
    return this.publicClientService.findByClientNumber(clientNumber);
  }
}

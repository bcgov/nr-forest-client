import { Controller, Get } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { DeferralCategoryCodeService } from '../services/deferralCategoryCode.service';

@ApiTags('Deferral Category Code')
@Controller('deferralCategoryCode')
export class DeferralCategoryCodeController {
  constructor(private readonly deferralCategoryCodeService: DeferralCategoryCodeService) {}

  @Get('/findAllActive')
  findAllActive() {
      return this.deferralCategoryCodeService.findAllActive();
  }

}

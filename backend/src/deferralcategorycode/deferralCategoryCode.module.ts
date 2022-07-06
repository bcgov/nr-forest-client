import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { DeferralCategoryCodeController } from './controllers/deferralCategoryCode.controller';
import { DeferralCategoryCodeEntity } from './entities/deferralCategoryCode.entity';
import { DeferralCategoryCodeService } from './services/deferralCategoryCode.service';

@Module({
  imports: [TypeOrmModule.forFeature([DeferralCategoryCodeEntity])],
  controllers: [DeferralCategoryCodeController],
  providers: [DeferralCategoryCodeService],
})
export class DeferralCategoryCodeModule {}

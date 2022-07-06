import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { DeferralCategoryCodeEntity } from '../entities/deferralCategoryCode.entity';
import { DeferralCategoryCode } from '../entities/deferralCategoryCode.interface';


@Injectable()
export class DeferralCategoryCodeService {
 
  constructor(
    @InjectRepository(DeferralCategoryCodeEntity)
    private deferralCategoryCodeRepository: Repository<DeferralCategoryCodeEntity>,
) { }

  findAllActive(): Promise<DeferralCategoryCode[]> {
    return this.deferralCategoryCodeRepository
      .createQueryBuilder()
      .select("n")
      .from(DeferralCategoryCodeEntity, "n")
      .where("(n.expiry_date is null or n.expiry_date > current_date) " +
             "and n.effective_date <= current_date")
      .orderBy("n.description")
      .getMany();
  }

  findByCode(code: string) {
    //TODO
    return `This action returns a #${code} deferralCategoryCode`;
  }

}

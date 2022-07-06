import { BaseEntity, Column, Entity, PrimaryGeneratedColumn } from 'typeorm';

@Entity({ name: 'deferral_category_code' })
export class DeferralCategoryCodeEntity extends BaseEntity {

  @PrimaryGeneratedColumn({ name: 'deferral_category_code' })
  code: string;

  @Column({ name: 'description' })
  description: string;

  @Column({ name: 'effective_date' })
  effectiveDate: Date;

  @Column({ name: 'expiry_date' })
  expiryDate: Date;
  
}

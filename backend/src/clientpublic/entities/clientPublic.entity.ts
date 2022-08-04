
import { ApiProperty } from '@nestjs/swagger';
import { BaseEntity, Column, Entity, PrimaryColumn } from 'typeorm';


@Entity({ name: 'client_public' })
export class ClientPublicEntity extends BaseEntity {
  @ApiProperty({
    description: 'The client id',
  })
  @PrimaryColumn({ name: 'client_number' })
  clientNumber: string;

  @ApiProperty({
    description: 'The client name',
  })
  @Column({ name: 'client_name' })
  clientName: string;

  @ApiProperty({
    description: 'The client first name',
  })
  @Column({ name: 'legal_first_name' })
  legalFirstName: string;

  @ApiProperty({
    description: 'The client middle name',
  })
  @Column({ name: 'legal_middle_name' })
  legalMiddleName: string;

  @ApiProperty({
    description: 'The client status',
  })
  @Column({ name: 'client_status_code' })
  clientStatusCode: string;

  @ApiProperty({
    description: 'The client type',
  })
  @Column({ name: 'client_type_code' })
  clientTypeCode: string;

}


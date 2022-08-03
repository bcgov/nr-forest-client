
import { ApiProperty } from '@nestjs/swagger';
import { BaseEntity, ViewColumn, ViewEntity, PrimaryColumn } from 'typeorm';


@ViewEntity({ name: 'V_CLIENT_PUBLIC' })
export class ClientPublicViewEntity extends BaseEntity {
  @ApiProperty({
    description: 'The client id',
  })
  @PrimaryColumn({ name: 'CLIENT_NUMBER' })
  clientNumber: string;

  @ApiProperty({
    description: 'The client name',
  })
  @ViewColumn({ name: 'CLIENT_NAME' })
  clientName: string;

  @ApiProperty({
    description: 'The client first name',
  })
  @ViewColumn({ name: 'LEGAL_FIRST_NAME' })
  legalFirstName: string;

  @ApiProperty({
    description: 'The client middle name',
  })
  @ViewColumn({ name: 'LEGAL_MIDDLE_NAME' })
  legalMiddleName: string;

  @ApiProperty({
    description: 'The client status',
  })
  @ViewColumn({ name: 'CLIENT_STATUS_CODE' })
  clientStatusCode: string;

  @ApiProperty({
    description: 'The client type',
  })
  @ViewColumn({ name: 'CLIENT_TYPE_CODE' })
  clientTypeCode: string;

}


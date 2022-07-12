import { BaseEntity, ViewColumn, ViewEntity, PrimaryColumn } from 'typeorm';

@ViewEntity({ name: 'V_CLIENT_PUBLIC' })
export class ClientPublicEntity extends BaseEntity {

  @PrimaryColumn({ name: 'CLIENT_NUMBER' })
  clientNumber: number;

  @ViewColumn({ name: 'CLIENT_NAME' })
  clientName: string;

  @ViewColumn({ name: 'CLIENT_STATUS_CODE' })
  clientStatusCode: string;

  @ViewColumn({ name: 'CLIENT_TYPE_CODE' })
  clientTypeCode: string;

  @ViewColumn({ name: 'LEGAL_FIRST_NAME' })
  legalFirstName: string;

  @ViewColumn({ name: 'LEGAL_MIDDLE_NAME' })
  legalMiddleName: string;

}

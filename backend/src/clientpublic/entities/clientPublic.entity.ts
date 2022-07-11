import { BaseEntity, ViewColumn, ViewEntity, PrimaryColumn } from 'typeorm';

@ViewEntity({ name: 'V_CLIENT_PUBLIC' })
export class ClientPublicEntity extends BaseEntity {
  @ViewColumn({ name: 'CLIENT_NAME' })
  CLIENT_NAME: string;

  @ViewColumn({ name: 'CLIENT_NUMBER' })
  CLIENT_NUMBER: string;

  @ViewColumn({ name: 'CLIENT_STATUS_CODE' })
  CLIENT_STATUS_CODE: string;

  @ViewColumn({ name: 'CLIENT_TYPE_CODE' })
  CLIENT_TYPE_CODE: string;

  @ViewColumn({ name: 'LEGAL_FIRST_NAME' })
  LEGAL_FIRST_NAME: string;

  @ViewColumn({ name: 'LEGAL_MIDDLE_NAME' })
  LEGAL_MIDDLE_NAME: string;
}

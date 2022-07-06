import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class EmailEntity {
  @ApiPropertyOptional({
    example: 'Hello World',
    description: 'The email body',
  })
  emailBody?: string;

  @ApiPropertyOptional({
    example: [
      {
        content: 'file content as encoded data string',
        contentType: 'file type, i.e. application/pdf',
        encoding: 'base64/binary/hex',
        filename: '',
      },
    ],
    description: 'The attachments will send in the email',
  })
  emailAttachments?: Array<Object>;

  @ApiProperty({
    example: ['name@gov.bc.ca'],
    description: 'The email address sent to',
  })
  emailTo?: Array<string>;
}

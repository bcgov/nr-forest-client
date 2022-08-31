import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsOptional } from 'class-validator';

export class ClientFindByNameDto {
  @ApiPropertyOptional({
    description: 'The name of the entity',
  })
  @Type(() => String)
  @IsOptional()
  clientName?: string;

  @ApiPropertyOptional({
    description: "The client's first name",
  })
  @Type(() => String)
  @IsOptional()
  clientFirstName?: string;

  @ApiPropertyOptional({
    description: "The client's middle name",
  })
  @Type(() => String)
  @IsOptional()
  clientMiddleName?: string;

  @ApiPropertyOptional({
    description:
      'A code indicating a type of ministry client.<br>' +
      'Examples include but are not limited to: Corporation, Individual, Association, First Nation Band...<br>' +
      'Please enter one or more client type codes as CSV, i.e. C,A,B.',
  })
  @Type(() => String)
  @IsOptional()
  clientTypeCodesAsCsv?: string;
}

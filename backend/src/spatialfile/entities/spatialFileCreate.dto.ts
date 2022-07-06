import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class SptialFileCreate {
  @ApiProperty({
    example: 'polygon_file',
    description: 'The name of the spatial file',
  })
  fileName: string;

  @ApiProperty({
    example: 'Polygon',
    description: 'The error messages',
  })
  geoType: string;

  @ApiPropertyOptional({
    type: String,
    title: 'current_location',
    example:
      '{"type": "FeatureCollection", "features": [{"type": "Feature", "geometry": {"type": "Polygon", "coordinates": [[\
        [1474614.5923999995, 555392.24159999937],\
        [1474537.8630999997, 555275.82469999976],\
        [1474588.1340999994, 555146.17860000022],\
        [1474723.0717999991, 555080.0326000005],\
        [1474818.3220000006, 555138.24110000022],\
        [1474902.9889000002, 555220.26209999993],\
        [1474818.3220000006, 555334.03309999965],\
        [1474701.9050999992, 555437.22079999931],\
        [1474614.5923999995, 555392.24159999937]\
      ]]}}]\
      }',
  })
  geom?: string;
}

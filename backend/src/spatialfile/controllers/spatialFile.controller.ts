import { Body, Controller, Post, Get, Param } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { SpatialFileService } from '../services/spatialFile.service';
import { SptialFileCreate } from '../entities/spatialFileCreate.dto';

@ApiTags('Spatial File')
@Controller('spatialFile')
export class SpatialFileController {
  constructor(private SpatialFileService: SpatialFileService) {}

  @Post('/geojson')
  create(@Body() createSpatialDto: SptialFileCreate) {
    return this.SpatialFileService.insertSpatialFile(createSpatialDto);
  }

  @Get('/geojson/:fileId')
  getOne(@Param('fileId') fileId: number) {
    return this.SpatialFileService.getOne(fileId);
  }

  @Get('/geojson')
  getAll() {
    return this.SpatialFileService.getAll();
  }
}

import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { HttpModule } from '@nestjs/axios';
import { SpatialFileService } from './services/spatialFile.service';
import { SpatialFileController } from './controllers/spatialFile.controller';
import { SpatialFileEntity } from './entities/spatialFile.entity';

@Module({
  imports: [HttpModule, TypeOrmModule.forFeature([SpatialFileEntity])],
  controllers: [SpatialFileController],
  providers: [SpatialFileService],
})
export class SptialFileModule {}

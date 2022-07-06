import { Injectable, HttpException, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { SpatialFileEntity } from '../entities/spatialFile.entity';
import { SptialFileCreate } from '../entities/spatialFileCreate.dto';

@Injectable()
export class SpatialFileService {
  constructor(
    @InjectRepository(SpatialFileEntity)
    private spatialFileRepository: Repository<SpatialFileEntity>,
  ) {}

  getOne(fileId: number): Promise<SpatialFileEntity[]> {
    return this.spatialFileRepository.find({
      where: { fileId },
    });
  }

  getAll(): Promise<SpatialFileEntity[]> {
    return this.spatialFileRepository.find();
  }

  async insertSpatialFile(spatialFile: SptialFileCreate): Promise<any> {
    const newSpatilFileEntity = new SpatialFileEntity();
    newSpatilFileEntity.fileName = spatialFile.fileName;
    newSpatilFileEntity.geoType = spatialFile.geoType;
    newSpatilFileEntity.geom = spatialFile.geom || '';

    // console.log('spatialFile.geom', spatialFile.geom);

    await this.spatialFileRepository
      .createQueryBuilder()
      .insert()
      .values({
        ...newSpatilFileEntity,
        geom: () => `ST_GeomFromGeoJSON('${newSpatilFileEntity.geom}')`,
      })
      .execute()
      .catch((e) => {
        throw new HttpException(e, HttpStatus.NOT_IMPLEMENTED);
      });
  }
}

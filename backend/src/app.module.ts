import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { ClientPublicModule } from './clientpublic/clientPublic.module';

let port = '1521';
if (process.env.ORACLEDB_PORT) {
  port = process.env.ORACLEDB_PORT;
}
@Module({
  imports: [
    ConfigModule.forRoot(),
    TypeOrmModule.forRoot({
      type: 'oracle',
      host: process.env.ORACLEDB_HOST || 'localhost',
      port: Number(port),
      serviceName: process.env.ORACLEDB_SERVICENAME,
      connectString: `(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCPS)(HOST=nrcdb03.bcgov)(PORT=1543))) (CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=FORTMP1.nrs.bcgov)))`,
      database: process.env.ORACLEDB_DATABASE || 'postgres',
      username: process.env.ORACLEDB_USER || 'postgres',
      password: process.env.ORACLEDB_PASSWORD,
      autoLoadEntities: true, // Auto load all entities regiestered by typeorm forFeature method.
      synchronize: false, // This changes the DB schema to match changes to entities, which we might not want.
    }),
    ClientPublicModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}

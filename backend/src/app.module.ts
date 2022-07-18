import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { ClientPublicModule } from './clientpublic/clientPublic.module';

@Module({
  imports: [
    ConfigModule.forRoot(),
    TypeOrmModule.forRoot({
      type: 'oracle',
      host: process.env.ORACLEDB_HOST || 'localhost',
      port: 1521,
      serviceName: process.env.ORACLEDB_SERVICENAME,
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

import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { PublicClientModule } from './publicclient/publicClient.module';

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
    PublicClientModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}

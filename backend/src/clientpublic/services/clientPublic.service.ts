import { Injectable } from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { Repository } from "typeorm";
import { ClientPublicEntity } from "../entities/clientPublic.entity";
import { ClientPublic } from "../entities/clientPublic.interface";

@Injectable()
export class ClientPublicService {
  constructor(
    @InjectRepository(ClientPublicEntity)
    private clientPublicRepository: Repository<ClientPublicEntity>,
  ) {}

  // the full non individual client list is too long, currently only set to return 10
  findAllNonIndividualClients(): Promise<ClientPublic[]> {
    return this.clientPublicRepository
      .createQueryBuilder("V_CLIENT_PUBLIC")
      .where("V_CLIENT_PUBLIC.CLIENT_TYPE_CODE != :clientTypeCode", {
        clientTypeCode: "I",
      })
      .take(10)
      .getMany();
  }

  findBy(clientNumber: string, clientName: string): Promise<ClientPublic[]> {
    let sqlWhereStr = "1 = 1";

    if (clientNumber) {
      sqlWhereStr = sqlWhereStr + " AND C.CLIENT_NUMBER LIKE :clientNumber";
    }

    if (clientName) {
      sqlWhereStr =
        sqlWhereStr +
        " AND " +
        "(LOWER(C.CLIENT_NAME) LIKE LOWER(:clientName) OR " +
        " LOWER(C.LEGAL_FIRST_NAME) LIKE LOWER(:clientName) OR " +
        " LOWER(C.LEGAL_MIDDLE_NAME) LIKE LOWER(:clientName)" +
        ")";
    }

    //TODO: Put harcoded values in a different place
    return this.clientPublicRepository
      .createQueryBuilder()
      .select("C.CLIENT_NUMBER", "Client Number")
      .addSelect(
        "CASE WHEN C.CLIENT_TYPE_CODE = 'I' " + 
        "THEN C.LEGAL_FIRST_NAME || ' ' || C.LEGAL_MIDDLE_NAME " + 
        "ELSE C.CLIENT_NAME END",
        "Client Name",
      )
      .addSelect("C.CLIENT_TYPE_CODE", "Client Type Code")
      .addSelect(
        "CASE WHEN C.CLIENT_STATUS_CODE = 'ACT' " + 
        "THEN 'true' " + 
        "ELSE 'false' END", "Client Active")
      .from(ClientPublicEntity, "C")
      .where(sqlWhereStr, {
        clientNumber: "%" + clientNumber,
        clientName: clientName + "%",
      })
      .take(10)
      .orderBy("C.LEGAL_FIRST_NAME")
      .getRawOne();
  }
}

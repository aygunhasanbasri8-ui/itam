package com.itam.mapper;

import org.springframework.stereotype.Component;

import com.itam.model.dto.PersonSummary;
import com.itam.model.dto.ServerAssetResponse;
import com.itam.model.entity.Person;
import com.itam.model.entity.ServerAsset;

/**
 * Entity -> DTO çevrimi burada, tek yönlü. Request -> entity dönüşümü servis
 * katmanında yapılıyor çünkü orada zaten sorumlu kişi/asset tipi gibi
 * referansların DB'den çekilmesi gerekiyor; mapper'ın bunun için repository'ye
 * uzanması ArchitectureTest'in izin vermediği bir şey (mapper saf çevrim).
 */
@Component
public class ServerAssetMapper {

    public ServerAssetResponse toResponse(ServerAsset server) {
        return new ServerAssetResponse(
                server.getId(),
                server.getAssetName(),
                server.getStatus(),
                server.getNotes(),
                toSummary(server.getResponsibilityOwner()),
                server.getDomain(),
                server.getHostname(),
                server.getIpAddress(),
                server.getEnvironment(),
                server.getServerClass(),
                server.getGroupName(),
                server.getGroupInfo(),
                server.getGroupDesc(),
                server.getInfrastructureType(),
                server.getOs(),
                server.isVirtual(),
                toSummary(server.getServiceOwner()),
                toSummary(server.getAppResponsible()),
                toSummary(server.getOsResponsible()),
                server.getCustomFields(),
                server.getCreatedAt(),
                server.getUpdatedAt());
    }

    private PersonSummary toSummary(Person person) {
        return person == null ? null : new PersonSummary(person.getId(), person.getFullName());
    }
}

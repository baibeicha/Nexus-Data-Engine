package by.nexus.auth.mapper;

import java.util.List;

public abstract class BaseMapper <Entity, Dto> {
    public abstract Dto toDto(Entity entity);
    public abstract Entity toEntity(Dto dto);
    public List<Dto> toDtos(List<Entity> entities) {
        return entities.parallelStream().map(this::toDto).toList();
    }
    public List<Entity> toEntities(List<Dto> dtos) {
        return dtos.parallelStream().map(this::toEntity).toList();
    }
    public abstract Entity merge(Entity entity, Dto dto);
}

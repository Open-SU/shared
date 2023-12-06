package com.open.su;

import com.open.su.exceptions.CrudServiceException;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public abstract class CrudService<T extends Entity> {
    Logger logger;

    CrudService() {
        logger = Logger.getLogger(this.getClass());
    }

    public Uni<List<T>> list(Page page, Sort sort, @Nullable Parameters... parameters) {
        logger.trace("Listing entities with page " + page + " and sort " + sort);
        PanacheQuery<T> query = T.<T>findAll(sort).page(page);

        if (parameters != null) {
            for (Parameters parameter : parameters) {
                query = query.filter(parameter.toString(), parameter);
            }
        }

        return query.list()
                .onFailure().transform(t -> {
                    String message = "Failed to list entities";
                    logger.error("[" + Method.LIST + "] " + message, t);
                    return CrudServiceException.DATABASE_ERROR.withCause(t).withMessage(message);
                });
    }

    public Uni<T> getDetails(UUID id) {
        logger.trace("Getting entity details for entity with id " + id);
        return findOrFail(id, Method.DETAILS);
    }

    public Uni<UUID> create(T entity) {
        logger.trace("Creating entity" + entity);
        entity.id = null;
        return persistOrFail(entity, Method.CREATE)
                .onItem().transform(e -> e.id);
    }

    public Uni<UUID> update(T entity) {
        logger.trace("Updating entity with id " + entity.id);
        return findOrFail(entity.id, Method.UPDATE)
                .onItem().transformToUni((e -> persistOrFail(entity, Method.UPDATE)))
                .onItem().transform(e -> e.id);
    }

    public <E extends CrudServiceException> Uni<Void> delete(UUID id) throws E {
        logger.trace("Deleting entity with id " + id);
        return findOrFail(id, Method.DELETE)
                .onItem().transformToUni(e -> e.delete()
                        .onFailure().transform(t -> {
                            String message = "Failed to delete entity with id " + id;
                            logger.error("[" + Method.DELETE + "] " + message, t);
                            return CrudServiceException.DATABASE_ERROR.withCause(t).withMessage(message);
                        })
                        .onItem().ifNotNull().invoke(() -> logger.debug("[" + Method.DELETE + "] Deleted entity with id " + id)));
    }

    Uni<T> findOrFail(UUID id, Method method) {
        return T.<T>findById(id)
                .onFailure().transform(t -> {
                    String message = "Failed to find entity with id " + id;
                    logger.error("[" + method + "] " + message, t);
                    return CrudServiceException.DATABASE_ERROR.withCause(t).withMessage(message);
                })
                .onItem().ifNull().failWith(() -> {
                    String message = "Entity with id " + id + " not found";
                    logger.error("[" + method + "] " + message);
                    return CrudServiceException.NOT_FOUND.withMessage(message);
                });
    }

    Uni<T> persistOrFail(T entity, Method method) {
        return entity.<T>persist()
                .onFailure().transform(t -> {
                    String message = "Failed to persist entity";
                    logger.error("[" + method + "] " + message, t);
                    return CrudServiceException.DATABASE_ERROR.withCause(t).withMessage(message);
                })
                .onItem().ifNotNull().invoke(e -> logger.debug("[" + method + "] Persisted entity with id " + e.id));
    }

    enum Method {
        LIST,
        DETAILS,
        CREATE,
        UPDATE,
        DELETE
    }
}

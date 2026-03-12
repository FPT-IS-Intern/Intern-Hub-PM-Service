package com.intern.hub.pm.feign.model;

public record DmsDocumentClientModel(
        Long id,
        String objectKey,
        String originalFileName,
        String contentType,
        Long fileSize,
        Object status,
        Long actorId,
        Integer version,
        Object createdAt,
        Object updatedAt) {
}

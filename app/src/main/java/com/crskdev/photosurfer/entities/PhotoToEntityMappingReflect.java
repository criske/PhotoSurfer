package com.crskdev.photosurfer.entities;

import com.crskdev.photosurfer.data.local.photo.PhotoEntity;
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON;
import com.crskdev.photosurfer.data.remote.PagingData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.crskdev.photosurfer.entities.CollectionMappingsKt.collectionsAsLiteStr;
import static com.crskdev.photosurfer.entities.CollectionMappingsKt.collectionsJSONAsLiteStr;
import static com.crskdev.photosurfer.entities.PhotoMappingsKt.ENTRY_DELIM;
import static com.crskdev.photosurfer.entities.PhotoMappingsKt.KV_DELIM;

/**
 * Created by Cristian Pela on 19.08.2018.
 */
final class PhotoToEntityMappingReflect {

    private PhotoToEntityMappingReflect() {
    }

    static <T extends PhotoEntity> T toDbEntity(PhotoJSON photo, PagingData pagingData,
                                                Integer nextIndex, Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            setField(instance, photo.getId(), clazz, "id");
            setField(instance, nextIndex, clazz, "indexInResponse");
            setField(instance, photo.getCreatedAt(), clazz, "createdAt");
            setField(instance, photo.getUpdatedAt(), clazz, "updatedAt");
            setField(instance, photo.getWidth(), clazz, "width");
            setField(instance, photo.getHeight(), clazz, "height");
            setField(instance, photo.getColorString(), clazz, "colorString");
            setField(instance, transformUrls(photo.getUrls()), clazz, "urls");
            setField(instance, photo.getDescription(), clazz, "description");
            setField(instance, transformListToStr(photo.getCategories()), clazz, "categories");
            setField(instance, collectionsJSONAsLiteStr(photo.getCollections()), clazz, "collections");
            setField(instance, photo.getLikes(), clazz, "likes");
            setField(instance, photo.getLikedByMe(), clazz, "likedByMe");
            setField(instance, photo.getViews(), clazz, "views");
            setField(instance, photo.getAuthor().getId(), clazz, "authorId");
            setField(instance, photo.getAuthor().getUsername(), clazz, "authorUsername");
            if (pagingData != null) {
                setField(instance, pagingData.getTotal(), clazz, "total");
                setField(instance, pagingData.getCurr(), clazz, "curr");
                setField(instance, pagingData.getPrev(), clazz, "prev");
                setField(instance, pagingData.getNext(), clazz, "next");
            }
            return instance;
        } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    static <T extends PhotoEntity> T toDbEntity(Photo photo, PagingData pagingData,
                                                Integer nextIndex, Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            setField(instance, photo.getId(), clazz, "id");
            setField(instance, nextIndex, clazz, "indexInResponse");
            setField(instance, photo.getCreatedAt(), clazz, "createdAt");
            setField(instance, photo.getUpdatedAt(), clazz, "updatedAt");
            setField(instance, photo.getWidth(), clazz, "width");
            setField(instance, photo.getHeight(), clazz, "height");
            setField(instance, photo.getColorString(), clazz, "colorString");
            setField(instance, transformUrls(photo.getUrls()), clazz, "urls");
            setField(instance, photo.getDescription(), clazz, "description");
            setField(instance, transformListToStr(photo.getCategories()), clazz, "categories");
            setField(instance, collectionsAsLiteStr(photo.getCollections()), clazz, "collections");
            setField(instance, photo.getLikes(), clazz, "likes");
            setField(instance, photo.getLikedByMe(), clazz, "likedByMe");
            setField(instance, photo.getViews(), clazz, "views");
            setField(instance, photo.getAuthorId(), clazz, "authorId");
            setField(instance, photo.getAuthorUsername(), clazz, "authorUsername");
            if (pagingData != null) {
                setField(instance, pagingData.getTotal(), clazz, "total");
                setField(instance, pagingData.getCurr(), clazz, "curr");
                setField(instance, pagingData.getPrev(), clazz, "prev");
                setField(instance, pagingData.getNext(), clazz, "next");
            }
            return instance;
        } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String transformListToStr(List<String> categories) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, s = categories.size(); i < s; i++) {
            builder.append(categories.get(i));
            if (i != s - 1)
                builder.append(ENTRY_DELIM);

        }
        return builder.toString();
    }

    private static String transformUrls(EnumMap<ImageType, String> map) {
        List<String> kvList = new ArrayList<>();
        Set<Map.Entry<ImageType, String>> entries = map.entrySet();
        for (Map.Entry<ImageType, String> e : entries) {
            kvList.add(e.getKey() + KV_DELIM + e.getValue());
        }
        return transformListToStr(kvList);
    }

    private static void setField(Object instance, Object value, Class<?> clazz, String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = null;
        Class currentClazz = clazz;
        while (field == null) {
            try {
                field = currentClazz.getDeclaredField(name);
            } catch (NoSuchFieldException ex) {
                currentClazz = currentClazz.getSuperclass();
                if (currentClazz == null) {
                    throw ex;
                }
            }
        }

        Boolean wasMadePublic = false;
        if (!field.isAccessible()) {
            field.setAccessible(true);
            wasMadePublic = true;
        }
        field.set(instance, value);
        if (wasMadePublic)
            field.setAccessible(false);
    }
}

CMAKE_MINIMUM_REQUIRED(VERSION 3.22.1)

PROJECT("ignexplorer")

SET(LZMA_WITH_AES OFF CACHE BOOL "Build AES Support in lzma-sdk" FORCE)
SET(LZMA_WITH_SHA OFF CACHE BOOL "Build SHA Support in lzma-sdk" FORCE)
SET(LZMA_WITH_XZ OFF CACHE BOOL "Build XZ Support in lzma-sdk" FORCE)
SET(7Z_EXTRACT_WITH_EXAMPLE OFF CACHE BOOL "Build 7zExtract Example" FORCE)

SET(GEO_TIFF_UTILS_WITH_EXAMPLE OFF CACHE BOOL "Build GeoTiffUtils Example" FORCE)


ADD_SUBDIRECTORY(7zExtract)
ADD_SUBDIRECTORY(GeoTiffUtils)


ADD_LIBRARY(${CMAKE_PROJECT_NAME} SHARED
            ArchiveExtractor.h
            ArchiveExtractor.cpp
            ctpl_stl.h
            ImageWorker.h
            ImageWorker.cpp
            Interface.h
            Interface.cpp
            native-lib.h
            native-lib.cpp
            ProgressStatus.h
            ProgressStatus.cpp
            TileGeometry.h
            TileGeometry.cpp
            TileRecord.h
            TileRecord.cpp)

TARGET_INCLUDE_DIRECTORIES(${CMAKE_PROJECT_NAME} PUBLIC 7zExtract)
TARGET_INCLUDE_DIRECTORIES(${CMAKE_PROJECT_NAME} PUBLIC GeoTiffUtils)

TARGET_LINK_LIBRARIES(${CMAKE_PROJECT_NAME} android log 7zExtract GeoTiffUtils)


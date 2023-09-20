CMAKE_MINIMUM_REQUIRED(VERSION 3.22.1)

PROJECT("ignexplorer")
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
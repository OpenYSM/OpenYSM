cmake_minimum_required(VERSION 3.12...3.31)

include(CheckCCompilerFlag)

project(ysm-core C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(JNI)

include_directories(src/main/native)

if (JNI_FOUND)
    include_directories(${JNI_INCLUDE_DIRS})
endif (JNI_FOUND)

if(MSVC)
    set(CMAKE_WINDOWS_EXPORT_ALL_SYMBOLS ON)
endif()

set(YSM_TARGET_JNI ON CACHE BOOL "Enable YSMParser JNI build" FORCE)
add_subdirectory(YSMParser)

add_library(ysm-core SHARED
        src/main/native/render.cpp
        $<TARGET_OBJECTS:YSMParserJNI>
)

target_link_libraries(ysm-core PRIVATE cityhash libzstd_static xchacha20 AES md5 base64 zlibstatic fpng)

target_include_directories(ysm-core PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/YSMParser/YSMParser)

if(MSVC)
    target_compile_options(ysm-core PRIVATE
            /O2
            /arch:AVX2
            /W3
    )
    target_compile_definitions(ysm-core PRIVATE __FMA__)
else()
    target_compile_options(ysm-core PRIVATE
            -O3
            -fPIC
    )

    if(ANDROID)
        target_compile_options(ysm-core PRIVATE -mfloat-abi=softfp -mfpu=neon)
    else()
        target_compile_options(ysm-core PRIVATE -mavx2 -mfma)
    endif()
endif()

if(APPLE)
    set_target_properties(ysm-core PROPERTIES
            INSTALL_RPATH "@loader_path"
            MACOSX_RPATH ON
    )
endif()
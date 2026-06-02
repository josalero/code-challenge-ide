cmake_minimum_required(VERSION 3.20)
project(challenge CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

include(FetchContent)
FetchContent_Declare(
  Catch2
  GIT_REPOSITORY https://github.com/catchorg/Catch2.git
  GIT_TAG v3.5.4
  GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(Catch2)

file(GLOB TEST_SOURCES CONFIGURE_DEPENDS "${CMAKE_SOURCE_DIR}/tests/*.cpp")
add_executable(challenge_tests solution.cpp ${TEST_SOURCES})
target_link_libraries(challenge_tests PRIVATE Catch2::Catch2WithMain)
target_compile_options(challenge_tests PRIVATE -Wall -Wextra -g --coverage)
target_link_options(challenge_tests PRIVATE --coverage)

FROM gcc:14-bookworm

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        cmake \
        git \
        python3 \
        cppcheck \
        lcov \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/runner
COPY CMakeLists.txt run.py entrypoint.sh /opt/runner/
# Pre-fetch Catch2 for offline runs (--network none in submission containers).
RUN mkdir -p /tmp/catch2-prefetch/tests \
    && printf '%s\n' 'int gcd(int,int){return 0;}' > /tmp/catch2-prefetch/solution.cpp \
    && printf '%s\n' '#include <catch2/catch_test_macros.hpp>' 'TEST_CASE("x"){REQUIRE(true);}' > /tmp/catch2-prefetch/tests/dummy.cpp \
    && cp /opt/runner/CMakeLists.txt /tmp/catch2-prefetch/ \
    && cmake -S /tmp/catch2-prefetch -B /tmp/catch2-prefetch/build \
    && mv /tmp/catch2-prefetch/build/_deps/catch2-src /opt/catch2-src \
    && rm -rf /tmp/catch2-prefetch \
    && chmod +x /opt/runner/entrypoint.sh /opt/runner/run.py

ENTRYPOINT ["/opt/runner/entrypoint.sh"]

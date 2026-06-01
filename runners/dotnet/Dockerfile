FROM mcr.microsoft.com/dotnet/sdk:8.0

RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 \
    && rm -rf /var/lib/apt/lists/*

ENV NUGET_PACKAGES=/opt/nuget-packages

WORKDIR /opt/runner
COPY Challenge.csproj run.py entrypoint.sh /opt/runner/
RUN dotnet restore Challenge.csproj \
    && chmod +x /opt/runner/entrypoint.sh /opt/runner/run.py

ENTRYPOINT ["/opt/runner/entrypoint.sh"]

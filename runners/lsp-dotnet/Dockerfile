FROM mcr.microsoft.com/dotnet/sdk:8.0

ENV PATH="${PATH}:/root/.dotnet/tools"
RUN dotnet tool install --global csharp-ls --version 0.16.0

WORKDIR /workspace
ENTRYPOINT ["csharp-ls"]

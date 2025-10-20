# Hardware Performance Comparator

Aplicativo multiplataforma para comparar dados de monitoramento de desempenho de hardware gerados pelo CapFrameX.

## 📋 Funcionalidades

- **Importação de JSON**: Carregue arquivos JSON gerados pelo CapFrameX
- **Análise de Performance**: Visualize estatísticas detalhadas de FPS e frame time
- **Informações de Sistema**: Veja detalhes completos do hardware e configurações
- **Salvamento de Configurações**: Salve múltiplas configurações com nomes personalizados
- **Comparação**: Compare até 4 configurações diferentes lado a lado
- **Multiplataforma**: Funciona em Desktop (JVM), Android e Web (WASM)

## 🎯 Estatísticas Calculadas

- **FPS**: Médio, Mínimo, Máximo
- **Percentis**: 1%, 5%, 95%, 99%
- **Frame Time**: Médio, Mínimo, Máximo
- **Duração**: Tempo total de captura
- **Total de Frames**: Quantidade de frames capturados

## 🏗️ Arquitetura

### Estrutura de Pastas

```
composeApp/src/
├── commonMain/kotlin/org/example/project/
│   ├── data/
│   │   ├── CapFrameXData.kt          # Modelo de dados do JSON
│   │   ├── PerformanceStats.kt       # Estatísticas calculadas
│   │   └── HardwareConfig.kt         # Configuração salva
│   ├── repository/
│   │   └── ConfigRepository.kt       # Gerenciamento de configs
│   ├── viewmodel/
│   │   └── HardwareViewModel.kt      # Lógica de negócio
│   └── ui/
│       ├── FilePicker.kt             # Interface do file picker
│       ├── components/
│       │   ├── PerformanceCard.kt    # Card de performance
│       │   └── SystemInfoCard.kt     # Card de info do sistema
│       └── screens/
│           ├── MainScreen.kt         # Tela principal
│           ├── ConfigListScreen.kt   # Lista de configs
│           ├── ComparisonScreen.kt   # Comparação
│           └── SaveConfigDialog.kt   # Diálogo de salvamento
├── jvmMain/kotlin/                   # Implementação Desktop
├── androidMain/kotlin/               # Implementação Android
└── wasmJsMain/kotlin/                # Implementação Web
```

## 🚀 Como Usar

### 1. Importar Arquivo JSON

1. Clique no botão **+** (FAB) na tela de configurações
2. Selecione um arquivo JSON gerado pelo CapFrameX
3. O app irá processar e exibir as informações

### 2. Salvar Configuração

1. Após importar, uma tela será exibida com os dados
2. Digite um nome descritivo (ex: "RTX 4090 - Ultra Settings")
3. Clique em **Salvar**

### 3. Comparar Configurações

1. Na lista de configurações, clique nas configs que deseja comparar
2. Vá para a aba **Comparação**
3. Veja a comparação lado a lado de todas as métricas

## 🛠️ Executar o Projeto

### Desktop (JVM)
```bash
./gradlew :composeApp:run
```

### Android
```bash
./gradlew :composeApp:assembleDebug
```

### Web (WASM)
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

## 📊 Formato do JSON do CapFrameX

O app espera arquivos JSON com a seguinte estrutura:

```json
{
  "Hash": "...",
  "Info": {
    "Processor": "Intel Core i9-14900K",
    "GPU": "NVIDIA GeForce RTX 4090",
    "SystemRam": "96GB (4x24GB) 6000MT/s",
    "GameName": "bf6",
    ...
  },
  "Runs": [{
    "CaptureData": {
      "TimeInSeconds": [...],
      "MsBetweenPresents": [...],
      "MsInPresentAPI": [...],
      ...
    }
  }]
}
```

## 🔧 Tecnologias Utilizadas

- **Kotlin Multiplatform**: Código compartilhado entre plataformas
- **Compose Multiplatform**: UI declarativa multiplataforma
- **Kotlinx Serialization**: Parsing de JSON
- **Material 3**: Design system moderno

## 📝 Notas

- As configurações são armazenadas em memória (não persistem após fechar o app)
- Para persistência, pode-se implementar salvamento em arquivo ou banco de dados
- O app suporta comparação de até 4 configurações simultaneamente

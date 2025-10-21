# Arquitetura de Persistência - Hardware Performance Comparator

## Visão Geral

A camada de persistência foi implementada usando **SQLDelight** para banco de dados multiplataforma e **Koin** para injeção de dependências.

## Estrutura do Banco de Dados

### Tabela: `hardware_config`

```sql
CREATE TABLE hardware_config (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    
    -- System Info
    game_name TEXT NOT NULL,
    processor TEXT NOT NULL,
    gpu TEXT NOT NULL,
    os TEXT NOT NULL,
    system_ram TEXT NOT NULL,
    gpu_driver_version TEXT NOT NULL,
    
    -- Graphics Settings
    graphics_preset TEXT NOT NULL,
    rtx_enabled INTEGER (Boolean),
    framegen_enabled INTEGER (Boolean),
    upscaling_type TEXT NOT NULL,
    upscaling_quality TEXT,
    
    -- Performance Stats
    avg_fps REAL NOT NULL,
    min_fps REAL NOT NULL,
    max_fps REAL NOT NULL,
    percentile_1 REAL NOT NULL,
    percentile_5 REAL NOT NULL,
    
    -- Raw JSON (dados completos)
    raw_json TEXT NOT NULL
);
```

### Índices para Performance

- `idx_game_name` - Busca por jogo
- `idx_gpu` - Busca por GPU
- `idx_processor` - Busca por CPU
- `idx_graphics_preset` - Busca por preset gráfico
- `idx_upscaling_type` - Busca por tipo de upscaling
- `idx_timestamp` - Ordenação por data

## Queries Disponíveis

### 1. Queries Básicas

```kotlin
// Buscar todas as configurações
repository.getAllConfigs()

// Buscar por ID
repository.getConfigById(id)

// Deletar configuração
repository.deleteConfig(id)

// Salvar configuração
repository.saveConfig(config)
```

### 2. Queries de Filtro

```kotlin
// Buscar por jogo específico
repository.getConfigsByGame("Battlefield 6")

// Buscar por GPU específica
repository.getConfigsByGpu("RTX 4090")

// Buscar por CPU específico
repository.getConfigsByCpu("AMD Ryzen 9 7950X")

// Buscar por preset gráfico
repository.getConfigsByPreset(GraphicsPreset.MAXIMUM)

// Buscar por tipo de upscaling
repository.getConfigsByUpscaling(UpscalingType.DLSS)

// Filtro combinado: mesmo jogo + mesma GPU
repository.getConfigsByGameAndGpu("Battlefield 6", "RTX 4090")
```

### 3. Queries para Gráficos (Agregadas)

```kotlin
// Comparar FPS médio por preset gráfico
// Exemplo: BF6 + RTX 4090, variando apenas o preset
val results = repository.getAvgFpsByPreset("Battlefield 6", "RTX 4090")
// Retorna: graphics_preset, avg_fps, avg_1_low, test_count

// Comparar FPS médio por tipo de upscaling
// Exemplo: BF6 + RTX 4090 + Preset Máximo, variando upscaling
val results = repository.getAvgFpsByUpscaling(
    "Battlefield 6", 
    "RTX 4090", 
    GraphicsPreset.MAXIMUM
)
// Retorna: upscaling_type, upscaling_quality, avg_fps, avg_1_low, test_count

// Comparar FPS médio por GPU
// Exemplo: BF6 + Preset Máximo, variando apenas a GPU
val results = repository.getAvgFpsByGpu("Battlefield 6", GraphicsPreset.MAXIMUM)
// Retorna: gpu, avg_fps, avg_1_low, test_count
```

### 4. Queries de Metadados

```kotlin
// Buscar jogos únicos salvos
val games = repository.getUniqueGames()

// Buscar GPUs únicas salvas
val gpus = repository.getUniqueGpus()

// Buscar CPUs únicos salvos
val cpus = repository.getUniqueCpus()
```

## Exemplos de Uso para Gráficos

### Exemplo 1: Comparar Presets Gráficos

**Cenário**: Mesmo jogo (BF6), mesma GPU (RTX 4090), variando apenas o preset.

```kotlin
val repository = get<SqlDelightConfigRepository>()

// Buscar dados agregados
val data = repository.getAvgFpsByPreset("Battlefield 6", "RTX 4090")

// Gerar gráfico de barras
data.forEach { result ->
    println("${result.graphics_preset}: ${result.avg_fps} FPS (1% Low: ${result.avg_1_low})")
}
```

### Exemplo 2: Comparar Upscaling

**Cenário**: Mesmo jogo, mesma GPU, mesmo preset, variando upscaling.

```kotlin
val data = repository.getAvgFpsByUpscaling(
    gameName = "Battlefield 6",
    gpu = "RTX 4090",
    preset = GraphicsPreset.MAXIMUM
)

// Gerar gráfico comparativo
data.forEach { result ->
    val label = if (result.upscaling_quality != null) {
        "${result.upscaling_type} (${result.upscaling_quality})"
    } else {
        result.upscaling_type
    }
    println("$label: ${result.avg_fps} FPS")
}
```

### Exemplo 3: Comparar GPUs

**Cenário**: Mesmo jogo, mesmo preset, variando apenas a GPU.

```kotlin
val data = repository.getAvgFpsByGpu("Battlefield 6", GraphicsPreset.MAXIMUM)

// Gerar gráfico de barras
data.forEach { result ->
    println("${result.gpu}: ${result.avg_fps} FPS")
}
```

## Injeção de Dependências (Koin)

### Módulos Configurados

```kotlin
// appModule (comum)
val appModule = module {
    single { HardwareDatabase(get()) }
    singleOf(::SqlDelightConfigRepository) bind ConfigRepository::class
    viewModelOf(::HardwareViewModel)
}

// platformModule (específico de cada plataforma)
// Android
actual val platformModule = module {
    single { DatabaseDriverFactory(androidContext()) }
}

// JVM/Desktop
actual val platformModule = module {
    single { DatabaseDriverFactory() }
}

// iOS
actual val platformModule = module {
    single { DatabaseDriverFactory() }
}
```

### Uso no Compose

```kotlin
@Composable
fun MainScreen(viewModel: HardwareViewModel = koinViewModel()) {
    // ViewModel injetado automaticamente com repository
}
```

## Localização dos Bancos de Dados

- **Android**: `/data/data/org.example.project/databases/hardware_performance.db`
- **Desktop (JVM)**: `~/.hardware_performance/hardware_performance.db`
- **iOS**: Sandbox da aplicação

## Migrações Futuras

Para adicionar novas colunas ou tabelas:

1. Atualizar `HardwareConfig.sq`
2. Incrementar versão do schema no `build.gradle.kts`:

```kotlin
sqldelight {
    databases {
        create("HardwareDatabase") {
            packageName.set("org.example.project.database")
            version = 2 // Incrementar versão
        }
    }
}
```

3. Criar arquivo de migração em `sqldelight/migrations/`

## Próximos Passos

1. **Implementar tela de filtros** na UI para selecionar:
   - Jogo
   - GPU
   - CPU
   - Preset
   - Upscaling

2. **Melhorar ChartsScreen** para usar queries agregadas:
   - Gráficos de barras comparativos
   - Gráficos de linha para múltiplas métricas
   - Filtros dinâmicos

3. **Adicionar exportação de dados**:
   - Exportar comparações como CSV
   - Exportar gráficos como imagem

4. **Implementar backup/restore**:
   - Exportar banco completo
   - Importar de backup

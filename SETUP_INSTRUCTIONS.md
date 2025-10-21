# Setup e Build - Hardware Performance Comparator

## Dependências Adicionadas

### SQLDelight (v2.0.2)
- `app.cash.sqldelight:runtime`
- `app.cash.sqldelight:coroutines-extensions`
- `app.cash.sqldelight:android-driver` (Android)
- `app.cash.sqldelight:sqlite-driver` (JVM/Desktop)
- `app.cash.sqldelight:native-driver` (iOS)

### Koin (v4.0.1)
- `io.insert-koin:koin-core`
- `io.insert-koin:koin-compose`
- `io.insert-koin:koin-android` (Android)

## Build do Projeto

### 1. Sincronizar Gradle

```bash
./gradlew clean
```

### 2. Gerar código SQLDelight

O SQLDelight irá gerar automaticamente as classes Kotlin a partir do arquivo `.sq`:

```bash
./gradlew generateCommonMainHardwareDatabaseInterface
```

### 3. Build por Plataforma

**Android:**
```bash
./gradlew assembleDebug
```

**Desktop (JVM):**
```bash
./gradlew run
```

**iOS:**
```bash
./gradlew iosSimulatorArm64MainBinaries
```

## Estrutura de Arquivos Criados

```
composeApp/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   └── org/example/project/
│   │   │       ├── database/
│   │   │       │   └── DatabaseDriverFactory.kt
│   │   │       ├── di/
│   │   │       │   └── AppModule.kt
│   │   │       ├── data/
│   │   │       │   └── ChartFilter.kt
│   │   │       ├── repository/
│   │   │       │   └── SqlDelightConfigRepository.kt
│   │   │       └── KoinInitializer.kt
│   │   └── sqldelight/
│   │       └── org/example/project/database/
│   │           └── HardwareConfig.sq  ← Schema SQL
│   │
│   ├── androidMain/
│   │   └── kotlin/
│   │       └── org/example/project/
│   │           ├── database/
│   │           │   └── DatabaseDriverFactory.android.kt
│   │           ├── di/
│   │           │   └── PlatformModule.android.kt
│   │           └── HardwareApplication.kt
│   │
│   ├── jvmMain/
│   │   └── kotlin/
│   │       └── org/example/project/
│   │           ├── database/
│   │           │   └── DatabaseDriverFactory.jvm.kt
│   │           └── di/
│   │               └── PlatformModule.jvm.kt
│   │
│   └── iosMain/
│       └── kotlin/
│           └── org/example/project/
│               ├── database/
│               │   └── DatabaseDriverFactory.ios.kt
│               └── di/
│                   └── PlatformModule.ios.kt
```

## Possíveis Erros e Soluções

### Erro: "Unresolved reference: HardwareDatabase"

**Causa**: O código SQLDelight ainda não foi gerado.

**Solução**:
```bash
./gradlew generateCommonMainHardwareDatabaseInterface
```

### Erro: "Cannot access class 'org.koin.core.module.Module'"

**Causa**: Dependências do Koin não sincronizadas.

**Solução**:
```bash
./gradlew --refresh-dependencies
```

### Erro: "No Koin context found"

**Causa**: Koin não foi inicializado antes de usar `koinViewModel()`.

**Solução**: Verificar se `initKoin()` está sendo chamado:
- Android: `HardwareApplication.onCreate()`
- JVM: `main()` antes de `application {}`
- iOS: No `AppDelegate` ou `@main`

## Próximos Passos de Desenvolvimento

### 1. Melhorar ChartsScreen com Filtros

Adicionar UI de filtros na `ChartsScreen`:

```kotlin
@Composable
fun ChartsScreen(
    configs: List<HardwareConfig>,
    repository: SqlDelightConfigRepository = get()
) {
    var selectedGame by remember { mutableStateOf<String?>(null) }
    var selectedGpu by remember { mutableStateOf<String?>(null) }
    var compareBy by remember { mutableStateOf(CompareBy.PRESET) }
    
    // Buscar opções de filtro
    val games = remember { repository.getUniqueGames() }
    val gpus = remember { repository.getUniqueGpus() }
    
    // Buscar dados agregados
    val chartData = when (compareBy) {
        CompareBy.PRESET -> repository.getAvgFpsByPreset(selectedGame!!, selectedGpu!!)
        CompareBy.UPSCALING -> repository.getAvgFpsByUpscaling(...)
        CompareBy.GPU -> repository.getAvgFpsByGpu(...)
        else -> emptyList()
    }
    
    // Renderizar filtros + gráfico
}
```

### 2. Adicionar Biblioteca de Gráficos

Considerar adicionar uma biblioteca de charts:

**Opção 1: Vico (Recomendado para Compose)**
```kotlin
implementation("com.patrykandpatrick.vico:compose:1.13.1")
implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
```

**Opção 2: MPAndroidChart (Android only)**
```kotlin
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
```

**Opção 3: Canvas nativo do Compose (Multiplataforma)**
- Desenhar gráficos manualmente usando `Canvas`
- Mais trabalho, mas totalmente multiplataforma

### 3. Implementar Tela de Filtros Avançados

Criar `FilterScreen.kt` com:
- Seleção de jogo (dropdown)
- Seleção de GPU (dropdown)
- Seleção de CPU (dropdown)
- Seleção de preset (chips)
- Seleção de upscaling (chips)
- Botão "Aplicar Filtros"

### 4. Adicionar Exportação de Dados

```kotlin
// Exportar como CSV
fun exportToCSV(configs: List<HardwareConfig>): String {
    return configs.joinToString("\n") { config ->
        "${config.name},${config.systemInfo.gpu},${config.performanceStats.avgFps}"
    }
}

// Exportar como JSON
fun exportToJSON(configs: List<HardwareConfig>): String {
    return Json.encodeToString(configs)
}
```

### 5. Implementar Backup/Restore

```kotlin
// Backup completo do banco
suspend fun backupDatabase(): ByteArray {
    // Copiar arquivo .db para ByteArray
}

// Restore do banco
suspend fun restoreDatabase(data: ByteArray) {
    // Substituir arquivo .db
}
```

## Testando a Persistência

### Teste Manual

1. Rodar a aplicação
2. Importar um JSON do CapFrameX
3. Salvar configuração
4. Fechar e reabrir a aplicação
5. Verificar se os dados persistiram

### Teste de Queries

```kotlin
// No ViewModel ou em um teste
viewModelScope.launch {
    val repository = get<SqlDelightConfigRepository>()
    
    // Teste 1: Salvar e buscar
    repository.saveConfig(testConfig)
    val retrieved = repository.getConfigById(testConfig.id)
    assert(retrieved != null)
    
    // Teste 2: Filtro por jogo
    val bf6Configs = repository.getConfigsByGame("Battlefield 6")
    println("Encontrados ${bf6Configs.size} testes de BF6")
    
    // Teste 3: Query agregada
    val avgByPreset = repository.getAvgFpsByPreset("Battlefield 6", "RTX 4090")
    avgByPreset.forEach { 
        println("${it.graphics_preset}: ${it.avg_fps} FPS")
    }
}
```

## Recursos Adicionais

- [SQLDelight Documentation](https://cashapp.github.io/sqldelight/)
- [Koin Documentation](https://insert-koin.io/)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)

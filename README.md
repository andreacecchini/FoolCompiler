# FoolCompiler

## How to

To compile and execute `src/<fileName>.fool`:

```bash
./gradlew run --args="nomeFile"
```

To compile and execute on **visualsvm** `src/<fileName>.fool`:

```bash
./gradlew debug --args="fileName"
```

Examples:

```bash
./gradlew run --args="prova"
./gradlew debug --args="prova"
```

Input requirement:
- `fileName` is mandatory.s
- File must exists in `src/<nomeFile>.fool`.
- Error on bads `fileName`.

## Usefull gradle tasks

- `./gradlew build`
  - automatic format by `spotlessApply` before build.
  - grammar files are generated in `gen/` 
  -  artifacts are generated in `build/`.

- `./gradlew clean`
  - clean `build/`.
  - clean `gen/`.

Grammars (lexer/parser) can be generated selectively in `gen/`.

- `./gradlew generateCompilerGrammar`
- `./gradlew generateSvmGrammar`
- `./gradlew generateVisualsvmGrammar`

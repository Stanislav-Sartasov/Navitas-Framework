# Navitas Profiler
Android Studio plugin for energy consumption estimation.

## Features

- **Configuring**
    - Android module selection
    - Instrumented tests selection
    - Power profile selection
- **Profiling**
    - Using "NaviProf" gradle plugin
- **Analysis**
    - Mapping harvested raw logs and power profile in information about energy consumption of executed tests
- **Reporting**
    - Display information about energy consumption of executed tests in text format

## Technology stack

- Kotlin
- RxJava 2
- *MVVM*, *Repository* patterns

## Architecture

![Architeture](https://github.com/Stanislav-Sartasov/Navitas-Framework/blob/master/Navitas-Plugin/src/main/resources/pictures/Component-Diagram.png)

### Layers

- ***domain***
    - data models used in business logic
    - repository interfaces
- ***data***
    - other data models
    - repository implementations
- ***presentation***
    - UI components distributed by functionality by package
    - view models

### Other packages
   
- ***action*** – actions of the "Navitas Profiler"
- ***extensions*** – Kotlin extension functions
- ***tooling*** – various tool classes (parsers, analyzers, etc)

## Limitations

- Instrumenting build.gradle files written in Kotlin isn't supported
- Limitations of "NaviProf" gradle plugin (e.g. support for only one log format)

## Future Tasks

- Inclusion of new components in the analysis (Wi-Fi, Bluetooth, etc)
- Providing the ability to compare profiling results
- Adding a graphical representation of energy consumption information
- Adding new types of reports (e.g. energy consumption traces)

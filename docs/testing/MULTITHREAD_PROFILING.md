# Multithread Profiling

This project should be reviewed in multi-threaded mode with measurements, not only by code size.

## Goal

Compare `sim.threads=1/2/4/8` for:

- tick progress over fixed wall-clock time
- process CPU usage
- heap usage
- GC pause metric

## Prerequisites

- backend starts locally on `http://127.0.0.1:8080`
- actuator endpoints are enabled
- `curl` is available

## Run One Profile

Start the backend with a concrete thread count:

```bash
mvn spring-boot:run -pl island-app -Dspring-boot.run.arguments=--sim.threads=4
```

In another terminal, start or restart the simulation if needed:

```bash
curl -X POST "http://127.0.0.1:8080/api/v1/simulation/start?type=NATURE&width=20&height=20&tickMs=100"
```

Collect a 30-second profile:

```bash
bash scripts/profile-multithreading.sh http://127.0.0.1:8080 30 /tmp/profile_threads_4.csv
```

## Sweep 1/2/4/8 Threads

Run the same sequence for:

```text
--sim.threads=1
--sim.threads=2
--sim.threads=4
--sim.threads=8
```

Recommended output files:

```text
/tmp/profile_threads_1.csv
/tmp/profile_threads_2.csv
/tmp/profile_threads_4.csv
/tmp/profile_threads_8.csv
```

## What To Compare

- end tick count after 30 seconds
- average and peak `process.cpu.usage`
- heap growth trend
- `jvm.gc.pause` values

## Review Output Template

Use this short format in review notes:

```text
threads=1: ticks=?, cpu avg=?, heap max=?, gc pause max=?
threads=2: ticks=?, cpu avg=?, heap max=?, gc pause max=?
threads=4: ticks=?, cpu avg=?, heap max=?, gc pause max=?
threads=8: ticks=?, cpu avg=?, heap max=?, gc pause max=?

Observed bottleneck:
Scaling conclusion:
Recommended default thread count:
```

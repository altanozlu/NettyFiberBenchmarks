**Hello world Benchmark:**

Results for Netty
```
     Running 5s test @ http://127.0.0.1:8080/index.html
       1 threads and 400 connections
       Thread Stats   Avg      Stdev     Max   +/- Stdev
         Latency     2.14ms  554.25us  15.12ms   87.18%
         Req/Sec    98.03k    14.64k  119.55k    62.00%
       491363 requests in 5.05s, 49.67MB read
       Socket errors: connect 0, read 5, write 0, timeout 0
     Requests/sec:  97303.57
     Transfer/sec:      9.84MB
     
     Running 20s test @ http://127.0.0.1:8080/index.html
       2 threads and 1000 connections
       Thread Stats   Avg      Stdev     Max   +/- Stdev
         Latency     7.34ms    2.40ms  21.49ms   66.09%
         Req/Sec    55.73k     4.15k   67.07k    73.25%
       2218372 requests in 20.02s, 224.25MB read
       Socket errors: connect 0, read 1042, write 4, timeout 0
     Requests/sec: 110797.73
     Transfer/sec:     11.20MB
```
Results for Netty with fibers after warmup
```
    Running 5s test @ http://127.0.0.1:8080/index.html
      1 threads and 400 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     2.04ms  710.58us  18.26ms   94.31%
        Req/Sec   105.55k    14.11k  124.26k    60.00%
      528085 requests in 5.04s, 53.38MB read
      Socket errors: connect 0, read 23, write 0, timeout 0
    Requests/sec: 104733.28
    Transfer/sec:     10.59MB
    
    Running 20s test @ http://127.0.0.1:8080/index.html
      2 threads and 1000 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     7.71ms    2.15ms  26.11ms   70.14%
        Req/Sec    55.75k     4.44k   69.93k    71.75%
      2218428 requests in 20.02s, 224.26MB read
      Socket errors: connect 0, read 1004, write 23, timeout 0
    Requests/sec: 110828.35
    Transfer/sec:     11.20MB

```

**MongoDB Benchmark:**

Results for Netty
```
     Running 5s test @ http://127.0.0.1:8080/index.html
       1 threads and 400 connections
       Thread Stats   Avg      Stdev     Max   +/- Stdev
         Latency    18.47ms    8.63ms  39.62ms   61.55%
         Req/Sec    21.68k     3.41k   25.81k    54.00%
       107777 requests in 5.01s, 16.86MB read
       Socket errors: connect 0, read 2, write 0, timeout 0
     Requests/sec:  21495.16
     Transfer/sec:      3.36MB
```
Results for Netty with fibers after warmup
```
    Running 5s test @ http://127.0.0.1:8080/index.html
      1 threads and 400 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency    33.39ms    4.92ms  81.96ms   61.13%
        Req/Sec    12.00k     1.69k   15.00k    76.00%
      59634 requests in 5.01s, 9.33MB read
    Requests/sec:  11902.87
    Transfer/sec:      1.86MB

```


**Info about my configuration**
```
MongoDB server version: 4.0.3
MongoDB driver version: 3.10.1
Netty version: 4.1.34


MacOS Mojave (10.14.4)
i7 6700K
24GB 1867MHz DDR3
```
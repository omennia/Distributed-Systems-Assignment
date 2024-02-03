We start the five peers at five different terminals:
```java
./run 1

./run 2

./run 3

./run 4

./run 5
```
The run command is equivalent to executing:
```java
gradle run_peer --args="mx", where x is an identifier for the peer
```


We also start the calculator server:
```java
gradle run_calculator
```



We run the injector to inject the token into a peer,
which starts the execution:
```java
gradle run_injector
```
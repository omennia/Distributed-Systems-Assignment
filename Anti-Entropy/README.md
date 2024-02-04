To run a topology of peers we run a peer in each terminal.
The way to run peer **k** is to run the command:
```shell
gradle run_peer --args"mk mx1 mx2 ... mxn"
```
Where x1, x2, ..., xn are the identifiers of the peers we want to connect to.


As an example, we will run 6 peers, in 6 different terminals.
```shell
./run 1  || Equivalent to:  gradle run_peer --args="m1 m2"

./run 2  || Equivalent to:  gradle run_peer --args="m2 m1 m3 m4"

./run 3  || Equivalent to:  gradle run_peer --args="m3 m2"

./run 4  || Equivalent to:  gradle run_peer --args="m4 m2 m5 m6"

./run 5  || Equivalent to:  gradle run_peer --args="m5 m4"

./run 6  || Equivalent to:  gradle run_peer --args="m6 m4"
```



After setting up our peers, we finally run the injector, to get the process starting.
```shell
gradle run_injector
```
*It is important that the number of peers matches the one specified in the file config.properties*

To run this program, we only need to specify the peer number.
We can start a peer by running the command:
```shell
gradle run_peer --args="mk" # Let k be the peer id, for example k=1.
```

Here is an example:

In 6 separate terminals, we start each peer by running:
```shell
./run 1

./run 2

./run 3

./run 4

./run 5

./run 6
```

And finally we start the whole process, by running the injector
```shell
gradle run_injector
```

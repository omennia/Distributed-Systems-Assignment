Em 6 terminais correr os seguintes comandos (Exemplo do enunciado):

	./run 1  || Equivalente a:  gradle run_peer --args="m1 m2"

	./run 2  || Equivalente a:  gradle run_peer --args="m2 m1 m3 m4"

	./run 3  || Equivalente a:  gradle run_peer --args="m3 m2"

	./run 4  || Equivalente a:  gradle run_peer --args="m4 m2 m5 m6"

	./run 5  || Equivalente a:  gradle run_peer --args="m5 m4"

	./run 6  || Equivalente a:  gradle run_peer --args="m6 m4"




E depois correr o injetor para começar o programa:

	gradle run_injector
